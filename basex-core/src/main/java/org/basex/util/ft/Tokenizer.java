package org.basex.util.ft;

import static org.basex.util.Token.*;

import java.util.*;

import org.basex.util.*;

/**
 * Abstract tokenizer.
 *
 * @author BaseX Team, BSD License
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

  /**
   * Converts the specified token to upper case.
   * @param token token to be converted
   * @return the converted token
   */
  static byte[] upper(final byte[] token) {
    final int tl = token.length;
    if(ascii(token)) {
      int i = -1;
      while(++i < tl && token[i] == uc(token[i]));
      if(i == tl) return token;
      final byte[] tmp = token.clone();
      for(; i < tl; ++i) tmp[i] = (byte) uc(tmp[i]);
      return tmp;
    }
    final TokenBuilder tb = new TokenBuilder(tl);
    forEachCp(token, cp -> tb.add(uc(cp)));
    return tb.finish();
  }

  /**
   * Converts the specified token to lower case.
   * @param token token to be converted
   * @return the converted token
   */
  static byte[] lower(final byte[] token) {
    final int tl = token.length;
    if(ascii(token)) {
      int i = -1;
      while(++i < tl && token[i] == lc(token[i]));
      if(i == tl) return token;
      final byte[] tmp = token.clone();
      for(; i < tl; ++i) tmp[i] = (byte) lc(tmp[i]);
      return tmp;
    }
    final TokenBuilder tb = new TokenBuilder(tl);
    forEachCp(token, cp -> tb.add(lc(cp)));
    return tb.finish();
  }
}
