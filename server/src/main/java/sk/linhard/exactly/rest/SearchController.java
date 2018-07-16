package sk.linhard.exactly.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SearchController {

	@Autowired
	private SearchServer server;

	@RequestMapping(value = "/search", method = RequestMethod.POST)
	public SearchResponse search(@RequestBody SearchRequest request) {
		return server.search(request);
	}

	@RequestMapping(value = "/stats")
	public SearchServerStats stats() {
		return server.stats();
	}

	@RequestMapping(value = "/version")
	public String version() {
		return server.version();
	}

	@RequestMapping(value = "/document", method = RequestMethod.POST)
	public DocumentResponse requestDocument(@RequestBody DocumentRequest request) {
		return server.requestDocument(request);
	}

	@RequestMapping(value = "/document/{idx}", method = RequestMethod.GET)
	public DocumentResponse requestDocument(@PathVariable("idx") int idx) {
		return server.requestDocument(new DocumentRequest(null, idx));
	}

}