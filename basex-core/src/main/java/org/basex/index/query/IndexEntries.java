package org.basex.index.query;

import org.basex.index.*;

/**
 * This class contains information for returning index entries.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class IndexEntries implements IndexSearch {
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
   * @param type index type
   * @param text token
   */
  private IndexEntries(final IndexType type, final byte[] text) {
    this.type = type;
    this.text = text;
  }

  /**
   * Constructor for prefix search.
   * @param text token
   * @param type index type
   */
  public IndexEntries(final byte[] text, final IndexType type) {
    this(type, text);
    prefix = true;
  }

  /**
   * Constructor for traversing entries.
   * @param text token to start with
   * @param asc return results in ascending order
   * @param type index type
   */
  public IndexEntries(final byte[] text, final boolean asc, final IndexType type) {
    this(type, text);
    descending = !asc;
  }

  @Override
  public IndexType type() {
    return type;
  }

  @Override
  public byte[] token() {
    return text;
  }
}
