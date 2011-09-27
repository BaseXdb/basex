package org.basex.util.ft;

import static org.basex.util.Token.*;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;

import org.basex.util.Reflect;
import org.basex.util.Util;

/**
 * Stemmer implementation using the Lucene stemmer contributions.
 * The Lucene stemmers are based on the Apache License:
 * {@code http://lucene.apache.org/}.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
final class LuceneStemmer extends Stemmer {
  /** Name of the package with all Lucene stemmers. */
  private static final String PATTERN = "org.apache.lucene.analysis.%Stemmer";
  /** Stemmer classes which the Lucene library provides. */
  private static final HashMap<Language, StemmerClass> CLASSES =
      new HashMap<Language, StemmerClass>();

  /** Stemmer class corresponding to the required properties. */
  private StemmerClass clazz;
  /** Stemmer instance. */
  private Object stemmer;

  static {
    final String br = "br.Brazilian";
    if(Reflect.avl(Util.info(PATTERN, br))) {
      add("pt", br);
      add("de", "fr", "nl", "ru");
    }
  }

  /**
   * Check if a stemmer class is available, and add it the the list of stemmers.
   * @param lang language
   */
  private static void add(final String... lang) {
    for(final String ln : lang) {
      final Language l = Language.get(ln);
      add(ln, l.code() + '.' + l);
    }
  }

  /**
   * Check if a stemmer class is available, and add it the the list of stemmers.
   * @param lang language
   * @param path class path
   */
  private static void add(final String lang, final String path) {
    final Class<?> clz = Reflect.find(PATTERN, path);
    final Method m = Reflect.method(clz, "stem", String.class);
    CLASSES.put(Language.get(lang), new StemmerClass(clz, m));
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
    super();
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
  Collection<Language> languages() {
    return CLASSES.keySet();
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
