package org.basex.index;

import static org.basex.core.Text.*;

import java.io.*;

import org.basex.core.*;
import org.basex.data.*;
import org.basex.util.*;

/**
 * This interface defines the functions which are needed for building
 * new index structures.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public abstract class IndexBuilder extends Proc {
  /** Data reference. */
  protected final Data data;
  /** Total parsing value. */
  protected final int size;
  /** Number of index operations to perform before writing a partial index to disk. */
  private final int splitSize;

  /** Runtime for memory consumption. */
  private final Runtime rt = Runtime.getRuntime();
  /** Maximum memory to consume. */
  private final long maxMem = (long) (rt.maxMemory() * 0.8);

  /** Current pre value. */
  protected int pre;
  /** Total number of index operations (may get pretty large). */
  protected long count;
  /** Number of partial index structures. */
  protected int splits;
  /** Threshold for freeing memory when estimating main memory consumption. */
  private int gcCount;

  /**
   * Builds the index structure and returns an index instance.
   * @return index instance
   * @throws IOException I/O Exception
   */
  public abstract Index build() throws IOException;

  /**
   * Checks if the command was interrupted, and prints some debug output.
   */
  protected final void check() {
    checkStop();
    if(Prop.debug && (pre & 0x1FFFFF) == 0) Util.err(".");
  }

  /**
   * Decides whether in-memory temporary index structures are so large
   * that we must flush them to disk before continuing.
   * @return true if structures shall be flushed to disk
   * @throws IOException I/O Exception
   */
  protected final boolean split() throws IOException {
    // checks if a fixed split size has been specified
    final boolean split;
    if(splitSize > 0) {
      split = count >= (splits + 1L) * splitSize;
    } else {
      // if not, estimate how much main memory is left
      split = rt.totalMemory() - rt.freeMemory() >= maxMem;
      // stop operation if index splitting degenerates
      int gc = gcCount;
      if(split) {
        if(gc >= 0) throw new BaseXException(OUT_OF_MEM + H_OUT_OF_MEM);
        gc = 30;
      } else {
        gc = Math.max(-1, gc - 1);
      }
      gcCount = gc;
    }
    if(split && Prop.debug) Util.err("|");
    return split;
  }

  /**
   * Performs memory cleanup after writing partial memory, if necessary.
   */
  protected final void finishSplit() {
    if(splitSize <= 0) Performance.gc(1);
  }

  /**
   * Prints some final debugging information.
   * @param perf performance
   */
  protected final void finishIndex(final Performance perf) {
    if(!Prop.debug) return;

    final StringBuilder sb = new StringBuilder();
    if(splits > 1) sb.append(' ').append(splits).append(" splits,");
    sb.append(' ').append(count).append(" operations, ");
    sb.append(perf).append(" (").append(Performance.getMemory()).append(')');
    Util.errln(sb);
  }

  /**
   * Constructor.
   * @param d reference
   * @param max maximum number of operations per partial index
   */
  protected IndexBuilder(final Data d, final int max) {
    data = d;
    size = data.meta.size;
    splitSize = max;
    if(rt.totalMemory() - rt.freeMemory() >= maxMem) Performance.gc(1);
  }

  @Override
  public final String tit() {
    return CREATING_INDEXES;
  }

  @Override
  public final double prog() {
    return (double) pre / (size + (splits > 0 ? size / 50 : 0));
  }
}
