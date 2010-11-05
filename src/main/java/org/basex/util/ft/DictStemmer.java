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
final class DictStemmer extends SpanProcessor {
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
  SpanProcessor get(final Prop p, final FTOpt f) {
    return new DictStemmer(f.sd);
  }

  @Override
  SPType type() {
    return SPType.stemmer;
  }

  @Override
  int prec() {
    return 1001;
  }

  @Override
  EnumSet<Language> languages() {
    // [DP][JE] the user should supply the language of the dictionary
    return EnumSet.allOf(Language.class);
  }

  @Override
  Iterator<Span> process(final Iterator<Span> iter) {
    return new Iterator<Span>() {
      @Override
      public boolean hasNext() {
        return iter.hasNext();
      }

      @Override
      public Span next() {
        final Span s = iter.next();
        if(dict != null) s.txt = dict.stem(s.txt);
        return s;
      }

      @Override
      public void remove() {
        Util.notimplemented();
      }
    };
  }
}
