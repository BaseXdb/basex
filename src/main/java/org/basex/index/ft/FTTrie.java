package org.basex.index.ft;

import static org.basex.core.Text.*;
import static org.basex.data.DataText.*;
import static org.basex.util.Token.*;
import static org.basex.util.ft.FTFlag.*;
import java.io.IOException;
import java.util.Arrays;
import org.basex.core.Prop;
import org.basex.data.Data;
import org.basex.data.DataText;
import org.basex.index.IndexIterator;
import org.basex.index.IndexStats;
import org.basex.index.IndexToken;
import org.basex.io.random.DataAccess;
import org.basex.util.Num;
import org.basex.util.Performance;
import org.basex.util.TokenBuilder;
import org.basex.util.Util;
import org.basex.util.ft.FTLexer;
import org.basex.util.hash.TokenIntMap;
import org.basex.util.list.IntList;

/**
 * <p>This class performs full-text index requests on a compressed trie on disk.
 * The three database index files start with the prefix {@link DataText#DATAFTX}
 * and have the following format:</p>
 *
 * <ol>
 * <li>File <b>a</b> contains the trie nodes:<br/>
 * {@code [l, t1, ..., tl, n1, v1, ..., nu, vu, s, p]}<br/>
 * {@code l}: length of the token [byte]<br/>
 * {@code t1, ..., tl}: token bytes [byte]<br/>
 * {@code u}: number of child nodes<br/>
 * {@code n1, ..., nu}: child nodes<br/>
 * {@code v1}: the first bytes of each token {@code n1} points, ... [byte]<br/>
 * {@code s}: number of stored pre values [int]<br/>
 * {@code p}: pointer to pre values [long]<br/>
 * {@code [byte, byte[l], byte, int, byte, ..., int, long]}</li>
 *
 * <li>File <b>b</b> contains the {@code pre/pos} references.
 * The values are ordered, but not distinct:<br/>
 * {@code pre1/pos1, pre2/pos2, pre3/pos3, ...} [{@link Num}]</li>
 *
 * <li>File <b>c</b> contains the offsets to all trie nodes in
 * file <b>a</b>:<br/> {@code off0, off1, off2, ..., offN} [int]</li>
 * </ol>
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 * @author Sebastian Gath
 */
final class FTTrie extends FTIndex {
  /** Trie nodes. */
  private final DataAccess inA;
  /** Full-text data. */
  private final DataAccess inB;
  /** Trie node references. */
  private final DataAccess inC;
  /** ID of current node. */
  private long currID;

  /**
   * Constructor, initializing the index structure.
   * @param d data reference
   * @throws IOException I/O Exception
   */
  protected FTTrie(final Data d) throws IOException {
    super(d);
    inA = new DataAccess(d.meta.dbfile(DATAFTX + 'a'));
    inB = new DataAccess(d.meta.dbfile(DATAFTX + 'b'));
    inC = new DataAccess(d.meta.dbfile(DATAFTX + 'c'));
  }

  @Override
  public synchronized int count(final IndexToken ind) {
    if(ind.get().length > data.meta.maxlen) return Integer.MAX_VALUE;

    // estimate costs for queries which stretch over multiple index entries
    final FTLexer lex = (FTLexer) ind;
    if(lex.ftOpt().is(FZ) || lex.ftOpt().is(WC))
      return Math.max(1, data.meta.size / 10);

    final byte[] token = lex.get();
    final int id = cache.id(token);
    if(id > 0) return cache.size(id);

    int size = 0;
    long poi = 0;
    final int[] node = node(token, 0);
    if(node != null && node[node.length - 1] > 0) {
      size = node[node.length - 1];
      poi = currID;
    }
    cache.add(token, size, poi);
    return size;
  }

