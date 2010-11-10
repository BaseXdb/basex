package org.basex.util.ft;

import static org.basex.util.Token.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.EnumSet;
import java.util.List;

import org.basex.util.Reflect;

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
    // Don't try to find the other classes if Dictionary is not found:
    if(Reflect.available(PKG)) {
      IDICT_CLASS = null;
      DICT_CLASS = null;
      WORDNET_CLASS = null;
      FIND_STEMS = null;
      CTR = null;
      DICT = null;
    } else {
      IDICT_CLASS = Reflect.find(PKG + ".IDictionary");
      DICT_CLASS = Reflect.find(PKG + ".Dictionary");
      WORDNET_CLASS = Reflect.find(PKG + ".morph.WordnetStemmer");
      CTR = Reflect.find(WORDNET_CLASS, IDICT_CLASS);
      FIND_STEMS = Reflect.find(WORDNET_CLASS, "findStems", String.class);
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
      final Constructor<?> ctr = Reflect.find(DICT_CLASS, URL.class);
      final Object dict = Reflect.get(ctr, new URL("file", null, PATH));
      return Reflect.invoke(Reflect.find(DICT_CLASS, "open"), dict);
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

  /** Instance of WordNet stemmer. */
  private Object stemmer;

  /** Empty constructor. */
  WordnetStemmer() {
    super(null);
  }

  /**
   * Constructs a WordNet stemmer. Call {@link #available()} first to
   * check if the library is available.
   * @param fti full-text iterator
   */
  WordnetStemmer(final FTIterator fti) {
    super(fti);
    stemmer = Reflect.get(CTR, DICT);
  }

  @Override
  Stemmer get(final Language l, final FTIterator fti) {
    return new WordnetStemmer(fti);
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
    @SuppressWarnings("unchecked")
    final List<String> l = (List<String>)
      Reflect.invoke(FIND_STEMS, stemmer, string(word));
    final byte[] result = l.size() == 0 ? word : token(l.get(0));
    return result.length == 0 ? word : result;
  }
}
