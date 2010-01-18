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
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Sebastian Gath
 * @author Christian Gruen
 */
public final class FTTrieBuilder extends FTBuilder {
  /** Trie index. */
  private FTArray index = new FTArray(128);
  /** Hash structure for temporarily saving the tokens. */
  private FTHash hash = new FTHash();
  /** Offset for joining subtrees. */
  private int o = 0;

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
    if((ntok & 0xFFF) == 0 && scm == 0 && memFull()) {
      // currently no frequency support for tfidf based scoring
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
      writeCompleteTrie();
      return;
    }
    
    writeSortedList(csize++);
    final DataOutput outb = new DataOutput(data.meta.file(DATAFTX + 'b'));
    final DataOutput outt = new DataOutput(data.meta.file(DATAFTX + 't'));
    final IntList root = new IntList();

    final byte[][] tok = new byte[csize][];
    final int[][] pres = new int[csize][];
    final int[][] pos = new int[csize][];
    
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
      
      if (root.size() == 0 || root.get(root.size() - 1) != tok[min][0]) 
        root.add(tok[min][0]);
      outt.write(tok[min].length);
      outt.write(tok[min]);
//      tokens.add(tok[min]);
//      offsets.add(outb.size());
      
      int s = 0;
      final TokenBuilder tbp = new TokenBuilder();
      final TokenBuilder tbo = new TokenBuilder();
      tbp.add(new byte[4]);
      tbo.add(new byte[4]);
      for(int j = 0; j < mert.size(); j++) {
        final int m = mert.get(j);
        for(final int p : pres[m]) tbp.add(Num.num(p));
        for(final int p : pos[m]) tbo.add(Num.num(p));
        s += v[m].nextFTDataSize();
        tok[m] = nextToken(v, m);
        pres[m] = tok[m].length > 0 ? v[m].nextPreValues() : new int[0];
        pos[m] = tok[m].length > 0 ? v[m].nextPosValues() : new int[0];
      }
      
      outt.writeInt(s);
      outt.write5(outb.size());

      // write compressed pre and pos arrays
      final byte[] p = tbp.finish();
      Num.size(p, p.length);
      final byte[] op = tbo.finish();
      Num.size(op, op.length);
      writeFTData(outb, p, op);
    }
    outt.write(0);
    outt.close();
    outb.close();
    writeSplittedTrie(root);
  }

  /**
   * Returns next token.
   * @param v FTFuzzy Array
   * @param m pointer on current FTFuzzy
   * @return next token
   * @throws IOException I/O exception
   */
  protected byte[] nextToken(final FTSortedList[] v, final int m) throws IOException {
    if(v[m] == null) return EMPTY;
    final byte[] tok = v[m].nextTok();
    if(tok.length > 0) return tok;
    v[m].close();
    v[m] = null;
    return EMPTY;
  }

  /**
   * Write trie structure to disk.
   * @param roots root nodes
   * @throws IOException
   */
  void writeSplittedTrie(final IntList roots) throws IOException {
    DataAccess t = new DataAccess(data.meta.file(DATAFTX + 't'));
    // save each node: l, t1, ..., tl, n1, v1, ..., nu, vu, s, p
    // l = length of the token t1, ..., tl
    // u = number of next nodes n1, ..., nu
    // v1= the first byte of each token n1 points, ...
    // s = size of pre values saved at pointer p
    // [byte, byte[l], byte, int, byte, ..., int, long]
    final DataOutput outN = new DataOutput(data.meta.file(DATAFTX + 'a'));
    // ftdata is stored here, with pre1, ..., preu, pos1, ..., posu
    // each node entries size is stored here
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
    
    writeSubTree(root, outN, outS, rp, siz);

    t.close();
    outN.close();
    outS.close();

    // update root node
    final RandomAccessFile a = 
      new RandomAccessFile(data.meta.file(DATAFTX + 'a'),"rw");
    a.seek(2);
    for(int i = 0; i < root.length; i++) {
      a.writeInt(root[i]);
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
   * @throws IOException
   */
  private int writeSubTree(final int[] root, final DataOutput outN, 
      final DataOutput outS, final int rp, final int siz) throws IOException {
    int s = siz;
    // write subtree to disk
    final TokenList tokens = index.tokens;
    final IntArrayList next = index.next;
    if (root != null) root[rp] = next.get(0)[1] + o;
//    rp++;
    
    final int il = next.size();
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

//    outS.writeInt(s);
//    System.out.println(s);
    o += next.size() - 1;

    return s;
  }
  
  /**
   * Write trie structure to disk.
   * @throws IOException
   */
  void writeCompleteTrie() throws IOException {
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

    // save each node: l, t1, ..., tl, n1, v1, ..., nu, vu, s, p
    // l = length of the token t1, ..., tl
    // u = number of next nodes n1, ..., nu
    // v1= the first byte of each token n1 points, ...
    // s = size of pre values saved at pointer p
    // [byte, byte[l], byte, int, byte, ..., int, long]
    final DataOutput outN = new DataOutput(data.meta.file(DATAFTX + 'a'));
    // ftdata is stored here, with pre1, ..., preu, pos1, ..., posu
    // each node entries size is stored here
    final DataOutput outS = new DataOutput(data.meta.file(DATAFTX + 'c'));

    // document contains any text nodes -> empty index created;
    // only root node is kept
    int siz = 0;
    if(index.count != 1) {
      // index.next[i] : [p, n1, ..., s, d]
      // index.tokens[p], index.next[n1], ..., index.pre[d]

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
   * @throws IOException
   */
  void writeSortedList(final int cs) throws IOException {
    final String s = DATAFTX + (merge ? Integer.toString(cs) : "");
    final DataOutput outa = new DataOutput(data.meta.file(s +'a'));
    final DataOutput outb = new DataOutput(data.meta.file(s +'b'));

    if(scm == 0) hash.init();
    else hash.initIter();
//    outa.writeInt(hash.size());
    while(hash.more()) {
      final int p = hash.next();
      final byte[] tok = hash.key();
      final int ds = hash.ns[p];      
      // write compressed pre and pos arrays
      writeFTData(outb, hash.pre[p], hash.pos[p]);
      outa.write1(tok.length);
      outa.write(tok);
      outa.writeInt(ds);
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