  @Override
  public synchronized IndexIterator iter(final IndexToken ind) {
    final byte[] token = ind.get();

    // support fuzzy search
    final FTLexer lex = (FTLexer) ind;
    if(lex.ftOpt().is(FZ)) {
      int k = data.meta.prop.num(Prop.LSERROR);
      if(k == 0) k = token.length >> 2;
      return fuzzy(0, null, -1, token, 0, 0, 0, k, false);
    }

    // support wildcards
    if(lex.ftOpt().is(WC)) {
      final int pw = indexOf(token, '.');
      if(pw != -1) return wc(token, pw, false);
    }

    // return cached or new result
    final int id = cache.id(token);
    return id == 0 ? iter(0, token, false) :
      iter(cache.pointer(id), cache.size(id), inB, false);
  }

  @Override
  public synchronized void close() throws IOException {
    inB.close();
    inC.close();
    inA.close();
  }

  /**
   * Traverses the trie and returns a result iterator.
   * @param id on node array (in -memory)
   * @param token search token
   * @param fast fast evaluation
   * @return pre-values and corresponding positions
   * for each pre-value
   */
  private FTIndexIterator iter(final int id, final byte[] token,
      final boolean fast) {

    if(token == null || token.length == 0) return FTIndexIterator.FTEMPTY;
    final int[] node = node(token, id);
    return node == null ? FTIndexIterator.FTEMPTY :
      iter(currID, node[node.length - 1], inB, fast);
  }

  @Override
  public TokenIntMap entries(final byte[] prefix) {
    throw Util.notexpected(this);
  }

  /**
   * Traverses the trie and returns a node entry for the specified token
   * or a {@code null} reference.
   * @param token search token
   * @param id on node array (in main memory)
   * @return int ids
   */
  private int[] node(final byte[] token, final int id) {
    byte[] tok = token;

    // read node data from disk
    final int[] node = entry(id);
    if(id != 0) {
      int t = 0;
      final int tl = tok.length;
      while(t < tl && t < node[0] && node[t + 1] == tok[t]) ++t;
      // sub token did not match: stop search
      if(t != node[0]) return null;
      // all characters checked, correct leaf node found: return result
      if(t == tl) return node;
      // strip token prefix
      final byte[] tmp = new byte[tl - t];
      System.arraycopy(tok, t, tmp, 0, tmp.length);
      tok = tmp;
    }

    // scan succeeding node
    final int pos = pos(node, tok[0]);
    return pos < 0 ? null : node(tok, node[pos]);
  }

  @Override
  public synchronized byte[] info() {
    final TokenBuilder tb = new TokenBuilder();
    tb.add(INDEXSTRUC + TRIESTRUC + NL);
    tb.addExt("- %: %" + NL, CREATEST, Util.flag(data.meta.stemming));
    tb.addExt("- %: %" + NL, CREATECS, Util.flag(data.meta.casesens));
    tb.addExt("- %: %" + NL, CREATEDC, Util.flag(data.meta.diacritics));
    if(data.meta.language != null)
      tb.addExt("- %: %" + NL, CREATELN, data.meta.language);
    final long l = inA.length() + inB.length() + inC.length();
    tb.add(SIZEDISK + Performance.format(l, true) + NL);

    final IndexStats stats = new IndexStats(data);
    addOccs(EMPTY, 0, stats);
    stats.print(tb);
    return tb.finish();
  }

  /**
   * Called by {@link #info}. Collects all tokens and their sizes found
   * in the index structure.
   * @param token current token
   * @param id on node array (in main memory)
   * @param st statistics reference
   */
  private void addOccs(final byte[] token, final int id, final IndexStats st) {
    final int[] ne = entry(id);
    byte[] nt = token;
    if(id > 0) {
      nt = Arrays.copyOf(token, token.length + ne[0]);
      for(int i = 0; i < ne[0]; ++i) nt[token.length + i] = (byte) ne[i + 1];
      final int size = ne[ne.length - 1];
      if(size > 0 && st.adding(size)) st.add(nt);
    }
    for(int i = ne[0] + 1; i < ne.length - 1; i += 2) addOccs(nt, ne[i], st);
  }

