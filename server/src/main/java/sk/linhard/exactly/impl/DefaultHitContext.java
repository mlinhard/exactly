package sk.linhard.exactly.impl;

import sk.linhard.exactly.HitContext;

class DefaultHitContext implements HitContext<byte[]> {

	private DefaultSearch search;
	private int ctxPosition;
	private int beforeLength;
	private int patternLength;
	private int afterLength;

	DefaultHitContext(DefaultSearch search, int ctxPosition, int beforeLength, int patternLength, int afterLength) {
		this.search = search;
		this.ctxPosition = ctxPosition;
		this.beforeLength = beforeLength;
		this.patternLength = patternLength;
		this.afterLength = afterLength;
	}

	@Override
	public byte[] before() {
		return search.dataCopy(ctxPosition, beforeLength);
	}

	@Override
	public byte[] pattern() {
		return search.dataCopy(ctxPosition + beforeLength, patternLength);
	}

	@Override
	public byte[] after() {
		return search.dataCopy(ctxPosition + beforeLength + patternLength, afterLength);
	}

	@Override
	public int highlightStart() {
		return beforeLength;
	}

	@Override
	public int highlightEnd() {
		return beforeLength + patternLength;
	}

}
