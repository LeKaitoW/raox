package ru.bmstu.rk9.rdo.lib;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import ru.bmstu.rk9.rdo.lib.json.JSONArray;
import ru.bmstu.rk9.rdo.lib.json.JSONObject;

public class Tracer implements Subscriber
{
	private TraceInfo traceInfo;

	//TODO implement method to concatenate strings with delimiter
	private final String delimiter = " ";

	public Tracer()
	{
		this.traceInfo = new TraceInfo();
	}

	public TraceInfo getTraceInfo()
	{
		return this.traceInfo;
	}

	public void setTraceInfo(TraceInfo traceInfo)
	{
		this.traceInfo = traceInfo;
	}

	private ArrayList<String> traceText = new ArrayList<String>();

	public ArrayList<String> getTraceText()
	{
		return traceText;
	}

	//TODO do we even need this as a separate method?
	private Database.EntryType getEntryType(Database.Entry entry)
	{
		// TODO maybe change 8 to something more intelligible
		switch (entry.header.get(8))
		{
			case 0:
				return Database.EntryType.SYSTEM;
			case 1:
				return Database.EntryType.RESOURCE;
			case 2:
				return Database.EntryType.PATTERN;
			case 3:
				return Database.EntryType.DECISION;
			case 4:
				return Database.EntryType.RESULT;
			default:
				return null;
		}
	}

	private String parseResourceParameters(ByteBuffer resourceData, JSONObject structure)
	{
		String paramsTraceLine = "";
		//TODO is that what should be checked?
		if (resourceData.limit() == 0)
		{
			//TODO implement exception?
			return "";
		}

		resourceData.duplicate();
		resourceData.rewind();

		JSONArray parameters = structure.getJSONArray("parameters");

		for (int paramNum = 0; paramNum < parameters.length(); paramNum++)
		{
			JSONObject currentParameter = parameters.getJSONObject(paramNum);
			//TODO what about arrays?
			switch(currentParameter.getString("type"))
			{
			case "integer":
				paramsTraceLine += String.valueOf(resourceData.getInt());
				break;
			case "real":
				paramsTraceLine += String.valueOf(resourceData.getDouble());
				break;
			case "boolean":
				paramsTraceLine += String.valueOf(new Byte(resourceData.get()));
				break;
			case "enum":
				paramsTraceLine += String.valueOf(resourceData.getShort());
				break;
			case "string":
				int index = currentParameter.getInt("index");
				int stringPosition = resourceData.getInt(structure.getInt("last_offset") + (index - 1) * 4);
				int length = resourceData.getInt(stringPosition);

				byte rawString[] = new byte[length];
				for (int i = 0; i < length; i++)
				{
					rawString[i] = resourceData.get(stringPosition + 4 + i);
				}
				//TODO UTF_8? Cyrillic strings don't work, is that expected?
				paramsTraceLine += "\"" + new String(rawString, StandardCharsets.UTF_8) + "\"";
				break;
			default:
				//TODO implement exception?
				break;
			}
			paramsTraceLine += delimiter;
		}

		return paramsTraceLine;
	}

	private String parseResourceEntry(Database.Entry entry)
	{
		ByteBuffer resourceHeader = entry.header;
		//TODO is that what should be checked?
		if (resourceHeader.limit() == 0)
		{
			//TODO implement exception?
			return "";
		}

		resourceHeader.duplicate();
		resourceHeader.rewind();

		double time = resourceHeader.getDouble();
		//TODO empty get looks pretty ugly
		resourceHeader.get();
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
		int typeNum = resourceHeader.getInt();
		int resNum = resourceHeader.getInt();

		String headerLine =
			"R" + status + delimiter +
			String.valueOf(time) + delimiter +
			String.valueOf(typeNum) + delimiter +
			String.valueOf(resNum);

		if (status == "E")
		{
			return headerLine;
		}

		JSONObject structure =
			Simulator
			.getDatabase()
			.getModelStructure()
			.getJSONArray("resource_types")
			.getJSONObject(typeNum)
			.getJSONObject("structure");

		return
			headerLine + delimiter +
			parseResourceParameters(entry.data, structure);
	}

	private String parseResultParameter(ByteBuffer resultData, String valueType)
	{
		if (resultData.limit() == 0)
		{
			//TODO implement exception?
			return "";
		}

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
			//TODO which way is better, this or the one in resources?
			ByteArrayOutputStream rawString = new ByteArrayOutputStream();
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

	private String parseResultEntry(Database.Entry entry)
	{
		ByteBuffer resultHeader = entry.header;
		//TODO is that what should be checked?
		if (resultHeader.limit() == 0)
		{
			//TODO implement exception?
			return "";
		}

		resultHeader.duplicate();
		resultHeader.rewind();

		double time = resultHeader.getDouble();
		//TODO empty get looks pretty ugly
		resultHeader.get();
		int resultNum = resultHeader.getInt();

		String valueType =
				Simulator
				.getDatabase()
				.getModelStructure()
				.getJSONArray("results")
				.getJSONObject(resultNum)
				.getString("value_type");

		return
			"V" + delimiter +
			String.valueOf(time) + delimiter +
			String.valueOf(resultNum) + delimiter +
			parseResultParameter(entry.data, valueType);
	}

	private String parseSerializedData(Database.Entry entry)
	{
		Database.EntryType type = getEntryType(entry);
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

	public void saveTraceData()
	{
		ArrayList<Database.Entry> entries = Simulator.getDatabase().allEntries;

		for (Database.Entry entry : entries)
		{
			String entryText = parseSerializedData(entry);
			traceText.add(entryText + "\n");
		}
	}

	@Override
	public void fireChange() {}
}
