package sk.linhard.exactly.impl;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Random;

import org.apache.commons.lang3.ArrayUtils;
import org.junit.Assert;

public class TestUtil {

	public static byte[] join(byte[] a, byte[] b) {
		byte[] c = new byte[a.length + b.length];
		System.arraycopy(a, 0, c, 0, a.length);
		System.arraycopy(b, 0, c, a.length, b.length);
		return c;
	}

	public static byte[] bytes(int... a) {
		byte[] b = new byte[a.length];
		for (int i = 0; i < b.length; i++) {
			b[i] = (byte) a[i];
		}
		return b;
	}

	public static byte[] bytes(String s) {
		try {
			byte[] bytes = s.getBytes("UTF-8");
			Assert.assertEquals(s.length(), bytes.length);
			return bytes;
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}

	public static byte[] randomBytes(int n) {
		Random r = new Random();
		byte[] rb = new byte[n];
		r.nextBytes(rb);
		return rb;
	}

	public static String print(byte[] b) {
		return Arrays.asList(ArrayUtils.toObject(b)).toString();
	}
}
