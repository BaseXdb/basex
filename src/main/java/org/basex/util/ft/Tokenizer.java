package org.basex.util.ft;

import java.util.Collections;
import java.util.LinkedList;

/**
 * Abstract tokenizer.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Jens Erat
 */
public abstract class Tokenizer extends LanguageImpl {
  /** List of available tokenizers. */
  public static final LinkedList<Tokenizer> IMPL = new LinkedList<Tokenizer>();

  /** Are special characters included? */
  boolean special;

  /** Load tokenizer classes and order them by precedence. */
  static {
    IMPL.add(new WesternTokenizer(null));
    if(JapaneseTokenizer.available()) IMPL.add(new JapaneseTokenizer(null));
    Collections.sort(IMPL);
  }

  /**
   * Checks if the language is supported by the available tokenizers.
   * @param l language to be found
   * @return result of check
   */
  public static boolean supportFor(final Language l) {
    for(final Tokenizer t : Tokenizer.IMPL) if(t.supports(l)) return true;
    return false;
  }

  /**
   * Factory method.
   * @param f full-text options
   * @return tokenizer
   */
  abstract Tokenizer get(final FTOpt f);

  /**
   * Gets full-text info for the specified token; needed for visualizations.
   * Does not have to be implemented by all tokenizers.
   * <ul>
   * <li/>int[0]: length of each token
   * <li/>int[1]: sentence info, length of each sentence
   * <li/>int[2]: paragraph info, length of each paragraph
   * <li/>int[3]: each token as int[]
   * <li/>int[4]: punctuation marks of each sentence
   * </ul>
   * @return int arrays or empty array if not implemented
   */
  int[][] info() {
    return new int[0][];
  }

  /**
   * Checks if current token is a paragraph. Does not have to be implemented
   * by all tokenizers. Returns false if not implemented.
   * @return whether current token is a paragraph
   */
  boolean paragraph() {
    return false;
  }

  /**
   * Calculates a position value, dependent on the specified unit. Does not have
   * to be implemented by all tokenizers. Returns 0 if not implemented.
   * @param w word position
   * @param u unit
   * @return new position
   */
  @SuppressWarnings("unused")
  int pos(final int w, final FTUnit u) {
    return 0;
  }
}
