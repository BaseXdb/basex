package org.basex.index.ft;

import static org.basex.core.Text.*;
import static org.basex.data.DataText.*;
import static org.basex.util.Token.*;

import java.io.*;

import org.basex.core.*;
import org.basex.data.*;
import org.basex.index.*;
import org.basex.io.out.DataOutput;
import org.basex.util.*;
import org.basex.util.ft.*;
import org.basex.util.list.*;

/**
 * This class contains common methods for full-text index builders.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class FTBuilder extends IndexBuilder {
  /** Value trees. */
  private FTIndexTrees tree;
  /** Word parser. */
  private final FTLexer lexer;

  /**
   * Constructor.
   * @param data data reference
   * @throws IOException I/O exception
   */
  public FTBuilder(final Data data) throws IOException {
    super(data, IndexType.FULLTEXT);
    tree = new FTIndexTrees(data.meta.maxlen);
    lexer = lexer(data);
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
      // index the string values of the included elements, or the values of text nodes
      for(pre = 0; pre < size; pre++) {
        if((pre & 0x0FFF) == 0) check();
        // atomized value of a text node is its own value
        if(includeNames.unit(pre)) index(pre, data.atom(pre));
      }

      // write the index, or the last partial index, and merge all partial indexes
      if(splits == 0) {
        writeIndex(DATAFTX);
      } else {
        writeIndex(partial(splits));
        final String[] inputs = new String[splits];
        for(int s = 0; s < splits; s++) inputs[s] = partial(s);
        merge(inputs, DATAFTX);
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
   * Indexes the tokens of a value.
   * @param id ID of the value (currently, the PRE value)
   * @param value value to be indexed
   * @throws IOException I/O exception
   */
  private void index(final int id, final byte[] value) throws IOException {
    final StopWords sw = lexer.ftOpt().sw;
    lexer.init(value);
    int pos = -1;
    while(lexer.hasNext()) {
      final byte[] token = lexer.nextToken();
      ++pos;
      // skip too long and stopword tokens
      if(token.length <= data.meta.maxlen && !sw.contains(token)) {
        // check if main memory is exhausted
        if((count & 0xFFFF) == 0 && splitRequired(tree.memory())) {
          writeIndex(partial(splits));
          tree = new FTIndexTrees(data.meta.maxlen);
        }
        tree.index(token, id, pos);
        count++;
      }
    }
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
   * Merges index structures and deletes the input files.
   * @param inputs file prefixes of the input index structures
   * @param output file prefix of the output index structure
   * @throws IOException I/O exception
   */
  private void merge(final String[] inputs, final String output) throws IOException {
    final int il = inputs.length;
    final FTList[] lists = new FTList[il];
    try(DataOutput outX = new DataOutput(data.meta.dbFile(output + 'x'));
        DataOutput outY = new DataOutput(data.meta.dbFile(output + 'y'));
        DataOutput outZ = new DataOutput(data.meta.dbFile(output + 'z'))) {

      // open all sorted lists
      for(int l = 0; l < il; l++) lists[l] = new FTList(data, inputs[l]);

      final IntList ind = new IntList(), same = new IntList();
      while(true) {
        // find next token to write to disk: shortest token first, then smallest token
        byte[] token = null;
        same.reset();
        for(int l = 0; l < il; l++) {
          final byte[] tk = lists[l].token;
          if(tk.length == 0) continue;
          final int d = token == null ? -1 : tk.length != token.length ?
            tk.length - token.length : compare(tk, token);
          if(d < 0) {
            token = tk;
            same.reset();
          }
          if(d <= 0) same.add(l);
        }
        if(token == null) break;

        if(ind.isEmpty() || ind.get(ind.size() - 2) < token.length) {
          ind.add(token.length);
          ind.add((int) outY.size());
        }

        // write token
        outY.writeBytes(token);
        // pointer on full-text data
        outY.write5(outZ.size());
        // merge full-text data of all sorted lists with the same token
        int s = 0;
        final int ss = same.size();
        for(int l = 0; l < ss; l++) {
          final FTList list = lists[same.get(l)];
          final int[] prv = list.prv, pov = list.pov;
          final int pl = prv.length;
          for(int p = 0; p < pl; p++) {
            outZ.writeNum(prv[p]);
            outZ.writeNum(pov[p]);
          }
          s += pl;
          list.next();
        }
        // write data size
        outY.write4(s);
      }
      writeInd(outX, ind);
    } finally {
      for(final FTList list : lists) {
        if(list != null) list.close();
      }
    }
    for(final String input : inputs) {
      for(final char c : new char[] { 'x', 'y', 'z' }) data.meta.dbFile(input + c).delete();
    }
  }

  /**
   * Writes the token length index to disk.
   * @param outX output
   * @param il token length and offsets
   * @throws IOException I/O exception
   */
  private static void writeInd(final DataOutput outX, final IntList il) throws IOException {
    final int is = il.size();
    outX.writeNum(is / 2);
    for(int i = 0; i < is; i += 2) {
      outX.writeNum(il.get(i));
      outX.write4(il.get(i + 1));
    }
  }

  /**
   * Writes the current index to disk.
   * @param prefix file prefix of the index structure
   * @throws IOException I/O exception
   */
  private void writeIndex(final String prefix) throws IOException {
    try(DataOutput outX = new DataOutput(data.meta.dbFile(prefix + 'x'));
        DataOutput outY = new DataOutput(data.meta.dbFile(prefix + 'y'));
        DataOutput outZ = new DataOutput(data.meta.dbFile(prefix + 'z'))) {

      final IntList ind = new IntList();
      tree.init();
      int j = 0;
      while(tree.more()) {
        final IndexTree t = tree.nextTree();
        final int n = t.next();
        final byte[] key = t.keys.get(n), ids = t.ids.get(n);

        if(j < key.length) {
          j = key.length;
          // write index and pointer on first token
          ind.add(j);
          ind.add((int) outY.size());
        }
        outY.writeBytes(key);
        // write pointer on full-text data
        outY.write5(outZ.size());
        // write full-text data size (number of PRE values)
        outY.write4(entries(ids));
        // write compressed PRE and POS values: pre1 pos1 pre2 pos2 ...
        outZ.write(ids, 4, Num.size(ids) - 4);
      }
      writeInd(outX, ind);
    }

    // increase split counter
    splits++;
  }

  /**
   * Returns the number of PRE values in the specified compressed PRE and POS values.
   * @param ids compressed PRE and POS values
   * @return number of PRE values
   */
  private static int entries(final byte[] ids) {
    int n = 0, i = 4;
    final int is = Num.size(ids);
    while(i < is) {
      i += Num.length(ids, i);
      i += Num.length(ids, i);
      n++;
    }
    return n;
  }
}
