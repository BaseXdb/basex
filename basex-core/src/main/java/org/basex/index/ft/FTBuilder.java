package org.basex.index.ft;

import static org.basex.core.Text.*;
import static org.basex.data.DataText.*;

import java.io.*;

import org.basex.core.*;
import org.basex.data.*;
import org.basex.index.*;
import org.basex.util.*;
import org.basex.util.ft.*;

/**
 * This class contains common methods for full-text index builders.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class FTBuilder extends IndexBuilder {
  /** Number of tokens after which a partial index is written ({@code 0}: decided by memory). */
  static int splitTokens;

  /** Value trees. */
  private FTIndexTrees tree;
  /** Word parser. */
  private final FTLexer lexer;
  /** Indicates if node IDs are indexed instead of PRE values (see {@link FTIndex}). */
  private final boolean ids;

  /**
   * Constructor.
   * @param data data reference
   * @throws IOException I/O exception
   */
  public FTBuilder(final Data data) throws IOException {
    super(data, IndexType.FULLTEXT);
    tree = new FTIndexTrees(data.meta.maxlen);
    lexer = lexer(data);
    ids = data.meta.updindex;
  }

  /**
   * Returns a lexer for the full-text index of the specified database.
   * @param data data reference
   * @return lexer
   * @throws IOException I/O exception
   */
  public static FTLexer lexer(final Data data) throws IOException {
    final MetaData meta = data.meta;
    final FTOpt fto = new FTOpt();
    fto.set(FTFlag.DC, meta.diacritics);
    fto.set(FTFlag.ST, meta.stemming);
    fto.cs = meta.casesens ? FTCase.SENSITIVE : FTCase.INSENSITIVE;
    fto.sw = new StopWords(data, meta.stopwords);
    fto.ln = meta.language();

    // element names are required; wildcards are allowed (all elements on all levels)
    if(meta.ftmixed && meta.ftinclude.isEmpty())
      throw new BaseXException("% requires %.", MainOptions.FTMIXED.name(),
          MainOptions.FTINCLUDE.name());
    if(!Tokenizer.supportFor(fto.ln))
      throw new BaseXException(NO_TOKENIZER_X, fto.ln);
    if(meta.stemming && !Stemmer.supportFor(fto.ln))
      throw new BaseXException(NO_STEMMER_X, fto.ln);
    return new FTLexer(fto);
  }

  @Override
  public FTIndex build() throws IOException {
    Util.debugln(detailedInfo());

    try {
      // an updatable index is segmented and holds node IDs; if all IDs equal their PRE values,
      // the unsegmented layout is written, which older versions can read (see FTIndex#adopt)
      final MetaData meta = data.meta;
      final boolean segmented = ids && !FTIndex.unnumbered(data);
      if(segmented && size == 0) {
        // empty database: the first update will write the first segment
        meta.segments.put(IndexType.FULLTEXT, "");
      } else {
        build(0, size, segmented ? FTIndex.segment(0) : DATAFTX);
        if(segmented) meta.segments.put(IndexType.FULLTEXT, "0");
        else meta.segments.remove(IndexType.FULLTEXT);
      }
      finishIndex();
      return new FTIndex(data);
    } catch(final Throwable th) {
      // drop index files
      data.meta.drop(DATAFTX + ".*");
      throw th;
    }
  }

  /**
   * Indexes the units of the specified range and writes them to an index structure.
   * @param first PRE value of the first node
   * @param last PRE value of the last node (exclusive)
   * @param prefix file prefix of the index structure
   * @throws IOException I/O exception
   */
  void build(final int first, final int last, final String prefix) throws IOException {
    for(pre = first; pre < last; pre++) {
      if((pre & 0x0FFF) == 0) check();
      if(includeNames.unit(pre)) index(ids ? data.id(pre) : pre, data.atom(pre));
    }

    // write the index, or the last partial index, and merge all partial indexes
    if(splits == 0) {
      writeIndex(prefix);
    } else {
      writeIndex(partial(splits));
      final String[] inputs = new String[splits];
      for(int s = 0; s < splits; s++) inputs[s] = partial(s);
      merge(data, inputs, prefix);
      for(final String input : inputs) drop(data, input);
    }
  }

  /**
   * Passes the tokens of a value to be indexed, and their positions, to a consumer.
   * @param lexer lexer
   * @param maxlen maximum token length
   * @param value value
   * @param consumer consumer
   * @throws IOException I/O exception
   */
  static void tokens(final FTLexer lexer, final int maxlen, final byte[] value,
      final TokenConsumer consumer) throws IOException {
    final StopWords sw = lexer.ftOpt().sw;
    lexer.init(value);
    int pos = -1;
    while(lexer.hasNext()) {
      final byte[] token = lexer.nextToken();
      ++pos;
      // skip too long and stopword tokens
      if(token.length <= maxlen && !sw.contains(token)) consumer.accept(token, pos);
    }
  }

  /**
   * Consumer of indexed tokens.
   */
  interface TokenConsumer {
    /**
     * Accepts a token.
     * @param token token
     * @param pos position
     * @throws IOException I/O exception
     */
    void accept(byte[] token, int pos) throws IOException;
  }

  /**
   * Indexes the tokens of a value.
   * @param id ID or PRE value of the node
   * @param value value to be indexed
   * @throws IOException I/O exception
   */
  private void index(final int id, final byte[] value) throws IOException {
    tokens(lexer, data.meta.maxlen, value, (token, pos) -> {
      // check if main memory is exhausted
      if(splitTokens > 0 ? count > 0 && count % splitTokens == 0 :
        (count & 0xFFFF) == 0 && splitRequired(tree.memory())) {
        writeIndex(partial(splits));
        tree = new FTIndexTrees(data.meta.maxlen);
      }
      tree.index(token, id, pos);
      count++;
    });
  }

  /**
   * Returns the file prefix of a partial index.
   * @param split split counter
   * @return prefix
   */
  private static String partial(final int split) {
    return DATAFTX + "tmp" + split;
  }

  /**
   * Deletes the files of an index structure.
   * @param data data reference
   * @param prefix file prefix of the index structure
   */
  static void drop(final Data data, final String prefix) {
    data.meta.drop(prefix + '[' + FTSegment.SUFFIXES + ']');
  }

  /**
   * Merges partial index structures, keeping all references in input order.
   * @param data data reference
   * @param inputs file prefixes of the input index structures
   * @param output file prefix of the output index structure
   * @throws IOException I/O exception
   */
  private static void merge(final Data data, final String[] inputs, final String output)
      throws IOException {
    try(FTSegmentWriter writer = new FTSegmentWriter(data, output)) {
      SegmentedIndex.merge(inputs.length, i -> new FTList(data, inputs[i]), writer,
        FTIndex::compare, null);
    }
  }

  /**
   * Writes the current index to disk.
   * @param prefix file prefix of the index structure
   * @throws IOException I/O exception
   */
  private void writeIndex(final String prefix) throws IOException {
    try(FTSegmentWriter writer = new FTSegmentWriter(data, prefix)) {
      tree.init();
      while(tree.more()) {
        final IndexTree t = tree.nextTree();
        final int n = t.next();
        writer.write(t.keys.get(n), t.ids.get(n));
      }
    }

    // increase split counter
    splits++;
  }
}
