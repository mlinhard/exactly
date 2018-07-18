package sk.linhard.exactly.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;
import java.util.function.Consumer;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.Pair;

import sk.linhard.exactly.Document;
import sk.linhard.exactly.Hit;
import sk.linhard.exactly.HitContext;
import sk.linhard.exactly.Search;
import sk.linhard.exactly.SearchResult;

/**
 * Default search implementation that works with single document.
 */
public class DefaultSearch implements Search<byte[]> {

	protected final static int UNDEF = EnhancedSuffixArray.UNDEF;
	protected final byte[] data;
	protected final int[] SA;
	private final int[] lcp;
	private final int[] up;
	private final int[] down;
	private final int[] next;
	protected final Interval rootInterval;
	private final String documentId;

	public static DefaultSearch compute(Document<byte[]> document) {
		EnhancedSuffixArray esa = new EnhancedSuffixArray(document.content());
		esa.computeLCP();
		esa.computeUpDown();
		esa.computeNext();
		return new DefaultSearch(document.id(), esa);
	}

	protected DefaultSearch(String documentId, EnhancedSuffixArray esa) {
		this.data = esa.data;
		this.SA = esa.SA;
		this.lcp = esa.lcp;
		this.up = esa.up;
		this.down = esa.down;
		this.next = esa.next;
		this.rootInterval = new Interval(0, 0, SA.length - 1);
		this.documentId = documentId;
	}

	@Override
	public int documentCount() {
		return 1;
	}

	@Override
	public Document<byte[]> document(int documentIndex) {
		if (documentIndex < 0) {
			throw new ArrayIndexOutOfBoundsException("Negative index");
		}
		if (documentIndex > 0) {
			throw new ArrayIndexOutOfBoundsException(
					"This is a single document search. Index " + documentIndex + " is not valid");
		}
		return new Document<byte[]>() {

			@Override
			public String id() {
				return documentId;
			}

			@Override
			public byte[] content() {
				return data;
			}

			@Override
			public int index() {
				return documentIndex;
			}

		};
	}

	private void acceptInterval(Interval parent, int childStart, int childEnd, Consumer<Interval> func) {
		childEnd = childEnd == UNDEF ? parent.end : childEnd;
		if (childStart + 1 < childEnd) {
			func.accept(interval(childStart, childEnd));
		} else if (childStart != childEnd) {
			func.accept(new Interval(parent.len, childStart, childEnd));
		}
	}

	private Interval createInterval(Interval parent, int childStart, int childEnd) {
		childEnd = childEnd == UNDEF ? parent.end : childEnd;
		if (childStart + 1 < childEnd) {
			return interval(childStart, childEnd);
		} else if (childStart != childEnd) {
			return new Interval(parent.len, childStart, childEnd);
		} else {
			return null;
		}
	}

	private String substr(int i) {
		return substr(i, data.length);
	}

	private String substr(int start, int end) {
		if (start < 0) {
			start = 0;
		}
		if (end > data.length) {
			end = data.length;
		}
		byte[] buf = new byte[end - start];
		System.arraycopy(data, start, buf, 0, buf.length);
		return new String(buf);
	}

	String print() {
		StringBuffer s = new StringBuffer();
		s.append(StringUtils.leftPad("i", 6));
		s.append(StringUtils.leftPad("SA[i]", 6));
		s.append(StringUtils.leftPad("lcp[i]", 7));
		s.append(StringUtils.leftPad("up[i]", 6));
		s.append(StringUtils.leftPad("down[i]", 8));
		s.append(StringUtils.leftPad("next[i]", 8));
		s.append("  suffix[SA[i]]\n");

		int n = SA.length;

		for (int i = 0; i < n; i++) {
			s.append(StringUtils.leftPad(Integer.toString(i), 6));
			s.append(StringUtils.leftPad(Integer.toString(SA[i]), 6));
			s.append(StringUtils.leftPad(Integer.toString(lcp[i]), 7));
			s.append(StringUtils.leftPad(Integer.toString(up[i]), 6));
			s.append(StringUtils.leftPad(Integer.toString(down[i]), 8));
			s.append(StringUtils.leftPad(Integer.toString(next[i]), 8));
			s.append("  ");
			s.append(substr(SA[i]));
			s.append("\n");
		}

		return s.toString();
	}

