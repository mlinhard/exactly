package sk.linhard.exactly.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sk.linhard.exactly.impl.IndexingProgressReporter;

public class MainWindow extends JFrame {

	private static final Logger log = LoggerFactory.getLogger(MainWindow.class);
	private JTextField textField;
	private EntryPanel entryPanel;
	private ContentPanel contentPanel;
	private JPanel searchPanel;
	private Core core;
	private String lastSearch;

	public MainWindow(Core core) {
		this.core = core;
		setTitle("Search");
		setSize(960, 480);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(EXIT_ON_CLOSE);

		MainMenuBar mainMenu = new MainMenuBar(this.core);
		setJMenuBar(mainMenu);
		getContentPane().setLayout(new BorderLayout(0, 0));

		searchPanel = new JPanel();
		getContentPane().add(searchPanel);
		searchPanel.setLayout(new BorderLayout(0, 0));

		JPanel panSearchBar = new JPanel();
		panSearchBar.setBorder(new EmptyBorder(2, 2, 2, 2));
		panSearchBar.setPreferredSize(new Dimension(0, 40));
		searchPanel.add(panSearchBar, BorderLayout.NORTH);

		JLabel lblSearchQuery = new JLabel("Search query:");
		lblSearchQuery.setVerticalAlignment(SwingConstants.CENTER);
		lblSearchQuery.setBorder(new EmptyBorder(2, 2, 2, 2));
		lblSearchQuery.setSize(100, 50);
		panSearchBar.add(lblSearchQuery);

		textField = new JTextField();
		textField.setEditable(false);

		textField.addKeyListener(new KeyListener() {

			@Override
			public void keyTyped(KeyEvent e) {

			}

			@Override
			public void keyReleased(KeyEvent e) {
				String newSearch = textField.getText();
				if (lastSearch == null || !lastSearch.equals(newSearch)) {
					if (newSearch != null && !newSearch.isEmpty()) {
						lastSearch = newSearch;
						log.debug("Query updated: {}", lastSearch);
						entryPanel.setSearchResult(core.find(lastSearch));
					}
				}
			}

			@Override
			public void keyPressed(KeyEvent e) {

			}
		});

		panSearchBar.add(textField);
		textField.setColumns(10);

		entryPanel = new EntryPanel(core);
		contentPanel = new ContentPanel(core);

		searchPanel.add(entryPanel, BorderLayout.CENTER);

	}

	public void exitHit() {
		getContentPane().remove(contentPanel);
		getContentPane().add(searchPanel);
		JPanel p = (JPanel) getContentPane();
		p.updateUI();
	}

	public void reportRoot(File file) {
		textField.setText("");
		textField.setEditable(false);
		entryPanel.reportRoot(file);
	}

	public void reportProgress(IndexingProgressReporter.IndexingProgress progress) {
		textField.setEditable(progress.isDoneIndexing());
		entryPanel.reportProgress(progress);
	}

	public void selectItem(SearchResultItem selectedItem) {
		getContentPane().remove(searchPanel);
		contentPanel.setContent(selectedItem);
		getContentPane().add(contentPanel);
		JPanel p = (JPanel) getContentPane();
		p.updateUI();
	}

}
