package org.basex.index;

import static org.basex.data.DataText.*;
import static org.basex.util.Token.*;
import java.io.IOException;
import java.io.RandomAccessFile;
import org.basex.core.proc.DropDB;
import org.basex.data.Data;
import org.basex.io.DataAccess;
import org.basex.io.DataOutput;
import org.basex.io.IO;
import org.basex.util.IntArrayList;
import org.basex.util.IntList;
import org.basex.util.Num;
import org.basex.util.Performance;
import org.basex.util.TokenBuilder;
import org.basex.util.TokenList;

/**
 * This class builds an index for text contents in a compressed trie.
 *  - (1) the tokens are collected in main memory (hash map)
 *  - (2) if main memory is full, the data is written as sorted list to disk(1)
 *  - (3) merge disk data trough reading sorted lists
 *  - (4) write sorted list (merged) to disk
 *  - (5) create final trie structure out of it (4) with the following final
 *        format:
 *
 * The data is stored on disk with the following format:
 *
 * data.meta.file(DATAFTX + 'b'): stores the full text data; the pre values
 *  are ordered but not distinct
 *  [pre1, pos1, pre2, pos2, pre3, pos3, ...] as Nums
 *
 * data.meta.file(DATAFTX + 't'): stores the trie structure, each node has
 *  the following format:
 *  [l, t1, ..., tl, n1, v1, ..., nu, vu, s, p]
 *    l = length of the token t1, ..., tl [byte]
 *    u = number of next nodes n1, ..., nu
 *    v1= the first byte of each token n1 points, ... [byte]
 *    s = size of pre values [int] saved at pointer p [long]
 *
 * data.meta.file(DATAFTX + 'c'): stores the size of each trie node
 *  [size0, size1, size2, ..., sizeN]
 *  size is an int value
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Sebastian Gath
 * @author Christian Gruen
 */
final class FTTrieBuilder extends FTBuilder {
  /** Trie index. */
  private FTArray index = new FTArray(128);
  /** Hash structure for temporarily saving the tokens. */
  private FTHash hash = new FTHash();
  /** Offset for joining subtrees. */
  private int o;

  /**
   * Constructor.
   * @param d data reference
   * @throws IOException IOException
   */
  protected FTTrieBuilder(final Data d) throws IOException {
    super(d);
  }

  @Override
  public FTIndex build() throws IOException {
    maxMem = (long) (maxMem * 0.9);
    index();
    return new FTTrie(data);
  }

  @Override
  void index(final byte[] tok) throws IOException {
    // until there's enough free main memory
    if((ntok & 0xFFF) == 0 && scm == 0 && memFull()) {
      // currently no frequency support for tfidf based scoring
      // write data temporarily as sorted list
      writeSortedList(csize++);
      Performance.gc(2);
    }
    hash.index(tok, pre, wp.pos);
    ntok++;
  }

  @Override
  int nrTokens() {
    return hash.size();
  }

  @Override
  void getFreq() {
    hash.init();
    while(hash.more()) getFreq(hash.pre[hash.next()]);
  }

  @Override
  public void write() throws IOException {
    if(!merge) {
      // any subtries to merge, i.e. write complete trie
      writeCompleteTrie();
      return;
    }

    // the temporarily stored data has to be merged
    // first merge all sorted list to one sorted list
    // write full text data to disk
    // create trie index structure
    writeSortedList(csize++);
    final DataOutput outb = new DataOutput(data.meta.file(DATAFTX + 'b'));
    final DataOutput outt = new DataOutput(data.meta.file(DATAFTX + 't'));
    final IntList root = new IntList();

    final byte[][] tok = new byte[csize][];
    final int[][] pres = new int[csize][];
    final int[][] pos = new int[csize][];

    // open all temp sorted lists
    final FTSortedList[] v = new FTSortedList[csize];
    for(int b = 0; b < csize; b++) {
      v[b] = new FTSortedList(data, b);
      tok[b] = v[b].nextTok();
      pres[b] = v[b].nextPreValues();
      pos[b] = v[b].nextPosValues();
    }

    int min;
    final IntList mert = new IntList();

    while(check(tok)) {
      min = 0;
      mert.reset();
      mert.add(min);
      // find next token to write on disk
      for(int i = 0; i < csize; i++) {
        if(min == i || tok[i].length == 0) continue;
        final int d = diff(tok[min], tok[i]);
        if(d > 0 || tok[min].length == 0) {
          min = i;
          mert.reset();
          mert.add(min);
        } else if(d == 0 && tok[i].length > 0) {
          mert.add(i);
        }
      }

      // collect each child of the root node
      if (root.size() == 0 || root.get(root.size() - 1) != tok[min][0])
        root.add(tok[min][0]);
      // write token length to disk
      outt.write(tok[min].length);
      // write token to disk
      outt.write(tok[min]);

      int s = 0;
      final TokenBuilder tbp = new TokenBuilder();
      final TokenBuilder tbo = new TokenBuilder();
      tbp.add(new byte[4]);
      tbo.add(new byte[4]);
      // merge full text data of all sorted lists with the same token
      for(int j = 0; j < mert.size(); j++) {
        final int m = mert.get(j);
        for(final int p : pres[m]) tbp.add(Num.num(p));
        for(final int p : pos[m]) tbo.add(Num.num(p));
        s += v[m].nextFTDataSize();
        tok[m] = nextToken(v, m);
        pres[m] = tok[m].length > 0 ? v[m].nextPreValues() : new int[0];
        pos[m] = tok[m].length > 0 ? v[m].nextPosValues() : new int[0];
      }

      // write out data size
      outt.writeInt(s);
      // write out pointer on full text data
      outt.write5(outb.size());

      // write compressed pre and pos arrays
      final byte[] p = tbp.finish();
      Num.size(p, p.length);
      final byte[] op = tbo.finish();
      Num.size(op, op.length);
      // write full text data
      writeFTData(outb, p, op);
    }
    outt.write(0);
    outt.close();
    outb.close();
    // write trie index structure to disk, splitted in subtrees
    writeSplitTrie(root);
  }

