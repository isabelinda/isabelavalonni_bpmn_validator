package com.bpmncheck.view;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.List;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;

import com.bpmncheck.controller.OutputController;
import com.bpmncheck.model.Log;

public class OutputFrame extends JDialog {

	private static final long serialVersionUID = 1L;
	
	private OutputController controller;
	
	private JTable tableResults = new JTable();
	private ResultsTableModel resultsTableModel = new ResultsTableModel();
	
	private Icon iconCheck = new ImageIcon(getClass().getClassLoader().getResource("check.png"));
	
	private JLabel lblBpmn = new JLabel("Arquivo BPMN: ");
	private JLabel lblFileBpmn = new JLabel("");
	private JLabel lblCsv  = new JLabel("Arquivo LOG: ");
	private JLabel lblFileCsv  = new JLabel("");
	private JLabel lblFileTrues  = new JLabel("Arquivo Log (.csv) casos positivos:"); 
	private JTextField tfFileTrues = new JTextField();
	private JLabel lblFileFalses = new JLabel("Arquivo Log (.csv) casos negativos:"); 
	private JTextField tfFileFalses = new JTextField();
	private JButton btnOk = new JButton("Ok", iconCheck);
	
	
	public OutputFrame(List<Log> logs, String pathBpmn, String pathCsv, String pathSaveTrues, String pathSaveFalses) {
		this.setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getClassLoader().getResource("bpmn.png")));
		setTitle("BPMN Check Results");
		setSize(320, 320);
		setLocationRelativeTo(null);
		setModal(true);

		controller = new OutputController();
		controller.register(this);

		buildView();
		registerActions();
		loadTable(logs);
		loadPaths(pathBpmn, pathCsv, pathSaveTrues, pathSaveFalses);
	}

	private void buildView() {
		GridLayout gridNorth = new GridLayout(2, 2);
		JPanel panelHigher = new JPanel(gridNorth);
		lblBpmn.setHorizontalAlignment(JLabel.RIGHT);
		panelHigher.add(lblBpmn);
		panelHigher.add(lblFileBpmn);
		lblCsv.setHorizontalAlignment(JLabel.RIGHT);
		panelHigher.add(lblCsv);
		panelHigher.add(lblFileCsv);
		getContentPane().add(panelHigher, BorderLayout.NORTH);
		
		tableResults.setModel(resultsTableModel);
		tableResults.setDefaultRenderer(Object.class, new ResultsTableCellRenderer(tableResults));
		JScrollPane tabelaScroll = new JScrollPane(tableResults);
		getContentPane().add(tabelaScroll);
		
		GridLayout gridSouth = new GridLayout(5, 1);
		JPanel panelBottom = new JPanel(gridSouth);
		lblFileTrues.setHorizontalAlignment(JLabel.CENTER);
		panelBottom.add(lblFileTrues);
		tfFileTrues.setEnabled(false);
		panelBottom.add(tfFileTrues);
		lblFileFalses.setHorizontalAlignment(JLabel.CENTER);
		panelBottom.add(lblFileFalses);
		tfFileFalses.setEnabled(false);
		panelBottom.add(tfFileFalses);
		panelBottom.add(btnOk);
		getContentPane().add(panelBottom, BorderLayout.SOUTH);
	}
	
	private void registerActions() {
		btnOk.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				controller.disposeView();
			}
		});
	}
	
	private void loadTable(List<Log> logs) {
		resultsTableModel.loadTable(logs);
	}
	
	private void loadPaths(String pathBpmn, String pathCsv, String pathSaveTrues, String pathSaveFalses) {
		File fileBpmn = new File(pathBpmn);
		File fileCsv = new File(pathCsv);
		lblFileBpmn.setText(fileBpmn.getName());
		lblFileCsv.setText(fileCsv.getName());
		tfFileTrues.setText(pathSaveTrues);
		tfFileFalses.setText(pathSaveFalses);
	}
	
}