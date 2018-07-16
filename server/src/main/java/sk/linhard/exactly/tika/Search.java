package sk.linhard.exactly.tika;

import java.io.File;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableList;

public class Search {
	private static final Logger log = LoggerFactory.getLogger(Search.class);

	public static void main(String[] args) throws Exception {

		List<File> roots = ImmutableList.of(new File(args[0]));
		TikaFsCrawler crawler = new TikaFsCrawler(roots);
		List<ParseResult> results = crawler.crawl();

		log.info("Extracted {} bytes of text data from {} files", crawler.getTotalSize(), results.size());

		Collections.sort(results);

		PrintWriter w = new PrintWriter(args[1]);
		w.println("RESULT;PARSE_TIME;TEXT_SIZE;FILE_SIZE;FILE;SAMPLE");
		for (ParseResult res : results) {
			w.print(res.getType().toString());
			w.print(";");
			w.print(Long.toString(res.getParseTime()));
			w.print(";");
			w.print(Integer.toString(res.getTextLenght()));
			w.print(";");
			w.print(Long.toString(res.getFileSize()));
			w.print(";");
			w.print(res.getFile().getAbsolutePath());
			w.print(";");
			w.println(res.getContentSample() == null ? "" : res.getContentSample());
		}
		w.flush();
		w.close();
	}

}
