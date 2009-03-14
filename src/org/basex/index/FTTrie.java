package org.basex.index;

import static org.basex.Text.*;
import static org.basex.data.DataText.*;
import static org.basex.util.Token.*;
import java.io.IOException;
import org.basex.BaseX;
import org.basex.core.Prop;
import org.basex.data.Data;
import org.basex.io.DataAccess;
import org.basex.util.IntList;
import org.basex.util.Performance;
import org.basex.util.TokenBuilder;

/**
 * This class indexes text contents in a compressed trie on disk.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 * @author Sebastian Gath
 */
public final class FTTrie extends Index {
  // save each node: l, t1, ..., tl, n1, v1, ..., nu, vu, s, p
  // l = length of the token t1, ..., tl
  // u = number of next nodes n1, ..., nu
  // v1= the first byte of each token n1 points, ...
  // s = size of pre values saved at pointer p
  // [byte, byte[l], byte, int, byte, ..., int, long]

  /** Cache for number of hits and data reference per token. */
  private final FTTokenMap cache = new FTTokenMap();
  /** Values file. */
  private final Data data;
  /** Trie structure on disk. */
  private final DataAccess inN;
  // ftdata is stored here, with pre1, ..., preu, pos1, ..., posu
  /** FTData on disk. */
  public final DataAccess inD;
  // each node entries size is stored here
  /** FTData sizes on disk. */
  private final DataAccess inS;
  /** Id on data, corresponding to the current node entry. */
  private long did;

  /**
   * Constructor, initializing the index structure.
   * @param d data reference
   * @param db name of the database
   * @throws IOException IO Exception
   */
  public FTTrie(final Data d, final String db) throws IOException {
    inN = new DataAccess(db, DATAFTX + 'a');
    inD = new DataAccess(db, DATAFTX + 'b');
    inS = new DataAccess(db, DATAFTX + 'c');
    data = d;
  }

  @Override
  public byte[] info() {
    final TokenBuilder tb = new TokenBuilder();
    tb.add(TRIE + NL);
    tb.add("- %: %\n", CREATESTEM, BaseX.flag(data.meta.ftst));
    tb.add("- %: %\n", CREATECS, BaseX.flag(data.meta.ftcs));
    tb.add("- %: %\n", CREATEDC, BaseX.flag(data.meta.ftdc));
    final long l = inN.length() + inD.length() + inS.length();
    tb.add(SIZEDISK + Performance.format(l, true) + NL);
    final IndexStats stats = new IndexStats();
    addOccs(0, stats, EMPTY);
    stats.print(tb);
    return tb.finish();
  }

  @Override
  public int nrIDs(final IndexToken ind) {
    // skip count of queries which stretch over multiple index entries
    final FTTokenizer fto = (FTTokenizer) ind;
    if(fto.fz || fto.wc) return 1;
    final byte[] tok = fto.get();
    final int id = cache.id(tok);
    if(id > 0) return cache.getSize(id);

    int size = 0;
    long poi = 0;
    final int[] ne = nodeId(0, tok);
    if(ne != null && ne[ne.length - 1] > 0) {
      size = ne[ne.length - 1];
      poi = did;
    }
    cache.add(tok, size, poi);
    return size;
  }

  @Override
  public IndexIterator ids(final IndexToken ind) {
    final FTTokenizer ft = (FTTokenizer) ind;
    final byte[] tok = ft.get();

    // support fuzzy search
    if(ft.fz) {
      int k = Prop.lserr;
      if(k == 0) k = tok.length >> 2;
      return fuzzy(0, null, -1, tok, 0, 0, 0, k);
    }

    // support wildcards
    if(ft.wc) {
      final int pw = indexOf(tok, '.');
      if(pw != -1) return wc(tok, pw);
    }

    // return cached or new result
    final int id = cache.id(tok);
    return id == 0 ? get(0, tok) :
      FTFuzzy.data(cache.getPointer(id), cache.getSize(id), inD, data);
  }

  @Override
  public synchronized void close() throws IOException {
    inD.close();
    inS.close();
    inN.close();
  }

  /**
   * Traverses the trie and returns a result iterator.
   * @param id on node array (in main memory)
   * @param searchNode search nodes value
   * @return int[][] array with pre-values and corresponding positions
   * for each pre-value
   */
  private IndexArrayIterator get(final int id, final byte[] searchNode) {
    if(searchNode == null || searchNode.length == 0)
      return IndexArrayIterator.EMP;

    final int[] ne = nodeId(id, searchNode);
    return ne == null ? IndexArrayIterator.EMP :
      FTFuzzy.data(did, ne[ne.length - 1], inD, data);
  }

  /**
   * Traverse trie and return found node for searchValue.
   * Returns data from node or null.
   * @param id on node array (in main memory)
   * @param sn search nodes value
   * @return int id on node saving the data
   */
  private int[] nodeId(final int id, final byte[] sn) {
    byte[] vsn = sn;

    // read data entry from disk
    final int[] ne = entry(id);

    if(id != 0) {
      int i = 0;
      while(i < vsn.length && i < ne[0] && ne[i + 1] == vsn[i]) i++;
      // node not contained
      if(i != ne[0]) return null;
      // leaf node found with appropriate value
      if(i == vsn.length) return ne;

      // cut valueSearchNode for value current node
      final byte[] tmp = new byte[vsn.length - i];
      System.arraycopy(vsn, i, tmp, 0, tmp.length);
      vsn = tmp;
    }

    // scan succeeding node
    final int pos = insPos(ne, vsn[0]);
    return pos < 0 ? null : nodeId(ne[pos], vsn);
  }

