package sk.linhard.exactly.gui;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.ArrayList;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sk.linhard.exactly.SearchResult;
import sk.linhard.exactly.impl.IndexingProgressReporter;

public class EntryPanel extends JPanel implements ListSelectionListener {

	private static final Logger log = LoggerFactory.getLogger(EntryPanel.class);

	private EntryTable entryTable;
	private JScrollPane entryTableScrollPane;
	private LoadingPanel waitingPanel;
	private SearchResultItem selectedItem;
	private int maxCtx = 40;

	public EntryPanel(Core core) {
		super();

		setLayout(new GridLayout(0, 1, 0, 0));
		setBorder(new EmptyBorder(4, 4, 4, 4));

		entryTable = new EntryTable(new ArrayList<>());
		entryTable.setRowHeight(20);
		entryTable.setFillsViewportHeight(true);

		ListSelectionModel selmodel = entryTable.getSelectionModel();
		selmodel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		selmodel.addListSelectionListener(this);

		InputMap im = entryTable.getInputMap(JComponent.WHEN_FOCUSED);
		ActionMap am = entryTable.getActionMap();

		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "onEnter");

		am.put("onEnter", new AbstractAction() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (selectedItem != null) {
					log.debug("NOW ENTERING: {}", selectedItem.matchLine(maxCtx));
					core.selectItem(selectedItem);
				}
			}
		});

		entryTableScrollPane = new JScrollPane(entryTable);

		waitingPanel = new LoadingPanel();

		setSearchResult(null);
	}

	public void setSearchResult(SearchResult<String> searchResult) {
		remove(entryTableScrollPane);
		remove(waitingPanel);
		if (searchResult == null) {
			add(waitingPanel);
		} else {
			entryTable.setSearchResult(searchResult);
			add(entryTableScrollPane);
		}
		updateUI();
	}

	@Override
	public void valueChanged(ListSelectionEvent e) {
		int selectedRow = entryTable.getSelectedRow();
		if (!e.getValueIsAdjusting() && selectedRow != -1) {
			selectedItem = entryTable.getEntryAt(selectedRow);
			log.debug("Selected entry: '{}'", selectedItem.matchLine(maxCtx));
		}
	}

	public void reportRoot(File file) {
		setSearchResult(null);
		waitingPanel.reportRoot(file);
	}

	public void reportProgress(IndexingProgressReporter.IndexingProgress progress) {
		waitingPanel.reportProgress(progress);
	}

}
