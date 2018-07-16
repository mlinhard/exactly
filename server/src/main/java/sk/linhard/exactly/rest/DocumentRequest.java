package sk.linhard.exactly.rest;

import org.json.JSONException;
import org.json.JSONObject;

import com.fasterxml.jackson.annotation.JsonProperty;

public class DocumentRequest {

	@JsonProperty("document_id")
	private final String documentId;

	@JsonProperty("document_index")
	private final Integer documentIndex;

	public DocumentRequest(String documentId, Integer documentIndex) {
		this.documentId = documentId;
		this.documentIndex = documentIndex;
	}

	public DocumentRequest(String jsonStr) {
		try {
			JSONObject jsonObj = new JSONObject(jsonStr);
			documentId = jsonObj.has("document_id") ? jsonObj.getString("document_id") : null;
			documentIndex = jsonObj.has("document_index") ? jsonObj.getInt("document_index") : null;
		} catch (JSONException e) {
			throw new RuntimeException("Error parsing JSON", e);
		}
	}

	public String getDocumentId() {
		return documentId;
	}

	public Integer getDocumentIndex() {
		return documentIndex;
	}

}
