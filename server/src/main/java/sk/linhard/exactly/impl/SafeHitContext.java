package sk.linhard.exactly.impl;

/**
 * Removes special characters from context for better display in some cases
 *
 */
public class SafeHitContext extends DefaultHitContext {

	SafeHitContext(DefaultSearch search, int ctxPosition, int beforeLength, int patternLength, int afterLength) {
		super(search, ctxPosition, beforeLength, patternLength, afterLength);
	}

	private byte[] clean(byte[] data) {
		for (int i = 0; i < data.length; i++) {
			data[i] = clean(data[i]);
		}
		return data;
	}

	private byte clean(byte c) {
		return c == 127 || c < 32 ? 32 : c;
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
