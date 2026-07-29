package org.basex.util.ft;

import java.util.*;

import org.basex.util.*;

/**
 * Stemmer implementation for stemmers of external libraries, accessed via reflection.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 * @param <C> stemmer class type
 */
abstract class ExternalStemmer<C extends ExternalStemmer.StemmerClass> extends Stemmer {
  /** Stemmer class for the requested language (can be {@code null}). */
  final C clazz;
  /** Stemmer instance (can be {@code null}). */
  final Object stemmer;

  /** Empty constructor. */
  ExternalStemmer() {
    clazz = null;
    stemmer = null;
  }

  /**
   * Constructs a stemmer instance.
   * @param lang language of the text to stem
   * @param fti full-text iterator
   */
  ExternalStemmer(final Language lang, final FTIterator fti) {
    super(fti);
    clazz = classes().get(lang);
    stemmer = Reflect.get(clazz.clz());
  }

  /**
   * Returns the stemmer classes provided by the library.
   * @return stemmer classes, indexed by language
   */
  abstract Map<Language, C> classes();

  @Override
  final Collection<Language> languages() {
    return classes().keySet();
  }

  /** Stemmer class of an external library. */
  interface StemmerClass {
    /**
     * Returns the class implementing the stemmer.
     * @return stemmer class
     */
    Class<?> clz();
  }
}
