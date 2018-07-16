package sk.linhard.exactly;

public interface Search<TContent> {

	/**
	 * @return Number of documents indexed
	 */
	int documentCount();

	/**
	 * @param i
	 * @return i-th document
	 */
	Document<TContent> document(int i);

	/**
	 * Find occurences of pattern in text.
	 * 
	 * @param pattern
	 * @return
	 */
	SearchResult<TContent> find(TContent pattern);

}
