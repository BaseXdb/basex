package org.basex.util.ft;

import static org.basex.util.Token.*;

import java.io.*;
import java.lang.reflect.*;
import java.net.*;
import java.util.*;

import org.basex.util.*;

/**
 * Stemmer implementation using the WordNet stemmer.
 * The WordNet stemmer is developed by George A. Miller and is based on
 * the WordNet 3.0 License: {@code http://wordnet.princeton.edu/}.
 *
 * @author BaseX Team, BSD License
 * @author Dimitar Popov
 */
final class WordnetStemmer extends Stemmer {
  /** Name of the package of the WordNet stemmer. */
  private static final String PATTERN = "edu.mit.jwi.%";
  /** Path to the WordNet dictionary files. */
  private static final String PATH = "etc/wndict";
  /** WordnetStemmer class (can be {@code null}). */
  private static final Constructor<?> CTR;
  /** WordnetStemmer.findStems method (can be {@code null}). */
  private static final Method FIND_STEMS;
  /** WordNet dictionary instance (can be {@code null}). */
  private static final Object DICT;

  static {
    Constructor<?> ctr = null;
    Method findStems = null;
    Object dictionary = null;
    // don't try to find the other classes if Dictionary is not found:
    final Class<?> dct = Reflect.find(PATTERN, "Dictionary");
    if(dct != null) {
      try {
        final Class<?> wn = Class.forName(Util.info(PATTERN, "morph.WordnetStemmer"));
        ctr = wn.getConstructor(Class.forName(Util.info(PATTERN, "IDictionary")));
        findStems = wn.getMethod("findStems", String.class);
        final URL url = new File(PATH).toURI().toURL();
        final Object dict = dct.getConstructor(URL.class).newInstance(url);
        // open returns a boolean flag; discard dictionary if it could not be opened
        if(dct.getMethod("open").invoke(dict) == Boolean.TRUE) dictionary = dict;
      } catch(final Exception ex) {
        Util.debug(ex);
      }
    }
    CTR = ctr;
    FIND_STEMS = findStems;
    DICT = dictionary;
  }

  /**
   * Checks if the library is available.
   * @return result of check
   */
  static boolean available() {
    return CTR != null && FIND_STEMS != null && DICT != null;
  }

  /** Instance of WordNet stemmer. */
  private Object stemmer;

  /** Empty constructor. */
  WordnetStemmer() {
  }

  /**
   * Constructs a WordNet stemmer. Call {@link #available()} first to
   * check if the library is available.
   * @param fti full-text iterator
   */
  private WordnetStemmer(final FTIterator fti) {
    super(fti);
    try {
      stemmer = CTR.newInstance(DICT);
    } catch(final ReflectiveOperationException ex) {
      throw Util.notExpected(ex);
    }
  }

  @Override
  Stemmer get(final Language lang, final FTIterator fti) {
    return new WordnetStemmer(fti);
  }

  @Override
  protected byte prec() {
    return 30;
  }

  @Override
  Collection<Language> languages() {
    return collection("en");
  }

  @Override
  protected byte[] stem(final byte[] word) {
    @SuppressWarnings("unchecked")
    final List<String> l = (List<String>) Reflect.invoke(FIND_STEMS, stemmer, string(word));
    final byte[] result = l.isEmpty() ? word : token(l.getFirst());
    return result.length == 0 ? word : result;
  }
}
