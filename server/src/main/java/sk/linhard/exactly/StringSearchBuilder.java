package sk.linhard.exactly;

import static java.util.stream.Collectors.toList;

import java.io.File;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.List;

/**
 * So far we only take into account strings where one character is encoded as
 * one byte, i.e. string length = byte array length
 * 
 */
public class StringSearchBuilder {

	private EncodingContext encoding;
	private SearchBuilder searchBuilder;

	public StringSearchBuilder() {
		this(Charset.forName("UTF-8"));
	}

	public StringSearchBuilder(Charset charset) {
		this.encoding = new EncodingContext(charset);
		this.searchBuilder = new SearchBuilder();
	}

	public void add(String id, String content) {
		searchBuilder.add(id, encoding.toBytes(content));
	}

	public void add(String id, File file, int fileLength) {
		searchBuilder.add(id, file, fileLength);
	}

	public int size() {
		return searchBuilder.size();
	}

	public int totalLength() {
		return searchBuilder.totalLength();
	}

	public Search<String> build() {
		return build(null);
	}

	public Search<String> build(byte[] separator) {
		return new StringSearch(encoding, buildBinary(separator));
	}

	public Search<byte[]> buildBinary() {
		return buildBinary(null);
	}

	public Search<byte[]> buildBinary(byte[] separator) {
		return searchBuilder.build(separator);
	}

	static class EncodingContext {
		private Charset charset;

		public EncodingContext(Charset charset) {
			this.charset = charset;
		}

		public Charset charset() {
			return charset;
		}

		public String toString(byte[] bytes) {
			return new String(bytes, charset);
		}

		public byte[] toBytes(String string) {
			return string.getBytes(charset);
		}

	}

	static class StringDocument implements Document<String> {

		private EncodingContext encoding;
		private Document<byte[]> document;

		public StringDocument(EncodingContext encoding, Document<byte[]> document) {
			this.encoding = encoding;
			this.document = document;
		}

		@Override
		public int index() {
			return document.index();
		}

		@Override
		public String id() {
			return document.id();
		}

		@Override
		public String content() {
			return encoding.toString(document.content());
		}

	}

	static class StringSearch implements Search<String> {

		private EncodingContext encoding;
		private Search<byte[]> search;

		StringSearch(EncodingContext encoding, Search<byte[]> search) {
			this.encoding = encoding;
			this.search = search;
		}

		@Override
		public SearchResult<String> find(String pattern) {
			return new StringSearchResult(encoding, search.find(encoding.toBytes(pattern)));
		}

		@Override
		public int documentCount() {
			return search.documentCount();
		}

		@Override
		public Document<String> document(int i) {
			return new StringDocument(encoding, search.document(i));
		}

	}

	static class StringSearchResult implements SearchResult<String> {

		private EncodingContext encoding;
		private SearchResult<byte[]> searchResult;

		StringSearchResult(EncodingContext encoding, SearchResult<byte[]> searchResult) {
			this.encoding = encoding;
			this.searchResult = searchResult;
		}

		@Override
		public Iterator<Hit<String>> iterator() {
			return new StringHitIterator(encoding, searchResult.iterator());
		}

		@Override
		public int size() {
			return searchResult.size();
		}

		@Override
		public boolean isEmpty() {
			return searchResult.isEmpty();
		}

		@Override
		public Hit<String> hit(int i) {
			return new StringHit(encoding, searchResult.hit(i));
		}

		@Override
		public List<Hit<String>> hits() {
			return searchResult.hits().stream().map(h -> new StringHit(encoding, h)).collect(toList());
		}

		@Override
		public int patternLength() {
			return searchResult.patternLength();
		}

		@Override
		public String pattern() {
			return encoding.toString(searchResult.pattern());
		}

		@Override
		public boolean hasGlobalPosition(int position) {
			return searchResult.hasGlobalPosition(position);
		}

		@Override
		public Hit<String> hitWithGlobalPosition(int position) {
			return new StringHit(encoding, searchResult.hitWithGlobalPosition(position));
		}

		@Override
		public boolean hasPosition(int document, int position) {
			return searchResult.hasPosition(document, position);
		}

		@Override
		public Hit<String> hitWithPosition(int document, int position) {
			return new StringHit(encoding, searchResult.hitWithPosition(document, position));
		}

	}

	static class StringHit implements Hit<String> {

		private EncodingContext encoding;
		private Hit<byte[]> hit;

		StringHit(EncodingContext encoding, Hit<byte[]> hit) {
			super();
			this.encoding = encoding;
			this.hit = hit;
		}

		@Override
		public String toString() {
			return hit.toString();
		}

		@Override
		public int globalPosition() {
			return hit.globalPosition();
		}

		@Override
		public int position() {
			return hit.position();
		}

		@Override
		public Document<String> document() {
			return new StringDocument(encoding, hit.document());
		}

		@Override
		public HitContext<String> charContext(int charsBefore, int charsAfter) {
			return new StringHitContext(encoding, hit.charContext(charsBefore, charsAfter));
		}

		@Override
		public HitContext<String> safeCharContext(int charsBefore, int charsAfter) {
			return new StringHitContext(encoding, hit.safeCharContext(charsBefore, charsAfter));
		}

		@Override
		public HitContext<String> lineContext(int linesBefore, int linesAfter) {
			return new StringHitContext(encoding, hit.lineContext(linesBefore, linesAfter));
		}

	}

	static class StringHitContext implements HitContext<String> {
		private EncodingContext encoding;
		private HitContext<byte[]> hitCtx;

		StringHitContext(EncodingContext encoding, HitContext<byte[]> hitCtx) {
			this.encoding = encoding;
			this.hitCtx = hitCtx;
		}

		@Override
		public String before() {
			return encoding.toString(hitCtx.before());
		}

		@Override
		public String pattern() {
			return encoding.toString(hitCtx.pattern());
		}

		@Override
		public String after() {
			return encoding.toString(hitCtx.after());
		}

		@Override
		public int highlightStart() {
			return hitCtx.highlightStart();
		}

		@Override
		public int highlightEnd() {
			return hitCtx.highlightEnd();
		}

	}

	static class StringHitIterator implements Iterator<Hit<String>> {
		private EncodingContext encoding;
		private Iterator<Hit<byte[]>> hitIterator;

		StringHitIterator(EncodingContext encoding, Iterator<Hit<byte[]>> hitIterator) {
			this.encoding = encoding;
			this.hitIterator = hitIterator;
		}

		@Override
		public boolean hasNext() {
			return hitIterator.hasNext();
		}

		@Override
		public Hit<String> next() {
			return new StringHit(encoding, hitIterator.next());
		}

	}
}
