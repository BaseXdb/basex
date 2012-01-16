package org.basex.util.ft;

import java.util.Collection;

/**
 * Dummy stemmer for languages that do not require stemming.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Toshio HIRAI
 */
final class DummyStemmer extends InternalStemmer {
  /**
   * Constructor.
   * @param fti full-text iterator
   */
  DummyStemmer(final FTIterator fti) {
    super(fti);
  }

  @Override
  Stemmer get(final Language l, final FTIterator fti) {
    return new DummyStemmer(fti);
  }

  @Override
  Collection<Language> languages() {
    return collection("ja");
  }

  @Override
  protected byte[] stem(final byte[] word) {
    return word;
  }
}
