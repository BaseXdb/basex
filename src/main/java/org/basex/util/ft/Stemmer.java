package org.basex.util.ft;

import java.util.Collections;
import java.util.LinkedList;

/**
 * Implementation of common stemmer methods.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Dimitar Popov
 */
public abstract class Stemmer extends LanguageImpl {
  /** List of available stemmers. */
  public static final LinkedList<Stemmer> IMPL;

  /** Load stemmers and order them by precedence. */
  static {
    IMPL = new LinkedList<Stemmer>();
    // Built-in stemmers
    IMPL.add(new EnglishStemmer());
    IMPL.add(new GermanStemmer());

    if(SnowballStemmer.available()) IMPL.add(new SnowballStemmer());
    if(LuceneStemmer.available()) IMPL.add(new LuceneStemmer());
    if(WordnetStemmer.available()) IMPL.add(WordnetStemmer.get());

    // sort stemmers and tokenizers by precedence
    Collections.sort(IMPL);
  }

  /**
   * Stem a word.
   * @param word input word to stem
   * @return the stem of the word
   */
  abstract byte[] stem(final byte[] word);

  /**
   * Factory method.
   * @param l language
   * @return span processor
   */
  abstract Stemmer get(final Language l);

  /**
   * Returns an iterator, wrapping the specified iterator.
   * @param iter input iterator
   * @return output iterator
   */
  final FTIterator iter(final FTIterator iter) {
    return new FTIterator() {
      @Override
      public FTIterator init(final byte[] txt) {
        iter.init(txt);
        return this;
      }

      @Override
      public boolean hasNext() {
        return iter.hasNext();
      }

      @Override
      public Span next() {
        final Span s = iter.next();
        s.text = stem(s.text);
        return s;
      };

      @Override
      public byte[] nextToken() {
        return stem(iter.nextToken());
      };
    };
  }
}
