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
 * @author BaseX Team, BSD License
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
  /** Maximum estimated size of the temporary index structures. */
  private final long maxMem = Runtime.getRuntime().maxMemory() / 2;

  /** Names and namespace URI of element or attributes to include. */
  protected final IndexNames includeNames;

  /** Current PRE value. */
  protected int pre;
  /** Total number of index operations (may get pretty large). */
  protected long count;
  /** Number of partial index structures. */
  protected int splits;

  /**
   * Constructor.
   * @param data reference
   * @param type index type
   */
  protected IndexBuilder(final Data data, final IndexType type) {
    this.data = data;
    this.type = type;
    splitSize = (int) Math.min(Integer.MAX_VALUE, (long) data.meta.splitsize * splitFactor(type));
    size = data.nodes();
    includeNames = new IndexNames(type, data);
    text = type == IndexType.TEXT || type == IndexType.FULLTEXT;
  }

  /**
   * Builds the index structure and returns an index instance.
   * @return index instance
   * @throws IOException I/O exception
   */
  public abstract ValueIndex build() throws IOException;

  /**
   * Checks if the command was interrupted, and prints some debug output.
   * @throws IOException I/O exception
   */
  @SuppressWarnings("unused")
  protected void check() throws IOException {
    checkStop();
    if(Prop.debug && (pre & 0x1FFFFF) == 0) Util.err(".");
  }

  /**
   * Decides whether in-memory temporary index structures are so large
   * that we must flush them to disk before continuing.
   * @param memory estimated size of the temporary index structures
   * @return true if structures shall be flushed to disk
   */
  protected final boolean splitRequired(final long memory) {
    // checks if a fixed split size has been specified
    final boolean split = splitSize > 0 ? count >= (splits + 1L) * splitSize : memory >= maxMem;
    if(split && Prop.debug) Util.err("|");
    return split;
  }

  /**
   * Prints some final debugging information.
   */
  protected final void finishIndex() {
    if(!Prop.debug) return;

    final StringBuilder sb = new StringBuilder();
    sb.append(' ').append(count / 10000 / 100d).append(" M operations, ");
    sb.append(perf).append(" (").append(Performance.formatMemory()).append(").");
    if(splits > 1 && splitSize <= 0) {
      sb.append(" Recommended ").append(MainOptions.SPLITSIZE.name()).append(": ");
      sb.append((int) Math.ceil((double) count / splits / splitFactor(type))).append('.');
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
   * @param type index type
   * @return split factor
   */
  public static int splitFactor(final IndexType type) {
    return type == IndexType.FULLTEXT ? 1000000 : 100000;
  }

  @Override
  public final String shortInfo() {
    return CREATING_INDEXES + DOTS;
  }

  @Override
  public final double progressInfo() {
    return pre / (size + (splits > 0 ? size / 50.0d : 0.0d));
  }

  @Override
  public final String detailedInfo() {
    return switch(type) {
      case TEXT      -> INDEX_TEXTS_D;
      case ATTRIBUTE -> INDEX_ATTRIBUTES_D;
      case TOKEN     -> INDEX_TOKENS_D;
      case FULLTEXT  -> INDEX_FULLTEXT_D;
      default        -> throw Util.notExpected();
    };
  }
}
