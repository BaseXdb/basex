package org.basex.util.ft;

/**
 * Internal stemmer implementation.
 * The names of the implementations
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
abstract class InternalStemmer extends Stemmer {
  /**
   * Constructor.
   * @param fti full-text iterator
   */
  InternalStemmer(final FTIterator fti) {
    super(fti);
  }

  @Override
  protected final byte prec() {
    return 10;
  }

  @Override
  public final String toString() {
    return "Internal";
  }
}