  /**
   * Reads a node entry from disk.
   * @param id on node array (in main memory)
   * @return node entry from disk
   */
  private int[] entry(final long id) {
    // read start and end position
    int sp = inC.read4(id << 2);
    final int ep = inC.read4();

    final IntList il = new IntList();
    inA.cursor(sp++);
    final int l = inA.read1();
    il.add(l);
    for(int j = 0; j < l; ++j) il.add(inA.read1());
    sp += l;

    // inner node
    while(sp + 9 < ep) {
      il.add(inA.read4());
      il.add(inA.read1());
      sp += 5;
    }
    il.add(inA.read4());
    currID = inA.read5();
    return il.toArray();
  }

  /**
   * Checks whether a node is an inner node or a leaf node.
   * @param node current node entry
   * @return boolean leaf node or inner node
   */
  private boolean more(final int[] node) {
    return node[0] + 1 < node.length - 1;
  }

  /**
   * Uses linear search for finding inserting position.
   * returns:
   * -1 if no inserting position was found
   * 0 if any successor exists, or 0 is inserting position
   * n here to insert
   * n and found = true, if nth item is occupied and here to insert
   * @param cne current node entry
   * @param ins byte looking for
   * @return inserting position
   */
  private int pos(final int[] cne, final byte ins) {
    int i = cne[0] + 1;
    final int s = cne.length - 1;
    while(i < s && diff((byte) cne[i + 1], ins) < 0) i += 2;
    return i < s && cne[i + 1] == ins ? i : -1;
  }

  /** Saves astericsWildCardTraversing result
      has to be re-init each time (before calling method). */
  private FTIndexIterator idata;

  /**
   * Looks up a node with value, which matches the ending.
   * The parameter lastFound shows whether chars were found in last recursive
   * call, which correspond to the ending; consequently, those chars are
   * considered which occur successive in the ending.
   * pointerNode shows the position comparison between value[nodeId] and
   * ending starts
   * pointerEnding shows the position comparison between ending and
   * value[nodeId] starts
   * @param id on node array (in main memory)
   * @param ending ending of value
   * @param lst boolean if value was found in last run
   * @param nod pointer on current node
   * @param end pointer on value ending
   * @param fast fast evaluation
   */
  private void wc(final int id, final byte[] ending, final boolean lst,
      final int nod, final int end, final boolean fast) {

    int j = end;
    int i = nod;
    boolean last = lst;
    final int[] node = entry(id);
    final long tdid = currID;

    // wildcard at the end
    if(ending == null || ending.length == 0) {
      // save data current node
      if(node[node.length - 1] > 0) {
        idata = FTIndexIterator.union(
            iter(tdid, node[node.length - 1], inB, fast), idata);
      }
      // preorder traversal through trie
      for(int t = node[0] + 1; t < node.length - 1; t += 2) {
        wc(node[t], null, last, 0, 0, fast);
      }
      return;
    }

    // compare chars current node and ending
    // skip all unlike chars, if any suitable was found
    while(!last && i < node[0] + 1 && node[i] != ending[j]) ++i;

    // skip all chars, equal to first char
    while(i + ending.length < node[0] + 1 && node[i + 1] == ending[0]) ++i;

    while(i < node[0] + 1 && j < ending.length && node[i] == ending[j]) {
      ++i;
      ++j;
      last = true;
    }

    // not processed all chars from node, but all chars from
    // ending were processed or root
    if(id == 0 || j == ending.length && i < node[0] + 1) {
      if(!more(node)) return;

      //final int[] nextNodes = getNextNodes(ne);
      // preorder search in trie
      for(int t = node[0] + 1; t < node.length - 1; t += 2) {
        wc(node[t], ending, false, 1, 0, fast);
      }
      return;
    }

    if(j == ending.length && i == node[0] + 1) {
      // all chars form node and all chars from ending done
      idata = FTIndexIterator.union(
          iter(tdid, node[node.length - 1], inB, fast), idata);

      // node has successors and is leaf node: preorder search in trie
      // preorder search in trie
      for(int t = node[0] + 1; t < node.length - 1; t += 2) {
        if(j == 1) wc(node[t], ending, false, 0, 0, fast);
        wc(node[t], ending, last, 0, j, fast);
      }
      return;
    }

    if(j < ending.length && i < node[0] + 1) {
      // still chars from node and still chars from ending left, pointer = 0 and
      // restart searching
      if(!more(node)) return;

      // restart searching at node, but value-position i
      wc(id, ending, false, i + 1, 0, fast);
      return;
    }

    // all chars from node processed, but not all chars from processed
    if(j < ending.length && i == node[0] + 1) {
      // move pointer and go on
      if(!more(node)) return;

      // preorder search in trie
      for(int t = node[0] + 1; t < node.length - 1; t += 2) {
        // compare only first char from ending
        if(j == 1) wc(node[t], ending, last, 1, 0, fast);
        wc(node[t], ending, last, 1, j, fast);
      }
    }
  }

