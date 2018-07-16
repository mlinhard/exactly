package sk.linhard.exactly;

/**
 * Represents text context around the match. This can be lines or characters
 * around the pattern. You usually want to display the whole string before +
 * pattern + after with highlighted pattern.
 *
 * @param <TContent>
 */
public interface HitContext<TContent> {

	TContent before();

	TContent pattern();

	TContent after();

	/**
	 * @return Length of string returned by {@link #before()} method
	 */
	int highlightStart();

	/**
	 * 
	 * @return Length of before string + length of pattern
	 */
	int highlightEnd();
}
