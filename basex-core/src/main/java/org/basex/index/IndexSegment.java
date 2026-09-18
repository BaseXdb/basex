package org.basex.index;

import java.io.*;
import java.util.*;

import org.basex.data.*;
import org.basex.io.*;
import org.basex.io.in.DataInput;
import org.basex.io.out.DataOutput;
import org.basex.util.hash.*;

/**
 * An immutable segment of an updatable index structure (see {@link SegmentedIndex}).
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public abstract class IndexSegment {
  /** Data reference. */
  protected final Data data;
  /** Segment number ({@code -1} for the base structure). */
  public final int number;
  /** File prefix. */
  public final String prefix;
  /** IDs of the units whose older references this segment supersedes (can be {@code null}). */
  public final IntSet superseded;
  /** IDs superseded by newer segments. */
  protected IntSet newer = new IntSet();

  /**
   * Constructor.
   * @param data data reference
   * @param number segment number ({@code -1} for the base structure)
   * @param prefix file prefix
   * @throws IOException I/O exception
   */
  protected IndexSegment(final Data data, final int number, final String prefix)
      throws IOException {
    this.data = data;
    this.number = number;
    this.prefix = prefix;
    final IOFile file = data.meta.dbFile(prefix + 's');
    if(file.exists()) {
      superseded = new IntSet();
      try(DataInput in = new DataInput(file)) {
        for(final int id : in.readNums()) superseded.add(id);
      }
    } else {
      superseded = null;
    }
  }

  /**
   * Writes the supersede set of a segment.
   * @param data data reference
   * @param prefix file prefix of the segment
   * @param ids IDs of the superseded units (nothing is written if empty)
   * @throws IOException I/O exception
   */
  public static void superseded(final Data data, final String prefix, final int[] ids)
      throws IOException {
    if(ids.length == 0) return;
    Arrays.sort(ids);
    try(DataOutput out = new DataOutput(data.meta.dbFile(prefix + 's'))) {
      out.writeNums(ids);
    }
  }

  /**
   * Returns the number of keys, including keys without live references.
   * @return number of keys
   */
  public abstract int size();

  /**
   * Returns the size of the files.
   * @return size in bytes
   */
  public abstract long length();

  /**
   * Closes the files.
   */
  public abstract void close();
}