  /**
   * Saves the number of compared chars at wildcard search.
   * counter[0] = total number compared chars
   * counter[1] = number current method call (gets initialized before each call)
   */
  private int[] counter;

  /**
   * Saves node values from .-wildcard search according to records in id-array.
   */
  private byte[] valuesFound;

  /**
   * Method for wildcards search in trie.
   * @param token search token
   * @param pos position
   * @param f fast evaluation
   * @return result iterator
   */
  private FTIndexIterator wc(final byte[] token, final int pos,
      final boolean f) {
    // init counter
    counter = new int[2];
    return wc(0, token, pos, false, f);
  }

  /**
   * Supports different wildcard operators: ., .+, .* and .?.
   * PosWildCard points on bytes[], at position, where . is situated
   * recCall flags recursive calls
   * @param id current node
   * @param token search token
   * @param posw wildcards position
   * @param first flag for first call
   * @param fast fast evaluation
   * @return result iterator
   */
  private FTIndexIterator wc(final int id, final byte[] token,
      final int posw, final boolean first, final boolean fast) {

    final byte[] vsn = token;
    byte[] aw = null;
    byte[] bw = null;

    final int currentLength = 0;
    int resultNode;

    FTIndexIterator d = FTIndexIterator.FTEMPTY;
    // wildcard not at beginning
    if(posw > 0) {
      // copy part before wildcard
      bw = new byte[posw];
      System.arraycopy(vsn, 0, bw, 0, posw);
      resultNode = wc(id, bw);
      if(resultNode == -1) return FTIndexIterator.FTEMPTY;
    } else {
      resultNode = 0;
    }

    final byte wildcard = posw + 1 >= vsn.length ? (byte) '.' : vsn[posw + 1];
    if(wildcard == '?') {
      // append 0 or 1 symbols
      // look in trie without wildcard
      byte[] sc = new byte[vsn.length - 2 - currentLength];
      // copy unprocessed part before wildcard
      if(bw != null) {
        System.arraycopy(bw, 0, sc, 0, bw.length);
      }
      // copy part after wildcard
      if(bw == null) {
        System.arraycopy(vsn, posw + 2, sc, 0, sc.length);
      } else {
        System.arraycopy(vsn, posw + 2, sc, bw.length, sc.length - bw.length);
      }

      d = iter(0, sc, fast);

      // lookup in trie with . as wildcard
      sc = new byte[vsn.length - 1];
      if(bw != null) {
        // copy unprocessed part before wildcard
        System.arraycopy(bw, 0, sc, 0, bw.length);
        sc[bw.length] = '.';

        // copy part after wildcard
        System.arraycopy(vsn, posw + 2, sc, bw.length + 1,
            sc.length - bw.length - 1);
      } else {
        // copy unprocessed part before wildcard
        sc[0] = '.';
        // copy part after wildcard
        System.arraycopy(vsn, posw + 2, sc, 1, sc.length - 1);
      }
      // attach both result
      d = FTIndexIterator.union(wc(0, sc, posw, false, fast), d);
      return d;
    }

    if(wildcard == '*') {
      // append 0 or n symbols
      // valueSearchNode == .*
      if(!(posw == 0 && vsn.length == 2)) {
        // lookup in trie without wildcard
        final byte[] searchChar = new byte[vsn.length - 2 - currentLength];
        // copy unprocessed part before wildcard
        if(bw != null) {
          System.arraycopy(bw, 0, searchChar, 0, bw.length);
        }
        // copy part after wildcard
        if(bw == null) {
          aw = new byte[searchChar.length];
          System.arraycopy(vsn, posw + 2, searchChar, 0, searchChar.length);
          System.arraycopy(vsn, posw + 2, aw, 0, searchChar.length);
        } else {
          aw = new byte[searchChar.length - bw.length];
          System.arraycopy(vsn, posw + 2, searchChar,
              bw.length, searchChar.length - bw.length);
          System.arraycopy(vsn, posw + 2, aw,
              0, searchChar.length - bw.length);
        }
        d = iter(0, searchChar, fast);
        // all chars from valueSearchNode are contained in trie
        if(bw != null && counter[1] != bw.length) return d;
      }

      // delete data
      idata = FTIndexIterator.FTEMPTY;
      wc(resultNode, aw, false, counter[0], 0, fast);
      return FTIndexIterator.union(d, idata);
    }

    if(wildcard == '+') {
      // append 1 or more symbols
      final int[] rne = entry(resultNode);
      final byte[] nvsn = new byte[vsn.length + 1];
      int l = 0;
      if(bw != null) {
        System.arraycopy(bw, 0, nvsn, 0, bw.length);
        l = bw.length;
      }

      if(0 < vsn.length - posw - 2) {
        System.arraycopy(vsn, posw + 2, nvsn, posw + 3, vsn.length - posw - 2);
      }

      nvsn[l + 1] = '.';
      nvsn[l + 2] = '*';
      FTIndexIterator tmpres = FTIndexIterator.FTEMPTY;
      // append 1 symbol
      // not completely processed (value current node)
      if(rne[0] > counter[0] && resultNode > 0) {
        // replace wildcard with value from currentCompressedTrieNode
        nvsn[l] = (byte) rne[counter[0] + 1];
        tmpres = wc(nvsn, l + 1, fast);
      } else if(rne[0] == counter[0] || resultNode == 0) {
        // all chars from nodes[resultNode] are computed
        // any next values existing
        if(!more(rne)) return FTIndexIterator.FTEMPTY;

        for(int t = rne[0] + 1; t < rne.length - 1; t += 2) {
          nvsn[l] = (byte) rne[t + 1];
          tmpres = FTIndexIterator.union(wc(nvsn, l + 1, fast), tmpres);
        }
      }
      return tmpres;
    }

    final int[] rne = entry(resultNode);
    // append 1 symbol
    // not completely processed (value current node)
    if(rne[0] > counter[0] && resultNode > 0) {
      // replace wildcard with value from currentCompressedTrieNode
      vsn[posw] = (byte) rne[counter[0] + 1];

      // . wildcards left
      final FTIndexIterator resultData = iter(0, vsn, fast);
      // save nodeValues for recursive method call
      if(resultData.size() != 0 && first) {
        valuesFound = new byte[] {(byte) rne[counter[0] + 1]};
      }
      return resultData;
    }

    if(rne[0] == counter[0] || resultNode == 0) {
      // all chars from nodes[resultNode] are computed
      // any next values existing
      if(!more(rne)) return FTIndexIterator.FTEMPTY;

      FTIndexIterator tmpNode = FTIndexIterator.FTEMPTY;
      aw = new byte[vsn.length - posw];
      System.arraycopy(vsn, posw + 1, aw, 1, aw.length - 1);

      // simple method call
      if(!first) {
        for(int t = rne[0] + 1; t < rne.length - 1; t += 2) {
          aw[0] = (byte) rne[t + 1];
          tmpNode = FTIndexIterator.union(iter(rne[t], aw, fast), tmpNode);
        }
        return tmpNode;
      }

      // method call for .+ wildcard
      valuesFound = new byte[rne.length - 1 - rne[0] - 1];
      for(int t = rne[0] + 1; t < rne.length - 1; t += 2) {
        // replace first letter
        aw[0] = (byte) rne[t + 1];
        valuesFound[t - rne[0] - 1] = (byte) rne[t + 1];
        tmpNode = FTIndexIterator.union(iter(rne[t], aw, fast), tmpNode);
      }
    }
    return FTIndexIterator.FTEMPTY;
  }

