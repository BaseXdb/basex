package org.basex.index;

/**
 * This class represents a single index entry.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Dimitar Popov
 */
public final class IndexEntry {
  /** Entry key. */
  public final byte[] key;
  /** Number of index results. */
  public int size;
  /** File offset to the id list. */
  public long offset;

  /**
   * Constructor.
   * @param key key
   * @param size number of index results
   * @param offset file offset to the id list
   */
  public IndexEntry(final byte[] key, final int size, final long offset) {
    this.key = key;
    this.size = size;
    this.offset = offset;
  }
}
