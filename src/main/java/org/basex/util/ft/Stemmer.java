package org.basex.util.ft;

import java.util.Iterator;
import org.basex.core.Prop;
import org.basex.query.ft.FTOpt;
import org.basex.util.Util;

/**
 * Implementation of common stemmer methods.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Dimitar Popov
 */
abstract class Stemmer extends SpanProcessor {
  /**
   * Stem a word.
   * @param word input word to stem
   * @return the stem of the word
   */
  abstract byte[] stem(final byte[] word);

  @Override
  abstract SpanProcessor get(final Prop p, final FTOpt f);

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
        s.txt = stem(s.txt);
        return s;
      };

      @Override
      public void remove() {
        Util.notimplemented();
      }
    };
  }

  @Override
  SPType type() {
    return SPType.stemmer;
  }

  /**
   * Decide what language is required from {@link Prop} and {@link FTOpt}.
   * @param p {@link Prop}
   * @param f {@link FTOpt}
   * @return language
   */
  protected static Language getLanguage(final Prop p, final FTOpt f) {
    try {
      if(f != null && f.ln != null) {
        return Language.valueOf(f.ln);
      } else if(p != null && p.get(Prop.FTLANGUAGE).length() > 0) {
        return Language.valueOf(p.get(Prop.FTLANGUAGE).toUpperCase());
      } else {
        return Language.DEFAULT;
      }
    } catch(final IllegalArgumentException e) {
      // [DP][JE] invalid language supplied!
      return null;
    }
  }
}
