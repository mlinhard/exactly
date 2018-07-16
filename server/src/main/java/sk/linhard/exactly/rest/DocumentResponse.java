package sk.linhard.exactly.rest;

import java.util.Base64;

import org.json.JSONException;
import org.json.JSONObject;

import com.fasterxml.jackson.annotation.JsonProperty;

public class DocumentResponse {
	@JsonProperty("document_id")
	private final String documentId;

	@JsonProperty("document_index")
	private final Integer documentIndex;

	private final byte[] content;

	public DocumentResponse(String documentId, Integer documentIndex, byte[] content) {
		this.documentId = documentId;
		this.documentIndex = documentIndex;
		this.content = content;
	}

	public DocumentResponse(String jsonStr) {
		try {
			JSONObject jsonObj = new JSONObject(jsonStr);
			documentId = jsonObj.getString("document_id");
			String docIdxObj = jsonObj.getString("document_index");
			documentIndex = docIdxObj == null ? null : Integer.parseInt(docIdxObj);
			content = Base64.getDecoder().decode(jsonObj.getString("content"));
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

	public byte[] getContent() {
		return content;
	}

}
