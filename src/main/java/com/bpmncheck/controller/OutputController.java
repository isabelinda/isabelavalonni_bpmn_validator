package com.bpmncheck.controller;

import com.bpmncheck.view.OutputFrame;

public class OutputController {
	
	private OutputFrame view;
	
	public void register(OutputFrame view) {
		this.view = view;
	}
	
	public void disposeView() {
		this.view.dispose();
	}
	
}