  /**
   * Traverses the trie and returns a found node for searchValue;
   * returns the last touched node.
   * @param id node id
   * @param token search token
   * @return id int last touched node
   */
  private int wc(final int id, final byte[] token) {
    byte[] vsn = token;
    final int[] cne = entry(id);
    if(id != 0) {
      counter[1] += cne[0];

      int i = 0;
      while(i < vsn.length && i < cne[0] && cne[i + 1] == vsn[i]) ++i;

      if(cne[0] == i) {
        if(vsn.length == i) {
          // leaf node found with appropriate value
          counter[0] = i;
          return id;
        }

        // cut valueSearchNode for value current node
        final byte[] tmp = new byte[vsn.length - i];
        System.arraycopy(vsn, i, tmp, 0, tmp.length);
        vsn = tmp;

        // scan successors currentNode
        final int pos = pos(cne, vsn[0]);
        if(pos >= 0) return wc(cne[pos], vsn);
      }
      // node not contained
      counter[0] = i;
      counter[1] = counter[1] - cne[0] + i;
      return id;
    }

    // scan successors current node
    final int pos = pos(cne, vsn[0]);
    if(pos >= 0) return wc(cne[pos], vsn);

    // node not contained
    counter[0] = -1;
    counter[1] = -1;
    return -1;
  }

