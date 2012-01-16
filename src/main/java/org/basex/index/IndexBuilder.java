package org.basex.index;

import static org.basex.core.Text.*;
import java.io.IOException;

import org.basex.core.BaseXException;
import org.basex.core.Progress;
import org.basex.data.Data;
import org.basex.util.Performance;
import org.basex.util.Util;

/**
 * This interface defines the functions which are needed for building
 * new index structures.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public abstract class IndexBuilder extends Progress {
  /** Data reference. */
  protected final Data data;
  /** Total parsing value. */
  protected final int size;
  /** Current parsing value. */
  protected int pre;
  /** Merge flag. */
  protected boolean merge;
  /** Number of cached index structures. */
  protected int csize;

  /** Runtime for memory consumption. */
  private final Runtime rt = Runtime.getRuntime();
  /** Maximum memory to consume. */
  private final long maxMem = (long) (rt.maxMemory() * 0.8);

  /** Free memory threshold. */
  private int cc;

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
    if(Util.debug && (pre & 0x1FFFFF) == 0) Util.err(".");
  }

  /**
   * Checks if enough memory is left to continue index building.
   * @return result of check
   * @throws IOException I/O exception
   */
  protected final boolean memFull() throws IOException {
    final boolean full = rt.totalMemory() - rt.freeMemory() >= maxMem;
    if(full) {
      if(cc >= 0) throw new BaseXException(PROCMEM + PROCMEMCREATE);
      if(Util.debug) Util.err("!");
      merge = true;
      cc = 30;
    } else {
      --cc;
    }
    return full;
  }

  /**
   * Constructor.
   * @param d reference
   */
  protected IndexBuilder(final Data d) {
    data = d;
    size = data.meta.size;
    if(rt.totalMemory() - rt.freeMemory() >= rt.maxMemory() >> 1)
      Performance.gc(2);
  }

  @Override
  public final String tit() {
    return PROGINDEX;
  }

  @Override
  public final double prog() {
    return (double) pre / (size + (merge  ? size / 50 : 0));
  }
}
