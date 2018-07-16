package sk.linhard.exactly.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;

import sk.linhard.exactly.HitContext;

public class ContentPanel extends JPanel {

	private Core core;
	private JTextArea contentTextPane;
	private JLabel lblFileName;

	public ContentPanel(Core core) {
		this.core = core;
		setLayout(new BorderLayout());

		JPanel panSearchBar = new JPanel();
		panSearchBar.setBorder(new EmptyBorder(2, 2, 2, 2));
		panSearchBar.setPreferredSize(new Dimension(0, 40));
		add(panSearchBar, BorderLayout.NORTH);

		lblFileName = new JLabel("Unknown file");
		lblFileName.setVerticalAlignment(SwingConstants.CENTER);
		lblFileName.setBorder(new EmptyBorder(2, 2, 2, 2));
		lblFileName.setSize(100, 50);
		panSearchBar.add(lblFileName);

		contentTextPane = new JTextArea();

		JScrollPane scrollPane = new JScrollPane(contentTextPane);
		// scrollPane.set
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		// scrollPane.setPreferredSize(new Dimension(250, 155));
		scrollPane.setMinimumSize(new Dimension(10, 10));

		contentTextPane.setFont(EntryTable.FONT);

		InputMap im = getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
		ActionMap am = getActionMap();

		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "onEsc");

		am.put("onEsc", new AbstractAction() {

			@Override
			public void actionPerformed(ActionEvent e) {
				ContentPanel.this.core.escapeItem();
			}
		});

		add(scrollPane, BorderLayout.CENTER);
	}

	public void setContent(SearchResultItem selectedItem) {
		lblFileName.setText(selectedItem.file());
		HitContext<String> ctx = selectedItem.lineContext(10);
		contentTextPane.setText(ctx.before() + ctx.pattern() + ctx.after());
		try {
			contentTextPane.getHighlighter().removeAllHighlights();
			contentTextPane.getHighlighter().addHighlight(ctx.highlightStart(), ctx.highlightEnd(),
					new DefaultHighlighter.DefaultHighlightPainter(new Color(255, 128, 128)));
		} catch (BadLocationException e) {
			throw new RuntimeException(e);
		}
	}

}
