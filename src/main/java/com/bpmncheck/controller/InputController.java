package com.bpmncheck.controller;

import java.io.File;
import java.util.List;

import com.bpmncheck.model.Log;
import com.bpmncheck.service.BpmnService;
import com.bpmncheck.service.CsvService;
import com.bpmncheck.view.InputFrame;
import com.bpmncheck.view.OutputFrame;

public class InputController {

	private InputFrame view;
	private BpmnService bpmnService;
	private CsvService csvService;

	public void register(InputFrame view) {
		this.view = view;
		this.bpmnService = new BpmnService();
		this.csvService = new CsvService();
	}

	public void checkBpmn(String pathBpmn, String pathCsv) {
		List<Log> logs = bpmnService.checkAllBpmnLogs(this.bpmnService.readFile(pathBpmn), this.csvService.readLogCsvFile(pathCsv));
		//Salvar arquivos de resultado
		File file = new File(pathCsv);
		String pathSaveTrues = file.getPath().replace(file.getName(), file.getName().replace(".csv", "_true.csv"));
		String pathSaveFalses = file.getPath().replace(file.getName(), file.getName().replace(".csv", "_false.csv"));
		csvService.saveLogCheckResultTrue(logs, pathSaveTrues);
		csvService.saveLogCheckResultFalse(logs, pathSaveFalses);
		//Abre a tela output
		OutputFrame outputFrame = new OutputFrame(logs, pathBpmn, pathCsv, pathSaveTrues, pathSaveFalses);
		outputFrame.setVisible(true);
	}

	public Boolean fileExist(String path) {
		File file = new File(path);
		return file.exists();
	}
}
