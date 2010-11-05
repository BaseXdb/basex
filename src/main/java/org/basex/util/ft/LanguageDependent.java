package org.basex.util.ft;

import java.util.EnumSet;
import org.basex.util.Token;

/**
 * Functions for judging which classes (eg. tokenizers, stemmers) match to
 * chosen language.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Jens Erat
 */
abstract class LanguageDependent implements Comparable<LanguageDependent> {
  @Override
  public int compareTo(final LanguageDependent o) {
    // Higher precedence value = better
    return o.getPrecedence() - getPrecedence();
  }

  /**
   * Returns precedence of SpanProcessor.
   * @return precedence of SpanProcessor.
   */
  abstract int getPrecedence();

  /**
   * Checks if class is represented by identifier.
   * @param id identifier
   * @return true if represented by identifier
   */
  @SuppressWarnings("unused")
  boolean isRepresentedByIdentifier(final byte[] id) {
    return false;
  }

  /**
   * Checks if class supports language.
   * @param ln language
   * @return true if language is supported
   */
  boolean isLanguageSupported(final byte[] ln) {
    for(final LanguageTokens lt : supportedLanguages()) {
      if(Token.eq(ln, lt.ln)) return true;
    }
    return false;
  }

  /**
   * Returns supported languages.
   * @return supported languages
   */
  abstract EnumSet<LanguageTokens> supportedLanguages();
}
