package org.basex.util.ft;

import java.util.Collection;

import org.basex.util.Util;

/**
 * Internal stemmer implementation.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public abstract class InternalStemmer extends Stemmer {
  /**
   * Constructor.
   * @param fti full-text iterator
   */
  protected InternalStemmer(final FTIterator fti) {
    super(fti);
  }

  @Override
  final Collection<Language> languages() {
    return collection(Util.name(this).replace("Stemmer", ""));
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
