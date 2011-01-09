package org.basex.util.ft;

import java.util.EnumSet;

/**
 * Dictionary-based stemmer.
 *
 * @author BaseX Team 2005-11, ISC License
 * @author Dimitar Popov
 */
final class DictStemmer extends Stemmer {
  /** Stem dictionary. */
  final StemDir dict;

  /**
   * Constructor.
   * @param d stem dictionary
   * @param fti full-text iterator
   */
  DictStemmer(final StemDir d, final FTIterator fti) {
    super(fti);
    dict = d;
  }

  @Override
  Stemmer get(final Language l, final FTIterator fti) {
    return new DictStemmer(dict, fti);
  }

  @Override
  int prec() {
    return 1001;
  }

  @Override
  byte[] stem(final byte[] word) {
    return dict.stem(word);
  }

  @Override
  EnumSet<Language> languages() {
    return EnumSet.allOf(Language.class);
  }
}
