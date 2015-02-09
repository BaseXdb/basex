package org.basex.data.atomic;

import org.basex.data.*;

/**
 * Data container with start and end offset. Used mostly to save memory with insertion
 * sequence caching (only one {@link Data} instance).
 *
 * Arbitrary trees can be stored in this clip. To distinguish between two insertion
 * sequences, the root node of each sequence points to the parent PRE==-1.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
public final class DataClip {
  /** Data reference. */
  public final Data data;
  /** Start value. */
  public final int start;
  /** End value (+1). */
  public final int end;
  /** Number of contained fragments. */
  public int fragments;

  /**
   * Constructor.
   * @param data data reference
   */
  public DataClip(final Data data) {
    this(data, 0, data.meta.size);
  }

  /**
   * Constructor.
   * @param data data reference
   * @param start start
   * @param end end
   */
  public DataClip(final Data data, final int start, final int end) {
    this.data = data;
    this.start = start;
    this.end = end;
  }

  /**
   * Returns the box size.
   * @return size
   */
  public int size() {
    return end - start;
  }
}
