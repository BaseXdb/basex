package org.basex.util.ft;

import java.util.EnumSet;

/**
 * Dictionary-based stemmer.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Dimitar Popov
 */
final class DictStemmer extends Stemmer {
  /** Stem dictionary. */
  final StemDir dict;

  /**
   * Constructor.
   * @param d stem dictionary
   */
  DictStemmer(final StemDir d) {
    dict = d;
  }

  @Override
  Stemmer get(final Language l) {
    return new DictStemmer(dict);
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
