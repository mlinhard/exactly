package sk.linhard.exactly.rest;

import java.util.Base64;

import org.json.JSONException;
import org.json.JSONObject;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SearchRequest {

	private final byte[] pattern;

	@JsonProperty("max_candidates")
	private final int maxCandidates;

	@JsonProperty("max_context")
	private final int maxContext;

	public SearchRequest(String jsonStr) {
		try {
			JSONObject jsonObj = new JSONObject(jsonStr);
			pattern = Base64.getDecoder().decode(jsonObj.getString("pattern"));
			maxCandidates = jsonObj.getInt("max_candidates");
			maxContext = jsonObj.getInt("max_context");
		} catch (JSONException e) {
			throw new RuntimeException("Error parsing JSON", e);
		}
	}

	public SearchRequest(byte[] pattern, int maxCandidates, int maxContext) {
		super();
		this.pattern = pattern;
		this.maxCandidates = maxCandidates;
		this.maxContext = maxContext;
	}

	public byte[] getPattern() {
		return pattern;
	}

	public int getMaxCandidates() {
		return maxCandidates;
	}

	public int getMaxContext() {
		return maxContext;
	}

}
