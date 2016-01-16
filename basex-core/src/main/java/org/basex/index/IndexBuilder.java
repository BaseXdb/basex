package org.basex.index;

import static org.basex.core.Text.*;

import java.io.*;

import org.basex.core.*;
import org.basex.data.*;
import org.basex.index.value.*;
import org.basex.util.*;

/**
 * This interface defines the functions which are needed for building
 * new index structures.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
public abstract class IndexBuilder extends Proc {
  /** Performance. */
  protected final Performance perf = new Performance();

  /** Data reference. */
  protected final Data data;
  /** Total parsing value. */
  protected final int size;
  /** Node type to index (text/attributes). */
  protected final boolean text;
  /** Tokenize index values. */
  protected final boolean tokenize;

  /** Number of index operations to perform before writing a partial index to disk. */
  private final int splitSize;
  /** Maximum memory to consume. */
  private final long maxMem = (long) (Runtime.getRuntime().maxMemory() * 0.8);

  /** Names and namespace uri of element or attributes to include. */
  private final IndexNames includeNames;

  /** Current pre value. */
  protected int pre;
  /** Total number of index operations (may get pretty large). */
  protected long count;
  /** Number of partial index structures. */
  protected int splits;
  /** Threshold for freeing memory when estimating main memory consumption. */
  private int gcCount;

  /**
   * Constructor.
   * @param data reference
   * @param splitSize index split size
   * @param includes names of elements or attributes to include
   * @param text index type (text/attributes)
   * @param tokenize tokenize index values
   */
  protected IndexBuilder(final Data data, final int splitSize, final String includes,
      final boolean text, final boolean tokenize) {

    this.data = data;
    this.splitSize = splitSize;
    this.text = text;
    this.tokenize = tokenize;
    size = data.meta.size;
    includeNames = new IndexNames(includes);

    assert !text || !tokenize; // Token index only allowed for attribute index

    // run garbage collection if memory maximum is already reached
    if(Performance.memory() >= maxMem) Performance.gc(1);
  }

  /**
   * Builds the index structure and returns an index instance.
   * @return index instance
   * @throws IOException I/O Exception
   */
  public abstract ValueIndex build() throws IOException;

  /**
   * Checks if the command was interrupted, and prints some debug output.
   * @throws IOException I/O Exception
   */
  @SuppressWarnings("unused")
  protected void check() throws IOException {
    checkStop();
    if(Prop.debug && (pre & 0x1FFFFF) == 0) Util.err(".");
  }

  /**
   * Checks if the current entry should be indexed.
   * @return result of check
   */
  protected final boolean indexEntry() {
    return data.kind(pre) == (text ? Data.TEXT : Data.ATTR)
        && includeNames.contains(data, pre, text);
  }

  /**
   * Decides whether in-memory temporary index structures are so large
   * that we must flush them to disk before continuing.
   * @return true if structures shall be flushed to disk
   * @throws IOException I/O Exception
   */
  protected final boolean splitRequired() throws IOException {
    // checks if a fixed split size has been specified
    final boolean split;
    if(splitSize > 0) {
      split = count >= (splits + 1L) * splitSize;
    } else {
      // if not, estimate how much main memory is left
      split = Performance.memory() >= maxMem;
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
   * Performs memory cleanup after writing partial memory if necessary.
   */
  protected final void finishSplit() {
    if(splitSize <= 0) Performance.gc(1);
  }

  /**
   * Prints some final debugging information.
   */
  protected final void finishIndex() {
    if(!Prop.debug) return;

    final StringBuilder sb = new StringBuilder();
    if(splits > 1) sb.append(' ').append(splits).append(" splits,");
    sb.append(' ').append(count).append(" operations, ");
    sb.append(perf).append(" (").append(Performance.getMemory()).append(')');
    Util.errln(sb);
  }

  @Override
  public final String tit() {
    return CREATING_INDEXES;
  }

  @Override
  public final double prog() {
    return pre / (size + (splits > 0 ? size / 50d : 0d));
  }
}
