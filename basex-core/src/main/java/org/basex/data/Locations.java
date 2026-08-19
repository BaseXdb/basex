package org.basex.data;

import java.util.*;

import org.basex.util.*;
import org.basex.util.list.*;

/**
 * Source locations of parsed nodes, indexed by PRE value.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class Locations {
  /** Number of locations between two checkpoints (as power of two). */
  private static final int SHIFT = 6;

  /** Compressed line and column numbers. */
  private byte[] values = new byte[Array.INITIAL_CAPACITY];
  /** Checkpoints: positions in {@link #values}. */
  private final IntList offsets = new IntList();
  /** Checkpoints: line and column numbers. */
  private final LongList positions = new LongList();
  /** Number of locations. */
  private int size;
  /** Line number of the last location. */
  private int line;
  /** Column number of the last location. */
  private int column;

  /**
   * Constructor.
   */
  public Locations() {
    Num.size(values, 4);
  }

  /**
   * Adds the location of the next node.
   * @param ln line number
   * @param cl column number
   */
  public void add(final int ln, final int cl) {
    if((size & (1 << SHIFT) - 1) == 0) {
      offsets.add(Num.size(values));
      positions.add(pack(line, column));
    }
    // locations are added in document order: deltas are never negative
    final int ld = Math.max(ln - line, 0);
    values = Num.add(values, ld);
    values = Num.add(values, Math.max(ld == 0 ? cl - column : cl, 0));
    line = ln;
    column = cl;
    ++size;
  }

  /**
   * Discards unused capacity. No more locations can be added afterwards.
   */
  public void finish() {
    values = Arrays.copyOf(values, Num.size(values));
  }

  /**
   * Returns the location of a node.
   * @param pre PRE value
   * @return line number (upper 32 bits) and column number (lower 32 bits), or {@code 0} if the
   *   location is unknown
   */
  public long location(final int pre) {
    if(pre < 0 || pre >= size) return 0;

    final int c = pre >> SHIFT;
    final long position = positions.get(c);
    int ln = (int) (position >>> 32), cl = (int) position, pos = offsets.get(c);
    for(int p = c << SHIFT; p <= pre; p++) {
      final int ld = Num.get(values, pos);
      pos += Num.length(values, pos);
      final int cv = Num.get(values, pos);
      pos += Num.length(values, pos);
      if(ld == 0) {
        cl += cv;
      } else {
        ln += ld;
        cl = cv;
      }
    }
    return pack(ln, cl);
  }

  /**
   * Packs a line and a column number into a single value.
   * @param ln line number
   * @param cl column number
   * @return packed value
   */
  private static long pack(final int ln, final int cl) {
    return (long) ln << 32 | cl & 0xFFFFFFFFL;
  }
}
