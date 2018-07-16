package sk.linhard.exactly.impl;

import java.util.Arrays;

import sk.linhard.exactly.Document;
import sk.linhard.exactly.SearchResult;

/**
 * Search in data built from multiple documents.
 * 
 * Optimisation proposals for separator: - it can be at most 5 bytes (it's not
 * possible that the 2GB of data contains all 4byte values) - can be stored in
 * one long - can be compared as long with bit operations
 */
public class MultiDocumentSearch extends DefaultSearch {

	private final int[] offsets;
	private final String[] ids;
	private final byte[] separator;
	private final int newLineInSeparator;

	public static MultiDocumentSearch compute(byte[] data, int[] offsets, String[] ids) {
		EnhancedSuffixArray esa = new EnhancedSuffixArray(data);
		esa.computeLCP(true);
		esa.computeUpDown();
		esa.computeNext();
		byte[] separator = new DefaultSearch(null, esa).findSeparator();
		esa.introduceSeparators(offsets, separator);
		esa = new EnhancedSuffixArray(esa.data);
		esa.computeLCP();
		esa.computeUpDown();
		esa.computeNext();
		return new MultiDocumentSearch(esa, offsets, ids, separator);
	}

	public static MultiDocumentSearch compute(byte[] data, int[] offsets, String[] ids, byte[] separator) {
		EnhancedSuffixArray esa = new EnhancedSuffixArray(data);
		esa.computeLCP(true);
		esa.computeUpDown();
		esa.computeNext();
		SearchResult<byte[]> sr = new DefaultSearch(null, esa).find(separator);
		if (!sr.isEmpty()) {
			throw new IllegalArgumentException("Given separator found at position " + sr.hit(0).globalPosition());
		}
		esa.introduceSeparators(offsets, separator);
		esa = new EnhancedSuffixArray(esa.data);
		esa.computeLCP();
		esa.computeUpDown();
		esa.computeNext();
		return new MultiDocumentSearch(esa, offsets, ids, separator);
	}

	public MultiDocumentSearch(EnhancedSuffixArray esa, int[] offsets, String[] ids, byte[] separator) {
		super(null, esa);
		this.offsets = offsets;
		this.ids = ids;
		this.separator = separator;
		this.newLineInSeparator = newLineInSeparator(separator);
	}

	private int newLineInSeparator(byte[] separator) {
		for (int i = 0; i < separator.length; i++) {
			if (isNewLine(i, separator) > 0) {
				return i;
			}
		}
		return -1;
	}

	@Override
	public int documentCount() {
		return offsets.length;
	}

	@Override
	public Document<byte[]> document(int documentIdx) {
		if (documentIdx < 0) {
			throw new ArrayIndexOutOfBoundsException("Negative index");
		}
		if (documentIdx >= offsets.length) {
			throw new ArrayIndexOutOfBoundsException(
					"This search contains " + offsets.length + " documents. Index " + documentIdx + " is not valid");
		}

		return new Document<byte[]>() {

			@Override
			public int index() {
				return documentIdx;
			}

			@Override
			public String id() {
				return ids[documentIdx];
			}

			@Override
			public byte[] content() {
				return documentContent(documentIdx);
			}
		};
	}

	private byte[] documentContent(int documentIdx) {
		if (documentIdx < 0 || documentIdx >= offsets.length) {
			throw new ArrayIndexOutOfBoundsException();
		}
		int start = offsets[documentIdx];
		int end = documentIdx == offsets.length - 1 ? data.length : offsets[documentIdx + 1] - separator.length;
		return dataCopy(start, end - start);
	}

	@Override
	protected Byte edgeChar(Interval parent, Interval child) {
		int pos = SA[child.start] + parent.len;
		return pos < data.length && !separatorAt(pos) ? data[pos] : null;
	}

	@Override
	protected boolean match(byte[] pattern, int dataOff, int patternOff, int len) {
		for (int i = 0; i < len; i++) {
			int pIdx = patternOff + i;
			int dIdx = dataOff + i;
			if (pIdx >= pattern.length || dIdx >= data.length || pattern[pIdx] != data[dIdx] || separatorAt(dIdx)) {
				return false;
			}
		}
		return true;
	}

