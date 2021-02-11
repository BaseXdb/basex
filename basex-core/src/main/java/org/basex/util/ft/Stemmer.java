package org.basex.util.ft;

import java.util.*;

import org.basex.util.*;

/**
 * Implementation of common stemmer methods.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Dimitar Popov
 */
public abstract class Stemmer extends LanguageImpl {
  /** List of available stemmers. */
  static final ArrayList<Stemmer> IMPL = new ArrayList<>();

  /* Load stemmers and order them by precedence. */
  static {
    // built-in stemmers
    IMPL.add(new EnglishStemmer(null));
    IMPL.add(new GermanStemmer(null));
    IMPL.add(new GreekStemmer(null));
    IMPL.add(new IndonesianStemmer(null));
    IMPL.add(new DummyStemmer(null));

    if(SnowballStemmer.available()) IMPL.add(new SnowballStemmer());
    if(LuceneStemmer.available()) IMPL.add(new LuceneStemmer());
    if(WordnetStemmer.available()) IMPL.add(new WordnetStemmer());

    // sort stemmers and tokenizers by precedence
    Collections.sort(IMPL);
  }

  /** Full-text iterator. */
  private final FTIterator iter;

  /**
   * Constructor.
   */
  Stemmer() {
    this(null);
  }

  /**
   * Constructor.
   * @param iter full-text iterator
   */
  Stemmer(final FTIterator iter) {
    this.iter = iter;
  }

  /**
   * Checks if the language is supported by the available stemmers.
   * @param language language to be found
   * @return result of check
   */
  public static boolean supportFor(final Language language) {
    for(final Stemmer impl : IMPL) {
      if(impl.supports(language)) return true;
    }
    return false;
  }

  /**
   * Factory method.
   * @param lang language
   * @param fti full-text iterator
   * @return stemmer
   */
  abstract Stemmer get(Language lang, FTIterator fti);

  /**
   * Stems a word.
   * @param word input word to stem
   * @return the stem of the word
   */
  protected abstract byte[] stem(byte[] word);

  @Override
  public final Stemmer init(final byte[] txt) {
    iter.init(txt);
    return this;
  }

  @Override
  public final boolean hasNext() {
    return iter.hasNext();
  }

  @Override
  public final FTSpan next() {
    final FTSpan s = iter.next();
    s.text = stem(s.text);
    return s;
  }

  @Override
  public final byte[] nextToken() {
    return stem(iter.nextToken());
  }

  @Override
  public String toString() {
    return Util.className(this).replace("Stemmer", "");
  }
}
