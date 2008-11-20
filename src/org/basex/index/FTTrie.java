package org.basex.index;

import static org.basex.Text.*;
import static org.basex.data.DataText.*;
import java.io.IOException;
import org.basex.BaseX;
import org.basex.core.Prop;
import org.basex.data.Data;
import org.basex.io.DataAccess;
import org.basex.util.Array;
import org.basex.util.IntList;
import org.basex.util.Performance;
import org.basex.util.Token;
import org.basex.util.TokenBuilder;
import org.basex.util.XMLToken;

/**
 * This class indexes text contents in a compressed trie on disk.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
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
  /** Flag for case sensitive index. */
  private boolean cs = false;
  /** Cache for number of hits and data reference per token. */
  private final FTTokenMap cache;
  
  /**
   * Constructor, initializing the index structure.
   * @param d data reference
   * @param db name of the database
   * @throws IOException IO Exception
   */
  public FTTrie(final Data d, final String db) throws IOException {
    final String file = DATAFTX;
    inN = new DataAccess(db, file + 'a');
    inD = new DataAccess(db, file + 'b');
    inS = new DataAccess(db, file + 'c');
    data = d;
    did = -1;
    cs = data.meta.ftcs;
    cache = new FTTokenMap();
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
    addOccurrences(0, stats, new byte[] {});
    stats.print(tb);
    return tb.finish();
  }

  @Override
  public int nrIDs(final IndexToken ind) {
    // hack, should find general solution
    final FTTokenizer fto = (FTTokenizer) ind;
    if (fto.fz || fto.wc ||  fto.st || fto.cs || fto.dc) return 1;

    final byte[] tok = Token.lc(ind.get());
    final int id = cache.id(tok);
    if (id > 0) {
      return cache.getSize(id);
    } else {
      final int[] ne = getNodeIdFromTrieRecursive(0, tok);
      if (ne != null && ne[ne.length - 1] > 0) {
        cache.add(tok, ne[ne.length - 1], did);
        return ne[ne.length - 1];
      } else {
        cache.add(tok, 0, 0);
        return 0;
      }
    }
  }

  @Override
  public IndexIterator ids(final IndexToken ind) {
    if(ind.range()) return idRange((RangeToken) ind);

    final FTTokenizer ft = (FTTokenizer) ind;
    final byte[] tok = ft.get();
    if(ft.fz) {
      int k = Prop.lserr;
      if(k == 0) k = Math.max(1, tok.length >> 2);
      return getNodeFuzzy(0, null, -1, tok, 0, 0, 0, k);
    }

    if(ft.wc) {
      final int pw = Token.indexOf(tok, '.');
      if(pw != -1) {
        return getNodeFromTrieWithWildCard(tok, pw);
      }
    }

    if(!cs && ft.cs) {
      // case insensitive index create - check real case with dbdata
      IndexArrayIterator iai 
        = getNodeFromTrieRecursive(0, Token.lc(tok), false);
      if(iai == IndexArrayIterator.EMP) return iai;

      // check real case of each result node
      final FTTokenizer ftdb = new FTTokenizer();
      ftdb.st = ft.st;
      return FTFuzzy.csDBCheck(iai, data, ftdb, tok);
    }

    // check if result was cached
    final int id = cache.id(tok);
    IndexArrayIterator tmp = IndexArrayIterator.EMP;
    if (id > -1) {
      final int size = cache.getSize(id);
      final long p = cache.getPointer(id);
      return getData(size, p);
    } else {
      tmp = getNodeFromTrieRecursive(0, tok, ft.cs);
    }
    return tmp;
  }

  /**
   * Performs a range query.
   * @param tok index term
   * @return results
   */
  private IndexIterator idRange(final RangeToken tok) {
    final double from = tok.min;
    final double to = tok.min;

    final byte[] tokFrom = Token.token(from);
    final byte[] tokTo = Token.token(to);
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
          if (Token.letter(ne[i + 1]))
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
   */
  private static boolean ftdigit(final byte[] tok) {
    boolean d = false;
    for(final byte t : tok) if(!Token.digit(t)) {
      if (!d && t == '.') d = true;
      else return false;
    }
    return true;
  }

  /** Count current level. */
  private int cl = 0;

  /**
   * Returns all ids that are in the range of tok0 and tok1.
   * @param tokFrom token defining range start
   * @param tokTo token defining range end
   * @return number of ids
   */
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
      dt = getData(ne[ne.length - 1], ldid);
      //dt = getDataFromDataArray(ne[ne.length - 1], ldid);
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
      final IndexArrayIterator tmp = getData(ne[ne.length - 1], ldid);
      dt = IndexArrayIterator.merge(getAllNodes(id, b, tmp), dt);
      //final int[][] tmp = getDataFromDataArray(ne[ne.length - 1], ldid);
      //dt = calculateFTOr(dt, getAllNodes(id, b, tmp));
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

  @Override
  public synchronized void close() throws IOException {
    inD.close();
    inS.close();
    inN.close();
  }

  /**
   * Traverse trie and return found node for searchValue.
   * Returns data from node or null.
   *
   * @param cNode int
   * @param searchNode search nodes value
   * @param casesen flag for case sensitve search
   * @return int[][] array with pre-values and corresponding positions
   * for each pre-value
   */
  private IndexArrayIterator getNodeFromTrieRecursive(final int cNode,
      final byte[] searchNode, final boolean casesen) {
    if (searchNode == null || searchNode.length == 0) 
      return IndexArrayIterator.EMP;
    int[] ne = null;
    if (cs) {
      if (casesen) ne = getNodeIdFromTrieRecursive(cNode, searchNode);
      else  return getNodeFromCSTrie(cNode, searchNode);
    } else if (!casesen) ne = getNodeIdFromTrieRecursive(cNode, searchNode);

    if (ne == null) {
      return IndexArrayIterator.EMP;
    }

    final long ldid = did;
    return getData(ne[ne.length - 1], ldid);
  }

  /**
   * Traverse trie and return found node for searchValue.
   * Returns data from node or null.
   *
   * @param cn int
   * @param sn search nodes value
   * @return int id on node saving the data
   */
  private int[] getNodeIdFromTrieRecursive(final int cn, final byte[] sn) {
    byte[] vsn = sn;

    // read data entry from disk
    final int[] ne = getNodeEntry(cn);

    if(cn != 0) {
      int i = 0;
      while(i < vsn.length 
          && i < ne[0] && Token.diff((byte) ne[i + 1], vsn[i]) == 0) {
        i++;
      }

      if(ne[0] == i) {
        if(vsn.length == i) {
          // leaf node found with appropriate value
          return ne;
        } else {
          // cut valueSearchNode for value current node
          final byte[] tmp = new byte[vsn.length - i];
          System.arraycopy(vsn, i, tmp, 0, tmp.length);
          vsn = tmp;

          // scan successors currentNode
          final int pos = getInsertingPosition(ne, vsn[0]);
          if(!found) {
            // node not contained
            return null;
          } else {
            return getNodeIdFromTrieRecursive(
                ne[pos], vsn);
          }
        }
      } else {
        // node not contained
        return null;
      }
    } else {
      // scan successors current node
      final int pos = getInsertingPosition(ne, vsn[0]);
      if(!found) {
        // node not contained
        return null;
      } else {
        return getNodeIdFromTrieRecursive(ne[pos], vsn);
      }
    }
  }


  /**
   * Traverse trie and return found node for searchValue.
   * Searches case insensitive in a case sensitive trie.
   * Returns data from node or null.
   *
   * @param cn int
   * @param sn search nodes value
   * @return int id on node saving the data
   */
  private IndexArrayIterator getNodeFromCSTrie(final int cn, final byte[] sn) {
    byte[] vsn = sn;
    long ldid;

    // read data entry from disk
    final int[] ne = getNodeEntry(cn);

    if(cn != 0) {
      int i = 0;
      while(i < vsn.length && i < ne[0] 
           && Token.diff((byte) Token.lc(ne[i + 1]), vsn[i]) == 0) {
        i++;
      }

      if(ne[0] == i) {
        if(vsn.length == i) {
          // leaf node found with appropriate value
          ldid = did;
          return getData(ne[ne.length - 1], ldid);
        } else {
          // cut valueSearchNode for value current node
          final byte[] tmp = new byte[vsn.length - i];
          System.arraycopy(vsn, i, tmp, 0, tmp.length);
          vsn = tmp;

          // scan successors currentNode
          final int p = getInsPosLinCSF(ne, vsn[0]);
          if(!found) {
            //if (Token.lc(ne[p + 1]) != vsn[0])
            if (Token.diff((byte) Token.lc(ne[p + 1]), vsn[0]) != 0)
              return null;
            else
              return getNodeFromCSTrie(ne[p], vsn);
          } else {
            IndexArrayIterator d = getNodeFromCSTrie(ne[p], vsn);
            if (Token.diff((byte) Token.lc(ne[p + 3]), vsn[0]) == 0) {
              d = IndexArrayIterator.merge(
                  getNodeFromCSTrie(ne[p + 2], vsn), d);
            }
            return d;
          }
        }
      } else {
        // node not contained
        return null;
      }
    } else {
      // scan successors currentNode
      final int p = getInsPosLinCSF(ne, vsn[0]);
      if(!found) {
        if (Token.diff((byte) Token.lc(ne[p + 1]), vsn[0]) != 0)
          return null;
        else
          return getNodeFromCSTrie(ne[p], vsn);
      } else {
        IndexArrayIterator d = getNodeFromCSTrie(ne[p], vsn);
        if (Token.diff((byte) Token.lc(ne[p + 3]), vsn[0]) == 0) {
          d = IndexArrayIterator.merge(
              getNodeFromCSTrie(ne[p + 2], vsn), d);
        }
        return d;
      }
    }
  }

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
   */
  private int getNodeIdsForRQ(final int cn,
      final int[] searchNode, final int c, final int[][] idNN) {

    int[] vsn = searchNode;

    // read data entry from disk
    final int[] ne = getNodeEntry(cn);

    if(cn != 0) {
      int i = 0;
      // read data entry from disk
      while(i < vsn.length && i < ne[0] 
           && Token.eq((byte) ne[i + 1], (byte) vsn[i])) {
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
   */
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
          dn = IndexArrayIterator.merge(getData(ne[ne.length - 1], ldid), dn);
        }
      } else {
        if (checkLBConstrainDbl(vn, vid, lb, lbid)
            && checkUBConstrainDbl(vn, vid, ub, ubid)) {
          dn = IndexArrayIterator.merge(getData(ne[ne.length - 1], ldid), dn);
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
   * Returns the index of a '.' out of a node entry.
   * The ne[index + 1] = '.'.
   * @param ne current node entry
   * @return index of '.' - 1
   */
  private int getIndexDotNE(final int[] ne) {
    int i = 1;
    while (i < ne[0] + 1 && ne[i] != '.') i++;
    if (i == ne[0] + 1) return ne[0];
    return i - 1;
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
   */
  private IndexArrayIterator getAllNodesWithLevel(final int id, final int l1,
      final int l2, final IndexArrayIterator dt, final boolean dotFound) {
    IndexArrayIterator dn = dt;
    final int[] ne = getNodeEntry(id);
    final long tdid = did;
    if(dotFound) {
      dn = IndexArrayIterator.merge(getData(ne[ne.length - 1], tdid), dn);
      if (!hasNextNodes(ne)) return dn;
      for (int i = ne[0] + 1; i < ne.length - 2; i += 2) {
        dn = IndexArrayIterator.merge(
            getAllNodesWithLevel(ne[i], 0, 0, dn, dotFound), dn);
      }
      return dn;
    }

    final int neID = getIndexDotNE(ne);
    if (!Token.digit(ne[1]) || neID > l2) return dn;
    if (l1 <= neID && neID <= l2)  {
      dn = IndexArrayIterator.merge(getData(ne[ne.length - 1], tdid), dn);
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
   * Returns the index of a dot in v.
   * Looks only at c ints in v.
   *
   * @param v int[] value
   * @param c int look at c ints in v
   * @return i int index of dot
   */
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
   */
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
   */
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
   */
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
   */
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
   * Collects and returns all data found at nodes in the range of cn and b.
   *
   * @param cn id on nodeArray, current node
   * @param b id on nodeArray, lastNode to check (upper bound of range)
   * @param dt data found in trie in earlier recursion steps
   * @return int[][] idpos
   */
  private IndexArrayIterator getAllNodes(final int cn, final int b, 
      final IndexArrayIterator dt) {
    if (cn !=  b) {
      IndexArrayIterator newData = dt;
      //int[][] newData = dt;
      final int[] ne = getNodeEntry(cn);
      final long tdid = did;
      final IndexArrayIterator tmp = getData(ne[ne.length - 1], tdid);
      //final int[][] tmp = getDataFromDataArray(ne[ne.length - 1], tdid);
      if (tmp != null) {
        //newData = calculateFTOr(dt, tmp);
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
  
  /**
   * Collects all tokens and their sizes found in the index structure.
   *
   * @param cn id on nodeArray, current node
   * @param stats statistics reference
   * @param tok current token
   */
  private void addOccurrences(final int cn, final IndexStats stats,
      final byte[] tok) {
    final int[] ne = getNodeEntry(cn);
    byte[] ntok;
    if(cn > 0) {
      ntok = new byte[tok.length + ne[0]];
      System.arraycopy(tok, 0, ntok, 0, tok.length);
      for (int i = 0; i < ne[0]; i++) ntok[tok.length + i] = (byte) ne[i + 1];
      final int size = ne[ne.length - 1];
      if(size > 0 && stats.adding(size)) stats.add(ntok);
    } else {
      ntok = tok;
    }
    if (hasNextNodes(ne)) {
     for (int i = ne[0] + 1; i < ne.length - 1; i += 2) {
       addOccurrences(ne[i], stats, ntok);
     }
    }
    return;
  }

  /**
   * Read node entry from disk.
   * @param id on node array (in main memory)
   * @return node entry from disk
   */
  private int[] getNodeEntry(final int id) {
    int sp = inS.readInt(id * 4L);
    int ep = inS.readInt((id + 1) * 4L);
    final int[] ne = new int [(int) (ep - sp - 5L)];
    int c = 0;
    ne[c++] = inN.readBytes(sp, sp + 1L)[0];
    sp += 1L;
    for (int j = 0; j < ne[0]; j++)
      ne[c++] = inN.readBytes(sp + 1L * j, sp + 1L * j + 1L)[0];

    sp += ne[0];
    if (sp + 9L < ep) {
      // inner node
      while(sp < ep - 9L) {
        ne[c++] = inN.readInt(sp);
        sp += 4L;
        ne[c++] = inN.readBytes(sp, sp + 1L)[0];
        sp += 1L;
      }
    }
    ne[c++] = inN.readInt(ep - 9L);
    did = inN.read5(ep - 5L);
    final int[] r = new int[c];
    System.arraycopy(ne, 0, r, 0, c);
    return r;
  }

  /**
   * Extracts data from disk and returns it in
   * [[pre1, ..., pres], [pos1, ..., poss]] representation.
   *
   * @param s number of pre/pos values
   * @param ldid pointer on data
   * @return  int[][] data
   */
  private int[][] getDataFromDataArray(final int s, final long ldid) {
    if(s == 0 || ldid < 0) return null;
    final int[][] dt = new int[2][s];
    dt[0][0] = inD.readNum(ldid);

    if (data.meta.ftittr) {
      dt[1][0] = inD.readNum();
      for(int i = 1; i < s; i++) {
        dt[0][i] = inD.readNum();
        dt[1][i] = inD.readNum();
      }
    } else {
      for(int i = 1; i < s; i++) {
        dt[0][i] = inD.readNum();
      }
      
      for(int i = 0; i < s; i++) {
        dt[1][i] = inD.readNum();
      }
    }
    return dt;
  }

  /**
   * Read fulltext data from disk.
   * 
   * @param s size
   * @param ldid pointer on data
   * @return IndexArrayIterator
   */
  private IndexArrayIterator getData(final int s, final long ldid) {
    if (s == 0 || ldid < 0) return IndexArrayIterator.EMP;
    if (data.meta.ftittr) {
      return new IndexArrayIterator(s) {
        boolean f = true;
        int lpre = -1;
        int c = 0;
        long pos = ldid;
        FTNode n = new FTNode();
        
        @Override
        public boolean more() {
          if (c == s) return false;
          IntList il = new IntList();
          int pre;
          if (f) {
            f = false;
            pre = inD.readNum(pos);
            pos = inD.pos();
          } else {
            pre = lpre;
          }
          
          f = false;
          il.add(pre);
          il.add(inD.readNum(pos));
          c++;
          while (c < s && (lpre = inD.readNum()) == pre) {
            il.add(inD.readNum());
            c++;
          }
          pos = inD.pos();
          n = new FTNode(il.finish(), 1);
          return true;
        }
        
        @Override
        public FTNode nextFTNodeFD() {
          return n;
        }
      };
    } else {
      return new IndexArrayIterator(getDataFromDataArray(s, ldid), true);
    }
  }
  
  /**
   * Save whether a corresponding node was found in method getInsertingPosition.
   */
  private boolean found;

  /**
   * Checks wether a node is an inner node or a leaf node.
   * @param ne current node entrie.
   * @return boolean leaf node or inner node
   */
  private boolean hasNextNodes(final int[] ne) {
    return ne[0] + 1 < ne.length - 1;
  }

  /**
   * Uses linear search for finding inserting position.
   * returns:
   * 0 if any successor exists, or 0 is inserting position
   * n here to insert
   * n and found = true, if nth item is occupied and here to insert
   *
   * @param cne current node entry
   * @param toInsert byte looking for
   * @return inserting position
   */
  private int getInsertingPosition(final int[] cne, final int toInsert) {
    if (cs)
      return getInsPosLinCSF(cne, toInsert);
    else return getInsertingPositionLinear(cne, toInsert);
  }

  /**
   * Uses linear search for finding inserting position.
   * returns:
   * 0 if any successor exists, or 0 is inserting position
   * n here to insert
   * n and found = true, if nth item is occupied and here to insert
   *
   * @param cne current node entry
   * @param toInsert byte looking for
   * @return inserting position
   */
  private int getInsertingPositionLinear(final int[] cne,
      final int toInsert) {
    found = false;
    int i = cne[0] + 1;
    final int s = cne.length - 1;
    if (s == i)
      return i;

    while (i < s && 
        Token.diff((byte) cne[i + 1], (byte) toInsert) < 0) i += 2;
    if (i < s && Token.diff((byte) cne[i + 1], (byte) toInsert) == 0) {
      found = true;
    }
    return i;
  }


  /**
   * Uses linear search for finding inserting position.
   * values are order like this:
   * a,A,b,B,r,T,s,y,Y
   * returns:
   * 0 if any successor exists, or 0 is inserting position
   * n here to insert
   * n and found = true, if nth item is occupied and here to insert
   * BASED ON FINISHED STRUCTURE ARRAYS!!!!
   *
   * @param cne current node entry
   * @param toInsert value to be inserted
   * @return inserting position
   */
  private int getInsPosLinCSF(final int[] cne,
      final int toInsert) {
    found = false;

    int i = cne[0] + 1;
    final int s = cne.length - 1;
    if (s == i)
      return i;
    while (i < s && Token.diff(
        (byte) Token.lc(cne[i + 1]), (byte) Token.lc(toInsert)) < 0) 
      i += 2;

    if (i < s) {
      if(Token.diff((byte) cne[i + 1], (byte) toInsert) == 0) {
        found = true;
        return i;
      } else if(Token.diff((byte) Token.lc(cne[i + 1]),
          (byte) Token.lc(toInsert)) == 0) {
        if (Token.eq((byte) cne[i + 1], (byte) Token.uc(toInsert))) {
          return i;
          }
        if (i + 3 < s && Token.diff((byte) cne[i + 3], (byte) toInsert) == 0) {
          found = true;
        }
        return i + 2;
      }
    }
    return i;
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
  private void astericsWildCardTraversing(final int node, final byte[] ending,
    final boolean lastFound, final int pointerNode, final int pointerEnding) {

    int j = pointerEnding;
    int i = pointerNode;
    boolean last = lastFound;
    final int[] ne = getNodeEntry(node);
    final long tdid = did;

    // wildcard at the end
    if(ending == null || ending.length == 0) {
      // save data current node
      if (ne[ne.length - 1] > 0) {
        idata = IndexArrayIterator.merge(
            getData(ne[ne.length - 1], tdid), idata);
      }
      if (hasNextNodes(ne)) {
        // preorder traversal through trie
        for (int t = ne[0] + 1; t < ne.length - 1; t += 2) {
          astericsWildCardTraversing(ne[t], null, last, 0, 0);
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
        astericsWildCardTraversing(ne[t], ending, false, 1, 0);
      }
      countSkippedChars = 0;
      return;
    } else if(j == ending.length && i == ne[0] + 1) {
      // all chars form node and all chars from ending done
      idata = IndexArrayIterator.merge(
          getData(ne[ne.length - 1], tdid), idata);
      
      countSkippedChars = 0;

      // node has successors and is leaf node
      if (hasNextNodes(ne)) {
        // preorder search in trie
        for (int t = ne[0] + 1; t < ne.length - 1; t += 2) {
          if(j == 1) {
            astericsWildCardTraversing(ne[t], ending, false, 0, 0);
          }
          astericsWildCardTraversing(ne[t], ending, last, 0, j);
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
      astericsWildCardTraversing(node, ending, false, i + 1, 0);
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
          astericsWildCardTraversing(ne[t], ending, last, 1, 0);
        }
        astericsWildCardTraversing(ne[t], ending, last, 1, j);
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
   * Method for wildcards search in trie.
   *
   * getNodeFromTrieWithWildCard(char[] valueSearchNode, int pos) is called
   * getNodeFromTrieWithWildCard(FTIndexCTNode currentCompressedTrieNode,
   *    char[] valueSearchNode, int posWildcard)
   * executes the wildcard search and works on trie via
   * getNodeFromTrieRecursiveWildcard(FTIndexCTNode currentCompressedTrieNode,
   *    byte[] valueSearchNode)
   * calls
   *
   * @param valueSearchNode search nodes value
   * @param pos position
   * @return data int[][]
   */
  private IndexArrayIterator getNodeFromTrieWithWildCard(
      final byte[] valueSearchNode, final int pos) {
    // init counter
    counter = new int[2];
    return getNodeFromTrieWithWildCard(0, valueSearchNode, pos, false);
  }

  /**
   * Saves node values from .-wildcard search according to records in id-array.
   */
  private byte[] valuesFound;

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
  private IndexArrayIterator getNodeFromTrieWithWildCard(
      final int cn, final byte[] sn,
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
      resultNode = getNodeFromTrieRecursiveWildcard(cn, bw);
      if(resultNode == -1) return IndexArrayIterator.EMP;
    } else {
      resultNode = 0;
    }

    byte wildcard;
    if(posw + 1 >= vsn.length) {
      wildcard = '.';
    } else {
      wildcard = vsn[posw + 1];
    }

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

      d = getNodeFromTrieRecursive(0, sc, false);

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
      d = IndexArrayIterator.merge(
          getNodeFromTrieWithWildCard(0, sc, posw, false), d);
      return d;
    } else if(wildcard == '*') {
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
        d = getNodeFromTrieRecursive(0, searchChar, false);
        // all chars from valueSearchNode are contained in trie
        if(bw != null && counter[1] != bw.length) {
          return d;
        }
      }

      // delete data
      idata = IndexArrayIterator.EMP;
      astericsWildCardTraversing(resultNode, aw, false, counter[0], 0);
      return IndexArrayIterator.merge(d, idata);
    } else if(wildcard == '+') {
      // append 1 or more symbols
      final int[] rne = getNodeEntry(resultNode);
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
        tmpres = getNodeFromTrieWithWildCard(nvsn, l + 1);
      } else if(rne[0] == counter[0] || resultNode == 0) {
        // all chars from nodes[resultNode] are computed
        // any next values existing
        if(!hasNextNodes(rne)) {
          return IndexArrayIterator.EMP;
        }

        for (int t = rne[0] + 1; t < rne.length - 1; t += 2) {
          nvsn[l] = (byte) rne[t + 1];
          tmpres = IndexArrayIterator.merge(
              getNodeFromTrieWithWildCard(nvsn, l + 1), tmpres);
        }
      }
      return tmpres;
    } else {
      final int[] rne = getNodeEntry(resultNode);
      // append 1 symbol
      // not completely processed (value current node)
      if(rne[0] > counter[0] && resultNode > 0) {
        // replace wildcard with value from currentCompressedTrieNode
        vsn[posw] = (byte) rne[counter[0] + 1];

        // . wildcards left
        IndexArrayIterator resultData = getNodeFromTrieRecursive(0, vsn, false);
        // save nodeValues for recursive method call
        if(resultData != IndexArrayIterator.EMP && recCall) {
          valuesFound = new byte[] {(byte) rne[counter[0] + 1]};
        }
        return resultData;
      } else if(rne[0] == counter[0] || resultNode == 0) {
        // all chars from nodes[resultNode] are computed
        // any next values existing
        if(!hasNextNodes(rne)) {
          return IndexArrayIterator.EMP;
        }

        IndexArrayIterator tmpNode = IndexArrayIterator.EMP;
        aw = new byte[vsn.length - posw];
        System.arraycopy(vsn, posw + 1, aw, 1, aw.length - 1);

        // simple method call
        if(!recCall) {
          for (int t = rne[0] + 1; t < rne.length - 1; t += 2) {
            aw[0] = (byte) rne[t + 1];
            tmpNode = IndexArrayIterator.merge(
                getNodeFromTrieRecursive(rne[t], aw, false), tmpNode);
          }

          return tmpNode;
        } else {
          // method call for .+ wildcard
          valuesFound = new byte[rne.length - 1 - rne[0] - 1];
          for (int t = rne[0] + 1; t < rne.length - 1; t += 2) {
            // replace first letter
            aw[0] = (byte) rne[t + 1];
            valuesFound[t - rne[0] - 1] = (byte) rne[t + 1];
            tmpNode = IndexArrayIterator.merge(
                getNodeFromTrieRecursive(rne[t], aw, false), tmpNode);
          }
        }
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
  private int getNodeFromTrieRecursiveWildcard(
      final int cn, final byte[] sn) {

    byte[]vsn = sn;
    final int[] cne = getNodeEntry(cn);
    if(cn != 0) {
      counter[1] += cne[0];

      int i = 0;
      while(i < vsn.length && i < cne[0] && cne[i + 1] == vsn[i]) {
        i++;
      }

      if(cne[0] == i) {
        if(vsn.length == i) {
          counter[0] = i;
          // leafnode found with appropriate value
          return cn;
        } else {
          // cut valueSearchNode for value current node
          final byte[] tmp = new byte[vsn.length - i];
          System.arraycopy(vsn, i, tmp, 0, tmp.length);
          vsn = tmp;

          // scan successors currentNode
          final int pos = getInsertingPosition(cne, vsn[0]);
          if(!found) {
            // node not contained
            counter[0] = i;
            counter[1] = counter[1] - cne[0] + i;

            return cn;
          } else {
            return getNodeFromTrieRecursiveWildcard(cne[pos], vsn);
          }
        }
      } else {
        // node not contained
        counter[0] = i;
        counter[1] = counter[1] - cne[0] + i;
        return cn;
      }
    } else {
      // scan successors current node
      final int pos = getInsertingPosition(cne, vsn[0]);

      if(!found) {
        // node not contained
        counter[0] = -1;
        counter[1] = -1;
        return -1;
      } else {
        return getNodeFromTrieRecursiveWildcard(cne[pos], vsn);
      }
    }
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
   * @param r int counter for replacepments
   * @param c int counter sum of errors
   * @return int[][]
   */
  private IndexArrayIterator getNodeFuzzy(final int cn, final int[] crne,
       final long crdid, final byte[] sn, final int d, final int p,
       final int r, final int c) {
    byte[] vsn = sn;
    int[] cne = crne;
    long cdid = crdid;
    if (cne == null) {
      cne = getNodeEntry(cn);
      cdid = did;
    }

    if(cn != 0) {
      // not root node
      int i = 0;
      while(i < vsn.length && i < cne[0] 
            && Token.diff((byte) cne[i + 1],  vsn[i]) == 0) {
        i++;
      }

      if(cne[0] == i) {
        // node entry processed complete
        if(vsn.length == i) {
          // leafnode found with appropriate value
          if (c >= d + p + r) {
            IndexArrayIterator ld = IndexArrayIterator.EMP;
            ld = getData(cne[cne.length - 1], cdid);
            if (hasNextNodes(cne)) {
              for (int t = cne[0] + 1; t < cne.length - 1; t++) {
                ld = IndexArrayIterator.merge(getNodeFuzzy(cne[t], null, -1,
                    new byte[]{(byte) cne[t + 1]}, d, p + 1, r, c), ld);
                t++;
              }
            }
            return ld;
          } else return IndexArrayIterator.EMP;
        } else {
          IndexArrayIterator ld = IndexArrayIterator.EMP;
          byte[] b;
          if (c > d + p + r) {
              // delete char
              b = new byte[vsn.length - 1];
              System.arraycopy(vsn, 0, b, 0, i);
              ld = getNodeFuzzy(cn, cne, cdid, b, d + 1, p, r, c);
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
                  ne = getNodeEntry(cne[k]);
                  tdid = did;
                  b = new byte[vsn.length];
                  System.arraycopy(vsn, 0, b, 0, vsn.length);
                  ld = IndexArrayIterator.merge(
                      getNodeFuzzy(cne[k], ne, tdid, b, d, p, r, c), ld);
                }

              if (c > d + p + r) {
                if (ne == null) {
                  ne = getNodeEntry(cne[k]);
                  tdid = did;
                }
                // paste char
                b = new byte[vsn.length + 1];
                b[0] = (byte) cne[k + 1];
                System.arraycopy(vsn, 0, b, 1, vsn.length);
                ld = IndexArrayIterator.merge(getNodeFuzzy(cne[k], ne, tdid,
                    b, d, p + 1, r, c), ld);

                if (vsn.length > 0) {
                  // delete char
                  b = new byte[vsn.length - 1];
                  System.arraycopy(vsn, 1, b, 0, b.length);
                  ld = IndexArrayIterator.merge(getNodeFuzzy(cne[k], ne, tdid,
                      b, d + 1, p, r, c), ld);
                  // replace char
                  b = new byte[vsn.length];
                  System.arraycopy(vsn, 1, b, 1, vsn.length - 1);
                  b[0] = (byte) ne[1];
                  ld = IndexArrayIterator.merge(getNodeFuzzy(cne[k], ne, tdid,
                      b, d, p, r + 1, c), ld);
                }
              }
            }
          }
          return ld;
        }
      } else {
        IndexArrayIterator ld = IndexArrayIterator.EMP;
        byte[] b;

        if (c > d + p + r) {
          // paste char
          b = new byte[vsn.length + 1];
          System.arraycopy(vsn, 0, b, 0, i);
          b[i] = (byte) cne[i + 1];
          System.arraycopy(vsn, i, b, i + 1, vsn.length - i);

          ld = getNodeFuzzy(cn, cne, cdid, b, d, p + 1, r, c);

          if (vsn.length > 0 && i < vsn.length) {
            // replace
            b = new byte[vsn.length];
            System.arraycopy(vsn, 0, b, 0, vsn.length);

            b[i] = (byte) cne[i + 1];
            ld = IndexArrayIterator.merge(getNodeFuzzy(cn, cne, cdid,
                b, d, p, r + 1, c), ld);
            if (vsn.length > 1) {
              // delete char
              b = new byte[vsn.length - 1];
              System.arraycopy(vsn, 0, b, 0, i);
              System.arraycopy(vsn, i + 1, b, i, vsn.length - i - 1);
              ld = IndexArrayIterator.merge(getNodeFuzzy(cn, cne, cdid,
                  b, d + 1, p, r, c), ld);
            }
           }
          } else {
          return ld;
        }
        return ld;
      }
    } else {
      int[] ne = null;
      long tdid = -1;
      IndexArrayIterator ld = IndexArrayIterator.EMP;

      byte[] b;
      if(hasNextNodes(cne)) {
          for (int k = cne[0] + 1; k < cne.length - 1; k += 2) {
            if (cne[k + 1] == vsn[0]) {
              ne = getNodeEntry(cne[k]);
              tdid = did;
              b = new byte[vsn.length];
              System.arraycopy(vsn, 0, b, 0, vsn.length);
              ld = IndexArrayIterator.merge(
                  getNodeFuzzy(cne[k], ne, tdid, b, d, p, r, c), ld);
            }
          if (c > d + p + r) {
            if (ne == null) {
              ne = getNodeEntry(cne[k]);
              tdid = did;
            }
            // paste char
            b = new byte[vsn.length + 1];
            b[0] = (byte) ne[1];
            System.arraycopy(vsn, 0, b, 1, vsn.length);
            ld = IndexArrayIterator.merge(getNodeFuzzy(cne[k], ne, tdid,
                b, d, p + 1, r, c), ld);

            if (vsn.length > 0) {
              // delete char
              b = new byte[vsn.length - 1];
              System.arraycopy(vsn, 1, b, 0, b.length);
              ld = IndexArrayIterator.merge(getNodeFuzzy(cne[k], ne, tdid,
                  b, d + 1, p, r, c), ld);
                // replace
              b = new byte[vsn.length];
              System.arraycopy(vsn, 1, b, 1, vsn.length - 1);
                b[0] = (byte) ne[1];
                ld = IndexArrayIterator.merge(getNodeFuzzy(cne[k], ne, tdid,
                    b, d, p, r + 1, c), ld);
            }
          }
        }
      }
      return ld;
    }
  }

  /**
   * Builds an or-conjunction of values1 and values2.
    * @param values1 input set
    * @param values2 input set
    * @return union set int[][]
    */
  static int[][] calculateFTOr(final int[][] values1,
      final int[][] values2) {

    int[][] val1 = values1;
    int[][] val2 = values2;

    if(val1 == null || val1[0].length == 0) return val2;
    if(val2 == null || val2[0].length == 0) return val1;

    final int[][] maxResult = new int[2][val1[0].length + val2[0].length];

    // calculate maximum
    final int max = Math.max(val1[0].length, val2[0].length);
    if(max == val1.length) {
      final int[][] tmp = val1;
      val1 = val2;
      val2 = tmp;
    }

    // process smaller set
    int i = 0;
    int k = 0;
    int c = 0;
    while(val1[0].length > i) {
      if(k >= val2[0].length) break;

      final int cmp = compareIntArrayEntry(val1[0][i], val1[1][i],
          val2[0][k], val2[1][k]);
      final boolean l = cmp > 0;
      maxResult[0][c] = l ? val2[0][k] : val1[0][i];
      maxResult[1][c] = l ? val2[1][k] : val1[1][i];

      if(cmp >= 0) k++;
      if(cmp <= 0) i++;
      c++;
    }
    if(c == 0) return null;

    final boolean l = k == val2[0].length && i < val1[0].length;
    final int[] left = l ? val1[0] : val2[0];
    final int v = left.length - (l ? i : k);

    final int[][] result = new int[2][c + v];
    // copy first values
    System.arraycopy(maxResult[0], 0, result[0], 0, c);
    System.arraycopy(maxResult[1], 0, result[1], 0, c);

    // copy left values
    System.arraycopy(left, l ? i : k, result[0], c, v);
    System.arraycopy(left, l ? i : k, result[1], c, v);

    return result;
  }

  /**
   * Compares 2 int[1][2] array entries and returns.
   *  0 for equality
   * -1 if intArrayEntry1 < intArrayEntry2 (same id) or
   *  1  if intArrayEntry1 > intArrayEntry2 (same id)
   *  2  real bigger (different id)
   * -2 real smaller (different id)
   *
   * @param id1 first id
   * @param p1 first position
   * @param id2 second id
   * @param p2 second position
   * @return result [0|-1|1|2|-2]
   */
  static int compareIntArrayEntry(final int id1, final int p1,
      final int id2, final int p2) {

    // equal ID, equal pos or data1 behind/before data2
    if(id1 == id2) return p1 == p2 ? 0 : p1 > p2 ? 1 : -1;
    // real bigger/smaller
    return id1 > id2 ? 2 : -2;
  }

  /**
   * Extracts ids out of an int[2][]-array.
   * int[0] = ids
   * int[1] = position values
   * @param data data
   * @return ids int[]
   */
  static int[] extractIDsFromData(final int[][] data) {
    if(data == null || data.length == 0 || data[0] == null
        || data[0].length == 0) return Array.NOINTS;

    final int l = data[0].length;
    final int[] ids = new int[l];
    ids[0] = data[0][0];
    int c = 1;
    for(int i = 1; i < l; i++) {
      final int j = data[0][i];
      if(ids[c - 1] != j) ids[c++] = j;
    }
    return Array.finish(ids, c);
  }
}