	private int firstLIndex(Interval parent) {
		if (parent == rootInterval) {
			return 0;
		} else {
			int cup = up[parent.end];
			if (cup < parent.end && parent.start < cup) {
				return cup;
			} else {
				return down[parent.start];
			}
		}
	}

	protected Byte edgeChar(Interval parent, Interval child) {
		int pos = SA[child.start] + parent.len;
		return pos >= data.length ? null : data[pos];
	}

	private Interval getInterval(Interval parent, Byte c) {
		for (Interval child : children(parent)) {
			if (c.equals(edgeChar(parent, child))) {
				return child;
			}
		}
		return null;
	}

	protected Iterable<Interval> children(Interval parent) {
		return new Iterable<DefaultSearch.Interval>() {

			@Override
			public Iterator<Interval> iterator() {
				return new IntervalIterator(parent);
			}
		};
	}

	protected void forEachChild(Interval parent, Consumer<Interval> func) {
		int i = parent.start;
		int nexti = firstLIndex(parent);
		if (nexti == i) {
			nexti = next[i];
		}
		acceptInterval(parent, i, nexti, func);
		while (nexti != UNDEF) {
			i = nexti;
			nexti = next[i];
			acceptInterval(parent, i, nexti, func);
		}
	}

	protected boolean match(byte[] pattern, int dataOff, int patternOff, int len) {
		for (int i = 0; i < len; i++) {
			int pIdx = patternOff + i;
			int dIdx = dataOff + i;
			if (pIdx >= pattern.length || dIdx >= data.length || pattern[pIdx] != data[dIdx]) {
				return false;
			}
		}
		return true;
	}

	@Override
	public SearchResult<byte[]> find(byte[] pattern) {
		if (pattern == null || pattern.length == 0) {
			throw new RuntimeException("You must specify a non-empty pattern");
		}
		int c = 0;
		boolean queryFound = true;
		Interval intv = getInterval(rootInterval, pattern[c]);
		int intvLen = 0;
		while (intv != null && c < pattern.length && queryFound) {
			intvLen = intv.end - intv.start;
			if (intvLen > 1) {
				int min = Math.min(intv.len, pattern.length);
				queryFound = match(pattern, SA[intv.start] + c, c, min - c);
				c = min;
				if (c < pattern.length) {
					intv = getInterval(intv, pattern[c]);
				}
			} else {
				queryFound = match(pattern, SA[intv.start] + c, c, pattern.length - c);
				break;
			}
		}
		if (intv != null && queryFound) {
			return createSearchResult(intv.start, intvLen, pattern.length);
		} else {
			return new EmptyResultImpl(pattern);
		}
	}

	protected SearchResult<byte[]> createSearchResult(int saIntervalStart, int saIntervalLength, int patternLength) {
		return new DefaultSearchResult(saIntervalStart, saIntervalLength, patternLength);
	}

	private Interval interval(int i, int j) {
		int cup = up[j];
		if (cup < j && i < cup) {
			return new Interval(lcp[cup], i, j);
		} else {
			return new Interval(lcp[down[i]], i, j);
		}
	}

	public int getLCPOverflow1() {
		int cnt = 0;
		for (int i = 0; i < lcp.length; i++) {
			if (lcp[i] > 254) {
				cnt++;
			}
		}
		return cnt;
	}

	public int getLCPOverflow2() {
		int max = 256 * 256 - 2;
		int cnt = 0;
		for (int i = 0; i < lcp.length; i++) {
			if (lcp[i] > max) {
				cnt++;
			}
		}
		return cnt;
	}

	byte[] dataCopy(int globalPosition, int length) {
		byte[] buf = new byte[length];
		System.arraycopy(data, globalPosition, buf, 0, buf.length);
		return buf;
	}

	/**
	 * This is basically "shortest non-substring" problem
	 */
	byte[] findSeparator() {
		Stack<Pair<Integer, Interval>> intervalStack = new Stack<>();
		boolean[] occurenceBuf = new boolean[256];
		intervalStack.push(Pair.of(0, rootInterval));

		while (!intervalStack.isEmpty()) {
			Pair<Integer, Interval> t = intervalStack.pop();
			Integer sepLen = t.getLeft();
			Interval interval = t.getRight();
			Byte nonExistentChar = findNonExistentChar(interval, sepLen, occurenceBuf);
			if (nonExistentChar != null) {
				return buildSeparator(interval.start, sepLen, nonExistentChar);
			} else {
				forEachChild(interval, child -> {
					intervalStack.push(Pair.of(sepLen + 1, child));
				});
			}
		}

		throw new IllegalStateException("Separator must be found");
	}

