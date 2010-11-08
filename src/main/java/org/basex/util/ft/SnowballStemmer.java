package org.basex.util.ft;

import static org.basex.util.Token.*;
import static org.basex.util.ft.Language.*;
import java.lang.reflect.Method;
import java.util.EnumMap;
import java.util.EnumSet;
import org.basex.util.Util;

/**
 * Stemmer implementation using the Snowball stemmer.
 * The Snowball stemmers were written by Dr Martin Porter and Richard Boulton
 * and is based on the BSD License: {@code http://snowball.tartarus.org/}.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Dimitar Popov
 */
final class SnowballStemmer extends Stemmer {
  /** Name of the package with all Snowball stemmers. */
  private static final String PKG = "org.tartarus.snowball";
  /** Stemmer classes which the Snowball library provides. */
  private static final EnumMap<Language, StemmerClass> CLASSES =
      new EnumMap<Language, StemmerClass>(Language.class);

  /** Stemmer class corresponding to the required properties. */
  private StemmerClass clazz;
  /** Stemmer instance. */
  private Object stemmer;

  static {
    try {
      add(DA); add(DE); add(EN); add(ES); add(FI); add(FR); add(HU); add(IT);
      add(NL); add(NO); add(PT); add(RO); add(RU); add(SV); add(TR);
    } catch(final Exception e) {
      // class paths were not found
    }
  }

  /**
   * Check if a stemmer class is available, and add it the the list of stemmers.
   * @param lang language
   * @throws Exception exception
   */
  private static void add(final Language lang) throws Exception {
    final Class<?> c = Class.forName(
        PKG + ".ext." + lang.toString().toLowerCase() + "Stemmer");
    CLASSES.put(lang, new StemmerClass(c,
        findMethod(c, "setCurrent", String.class),
        findMethod(c, "stem"), findMethod(c, "getCurrent")));
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
    super(null);
  }

  /**
   * Constructs a Snowball stemmer. Call {@link #available()} first to
   * check if the library is available.
   * @param fti full-text iterator
   * @param lang language of the text to stem
   */
  SnowballStemmer(final Language lang, final FTIterator fti) {
    super(fti);
    clazz = CLASSES.get(lang);
    try {
      stemmer = clazz.clz.newInstance();
    } catch(final Exception ex) {
      System.err.println(ex.getMessage());
      Util.notexpected(ex);
    }
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
  int prec() {
    return 100;
  }

  @Override
  EnumSet<Language> languages() {
    return EnumSet.copyOf(CLASSES.keySet());
  }

  @Override
  byte[] stem(final byte[] word) {
    try {
      clazz.setCurrent.invoke(stemmer, string(word));
      clazz.stem.invoke(stemmer);
      return token((String) clazz.getCurrent.invoke(stemmer));
    } catch(final Exception e) {
      throw new RuntimeException(e);
    }
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
