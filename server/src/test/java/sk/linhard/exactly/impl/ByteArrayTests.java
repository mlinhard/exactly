package sk.linhard.exactly.impl;

import static sk.linhard.exactly.impl.TestUtil.bytes;
import static sk.linhard.exactly.impl.TestUtil.randomBytes;

import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;

import sk.linhard.exactly.Search;
import sk.linhard.exactly.SearchBuilder;

public class ByteArrayTests {

	private SearchBuilder searchBuilder = new SearchBuilder();
	private Search<byte[]> search;
	private SearchResultChecker srChecker;

	protected void document(byte[] document) {
		searchBuilder.add(Integer.toString(searchBuilder.size()), document);
	}

	protected void document(int... document) {
		searchBuilder.add(Integer.toString(searchBuilder.size()), TestUtil.bytes(document));
	}

	protected SearchResultChecker find(byte[] pattern) {
		return (srChecker = new SearchResultChecker(search.find(pattern)));
	}

	protected SearchResultChecker find(int... pattern) {
		return (srChecker = new SearchResultChecker(search.find(TestUtil.bytes(pattern))));
	}

	protected void buildSearch() {
		search = searchBuilder.build();
	}

	protected void buildSearch(int... separatorBytes) {
		search = searchBuilder.build(separatorBytes.length == 0 ? null : bytes(separatorBytes));
	}

	@Test
	public void testEmpty() {
		document();
		buildSearch();
		find(1).assertEmpty();
		try {
			find();
			Assert.fail();
		} catch (RuntimeException e) {
			// ok
		}
		try {
			find((byte[]) null);
			Assert.fail();
		} catch (RuntimeException e) {
			// ok
		}
	}

	@Test
	public void testRandomMegabyte() {
		byte[] randomBytes = randomBytes(1024 * 1024); // 1mb of random data
		document(randomBytes);
		buildSearch();
		for (int i = 0; i < 1024; i++) {
			int position = i * 1024;
			byte[] chunk = Arrays.copyOfRange(randomBytes, position, position + 1024);
			find(chunk).assertHasGlobalPosition(position);
		}
	}

	@Test
	public void testZeroMegabyte() {
		document(new byte[1024 * 1024]); // 1mb of zeroes
		buildSearch();
		SearchResultChecker sr = find(new byte[1024]);
		for (int i = 0; i < 1024; i++) {
			sr.assertHasGlobalPosition(i);
		}
	}

	@Test
	public void testLinesBinary() {
		assertLinesAboveBelowScenario();
	}

	@Test
	public void testLinesBinaryNewLineInSeparator1() {
		assertLinesAboveBelowScenario(-128, 13, 10);
	}

	@Test
	public void testLinesBinaryNewLineInSeparator2() {
		assertLinesAboveBelowScenario(-128, 13, 10, -128);
	}

	@Test
	public void testLinesBinaryNewLineInSeparator3() {
		assertLinesAboveBelowScenario(10, 13);
	}

	private void assertLinesAboveBelowScenario(int... separator) {
		document(0, 0, 0, 13, 1, 1, 1, 13, 2, 2, 2, 13, 3, 3, 3, 13, 4, 4, 4);
		document(0, 0, 0, 13, 10, 1, 1, 1, 13, 10, 2, 2, 2, 13, 10, 3, 3, 3, 13, 10, 4, 4, 4);

		buildSearch(separator);

		find(2, 2, 2);

		assertLinesAbove(0, 8, 0);
		assertLinesAbove(0, 8, 1, 1, 1, 1, 13);
		assertLinesAbove(0, 8, 2, 0, 0, 0, 13, 1, 1, 1, 13);
		assertLinesAbove(0, 8, 3, 0, 0, 0, 13, 1, 1, 1, 13);

		assertLinesAbove(1, 10, 0);
		assertLinesAbove(1, 10, 1, 1, 1, 1, 13, 10);
		assertLinesAbove(1, 10, 2, 0, 0, 0, 13, 10, 1, 1, 1, 13, 10);
		assertLinesAbove(1, 10, 3, 0, 0, 0, 13, 10, 1, 1, 1, 13, 10);

		assertLinesBelow(0, 8, 0);
		assertLinesBelow(0, 8, 1, 13, 3, 3, 3);
		assertLinesBelow(0, 8, 2, 13, 3, 3, 3, 13, 4, 4, 4);
		assertLinesBelow(0, 8, 3, 13, 3, 3, 3, 13, 4, 4, 4);

		assertLinesBelow(1, 10, 0);
		assertLinesBelow(1, 10, 1, 13, 10, 3, 3, 3);
		assertLinesBelow(1, 10, 2, 13, 10, 3, 3, 3, 13, 10, 4, 4, 4);
		assertLinesBelow(1, 10, 3, 13, 10, 3, 3, 3, 13, 10, 4, 4, 4);
	}

	private void assertLinesAbove(int doc, int pos, int maxLines, int... expectedBytes) {
		srChecker.assertLinesAbove(doc, pos, maxLines, expectedBytes);
	}

	private void assertLinesBelow(int doc, int pos, int maxLines, int... expectedBytes) {
		srChecker.assertLinesBelow(doc, pos, maxLines, expectedBytes);
	}

}
