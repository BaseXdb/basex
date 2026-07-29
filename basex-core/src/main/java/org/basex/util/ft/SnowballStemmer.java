package org.basex.util.ft;

import static org.basex.util.Token.*;

import java.lang.reflect.*;
import java.util.*;

import org.basex.util.*;

/**
 * Stemmer implementation using the Snowball stemmer.
 * The Snowball stemmers were written by Dr Martin Porter and Richard Boulton
 * and is based on the BSD License: {@code http://snowball.tartarus.org/}.
 *
 * @author BaseX Team, BSD License
 * @author Dimitar Popov
 */
final class SnowballStemmer extends ExternalStemmer<SnowballStemmer.StemmerClass> {
  /** Name of the package with all Snowball stemmers. */
  private static final String PATTERN = "org.tartarus.snowball.ext.%Stemmer";
  /** Stemmer classes which the Snowball library provides. */
  private static final HashMap<Language, StemmerClass> CLASSES = new HashMap<>();

  static {
    if(Reflect.available(PATTERN, "German")) {
      for(final Language l : Language.ALL.values()) {
        final Class<?> clz = Reflect.find(PATTERN, l);
        if(clz == null) continue;
        final Method m1 = Reflect.method(clz, "setCurrent", String.class);
        final Method m2 = Reflect.method(clz, "stem");
        final Method m3 = Reflect.method(clz, "getCurrent");
        if(m1 == null || m2 == null || m3 == null) {
          Util.debugln("Could not initialize \"%\" Snowball stemmer.", l);
        } else {
          CLASSES.put(l, new StemmerClass(clz, m1, m2, m3));
        }
      }
    }
  }

  /**
   * Checks if the library is available.
   * @return result of check
   */
  static boolean available() {
    return !CLASSES.isEmpty();
  }

  /** Empty constructor. */
  SnowballStemmer() {
  }

  /**
   * Constructs a Snowball stemmer. Call {@link #available()} first to
   * check if the library is available.
   * @param fti full-text iterator
   * @param lang language of the text to stem
   */
  private SnowballStemmer(final Language lang, final FTIterator fti) {
    super(lang, fti);
  }

  @Override
  Stemmer get(final Language lang, final FTIterator fti) {
    return new SnowballStemmer(lang, fti);
  }

  @Override
  Map<Language, StemmerClass> classes() {
    return CLASSES;
  }

  @Override
  protected byte prec() {
    return 2;
  }

  @Override
  protected byte[] stem(final byte[] word) {
    Reflect.invoke(clazz.setCurrent, stemmer, string(word));
    Reflect.invoke(clazz.stem, stemmer);
    final String s = (String) Reflect.invoke(clazz.getCurrent, stemmer);
    return s == null ? word : token(s);
  }

  /**
   * Methods of a particular stemmer.
   * @param clz        Class implementing the stemmer.
   * @param setCurrent Method {@code setCurrent}.
   * @param stem       Method {@code stem}.
   * @param getCurrent Method {@code getCurrent}.
   */
  record StemmerClass(Class<?> clz, Method setCurrent, Method stem, Method getCurrent)
      implements ExternalStemmer.StemmerClass { }
}
