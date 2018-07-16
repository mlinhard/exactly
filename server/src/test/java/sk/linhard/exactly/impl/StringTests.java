package sk.linhard.exactly.impl;

import org.junit.Assert;
import org.junit.Test;

import sk.linhard.exactly.Hit;
import sk.linhard.exactly.HitContext;
import sk.linhard.exactly.Search;
import sk.linhard.exactly.SearchResult;
import sk.linhard.exactly.StringSearchBuilder;

public class StringTests {

	private StringSearchBuilder searchBuilder = new StringSearchBuilder();
	private Search<String> search;
	private StringSearchResultChecker srChecker;

	protected void document(String document) {
		searchBuilder.add(Integer.toString(searchBuilder.size()), document);
	}

	protected void buildSearch() {
		search = searchBuilder.build();
	}

	protected StringSearchResultChecker find(String pattern) {
		return (srChecker = new StringSearchResultChecker(search.find(pattern)));
	}

	protected void assertPositions(String pattern, int... position) {
		find(pattern);
		srChecker.assertPositions(position);
	}

	protected Hit<String> assertSingleHit(String pattern, int document, int position) {
		SearchResult<String> r = search.find(pattern);
		Assert.assertEquals(1, r.size());
		Hit<String> hit = r.hit(0);
		Assert.assertEquals(document, hit.document().index());
		Assert.assertEquals(position, hit.position());
		return hit;
	}

	protected void assertSingleHit(String pattern, int document, int position, int maxCtx, String leftCtx,
			String rightCtx) {
		Hit<String> hit = assertSingleHit(pattern, document, position);
		HitContext<String> ctx = hit.charContext(maxCtx, maxCtx);
		Assert.assertEquals(leftCtx, ctx.before());
		Assert.assertEquals(rightCtx, ctx.after());
	}

	@Test
	public void testAbracadabra() {
		document("abracadabra");
		buildSearch();
		assertPositions("abracadabra", 0);
		assertPositions("bracadabra", 1);
		assertPositions("racadabra", 2);
		assertPositions("acadabra", 3);
		assertPositions("cadabra", 4);
		assertPositions("adabra", 5);
		assertPositions("dabra", 6);
		assertPositions("abra", 7, 0);
		assertPositions("bra", 8, 1);
		assertPositions("ra", 9, 2);
		assertPositions("a", 10, 7, 0, 3, 5);
		assertPositions("b", 8, 1);
		assertPositions("c", 4);
		assertPositions("d", 6);
		assertPositions("r", 9, 2);
	}

	@Test
	public void testAcaaacatat() {
		document("acaaacatat");
		buildSearch();
		assertPositions("acaaacatat", 0);
		assertPositions("caaacatat", 1);
		assertPositions("aaacatat", 2);
		assertPositions("aacatat", 3);
		assertPositions("acatat", 4);
		assertPositions("catat", 5);
		assertPositions("atat", 6);
		assertPositions("tat", 7);
		assertPositions("at", 8, 6);
		assertPositions("t", 9, 7);

		assertPositions("acaaacatat", 0);
		assertPositions("acaaacata", 0);
		assertPositions("acaaacat", 0);
		assertPositions("acaaaca", 0);
		assertPositions("acaaac", 0);
		assertPositions("acaaa", 0);
		assertPositions("acaa", 0);
		assertPositions("aca", 0, 4);
		assertPositions("ac", 0, 4);
		assertPositions("a", 2, 3, 0, 4, 8, 6);

		assertPositions("caaacatat", 1);
		assertPositions("caaacata", 1);
		assertPositions("caaacat", 1);
		assertPositions("caaaca", 1);
		assertPositions("caaac", 1);
		assertPositions("caaa", 1);
		assertPositions("caa", 1);
		assertPositions("ca", 1, 5);
		assertPositions("c", 1, 5);

		assertPositions("aaacatat", 2);
		assertPositions("aaacata", 2);
		assertPositions("aaacat", 2);
		assertPositions("aaaca", 2);
		assertPositions("aaac", 2);
		assertPositions("aaa", 2);
		assertPositions("aa", 2, 3);

		assertPositions("aacatat", 3);
		assertPositions("aacata", 3);
		assertPositions("aacat", 3);
		assertPositions("aaca", 3);
		assertPositions("aac", 3);

		assertPositions("acatat", 4);
		assertPositions("acata", 4);
		assertPositions("acat", 4);

		assertPositions("catat", 5);
		assertPositions("cata", 5);
		assertPositions("cat", 5);

		assertPositions("atat", 6);
		assertPositions("ata", 6);
	}