  /**
   * Collects all tokens and their sizes found in the index structure.
   * @param cn id on nodeArray, current node
   * @param st statistics reference
   * @param tok current token
   */
  private void addOccs(final int cn, final IndexStats st, final byte[] tok) {
    final int[] ne = entry(cn);
    byte[] nt = tok;
    if(cn > 0) {
      nt = new byte[tok.length + ne[0]];
      System.arraycopy(tok, 0, nt, 0, tok.length);
      for (int i = 0; i < ne[0]; i++) nt[tok.length + i] = (byte) ne[i + 1];
      final int size = ne[ne.length - 1];
      if(size > 0 && st.adding(size)) st.add(nt);
    }
    if(hasNextNodes(ne)) {
      for(int i = ne[0] + 1; i < ne.length - 1; i += 2) addOccs(ne[i], st, nt);
    }
  }

  /**
   * Read node entry from disk.
   * @param id on node array (in main memory)
   * @return node entry from disk
   */
  private int[] entry(final long id) {
    int sp = inS.readInt(id * 4);
    final int ep = inS.readInt((id + 1) * 4);
    final IntList il = new IntList();
    final int l = inN.readByte(sp++);
    il.add(l);
    for(int j = 0; j < l; j++) il.add(inN.readByte(sp++));

    // inner node
    while(sp + 9 < ep) {
      il.add(inN.readInt(sp));
      sp += 4;
      il.add(inN.readByte(sp));
      sp += 1;
    }
    il.add(inN.readInt(ep - 9));
    did = inN.read5(ep - 5);
    return il.finish();
  }

  /**
   * Checks whether a node is an inner node or a leaf node.
   * @param ne current node entry.
   * @return boolean leaf node or inner node
   */
  private boolean hasNextNodes(final int[] ne) {
    return ne[0] + 1 < ne.length - 1;
  }

  /**
   * Uses linear search for finding inserting position.
   * returns:
   * -1 if no inserting position was found
   * 0 if any successor exists, or 0 is inserting position
   * n here to insert
   * n and found = true, if nth item is occupied and here to insert
   *
   * @param cne current node entry
   * @param ins byte looking for
   * @return inserting position
   */
  private int insPos(final int[] cne, final int ins) {
    int i = cne[0] + 1;
    final int s = cne.length - 1;
    while(i < s && diff(cne[i + 1], ins) < 0) i += 2;
    return i < s && cne[i + 1] == ins ? i : -1;
  }

  /** saves astericsWildCardTraversing result
  has to be re-init each time (before calling method). */
  private IndexArrayIterator idata;

  /** counts number of chars skip per astericsWildCardTraversing. */
  private int countSkippedChars;

  /**
   * Looking up node with value, which match ending.
   * The parameter lastFound shows, whether chars were found in last recursive
   * call, which correspond to the ending, consequently those chars are
   * considered, which occur successive in ending.
   * pointerNode shows the position comparison between value[nodeId] and
   * ending starts
   * pointerEnding shows the position comparison between ending and
   * value[nodeId] starts
   *
   * @param node id on node
   * @param ending ending of value
   * @param lastFound boolean if value was found in last run
   * @param pointerNode pointer on current node
   * @param pointerEnding pointer on value ending
   */
  private void wc(final int node, final byte[] ending,
    final boolean lastFound, final int pointerNode, final int pointerEnding) {

    int j = pointerEnding;
    int i = pointerNode;
    boolean last = lastFound;
    final int[] ne = entry(node);
    final long tdid = did;

    // wildcard at the end
    if(ending == null || ending.length == 0) {
      // save data current node
      if (ne[ne.length - 1] > 0) {
        idata = IndexArrayIterator.merge(
            FTFuzzy.data(tdid, ne[ne.length - 1], inD, data), idata);
      }
      if (hasNextNodes(ne)) {
        // preorder traversal through trie
        for (int t = ne[0] + 1; t < ne.length - 1; t += 2) {
          wc(ne[t], null, last, 0, 0);
        }
      }
      return;
    }

    // compare chars current node and ending
      // skip all unlike chars, if any suitable was found
      while(!last && i < ne[0] + 1 && ne[i] != ending[j]) i++;

      // skip all chars, equal to first char
      while(i + ending.length < ne[0] + 1 && ne[i + 1] == ending[0]) i++;

      countSkippedChars = countSkippedChars + i - pointerNode - 1;

      while(i < ne[0] + 1 && j < ending.length && ne[i] == ending[j]) {
        i++;
        j++;
        last = true;
      }

    // not processed all chars from node, but all chars from
    // ending were processed or root
    if(node == 0 || j == ending.length && i < ne[0] + 1) {
      if (!hasNextNodes(ne)) {
        countSkippedChars = 0;
        return;
      }

      //final int[] nextNodes = getNextNodes(ne);
      // preorder search in trie
      for (int t = ne[0] + 1; t < ne.length - 1; t += 2) {
        wc(ne[t], ending, false, 1, 0);
      }
      countSkippedChars = 0;
      return;
    } else if(j == ending.length && i == ne[0] + 1) {
      // all chars form node and all chars from ending done
      idata = IndexArrayIterator.merge(
          FTFuzzy.data(tdid, ne[ne.length - 1], inD, data), idata);

      countSkippedChars = 0;

      // node has successors and is leaf node
      if (hasNextNodes(ne)) {
        // preorder search in trie
        for (int t = ne[0] + 1; t < ne.length - 1; t += 2) {
          if(j == 1) {
            wc(ne[t], ending, false, 0, 0);
          }
          wc(ne[t], ending, last, 0, j);
        }
      }

      return;
    } else if(j < ending.length && i < ne[0] + 1) {
      // still chars from node and still chars from ending left, pointer = 0 and
      // restart searching
      if (!hasNextNodes(ne)) {
        countSkippedChars = 0;
        return;
      }

      // restart searching at node, but value-position i
      wc(node, ending, false, i + 1, 0);
      return;
    } else if(j < ending.length &&  i == ne[0] + 1) {
      // all chars form node processed, but not all chars from processed

      // move pointer and go on
      if (!hasNextNodes(ne)) {
        countSkippedChars = 0;
        return;
      }

      // preorder search in trie
      for (int t = ne[0] + 1; t < ne.length - 1; t += 2) {
        // compare only first char from ending
        if(j == 1) {
          wc(ne[t], ending, last, 1, 0);
        }
        wc(ne[t], ending, last, 1, j);
      }
    }
  }

