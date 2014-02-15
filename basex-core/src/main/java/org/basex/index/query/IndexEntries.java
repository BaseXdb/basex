package org.basex.index.query;

import org.basex.index.*;

/**
 * This class contains information for returning index entries.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class IndexEntries implements IndexToken {
  /** Index type. */
  private final IndexType type;
  /** Text. */
  private final byte[] text;
  /** Ascending/descending traversal. */
  public boolean descending;
  /** Prefix/traversal flag. */
  public boolean prefix;

  /**
   * Private constructor.
   * @param it index type
   * @param tok token
   */
  private IndexEntries(final IndexType it, final byte[] tok) {
    type = it;
    text = tok;
  }

  /**
   * Constructor for prefix search.
   * @param tok token
   * @param it index type
   */
  public IndexEntries(final byte[] tok, final IndexType it) {
    this(it, tok);
    prefix = true;
  }

  /**
   * Constructor for traversing entries.
   * @param tok token to start with
   * @param asc return results in ascending order
   * @param it index type
   */
  public IndexEntries(final byte[] tok, final boolean asc, final IndexType it) {
    this(it, tok);
    descending = !asc;
  }

  @Override
  public IndexType type() {
    return type;
  }

  @Override
  public byte[] get() {
    return text;
  }
}
