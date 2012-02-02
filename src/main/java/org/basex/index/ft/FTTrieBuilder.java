package org.basex.index.ft;

import static org.basex.data.DataText.*;
import static org.basex.util.Token.*;
import java.io.IOException;
import org.basex.data.Data;
import org.basex.data.MetaData;
import org.basex.io.out.DataOutput;
import org.basex.io.random.DataAccess;
import org.basex.util.list.IntArrayList;
import org.basex.util.list.IntList;
import org.basex.util.list.TokenList;

/**
 * <p>This class builds an index for text contents in a compressed trie:</p>
 *
 * <ol>
 * <li> The tokens are indexed in a main memory tree structure.</li>
 * <li> If main memory is full, data is written as sorted list to disk.</li>
 * <li> The temporary index instances are merged and written to disk.</li>
 * </ol>
 *
 * <p>The file structure is specified in the {@link FTTrie} class.</p>
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Sebastian Gath
 * @author Christian Gruen
 */
final class FTTrieBuilder extends FTBuilder {
  /** Trie index. */
  private FTTrieArray index = new FTTrieArray(128);
  /** Hash structure for temporarily saving the tokens. */
  private FTTrieHash hash = new FTTrieHash();
  /** Offset for joining subtrees. */
  private int offset;

  /**
   * Constructor.
   * @param d data reference
   * @throws IOException IOException
   */
  FTTrieBuilder(final Data d) throws IOException {
    super(d);
  }

  @Override
  public FTIndex build() throws IOException {
    index();
    return new FTTrie(data);
  }

  @Override
  void index(final byte[] tok) {
    hash.index(tok, pre, pos);
  }

  @Override
  int nrTokens() {
    return hash.size();
  }

  @Override
  void calcFreq() {
    hash.init();
    while(hash.more()) calcFreq(hash.pre[hash.next()]);
  }