  /**
   * Save number compared chars at wildcard search.
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
   * @param sn search nodes value
   * @param pos position
   * @return data int[][]
   */
  private IndexArrayIterator wc(final byte[] sn, final int pos) {
    // init counter
    counter = new int[2];
    return wc(0, sn, pos, false);
  }

  /**
   * Supports different wildcard operators: ., .+, .* and .?.
   * PosWildCard points on bytes[], at position, where .  is situated
   * recCall flags recursive calls
   *
   * @param cn current node
   * @param sn value looking for
   * @param posw wildcards position
   * @param recCall first call??
   * @return data result ids
   */
  private IndexArrayIterator wc(final int cn, final byte[] sn,
      final int posw, final boolean recCall) {

    final byte[] vsn = sn;
    byte[] aw = null;
    byte[] bw = null;

    final int currentLength = 0;
    int resultNode;

    IndexArrayIterator d = IndexArrayIterator.EMP;
    // wildcard not at beginning
    if(posw > 0) {
      // copy part before wildcard
      bw = new byte[posw];
      System.arraycopy(vsn, 0, bw, 0, posw);
      resultNode = wc(cn, bw);
      if(resultNode == -1) return IndexArrayIterator.EMP;
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

      d = get(0, sc);

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
      d = IndexArrayIterator.merge(wc(0, sc, posw, false), d);
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
        d = get(0, searchChar);
        // all chars from valueSearchNode are contained in trie
        if(bw != null && counter[1] != bw.length) return d;
      }

      // delete data
      idata = IndexArrayIterator.EMP;
      wc(resultNode, aw, false, counter[0], 0);
      return IndexArrayIterator.merge(d, idata);
    }
    
