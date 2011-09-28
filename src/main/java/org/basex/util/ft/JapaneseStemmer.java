package org.basex.util.ft;

/**
 * Dummy stemmer to activate Japanese tokenizer.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
final class JapaneseStemmer extends InternalStemmer {
  /**
   * Constructor.
   * @param fti full-text iterator
   */
  JapaneseStemmer(final FTIterator fti) {
    super(fti);
  }

  @Override
  Stemmer get(final Language l, final FTIterator fti) {
    return new JapaneseStemmer(fti);
  }

  @Override
  protected byte[] stem(final byte[] word) {
    return word;
  }
}
