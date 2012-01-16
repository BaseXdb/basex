package org.basex.util.ft;

import java.util.Iterator;
import org.basex.util.Util;

/**
 * Full-text iterator.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public abstract class FTIterator implements Iterator<FTSpan> {
  /**
   * Initializes the iterator.
   * @param text text
   * @return self reference
   */
  public abstract FTIterator init(final byte[] text);

  /**
   * Returns the next token. May be called as an alternative to {@link #next}
   * to avoid the creation of new {@link FTSpan} instances.
   * @return token
   */
  public abstract byte[] nextToken();

  @Override
  public final void remove() {
    Util.notimplemented();
  }
}
