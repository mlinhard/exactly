package sk.linhard.exactly.impl;

import sk.linhard.exactly.Document;
import sk.linhard.exactly.Hit;
import sk.linhard.exactly.HitContext;
import sk.linhard.exactly.impl.DefaultSearch.DefaultSearchResult;

class DefaultHit implements Hit<byte[]> {

	private final DefaultSearchResult searchResult;
	private final int hitIdx;

	public DefaultHit(DefaultSearchResult defaultSearchResult, int hitIdx) {
		searchResult = defaultSearchResult;
		this.hitIdx = hitIdx;
	}

	@Override
	public String toString() {
		return "[hitIdx=" + hitIdx + ", doc=" + document().id() + ", pos=" + position() + "]";
	}

	@Override
	public int globalPosition() {
		return searchResult.globalPosition(hitIdx);
	}

	@Override
	public int position() {
		return searchResult.position(hitIdx);
	}

	@Override
	public Document<byte[]> document() {
		return searchResult.document(hitIdx);
	}

	@Override
	public HitContext<byte[]> charContext(int charsBefore, int charsAfter) {
		return searchResult.charContext(hitIdx, charsBefore, charsAfter);
	}

	@Override
	public HitContext<byte[]> safeCharContext(int charsBefore, int charsAfter) {
		return searchResult.safeCharContext(hitIdx, charsBefore, charsAfter);
	}

	@Override
	public HitContext<byte[]> lineContext(int linesBefore, int linesAfter) {
		return searchResult.lineContext(hitIdx, linesBefore, linesAfter);
	}

}