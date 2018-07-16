package sk.linhard.exactly.tika;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sk.linhard.exactly.tika.ParseResult.Type;

public class TikaFsCrawler {

	private static final long SIZE_LIMIT = 100_000_000l;
	private static final Logger log = LoggerFactory.getLogger(TikaFsCrawler.class);
	private final Tika tika;
	private final List<File> roots;
	private long totalSize = 0l;

	public TikaFsCrawler(List<File> roots) {
		this.tika = new Tika();
		this.roots = roots;
	}

	public List<ParseResult> crawl() {
		totalSize = 0l;
		if (roots == null || roots.isEmpty()) {
			return Collections.emptyList();
		}
		List<ParseResult> collector = new ArrayList<>();
		for (File file : roots) {
			if (file.isDirectory()) {
				collector.addAll(crawlDirectory(file));
			} else {
				collector.add(parseContent(file));
			}
		}
		return collector;
	}

	private List<ParseResult> crawlDirectory(File dir) {
		if (dir == null) {
			return Collections.emptyList();
		}
		List<ParseResult> collector = new ArrayList<>();
		for (File file : dir.listFiles()) {
			if (file.isDirectory()) {
				collector.addAll(crawlDirectory(file));
			} else {
				collector.add(parseContent(file));
			}
		}
		return collector;
	}

	private ParseResult parseContent(File file) {
		log.info("Parsing {}", file.getAbsolutePath());
		long fileSize = FileUtils.sizeOf(file);
		if (fileSize > SIZE_LIMIT) {
			return new ParseResult(Type.BIG, file, fileSize, 0, null, 0);
		}
		long nanoBefore = System.nanoTime();
		try (InputStream stream = new FileInputStream(file)) {
			String s = tika.parseToString(stream);
			long duration = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - nanoBefore);
			String stripped = StringUtils.stripToNull(StringUtils.normalizeSpace(s));
			if (stripped == null) {
				return new ParseResult(Type.EMPTY, file, fileSize, 0, null, duration);
			} else {
				int len = stripped.length();
				if (len > 200) {
					stripped = stripped.substring(0, 200);
				}
				totalSize += fileSize;
				return new ParseResult(Type.PARSED, file, fileSize, len, stripped.replaceAll(";", "?"), duration);
			}
		} catch (IOException | TikaException e) {
			long duration = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - nanoBefore);
			return new ParseResult(Type.ERROR, file, fileSize, 0, null, duration);
		}
	}

	public long getTotalSize() {
		return totalSize;
	}

}
