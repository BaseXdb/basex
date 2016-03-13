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
 * @author BaseX Team 2005-16, BSD License
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
    fto.ln = data.meta.language;

    if(!Tokenizer.supportFor(fto.ln))
      throw new BaseXException(NO_TOKENIZER_X, fto.ln);
    if(meta.stemming && !Stemmer.supportFor(fto.ln))
      throw new BaseXException(NO_STEMMER_X, fto.ln);

    lexer = new FTLexer(fto);
  }

  @Override
  public FTIndex build() throws IOException {
    Util.debug(det());

    for(pre = 0; pre < size; ++pre) {
      if((pre & 0x0FFF) == 0) check();
      if(!indexEntry()) continue;

      // current lexer position
      final StopWords sw = lexer.ftOpt().sw;
      lexer.init(data.text(pre, true));
      int pos = -1;
      while(lexer.hasNext()) {
        final byte[] tok = lexer.nextToken();
        ++pos;
        // skip too long and stopword tokens
        if(tok.length <= data.meta.maxlen && (sw.isEmpty() || !sw.contains(tok))) {
          // check if main memory is exhausted
          if((ntok++ & 0xFFFF) == 0 && splitRequired()) {
            writeIndex(true);
            clean();
          }
          tree.index(tok, pre, pos, splits);
          count++;
        }
      }
    }

    // finalize partial or all index structures
    write(splits > 0);

    finishIndex();
    return new FTIndex(data);
  }

  /**
   * Writes the index data to disk.
   * @param partial write partial index
   * @throws IOException I/O exception
   */
  private void write(final boolean partial) throws IOException {
    writeIndex(partial);
    if(!partial) return;

    // merges temporary index files
    try(final DataOutput outX = new DataOutput(data.meta.dbfile(DATAFTX + 'x'));
        final DataOutput outY = new DataOutput(data.meta.dbfile(DATAFTX + 'y'));
        final DataOutput outZ = new DataOutput(data.meta.dbfile(DATAFTX + 'z'))) {

      final IntList ind = new IntList();

      // open all temporary sorted lists
      final FTList[] v = new FTList[splits];
      for(int b = 0; b < splits; ++b) v[b] = new FTList(data, b);

      final IntList il = new IntList();
      while(check(v)) {
        il.reset();
        int m = 0;
        il.add(m);
        // find next token to write on disk
        for(int i = 0; i < splits; ++i) {
          if(m == i || v[i].tok.length == 0) continue;
          final int l = v[i].tok.length - v[m].tok.length;
          final int d = diff(v[m].tok, v[i].tok);
          if(l < 0 || l == 0 && d > 0 || v[m].tok.length == 0) {
            m = i;
            il.reset();
            il.add(m);
          } else if(d == 0 && v[i].tok.length > 0) {
            il.add(i);
          }
        }

        if(ind.isEmpty() || ind.get(ind.size() - 2) < v[m].tok.length) {
          ind.add(v[m].tok.length);
          ind.add((int) outY.size());
        }

        // write token
        outY.writeBytes(v[m].tok);
        // pointer on full-text data
        outY.write5(outZ.size());
        // merge and write data size
        outY.write4(merge(outZ, il, v));
      }
      writeInd(outX, ind, ind.get(ind.size() - 2) + 1, (int) outY.size());
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
  private static void writeInd(final DataOutput outX, final IntList il,
      final int ls, final int lp) throws IOException {

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
   * @param partial partial flag
   * @throws IOException I/O exception
   */
  private void writeIndex(final boolean partial) throws IOException {
    final String name = DATAFTX + (partial ? splits : "");
    try(final DataOutput outX = new DataOutput(data.meta.dbfile(name + 'x'));
        final DataOutput outY = new DataOutput(data.meta.dbfile(name + 'y'));
        final DataOutput outZ = new DataOutput(data.meta.dbfile(name + 'z'))) {

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
        // write full-text data size (number of pre values)
        outY.write4(t.nextNumPre());
        // write compressed pre and pos arrays
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
   * @param v full-text list
   * @return written size
   * @throws IOException I/O exception
   */
  private static int merge(final DataOutput out, final IntList il, final FTList[] v)
      throws IOException {

    final TokenBuilder tbp = new TokenBuilder();
    final TokenBuilder tbo = new TokenBuilder();
    tbp.add(new byte[4]);
    tbo.add(new byte[4]);
    // merge full-text data of all sorted lists with the same token
    int s = 0;
    final int is = il.size();
    for(int j = 0; j < is; ++j) {
      final int m = il.get(j);
      for(final int p : v[m].prv) tbp.add(Num.num(p));
      for(final int p : v[m].pov) tbo.add(Num.num(p));
      s += v[m].size;
      v[m].next();
    }
    // write compressed pre and pos arrays
    final byte[] pr = tbp.finish();
    Num.size(pr, pr.length);
    final byte[] po = tbo.finish();
    Num.size(po, po.length);

    // write full-text data
    writeFTData(out, pr, po);
    return s;
  }

  /**
   * Writes full-text data for a single token to disk.<br/>
   * Format: {@code score? pre1 pos1 pre2 pos2 ... (0 score)? pre...}
   * @param out DataOutput for disk access
   * @param vpre compressed pre values
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
   * Checks if any unprocessed pre values are remaining.
   * @param lists lists
   * @return boolean
   */
  private static boolean check(final FTList[] lists) {
    for(final FTList l : lists) if(l.tok.length > 0) return true;
    return false;
  }

  @Override
  protected void abort() {
    // drop index files
    data.meta.drop(DATAFTX + ".*");
  }
}
