package ru.bmstu.rk9.rdo.lib;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;

import ru.bmstu.rk9.rdo.lib.json.JSONArray;
import ru.bmstu.rk9.rdo.lib.json.JSONObject;

public final class Tracer implements Subscriber
{
	static private final String delimiter = " ";

	private HashMap<Integer, JSONObject> resourceTypeStructure = new HashMap<Integer, JSONObject>();
	private HashMap<Integer, String> resultValueTypes = new HashMap<Integer, String>();

	Tracer()
	{
		fillResourceTypeStructure();
		fillResultValueTypes();
	}

	private final void fillResourceTypeStructure()
	{
		final JSONArray resourceTypes =
			Simulator
			.getDatabase()
			.getModelStructure()
			.getJSONArray("resource_types");

		for (int typeNum = 0; typeNum < resourceTypes.length(); typeNum++)
		{
			resourceTypeStructure.put(typeNum, resourceTypes.getJSONObject(typeNum).getJSONObject("structure"));
		}
	}

	private final void fillResultValueTypes()
	{
		final JSONArray results =
			Simulator
			.getDatabase()
			.getModelStructure()
			.getJSONArray("results");

		for (int resultNum = 0; resultNum < results.length(); resultNum++)
		{
			resultValueTypes.put(resultNum, results.getJSONObject(resultNum).getString("value_type"));
		}
	}

	//TODO choose the proper container for traceText
	//TODO besides string it should contain type identifier for future
	//coloring in UI
	private ArrayList<String> traceText = new ArrayList<String>();

	public final ArrayList<String> getTraceText()
	{
		//TODO make unmodifiable
		return traceText;
	}

	private final String parseResourceParameters(final ByteBuffer resourceData, final JSONObject structure)
	{
		final StringJoin stringBuilder = new StringJoin(delimiter);

		resourceData.duplicate();
		resourceData.rewind();

		final JSONArray parameters = structure.getJSONArray("parameters");

		for (int paramNum = 0; paramNum < parameters.length(); paramNum++)
		{
			final JSONObject currentParameter = parameters.getJSONObject(paramNum);
			//TODO trace arrays when they are implemented
			switch(currentParameter.getString("type"))
			{
			case "integer":
				stringBuilder.add(String.valueOf(resourceData.getInt()));
				break;
			case "real":
				stringBuilder.add(String.valueOf(resourceData.getDouble()));
				break;
			case "boolean":
				stringBuilder.add(String.valueOf(new Byte(resourceData.get())));
				break;
			case "enum":
				stringBuilder.add(String.valueOf(resourceData.getShort()));
				break;
			case "string":
				final int index = currentParameter.getInt("index");
				final int stringPosition =
					resourceData.getInt(structure.getInt("last_offset") + (index - 1) * Integer.SIZE / Byte.SIZE);
				final int length = resourceData.getInt(stringPosition);

				byte rawString[] = new byte[length];
				for (int i = 0; i < length; i++)
				{
					rawString[i] = resourceData.get(stringPosition + 4 + i);
				}
				//TODO Will work in all cases after string serialization fixed
				stringBuilder.add("\"" + new String(rawString, StandardCharsets.UTF_8) + "\"");
				break;
			default:
				//TODO implement exception?
				break;
			}
		}
		return stringBuilder.getString();
	}

	private final String parseResourceEntry(final Database.Entry entry)
	{
		final ByteBuffer resourceHeader = entry.header;

		resourceHeader.duplicate();
		resourceHeader.rewind();

		final double time = resourceHeader.getDouble();
		skipByte(resourceHeader);
		String status = "";
		switch(resourceHeader.get())
		{
		case 0:
			status = "C";
			break;
		case 1:
			status = "E";
			break;
		case 2:
			status = "K";
			break;
		default:
			//TODO implement exception?
			break;
		}
		final int typeNum = resourceHeader.getInt();
		final int resNum = resourceHeader.getInt();

		final String headerLine =
			new StringJoin(delimiter)
			.add("R" + status)
			.add(String.valueOf(time))
			.add(String.valueOf(typeNum))
			.add(String.valueOf(resNum))
			.getString();

		//TODO fix when resource parameters are also serialized on erase
		if (status == "E")
		{
			return headerLine;
		}

		final JSONObject structure = resourceTypeStructure.get(typeNum);

		return
			new StringJoin(delimiter)
			.add(headerLine)
			.add(parseResourceParameters(entry.data, structure))
			.getString();
	}

	private final String parseResultParameter(final ByteBuffer resultData, final String valueType)
	{
		resultData.duplicate();
		resultData.rewind();

		switch(valueType)
		{
		case "integer":
			return String.valueOf(resultData.getInt());
		case "real":
			return String.valueOf(resultData.getDouble());
		case "boolean":
			return String.valueOf(new Byte(resultData.get()));
		case "enum":
			return String.valueOf(resultData.getShort());
		case "string":
			final ByteArrayOutputStream rawString = new ByteArrayOutputStream();
			while (resultData.hasRemaining())
			{
				rawString.write(resultData.get());
			}
			return "\"" + rawString.toString() + "\"";
		default:
			//TODO implement exception?
			break;
		}

		return "";
	}

	private final String parseResultEntry(final Database.Entry entry)
	{
		final ByteBuffer resultHeader = entry.header;

		resultHeader.duplicate();
		resultHeader.rewind();

		final double time = resultHeader.getDouble();
		skipByte(resultHeader);
		final int resultNum = resultHeader.getInt();

		final String valueType = resultValueTypes.get(resultNum);

		return
			new StringJoin(delimiter)
			.add("V")
			.add(String.valueOf(time))
			.add(String.valueOf(resultNum))
			.add(parseResultParameter(entry.data, valueType))
			.getString();
	}

	private final String parseSerializedData(final Database.Entry entry)
	{
		final Database.EntryType type = Database.EntryType.values()[entry.header.get(8)];
		switch(type)
		{
		//TODO implement the rest of EntryTypes
		case RESOURCE:
			return parseResourceEntry(entry);
		case RESULT:
			return parseResultEntry(entry);
		default:
			return "";
		}
	}

	public final void saveTraceData()
	{
		final ArrayList<Database.Entry> entries = Simulator.getDatabase().allEntries;

		for (Database.Entry entry : entries)
		{
			final String entryText = parseSerializedData(entry);
			traceText.add(entryText + (entryText == "" ? "" : "\n"));
		}
	}

	private final void skipByte(final ByteBuffer buffer)
	{
		buffer.get();
	}

	@Override
	public void fireChange() {}
}

//TODO use standard class when switched to Java8
class StringJoin
{
	private final String delimiter;

	private String current = null;

	public final String getString()
	{
		return current;
	}

	StringJoin(String delimiter)
	{
		this.delimiter = delimiter;
	}

	public final StringJoin add(final String toAppend)
	{
		if (current == null)
		{
			current = new String(toAppend);
		}
		else
		{
			current += delimiter + toAppend;
		}
		return this;
	}
}
