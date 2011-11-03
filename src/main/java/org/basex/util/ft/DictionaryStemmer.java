package org.basex.util.ft;

import java.util.Collection;

/**
 * Dictionary-based stemmer.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Dimitar Popov
 */
final class DictionaryStemmer extends Stemmer {
  /** Stem dictionary. */
  final StemDir dict;

  /**
   * Constructor.
   * @param d stem dictionary
   * @param fti full-text iterator
   */
  DictionaryStemmer(final StemDir d, final FTIterator fti) {
    super(fti);
    dict = d;
  }

  @Override
  Stemmer get(final Language l, final FTIterator fti) {
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
