package org.basex.util.ft;

import java.util.Collections;
import java.util.LinkedList;
import org.basex.core.Prop;

/**
 * Abstract tokenizer.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Jens Erat
 */
public abstract class Tokenizer extends LanguageImpl {
  /** List of available tokenizers. */
  public static final LinkedList<Tokenizer> IMPL;

  /** Are special characters included? */
  protected boolean special;

  /** Load tokenizer classes and order them by precedence. */
  static {
    IMPL = new LinkedList<Tokenizer>();
    IMPL.add(new WesternTokenizer(null));
    Collections.sort(IMPL);
  }

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
  
  /**
   * Returns an iterator.
   * @return iterator
   */
  abstract FTIterator iter();
}
