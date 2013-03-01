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
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class FTBuilder extends IndexBuilder {
  /** Value trees. */
  private final FTIndexTrees tree;
  /** Word parser. */
  private final FTLexer lex;
  /** Current lexer position. */
  int pos;
  /** Number of indexed tokens. */
  private long ntok;
  /** Counter variable for check against data.meta.ftIndSliceSize. */
  private int currentSliceSize;

  /**
   * Constructor.
   * @param d data reference
   * @throws IOException IOException
   */
  public FTBuilder(final Data d) throws IOException {
    super(d);
    tree = new FTIndexTrees(d.meta.maxlen);

    final Prop prop = d.meta.prop;
    final FTOpt fto = new FTOpt();
    fto.set(FTFlag.DC, prop.is(Prop.DIACRITICS));
    fto.set(FTFlag.CS, prop.is(Prop.CASESENS));
    fto.set(FTFlag.ST, prop.is(Prop.STEMMING));
    fto.sw = new StopWords(d, prop.get(Prop.STOPWORDS));
    fto.ln = Language.get(prop);

    if(!Tokenizer.supportFor(fto.ln))
      throw new BaseXException(NO_TOKENIZER_X, fto.ln);
    if(prop.is(Prop.STEMMING) && !Stemmer.supportFor(fto.ln))
      throw new BaseXException(NO_STEMMER_X, fto.ln);

    lex = new FTLexer(fto);
  }

  /**
   * Extracts and indexes words from the specified data reference.
   * @throws IOException I/O Exception
   */
  private void index() throws IOException {
    // delete old index
    abort();

    final Performance perf = Prop.debug ? new Performance() : null;
    Util.debug(det());
    currentSliceSize = 0;

    for(pre = 0; pre < size; ++pre) {
      if((pre & 0xFFFF) == 0) check();

      final int k = data.kind(pre);
      if(k != Data.TEXT) continue;

      pos = -1;
      final StopWords sw = lex.ftOpt().sw;
      lex.init(data.text(pre, true));
      while(lex.hasNext()) {
        final byte[] tok = lex.nextToken();
        ++pos;
        // skip too long and stopword tokens
        if(tok.length <= data.meta.maxlen && (sw.isEmpty() || !sw.contains(tok))) {
          // check if main memory is exhausted
          if((ntok++ & 0xFFF) == 0 && temporaryFlushToDiskNeeded()) {
            writeIndex(csize++);
            memoryCleanupAfterFlushToDisk();
          }
          index(tok);
        }
      }
    }

    // write tokens
    write();

    data.meta.ftxtindex = true;
    Util.memory(perf);
  }

  /**
   * Decides whether in-memory temporary index structures are so large
   * that we must flush them to disk before continuing.
   * @return true if structures shall be flushed to disk
   * @throws IOException I/O Exception
   */
  private boolean temporaryFlushToDiskNeeded() throws IOException {
    if (data.meta.ftIndSliceSize > 0) {
      merge = true;
      return currentSliceSize >= data.meta.ftIndSliceSize;
    }
    return memFull();
  }

  /**
   * Performs memory cleanup after flusing to disk, if necessary.
   */
  private void memoryCleanupAfterFlushToDisk() {
    if (data.meta.ftIndSliceSize > 0) {
      currentSliceSize = 0;
    } else {
      Performance.gc(singlegc ? 1 : 2);
    }
  }


  @Override
  public FTIndex build() throws IOException {
    index();
    return new FTIndex(data);
  }

  /**
   * Indexes a single token.
   * @param tok token to be indexed
   */
  void index(final byte[] tok) {
    tree.index(tok, pre, pos, csize);
    currentSliceSize++;
  }

  /**
   * Writes the index data to disk.
   * @throws IOException I/O exception
   */
  public void write() throws IOException {
    writeIndex(csize++);
    Util.debug("Finalizing FTIndex " + data.meta.name + " with " + csize +
        " slices, current slice size = " + currentSliceSize);
    if(!merge) return;

    // merges temporary index files
    final DataOutput outX = new DataOutput(data.meta.dbfile(DATAFTX + 'x'));
    final DataOutput outY = new DataOutput(data.meta.dbfile(DATAFTX + 'y'));
    final DataOutput outZ = new DataOutput(data.meta.dbfile(DATAFTX + 'z'));
    final IntList ind = new IntList();

    // open all temporary sorted lists
    final FTList[] v = new FTList[csize];
    for(int b = 0; b < csize; ++b) v[b] = new FTList(data, b);

    final IntList il = new IntList();
    while(check(v)) {
      int m = 0;
      il.reset();
      il.add(m);
      // find next token to write on disk
      for(int i = 0; i < csize; ++i) {
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

    outX.close();
    outY.close();
    outZ.close();
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
   * @param cs current file pointer
   * @throws IOException I/O exception
   */
  protected void writeIndex(final int cs) throws IOException {
    final String s = DATAFTX + (merge ? cs : "");
    final DataOutput outX = new DataOutput(data.meta.dbfile(s + 'x'));
    final DataOutput outY = new DataOutput(data.meta.dbfile(s + 'y'));
    final DataOutput outZ = new DataOutput(data.meta.dbfile(s + 'z'));

    final IntList ind = new IntList();
    long dr = 0;
    int tr = 0;
    int j = 0;
    tree.init();
    while(tree.more(cs)) {
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

    outX.close();
    outY.close();
    outZ.close();
    tree.initFT();
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

    int s = 0;
    final TokenBuilder tbp = new TokenBuilder();
    final TokenBuilder tbo = new TokenBuilder();
    tbp.add(new byte[4]);
    tbo.add(new byte[4]);
    // merge full-text data of all sorted lists with the same token
    for(int j = 0; j < il.size(); ++j) {
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
  private static void writeFTData(final DataOutput out, final byte[] vpre,
                                  final byte[] vpos) throws IOException {

    int np = 4, pp = 4;
    final int ns = Num.size(vpre);
    while(np < ns) {
      // full-text data is stored here, with -scoreU, pre1, pos1, ...,
      // -scoreU, preU, posU
      for(final int l = np + Num.length(vpre, np); np < l; ++np)
        out.write(vpre[np]);
      for(final int l = pp + Num.length(vpos, pp); pp < l; ++pp)
        out.write(vpos[pp]);
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
  public void abort() {
    data.meta.drop(DATAFTX + ".*");
    data.meta.ftxtindex = false;
  }

  @Override
  protected String det() {
    return INDEX_FULLTEXT_D;
  }
}
