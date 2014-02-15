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
   * @param k key
   * @param s number of hits
   * @param p pointer to the id list
   */
  public IndexEntry(final byte[] k, final int s, final long p) {
    key = k;
    size = s;
    pointer = p;
  }
}
