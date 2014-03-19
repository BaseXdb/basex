package org.basex.util.ft;

import static org.basex.util.Token.*;

import java.lang.reflect.*;
import java.util.*;

import org.basex.util.*;

/**
 * Stemmer implementation using the Lucene stemmer contributions.
 * The Lucene stemmers are based on the Apache License:
 * {@code http://lucene.apache.org/}.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
final class LuceneStemmer extends Stemmer {
  /** Name of the package with all Lucene stemmers. */
  private static final String PATTERN = "org.apache.lucene.analysis.%Stemmer";
  /** Stemmer classes which the Lucene library provides. */
  private static final HashMap<Language, StemmerClass> CLASSES = new HashMap<>();

  /** Stemmer class corresponding to the required properties. */
  private StemmerClass clazz;
  /** Stemmer instance. */
  private Object stemmer;

  static {
    if(Reflect.available(PATTERN, "de.German")) {
      add(Language.get("cs"), "cz.Czech");
      add(Language.get("es"), "es.SpanishLight");
      add(Language.get("fi"), "fi.FinnishLight");
      add(Language.get("hu"), "hu.HungarianLight");
      add(Language.get("it"), "it.ItalianLight");
      add(Language.get("pt"), "br.Brazilian");
      add(Language.get("sv"), "sv.SwedishLight");
      add("ar", "bg", "de", "fr", "hi", "lv", "nl", "ru");
    }
  }

  /**
   * Check if a stemmer class is available, and add it the the list of stemmers.
   * @param lang language
   */
  private static void add(final String... lang) {
    for(final String ln : lang) {
      final Language l = Language.get(ln);
      if(l != null) add(l, l.code() + '.' + l);
    }
  }

  /**
   * Check if a stemmer class is available, and add it the the list of stemmers.
   * @param lang language
   * @param name name of language
   */
  private static void add(final Language lang, final String name) {
    final Class<?> clz = Reflect.find(PATTERN, name);
    if(clz == null) {
      Util.debug("Could not initialize \"%\" Lucene stemmer class.", lang);
      return;
    }
    Method m = Reflect.method(clz, "stem", String.class);
    final boolean ch = m == null;
    if(ch) m = Reflect.method(clz, "stem", char[].class, int.class);
    if(m == null) {
      Util.debug("Could not initialize \"%\" Lucene stemmer method.", lang);
    } else {
      CLASSES.put(lang, new StemmerClass(clz, m, ch));
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
  LuceneStemmer() {
  }

  /**
   * Constructs a stemmer instance. Call {@link #available()} first to
   * check if the library is available.
   * @param lang language of the text to stem
   * @param fti full-text iterator
   */
  private LuceneStemmer(final Language lang, final FTIterator fti) {
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
  protected byte prec() {
    return 5;
  }

  @Override
  protected byte[] stem(final byte[] word) {
    String s = string(word);
    if(clazz.chars) {
      final char[] ch = s.toCharArray();
      final int cl = s.length();
      final int nl = (Integer) Reflect.invoke(clazz.stem, stemmer, ch, cl);
      s = new String(ch, 0, nl);
    } else {
      s = (String) Reflect.invoke(clazz.stem, stemmer, s);
    }
    return s == null ? word : token(s);
  }

  /** Structure, containing stemming methods. */
  private static class StemmerClass {
    /** Class implementing the stemmer. */
    final Class<?> clz;
    /** Method {@code stem}. */
    final Method stem;
    /** String indicator. */
    final boolean chars;

    /**
     * Constructor.
     * @param sc class implementing the stemmer
     * @param stm method {@code stem}
     * @param ch indicator for stemming via character array
     */
    StemmerClass(final Class<?> sc, final Method stm, final boolean ch) {
      clz = sc;
      stem = stm;
      chars = ch;
      stem.setAccessible(true);
    }
  }
}
