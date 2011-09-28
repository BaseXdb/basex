package org.basex.util.ft;

import java.util.Collection;

/**
 * Dictionary-based stemmer.
 *
 * @author BaseX Team 2005-11, BSD License
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
  Collection<Language> languages() {
    return Language.ALL.values();
  }

  @Override
  byte[] stem(final byte[] word) {
    return dict.stem(word);
  }

  @Override
  public String toString() {
    return "Dictionary";
  }
}
