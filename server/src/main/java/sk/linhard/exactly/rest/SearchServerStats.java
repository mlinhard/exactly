package sk.linhard.exactly.rest;

import com.fasterxml.jackson.annotation.JsonProperty;

import sk.linhard.exactly.impl.IndexingProgressReporter.IndexingProgress;

public class SearchServerStats {

	@JsonProperty("indexed_bytes")
	private final int indexedBytes;

	@JsonProperty("indexed_files")
	private final int indexedFiles;

	@JsonProperty("done_crawling")
	private final boolean doneCrawling;

	@JsonProperty("done_loading")
	private final boolean doneLoading;

	@JsonProperty("done_indexing")
	private final boolean doneIndexing;

	public SearchServerStats(IndexingProgress progress) {
		indexedBytes = progress.getLoadingProgressBytes();
		indexedFiles = progress.getLoadingProgressFiles();
		doneCrawling = progress.isDoneCrawling();
		doneLoading = progress.isDoneLoading();
		doneIndexing = progress.isDoneIndexing();
	}

	public SearchServerStats(int indexedBytes, int indexedFiles, boolean doneCrawling, boolean doneLoading,
			boolean doneIndexing) {
		super();
		this.indexedBytes = indexedBytes;
		this.indexedFiles = indexedFiles;
		this.doneCrawling = doneCrawling;
		this.doneLoading = doneLoading;
		this.doneIndexing = doneIndexing;
	}

	public int getIndexedBytes() {
		return indexedBytes;
	}

	public int getIndexedFiles() {
		return indexedFiles;
	}

	public boolean isDoneCrawling() {
		return doneCrawling;
	}

	public boolean isDoneLoading() {
		return doneLoading;
	}

	public boolean isDoneIndexing() {
		return doneIndexing;
	}

}
