package sk.linhard.exactly;

/**
 * Represents one occurrence of the pattern in the text composed of one or more
 * documents
 */
public interface Hit<TContent> {

	/**
	 * @return global position in concatenated string of all documents including
	 *         separators (will never return position inside of the separator)
	 */
	int globalPosition();

	/**
	 * @return position inside of the document, i.e. number of bytes from the
	 *         document start.
	 */
	int position();

	/**
	 * @return The document this hit was found in
	 */
	Document<TContent> document();

	/**
	 * Context of the found pattern inside of the document given as number of
	 * characters.
	 * 
	 * @param charsBefore
	 *            Number of characters / bytes to get. If the position -
	 *            charsBefore is before document start will return characters
	 *            from the beginning of the document
	 * @param charsAfter
	 * @return
	 */
	HitContext<TContent> charContext(int charsBefore, int charsAfter);

	HitContext<TContent> safeCharContext(int charsBefore, int charsAfter);

	HitContext<TContent> lineContext(int linesBefore, int linesAfter);

}