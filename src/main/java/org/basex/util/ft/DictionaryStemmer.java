package org.basex.util.ft;

import java.util.EnumSet;
import java.util.Iterator;
import org.basex.core.Prop;
import org.basex.query.ft.FTOpt;
import org.basex.query.ft.StemDir;
import org.basex.util.Util;

/**
 * Dictionary based stemmer.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Dimitar Popov
 */
final class DictionaryStemmer extends SpanProcessor {
  /** Stem dictionary. */
  final StemDir dictionary;

  /**
   * Constructor.
   * @param dict stem dictionary
   */
  DictionaryStemmer(final StemDir dict) {
    dictionary = dict;
  }

  @Override
  SpanProcessor newInstance(final Prop p, final FTOpt f) {
    return new DictionaryStemmer(f.sd);
  }

  @Override
  SPType getType() {
    return SPType.stemmer;
  }

  @Override
  int getPrecedence() {
    return 1001;
  }

  @Override
  EnumSet<LanguageTokens> supportedLanguages() {
    // [DP][JE] the user should supply the language of the dictionary
    return EnumSet.allOf(LanguageTokens.class);
  }

  @Override
  Iterator<Span> process(final Iterator<Span> iterator) {
    return new Iterator<Span>() {
      @Override
      public boolean hasNext() {
        return iterator.hasNext();
      }

      @Override
      public Span next() {
        final Span s = iterator.next();
        if (dictionary != null) s.txt = dictionary.stem(s.txt);
        return s;
      }

      @Override
      public void remove() {
        Util.notimplemented();
      }
    };
  }
}
