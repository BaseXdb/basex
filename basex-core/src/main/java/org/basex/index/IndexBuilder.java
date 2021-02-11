package org.basex.index;

import static org.basex.core.Text.*;

import java.io.*;

import org.basex.core.*;
import org.basex.core.jobs.*;
import org.basex.data.*;
import org.basex.index.value.*;
import org.basex.util.*;

/**
 * This interface defines the functions which are needed for building
 * new index structures.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public abstract class IndexBuilder extends Job {
  /** Performance. */
  private final Performance perf = new Performance();

  /** Data reference. */
  protected final Data data;
  /** Total parsing value. */
  protected final int size;
  /** Index type. */
  protected final IndexType type;
  /** Text node flag. */
  protected final boolean text;

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
   * @param type index type
   */
  protected IndexBuilder(final Data data, final IndexType type) {
    this.data = data;
    this.type = type;
    splitSize = (int) Math.min(Integer.MAX_VALUE, (long) data.meta.splitsize * splitFactor());
    size = data.meta.size;
    includeNames = new IndexNames(type, data);
    text = type == IndexType.TEXT || type == IndexType.FULLTEXT;

    // run garbage collection if memory maximum is already reached
    if(Performance.memory() >= maxMem) clean();
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
    return data.kind(pre) == (text ? Data.TEXT : Data.ATTR) && includeNames.contains(pre, text);
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
  protected final void clean() {
    if(splitSize <= 0) Performance.gc(2);
  }

  /**
   * Prints some final debugging information.
   */
  protected final void finishIndex() {
    if(!Prop.debug) return;

    final StringBuilder sb = new StringBuilder();
    sb.append(' ').append((count / 10000) / 100.0d).append(" M operations, ");
    sb.append(perf).append(" (").append(Performance.getMemory()).append(").");
    if(splits > 1 && splitSize <= 0) {
      sb.append(" Recommended ").append(MainOptions.SPLITSIZE.name()).append(": ");
      sb.append((int) Math.ceil(((double) count / splits) / splitFactor())).append('.');
    }
    Util.errln(sb);
  }

  /**
   * Returns the split factor dependent on the index type.
   * The following values are returned:
   * <ul>
   *   <li> Full-text index: 1'000'000</li>
   *   <li> Other value indexes: 100'000</li>
   * </ul>
   * @return split factor
   */
  private int splitFactor() {
    return type == IndexType.FULLTEXT ? 1000000 : 100000;
  }

  @Override
  public final String shortInfo() {
    return CREATING_INDEXES;
  }

  @Override
  public final double progressInfo() {
    return pre / (size + (splits > 0 ? size / 50.0d : 0.0d));
  }

  @Override
  public final String detailedInfo() {
    switch(type) {
      case TEXT: return INDEX_TEXTS_D;
      case ATTRIBUTE: return INDEX_ATTRIBUTES_D;
      case TOKEN: return INDEX_TOKENS_D;
      case FULLTEXT: return INDEX_FULLTEXT_D;
      default: throw Util.notExpected();
    }
  }
}
