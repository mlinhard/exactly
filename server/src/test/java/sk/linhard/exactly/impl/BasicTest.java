package sk.linhard.exactly.impl;

import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;

public class BasicTest {
	@Test
	public void binarySearchTest() {
		int[] a = new int[] { 0, 12, 15, 16, 81 };
		Assert.assertEquals(-1, Arrays.binarySearch(a, -1));
		Assert.assertEquals(0, Arrays.binarySearch(a, 0));
		Assert.assertEquals(-2, Arrays.binarySearch(a, 1));
		Assert.assertEquals(-2, Arrays.binarySearch(a, 2));
		Assert.assertEquals(-2, Arrays.binarySearch(a, 11));
		Assert.assertEquals(1, Arrays.binarySearch(a, 12));
		Assert.assertEquals(-3, Arrays.binarySearch(a, 13));
		Assert.assertEquals(-3, Arrays.binarySearch(a, 14));
		Assert.assertEquals(2, Arrays.binarySearch(a, 15));
		Assert.assertEquals(3, Arrays.binarySearch(a, 16));
		Assert.assertEquals(-5, Arrays.binarySearch(a, 17));
		Assert.assertEquals(-5, Arrays.binarySearch(a, 80));
		Assert.assertEquals(4, Arrays.binarySearch(a, 81));
		Assert.assertEquals(-6, Arrays.binarySearch(a, 82));
	}

	@Test
	public void byteEqualityTest() {
		Byte b1 = new Byte((byte) 1);
		Byte b2 = new Byte((byte) 1);
		Assert.assertEquals(b1, b2);
		Assert.assertFalse(b1 == b2);
	}
}
