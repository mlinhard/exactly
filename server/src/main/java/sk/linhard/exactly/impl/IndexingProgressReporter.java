package sk.linhard.exactly.impl;

import java.io.File;
import java.text.DecimalFormat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IndexingProgressReporter {
	private static final Logger log = LoggerFactory.getLogger(FileLoader.class);
	private static final DecimalFormat NFMT = new DecimalFormat("#,###,###");

	private volatile boolean doneCrawling = false;
	private volatile boolean doneLoading = false;
	private volatile boolean doneIndexing = false;
	private volatile long crawlingProgressBytes = 0l;
	private volatile int crawlingProgressFiles = 0;
	private volatile int loadingProgressBytes = 0;
	private volatile int loadingProgressFiles = 0;

	public void discovered(File file, long size) {
		log.debug("Found {} size {}", file.getAbsolutePath(), NFMT.format(size));
		crawlingProgressBytes += size;
	}

	public void added(File file, long size) {
		log.debug("Added {} size {}", file.getAbsolutePath(), NFMT.format(size));
		loadingProgressBytes += size;
		loadingProgressFiles++;
	}

	public void doneCrawling() {
		if (crawlingProgressBytes > Integer.MAX_VALUE) {
			throw new RuntimeException("Total data size cannot exceed 2 GB");
		}
		log.debug("Found {} bytes in {} files", NFMT.format(crawlingProgressBytes), NFMT.format(crawlingProgressFiles));
		doneCrawling = true;
	}

	public void doneLoading() {
		log.debug("Loaded {} bytes from {} files", NFMT.format(loadingProgressBytes),
				NFMT.format(loadingProgressFiles));
		doneLoading = true;
	}

	public void doneIndexing() {
		log.debug("Done indexing. Created suffix array");
		doneIndexing = true;
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

	public void startLoading() {
		log.debug("Loading {} bytes", NFMT.format(this.crawlingProgressBytes));
	}

	public void startIndexing() {
		log.debug("Indexing {} bytes", NFMT.format(loadingProgressBytes));
	}

	public IndexingProgress getProgress() {
		return new IndexingProgressReporter.IndexingProgress(//
				doneCrawling, //
				doneLoading, //
				doneIndexing, //
				crawlingProgressBytes, //
				crawlingProgressFiles, //
				loadingProgressBytes, //
				loadingProgressFiles);
	}

	public static class IndexingProgress {
		private static final DecimalFormat NFMT = new DecimalFormat("#,###,###");

		private final boolean doneCrawling;
		private final boolean doneLoading;
		private final boolean doneIndexing;
		private final long crawlingProgressBytes;
		private final int crawlingProgressFiles;
		private final int loadingProgressBytes;
		private final int loadingProgressFiles;

		public IndexingProgress(boolean doneCrawling, boolean doneLoading, boolean doneIndexing,
				long crawlingProgressBytes, int crawlingProgressFiles, int loadingProgressBytes,
				int loadingProgressFiles) {
			super();
			this.doneCrawling = doneCrawling;
			this.doneLoading = doneLoading;
			this.doneIndexing = doneIndexing;
			this.crawlingProgressBytes = crawlingProgressBytes;
			this.crawlingProgressFiles = crawlingProgressFiles;
			this.loadingProgressBytes = loadingProgressBytes;
			this.loadingProgressFiles = loadingProgressFiles;
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

		public long getCrawlingProgressBytes() {
			return crawlingProgressBytes;
		}

		public int getCrawlingProgressFiles() {
			return crawlingProgressFiles;
		}

		public int getLoadingProgressBytes() {
			return loadingProgressBytes;
		}

		public int getLoadingProgressFiles() {
			return loadingProgressFiles;
		}

		public String getFormattedCrawlingProgressBytes() {
			return NFMT.format(crawlingProgressBytes);
		}

		public String getFormattedCrawlingProgressFiles() {
			return NFMT.format(crawlingProgressFiles);
		}

		public String getFormattedLoadingProgressBytes() {
			return NFMT.format(loadingProgressBytes);
		}

		public String getFormattedLoadingProgressFiles() {
			return NFMT.format(loadingProgressFiles);
		}

	}

}
