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
  private final FTIndexTrees tree;
  /** Word parser. */
  private final FTLexer lexer;
  /** Number of indexed tokens. */
  private long ntok;

  /**
   * Constructor.
   * @param data data reference
   * @throws IOException IOException
   */
  public FTBuilder(final Data data) throws IOException {
    super(data, IndexType.FULLTEXT);
    final MetaData meta = data.meta;
    tree = new FTIndexTrees(data.meta.maxlen);

    final FTOpt fto = new FTOpt();
    fto.set(FTFlag.DC, meta.diacritics);
    fto.set(FTFlag.ST, meta.stemming);
    fto.cs = meta.casesens ? FTCase.SENSITIVE : FTCase.INSENSITIVE;
    fto.sw = new StopWords(data, meta.stopwords);
    fto.ln = data.meta.language();

    // element names are required; wildcards are allowed (all elements on all levels)
    if(meta.ftmixed && meta.ftinclude.isEmpty())
      throw new BaseXException("% requires %.", MainOptions.FTMIXED.name(),
          MainOptions.FTINCLUDE.name());
    if(!Tokenizer.supportFor(fto.ln))
      throw new BaseXException(NO_TOKENIZER_X, fto.ln);
    if(meta.stemming && !Stemmer.supportFor(fto.ln))
      throw new BaseXException(NO_STEMMER_X, fto.ln);

    lexer = new FTLexer(fto);
  }

  @Override
  public FTIndex build() throws IOException {
    Util.debugln(detailedInfo());

    try {
      // index the string values of the included elements, or the values of text nodes
      final boolean mixed = data.meta.ftmixed;
      for(pre = 0; pre < size; ++pre) {
        if((pre & 0x0FFF) == 0) check();
        // atomized value of a text node is its own value
        if(mixed ? indexElement() : indexEntry()) index(pre, data.atom(pre));
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
        if((ntok++ & 0xFFFF) == 0 && splitRequired()) {
          writeIndex(partial(splits));
          clean();
        }
        tree.index(token, id, pos, splits);
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
    try(DataOutput outX = new DataOutput(data.meta.dbFile(output + 'x'));
        DataOutput outY = new DataOutput(data.meta.dbFile(output + 'y'));
        DataOutput outZ = new DataOutput(data.meta.dbFile(output + 'z'))) {

      final IntList ind = new IntList();

      // open all sorted lists
      final FTList[] v = new FTList[il];
      for(int b = 0; b < il; ++b) v[b] = new FTList(data, inputs[b]);

      final IntList list = new IntList();
      while(check(v)) {
        list.reset();
        int m = 0;
        list.add(m);
        // find next token to write on disk
        for(int i = 0; i < il; ++i) {
          if(m == i || v[i].token.length == 0) continue;
          final int l = v[i].token.length - v[m].token.length;
          final int d = compare(v[m].token, v[i].token);
          if(l < 0 || l == 0 && d > 0 || v[m].token.length == 0) {
            m = i;
            list.reset();
            list.add(m);
          } else if(d == 0 && v[i].token.length > 0) {
            list.add(i);
          }
        }

        if(ind.isEmpty() || ind.get(ind.size() - 2) < v[m].token.length) {
          ind.add(v[m].token.length);
          ind.add((int) outY.size());
        }

        // write token
        outY.writeBytes(v[m].token);
        // pointer on full-text data
        outY.write5(outZ.size());
        // merge and write data size
        outY.write4(merge(outZ, list, v));
      }
      writeInd(outX, ind, ind.get(ind.size() - 2) + 1, (int) outY.size());
    }
    for(final String input : inputs) {
      for(final char c : new char[] { 'x', 'y', 'z' }) data.meta.dbFile(input + c).delete();
    }
  }

  /**
   * Writes the token length index to disk.
   * @param outX output
   * @param il token length and offsets
   * @param ls last token length
   * @param lp last offset
   * @throws IOException I/O exception
   */
  private static void writeInd(final DataOutput outX, final IntList il, final int ls, final int lp)
      throws IOException {

    final int is = il.size();
    outX.writeNum(is >> 1);
    for(int i = 0; i < is; i += 2) {
      outX.writeNum(il.get(i));
      outX.write4(il.get(i + 1));
    }
    outX.writeNum(ls);
    outX.write4(lp);
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
      long dr = 0;
      int tr = 0, j = 0;
      while(tree.more(splits)) {
        final FTIndexTree t = tree.nextTree();
        t.next();
        final byte[] key = t.nextTok();

        if(j < key.length) {
          j = key.length;
          // write index and pointer on first token
          ind.add(j);
          ind.add(tr);
        }
        for(int i = 0; i < j; ++i) outY.write1(key[i]);
        // write pointer on full-text data
        outY.write5(dr);
        // write full-text data size (number of PRE values)
        outY.write4(t.nextNumPre());
        // write compressed PRE and POS arrays
        writeFTData(outZ, t.nextPres(), t.nextPoss());

        dr = outZ.size();
        tr = (int) outY.size();
      }
      writeInd(outX, ind, ++j, tr);
    }
    tree.initFT();

    // increase split counter
    splits++;
  }

  /**
   * Merges temporary indexes for the current token.
   * @param out full-text data
   * @param il array mapping
   * @param list full-text list
   * @return written size
   * @throws IOException I/O exception
   */
  private static int merge(final DataOutput out, final IntList il, final FTList[] list)
      throws IOException {

    final ByteList tbp = new ByteList().add(new byte[4]), tbo = new ByteList().add(new byte[4]);
    // merge full-text data of all sorted lists with the same token
    int s = 0;
    final int is = il.size();
    for(int j = 0; j < is; ++j) {
      final int m = il.get(j);
      for(final int p : list[m].prv) tbp.add(Num.num(p));
      for(final int p : list[m].pov) tbo.add(Num.num(p));
      s += list[m].size;
      list[m].next();
    }
    // write compressed PRE and POS arrays
    final byte[] pr = tbp.finish();
    Num.size(pr, pr.length);
    final byte[] po = tbo.finish();
    Num.size(po, po.length);

    // write full-text data
    writeFTData(out, pr, po);
    return s;
  }

  /**
   * Writes full-text data for a single token to disk.
   * Format: {@code score? pre1 pos1 pre2 pos2 ... (0 score)? pre...}
   * @param out DataOutput for disk access
   * @param vpre compressed PRE values
   * @param vpos compressed pos values
   * @throws IOException IOException
   */
  private static void writeFTData(final DataOutput out, final byte[] vpre, final byte[] vpos)
      throws IOException {

    int np = 4, pp = 4;
    final int ns = Num.size(vpre);
    while(np < ns) {
      // full-text data is stored here, with -scoreU, pre1, pos1, ...,
      // -scoreU, preU, posU
      for(final int l = np + Num.length(vpre, np); np < l; ++np) out.write(vpre[np]);
      for(final int l = pp + Num.length(vpos, pp); pp < l; ++pp) out.write(vpos[pp]);
    }
  }

  /**
   * Checks if any unprocessed PRE values are remaining.
   * @param lists lists
   * @return boolean
   */
  private static boolean check(final FTList[] lists) {
    for(final FTList list : lists) {
      if(list.token.length > 0) return true;
    }
    return false;
  }
}