	private boolean separatorAt(int pos) {
		if (pos + separator.length <= data.length && pos >= 0) {
			for (int i = 0; i < separator.length; i++) {
				if (separator[i] != data[pos + i]) {
					return false;
				}
			}
			return true;
		} else {
			return false;
		}
	}

	@Override
	protected SearchResult<byte[]> createSearchResult(int saIntervalStart, int saIntervalLength, int patternLength) {
		return new MultiDocResultImpl(saIntervalStart, saIntervalLength, patternLength);
	}

	class MultiDocResultImpl extends DefaultSearchResult {

		private int[] documentCache;

		public MultiDocResultImpl(int saIntervalStart, int saIntervalLength, int patternLength) {
			super(saIntervalStart, saIntervalLength, patternLength);
			documentCache = new int[saIntervalLength];
			Arrays.fill(documentCache, UNDEF);
		}

		@Override
		protected int documentIndex(int hitIdx) {
			if (documentCache[hitIdx] == UNDEF) {
				int pos = globalPosition(hitIdx);
				int r = Arrays.binarySearch(offsets, pos);
				documentCache[hitIdx] = r >= 0 ? r : -r - 2;
			}
			return documentCache[hitIdx];
		}

		@Override
		protected int position(int hitIdx) {
			return globalPosition(hitIdx) - offsets[documentIndex(hitIdx)];
		}

		protected int[] positions() {
			int[] positions = new int[size()];
			for (int i = 0; i < positions.length; i++) {
				positions[i] = position(i);
			}
			return positions;
		}

		@Override
		protected int checkBefore(int pos, int maxSize) {
			int leftLimit = super.checkBefore(pos, maxSize);
			for (int i = pos - separator.length; i >= leftLimit; i--) {
				if (separatorAt(i)) {
					return i + separator.length;
				}
			}
			return leftLimit;
		}

		@Override
		protected int checkAfter(int pos, int maxSize) {
			int rightLimit = super.checkAfter(pos, maxSize);
			int sepRightLimit = rightLimit - separator.length;
			for (int i = pos; i <= sepRightLimit; i++) {
				if (separatorAt(i)) {
					return i;
				}
			}
			return rightLimit;
		}

		@Override
		public DefaultHit hitWithPosition(int document, int position) {
			return hitWithGlobalPosition(offsets[document] + position);
		}

		@Override
		protected int linesBeforeStart(int i, int maxLines) {
			int j = globalPosition(i);
			int newLine = 0;
			int lineCount = 0;
			boolean sep = separatorAt(j);
			while (j >= 0 && !sep && lineCount <= maxLines) {
				newLine = isNewLine(j, data);
				if (newLine > 0) {
					lineCount++;
				}
				sep = separatorAt(--j);
			}
			/*
			 * if separator is contained in (newLineInSeparator == -1) or equal
			 * to (newLineInSeparator == 0) newline sequence this means that the
			 * newline sequence never appears in the data. That means that
			 * isNewLine always returns 0, lineCount never increases and
			 * therefore the loop is ended only by the separator. in both cases
			 * we want to return j + 1 + separator.length
			 * 
			 * if newLine is contained (but not equal) in the separator
			 * (newLineInSeparator > 0) we want to return
			 * 
			 */
			int newLineEnd = j + 1 + newLine;
			if (newLineInSeparator == -1) {
				return newLineEnd + (sep ? separator.length - 1 : 0);
			} else {
				int limit = Math.max(0, j - newLineInSeparator);
				while (j >= limit && !sep) {
					sep = separatorAt(--j);
				}
				return sep ? j + separator.length : newLineEnd;
			}
		}

		@Override
		protected int linesAfterStart(int i, int maxLines) {
			int j = globalPosition(i) + patternLength();
			int lineCount = 0;
			boolean sep = false;
			while (j < data.length && !(sep = separatorAt(j)) && lineCount <= maxLines) {
				if (isNewLine(j, data) > 0) {
					lineCount++;
				}
				j++;
			}
			return j == data.length || sep ? j : j - 1;
		}
	}

}
