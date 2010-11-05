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
    return o.prec() - prec();
  }

  /**
   * Returns the precedence of the processor.
   * @return precedence
   */
  abstract int prec();

  /**
   * Checks if the class is represented by the specified identifier.
   * @param id identifier
   * @return true if represented by identifier
   */
  @SuppressWarnings("unused")
  boolean eq(final byte[] id) {
    return false;
  }

  /**
   * Checks if the specified language is supported.
   * @param ln language
   * @return true if language is supported
   */
  boolean supports(final byte[] ln) {
    for(final Language lt : languages()) {
      if(Token.eq(ln, lt.ln)) return true;
    }
    return false;
  }

  /**
   * Returns the supported languages.
   * @return languages
   */
  abstract EnumSet<Language> languages();
}
