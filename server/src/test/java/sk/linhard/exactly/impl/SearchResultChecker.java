package sk.linhard.exactly.impl;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.ArrayUtils;
import org.junit.Assert;

import sk.linhard.exactly.Hit;
import sk.linhard.exactly.SearchResult;

public class SearchResultChecker {
	private SearchResult<byte[]> searchResult;

	public SearchResultChecker(SearchResult<byte[]> searchResult) {
		this.searchResult = searchResult;
	}

	public SearchResult<byte[]> result() {
		return searchResult;
	}

	public void assertEmpty() {
		Assert.assertEquals(0, searchResult.size());
	}

	public void assertHasGlobalPosition(int position) {
		Assert.assertTrue("Position " + position + " not found", searchResult.hasGlobalPosition(position));
	}

	private <T> T require(T object, String message) {
		Assert.assertNotNull(message, object);
		return object;
	}

	// TODO: rename before after
	public void assertLinesAbove(int doc, int pos, int maxLines, int... expectedBytes) {
		Hit<byte[]> hit = require(searchResult.hitWithPosition(doc, pos),
				"Hit doc=" + doc + ", pos=" + pos + " not found");
		Assert.assertArrayEquals(TestUtil.bytes(expectedBytes), hit.lineContext(maxLines, 0).before());
	}

	public void assertLinesBelow(int doc, int pos, int maxLines, int... expectedBytes) {
		Hit<byte[]> hit = require(searchResult.hitWithPosition(doc, pos),
				"Hit doc=" + doc + ", pos=" + pos + " not found");
		Assert.assertArrayEquals(TestUtil.bytes(expectedBytes), hit.lineContext(0, maxLines).after());
	}

	public void assertGlobalPositions(int... position) {
		Set<Integer> expectedPositions = new HashSet<>(Arrays.asList(ArrayUtils.toObject(position)));
		Set<Integer> actualPositions = globalPositionSet();
		Assert.assertEquals(expectedPositions, actualPositions);
	}

	public Set<Integer> globalPositionSet() {
		return searchResult.hits().stream().map(h -> h.globalPosition()).collect(Collectors.toSet());
	}

}
