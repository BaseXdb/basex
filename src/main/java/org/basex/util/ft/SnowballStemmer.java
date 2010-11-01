package org.basex.util.ft;

import static org.basex.util.Token.*;
import static org.basex.util.ft.LanguageTokens.*;
import java.lang.reflect.Method;
import java.util.EnumMap;
import java.util.EnumSet;
import org.basex.core.Prop;
import org.basex.query.QueryException;
import org.basex.query.ft.FTOpt;
import org.basex.query.util.Err;

/**
 * Stemmer implementation using the Snowball stemmer.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Dimitar Popov
 */
final class SnowballStemmer extends Stemmer {
  /** Name of the stemmer. */
  private static final byte[] NAME = token("Snowball");
  /** Name of the package with all Snowball stemmers. */
  private static final String PKG = "org.tartarus.snowball.ext";
  /** Stemmer classes which the Snowball library provides. */
  private static final EnumMap<LanguageTokens, StemmerClass> CLASSES =
      new EnumMap<LanguageTokens, StemmerClass>(LanguageTokens.class);

  /** Stemmer class corresponding to the required properties. */
  private final StemmerClass stemmerClass;
  /** Stemmer instance. */
  private final Object stemmer;

  static {
    try {
      // if the base class cannot be loaded, then we shouldn't try to load any:
      Class.forName("org.tartarus.snowball.SnowballStemmer");
      add(CLASSES, DA, "danishStemmer");
      add(CLASSES, DE, "germanStemmer");
      add(CLASSES, EN, "englishStemmer");
      add(CLASSES, ES, "spanishStemmer");
      add(CLASSES, FI, "finnishStemmer");
      add(CLASSES, FR, "frenchStemmer");
      add(CLASSES, HU, "hungarianStemmer");
      add(CLASSES, IT, "italianStemmer");
      add(CLASSES, NL, "dutchStemmer");
      add(CLASSES, NO, "norwegianStemmer");
      add(CLASSES, PT, "portugueseStemmer");
      add(CLASSES, RO, "romanianStemmer");
      add(CLASSES, RU, "russianStemmer");
      add(CLASSES, SV, "swedishStemmer");
      add(CLASSES, TR, "turkishStemmer");
    } catch(final ClassNotFoundException e) {
      ;
    }
  }

  /**
   * Check if a Snowball stemmer class is available, and add it the the list of
   * stemmers.
   * @param stemmers a list of available Snowball stemmers
   * @param lang language
   * @param className Snowball stemmer class name
   */
  @SuppressWarnings({"unchecked"})
  private static void add(
      final EnumMap<LanguageTokens, StemmerClass> stemmers,
      final LanguageTokens lang, final String className) {

    try {
      final Class<Stemmer> c = (Class<Stemmer>)
        Class.forName(PKG + "." + className);
      stemmers.put(lang,
          new StemmerClass(c, c.getMethod("setCurrent", String.class),
              c.getMethod("stem"), c.getMethod("getCurrent")));
    } catch(final Exception e) {
      ;
    }
  }

  /**
   * Is the Snowball library available?
   * @return {@code true} if Snowball stemmers are available
   */
  static boolean isAvailable() {
    return CLASSES.size() > 0;
  }

  /**
   * Construct a Snowball stemmer. If the Snowball library is not available, a
   * {@link RuntimeException} will be thrown. Call {@link #isAvailable()}
   * first!
   * @param lang language of the text to stem
   * @throws QueryException if the language specified in the input properties is
   *           not supported by the stemmer
   */
  SnowballStemmer(final LanguageTokens lang) throws QueryException {
    if(!isAvailable()) {
      throw new RuntimeException("Snowball is not available");
    }

    stemmerClass = CLASSES.get(lang);
    if(stemmerClass == null) {
      // [DP][JE] what should we do in case of an unsupported language?
      throw new QueryException(null, Err.FTLAN, lang);
    }

    try {
      stemmer = stemmerClass.stemmerClass.newInstance();
    } catch(final Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  SpanProcessor newInstance(final Prop p, final FTOpt f) {
    try {
      return new SnowballStemmer(getLanguage(p, f));
    } catch(final QueryException e) {
      // [DP][JE] language is unsupported!
      e.printStackTrace();
      return null;
    }
  }

  @Override
  boolean isLanguageSupported(final byte[] lang) {
    return CLASSES.containsKey(LanguageTokens.valueOf(lang));
  }

  @Override
  boolean isRepresentedByIdentifier(final byte[] id) {
    return id != null && eq(id, NAME);
  }

  @Override
  int getPrecedence() {
    // [DP][JE] what would be an appropriate value?
    return 100;
  }

  @Override
  EnumSet<LanguageTokens> supportedLanguages() {
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
      stemmerClass.setCurrent.invoke(stemmer, word);
      stemmerClass.stem.invoke(stemmer);
      return (String) stemmerClass.getCurrent.invoke(stemmer);

    } catch(final Exception e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Structure to store stemming methods of a Snowball stemmer.
   */
  private static class StemmerClass {
    /** Class implementing {@link SnowballStemmer}. */
    final Class<Stemmer> stemmerClass;
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
      stemmerClass = sc;
      setCurrent = s;
      stem = stm;
      getCurrent = g;
    }
  }
}