  /**
   * Returns next token.
   * @param v FTFuzzy Array
   * @param m pointer on current FTFuzzy
   * @return next token
   * @throws IOException I/O exception
   */
  private byte[] nextToken(final FTSortedList[] v, final int m)
      throws IOException {

    if(v[m] != null) {
      final byte[] tok = v[m].nextTok();
      if(tok.length > 0) return tok;
      v[m].close();
      v[m] = null;
    }
    return EMPTY;
  }

  /**
   * Write trie structure to disk, splitted in subtrees to safe memory.
   * @param roots root nodes
   * @throws IOException I/O exception
   */
  private void writeSplitTrie(final IntList roots) throws IOException {
    final DataAccess t = new DataAccess(data.meta.file(DATAFTX + 't'));
    final DataOutput outN = new DataOutput(data.meta.file(DATAFTX + 'a'));
    final DataOutput outS = new DataOutput(data.meta.file(DATAFTX + 'c'));
    final int[] root = new int[roots.size()];
    int rp = 0;

    // write tmp root node first
    outN.write((byte) 1);
    outN.write((byte) -1);
    // write tmp next pointer
    for(int j = 0; j < roots.size(); j++) {
     outN.writeInt(0); // dummy pointer
     outN.write(roots.get(j)); // first char of next node
    }
    outN.writeInt(0); // data size
    outN.write5(-1); // pointer on data - root has no data
    outS.writeInt(0);
    int siz = (int) (2L + roots.size() * 5L + 9L);

    byte tl = t.read1();
    while (tl > 0) {
      final long pos = t.pos();
      final byte[] tok = t.readBytes(pos, pos + tl);
      final int s = t.read4();
      final long off = t.read5();
      if (rp < roots.size() && tok[0] != roots.get(rp)) {
        // write subtree to disk
        siz = writeSubTree(root, outN, outS, rp, siz);
        rp++;
        index = new FTArray(128);
      }
      index.insertSorted(tok, s, off);
      tl = t.read1();
    }

    // write subtree to disk
    writeSubTree(root, outN, outS, rp, siz);

    t.close();
    outN.close();
    outS.close();

    // finally update root node
    final RandomAccessFile a =
      new RandomAccessFile(data.meta.file(DATAFTX + 'a'), "rw");
    a.seek(2);
    for(final int element : root) {
      a.writeInt(element);
      a.seek(a.getFilePointer() + 1);
    }
    a.close();
    DropDB.delete(data.meta.name, DATAFTX + "\\d+." + IO.BASEXSUFFIX,
    data.meta.prop);
  }

