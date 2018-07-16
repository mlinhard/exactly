package sk.linhard.exactly.gui;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.io.File;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;

import sk.linhard.exactly.impl.IndexingProgressReporter;

public class LoadingPanel extends JPanel {

	private JLabel label;
	private JLabel textRoot;
	private JLabel textFiles;
	private JLabel textBytes;

	public LoadingPanel() {
		setLayout(new BorderLayout(0, 0));

		JPanel centerPanel = new JPanel();
		add(centerPanel, BorderLayout.CENTER);
		GridBagLayout centerPanelLayout = new GridBagLayout();
		centerPanelLayout.columnWidths = new int[] { 100, 300 };
		centerPanelLayout.rowHeights = new int[] { 20, 20, 20, 20 };
		centerPanelLayout.columnWeights = new double[] { 0.0, Double.MIN_VALUE };
		centerPanelLayout.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0 };
		centerPanel.setLayout(centerPanelLayout);

		label = new JLabel("Please select a folder to index");
		label.setHorizontalAlignment(SwingConstants.CENTER);
		Border labelBorder = new EmptyBorder(4, 4, 4, 4);
		label.setBorder(labelBorder);
		GridBagConstraints labelGBC = new GridBagConstraints();
		labelGBC.gridx = 0;
		labelGBC.gridy = 0;
		labelGBC.gridwidth = 2;
		labelGBC.gridheight = 1;

		JLabel labelRoot = new JLabel("Root");
		labelRoot.setBorder(labelBorder);
		GridBagConstraints labelRootGBC = new GridBagConstraints();
		labelRootGBC.gridx = 0;
		labelRootGBC.gridy = 1;
		labelRootGBC.anchor = GridBagConstraints.WEST;

		JLabel labelFiles = new JLabel("Files");
		labelFiles.setBorder(labelBorder);
		GridBagConstraints labelFilesGBC = new GridBagConstraints();
		labelFilesGBC.gridx = 0;
		labelFilesGBC.gridy = 2;
		labelFilesGBC.anchor = GridBagConstraints.WEST;

		JLabel labelBytes = new JLabel("Bytes");
		labelBytes.setBorder(labelBorder);
		labelBytes.setHorizontalTextPosition(JLabel.LEFT);
		GridBagConstraints labelBytesGBC = new GridBagConstraints();
		labelBytesGBC.gridx = 0;
		labelBytesGBC.gridy = 3;
		labelBytesGBC.anchor = GridBagConstraints.WEST;

		textRoot = new JLabel("-");
		textRoot.setBorder(labelBorder);
		GridBagConstraints textRootGBC = new GridBagConstraints();
		textRootGBC.gridx = 1;
		textRootGBC.gridy = 1;
		textRootGBC.anchor = GridBagConstraints.CENTER;

		textFiles = new JLabel("-");
		textFiles.setHorizontalAlignment(SwingConstants.RIGHT);
		textFiles.setBorder(labelBorder);
		GridBagConstraints textFilesGBC = new GridBagConstraints();
		textFilesGBC.gridx = 1;
		textFilesGBC.gridy = 2;
		textFilesGBC.anchor = GridBagConstraints.EAST;

		textBytes = new JLabel("-");
		textBytes.setHorizontalAlignment(SwingConstants.RIGHT);
		textBytes.setBorder(labelBorder);
		GridBagConstraints textBytesGBC = new GridBagConstraints();
		textBytesGBC.gridx = 1;
		textBytesGBC.gridy = 3;
		textBytesGBC.anchor = GridBagConstraints.EAST;

		centerPanel.add(label, labelGBC);
		centerPanel.add(labelRoot, labelRootGBC);
		centerPanel.add(labelFiles, labelFilesGBC);
		centerPanel.add(labelBytes, labelBytesGBC);
		centerPanel.add(textRoot, textRootGBC);
		centerPanel.add(textFiles, textFilesGBC);
		centerPanel.add(textBytes, textBytesGBC);

	}

	public void reportRoot(File file) {
		textRoot.setText(file.getAbsolutePath());
	}

	public void reportProgress(IndexingProgressReporter.IndexingProgress progress) {
		if (!progress.isDoneCrawling()) {
			label.setText("Analysing files ...");
			textFiles.setText(progress.getFormattedCrawlingProgressFiles());
			textBytes.setText(progress.getFormattedCrawlingProgressBytes());
		} else if (!progress.isDoneLoading()) {
			label.setText("Loading data ...");
			textFiles.setText(progress.getFormattedLoadingProgressFiles() //
					+ " / " + progress.getFormattedCrawlingProgressFiles());
			textBytes.setText(progress.getFormattedLoadingProgressBytes() //
					+ " / " + progress.getFormattedCrawlingProgressBytes());
		} else if (!progress.isDoneIndexing()) {
			label.setText("Indexing ...");
			textFiles.setText(progress.getFormattedLoadingProgressFiles());
			textBytes.setText(progress.getFormattedLoadingProgressBytes());
		} else {
			label.setText("Enter a search query");
			textFiles.setText(progress.getFormattedLoadingProgressFiles());
			textBytes.setText(progress.getFormattedLoadingProgressBytes());
		}
	}

}
