package org.basex.util.ft;

import static org.basex.util.Token.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.EnumSet;
import java.util.List;

/**
 * Stemmer implementation using the WordNet stemmer.
 * The WordNet stemmer is developed by George A. Miller and is based on
 * the WordNet 3.0 License: {@code http://wordnet.princeton.edu/}.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Dimitar Popov
 */
final class WordnetStemmer extends Stemmer {
  /** Path to the WordNet dictionary files; relative to current directory. */
  private static final String PATH = "etc/wndict";
  /** Name of the package of the WordNet stemmer. */
  private static final String PKG = "edu.mit.jwi";
  /** IDictionary class. */
  private static final Class<?> IDICT_CLASS;
  /** Dictionary class. */
  private static final Class<?> DICT_CLASS;
  /** WordnetStemmer class. */
  private static final Class<?> WORDNET_CLASS;
  /** WordnetStemmer class. */
  private static final Constructor<?> CTR;
  /** WordnetStemmer.findStems method. */
  private static final Method FIND_STEMS;
  /** WordNet dictionary instance. */
  private static final Object DICT;

  static {
    IDICT_CLASS = findClass(PKG + ".IDictionary");

    // Don't try to find the other classes if Dictionary is not found:
    if(IDICT_CLASS == null) {
      DICT_CLASS = null;
      WORDNET_CLASS = null;
      FIND_STEMS = null;
      CTR = null;
      DICT = null;
    } else {
      DICT_CLASS = findClass(PKG + ".Dictionary");
      WORDNET_CLASS = findClass(PKG + ".morph.WordnetStemmer");
      CTR = findConstructor(WORDNET_CLASS, IDICT_CLASS);
      FIND_STEMS = findMethod(WORDNET_CLASS, "findStems", String.class);
      DICT = DICT_CLASS == null || WORDNET_CLASS == null || CTR == null ||
        FIND_STEMS == null ? null : newDict();
    }
  }

  /**
   * Create new instance of the WordNet dictionary.
   * @return new instance of the WordNet dictionary
   */
  private static Object newDict() {
    try {
      final Constructor<?> ctr = DICT_CLASS.getConstructor(URL.class);
      final Object dict = ctr.newInstance(new URL("file", null, PATH));
      DICT_CLASS.getMethod("open").invoke(dict);
      return dict;
    } catch(final Exception e) {
      return null;
    }
  }

  /**
   * Checks if the library is available.
   * @return result of check
   */
  static boolean available() {
    return DICT != null && CTR != null;
  }

  /** Singleton instance. */
  private static WordnetStemmer instance;
  /** Instance of WordNet stemmer. */
  private final Object stemmer;

  /**
   * Returns a singleton instance of the stemmer.
   * @return instance
   */
  static Stemmer get() {
    if(instance == null) instance = new WordnetStemmer();
    return instance;
  }

  /**
   * Constructs a WordNet stemmer. Call {@link #available()} first to
   * check if the library is available.
   */
  private WordnetStemmer() {
    try {
      stemmer = CTR.newInstance(DICT);
    } catch(final Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  Stemmer get(final Language l) {
    return get();
  }

  @Override
  public boolean supports(final Language lang) {
    return lang == Language.EN;
  }

  @Override
  EnumSet<Language> languages() {
    return EnumSet.of(Language.EN);
  }

  @Override
  int prec() {
    return 10;
  }

  @Override
  byte[] stem(final byte[] word) {
    try {
      @SuppressWarnings("unchecked")
      final List<String> l = (List<String>)
        FIND_STEMS.invoke(stemmer, string(word));
      final byte[] result = l.size() == 0 ? word : token(l.get(0));
      return result.length == 0 ? word : result;
    } catch(final Exception e) {
      throw new RuntimeException(e);
    }
  }
}
