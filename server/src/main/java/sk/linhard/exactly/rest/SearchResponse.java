package sk.linhard.exactly.rest;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SearchResponse {

	private final List<Hit> hits;

	public SearchResponse(List<Hit> hits) {
		this.hits = hits;
	}

	public List<Hit> getHits() {
		return hits;
	}

	public static class Hit {

		private final int pos;

		@JsonProperty("doc_id")
		private final String docId;

		@JsonProperty("ctx_before")
		private final byte[] ctxBefore;

		@JsonProperty("ctx_after")
		private final byte[] ctxAfter;

		public Hit(int pos, String docId, byte[] ctxBefore, byte[] ctxAfter) {
			this.pos = pos;
			this.docId = docId;
			this.ctxBefore = ctxBefore;
			this.ctxAfter = ctxAfter;
		}

		public int getPos() {
			return pos;
		}

		public String getDocId() {
			return docId;
		}

		public byte[] getCtxBefore() {
			return ctxBefore;
		}

		public byte[] getCtxAfter() {
			return ctxAfter;
		}

	}

}
