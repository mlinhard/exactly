package sk.linhard.exactly.impl;

import static sk.linhard.exactly.impl.TestUtil.bytes;

import org.junit.Assert;
import org.junit.Test;

import sk.linhard.exactly.Search;
import sk.linhard.exactly.SearchBuilder;
import sk.linhard.exactly.SearchResult;
import sk.linhard.exactly.impl.DefaultSearch;
import sk.linhard.exactly.impl.EnhancedSuffixArray;
import sk.linhard.exactly.impl.MultiDocumentSearch;

public class SASearchSeparatorTest {

	private DefaultSearch assertSeparator(byte[] input, int... expectedSeparatorInts) {
		return assertSeparator(input, bytes(expectedSeparatorInts));
	}

	private DefaultSearch assertSeparator(byte[] input, byte[] expectedSeparator) {
		MultiDocumentSearch s = MultiDocumentSearch.compute(input, new int[] { 0 }, new String[] { "0" });
		byte[] actualSeparator = s.findSeparator();
		Assert.assertArrayEquals(expectedSeparator, actualSeparator);
		return s;
	}

	@Test
	public void testFindSeparator1() {
		assertSeparator(TestUtil.bytes("a"), -128);
		assertSeparator(new byte[] { -127 }, -128);
		assertSeparator(new byte[] { -128 }, -127);
		assertSeparator(new byte[] { -128, -127 }, -126);
		assertSeparator(new byte[] { -128, -126 }, -127);
	}

	@Test
	public void testFindSeparator2() {
		byte[] data = new byte[256 * 256 * 2];
		for (int i = 0; i < 256 * 256; i++) {
			data[2 * i] = (byte) (i >> 8);
			data[2 * i + 1] = (byte) (i);
		}
		MultiDocumentSearch s = MultiDocumentSearch.compute(data, new int[] { 0 }, new String[] { "0" });
		byte[] sep = s.findSeparator();
		Assert.assertEquals(0, s.find(sep).size());
	}

	@Test
	public void testFindSeparator3() {
		byte[] data = new byte[256];
		for (int i = 0; i < 256; i++) {
			data[i] = (byte) i;
		}
		MultiDocumentSearch s = MultiDocumentSearch.compute(data, new int[] { 0 }, new String[] { "0" });
		byte[] sep = s.findSeparator();
		Assert.assertEquals(0, s.find(sep).size());
	}

	@Test
	public void testFindSeparator4() {
		MultiDocumentSearch s = MultiDocumentSearch.compute(TestUtil.randomBytes(1024), new int[] { 0 },
				new String[] { "0" });
		byte[] sep = s.findSeparator();
		Assert.assertEquals(0, s.find(sep).size());
	}

	// @Test
	public void testMultiRandom() {
		SearchBuilder builder = new SearchBuilder();
		byte[][] random = new byte[1024][];
		for (int i = 0; i < 1024; i++) {
			random[i] = TestUtil.randomBytes(1024);
			builder.add(Integer.toString(i), random[i]);
		}
		Search<byte[]> search = builder.build();
		for (int i = 0; i < random.length; i++) {
			SearchResult<byte[]> r = search.find(random[i]);
			Assert.assertTrue(r.hasPosition(i, 0));
		}

	}

	@Test
	public void introduceSeparators() throws Exception {
		EnhancedSuffixArray esa = new EnhancedSuffixArray(TestUtil.bytes("aabbcc"));
		esa.computeLCP(true);
		esa.computeUpDown();
		esa.computeNext();
		int[] offset = { 0, 2, 4 };
		esa.introduceSeparators(//
				offset, //
				TestUtil.bytes("dd"));
		Assert.assertEquals("aaddbbddcc", new String(esa.data, "UTF-8"));
		Assert.assertEquals(0, offset[0]);
		Assert.assertEquals(4, offset[1]);
		Assert.assertEquals(8, offset[2]);
	}
}
