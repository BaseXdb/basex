package org.basex.util.ft;

import static org.basex.util.Token.*;
import static org.basex.util.ft.Language.*;
import java.lang.reflect.Method;
import java.util.EnumMap;
import java.util.EnumSet;
import org.basex.util.Reflect;

/**
 * Stemmer implementation using the Lucene stemmer contributions.
 * The Lucene stemmers are based on the Apache License:
 * {@code http://lucene.apache.org/}.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
final class LuceneStemmer extends Stemmer {
  /** Name of the package with all Snowball stemmers. */
  private static final String PKG = "org.apache.lucene.analysis";
  /** Stemmer classes which the Snowball library provides. */
  private static final EnumMap<Language, StemmerClass> CLASSES =
      new EnumMap<Language, StemmerClass>(Language.class);

  /** Stemmer class corresponding to the required properties. */
  private StemmerClass clazz;
  /** Stemmer instance. */
  private Object stemmer;

  static {
    if(Reflect.available(PKG)) {
      add(PT, "br.Brazilian"); add(DE); add(FR); add(NL); add(RU);
    }
  }

  /**
   * Check if a stemmer class is available, and add it the the list of stemmers.
   * @param lang language
   */
  private static void add(final Language lang) {
    add(lang, lang.name().toLowerCase() + '.' + lang);
  }

  /**
   * Check if a stemmer class is available, and add it the the list of stemmers.
   * @param lang language
   * @param path class path
   */
  private static void add(final Language lang, final String path) {
    final Class<?> clz = Reflect.find(PKG + '.' + path + "Stemmer");
    final Method m = Reflect.find(clz, "stem", String.class);
    CLASSES.put(lang, new StemmerClass(clz, m));
  }

  /**
   * Checks if the library is available.
   * @return result of check
   */
  static boolean available() {
    return CLASSES.size() > 0;
  }

  /** Empty constructor. */
  LuceneStemmer() {
    super(null);
  }

  /**
   * Constructs a stemmer instance. Call {@link #available()} first to
   * check if the library is available.
   * @param lang language of the text to stem
   * @param fti full-text iterator
   */
  LuceneStemmer(final Language lang, final FTIterator fti) {
    super(fti);
    clazz = CLASSES.get(lang);
    stemmer = Reflect.get(clazz.clz);
  }

  @Override
  Stemmer get(final Language l, final FTIterator fti) {
    return new LuceneStemmer(l, fti);
  }

  @Override
  public boolean supports(final Language lang) {
    return CLASSES.containsKey(lang);
  }

  @Override
  int prec() {
    return 200;
  }

  @Override
  EnumSet<Language> languages() {
    return EnumSet.copyOf(CLASSES.keySet());
  }

  @Override
  byte[] stem(final byte[] word) {
    return token((String) Reflect.invoke(clazz.stem, stemmer, string(word)));
  }

  /** Structure, containing stemming methods. */
  private static class StemmerClass {
    /** Class implementing the stemmer. */
    final Class<?> clz;
    /** Method {@code stem}. */
    final Method stem;

    /**
     * Constructor.
     * @param sc class implementing the stemmer
     * @param stm method {@code stem}
     */
    StemmerClass(final Class<?> sc, final Method stm) {
      clz = sc;
      stem = stm;
      stem.setAccessible(true);
    }
  }
}
