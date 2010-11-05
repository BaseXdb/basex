package org.basex.util.ft;

import static org.basex.util.Token.*;
import static org.basex.util.ft.Language.*;
import java.lang.reflect.Method;
import java.util.EnumMap;
import java.util.EnumSet;
import org.basex.core.Prop;
import org.basex.query.QueryException;
import org.basex.query.ft.FTOpt;
import org.basex.query.util.Err;
import org.basex.util.Token;

/**
 * Stemmer implementation using the Snowball stemmer
 * The Snowball stemmers were written by Dr Martin Porter and Richard Boulton
 * and is based on the BSD License: {@code http://snowball.tartarus.org/}).
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Dimitar Popov
 */
final class SnowballStemmer extends Stemmer {
  /** Name of the stemmer. */
  private static final byte[] NAME = token("Snowball");
  /** Name of the package with all Snowball stemmers. */
  private static final String PKG = "org.tartarus.snowball";
  /** Stemmer classes which the Snowball library provides. */
  private static final EnumMap<Language, StemmerClass> CLASSES =
      new EnumMap<Language, StemmerClass>(Language.class);

  /** Stemmer class corresponding to the required properties. */
  private final StemmerClass clazz;
  /** Stemmer instance. */
  private final Object stemmer;

  static {
    try {
      // if the base class cannot be loaded, then we shouldn't try to load any:
      Class.forName(PKG + ".SnowballStemmer");
      add(CLASSES, DA);
      add(CLASSES, DE);
      add(CLASSES, EN);
      add(CLASSES, ES);
      add(CLASSES, FI);
      add(CLASSES, FR);
      add(CLASSES, HU);
      add(CLASSES, IT);
      add(CLASSES, NL);
      add(CLASSES, NO);
      add(CLASSES, PT);
      add(CLASSES, RO);
      add(CLASSES, RU);
      add(CLASSES, SV);
      add(CLASSES, TR);
    } catch(final ClassNotFoundException e) {
    }
  }

  /**
   * Check if a Snowball stemmer class is available, and add it the the list of
   * stemmers.
   * @param stemmers a list of available Snowball stemmers
   * @param lang language
   */
  @SuppressWarnings("unchecked")
  private static void add(
      final EnumMap<Language, StemmerClass> stemmers, final Language lang) {

    try {
      final String clz = lang.toString().toLowerCase() + "Stemmer";
      final Class<Stemmer> c = (Class<Stemmer>)
        Class.forName(PKG + ".ext." + clz);
      stemmers.put(lang,
          new StemmerClass(c, c.getMethod("setCurrent", String.class),
              c.getMethod("stem"), c.getMethod("getCurrent")));
    } catch(final Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * Checks if the library is available.
   * @return result of check
   */
  static boolean available() {
    return CLASSES.size() > 0;
  }

  /**
   * Constructs a Snowball stemmer. Call {@link #available()} first to
   * check if the library is available.
   * @param lang language of the text to stem
   * @throws QueryException if the specified language is not supported
   */
  SnowballStemmer(final Language lang) throws QueryException {
    clazz = CLASSES.get(lang);
    if(clazz == null) Err.FTLAN.thrw(null, lang);
    try {
      stemmer = clazz.clazz.newInstance();
    } catch(final Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  SpanProcessor get(final Prop p, final FTOpt f) {
    try {
      return new SnowballStemmer(getLanguage(p, f));
    } catch(final QueryException e) {
      // [DP][JE] language is unsupported!
      e.printStackTrace();
      return null;
    }
  }

  @Override
  boolean supports(final byte[] lang) {
    return CLASSES.containsKey(Language.valueOf(lang));
  }

  @Override
  boolean eq(final byte[] id) {
    return id != null && Token.eq(id, NAME);
  }

  @Override
  int prec() {
    // [DP][JE] what would be an appropriate value?
    return 100;
  }

  @Override
  EnumSet<Language> languages() {
    return EnumSet.copyOf(CLASSES.keySet());
  }

  @Override
  byte[] stem(final byte[] word) {
    return token(stem(string(word)));
  }

  // [DP][JE] the following methods should be available in all Stemmers:
  // ...used at all?

  /**
   * Get the stemmer name.
   * @return the stemmer name
   */
  byte[] getStemmerName() {
    return NAME;
  }

  /**
   * Stem a word.
   * @param word input word to stem
   * @return the stem of the word
   */
  private String stem(final String word) {
    try {
      clazz.setCurrent.invoke(stemmer, word);
      clazz.stem.invoke(stemmer);
      return (String) clazz.getCurrent.invoke(stemmer);
    } catch(final Exception e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Structure to store stemming methods of a Snowball stemmer.
   */
  private static class StemmerClass {
    /** Class implementing {@link SnowballStemmer}. */
    final Class<Stemmer> clazz;
    /** Method {@code setCurrent}. */
    final Method setCurrent;
    /** Method {@code stem}. */
    final Method stem;
    /** Method {@code getCurrent}. */
    final Method getCurrent;

    /**
     * Constructor.
     * @param sc class implementing {@link SnowballStemmer}
     * @param s method {@code setCurrent}
     * @param stm method {@code stem}
     * @param g method {@code getCurrent}
     */
    StemmerClass(final Class<Stemmer> sc, final Method s,
        final Method stm, final Method g) {
      clazz = sc;
      setCurrent = s;
      stem = stm;
      getCurrent = g;
    }
  }
}
