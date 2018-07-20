package sk.linhard.exactly.impl;

import java.nio.charset.Charset;

import org.bouncycastle.util.Arrays;

/**
 * Removes special characters from context for better display in some cases
 *
 */
public class SafeHitContext extends DefaultHitContext {

	SafeHitContext(DefaultSearch search, int ctxPosition, int beforeLength, int patternLength, int afterLength) {
		super(search, ctxPosition, beforeLength, patternLength, afterLength);
	}

	private static byte[] clean(byte[] data) {
		for (int i = 0; i < data.length; i++) {
			data[i] = clean(data[i]);
		}
		return data;
	}

	private static byte clean(byte c) {
		return c == 127 || c < 32 ? 32 : c;
	}

	public static String toSafeString(byte[] bytes, Charset charset) {
		byte[] cleanCopy = clean(Arrays.copyOf(bytes, bytes.length));
		return new String(cleanCopy, charset);
	}

	@Override
	public byte[] before() {
		return clean(super.before());
	}

	@Override
	public byte[] pattern() {
		return clean(super.pattern());
	}

	@Override
	public byte[] after() {
		return clean(super.after());
	}
}
