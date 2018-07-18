package sk.linhard.exactly.rest;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SearchResponse {

	private final List<Hit> hits;
	private final Cursor cursor;

	public SearchResponse(List<Hit> hits, Cursor cursor) {
		this.hits = hits;
		this.cursor = cursor;
	}

	public List<Hit> getHits() {
		return hits;
	}

	public Cursor getCursor() {
		return cursor;
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

	public static class Cursor {

		@JsonProperty("complete_size")
		private final int completeSize;

		private final int offset;

		public Cursor(int completeSize, int offset) {
			this.completeSize = completeSize;
			this.offset = offset;
		}

		public int getCompleteSize() {
			return completeSize;
		}

		public int getOffset() {
			return offset;
		}

	}

}