  /**
   * Writes subtree to disk.
   * @param root Array with root offsets
   * @param outN DataOutput
   * @param outS DataOutput
   * @param rp pointer on root offsets
   * @param siz size
   * @return new size
   * @throws IOException I/O exception
   */
  private int writeSubTree(final int[] root, final DataOutput outN,
      final DataOutput outS, final int rp, final int siz) throws IOException {
    int s = siz;
    // indexed full text tokens
    final TokenList tokens = index.tokens;
    // trie index structure
    final IntArrayList next = index.next;
    if (root != null) root[rp] = next.get(0)[1] + o;

    final int il = next.size();
    // loop over all trie nodes
    for(int i = 1; i < il; i++) {
      final int[] nxt = next.get(i);
      // check pointer on data needs 1 or 2 ints
      final int lp = nxt[nxt.length - 1] > -1 ? 0 : -1;
      // write token size as byte
      outN.write((byte) tokens.get(nxt[0]).length);
      // write token
      outN.write(tokens.get(nxt[0]));
      // write next pointer
      int j = 1;
      for(; j < nxt.length - 2 + lp; j++) {
        outN.writeInt(nxt[j] + o); // pointer
        // first char of next node
        outN.write(tokens.get(next.get(nxt[j])[0])[0]);
      }
      outN.writeInt(nxt[j]); // data size
      if(nxt[j] == 0 && nxt[j + 1] == 0) {
        // node has no data
        outN.write5(nxt[j + 1]);
      } else {
        // write pointer on data
        if(lp == 0) {
          outN.write5(nxt[j + 1]);
        } else {
          outN.write5(toLong(nxt, nxt.length - 2));
        }
      }
      outS.writeInt(s);
      s += 1L + tokens.get(nxt[0]).length * 1L +
        (nxt.length - 3 + lp) * 5L + 9L;
    }
    o += next.size() - 1;

    return s;
  }

  /**
   * Write trie structure to disk.
   * @throws IOException I/O exception
   */
  private void writeCompleteTrie() throws IOException {
    if(scm == 0) hash.init();
    else hash.initIter();
    final DataOutput outb = new DataOutput(data.meta.file(DATAFTX + 'b'));
    while(hash.more()) {
      final int p = hash.next();
      final byte[] tok = hash.key();
      final int ds = hash.ns[p];
      final long cpre = outb.size();
      // write compressed pre and pos arrays
      writeFTData(outb, hash.pre[p], hash.pos[p]);
      index.insertSorted(tok, ds, cpre);
    }
    outb.close();
    hash = null;

    final TokenList tokens = index.tokens;
    final IntArrayList next = index.next;

    final DataOutput outN = new DataOutput(data.meta.file(DATAFTX + 'a'));
    final DataOutput outS = new DataOutput(data.meta.file(DATAFTX + 'c'));

    // document contains any text nodes -> empty index created;
    // only root node is kept
    int siz = 0;
    if(index.count != 1) {
      // first root node
      // write token size as byte
      outN.write((byte) 1);
      // write token
      outN.write((byte) -1);
      // write next pointer
      int j = 1;
      final int js = next.get(0).length - 2;
      for(; j < js; j++) {
        outN.writeInt(next.get(0)[j]); // pointer
        // first char of next node
        outN.write(tokens.get(next.get(next.get(0)[j])[0])[0]);
      }

      outN.writeInt(next.get(0)[j]); // data size
      outN.write5(-1); // pointer on data - root has no data
      outS.writeInt(siz);
      siz += 2L + (next.get(0).length - 3) * 5L + 9L;
      // all other nodes
      siz = writeSubTree(null, outN, outS, 0, siz);
      outN.close();
      outS.close();
    }
    DropDB.delete(data.meta.name, DATAFTX + "\\d+." + IO.BASEXSUFFIX,
    data.meta.prop);
  }

  /**
   * Write data as sorted list on disk.
   * The data is stored in two files:
   * 'a': length|byte|,token|byte[length]|, size|int|, offset|long|, ...
   * 'b': used method writeFTData
   * @param cs current file
   * @throws IOException I/O exception
   */
  private void writeSortedList(final int cs) throws IOException {
    final String s = DATAFTX + (merge ? Integer.toString(cs) : "");
    final DataOutput outa = new DataOutput(data.meta.file(s + 'a'));
    final DataOutput outb = new DataOutput(data.meta.file(s + 'b'));

    if(scm == 0) hash.init();
    else hash.initIter();
    while(hash.more()) {
      final int p = hash.next();
      final byte[] tok = hash.key();
      final int ds = hash.ns[p];
      // write compressed pre and pos arrays
      writeFTData(outb, hash.pre[p], hash.pos[p]);
      // write token length
      outa.write1(tok.length);
      // write token
      outa.write(tok);
      // write number of full text data size
      outa.writeInt(ds);
      // write pointer on full text data
      outa.write5(outb.size());
    }

    outa.write1(0);
    hash = new FTHash();
    outb.close();
    outa.close();
  }

  /**
   * Converts long values split with toArray back.
   * @param ar int[] with values
   * @param p pointer where the first value is found
   * @return long l
   */
  private static long toLong(final int[] ar, final int p) {
    long l = (long) ar[p] << 16;
    l += -ar[p + 1] & 0xFFFF;
    return l;
  }
}
