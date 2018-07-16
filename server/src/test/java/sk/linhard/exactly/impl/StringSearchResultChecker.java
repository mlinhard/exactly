package sk.linhard.exactly.impl;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.ArrayUtils;
import org.junit.Assert;

import sk.linhard.exactly.SearchResult;

public class StringSearchResultChecker {

	private SearchResult<String> searchResult;

	public StringSearchResultChecker(SearchResult<String> searchResult) {
		this.searchResult = searchResult;
	}

	private Set<Integer> set(int... position) {
		return new HashSet<>(Arrays.asList(ArrayUtils.toObject(position)));
	}

	public void assertGlobalPositions(int... expectedGlobalPositions) {
		Assert.assertEquals(set(expectedGlobalPositions), globalPositionSet());
	}

	public void assertPositions(int... expectedPositions) {
		Assert.assertEquals(set(expectedPositions), positionSet());
	}

	public Set<Integer> globalPositionSet() {
		return searchResult.hits().stream().map(h -> h.globalPosition()).collect(Collectors.toSet());
	}

	public Set<Integer> positionSet() {
		return searchResult.hits().stream().map(h -> h.position()).collect(Collectors.toSet());
	}

	public void assertSize(int expectedSize) {
		Assert.assertEquals(expectedSize, searchResult.size());
	}

	public void assertLinesBefore(int maxLines, String expectedLinesAbove) {
		assertSize(1);
		Assert.assertEquals(expectedLinesAbove, searchResult.hit(0).lineContext(maxLines, 0).before());
	}

	public void assertLinesAfter(int maxLines, String expectedLinesBelow) {
		assertSize(1);
		Assert.assertEquals(expectedLinesBelow, searchResult.hit(0).lineContext(0, maxLines).after());
	}

}
