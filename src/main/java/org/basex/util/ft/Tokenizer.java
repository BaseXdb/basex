package org.basex.util.ft;

import org.basex.core.Prop;
import org.basex.query.ft.FTOpt;

/**
 * Abstract tokenizer.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Jens Erat
 */
abstract class Tokenizer extends LanguageDependent implements Iterable<Span> {
  /** Are special characters included? */
  protected boolean special;

  /**
   * Factory method.
   * @param t Text
   * @param p database properties
   * @param f full-text options
   * @return tokenizer instance
   */
  abstract Tokenizer get(final byte[] t, final Prop p, final FTOpt f);

  /**
   * Factory method.
   * @param t Text
   * @param p database properties
   * @param f full-text options
   * @param sc include special characters
   * @return tokenizer instance
   */
  final Tokenizer get(final byte[] t, final Prop p, final FTOpt f,
      final boolean sc) {
    final Tokenizer tk = get(t, p, f);
    tk.special = sc;
    return tk;
  }

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
   * @param t text to be parsed
   * @return int arrays or empty array if not implemented
   */
  int[][] info(@SuppressWarnings("unused") final byte[] t) {
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
  int pos(@SuppressWarnings("unused") final int w,
      @SuppressWarnings("unused") final FTUnit u) {
    return 0;
  }
}