	private byte[] buildSeparator(int saIdx, int sepLen, byte tail) {
		byte[] separator = new byte[sepLen + 1];
		System.arraycopy(data, SA[saIdx], separator, 0, sepLen);
		separator[sepLen] = tail;
		return separator;
	}

	private Byte findNonExistentChar(Interval parent, int sepLen, boolean[] occurence) {
		Arrays.fill(occurence, false);
		forEachChild(parent, child -> {
			Byte edgeStart = SA[child.start] + sepLen >= data.length ? null : data[SA[child.start] + sepLen];
			if (edgeStart != null) {
				int occurenceIdx = edgeStart;
				occurenceIdx -= Byte.MIN_VALUE;
				occurence[occurenceIdx] = true;
			}
		});
		for (int i = 0; i < occurence.length; i++) {
			if (!occurence[i]) {
				return (byte) (i + Byte.MIN_VALUE);
			}
		}
		return null;
	}

	String printSA(int start, int end) {
		StringBuilder s = new StringBuilder();
		s.append(StringUtils.leftPad("i", 12));
		s.append(StringUtils.leftPad("SA[i]", 12));
		s.append(StringUtils.leftPad("lcp[i]", 12));
		s.append(StringUtils.leftPad("up[i]", 12));
		s.append(StringUtils.leftPad("down[i]", 12));
		s.append(StringUtils.leftPad("next[i]", 12));
		s.append(" suffix start");
		s.append("\n");
		for (int i = start; i < end; i++) {
			s.append(StringUtils.leftPad(Integer.toString(i), 12));
			s.append(StringUtils.leftPad(Integer.toString(SA[i]), 12));
			s.append(StringUtils.leftPad(Integer.toString(lcp[i]), 12));
			s.append(StringUtils.leftPad(Integer.toString(up[i]), 12));
			s.append(StringUtils.leftPad(Integer.toString(down[i]), 12));
			s.append(StringUtils.leftPad(Integer.toString(next[i]), 12));
			s.append(" ");
			s.append(printSuffix(SA[i], 10));
			s.append("\n");
		}
		return s.toString();
	}

	String printSuffix(int pos, int len) {
		byte[] cp = Arrays.copyOfRange(data, pos, pos + len);
		Byte[] cpo = ArrayUtils.toObject(cp);
		return Arrays.asList(cpo).toString();
	}

	String printArray(int[] a, int start, int end) {
		StringBuilder s = new StringBuilder();
		s.append(StringUtils.leftPad("i", 12));
		s.append(StringUtils.leftPad("a[i]", 12));
		s.append("\n");
		for (int i = 0; i < end - start; i++) {
			s.append(StringUtils.leftPad(Integer.toString(i), 12));
			s.append(StringUtils.leftPad(Integer.toString(a[i]), 12));
			s.append("\n");
		}
		return s.toString();
	}

	String printArray(byte[] a, int start, int end) {
		StringBuilder s = new StringBuilder();
		s.append(StringUtils.leftPad("i", 12));
		s.append(StringUtils.leftPad("a[i]", 12));
		s.append("\n");
		for (int i = 0; i < end - start; i++) {
			s.append(StringUtils.leftPad(Integer.toString(i), 12));
			s.append(StringUtils.leftPad(Integer.toString(a[i]), 12));
			s.append("\n");
		}
		return s.toString();
	}

	int isNewLine(int i) {
		return isNewLine(i, this.data);
	}

	/**
	 * recognizes following newline sequences: [13], [10], [13, 10]
	 * 
	 * @return 0 if newline not present, 1 newline of length 1 present, 2
	 *         newline of length 2 present at given index
	 */
	static int isNewLine(int i, byte[] data) {
		if (i >= 0 && i < data.length) {
			byte c0 = data[i];
			if (c0 == 13) {
				return i == data.length - 1 || data[i + 1] != 10 ? 1 : 2;
			} else if (c0 == 10) {
				return i == 0 || data[i - 1] != 13 ? 1 : 0;
			} else {
				return 0;
			}
		} else {
			return 0;
		}
	}