  /**
   * Traverses the trie and returns the found node for searchValue;
   * returns data from node or {@code null}.
   * @param id on node array (in main memory)
   * @param crne current node entry
   * @param crdid current pointer on data
   * @param token search token
   * @param d int counter for deletions
   * @param p int counter for pastes
   * @param r int counter for replacements
   * @param c int counter sum of errors
   * @param f fast evaluation
   * @return result iterator
   */
  private FTIndexIterator fuzzy(final int id, final int[] crne,
      final long crdid, final byte[] token, final int d, final int p,
      final int r, final int c, final boolean f) {
    byte[] vsn = token;
    int[] cne = crne;
    long cdid = crdid;
    if(cne == null) {
      cne = entry(id);
      cdid = currID;
    }

    if(id != 0) {
      // not root node
      int i = 0;
      while(i < vsn.length && i < cne[0] && cne[i + 1] == vsn[i]) ++i;

      if(cne[0] == i) {
        // node entry processed complete
        if(vsn.length == i) {
          // leaf node found with appropriate value
          if(c < d + p + r) return FTIndexIterator.FTEMPTY;

          FTIndexIterator ld = FTIndexIterator.FTEMPTY;
          ld = iter(cdid, cne[cne.length - 1], inB, f);
          for(int t = cne[0] + 1; t < cne.length - 1; t += 2) {
            ld = FTIndexIterator.union(fuzzy(cne[t], null, -1,
                new byte[] { (byte) cne[t + 1] }, d, p + 1, r, c, f), ld);
          }
          return ld;
        }

        FTIndexIterator ld = FTIndexIterator.FTEMPTY;
        byte[] b;
        if(c > d + p + r) {
          // delete char
          b = new byte[vsn.length - 1];
          System.arraycopy(vsn, 0, b, 0, i);
          ld = FTIndexIterator.union(
              fuzzy(id, cne, cdid, b, d + 1, p, r, c, f), ld);
        }

        // cut valueSearchNode for value current node
        final byte[] tmp = new byte[vsn.length - i];
        System.arraycopy(vsn, i, tmp, 0, tmp.length);
        vsn = tmp;

        // scan successors currentNode
        int[] ne = null;
        long tdid = -1;
        for(int k = cne[0] + 1; k < cne.length - 1; k += 2) {
          if(cne[k + 1] == vsn[0]) {
            ne = entry(cne[k]);
            tdid = currID;
            b = new byte[vsn.length];
            System.arraycopy(vsn, 0, b, 0, vsn.length);
            ld = FTIndexIterator.union(
                fuzzy(cne[k], ne, tdid, b, d, p, r, c, f), ld);
          }

          if(c > d + p + r) {
            if(ne == null) {
              ne = entry(cne[k]);
              tdid = currID;
            }
            // paste char
            b = new byte[vsn.length + 1];
            b[0] = (byte) cne[k + 1];
            System.arraycopy(vsn, 0, b, 1, vsn.length);
            ld = FTIndexIterator.union(fuzzy(cne[k], ne, tdid,
                b, d, p + 1, r, c, f), ld);

            if(vsn.length > 0) {
              // delete char
              b = new byte[vsn.length - 1];
              System.arraycopy(vsn, 1, b, 0, b.length);
              ld = FTIndexIterator.union(fuzzy(cne[k], ne, tdid,
                  b, d + 1, p, r, c, f), ld);
              // replace char
              b = new byte[vsn.length];
              System.arraycopy(vsn, 1, b, 1, vsn.length - 1);
              b[0] = (byte) ne[1];
              ld = FTIndexIterator.union(fuzzy(cne[k], ne, tdid,
                  b, d, p, r + 1, c, f), ld);
            }
          }
        }
        return ld;
      }

      FTIndexIterator ld = FTIndexIterator.FTEMPTY;

      if(c > d + p + r) {
        // paste char
        byte[] b = new byte[vsn.length + 1];
        System.arraycopy(vsn, 0, b, 0, i);
        b[i] = (byte) cne[i + 1];
        System.arraycopy(vsn, i, b, i + 1, vsn.length - i);

        ld = fuzzy(id, cne, cdid, b, d, p + 1, r, c, f);

        if(vsn.length > 0 && i < vsn.length) {
          // replace
          b = new byte[vsn.length];
          System.arraycopy(vsn, 0, b, 0, vsn.length);

          b[i] = (byte) cne[i + 1];
          ld = FTIndexIterator.union(fuzzy(id, cne, cdid,
              b, d, p, r + 1, c, f), ld);
          if(vsn.length > 1) {
            // delete char
            b = new byte[vsn.length - 1];
            System.arraycopy(vsn, 0, b, 0, i);
            System.arraycopy(vsn, i + 1, b, i, vsn.length - i - 1);
            ld = FTIndexIterator.union(fuzzy(id, cne, cdid,
                b, d + 1, p, r, c, f), ld);
          }
        }
      }
      return ld;
    }

    int[] ne = null;
    long tdid = -1;
    FTIndexIterator ld = FTIndexIterator.FTEMPTY;

    byte[] b;
    for(int k = cne[0] + 1; k < cne.length - 1; k += 2) {
      if(cne[k + 1] == vsn[0]) {
        ne = entry(cne[k]);
        tdid = currID;
        b = new byte[vsn.length];
        System.arraycopy(vsn, 0, b, 0, vsn.length);
        ld = FTIndexIterator.union(
            fuzzy(cne[k], ne, tdid, b, d, p, r, c, f), ld);
      }
      if(c > d + p + r) {
        if(ne == null) {
          ne = entry(cne[k]);
          tdid = currID;
        }
        // paste char
        b = new byte[vsn.length + 1];
        b[0] = (byte) ne[1];
        System.arraycopy(vsn, 0, b, 1, vsn.length);
        ld = FTIndexIterator.union(fuzzy(cne[k], ne, tdid,
            b, d, p + 1, r, c, f), ld);

        if(vsn.length > 0) {
          // delete char
          b = new byte[vsn.length - 1];
          System.arraycopy(vsn, 1, b, 0, b.length);
          ld = FTIndexIterator.union(fuzzy(cne[k], ne, tdid,
              b, d + 1, p, r, c, f), ld);
            // replace
          b = new byte[vsn.length];
          System.arraycopy(vsn, 1, b, 1, vsn.length - 1);
            b[0] = (byte) ne[1];
            ld = FTIndexIterator.union(fuzzy(cne[k], ne, tdid,
                b, d, p, r + 1, c, f), ld);
        }
      }
    }
    return ld;
  }
}
