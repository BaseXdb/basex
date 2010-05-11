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
import org.basex.util.Performance;
import org.basex.util.TokenList;

/**
 * This class builds an index for text contents in a compressed trie.
 * <ol>
 * <li> the tokens are collected in main memory (hash map)</li>
 * <li> if main memory is full, data is written as sorted list to disk (1)</li>
 * <li> merge disk data trough reading sorted lists</li>
 * <li> write sorted list (merged) to disk</li>
 * <li> create final trie structure out of it (4) with the following final
 *        format:<br/>
 *
 * The data is stored on disk with the following format:<br/>
 *
 * {@code DATAFTX + 'a'}: stores the trie structure in the following format:
 * <br/>
 *  [l, t1, ..., tl, n1, v1, ..., nu, vu, s, p]<br/>
 *    l  = length of the token t1, ..., tl [byte]<br/>
 *    u  = number of next nodes n1, ..., nu<br/>
 *    v1 = the first byte of each token n1 points, ... [byte]<br/>
 *    s  = size of pre values [int] saved at pointer p [long]<br/>
 *
 * {@code DATAFTX + 'b'}: stores the full-text data; the pre values are ordered,
 *  but not distinct<br/>
 *  [pre1, pos1, pre2, pos2, pre3, pos3, ...] as Nums<br/>
 *
 * {@code DATAFTX + 'c'}: stores the size of each trie node<br/>
 *  [size0, size1, size2, ..., sizeN]<br/>
 *  size is an int value
 * </li>
 * </ol>
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
  private int offset;

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
      writeIndex(csize++);
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
      writeAll();
      return;
    }

    // merges temporary index files
    writeIndex(csize++);
    final DataOutput outb = new DataOutput(data.meta.file(DATAFTX + 'b'));
    final DataOutput tmp  = new DataOutput(data.meta.file(DATAFTX + 't'));
    final IntList ind = new IntList();

    // open all temporary sorted lists
    final FTList[] v = new FTList[csize];
    for(int b = 0; b < csize; b++) v[b] = new FTTrieList(data, b);

    final IntList il = new IntList();
    while(check(v)) {
      int min = 0;
      il.reset();
      il.add(min);
      // find next token to write on disk
      for(int i = 0; i < csize; i++) {
        if(min == i || v[i].tok.length == 0) continue;
        final int d = diff(v[min].tok, v[i].tok);
        if(d > 0 || v[min].tok.length == 0) {
          min = i;
          il.reset();
          il.add(min);
        } else if(d == 0 && v[i].tok.length > 0) {
          il.add(i);
        }
      }

      // collect each child of the root node
      if(ind.size() == 0 || ind.get(ind.size() - 1) != v[min].tok[0]) {
        ind.add(v[min].tok[0]);
      }

      // write token to disk
      tmp.writeToken(v[min].tok);

      // merge and write out data size
      int s = merge(outb, il, v);
      tmp.write4(s);
      // write out pointer on full-text data
      tmp.write5(outb.size());
    }

    tmp.writeToken(EMPTY);
    tmp.close();
    outb.close();
    // write trie index structure to disk, split in subtrees
    writeSplitTrie(ind);
  }

  /**
   * Write trie structure to disk.
   * @throws IOException I/O exception
   */
  private void writeAll() throws IOException {
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

    final DataOutput outa = new DataOutput(data.meta.file(DATAFTX + 'a'));
    final DataOutput outc = new DataOutput(data.meta.file(DATAFTX + 'c'));

    // first root node
    // write token size as byte
    outa.write1(1);
    // write token
    outa.write1(-1);
    // write next pointer
    int j = 1;
    final int js = next.get(0).length - 2;
    for(; j < js; j++) {
      // pointer
      final int p = next.get(0)[j];
      outa.write4(p);
      // first char of next node
      outa.write1(tokens.get(next.get(p)[0])[0]);
    }
    outa.write4(next.get(0)[j]); // data size
    outa.write5(-1); // pointer on data - root has no data
    outc.write4(0);

    final int siz = (next.get(0).length - 3) * 5 + 11;
    // all other nodes
    writeSubTree(null, outa, outc, 0, siz);

    outa.close();
    outc.close();
  }

  /**
   * Write trie structure to disk, split in subtrees to save memory.
   * @param roots root nodes
   * @throws IOException I/O exception
   */
  private void writeSplitTrie(final IntList roots) throws IOException {
    final DataOutput outa = new DataOutput(data.meta.file(DATAFTX + 'a'));
    final DataOutput outc = new DataOutput(data.meta.file(DATAFTX + 'c'));
    final DataAccess tmp  = new DataAccess(data.meta.file(DATAFTX + 't'));
    final int[] root = new int[roots.size()];
    int rp = 0;

    // write tmp root node first
    outa.write1(1);
    outa.write1(-1);
    // write tmp next pointer
    for(int j = 0; j < roots.size(); j++) {
     outa.write4(0); // dummy pointer
     outa.write1(roots.get(j)); // first char of next node
    }
    outa.write4(0); // data size
    outa.write5(-1); // pointer on data - root has no data
    outc.write4(0);
    int siz = (int) (2L + roots.size() * 5L + 9L);

    while(true) {
      final byte[] tok = tmp.readToken();
      if(tok.length == 0) break;
      final int s = tmp.read4();
      final long off = tmp.read5();
      if(rp < roots.size() && tok[0] != roots.get(rp)) {
        // write subtree to disk
        siz = writeSubTree(root, outa, outc, rp, siz);
        rp++;
        index = new FTArray(128);
      }
      index.insertSorted(tok, s, off);
    }

    // write subtree to disk
    writeSubTree(root, outa, outc, rp, siz);

    tmp.close();
    outa.close();
    outc.close();

    // finally update root node
    final RandomAccessFile a =
      new RandomAccessFile(data.meta.file(DATAFTX + 'a'), "rw");
    a.seek(2);
    for(final int r : root) {
      a.writeInt(r);
      a.seek(a.getFilePointer() + 1);
    }
    a.close();
    DropDB.delete(data.meta.name, DATAFTX + 't' + IO.BASEXSUFFIX,
        data.meta.prop);
  }

  /**
   * Writes subtree to disk.
   * @param root Array with root offsets
   * @param outa trie structure
   * @param outc node sizes
   * @param rp pointer on root offsets
   * @param siz size
   * @return new size
   * @throws IOException I/O exception
   */
  private int writeSubTree(final int[] root, final DataOutput outa,
      final DataOutput outc, final int rp, final int siz) throws IOException {

    // indexed full-text tokens
    final TokenList tokens = index.tokens;
    // trie index structure
    final IntArrayList next = index.next;
    if(root != null) root[rp] = next.get(0)[1] + offset;

    int s = siz;
    final int il = next.size();
    // loop over all trie nodes
    for(int i = 1; i < il; i++) {
      final int[] nxt = next.get(i);
      // check pointer on data needs 1 or 2 ints
      final int lp = nxt[nxt.length - 1] >= 0 ? 0 : -1;
      // write token size as byte
      outa.write1(tokens.get(nxt[0]).length);
      // write token
      outa.writeBytes(tokens.get(nxt[0]));
      // write next pointer
      int j = 1;
      for(; j < nxt.length - 2 + lp; j++) {
        outa.write4(nxt[j] + offset); // pointer
        // first char of next node
        outa.write1(tokens.get(next.get(nxt[j])[0])[0]);
      }
      outa.write4(nxt[j]); // data size
      if(nxt[j] == 0 && nxt[j + 1] == 0 || lp == 0) {
        // node has no data
        outa.write5(nxt[j + 1]);
      } else {
        // write pointer on data
        int n = nxt.length - 2;
        outa.write5((long) nxt[n] << 16 + (-nxt[n + 1] & 0xFFFF));
      }
      outc.write4(s);
      s += tokens.get(nxt[0]).length + (nxt.length - 3 + lp) * 5 + 10;
    }
    offset += next.size() - 1;

    return s;
  }

  /**
   * Write data as sorted list on disk.
   * The data is stored in two files:
   * 'a': length|byte|,token|byte[length]|, size|int|, offset|long|, ...
   * 'b': used method writeFTData
   * @param cs current file
   * @throws IOException I/O exception
   */
  @Override
  protected void writeIndex(final int cs) throws IOException {
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
      outa.writeBytes(tok);
      // write number of full-text data size
      outa.write4(ds);
      // write pointer on full-text data
      outa.write5(outb.size());
    }

    outa.write1(0);
    outa.close();
    outb.close();
    hash = new FTHash();
  }
}