	@Test
	public void testMississippi() {
		document("mississippi");
		buildSearch();
		assertPositions("mississippi", 0);
		assertPositions("ississippi", 1);
		assertPositions("ssissippi", 2);
		assertPositions("sissippi", 3);
		assertPositions("issippi", 4);
		assertPositions("ssippi", 5);
		assertPositions("sippi", 6);
		assertPositions("ippi", 7);
		assertPositions("ppi", 8);
		assertPositions("pi", 9);
		assertPositions("i", 10, 7, 4, 1);

		assertPositions("mississippi", 0);
		assertPositions("mississipp", 0);
		assertPositions("mississip", 0);
		assertPositions("mississi", 0);
		assertPositions("mississ", 0);
		assertPositions("missis", 0);
		assertPositions("missi", 0);
		assertPositions("miss", 0);
		assertPositions("mis", 0);
		assertPositions("mi", 0);
		assertPositions("m", 0);

		assertPositions("ississippi", 1);
		assertPositions("ississipp", 1);
		assertPositions("ississip", 1);
		assertPositions("ississi", 1);
		assertPositions("ississ", 1);
		assertPositions("issis", 1);
		assertPositions("issi", 1, 4);
		assertPositions("iss", 1, 4);
		assertPositions("is", 1, 4);

		assertPositions("ssissippi", 2);
		assertPositions("ssissipp", 2);
		assertPositions("ssissip", 2);
		assertPositions("ssissi", 2);
		assertPositions("ssiss", 2);
		assertPositions("ssis", 2);
		assertPositions("ssi", 2, 5);
		assertPositions("ss", 2, 5);
		assertPositions("s", 2, 3, 5, 6);

		assertPositions("sissippi", 3);
		assertPositions("sissipp", 3);
		assertPositions("sissip", 3);
		assertPositions("sissi", 3);
		assertPositions("siss", 3);
		assertPositions("sis", 3);
		assertPositions("si", 3, 6);

		assertPositions("issippi", 4);
		assertPositions("issipp", 4);
		assertPositions("issip", 4);

		assertPositions("ssippi", 5);
		assertPositions("ssipp", 5);
		assertPositions("ssip", 5);

		assertPositions("sippi", 6);
		assertPositions("sipp", 6);
		assertPositions("sip", 6);
	}

	@Test
	public void testJoin() {
		document("abcde");
		document("fghij");
		document("klmno");
		document("pqrst");
		buildSearch();
		assertPositions("defg");
		assertSingleHit("abc", 0, 0);
		assertSingleHit("fgh", 1, 0);
		assertSingleHit("klm", 2, 0);
		assertSingleHit("pqr", 3, 0);

		assertSingleHit("bcd", 0, 1, 2, "a", "e");
		assertSingleHit("ghi", 1, 1, 1, "f", "j");
		assertSingleHit("lmn", 2, 1, 10, "k", "o");
		assertSingleHit("qrs", 3, 1, 100, "p", "t");

		assertSingleHit("abcde", 0, 0);
		assertSingleHit("fghij", 1, 0);
		assertSingleHit("klmno", 2, 0);
		assertSingleHit("pqrst", 3, 0);

		assertPositions("abcde", 0);
		assertPositions("fghij", 0);
		assertPositions("klmno", 0);
		assertPositions("pqrst", 0);
	}

	protected void assertLinesAbove(int maxLines, String expectedLinesAbove) {
		srChecker.assertLinesBefore(maxLines, expectedLinesAbove);
	}

	protected void assertLinesBelow(int maxLines, String expectedLinesBelow) {
		srChecker.assertLinesAfter(maxLines, expectedLinesBelow);
	}

	@Test
	public void testLineContext() {
		document("aaa\nbbb\nccc\nddd\neee");
		buildSearch();
		find("ccc");
		assertLinesAbove(0, "");
		assertLinesAbove(1, "bbb\n");
		assertLinesAbove(2, "aaa\nbbb\n");
		assertLinesAbove(3, "aaa\nbbb\n");
		assertLinesBelow(0, "");
		assertLinesBelow(1, "\nddd");
		assertLinesBelow(2, "\nddd\neee");
		assertLinesBelow(3, "\nddd\neee");
	}

	@Test
	public void testLineContext2() {
		document("aaa\nbbb\nccGGcc\nddd\neee");
		buildSearch();
		find("GG");
		assertLinesAbove(0, "cc");
		assertLinesAbove(1, "bbb\ncc");
		assertLinesAbove(2, "aaa\nbbb\ncc");
		assertLinesAbove(3, "aaa\nbbb\ncc");
		assertLinesBelow(0, "cc");
		assertLinesBelow(1, "cc\nddd");
		assertLinesBelow(2, "cc\nddd\neee");
		assertLinesBelow(3, "cc\nddd\neee");
	}

	@Test
	public void testAaaa() {
		document("aaaaaaaaaaaaaaaaaaaa");
		buildSearch();
		assertPositions("aaaa", 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16);
	}

}
