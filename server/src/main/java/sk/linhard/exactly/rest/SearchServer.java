package sk.linhard.exactly.rest;

import java.io.File;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.annotation.PostConstruct;
import javax.ws.rs.NotFoundException;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import com.google.common.collect.ImmutableList;

import sk.linhard.exactly.Document;
import sk.linhard.exactly.Hit;
import sk.linhard.exactly.HitContext;
import sk.linhard.exactly.Search;
import sk.linhard.exactly.SearchResult;
import sk.linhard.exactly.impl.FileLoader;
import sk.linhard.exactly.impl.IndexingProgressReporter;
import sk.linhard.exactly.impl.IndexingProgressReporter.IndexingProgress;

@Scope(value = "singleton")
@Component
public class SearchServer {

	private static final Logger log = LoggerFactory.getLogger(SearchServer.class);

	@Autowired
	private SearchServerConfig config;

	private ExecutorService executor = Executors.newCachedThreadPool();

	private IndexingProgressReporter reporter = new IndexingProgressReporter();

	private Pair<Search<byte[]>, Map<String, Integer>> search;

	private String cachedVersion;

	public IndexingProgress getProgress() {
		return reporter.getProgress();
	}

	@PostConstruct
	public void start() {
		log.info("Root folder: {}", config.getIndexedFolderRoot().getAbsolutePath());
		executor.submit(() -> {
			indexFolder(config.getIndexedFolderRoot(), reporter);
		});
	}

	private void indexFolder(File folder, IndexingProgressReporter reporter) {
		log.info("Started indexing folder {}", folder.getAbsolutePath());
		synchronized (SearchServer.this) {
			search = null;
		}
		FileLoader loader = new FileLoader(ImmutableList.of(folder), reporter);
		loader.crawl();
		loader.load();
		Search<byte[]> aSearch = loader.indexBinary();
		Map<String, Integer> anIdToIdx = computeIdToIdx(aSearch);
		synchronized (SearchServer.this) {
			search = Pair.of(aSearch, anIdToIdx);
		}
		IndexingProgress progress = reporter.getProgress();
		log.info("Done indexing {} files, {} bytes total", progress.getFormattedLoadingProgressFiles(),
				progress.getFormattedLoadingProgressBytes());
	}

	// TODO: this is lame, search by document ID should be a suffix array search
	// as well
	// suffix array could be sparse (we only care about the whole id string, not
	// search inside)
	private Map<String, Integer> computeIdToIdx(Search<byte[]> aSearch) {
		Map<String, Integer> anIdToIdx = new HashMap<>();
		int n = aSearch.documentCount();
		for (int i = 0; i < n; i++) {
			Document<byte[]> doc = aSearch.document(i);
			if (doc.index() != i) {
				throw new IllegalStateException("Wrong document index " + i + " vs " + doc.index());
			}
			anIdToIdx.put(doc.id(), i);
		}
		return anIdToIdx;
	}

	private synchronized Pair<Search<byte[]>, Map<String, Integer>> checkSearch() {
		if (search == null) {
			throw new RuntimeException("Search not ready yet");
		}
		return search;
	}

	public SearchResponse search(SearchRequest request) {
		Pair<Search<byte[]>, Map<String, Integer>> search = checkSearch();
		SearchResult<byte[]> searchResult = search.getKey().find(request.getPattern());
		List<SearchResponse.Hit> hits = new ArrayList<>(searchResult.size());

		int hitCount = 0;
		for (Hit<byte[]> hit : searchResult) {
			HitContext<byte[]> ctx = hit.charContext(request.getMaxContext(), request.getMaxContext());
			hits.add(new SearchResponse.Hit(hit.position(), hit.document().id(), ctx.before(), ctx.after()));
			hitCount++;
			if (hitCount >= request.getMaxCandidates()) {
				break;
			}
		}

		return new SearchResponse(hits);
	}

	public SearchServerStats stats() {
		return new SearchServerStats(getProgress());
	}

	public DocumentResponse requestDocument(DocumentRequest request) {
		Integer documentIndex = request.getDocumentIndex();
		if (documentIndex == null) {
			documentIndex = search.getValue().get(request.getDocumentId());
		}
		if (documentIndex == null) {
			throw new NotFoundException();
		}
		Document<byte[]> document = search.getKey().document(documentIndex);
		return new DocumentResponse(document.id(), document.index(), document.content());
	}

	public String version() {
		if (cachedVersion == null) {
			try {
				InputStream in = new ClassPathResource("VERSION").getInputStream();
				if (in == null) {
					log.error("Can't find VERSION file");
					return "UNKNOWN";
				} else {
					cachedVersion = IOUtils.toString(in, Charset.forName("UTF-8"));
				}
			} catch (Exception e) {
				log.error("Error while discovering version", e);
				return "UNKNOWN";
			}
		}
		return cachedVersion;
	}
}
