package org.basex.index;

/**
 * This class provides a single index entry.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Dimitar Popov
 */
public final class IndexEntry {
  /** Entry key. */
  public final byte[] key;
  /** Number of index hits for the key. */
  public int size;
  /** Pointer to the id list for the key. */
  public long pointer;

  /**
   * Constructor.
   * @param key key
   * @param size number of hits
   * @param pointer pointer to the id list
   */
  public IndexEntry(final byte[] key, final int size, final long pointer) {
    this.key = key;
    this.size = size;
    this.pointer = pointer;
  }
}
