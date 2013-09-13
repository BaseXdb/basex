package org.basex.data.atomic;

import org.basex.data.*;

/**
 * Data container with start and end offset.
 *
 * @author BaseX Team 2005-12, BSD License
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
   * @param d data reference
   */
  public DataClip(final Data d) {
    this(d, 0, d.meta.size);
  }

  /**
   * Constructor.
   * @param d data reference
   * @param s start
   * @param e end
   */
  public DataClip(final Data d, final int s, final int e) {
    data = d;
    start = s;
    end = e;
  }

  /**
   * Returns the box size.
   * @return size
   */
  public int size() {
    return end - start;
  }
}