    if(wildcard == '+') {
      // append 1 or more symbols
      final int[] rne = entry(resultNode);
      final byte[] nvsn = new byte[vsn.length + 1];
      int l = 0;
      if (bw != null) {
        System.arraycopy(bw, 0, nvsn, 0, bw.length);
        l = bw.length;
      }

      if (0 < vsn.length - posw - 2) {
        System.arraycopy(vsn, posw + 2, nvsn, posw + 3, vsn.length - posw - 2);
      }

      nvsn[l + 1] = '.';
      nvsn[l + 2] = '*';
      IndexArrayIterator tmpres = IndexArrayIterator.EMP;
      // append 1 symbol
      // not completely processed (value current node)
      if(rne[0] > counter[0] && resultNode > 0) {
        // replace wildcard with value from currentCompressedTrieNode
        nvsn[l] = (byte) rne[counter[0] + 1];
        tmpres = wc(nvsn, l + 1);
      } else if(rne[0] == counter[0] || resultNode == 0) {
        // all chars from nodes[resultNode] are computed
        // any next values existing
        if(!hasNextNodes(rne)) return IndexArrayIterator.EMP;

        for (int t = rne[0] + 1; t < rne.length - 1; t += 2) {
          nvsn[l] = (byte) rne[t + 1];
          tmpres = IndexArrayIterator.merge(wc(nvsn, l + 1), tmpres);
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
      final IndexArrayIterator resultData = get(0, vsn);
      // save nodeValues for recursive method call
      if(resultData.size() != 0 && recCall) {
        valuesFound = new byte[] {(byte) rne[counter[0] + 1]};
      }
      return resultData;
    }

    if(rne[0] == counter[0] || resultNode == 0) {
      // all chars from nodes[resultNode] are computed
      // any next values existing
      if(!hasNextNodes(rne)) return IndexArrayIterator.EMP;

      IndexArrayIterator tmpNode = IndexArrayIterator.EMP;
      aw = new byte[vsn.length - posw];
      System.arraycopy(vsn, posw + 1, aw, 1, aw.length - 1);

      // simple method call
      if(!recCall) {
        for (int t = rne[0] + 1; t < rne.length - 1; t += 2) {
          aw[0] = (byte) rne[t + 1];
          tmpNode = IndexArrayIterator.merge(get(rne[t], aw), tmpNode);
        }
        return tmpNode;
      }

      // method call for .+ wildcard
      valuesFound = new byte[rne.length - 1 - rne[0] - 1];
      for (int t = rne[0] + 1; t < rne.length - 1; t += 2) {
        // replace first letter
        aw[0] = (byte) rne[t + 1];
        valuesFound[t - rne[0] - 1] = (byte) rne[t + 1];
        tmpNode = IndexArrayIterator.merge(get(rne[t], aw), tmpNode);
      }
    }
    return IndexArrayIterator.EMP;
  }

  /**
   * Traverse trie and return found node for searchValue; returns last
   * touched node.
   *
   * @param cn int
   * @param sn int
   * @return id int last touched node
   */
  private int wc(final int cn, final byte[] sn) {
    byte[]vsn = sn;
    final int[] cne = entry(cn);
    if(cn != 0) {
      counter[1] += cne[0];

      int i = 0;
      while(i < vsn.length && i < cne[0] && cne[i + 1] == vsn[i]) i++;

      if(cne[0] == i) {
        if(vsn.length == i) {
          // leaf node found with appropriate value
          counter[0] = i;
          return cn;
        }

        // cut valueSearchNode for value current node
        final byte[] tmp = new byte[vsn.length - i];
        System.arraycopy(vsn, i, tmp, 0, tmp.length);
        vsn = tmp;

        // scan successors currentNode
        final int pos = insPos(cne, vsn[0]);
        if(pos >= 0) return wc(cne[pos], vsn);
      }
      // node not contained
      counter[0] = i;
      counter[1] = counter[1] - cne[0] + i;
      return cn;
    }
    
    // scan successors current node
    final int pos = insPos(cne, vsn[0]);
    if(pos >= 0) return wc(cne[pos], vsn);

    // node not contained
    counter[0] = -1;
    counter[1] = -1;
    return -1;
  }

  /**
   * Traverse trie and return found node for searchValue; returns data
   * from node or null.
   *
   * @param cn int current node id
   * @param crne byte[] current node entry (of cn)
   * @param crdid long current pointer on data
   * @param sn byte[] search nodes value
   * @param d int counter for deletions
   * @param p int counter for pastes
   * @param r int counter for replacements
   * @param c int counter sum of errors
   * @return int[][]
   */
  private IndexArrayIterator fuzzy(final int cn, final int[] crne,
      final long crdid, final byte[] sn, final int d, final int p,
      final int r, final int c) {
    byte[] vsn = sn;
    int[] cne = crne;
    long cdid = crdid;
    if (cne == null) {
      cne = entry(cn);
      cdid = did;
    }

    if(cn != 0) {
      // not root node
      int i = 0;
      while(i < vsn.length && i < cne[0] && cne[i + 1] == vsn[i]) i++;

      if(cne[0] == i) {
        // node entry processed complete
        if(vsn.length == i) {
          // leaf node found with appropriate value
          if (c < d + p + r) return IndexArrayIterator.EMP;
          
          IndexArrayIterator ld = IndexArrayIterator.EMP;
          ld = FTFuzzy.data(cdid, cne[cne.length - 1], inD, data);
          if (hasNextNodes(cne)) {
            for (int t = cne[0] + 1; t < cne.length - 1; t += 2) {
              ld = IndexArrayIterator.merge(fuzzy(cne[t], null, -1,
                  new byte[]{(byte) cne[t + 1]}, d, p + 1, r, c), ld);
            }
          }
          return ld;
        }

        IndexArrayIterator ld = IndexArrayIterator.EMP;
        byte[] b;
        if (c > d + p + r) {
          // delete char
          b = new byte[vsn.length - 1];
          System.arraycopy(vsn, 0, b, 0, i);
          ld = IndexArrayIterator.merge(
              fuzzy(cn, cne, cdid, b, d + 1, p, r, c), ld);
        }

        // cut valueSearchNode for value current node
        final byte[] tmp = new byte[vsn.length - i];
        System.arraycopy(vsn, i, tmp, 0, tmp.length);
        vsn = tmp;

        // scan successors currentNode
        int[] ne = null;
        long tdid = -1;
        if (hasNextNodes(cne)) {
          for (int k = cne[0] + 1; k < cne.length - 1; k += 2) {
            if (cne[k + 1] == vsn[0]) {
              ne = entry(cne[k]);
              tdid = did;
              b = new byte[vsn.length];
              System.arraycopy(vsn, 0, b, 0, vsn.length);
              ld = IndexArrayIterator.merge(
                  fuzzy(cne[k], ne, tdid, b, d, p, r, c), ld);
            }

            if (c > d + p + r) {
              if (ne == null) {
                ne = entry(cne[k]);
                tdid = did;
              }
              // paste char
              b = new byte[vsn.length + 1];
              b[0] = (byte) cne[k + 1];
              System.arraycopy(vsn, 0, b, 1, vsn.length);
              ld = IndexArrayIterator.merge(fuzzy(cne[k], ne, tdid,
                  b, d, p + 1, r, c), ld);

              if (vsn.length > 0) {
                // delete char
                b = new byte[vsn.length - 1];
                System.arraycopy(vsn, 1, b, 0, b.length);
                ld = IndexArrayIterator.merge(fuzzy(cne[k], ne, tdid,
                    b, d + 1, p, r, c), ld);
                // replace char
                b = new byte[vsn.length];
                System.arraycopy(vsn, 1, b, 1, vsn.length - 1);
                b[0] = (byte) ne[1];
                ld = IndexArrayIterator.merge(fuzzy(cne[k], ne, tdid,
                    b, d, p, r + 1, c), ld);
              }
            }
          }
        }
        return ld;
      }
      
      IndexArrayIterator ld = IndexArrayIterator.EMP;

      if(c > d + p + r) {
        // paste char
        byte[] b = new byte[vsn.length + 1];
        System.arraycopy(vsn, 0, b, 0, i);
        b[i] = (byte) cne[i + 1];
        System.arraycopy(vsn, i, b, i + 1, vsn.length - i);

        ld = fuzzy(cn, cne, cdid, b, d, p + 1, r, c);

        if (vsn.length > 0 && i < vsn.length) {
          // replace
          b = new byte[vsn.length];
          System.arraycopy(vsn, 0, b, 0, vsn.length);

          b[i] = (byte) cne[i + 1];
          ld = IndexArrayIterator.merge(fuzzy(cn, cne, cdid,
              b, d, p, r + 1, c), ld);
          if (vsn.length > 1) {
            // delete char
            b = new byte[vsn.length - 1];
            System.arraycopy(vsn, 0, b, 0, i);
            System.arraycopy(vsn, i + 1, b, i, vsn.length - i - 1);
            ld = IndexArrayIterator.merge(fuzzy(cn, cne, cdid,
                b, d + 1, p, r, c), ld);
          }
        }
      }
      return ld;

    } else {
      int[] ne = null;
      long tdid = -1;
      IndexArrayIterator ld = IndexArrayIterator.EMP;

      byte[] b;
      if(hasNextNodes(cne)) {
        for (int k = cne[0] + 1; k < cne.length - 1; k += 2) {
          if (cne[k + 1] == vsn[0]) {
            ne = entry(cne[k]);
            tdid = did;
            b = new byte[vsn.length];
            System.arraycopy(vsn, 0, b, 0, vsn.length);
            ld = IndexArrayIterator.merge(
                fuzzy(cne[k], ne, tdid, b, d, p, r, c), ld);
          }
          if (c > d + p + r) {
            if (ne == null) {
              ne = entry(cne[k]);
              tdid = did;
            }
            // paste char
            b = new byte[vsn.length + 1];
            b[0] = (byte) ne[1];
            System.arraycopy(vsn, 0, b, 1, vsn.length);
            ld = IndexArrayIterator.merge(fuzzy(cne[k], ne, tdid,
                b, d, p + 1, r, c), ld);

            if (vsn.length > 0) {
              // delete char
              b = new byte[vsn.length - 1];
              System.arraycopy(vsn, 1, b, 0, b.length);
              ld = IndexArrayIterator.merge(fuzzy(cne[k], ne, tdid,
                  b, d + 1, p, r, c), ld);
                // replace
              b = new byte[vsn.length];
              System.arraycopy(vsn, 1, b, 1, vsn.length - 1);
                b[0] = (byte) ne[1];
                ld = IndexArrayIterator.merge(fuzzy(cne[k], ne, tdid,
                    b, d, p, r + 1, c), ld);
            }
          }
        }
      }
      return ld;
    }
  }

  /**
   * Traverse trie and return found node for searchValue.
   * Searches case insensitive in a case sensitive trie.
   * Returns data from node or null.
   *
   * @param cn int
   * @param sn search nodes value
   * @param casesen flag for casesentive search
   * @return int id on node saving the data
  private IndexArrayIterator getNodeFromCSTrieNew(final int cn,
      final byte[] sn, final boolean casesen) {
    byte[] vsn = sn;
    long ldid;

    // read data entry from disk
    final int[] ne = getNodeEntry(cn);

    if(cn != 0) {
      int i = 0;
      while(i < vsn.length && i < ne[0]
           && diff((byte) lc(ne[i + 1]), lc(vsn[i])) == 0) {
        i++;
      }

      if(ne[0] == i) {
        if(vsn.length == i) {
          // leaf node found with appropriate value
          ldid = did;
          return FTFuzzy.getData(ldid, ne[ne.length - 1], inD, data);
        } else {
          // cut valueSearchNode for value current node
          final byte[] tmp = new byte[vsn.length - i];
          System.arraycopy(vsn, i, tmp, 0, tmp.length);
          vsn = tmp;

          // scan successors currentNode
          final IntList p = getInsPosLinCSFNew(ne, vsn[0], casesen);
          if(!found) {
            if (diff((byte) lc(ne[p.list[0] + 1]), vsn[0]) != 0)
              return IndexArrayIterator.EMP;
            else
              return getNodeFromCSTrieNew(ne[p.list[0]], vsn, casesen);
          } else {
            IndexArrayIterator d = IndexArrayIterator.EMP;
            for (int z = 0; z < p.size; z++) {
              d = IndexArrayIterator.merge(
                  getNodeFromCSTrieNew(ne[p.list[z]], vsn, casesen), d);
            }
            return d;
          }
        }
      } else {
        // node not contained
        return IndexArrayIterator.EMP;
      }
    } else {
      // scan successors currentNode
      final IntList p = getInsPosLinCSFNew(ne, vsn[0], casesen);
      if(!found) {
        if (diff((byte) lc(ne[p.list[0] + 1]), vsn[0]) != 0)
          return IndexArrayIterator.EMP;
        else
          return getNodeFromCSTrieNew(ne[p.list[0]], vsn, casesen);
      } else {
        IndexArrayIterator d = IndexArrayIterator.EMP;
        for (int z = 0; z < p.size; z++) {
          d = IndexArrayIterator.merge(
              getNodeFromCSTrieNew(ne[p.list[z]], vsn, casesen), d);
        }
        return d;
      }
    }
  }
   */

  /**
   * Uses linear search for finding inserting position.
   * values are order like this:
   * A, B, T, Y, a, b, t, u, z
   * returns:
   * 0 if any successor exists, or 0 is inserting position
   * n here to insert
   * n and found = true, if nth item is occupied and here to insert
   * BASED ON FINISHED STRUCTURE ARRAYS!!!!
   *
   * @param cne current node entry
   * @param toInsert value to be inserted
   * @param casesen flag for case sensitve search
   * @return inserting position
  private IntList getInsPosLinCSFNew(final int[] cne,
      final int toInsert, final boolean casesen) {
    found = false;

    int i = cne[0] + 1;
    final int s = cne.length - 1;
    IntList ial = new IntList();
    if (s == i) {
      ial.add(i);
      return ial;
    }
    while (i < s) {
      if(diff((byte) cne[i + 1], (byte) toInsert) == 0) {
          found = true;
          ial.add(i);
          if (casesen) return ial;
      } else if(!casesen && diff((byte) lc(cne[i + 1]),
            (byte) lc(toInsert)) == 0) {
            found = true;
            ial.add(i);
      }
      i += 2;
      if (ial.size == 2) return ial;
    }

    return ial;
  }

  /*
   * Performs a range query.
   * @param tok index term
   * @return results
  private IndexIterator idRange(final RangeToken tok) {
    final double from = tok.min;
    final double to = tok.min;

    final byte[] tokFrom = token(from);
    final byte[] tokTo = token(to);
    final int[] tokF = new int[tokFrom.length];
    int tokFID = tokFrom.length;
    final int[] tokT = new int[tokTo.length];
    int tokTID = tokTo.length;
    for (int i = 0; i < tokF.length; i++) {
      tokF[i] = tokFrom[i];
      if (tokFrom[i] == '.') tokFID = i;
    }

    for (int i = 0; i < tokTo.length; i++) {
      tokT[i] = tokTo[i];
      if (tokTo[i] == '.') tokTID = i;
    }

    //int[][] dt = null;
    IndexArrayIterator dt = IndexArrayIterator.EMP;

    if (ftdigit(tokFrom) && ftdigit(tokTo)) {
      final int td = tokTo.length - tokFrom.length;
      final int[] ne = getNodeEntry(0);
      if (hasNextNodes(ne)) {
        for (int i = ne[0] + 1; i < ne.length - 1; i += 2) {
          if (letter(ne[i + 1]))
            //return new IndexArrayIterator(extractIDsFromData(dt));
            return dt;

          if (ne[i + 1] != tokFrom[0] && ne[i + 1] != tokTo[0]) {
            if (tokTID == tokFID) {
              if (ne[i + 1] > tokFrom[0] && ne[i + 1] < tokTo[0]) {
                dt = getAllNodesWithLevel(ne[i], tokFID,
                    tokTID, dt, false);
              }
            } else {
              int lb;
              final int ub = (ne[i + 1] < tokTo[0]) ? tokTID : tokTID - 1;
              if (td > 1) {
                lb = (ne[i + 1] < tokFrom[0]) ? tokFID + 1 : tokFID;
                dt = getAllNodesWithLevel(ne[i], lb, ub, dt, false);
              } else {
                lb = (ne[i + 1] < tokFrom[0]) ?
                    ((ne[i + 1] < tokTo[0]) ? tokFID + 1 : -1) : tokFID;
                if (lb > -1)
                  dt = getAllNodesWithLevel(ne[i], lb, ub, dt, false);
              }
            }
          } else {
            dt = getAllNodesWithinBounds(
                ne[i], new int[tokTo.length], tokT,
                tokTID, tokF, tokFID, 0, dt);
          }
        }
      }
    } else if (XMLToken.isNMToken(tokFrom) && XMLToken.isNMToken(tokTo)) {
       idPosRangeText(tokF, tokT);
    }
    return dt;
  }

  /**
   * Checks if the specified token is a digit.
   * @param tok the letter to be checked
   * @return result of comparison
  private static boolean ftdigit(final byte[] tok) {
    boolean d = false;
    for(final byte t : tok) if(!digit(t)) {
      if (!d && t == '.') d = true;
      else return false;
    }
    return true;
  }

  /**
   * Returns all ids that are in the range of tok0 and tok1.
   * @param tokFrom token defining range start
   * @param tokTo token defining range end
   * @return number of ids
  private IndexArrayIterator idPosRangeText(final int[] tokFrom,
      final int[] tokTo) {
    // you have to backup all passed nodes
    // and maybe the next child of the current node.
    final int[][] idNNF = new int[2][tokFrom.length + 1];
    final int[][] idNNT = new int[2][tokTo.length];
    int c = 0;
    int b;
    //int[][] dt = null;
    IndexArrayIterator dt = null;
    int[] ne;
    long ldid;

    cl = 0;
    // find bound node at upper range
    b = getNodeIdsForRQ(0, tokTo, c, idNNF);
    if (b == -1) {
      // there is no node with value tokTo
      int k = tokTo.length - 1;

      // find other node
      while (k > -1 && idNNT[0][k] == 0) k--;
      if (k > -1) b = idNNT[0][k];
      else b = -1;
    } else {
      ne = getNodeEntry(b);
      ldid = did;
      dt = FTFuzzy.getData(ldid, ne[ne.length - 1], inD, data);
      dt = getAllNodes(b, b, dt);
    }

    c = 0;
    cl = 0;
    int id;
    id = getNodeIdsForRQ(0, tokFrom, c, idNNF);
    c = 0;
    // start traversing with id of tokFrom
    // reduce minimal level, every following node has to be checked
    // with same or bigger length than tokFrom
    if (id > -1) {
      ne = getNodeEntry(id);
      ldid = did;
      final IndexArrayIterator tmp =
        FTFuzzy.getData(ldid, ne[ne.length - 1], inD, data);
      dt = IndexArrayIterator.merge(getAllNodes(id, b, tmp), dt);
    }

    for (int i = 0; i < idNNF[0].length; i++) {
      if (i > 0 && idNNF[0][i] == 0 && idNNF[1][i] == 0) break;
      ne = getNodeEntry(idNNF[0][i]);
      ldid = did;
      if (ne.length - 2 - ne[0] / 2 >= idNNF[1][i]) {
        for (int k = idNNF[1][i]; k < ne.length - 2 - ne[0]; k += 2)
          dt = IndexArrayIterator.merge(getAllNodes(ne[k], b, dt), dt);
          //dt = calculateFTOr(dt, getAllNodes(ne[k], b, dt));
       }
    }
    return dt;
  }

  /** Count current level.
  private int cl = 0;

  /**
   * Parses the trie and backups each passed node its first/next child node
   * in idNN. If searchNode is contained, the corresponding node id is
   * returned, else - 1.
   *
   * @param cn int current node
   * @param searchNode search nodes value
   * @param c int count number of passed nodes
   * @param idNN backup passed nodes first/next child
   * @return int id on node saving the data
  private int getNodeIdsForRQ(final int cn,
      final int[] searchNode, final int c, final int[][] idNN) {

    int[] vsn = searchNode;

    // read data entry from disk
    final int[] ne = getNodeEntry(cn);

    if(cn != 0) {
      int i = 0;
      // read data entry from disk
      while(i < vsn.length && i < ne[0]
           && eq((byte) ne[i + 1], (byte) vsn[i])) {
        i++;
        cl++;
      }

      if(ne[0] == i) {
        if(vsn.length == i) {
          // leaf node found with appropriate value
          if (hasNextNodes(ne)) {
              // node is not a leaf node
              idNN[0][c] = cn;
              idNN[1][c] = 0;
          }
          return cn;
        } else {
          // cut valueSearchNode for value current node
          final int[] tmp = new int[vsn.length - i];
          System.arraycopy(vsn, i, tmp, 0, tmp.length);
          vsn = tmp;

          // scan successors currentNode
          final int position = getInsertingPosition(ne, vsn[0]);
          if(!found) {
            // node not contained
            return -1;
          } else {
            idNN[0][c] = cn;
            idNN[1][c] = position + 2; // +1
            return getNodeIdsForRQ(ne[position],
                vsn, c + 1, idNN);
          }
        }
      } else {
        // node not contained
        return -1;
      }
    } else {
      // scan successors current node
      final int position = getInsertingPosition(ne, vsn[0]);
      if(!found) {
        // node not contained
        return -1;
      } else {
        idNN[0][c] = cn;
        idNN[1][c] = position + 2;
        return getNodeIdsForRQ(ne[position],
            vsn, c + 1, idNN);
      }
    }
  }

  /**
   * Returns the index of a '.' out of a node entry.
   * The ne[index + 1] = '.'.
   * @param ne current node entry
   * @return index of '.' - 1
  private int getIndexDotNE(final int[] ne) {
    int i = 1;
    while (i < ne[0] + 1 && ne[i] != '.') i++;
    if (i == ne[0] + 1) return ne[0];
    return i - 1;
  }

  /**
   * Returns the index of a dot in v.
   * Looks only at c ints in v.
   *
   * @param v int[] value
   * @param c int look at c ints in v
   * @return i int index of dot
  private int getIndexDot(final int[] v, final int c) {
    int i = 0;
    while (i < c && v[i] != '.') i++;
    return i;
  }

  /**
   * Checks whether lb < v.
   *
   * @param v byte[] value
   * @param vid index of dot in v
   * @param lb lower bound of a range
   * @param lbid of dot in lb
   * @return boolean result of constraint check
  private boolean checkLBConstrain(final int[] v, final int vid,
      final int[] lb, final int lbid) {
    if (vid < lbid) return false;
    else if (vid > lbid) return true;

    int i = 0;
    while (i < lb.length)  {
      if (v[i] > lb[i]) return true;
      else if (v[i] < lb[i]) return false;
      i++;
    }
    return true;
  }

  /**
   * Checks whether lb < v.
   * Used for double values.
   *
   * @param v byte[] value
   * @param vid index of dot in v
   * @param lb lower bound of a range
   * @param lbid index of dot in lb
   * @return boolean result of constraintcheck
  private boolean checkLBConstrainDbl(final int[] v, final int vid,
      final int[] lb, final int lbid) {
    if (vid < lbid) return false;
    else if (vid > lbid) return true;

    int i = 0;
    final int l = (v.length > lb.length) ? lb.length : v.length;
    while (i < l)  {
      if (v[i] > lb[i]) return true;
      else if (v[i] < lb[i]) return false;
      i++;
    }
    return lb.length <= v.length;
  }

  /**
   * Checks whether ub > v.
   *
   * @param v byte[] value
   * @param vid index of dot in v
   * @param ub upper bound of a range
   * @param ubid index of dot in ub
   * @return boolean result of constraintcheck
  private boolean checkUBConstrain(final int[] v, final int vid,
      final  int[] ub, final int ubid) {
    if (vid < ubid) return true;

    int i = 0;
    final int l = (v.length > ub.length) ? ub.length : v.length;
    while (i < l)  {
      if (v[i] < ub[i]) return true;
      else if (v[i] > ub[i]) return false;
      i++;
    }
    return true;
  }

  /**
   * Checks whether ub > v.
   * Used for double values.
   *
   * @param v byte[] value
   * @param vid index of dot in v
   * @param ub upper bound of a range
   * @param ubid index of dot in ub
   * @return boolean result of constraint check
  private boolean checkUBConstrainDbl(final int[] v, final int vid,
      final  int[] ub, final int ubid) {
    if (vid < ubid) return true;

    int i = 0;

    while (i < ub.length)  {
      if (v[i] < ub[i]) return true;
      else if (v[i] > ub[i]) return false;
      i++;
    }
    return ub.length >= v.length;
  }

  /**
   * Parses the trie and backups each passed node that has a value
   * within the range of lb and ub.
   *
   * @param id int current node
   * @param v appended value for the current node, starting from root
   * @param ub upper bound of the range
   * @param ubid index of dot in ub
   * @param lb lower bound of the range
   * @param lbid index of dot in lb
   * @param c int count number of passed nodes
   * @param dt data found in the trie
   * @return int id on node saving the data
  private IndexArrayIterator getAllNodesWithinBounds(final int id,
      final int[] v, final int[] ub, final int ubid, final int[] lb,
      final int lbid, final int c, final IndexArrayIterator dt) {
    IndexArrayIterator dn = dt;
    final int[] ne = getNodeEntry(id);
    final long ldid = did;
    final int in = getIndexDotNE(ne);

    if (in + c < lbid) {
      // process children
      if (!hasNextNodes(ne)) return dn;
      System.arraycopy(ne, 1, v, c, ne[0]);
      for (int i = ne[0] + 1; i < ne.length - 1; i += 2) {
        dn = getAllNodesWithinBounds(
            ne[i], v, ub, ubid, lb, lbid, c + ne[0], dn);
      }
    } else if (in + c > ubid && ubid == ub.length) {
      return dn;
    } else {
      int [] vn = v;
      if (v.length < c + ne[0])
        vn = Array.resize(v, v.length, v.length << 2 + c + ne[0]);
      System.arraycopy(ne, 1, vn, c, ne[0]);
      final int vid = getIndexDot(vn, c + ne[0]);

      if (vid == c + ne[0]) {
        if (checkLBConstrain(vn, vid, lb, lbid)
            && checkUBConstrain(vn, vid, ub, ubid)) {
          dn = IndexArrayIterator.merge(
              FTFuzzy.getData(ldid, ne[ne.length - 1], inD, data), dn);
        }
      } else {
        if (checkLBConstrainDbl(vn, vid, lb, lbid)
            && checkUBConstrainDbl(vn, vid, ub, ubid)) {
          dn = IndexArrayIterator.merge(
              FTFuzzy.getData(ldid, ne[ne.length - 1], inD, data), dn);
        }
      }

      if (!hasNextNodes(ne))
        return dn;
      for (int i = ne[0] + 1; i < ne.length - 1; i += 2) {
        dn = IndexArrayIterator.merge(
            getAllNodesWithinBounds(ne[i], vn, ub, ubid, lb,
            lbid, c + ne[0], dn), dn);
      }
    }
    return dn;
  }

  /**
   * Parses the trie and returns all nodes that have a level in the range of
   * l1 and l2, each digit counts as one level.
   *
   * @param id id of the current node
   * @param l1 minimum level bound
   * @param l2 maximum level bound
   * @param dt data found in the trie
   * @param dotFound boolean flag to be set if dot was found (doubel values)
   * @return int[][] data
  private IndexArrayIterator getAllNodesWithLevel(final int id, final int l1,
      final int l2, final IndexArrayIterator dt, final boolean dotFound) {
    IndexArrayIterator dn = dt;
    final int[] ne = getNodeEntry(id);
    final long tdid = did;
    if(dotFound) {
      dn = IndexArrayIterator.merge(
          FTFuzzy.getData(tdid, ne[ne.length - 1], inD, data), dn);
      if (!hasNextNodes(ne)) return dn;
      for (int i = ne[0] + 1; i < ne.length - 2; i += 2) {
        dn = IndexArrayIterator.merge(
            getAllNodesWithLevel(ne[i], 0, 0, dn, dotFound), dn);
      }
      return dn;
    }

    final int neID = getIndexDotNE(ne);
    if (!digit(ne[1]) || neID > l2) return dn;
    if (l1 <= neID && neID <= l2)  {
      dn = IndexArrayIterator.merge(
          FTFuzzy.getData(tdid, ne[ne.length - 1], inD, data), dn);
      if (!hasNextNodes(ne)) return dn;
      for (int i = ne[0] + 1; i < ne.length - 1; i += 2) {
        dn = IndexArrayIterator.merge(getAllNodesWithLevel(ne[i], l1 - ne[0],
              l2 - ne[0], dn, ne[0] > neID), dn);
      }
    } else if (ne[0] < l1 && ne [0] == neID) {
      if (!hasNextNodes(ne)) return dn;
      for (int i = ne[0] + 1; i < ne.length - 1; i += 2) {
        dn = IndexArrayIterator.merge(
            getAllNodesWithLevel(ne[i], l1 - ne[0], l2 - ne[0], dn, false), dn);
      }
    }
    return dn;
  }

  /**
   * Collects and returns all data found at nodes in the range of cn and b.
   *
   * @param cn id on nodeArray, current node
   * @param b id on nodeArray, lastNode to check (upper bound of range)
   * @param dt data found in trie in earlier recursion steps
   * @return int[][] idpos
  private IndexArrayIterator getAllNodes(final int cn, final int b,
      final IndexArrayIterator dt) {
    if (cn !=  b) {
      IndexArrayIterator newData = dt;
      //int[][] newData = dt;
      final int[] ne = getNodeEntry(cn);
      final long tdid = did;
      final IndexArrayIterator tmp
        = FTFuzzy.getData(tdid, ne[ne.length - 1], inD, data);
      if (tmp != null) {
        newData = IndexArrayIterator.merge(dt, tmp);
      }

      if (hasNextNodes(ne)) {
        for (int i = ne[0] + 1; i < ne.length - 1; i += 2) {
          newData = getAllNodes(ne[i], b, newData);
        }
      }
      return newData;
    }
    return dt;
  }
   */
}
