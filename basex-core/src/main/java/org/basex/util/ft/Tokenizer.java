package org.basex.util.ft;

import java.util.*;

/**
 * Abstract tokenizer.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Jens Erat
 */
public abstract class Tokenizer extends LanguageImpl {
  /** List of available tokenizers. */
  static final ArrayList<Tokenizer> IMPL = new ArrayList<>();

  /** Return original tokens. */
  boolean original;
  /** Return all tokens. */
  boolean all;

  /* Load tokenizer classes and order them by precedence. */
  static {
    IMPL.add(new WesternTokenizer(null));
    if(JapaneseTokenizer.available()) IMPL.add(new JapaneseTokenizer(null));
    Collections.sort(IMPL);
  }

  /**
   * Checks if the language is supported by the available tokenizers.
   * @param language language to be found
   * @return result of check
   */
  public static boolean supportFor(final Language language) {
    for(final Tokenizer impl : IMPL) {
      if(impl.supports(language)) return true;
    }
    return false;
  }

  /**
   * Factory method.
   * @param f full-text options
   * @return tokenizer
   */
  abstract Tokenizer get(FTOpt f);

  /**
   * Gets full-text info for the specified token.
   * Needed for visualizations; does not have to be implemented by all tokenizers.
   * <ul>
   *   <li> int[0]: length of each token</li>
   *   <li> int[1]: sentence info, length of each sentence</li>
   *   <li> int[2]: paragraph info, length of each paragraph</li>
   *   <li> int[3]: each token as int[]</li>
   *   <li> int[4]: punctuation marks of each sentence</li>
   * </ul>
   * @return int arrays or empty array if not implemented
   */
  int[][] info() {
    return new int[0][];
  }

  /**
   * Checks if current token is a paragraph.
   * Needed for visualizations; Does not have to be implemented by all tokenizers.
   * @return whether current token is a paragraph, or {@code false} if not implemented
   */
  boolean paragraph() {
    return false;
  }

  /**
   * Calculates a position value, dependent on the specified unit. Does not have
   * to be implemented by all tokenizers. Returns 0 if not implemented.
   * @param word word position
   * @param unit unit
   * @return new position
   */
  @SuppressWarnings("unused")
  int pos(final int word, final FTUnit unit) {
    return 0;
  }
}
