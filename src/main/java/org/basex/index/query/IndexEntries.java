package org.basex.index.query;

import org.basex.index.*;

/**
 * This class contains information for returning index entries.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class IndexEntries implements IndexToken {
  /** Text. */
  private final byte[] text;
  /** Ascending/descending traversal. */
  public boolean descending;
  /** Prefix/traversal flag. */
  public boolean prefix;

  /**
   * Constructor for prefix search.
   * @param tok token
   */
  public IndexEntries(final byte[] tok) {
    text = tok;
    prefix = true;
  }

  /**
   * Constructor for traversing entries.
   * @param tok token to start with
   * @param asc return results in ascending order
   */
  public IndexEntries(final byte[] tok, final boolean asc) {
    descending = !asc;
    text = tok;
  }

  @Override
  public IndexType type() {
    return null;
  }

  @Override
  public byte[] get() {
    return text;
  }
}
