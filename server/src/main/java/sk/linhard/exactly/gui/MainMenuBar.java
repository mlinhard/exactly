package sk.linhard.exactly.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

public class MainMenuBar extends JMenuBar {

	public MainMenuBar(Core core) {
		JMenu mnFile = new JMenu("File");
		add(mnFile);

		JMenuItem mnIndex = new JMenuItem("Index folder ...");
		mnFile.add(mnIndex);

		mnIndex.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				core.onMenuActionIndexFolder();
			}
		});

	}

}
