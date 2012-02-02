package org.basex.util.ft;

import static org.basex.util.Token.*;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;

import org.basex.util.Reflect;
import org.basex.util.Util;

/**
 * Stemmer implementation using the Snowball stemmer.
 * The Snowball stemmers were written by Dr Martin Porter and Richard Boulton
 * and is based on the BSD License: {@code http://snowball.tartarus.org/}.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Dimitar Popov
 */
final class SnowballStemmer extends Stemmer {
  /** Name of the package with all Snowball stemmers. */
  private static final String PATTERN = "org.tartarus.snowball.ext.%Stemmer";
  /** Stemmer classes which the Snowball library provides. */
  private static final HashMap<Language, StemmerClass> CLASSES =
      new HashMap<Language, StemmerClass>();

  /** Stemmer class corresponding to the required properties. */
  private StemmerClass clazz;
  /** Stemmer instance. */
  private Object stemmer;

  static {
    if(Reflect.available(PATTERN, "German")) {
      for(final Language l : Language.ALL.values()) {
        final Class<?> clz = Reflect.find(PATTERN, l);
        if(clz == null) continue;
        final Method m1 = Reflect.method(clz, "setCurrent", String.class);
        final Method m2 = Reflect.method(clz, "stem");
        final Method m3 = Reflect.method(clz, "getCurrent");
        if(m1 == null || m2 == null || m3 == null) {
          Util.debug("Could not initialize \"%\" Snowball stemmer.", l);
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
    return CLASSES.size() > 0;
  }

  /** Empty constructor. */
  SnowballStemmer() {
    super();
  }

  /**
   * Constructs a Snowball stemmer. Call {@link #available()} first to
   * check if the library is available.
   * @param fti full-text iterator
   * @param lang language of the text to stem
   */
  private SnowballStemmer(final Language lang, final FTIterator fti) {
    super(fti);
    clazz = CLASSES.get(lang);
    stemmer = Reflect.get(clazz.clz);
  }

  @Override
  Stemmer get(final Language l, final FTIterator fti) {
    return new SnowballStemmer(l, fti);
  }

  @Override
  public boolean supports(final Language lang) {
    return CLASSES.containsKey(lang);
  }

  @Override
  protected byte prec() {
    return 2;
  }

  @Override
  Collection<Language> languages() {
    return CLASSES.keySet();
  }

  @Override
  protected byte[] stem(final byte[] word) {
    Reflect.invoke(clazz.setCurrent, stemmer, string(word));
    Reflect.invoke(clazz.stem, stemmer);
    final String s = (String) Reflect.invoke(clazz.getCurrent, stemmer);
    return s == null ? word : token(s);
  }

  /** Structure, containing stemming methods. */
  private static class StemmerClass {
    /** Class implementing the stemmer. */
    final Class<?> clz;
    /** Method {@code setCurrent}. */
    final Method setCurrent;
    /** Method {@code stem}. */
    final Method stem;
    /** Method {@code getCurrent}. */
    final Method getCurrent;

    /**
     * Constructor.
     * @param sc class implementing the stemmer
     * @param s method {@code setCurrent}
     * @param stm method {@code stem}
     * @param g method {@code getCurrent}
     */
    StemmerClass(final Class<?> sc, final Method s, final Method stm,
        final Method g) {
      clz = sc;
      setCurrent = s;
      stem = stm;
      getCurrent = g;
    }
  }
}
