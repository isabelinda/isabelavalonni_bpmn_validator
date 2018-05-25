package com.bpmncheck.view;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.filechooser.FileSystemView;

import com.bpmncheck.controller.InputController;

public class InputFrame extends JFrame {

	private static final long serialVersionUID = 1L;

	private InputController controller;

	private Icon iconSearch = new ImageIcon(getClass().getClassLoader().getResource("search.png"));
	private Icon iconCheck = new ImageIcon(getClass().getClassLoader().getResource("check.png"));

	private JLabel lblFileBpmn = new JLabel("Caminho do arquivo BPMN (.bpmn):");
	private JTextField tfFileBpmn = new JTextField("");
	private JButton btnSelectFileBpmn = new JButton(iconSearch);
	private JLabel lblFileCsv = new JLabel("Caminho do arquivo Log (.csv):");
	private JTextField tfFileCsv = new JTextField("");
	private JButton btnSelectFileCsv = new JButton(iconSearch);
	private JButton btnCheck = new JButton("Check", iconCheck);

	public InputFrame() {
		this.setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getClassLoader().getResource("bpmn.png")));
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setResizable(false);
		setTitle("BPMN Check");
		setSize(320, 160);
		setLocationRelativeTo(null);

		controller = new InputController();
		controller.register(this);

		buildView();
		registerActions();
	}

	private void buildView() {
		GridBagLayout layout = new GridBagLayout();
		getContentPane().setLayout(layout);

		GridBagConstraints gbc_lblFileBpmn = new GridBagConstraints();
		gbc_lblFileBpmn.insets = new Insets(0, 0, 5, 5);
		gbc_lblFileBpmn.gridx = 1;
		gbc_lblFileBpmn.gridy = 1;
		lblFileBpmn.setHorizontalTextPosition(SwingConstants.CENTER);
		lblFileBpmn.setVerticalTextPosition(SwingConstants.BOTTOM);
		getContentPane().add(lblFileBpmn, gbc_lblFileBpmn);

		GridBagConstraints gbc_tfFileBpmn = new GridBagConstraints();
		gbc_tfFileBpmn.insets = new Insets(0, 0, 5, 5);
		gbc_tfFileBpmn.fill = GridBagConstraints.HORIZONTAL;
		gbc_tfFileBpmn.gridx = 1;
		gbc_tfFileBpmn.gridy = 2;
		tfFileBpmn.setColumns(20);
		tfFileBpmn.setEnabled(false);
		getContentPane().add(tfFileBpmn, gbc_tfFileBpmn);

		GridBagConstraints gbc_btnSelectFileBpmn = new GridBagConstraints();
		gbc_btnSelectFileBpmn.insets = new Insets(0, 0, 5, 5);
		gbc_btnSelectFileBpmn.gridx = 2;
		gbc_btnSelectFileBpmn.gridy = 2;
		getContentPane().add(btnSelectFileBpmn, gbc_btnSelectFileBpmn);

		GridBagConstraints gbc_lblFileCsv = new GridBagConstraints();
		gbc_lblFileCsv.insets = new Insets(0, 0, 5, 5);
		gbc_lblFileCsv.gridx = 1;
		gbc_lblFileCsv.gridy = 3;
		lblFileCsv.setHorizontalTextPosition(SwingConstants.CENTER);
		lblFileCsv.setVerticalTextPosition(SwingConstants.BOTTOM);
		getContentPane().add(lblFileCsv, gbc_lblFileCsv);

		GridBagConstraints gbc_tfFileCsv = new GridBagConstraints();
		gbc_tfFileCsv.insets = new Insets(0, 0, 5, 5);
		gbc_tfFileCsv.fill = GridBagConstraints.HORIZONTAL;
		gbc_tfFileCsv.gridx = 1;
		gbc_tfFileCsv.gridy = 4;
		tfFileCsv.setColumns(20);
		tfFileCsv.setEnabled(false);
		getContentPane().add(tfFileCsv, gbc_tfFileCsv);

		GridBagConstraints gbc_btnSelectFileCsv = new GridBagConstraints();
		gbc_btnSelectFileCsv.insets = new Insets(0, 0, 5, 5);
		gbc_btnSelectFileCsv.gridx = 2;
		gbc_btnSelectFileCsv.gridy = 4;
		getContentPane().add(btnSelectFileCsv, gbc_btnSelectFileCsv);

		GridBagConstraints gbc_btnCheck = new GridBagConstraints();
		gbc_btnCheck.insets = new Insets(0, 0, 0, 5);
		gbc_btnCheck.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnCheck.gridx = 0;
		gbc_btnCheck.gridy = 5;
		gbc_btnCheck.gridwidth = 3;
		getContentPane().add(btnCheck, gbc_btnCheck);
	}

	private void registerActions() {
		btnSelectFileBpmn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser jfcBpmn = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());
				jfcBpmn.setDialogTitle("Selecione o arquivo BPMN");
				jfcBpmn.setAcceptAllFileFilterUsed(false);
				FileNameExtensionFilter filter = new FileNameExtensionFilter("BPMN File", "bpmn");
				jfcBpmn.addChoosableFileFilter(filter);

				int returnValue = jfcBpmn.showOpenDialog(null);
				if (returnValue == JFileChooser.APPROVE_OPTION) {
					String path = jfcBpmn.getSelectedFile().getPath();
					tfFileBpmn.setText(path);
				}
			}
		});

		btnSelectFileCsv.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser jfcCsv = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());
				jfcCsv.setDialogTitle("Selecione o arquivo Log");
				jfcCsv.setAcceptAllFileFilterUsed(false);
				FileNameExtensionFilter filter = new FileNameExtensionFilter("CSV File", "csv");
				jfcCsv.addChoosableFileFilter(filter);

				int returnValue = jfcCsv.showOpenDialog(null);
				if (returnValue == JFileChooser.APPROVE_OPTION) {
					String path = jfcCsv.getSelectedFile().getPath();
					tfFileCsv.setText(path);
				}
			}
		});

		btnCheck.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if(controller.fileExist(tfFileBpmn.getText()) && controller.fileExist(tfFileCsv.getText())) {
					controller.checkBpmn(tfFileBpmn.getText(), tfFileCsv.getText());
				} else {
					JOptionPane.showMessageDialog(null, "Selecione um arquivo BPMN e um CSV válidos!", "Arquivo Inválido", JOptionPane.ERROR_MESSAGE);
				}
			}
		});
	}

}
