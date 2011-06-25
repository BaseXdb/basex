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
 * @author BaseX Team 2005-11, BSD License
 * @author Dimitar Popov
 */
final class WordnetStemmer extends Stemmer {
  /** Name of the package of the WordNet stemmer. */
  private static final String PKG = "edu.mit.jwi";
  /** Path to the WordNet dictionary files (currently: static). */
  private static final String PATH = "etc/wndict";
  /** WordnetStemmer class. */
  private static final Constructor<?> CTR;
  /** WordnetStemmer.findStems method. */
  private static final Method FIND_STEMS;
  /** WordNet dictionary instance. */
  private static final Object DICT;

  static {
    // don't try to find the other classes if Dictionary is not found:
    if(Reflect.available(PKG)) {
      FIND_STEMS = null;
      CTR = null;
      DICT = null;
    } else {
      final Class<?> dict = Reflect.find(PKG + ".Dictionary");
      final Class<?> wn = Reflect.find(PKG + ".morph.WordnetStemmer");
      CTR = Reflect.find(wn, Reflect.find(PKG + ".IDictionary"));
      FIND_STEMS = Reflect.find(wn, "findStems", String.class);
      DICT = newDict(dict);
    }
  }

  /**
   * Create new instance of the WordNet dictionary.
   * @param dct dictionary class
   * @return new instance of the WordNet dictionary
   */
  private static Object newDict(final Class<?> dct) {
    try {
      final Constructor<?> ctr = Reflect.find(dct, URL.class);
      final Object dict = Reflect.get(ctr, new URL("file", null, PATH));
      return Reflect.invoke(Reflect.find(dct, "open"), dict);
    } catch(final Exception ex) {
      return null;
    }
  }

  /**
   * Checks if the library is available.
   * @return result of check
   */
  static boolean available() {
    return DICT != null;
  }

  /** Instance of WordNet stemmer. */
  private Object stemmer;

  /** Empty constructor. */
  WordnetStemmer() {
    super();
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
