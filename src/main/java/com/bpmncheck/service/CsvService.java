package com.bpmncheck.service;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import com.bpmncheck.model.Log;

public class CsvService {

	public List<Log> readLogCsvFile(String path) {
		List<Log> logs = new ArrayList<Log>();

		try {
			File file = new File(path);
			InputStream fileStream = new FileInputStream(file);
			BufferedReader bufferFile = new BufferedReader(new InputStreamReader(fileStream));
			bufferFile.readLine(); //Pula o cabeçalho do arquivo
			String line = bufferFile.readLine();
			while (line != null) {
				Log log = new Log();
				log.setId(line.split(",")[0]);
				int index = logs.indexOf(log);
				if (index != -1) {
					logs.get(index).getEvents().add(line.split(",")[1]);
				} else {
					log.getEvents().add(line.split(",")[1]);
					logs.add(log);
				}
				line = bufferFile.readLine();
			}
			bufferFile.close();
		} catch (IOException e) {
			System.out.println("Erro na leitura do CSV.");
		}
		
		return logs;
	}
	
	public void saveLogCheckResultTrue(List<Log> logs, String path) {
		List<Log> logsTrue = new ArrayList<>();
		for(Log log: logs) {
			if(log.getResult()) {
				logsTrue.add(log);
			}
		}
		saveLogCsvFile(logsTrue, path);
	}
	
	public void saveLogCheckResultFalse(List<Log> logs, String path) {
		List<Log> logsFalse = new ArrayList<>();
		for(Log log: logs) {
			if(!log.getResult()) {
				logsFalse.add(log);
			}
		}
		saveLogCsvFile(logsFalse, path);
	}
	
	public void saveLogCsvFile(List<Log> logs, String path) {
		try {
			FileWriter fileWriter = new FileWriter (path);
            BufferedWriter bufferFile = new BufferedWriter (fileWriter);
            bufferFile.write("case,event"); //Grava Cabeçalho
            bufferFile.newLine();
            for(Log log : logs) {
            		for(String event: log.getEvents()) {
            			bufferFile.write(log.getId() + "," + event);
            			bufferFile.newLine();
            		}
            }
            bufferFile.close();
		} catch (IOException e) {
			System.out.println("Erro gravando arquivo CSV.");
		}
		
	}
 }
