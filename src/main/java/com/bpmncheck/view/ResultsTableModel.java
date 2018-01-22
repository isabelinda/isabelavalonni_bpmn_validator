package com.bpmncheck.view;

import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import com.bpmncheck.model.Log;

public class ResultsTableModel extends AbstractTableModel {
	
	private static final long serialVersionUID = 1L;

	private List<Log> logs = new ArrayList<Log>();
	private String[] columns = { "Log Id", "Resultado" };

	@Override
	public int getColumnCount() {
		return columns.length;
	}

	@Override
	public int getRowCount() {
		return logs.size();
	}
	
	@Override
	public String getColumnName(int column) {
		return columns[column];
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		Log log = logs.get(rowIndex);

		if (columnIndex == 0) {
			return log.getId();
		}

		if (columnIndex == 1) {
			return log.getResult();
		}

		return null;
	}
	
	public void loadTable(List<Log> logs) {
		this.logs = logs;
		fireTableDataChanged();
	}

}
