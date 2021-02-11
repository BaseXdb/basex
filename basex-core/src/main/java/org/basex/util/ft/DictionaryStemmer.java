package org.basex.util.ft;

import java.util.*;

/**
 * Dictionary-based stemmer.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Dimitar Popov
 */
final class DictionaryStemmer extends Stemmer {
  /** Stem dictionary. */
  private final StemDir dict;

  /**
   * Constructor.
   * @param dict stem dictionary
   * @param fti full-text iterator
   */
  DictionaryStemmer(final StemDir dict, final FTIterator fti) {
    super(fti);
    this.dict = dict;
  }

  @Override
  Stemmer get(final Language lang, final FTIterator fti) {
    return new DictionaryStemmer(dict, fti);
  }

  @Override
  protected byte prec() {
    return 20;
  }

  @Override
  Collection<Language> languages() {
    return Language.ALL.values();
  }

  @Override
  protected byte[] stem(final byte[] word) {
    return dict.stem(word);
  }
}
