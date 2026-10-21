package org.basex.index;

import static org.basex.core.Text.*;

import java.io.*;

import org.basex.core.*;
import org.basex.core.jobs.*;
import org.basex.data.*;
import org.basex.index.ft.*;
import org.basex.index.value.*;
import org.basex.util.*;
import org.basex.util.ft.*;

/**
 * This class defines the functions which are needed for building
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
    size = data.nodes();
    includeNames = new IndexNames(type, data);
  }

  /**
   * Checks if the index structures can be built with the specified options.
   * @param options main options
   * @param data existing database (can be {@code null})
   * @throws IOException I/O exception
   */
  public static void check(final MainOptions options, final Data data) throws IOException {
    if(options.get(MainOptions.TEXTINDEX)) {
      IndexNames.check(IndexType.TEXT, options.get(MainOptions.TEXTINCLUDE));
    }
    if(options.get(MainOptions.ATTRINDEX)) {
      IndexNames.check(IndexType.ATTRIBUTE, options.get(MainOptions.ATTRINCLUDE));
    }
    if(options.get(MainOptions.TOKENINDEX)) {
      IndexNames.check(IndexType.TOKEN, options.get(MainOptions.TOKENINCLUDE));
    }
    if(options.get(MainOptions.FTINDEX)) {
      final String ftinclude = options.get(MainOptions.FTINCLUDE);
      IndexNames.check(IndexType.FULLTEXT, ftinclude);
      FTBuilder.check(options.get(MainOptions.FTMIXED), ftinclude, Language.get(options),
          options.get(MainOptions.STEMMING));
      StopWords.check(data, options.get(MainOptions.STOPWORDS));
    }
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
    final boolean split = MemoryLimit.exceeded(memory);
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
    Util.errln(sb);
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
