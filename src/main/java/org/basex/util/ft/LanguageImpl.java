package org.basex.util.ft;

import java.util.Collection;
import java.util.HashSet;

/**
 * Abstract class for stemmer and tokenizer implementations.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Jens Erat
 */
abstract class LanguageImpl extends FTIterator
    implements Comparable<LanguageImpl> {

  /**
   * Returns the precedence of the processor. If two language implementations
   * support the same language, the processor with the higher precedence
   * will be selected.
   * @return precedence
   */
  protected abstract byte prec();

  /**
   * Checks if the specified language is supported.
   * @param ln language
   * @return true if language is supported
   */
  public boolean supports(final Language ln) {
    return languages().contains(ln);
  }

  /**
   * Creates a collection with the specified language.
   * @param ln language
   * @return collection
   */
  protected Collection<Language> collection(final String ln) {
    final HashSet<Language> coll = new HashSet<Language>();
    coll.add(Language.get(ln));
    return coll;
  }

  /**
   * Returns the supported languages.
   * @return languages
   */
  abstract Collection<Language> languages();

  @Override
  public final boolean equals(final Object o) {
    return o instanceof LanguageImpl && ((LanguageImpl) o).prec() == prec();
  }

  @Override
  public final int compareTo(final LanguageImpl o) {
    return o.prec() - prec();
  }

  @Override
  public int hashCode() {
    return prec();
  }
}