	String print(int saIntervalStart, int saIntervalLength, int patternLength, int context) {
		StringBuffer s = new StringBuffer();
		s.append(StringUtils.leftPad("i", 6));
		s.append(StringUtils.leftPad("pos", 6));
		s.append(StringUtils.leftPad("doc", 6));
		s.append(" match\n");

		for (int i = 0; i < saIntervalLength; i++) {
			s.append(StringUtils.leftPad(Integer.toString(i), 6));
			int posi = SA[saIntervalStart + i];
			s.append(StringUtils.leftPad(Integer.toString(posi), 6));
			s.append(StringUtils.leftPad(document(i).id(), 6));
			s.append(" ");
			s.append(substr(posi - context, posi));
			s.append(">");
			s.append(substr(posi, posi + patternLength));
			s.append("<");
			s.append(substr(posi + patternLength, posi + patternLength + context));
			s.append("\n");
		}

		return s.toString();
	}

	int dataLength() {
		return data.length;
	}

	protected static class Interval {
		public int len;
		public int start;
		public int end;

		public Interval(int len, int start, int end) {
			this.len = len;
			this.start = start;
			this.end = end;
		}

		@Override
		public String toString() {
			return Integer.toString(len) + "-[" + start + ", " + end + "]";
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + end;
			result = prime * result + len;
			result = prime * result + start;
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Interval other = (Interval) obj;
			if (end != other.end)
				return false;
			if (len != other.len)
				return false;
			if (start != other.start)
				return false;
			return true;
		}

	}

	private class IntervalIterator implements Iterator<Interval> {
		private Interval parent;
		private int start;
		private int end;
		private Interval nextInterval;

		public IntervalIterator(Interval parent) {
			this.parent = parent;
			this.start = parent.start;
			this.end = firstLIndex(parent);
			if (end == start) {
				end = next[start];
			}
			this.nextInterval = createInterval(parent, start, end);
		}

		@Override
		public boolean hasNext() {
			return nextInterval != null;
		}

		@Override
		public Interval next() {
			Interval r = nextInterval;
			if (end != UNDEF) {
				start = end;
				end = next[start];
				this.nextInterval = createInterval(parent, start, end);
			} else {
				this.nextInterval = null;
			}
			return r;
		}

	}

	class EmptyResultImpl implements SearchResult<byte[]> {
		private byte[] pattern;

		public EmptyResultImpl(byte[] pattern) {
			this.pattern = pattern;
		}

		@Override
		public boolean isEmpty() {
			return true;
		}

		@Override
		public int size() {
			return 0;
		}

		@Override
		public DefaultHit hit(int i) {
			throw new IndexOutOfBoundsException("Result is empty");
		}

		@Override
		public int patternLength() {
			return pattern.length;
		}

		@Override
		public byte[] pattern() {
			return pattern;
		}

		@Override
		public boolean hasGlobalPosition(int position) {
			return false;
		}

		@Override
		public DefaultHit hitWithGlobalPosition(int position) {
			return null;
		}

		@Override
		public boolean hasPosition(int document, int position) {
			return false;
		}

		@Override
		public DefaultHit hitWithPosition(int document, int position) {
			return null;
		}

		@Override
		public List<Hit<byte[]>> hits() {
			return Collections.emptyList();
		}

		@Override
		public Iterator<Hit<byte[]>> iterator() {
			return Collections.emptyIterator();
		}

		@Override
		public Iterable<Hit<byte[]>> skipIterator(int offset) {
			return new Iterable<Hit<byte[]>>() {

				@Override
				public Iterator<Hit<byte[]>> iterator() {
					return Collections.emptyIterator();
				}
			};
		}
	}

	class DefaultSearchResult implements SearchResult<byte[]> {

		private final int saIntervalStart;
		private final int saIntervalLength;
		private final int patternLength;

		public DefaultSearchResult(int saIntervalStart, int saIntervalLength, int patternLength) {
			this.saIntervalStart = saIntervalStart;
			this.saIntervalLength = saIntervalLength;
			this.patternLength = patternLength;
		}

		@Override
		public boolean isEmpty() {
			return false;
		}

		@Override
		public int size() {
			return saIntervalLength;
		}

		protected int globalPosition(int hitIdx) {
			if (hitIdx < 0 || hitIdx >= saIntervalLength) {
				throw new IndexOutOfBoundsException(
						"The hit index " + hitIdx + " exceeds the search result size " + saIntervalLength);
			}
			return SA[saIntervalStart + hitIdx];
		}

		protected int position(int hitIdx) {
			return globalPosition(hitIdx);
		}

