package ru.bmstu.rk9.rao.lib.result;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import ru.bmstu.rk9.rao.lib.json.JSONObject;

public class LogStatistics<T> extends Statistics<T> {

	public static String defaultDelimeter = "; ";
	private boolean outputInitialized;
	private String delimeter;
	private File logFile;
	private final BufferedWriter out;

	public LogStatistics(String FileName, String delimeter) {
		this.logFile = new File(FileName);
		this.delimeter = delimeter;
		this.outputInitialized = true;

		FileWriter fileWriter = null;
		try {
			fileWriter = new FileWriter(logFile.getAbsoluteFile());
		} catch (IOException e) {
			outputInitialized = false;
			e.printStackTrace();
		}
		out = new BufferedWriter(fileWriter);
	}

	public LogStatistics(String FileName) {
		this(FileName, defaultDelimeter);
	}

	@Override
	public void updateData(JSONObject data) {
		data.put("Log file", outputInitialized ? logFile.getAbsolutePath() : "Error: output file cannot be written");
	}

	@Override
	public void update(T value, double currentTime) {
		if (!outputInitialized)
			return;

		try {
			out.write(String.valueOf(currentTime) + delimeter + value.toString());
			out.newLine();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void prepareData() {
		if (!outputInitialized)
			return;

		try {
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
