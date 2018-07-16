package sk.linhard.exactly.impl;

import static java.util.stream.Collectors.toList;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.FileUtils;

import sk.linhard.exactly.Search;
import sk.linhard.exactly.StringSearchBuilder;

public class FileLoader {

	private List<File> roots;
	private List<Item> items;
	private FileFilter fileFilter;
	private StringSearchBuilder searchBuilder;
	private IndexingProgressReporter reporter;

	public FileLoader(List<File> roots, IndexingProgressReporter reporter) {
		this.roots = roots;
		this.fileFilter = f -> true;
		this.reporter = reporter;
	}

	public List<File> getRoots() {
		return roots;
	}

	private List<Item> crawlInternal() {
		if (roots == null || roots.isEmpty()) {
			return Collections.emptyList();
		}
		List<Item> collector = new ArrayList<>();
		for (File file : roots) {
			if (file.isDirectory()) {
				collector.addAll(crawlDirectory(file));
			} else {
				collector.add(itemFor(file));
			}
		}
		return collector;
	}

	private Item itemFor(File file) {
		Item item = new Item(file, FileUtils.sizeOf(file));
		reporter.discovered(file, item.size);
		return item;
	}

	private List<Item> crawlDirectory(File dir) {
		if (dir == null) {
			return Collections.emptyList();
		}
		List<Item> collector = new ArrayList<>();
		for (File file : dir.listFiles()) {
			if (file.isDirectory()) {
				collector.addAll(crawlDirectory(file));
			} else if (fileFilter.accept(file)) {
				collector.add(itemFor(file));
			}
		}
		return collector;
	}

	public void crawl() {
		items = crawlInternal();
		reporter.doneCrawling();
	}

	public int getFileCount() {
		return items.size();
	}

	public int getTotalSize() {
		return searchBuilder.totalLength();
	}

	public void load() {
		if (items == null || !reporter.isDoneCrawling()) {
			throw new IllegalStateException("Must crawl the filesystem first");
		}
		reporter.startLoading();
		searchBuilder = new StringSearchBuilder();
		for (int i = 0; i < items.size(); i++) {
			Item r = items.get(i);
			searchBuilder.add(r.file.getAbsolutePath(), r.file, (int) r.size);
			reporter.added(r.file, r.size);
		}
		reporter.doneLoading();
	}

	public Search<String> index() {
		if (searchBuilder == null || !reporter.isDoneLoading()) {
			throw new IllegalStateException("Must load the data first");
		}

		reporter.startIndexing();
		Search<String> search = searchBuilder.build();
		reporter.doneIndexing();

		return search;
	}

	public Search<byte[]> indexBinary() {
		if (searchBuilder == null || !reporter.isDoneLoading()) {
			throw new IllegalStateException("Must load the data first");
		}

		reporter.startIndexing();
		Search<byte[]> search = searchBuilder.buildBinary();
		reporter.doneIndexing();

		return search;
	}

	public List<File> fileList() {
		return items.stream().map(i -> i.file).collect(toList());
	}

	private static class Item {
		public File file;
		public long size;

		public Item(File file, long fileSize) {
			this.file = file;
			this.size = fileSize;
		}

	}

}
