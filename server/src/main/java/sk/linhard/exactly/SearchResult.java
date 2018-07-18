package sk.linhard.exactly;

import java.util.List;

import sk.linhard.exactly.impl.DefaultSearch;

/**
 * Result of the search for pattern in the text indexed by {@link DefaultSearch}
 */
public interface SearchResult<TContent> extends Iterable<Hit<TContent>> {

	/**
	 * @return Number of occurrences of the pattern found
	 */
	int size();

	boolean isEmpty();

	/**
	 * @param i
	 * @return i-th hit (occurence of pattern)
	 */
	Hit<TContent> hit(int i);

	List<Hit<TContent>> hits();

	/**
	 * 
	 * @return Length of the original pattern that we searched for.
	 */
	int patternLength();

	/**
	 * 
	 * @return Pattern that we searched for.
	 */
	TContent pattern();

	/**
	 * @param position
	 * @return True iff pattern was found on given position
	 */
	boolean hasGlobalPosition(int position);

	Hit<TContent> hitWithGlobalPosition(int position);

	boolean hasPosition(int document, int position);

	Hit<TContent> hitWithPosition(int document, int position);

	Iterable<Hit<TContent>> skipIterator(int offset);
}