		protected Document<byte[]> document(int hitIdx) {
			return DefaultSearch.this.document(documentIndex(hitIdx));
		}

		protected int documentIndex(int hitIdx) {
			return 0;
		}

		@Override
		public boolean hasGlobalPosition(int position) {
			return hitWithGlobalPosition(position) != null;
		}

		@Override
		public DefaultHit hitWithGlobalPosition(int position) {
			int saIntervalEnd = saIntervalStart + saIntervalLength;
			for (int i = saIntervalStart; i < saIntervalEnd; i++) {
				if (SA[i] == position) {
					return new DefaultHit(this, i - saIntervalStart);
				}
			}
			return null;
		}

		@Override
		public boolean hasPosition(int document, int position) {
			return hitWithPosition(document, position) != null;
		}

		@Override
		public DefaultHit hitWithPosition(int document, int position) {
			return hitWithGlobalPosition(position);
		}

		@Override
		public int patternLength() {
			return patternLength;
		}

		@Override
		public byte[] pattern() {
			return dataCopy(globalPosition(0), patternLength);
		}

		protected int checkBefore(int pos, int maxSize) {
			return Math.max(pos - maxSize, 0);
		}

		protected int checkAfter(int pos, int maxSize) {
			return Math.min(pos + maxSize, dataLength());
		}

		protected int linesBeforeStart(int i, int maxLines) {
			int j = globalPosition(i);
			int newLine = 0;
			int lineCount = 0;
			while (j >= 0 && lineCount <= maxLines) {
				newLine = isNewLine(j);
				if (newLine > 0) {
					lineCount++;
				}
				j--;
			}
			return j + 1 + newLine;
		}

		protected int linesAfterStart(int i, int maxLines) {
			int j = globalPosition(i) + patternLength;
			int lineCount = 0;
			int dataLength = dataLength();
			while (j < dataLength && lineCount <= maxLines) {
				if (isNewLine(j) > 0) {
					lineCount++;
				}
				j++;
			}
			return j == dataLength ? j : j - 1;
		}

		HitContext<byte[]> charContext(int idx, int charsBefore, int charsAfter) {
			int pos = globalPosition(idx);
			int beforeStart = checkBefore(pos, charsBefore);
			int afterEnd = checkAfter(pos + patternLength, charsAfter);
			return new DefaultHitContext(DefaultSearch.this, beforeStart, pos - beforeStart, patternLength,
					afterEnd - pos - patternLength);
		}

		HitContext<byte[]> safeCharContext(int idx, int charsBefore, int charsAfter) {
			int pos = globalPosition(idx);
			int beforeStart = checkBefore(pos, charsBefore);
			int afterEnd = checkAfter(pos, charsAfter);
			return new SafeHitContext(DefaultSearch.this, beforeStart, pos - beforeStart, patternLength,
					afterEnd - pos - patternLength);
		}

		HitContext<byte[]> lineContext(int idx, int linesBefore, int linesAfter) {
			int patternStart = globalPosition(idx);
			int beforeStart = linesBeforeStart(idx, linesBefore);
			int afterEnd = linesAfterStart(idx, linesAfter);
			return new DefaultHitContext(DefaultSearch.this, beforeStart, patternStart - beforeStart, patternLength,
					afterEnd - patternStart - patternLength);
		}

		@Override
		public DefaultHit hit(int idx) {
			return new DefaultHit(this, idx);
		}

		@Override
		public List<Hit<byte[]>> hits() {
			int n = size();
			List<Hit<byte[]>> r = new ArrayList<>(n);
			for (int i = 0; i < n; i++) {
				r.add(new DefaultHit(this, i));
			}
			return r;
		}

		@Override
		public Iterator<Hit<byte[]>> iterator() {
			return new HitIterator(0);
		}

		@Override
		public Iterable<Hit<byte[]>> skipIterator(int offset) {
			return new Iterable<Hit<byte[]>>() {

				@Override
				public Iterator<Hit<byte[]>> iterator() {
					return new HitIterator(offset);
				}
			};
		}

		class HitIterator implements Iterator<Hit<byte[]>> {

			private int idx;

			public HitIterator(int offset) {
				this.idx = offset - 1;
			}

			@Override
			public boolean hasNext() {
				return idx + 1 < DefaultSearchResult.this.size();
			}

			@Override
			public Hit<byte[]> next() {
				return hit(++idx);
			}

		}

	}
}
