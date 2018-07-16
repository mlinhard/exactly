package sk.linhard.exactly.gui;

import java.awt.EventQueue;
import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.JFileChooser;
import javax.swing.UIManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableList;

import sk.linhard.exactly.Search;
import sk.linhard.exactly.SearchResult;
import sk.linhard.exactly.impl.FileLoader;
import sk.linhard.exactly.impl.IndexingProgressReporter;
import sk.linhard.exactly.impl.IndexingProgressReporter.IndexingProgress;

public class Core {

	private static final Logger log = LoggerFactory.getLogger(Core.class);
	private MainWindow mainFrame;
	private Search<String> search;
	private ExecutorService executor;

	private Core() {
		executor = Executors.newCachedThreadPool();
	}

	public static void main(String[] args) {
		new Core().start();
	}

	public SearchResult<String> find(String query) {
		return search.find(query);
	}

	public void start() {
		log.debug("Starting");
		EventQueue.invokeLater(() -> {
			try {
				UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			} catch (Exception e) {
				log.error("Error while changing look and feel", e);
			}
			mainFrame = new MainWindow(this);
			mainFrame.setVisible(true);
		});
	}

	private void indexFolder(File folder) {
		IndexingProgressReporter reporter = new IndexingProgressReporter();
		FileLoader fileLoader = new FileLoader(ImmutableList.of(folder), reporter);
		executor.submit(() -> {
			checkFileLoaderProgress(reporter, fileLoader);
		});
		fileLoader.crawl();
		fileLoader.load();
		search = fileLoader.index();
	}

	private void checkFileLoaderProgress(IndexingProgressReporter reporter, FileLoader fileLoader) {
		EventQueue.invokeLater(() -> {
			mainFrame.reportRoot(fileLoader.getRoots().iterator().next());
		});
		boolean done = false;
		while (!done) {
			IndexingProgress progress = reporter.getProgress();
			done = progress.isDoneIndexing();
			EventQueue.invokeLater(() -> {
				mainFrame.reportProgress(progress);
			});
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				log.debug("Interrupted for some reason");
			}
		}
	}

	private void onFolderSelected(File folder) {
		if (folder != null && folder.exists() && folder.isDirectory()) {
			log.debug("Loading: " + folder.getAbsolutePath());
			executor.submit(() -> {
				indexFolder(folder);
			});
		} else {
			log.debug("Not a folder: {}", folder == null ? "null" : folder.getAbsolutePath());
		}
	}

	public void onMenuActionIndexFolder() {
		JFileChooser fc = new JFileChooser();
		fc.setDialogTitle("Select root directory to index");
		fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		log.debug("Menu action performed");
		int returnVal = fc.showOpenDialog(mainFrame);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			onFolderSelected(fc.getSelectedFile());
		} else {
			log.debug("Open command cancelled by user.");
		}
	}

	public void selectItem(SearchResultItem selectedItem) {
		mainFrame.selectItem(selectedItem);
	}

	public void escapeItem() {
		mainFrame.exitHit();
	}

}
