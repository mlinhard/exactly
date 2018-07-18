package sk.linhard.exactly.rest;

import java.util.Base64;

import org.json.JSONException;
import org.json.JSONObject;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SearchRequest {

	private final byte[] pattern;

	@JsonProperty("max_hits")
	private final int maxHits;

	@JsonProperty("max_context")
	private final int maxContext;

	private final int offset;

	public SearchRequest(String jsonStr) {
		try {
			JSONObject jsonObj = new JSONObject(jsonStr);
			pattern = Base64.getDecoder().decode(jsonObj.getString("pattern"));
			maxHits = jsonObj.getInt("max_hits");
			maxContext = jsonObj.getInt("max_context");
			offset = jsonObj.has("offset") ? jsonObj.getInt("offset") : 0;
		} catch (JSONException e) {
			throw new RuntimeException("Error parsing JSON", e);
		}
	}

	public SearchRequest(byte[] pattern, int maxHits, int maxContext, int offset) {
		super();
		this.pattern = pattern;
		this.maxHits = maxHits;
		this.maxContext = maxContext;
		this.offset = offset;
	}

	public byte[] getPattern() {
		return pattern;
	}

	public int getMaxHits() {
		return maxHits;
	}

	public int getMaxContext() {
		return maxContext;
	}

	public int getOffset() {
		return offset;
	}

}
