package org.basex.util.ft;

import static org.basex.util.Token.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.EnumSet;
import java.util.List;
import org.basex.core.Prop;
import org.basex.query.QueryException;
import org.basex.query.ft.FTOpt;
import org.basex.query.util.Err;

/**
 * Stemmer implementation using the WordNet stemmer.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Dimitar Popov
 */
class WordnetStemmer extends Stemmer {
  /** Name of the stemmer. */
  private static final byte[] NAME = token("WordNet");
  /** Path to the WordNet dictionary files; relative to current directory. */
  private static final String PATH = "etc/wndict";
  /** Name of the package of the WordNet stemmer. */
  private static final String PKG = "edu.mit.jwi";
  /** IDictionary class. */
  private static final Class<?> IDICT_CLASS;
  /** Dictionary class. */
  private static final Class<?> DICT_CLASS;
  /** SimpleStemmer class. */
  /** WordnetStemmer class. */
  private static final Class<?> WORDNET_CLASS;
  /** WordnetStemmer class. */
  private static final Constructor<?> WORDNET_CTR;
  /** WordnetStemmer.findStems method. */
  private static final Method WORDNET_FIND_STEMS;
  /** WordNet dictionary instance. */
  private static final Object DICT;

  static {
    IDICT_CLASS = findClass(PKG + ".IDictionary");
    // Don't try to find the other classes if Dictionary is not found:
    if(IDICT_CLASS == null) {
      DICT_CLASS = null;
      WORDNET_CLASS = null;
      WORDNET_FIND_STEMS = null;
      WORDNET_CTR = null;
      DICT = null;
    } else {
      DICT_CLASS = findClass(PKG + ".Dictionary");
      WORDNET_CLASS = findClass(PKG + ".morph.WordnetStemmer");
      WORDNET_CTR = findConstructor(WORDNET_CLASS, IDICT_CLASS);
      WORDNET_FIND_STEMS = findMethod(WORDNET_CLASS, "findStems", String.class);
      DICT = DICT_CLASS == null || WORDNET_CLASS == null || WORDNET_CTR == null
          || WORDNET_FIND_STEMS == null ? null : newDictionary();
    }
  }

  /**
   * Find a class by name.
   * @param name class name
   * @return {@code null} if the class is not found
   */
  private static Class<?> findClass(final String name) {
    try {
      return Class.forName(name);
    } catch(final Exception e) {
      return null;
    }
  }

  /**
   * Find a method by name and parameter types.
   * @param c class to search for the method
   * @param name method name
   * @param parameterTypes method parameters
   * @return {@code null} if the class is not found
   */
  private static Method findMethod(final Class<?> c, final String name,
      final Class<?>... parameterTypes) {
    if(c == null) return null;
    try {
      return c.getMethod(name, parameterTypes);
    } catch(final Exception e) {
      return null;
    }
  }

  /**
   * Find a constructor by parameter types.
   * @param c class to search for the constructor
   * @param parameterTypes constructor parameters
   * @return {@code null} if the class is not found
   */
  private static Constructor<?> findConstructor(final Class<?> c,
      final Class<?>... parameterTypes) {
    if(c == null) return null;
    try {
      return c.getConstructor(parameterTypes);
    } catch(final Exception e) {
      return null;
    }
  }

  /**
   * Create new instance of the WordNet dictionary.
   * @return new instance of the WordNet dictionary
   */
  private static Object newDictionary() {
    try {
      final Constructor<?> ctr = DICT_CLASS.getConstructor(URL.class);
      final Object dict = ctr.newInstance(new URL("file", null,
          PATH));
      DICT_CLASS.getMethod("open").invoke(dict);
      return dict;
    } catch(final Exception e) {
      return null;
    }
  }

  /**
   * Is the WordNet library available?
   * @return {@code true} if WordNet stemmer is available
   */
  static boolean isAvailable() {
    return DICT != null && WORDNET_CTR != null;
  }

  /** Instance of WordNet stemmer. */
  private final Object stemmer;

  /**
   * Construct a WordNet stemmer. If the WordNet library is not available, a
   * {@link RuntimeException} will be thrown. Call {@link #isAvailable()} first!
   * @param lang language of the text to stem
   * @throws QueryException if the language specified in the input properties is
   *           not supported by the stemmer
   */
  WordnetStemmer(final LanguageTokens lang) throws QueryException {
    if(!isAvailable()) throw new RuntimeException("WordNet is not available");

    if(!isLanguageSupported(lang.ln)) throw new QueryException(null, Err.FTLAN,
        lang);
    try {
      stemmer = WORDNET_CTR.newInstance(DICT);
    } catch(final Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  SpanProcessor newInstance(final Prop p, final FTOpt f) {
    try {
      return new WordnetStemmer(getLanguage(p, f));
    } catch(final QueryException e) {
      // [DP][JE] language is unsupported!
      e.printStackTrace();
      return null;
    }
  }

  @Override
  boolean isLanguageSupported(final byte[] lang) {
    return eq(lang, LanguageTokens.EN.ln);
  }

  @Override
  boolean isRepresentedByIdentifier(final byte[] id) {
    return id != null && eq(id, NAME);
  }

  @Override
  EnumSet<LanguageTokens> supportedLanguages() {
    return EnumSet.of(LanguageTokens.EN);
  }

  @Override
  int getPrecedence() {
    // [DP][JE] what would be an appropriate value?
    return 10;
  }

  @Override
  byte[] stem(final byte[] word) {
    return token(stem(string(word)));
  }

  // [DP][JE] the following methods should be available in all Stemmers:
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
      @SuppressWarnings("unchecked")
      final List<String> l = (List<String>) WORDNET_FIND_STEMS.invoke(stemmer,
          word);
      final String result = l.size() == 0 ? word : l.get(0);
      return result.length() == 0 ? word : result;
    } catch(final Exception e) {
      throw new RuntimeException(e);
    }
  }
}
