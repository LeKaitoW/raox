package ru.bmstu.rk9.rao.lib.result;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import ru.bmstu.rk9.rao.lib.json.JSONObject;

public class LogStatistics<T> extends Statistics<T> {

	public final static String defaultDelimeter = "; ";
	private String delimeter;
	private File logFile;
	private BufferedWriter logFileBuffer = null;

	public LogStatistics(String FileName, String delimeter) {
		this.logFile = new File(FileName);
		this.delimeter = delimeter;

		FileWriter logFileWriter = null;
		try {
			logFileWriter = new FileWriter(logFile.getAbsoluteFile());
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		logFileBuffer = new BufferedWriter(logFileWriter);
	}

	public LogStatistics(String FileName) {
		this(FileName, defaultDelimeter);
	}

	@Override
	public void updateData(JSONObject data) {
		data.put("Log file",
				logFileBuffer != null ? logFile.getAbsolutePath() : "Error: output file cannot be written");
	}

	@Override
	public void update(T value, double currentTime) {
		if (logFileBuffer == null)
			return;

		try {
			logFileBuffer.write(String.valueOf(currentTime) + delimeter + value.toString());
			logFileBuffer.newLine();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void prepareData() {
		if (logFileBuffer == null)
			return;

		try {
			logFileBuffer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		logFileBuffer = null;
	}
}
