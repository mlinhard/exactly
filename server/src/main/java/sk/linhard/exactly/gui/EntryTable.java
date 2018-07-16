package sk.linhard.exactly.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.util.List;

import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sk.linhard.exactly.SearchResult;

public class EntryTable extends JTable {

	private static final Logger log = LoggerFactory.getLogger(EntryTable.class);

	private static final String[] COLNAMES = { "Match" };

	private static final int[] COLWIDTH = { 960 };
	private int maxCtx = 40;

	static final Font FONT = createFont();

	public EntryTable(List<SearchResultItem> entries) {
		super(new EntryTableModel(entries));

		TableColumnModel cm = getColumnModel();
		for (int i = 0; i < COLWIDTH.length; i++) {
			TableColumn col = cm.getColumn(i);
			col.setPreferredWidth(COLWIDTH[i]);
		}
	}

	private static Font createFont() {
		return new Font("Liberation Mono", Font.PLAIN, 16);
	}

	private static final Color BG_NORMAL = new Color(255, 255, 255);
	private static final Color BG_SELECTED = new Color(128, 128, 255);
	private static final Color HIGHLIGHT = new Color(255, 128, 128);

	private class MatchCellRendererComponent extends JTextArea {

		public MatchCellRendererComponent(SearchResultItem item, boolean isSelected) {
			try {
				setBackground(isSelected ? BG_SELECTED : BG_NORMAL);
				setFont(FONT);
				setText(item.matchLine(maxCtx));
				getHighlighter().addHighlight(maxCtx, maxCtx + item.patternLength(),
						new DefaultHighlighter.DefaultHighlightPainter(HIGHLIGHT));
			} catch (BadLocationException e) {
				throw new RuntimeException(e);
			}
		}
	}

	private class MatchCellRenderer extends DefaultTableCellRenderer {
		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
				int row, int column) {
			if (column == 0) {
				return new MatchCellRendererComponent((SearchResultItem) value, isSelected);
			} else {
				return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			}
		}
	}

	@Override
	public TableCellRenderer getCellRenderer(int row, int column) {
		if (column == 0) {
			return new MatchCellRenderer();
		}
		return super.getCellRenderer(row, column);
	}

	@Override
	public EntryTableModel getModel() {
		return (EntryTableModel) super.getModel();
	}

	public SearchResultItem getEntryAt(int row) {
		return getModel().entries.get(row);
	}

	public void setSearchResult(SearchResult<String> searchResult) {
		List<SearchResultItem> items = SearchResultItem.toItems(100, searchResult);
		log.debug("Replacing table with {} new entries", items.size());
		setModel(new EntryTableModel(items));
	}

	private static class EntryTableModel extends AbstractTableModel {

		private List<SearchResultItem> entries;

		public EntryTableModel(List<SearchResultItem> entries) {
			this.entries = entries;
		}

		@Override
		public String getColumnName(int column) {
			return COLNAMES[column];
		}

		@Override
		public int getRowCount() {
			return entries.size();
		}

		@Override
		public int getColumnCount() {
			return COLNAMES.length;
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			return entries.get(rowIndex);
		}
	}
}
