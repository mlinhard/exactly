package sk.linhard.exactly.gui;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import sk.linhard.exactly.Hit;
import sk.linhard.exactly.HitContext;
import sk.linhard.exactly.SearchResult;

public class SearchResultItem {

	private SearchResult<String> sr;
	private Hit<String> hit;

	public SearchResultItem(SearchResult<String> sr, Hit<String> hit) {
		this.sr = sr;
		this.hit = hit;
	}

	public static List<SearchResultItem> toItems(int maxLen, SearchResult<String> sr) {
		int listLen = Math.min(sr.size(), maxLen);
		List<SearchResultItem> r = new ArrayList<>(listLen);
		for (int i = 0; i < listLen; i++) {
			r.add(new SearchResultItem(sr, sr.hit(i)));
		}
		return r;
	}

	public String file() {
		return hit.document().id();
	}

	public HitContext<String> charContext(int maxCtx) {
		return hit.charContext(maxCtx, maxCtx);
	}

	public HitContext<String> lineContext(int maxLines) {
		return hit.lineContext(maxLines, maxLines);
	}

	public String matchLine(int maxCtx) {
		HitContext<String> ctx = hit.safeCharContext(maxCtx, maxCtx);
		return StringUtils.leftPad(ctx.before(), maxCtx) + ctx.pattern() + StringUtils.rightPad(ctx.after(), maxCtx);
	}

	public int patternLength() {
		return sr.patternLength();
	}
}