  @Override
  public void write() throws IOException {
    if(!merge) {
      writeAll();
      return;
    }

    // merges temporary index files
    writeIndex(csize++);
    final DataOutput outB = new DataOutput(data.meta.dbfile(DATAFTX + 'b'));
    final DataOutput outT = new DataOutput(data.meta.dbfile(DATAFTX + 't'));
    final IntList ind = new IntList();

    // open all temporary sorted lists
    final FTList[] v = new FTList[csize];
    for(int b = 0; b < csize; ++b) v[b] = new FTTrieList(data, b);

    final IntList il = new IntList();
    while(check(v)) {
      int min = 0;
      il.reset();
      il.add(min);
      // find next token to write on disk
      for(int i = 0; i < csize; ++i) {
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
      outT.writeToken(v[min].tok);
      // merge and write data size
      outT.write4(merge(outB, il, v));
      // write pointer on full-text data
      outT.write5(outB.size());
    }

    outT.writeToken(EMPTY);
    outT.close();
    outB.close();

    // write trie index structure to disk, split in subtrees
    writeSplitTrie(ind);
  }

  /**
   * Writes the trie structure to disk.
   * @throws IOException I/O exception
   */
  private void writeAll() throws IOException {
    if(scm == 0) hash.init();
    else hash.initIter();

    final DataOutput outB = new DataOutput(data.meta.dbfile(DATAFTX + 'b'));
    while(hash.more()) {
      final int p = hash.next();
      final byte[] tok = hash.key();
      final int ds = hash.sizes[p];
      final long cpre = outB.size();
      // write compressed pre and pos arrays
      writeFTData(outB, hash.pre[p], hash.pos[p]);
      index.insertSorted(tok, ds, cpre);
    }
    outB.close();
    hash = null;

    final TokenList tokens = index.tokens;
    final IntArrayList next = index.next;

    final DataOutput outA = new DataOutput(data.meta.dbfile(DATAFTX + 'a'));
    final DataOutput outC = new DataOutput(data.meta.dbfile(DATAFTX + 'c'));

    // write root node (token length and bytes)
    outA.write1(1);
    outA.write1(0);
    // write next pointer
    final int[] root = next.get(0);
    final int js = root.length - 2;
    for(int j = 1; j < js; ++j) {
      // pointer
      final int p = root[j];
      outA.write4(p);
      // first char of next node
      outA.write1(tokens.get(next.get(p)[0])[0]);
    }
    // write root node
    outA.write4(root[root.length - 2]); // data size
    // root has no data
    outA.write5(0);
    // write offset to first node
    outC.write4(0);

    // all other nodes
    writeSubTree(null, outA, outC, 0, (root.length - 3) * 5 + 11);

    outC.write4(0);
    outA.close();
    outC.close();
  }

  /**
   * Writes the trie structure to disk, split in subtrees to save memory.
   * @param roots root nodes
   * @throws IOException I/O exception
   */
  private void writeSplitTrie(final IntList roots) throws IOException {
    final MetaData md = data.meta;
    final DataOutput outA = new DataOutput(md.dbfile(DATAFTX + 'a'));
    final DataOutput outC = new DataOutput(md.dbfile(DATAFTX + 'c'));
    final DataAccess outT = new DataAccess(md.dbfile(DATAFTX + 't'));
    final int[] root = new int[roots.size()];
    int rp = 0;

    // write root node (token length and bytes)
    outA.write1(1);
    outA.write1(0);
    // write next pointers
    for(int j = 0; j < roots.size(); ++j) {
      // dummy pointer
      outA.write4(0);
      // first char of next node
      outA.write1(roots.get(j));
    }
    // data size
    outA.write4(0);
    // pointer on data - root has no data
    outA.write5(0);

    // write offset to first node
    outC.write4(0);

    int siz = (int) (2L + roots.size() * 5L + 9L);
    while(true) {
      final byte[] tok = outT.readToken();
      if(tok.length == 0) break;
      final int s = outT.read4();
      final long off = outT.read5();
      if(rp < roots.size() && tok[0] != roots.get(rp)) {
        // write subtree to disk
        siz = writeSubTree(root, outA, outC, rp, siz);
        ++rp;
        index = new FTTrieArray(128);
      }
      index.insertSorted(tok, s, off);
    }

    // write subtree to disk
    writeSubTree(root, outA, outC, rp, siz);

    outT.close();
    outA.close();
    outC.write4(0);
    outC.close();

    final DataAccess tmp = new DataAccess(md.dbfile(DATAFTX + 'a'));
    long c = 2;
    for(final int r : root) {
      tmp.write4(c, r);
      c += 5;
    }
    tmp.close();
    md.drop(DATAFTX + 't');
  }

  /**
   * Writes subtree to disk.
   * @param root Array with root offsets
   * @param outA trie structure
   * @param outC node sizes
   * @param rp pointer on root offsets
   * @param siz size
   * @return new size
   * @throws IOException I/O exception
   */
  private int writeSubTree(final int[] root, final DataOutput outA,
      final DataOutput outC, final int rp, final int siz) throws IOException {

    // indexed full-text tokens
    final TokenList tokens = index.tokens;
    // trie index structure
    final IntArrayList next = index.next;
    if(root != null) root[rp] = next.get(0)[1] + offset;

    int s = siz;
    final int il = next.size();
    // loop over all trie nodes
    for(int i = 1; i < il; ++i) {
      final int[] nxt = next.get(i);
      // check if pointer on data needs 1 or 2 integers
      final int lp = nxt[nxt.length - 1] >= 0 ? 0 : -1;
      // write token
      outA.write1(tokens.get(nxt[0]).length);
      outA.writeBytes(tokens.get(nxt[0]));
      // write next pointer
      final int jl = nxt.length - 2 + lp;
      for(int j = 1; j < jl; ++j) {
        // pointer
        outA.write4(nxt[j] + offset);
        // first char of next node
        outA.write1(tokens.get(next.get(nxt[j])[0])[0]);
      }
      // data size
      outA.write4(nxt[jl]);
      if(lp == 0 || nxt[jl] == 0 && nxt[jl + 1] == 0) {
        // node has no data
        outA.write5(nxt[jl + 1]);
      } else {
        // write pointer on data
        final int n = nxt.length - 2;
        outA.write5((long) nxt[n] << 16 + (-nxt[n + 1] & 0xFFFF));
      }
      // write node offset
      outC.write4(s);
      s += tokens.get(nxt[0]).length + (nxt.length - 3 + lp) * 5 + 10;
    }
    offset += next.size() - 1;

    return s;
  }

  /**
   * Writes the data as sorted list to disk.
   * The data is stored in two files:
   * <ul>
   * <li>File <b>a</b>: {@code length|byte|,token|byte[length]|,
   * size|int|, offset|long|, ...}</li>
   * <li>File <b>b</b>: written via {@link #writeFTData}</li>
   * </ul>
   * @param cs current file
   * @throws IOException I/O exception
   */
  @Override
  protected void writeIndex(final int cs) throws IOException {
    final String f = DATAFTX + (merge ? cs : "");
    final DataOutput outA = new DataOutput(data.meta.dbfile(f + 'a'));
    final DataOutput outB = new DataOutput(data.meta.dbfile(f + 'b'));

    if(scm == 0) hash.init();
    else hash.initIter();
    while(hash.more()) {
      final int p = hash.next();
      final byte[] t = hash.key();
      final int s = hash.sizes[p];
      // write compressed pre and pos arrays
      writeFTData(outB, hash.pre[p], hash.pos[p]);
      // write token length
      outA.write1(t.length);
      // write token
      outA.writeBytes(t);
      // write number of full-text data size
      outA.write4(s);
      // write pointer on full-text data
      outA.write5(outB.size());
    }

    outA.write1(0);
    outA.close();
    outB.close();
    hash = new FTTrieHash();
  }
}
