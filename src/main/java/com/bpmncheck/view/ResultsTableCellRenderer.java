package com.bpmncheck.view;

import java.awt.Component;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

public class ResultsTableCellRenderer extends DefaultTableCellRenderer {

	private static final long serialVersionUID = 1L;
	
	DefaultTableCellRenderer renderer;

	public ResultsTableCellRenderer(JTable table) {
		renderer = (DefaultTableCellRenderer) table.getTableHeader().getDefaultRenderer();
	    renderer.setHorizontalAlignment(JLabel.CENTER);
	}

	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
		this.setHorizontalAlignment(CENTER);
		if(column == 1) {
			Boolean trueOrFalse = (Boolean) value;
			JLabel lblIcon = new JLabel();
		    ImageIcon icon = null;
		    if (trueOrFalse) {
		      icon = new ImageIcon("src/main/resources/true.png");
		    } else {
		       icon = new ImageIcon("src/main/resources/false.png");
		    }
		    lblIcon.setHorizontalAlignment(JLabel.CENTER);
		    lblIcon.setVerticalAlignment(JLabel.CENTER);
		    lblIcon.setIcon(icon);
		    return lblIcon;
		}
		return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
	}
}
