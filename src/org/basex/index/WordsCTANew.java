package org.basex.index;

import static org.basex.data.DataText.*;
import static org.basex.Text.*;
import java.io.IOException;
import org.basex.BaseX;
import org.basex.core.Prop;
import org.basex.data.Data;
import org.basex.io.DataAccessPerf;
import org.basex.util.Array;
import org.basex.util.FTTokenizer;
import org.basex.util.Performance;
import org.basex.util.TokenBuilder;
import org.basex.query.xpath.expr.FTUnion;
import org.basex.util.Token;

/**
 * This class indexes text contents in a compressed trie on disk.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 * @author Sebastian Gath
 */
public final class WordsCTANew extends Index {
  // save each node: l, t1, ..., tl, n1, v1, ..., nu, vu, s, p
  // l = length of the token t1, ..., tl
  // u = number of next nodes n1, ..., nu
  // v1= the first byte of each token n1 points, ...
  // s = size of pre values saved at pointer p
  // [byte, byte[l], byte, int, byte, ..., int, long]
  
  /** Values file. */
  private final Data data;
  /** Trie structure on disk. */
  private final DataAccessPerf inN;
  // ftdata is stored here, with pre1, ..., preu, pos1, ..., posu
  /** FTData on disk. */
  private final DataAccessPerf inD;
  // each node entries size is stored here
  /** FTData sizes on disk. */
  private final DataAccessPerf inS;
  /** Id on data, corresponding to the current node entry. */
  private long did;
  /** Flag for case sensitive index. */
  private boolean cs = false;
  
  /**
   * Constructor, initializing the index structure.
   * @param d data reference
   * @param db name of the database
   * @throws IOException IO Exception
   */
  public WordsCTANew(final Data d, final String db) throws IOException {
    final String file = DATAFTX;
    inN = new DataAccessPerf(db, file + 'a', "NodeData");
    inD = new DataAccessPerf(db, file + 'b', "FT-Data");
    inS = new DataAccessPerf(db, file + 'c', "Size");
    data = d;
    did = -1;
    cs = data.meta.ftcs;
  }

  @Override
  public byte[] info() {
    final TokenBuilder tb = new TokenBuilder();
    tb.add(TRIE + NL);
    tb.add("- %: %\n", CREATESTEM, BaseX.flag(data.meta.ftstem));
    tb.add("- %: %\n", CREATECS, BaseX.flag(data.meta.ftcs));
    tb.add("- %: %\n", CREATEDC, BaseX.flag(data.meta.ftdc));
    final long l = inN.length() + inD.length() + inS.length();
    tb.add(SIZEDISK + Performance.formatSize(l, true) + NL);
    return tb.finish();
  }
  
  @Override
  public int nrIDs(final IndexToken ind) {
    return ids(ind).size();
  }

  @Override
  public IndexIterator ids(final IndexToken ind) {
    if(ind.range()) return idRange((RangeToken) ind);
    
    final FTTokenizer ft = (FTTokenizer) ind;
    final byte[] tok = ft.get();
    if(ft.fz) {
      int k = Prop.lserr;
      if(k == 0) k = Math.max(1, tok.length >> 2);
      final int[][] ids = getNodeFuzzy(0, null, -1, tok, 0, 0, 0, k);
      return new IndexArrayIterator(ids);
    }

    if(ft.wc) {
      final int pw = Token.indexOf(tok, '.');
      if(pw != -1) {
        final int[][] ids = getNodeFromTrieWithWildCard(tok, pw);
        return new IndexArrayIterator(ids);
      }
    }
    
    if(!cs && ft.cs) {
      // case insensitive index create - check real case with dbdata
      int[][] ids = getNodeFromTrieRecursive(0, Token.lc(tok), false);
      if(ids == null) {
        return null;
      }

      byte[] tokenFromDB;
      byte[] textFromDB;
      int[][] rIds = new int[2][ids[0].length];
      int count = 0;
      int readId;

      int i = 0;
      // check real case of each result node
      while(i < ids[0].length) {
        // get date from disk
        readId = ids[0][i];
        textFromDB = data.text(ids[0][i]);
        tokenFromDB = new byte[tok.length];

        System.arraycopy(textFromDB, ids[1][i], tokenFromDB, 0, tok.length);

        // check unique node ones
        while(i < ids[0].length && readId == ids[0][i]) {
          System.arraycopy(textFromDB, ids[1][i], tokenFromDB, 0, tok.length);

          readId = ids[0][i];

          // check unique node ones
          // compare token from db with token from query
          if(Token.eq(tokenFromDB, tok)) {
            rIds[0][count] = ids[0][i];
            rIds[1][count++] = ids[1][i];

            // jump over same ids
            while(i < ids[0].length && readId == ids[0][i])
              i++;
            break;
          }
          i++;
        }
      }
      return new IndexArrayIterator(rIds, count);
    }
    
    final int[][] tmp = getNodeFromTrieRecursive(0, tok, ft.cs);
    return new IndexArrayIterator(tmp);
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
    int[] tokF = new int[tokFrom.length];
    int tokFID = tokFrom.length;
    int[] tokT = new int[tokTo.length];
    int tokTID = tokTo.length;
    for (int i = 0; i < tokF.length; i++) {
      tokF[i] = tokFrom[i];
      if (tokFrom[i] == '.') tokFID = i;
    }
    
    for (int i = 0; i < tokTo.length; i++) {
      tokT[i] = tokTo[i];
      if (tokTo[i] == '.') tokTID = i;
    }
    
    int[][] dt = null;
    
    if (Token.ftdigit(tokFrom) && Token.ftdigit(tokTo)) {
      int td = tokTo.length - tokFrom.length;
      int[] ne = getNodeEntry(0);
      if (hasNextNodes(ne)) {
        //for (int i = ne[0] + 1; i < ne.length - 2; i += 2) {
        for (int i = ne[0] + 1; i < ne.length - 1; i += 2) {
          if (Token.letter(ne[i + 1]))
            return new IndexArrayIterator(Array.extractIDsFromData(dt));
          
          if (ne[i + 1] != tokFrom[0] && ne[i + 1] != tokTo[0]) {
            if (tokTID == tokFID) {
            //if (tokTo.length == tokFrom.length) {
              if (ne[i + 1] > tokFrom[0] && ne[i + 1] < tokTo[0]) {
                //data = getAllNodesWithLevel(ne[i], tokFrom.length, 
                //    tokTo.length, data, false);
                dt = getAllNodesWithLevel(ne[i], tokFID, 
                    tokTID, dt, false);            
              }
            } else {
              int lb;
              int ub = (ne[i + 1] < tokTo[0]) ? tokTID : tokTID - 1;
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
    } else if (Token.letterOrDigit(tokFrom) && Token.letterOrDigit(tokTo)) {
      dt = idPosRangeText(tokF, tokT);
    }
    return new IndexArrayIterator(Array.extractIDsFromData(dt));
  }
  
  /** Count current level. */
  private int cl = 0;

  /**
   * Returns all ids that are in the range of tok0 and tok1.
   * @param tokFrom token defining range start
   * @param tokTo token defining range end
   * @return number of ids
   */
  private int[][] idPosRangeText(final int[] tokFrom, final int[] tokTo) {
    // you have to backup all passed nodes 
    // and maybe the next child of the current node.
    int[][] idNNF = new int[2][tokFrom.length + 1];
    int[][] idNNT = new int[2][tokTo.length];
    int c = 0;
    int b; 
    int[][] dt = null;
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
      //data = getDataFromDataArray(ne[ne.length - 2], ne[ne.length - 1]);
      ldid = did;
      dt = getDataFromDataArray(ne[ne.length - 1], ldid);
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
      //int[][] tmp 
      //= getDataFromDataArray(ne[ne.length - 2], ne[ne.length - 1]);
      int[][] tmp = getDataFromDataArray(ne[ne.length - 1], ldid);
      dt = FTUnion.calculateFTOr(dt, getAllNodes(id, b, tmp));
    } 
    
    for (int i = 0; i < idNNF[0].length; i++) {
      if (i > 0 && idNNF[0][i] == 0 && idNNF[1][i] == 0) break;
      ne = getNodeEntry(idNNF[0][i]);
      ldid = did;
      //if (ne.length - 3 - ne[0] / 2 >= idNNF[1][i]) {
      if (ne.length - 2 - ne[0] / 2 >= idNNF[1][i]) {
        //for (int k = idNNF[1][i]; k < ne.length - 3 - ne[0]; k += 2)
        for (int k = idNNF[1][i]; k < ne.length - 2 - ne[0]; k += 2)
          dt = FTUnion.calculateFTOr(dt, 
              //getAllNodes(getDataEntry(idNNF[0][i], k), b, data));
              getAllNodes(ne[k], b, dt));
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
  private int[][] getNodeFromTrieRecursive(final int cNode,
      final byte[] searchNode, final boolean casesen) {
    if (searchNode == null || searchNode.length == 0) return null;
    int[] ne = null;
    if (cs) {
      if (casesen) ne = getNodeIdFromTrieRecursive(cNode, searchNode); 
      else  return getNodeFromCSTrie(cNode, searchNode);
    } else if (!casesen) ne = getNodeIdFromTrieRecursive(cNode, searchNode);

    if (ne == null) {
      return null;
    }
    
    long ldid = did;
    return getDataFromDataArray(ne[ne.length - 1], ldid);
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
    int[] ne = getNodeEntry(cn);

    if(cn != 0) {
      int i = 0;
      while(i < vsn.length && i < ne[0] && ne[i + 1] == vsn[i]) {
        i++;
      }

      if(ne[0] == i) {
        if(vsn.length == i) {
          // leaf node found with appropriate value
          return ne;
        } else {
          // cut valueSearchNode for value current node
          byte[] tmp = new byte[vsn.length - i];
          System.arraycopy(vsn, i, tmp, 0, tmp.length);
          vsn = tmp;

          // scan successors currentNode
          int pos = getInsertingPosition(ne, vsn[0]);
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
      int pos = getInsertingPosition(ne, vsn[0]);
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
  private int[][] getNodeFromCSTrie(final int cn, final byte[] sn) {
    byte[] vsn = sn;
    long ldid;
    
    // read data entry from disk
    int[] ne = getNodeEntry(cn);

    if(cn != 0) {
      int i = 0;
      while(i < vsn.length && i < ne[0] && Token.lc(ne[i + 1]) == vsn[i]) {
        i++;
      }

      if(ne[0] == i) {
        if(vsn.length == i) {
          // leaf node found with appropriate value
          ldid = did; 
          return getDataFromDataArray(ne[ne.length - 1], ldid);
        } else {
          // cut valueSearchNode for value current node
          byte[] tmp = new byte[vsn.length - i];
          System.arraycopy(vsn, i, tmp, 0, tmp.length);
          vsn = tmp;

          // scan successors currentNode
          final int p = getInsPosLinCSF(ne, vsn[0]);
          if(!found) {
            if (Token.lc(ne[p + 1]) != vsn[0])
              return null;
            else 
              return getNodeFromCSTrie(ne[p], vsn);
          } else {
            int[][] d = getNodeFromCSTrie(ne[p], vsn);
            if (Token.lc(ne[p + 3]) == vsn[0]) {
              d = FTUnion.calculateFTOr(d, 
                  getNodeFromCSTrie(ne[p + 2], vsn));
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
        if (Token.lc(ne[p + 1]) != vsn[0])
          return null;
        else 
          return getNodeFromCSTrie(ne[p], vsn);
      } else {
        int[][] d = getNodeFromCSTrie(ne[p], vsn);
        if (Token.lc(ne[p + 3]) == vsn[0]) {
          d = FTUnion.calculateFTOr(d, 
              getNodeFromCSTrie(ne[p + 2], vsn));
        }
        return d;
      }
    }
  }
  /**
   * Traverse case sensitive trie and search case sensitive.
   * Returns data from node or null.
   *
   * @param cn int
   * @param sn search nodes value
   * @return int id on node saving the data
   */
 /* private int[] getNodeFromSensitiveTrie(final int cn, final byte[] sn) {
    byte[] vsn = sn;

    // read data entry from disk
    int[] ne = getNodeEntry(cn);

    if(cn != 0) {
      int i = 0;
      while(i < vsn.length && i < ne[0] && Token.lc(ne[i + 1]) == vsn[i]) {
        i++;
      }

      if(ne[0] == i) {
        if(vsn.length == i) {
          // leaf node found with appropriate value
          return ne;
        } else {
          // cut valueSearchNode for value current node
          byte[] tmp = new byte[vsn.length - i];
          System.arraycopy(vsn, i, tmp, 0, tmp.length);
          vsn = tmp;

          // scan successors currentNode
          int pos = getInsertingPosition(ne, vsn[0]);
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
      int pos = getInsertingPosition(ne, vsn[0]);
      if(!found) {
        // node not contained
        return null;
      } else {
        return getNodeIdFromTrieRecursive(ne[pos], vsn);
      }
    }
  }

 */ 

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
    int[] ne = getNodeEntry(cn);

    if(cn != 0) {
      int i = 0;
      // read data entry from disk
      while(i < vsn.length && i < ne[0] && ne[i + 1] == vsn[i]) {
        i++;
        cl++;
      }

      if(ne[0] == i) {
        if(vsn.length == i) {
          // leaf node found with appropriate value
          //if(ne[ne[0] + 1] < 0) {
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
          int position = getInsertingPosition(ne, vsn[0]);
          if(!found) {
            // node not contained
            return -1;
          } else {
            //int id = getIdOnDataArray(ne);
            //id = getDataEntry(id, position);
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
      int position = getInsertingPosition(ne, vsn[0]);
      if(!found) {
        // node not contained
        return -1;
      } else {
        //int id = getIdOnDataArray(ne);
        //id = getDataEntry(id, position);
        idNN[0][c] = cn;
        idNN[1][c] = position + 2; // +1
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
  private int[][] getAllNodesWithinBounds(final int id, final int[] v,
      final int[] ub, final int ubid, final int[] lb, final int lbid, 
      final int c, final int[][] dt) {
    int[][] dn = dt;
    int[] ne = getNodeEntry(id);
    long ldid = did;
    int in = getIndexDotNE(ne);
    
    //if (ne[0] + c < lb.length) {
    if (in + c < lbid) {
      // process children
      if (!hasNextNodes(ne)) return dn;
      System.arraycopy(ne, 1, v, c, ne[0]);
      //for (int i = ne[0] + 1; i < ne.length - 2; i += 2) {
      for (int i = ne[0] + 1; i < ne.length - 1; i += 2) {
        dn = getAllNodesWithinBounds(
            ne[i], v, ub, ubid, lb, lbid, c + ne[0], dn);
      }
//    } else if (ne[0] + c > ub.length) {
    } else if (in + c > ubid && ubid == ub.length) {
      return dn;
    } else {
      int [] vn = v;
      if (v.length < c + ne[0]) 
        vn = Array.resize(v, v.length, v.length << 2 + c + ne[0]);
      System.arraycopy(ne, 1, vn, c, ne[0]);
      int vid = getIndexDot(vn, c + ne[0]);

      if (vid == c + ne[0]) {
        if (checkLBConstrain(vn, vid, lb, lbid) 
            && checkUBConstrain(vn, vid, ub, ubid)) {
          dn = FTUnion.calculateFTOr(dn, 
              //getDataFromDataArray(ne[ne.length - 2], ne[ne.length - 1]));
              getDataFromDataArray(ne[ne.length - 1], ldid));
        }        
      } else {
        if (checkLBConstrainDbl(vn, vid, lb, lbid) 
            && checkUBConstrainDbl(vn, vid, ub, ubid)) {
          dn = FTUnion.calculateFTOr(dn, 
              //getDataFromDataArray(ne[ne.length - 2], ne[ne.length - 1]));
              getDataFromDataArray(ne[ne.length - 1], ldid));
        }
      }

      if (!hasNextNodes(ne)) 
        return dn;
      //for (int i = ne[0] + 1; i < ne.length - 2; i += 2) {
      for (int i = ne[0] + 1; i < ne.length - 1; i += 2) {
        dn = FTUnion.calculateFTOr(dn, 
          getAllNodesWithinBounds(ne[i], vn, ub, ubid, lb, 
              lbid, c + ne[0], dn));
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
  private int[][] getAllNodesWithLevel(final int id, final int l1, 
      final int l2, final int[][] dt, final boolean dotFound) {
    int[][] dn = dt;
    int[] ne = getNodeEntry(id);
    long tdid = did;
    if(dotFound) {
      dn = FTUnion.calculateFTOr(dn, 
          //getDataFromDataArray(ne[ne.length - 2], ne[ne.length - 1]));
          getDataFromDataArray(ne[ne.length - 1], tdid));
      //int[] nn = getNextNodes(ne);
      if (!hasNextNodes(ne)) return dn;
      for (int i = ne[0] + 1; i < ne.length - 2; i += 2) {
        dn = FTUnion.calculateFTOr(dn, 
          getAllNodesWithLevel(ne[i], 0, 0, dn, dotFound));
      }
      return dn;
    }
    
    int neID = getIndexDotNE(ne);
    
    //if (!Token.digit(ne[1]) || ne[0] > l2) return dn;
    if (!Token.digit(ne[1]) || neID > l2) return dn;
    
    //if (l1 <= ne[0] && ne[0] <= l2)  {
    if (l1 <= neID && neID <= l2)  {
      dn = FTUnion.calculateFTOr(dn, 
          //getDataFromDataArray(ne[ne.length - 2], ne[ne.length - 1]));
          getDataFromDataArray(ne[ne.length - 1], tdid));
      //int[] nn = getNextNodes(ne);
      if (!hasNextNodes(ne)) return dn;
      //for (int i = ne[0] + 1; i < ne.length - 2; i += 2) {
      for (int i = ne[0] + 1; i < ne.length - 1; i += 2) {
        dn = FTUnion.calculateFTOr(dn, 
          getAllNodesWithLevel(ne[i], l1 - ne[0], 
              l2 - ne[0], dn, ne[0] > neID));
      }
    } else if (ne[0] < l1 && ne [0] == neID) {
      //int[] nn = getNextNodes(ne);
      if (!hasNextNodes(ne)) return dn;
      //for (int i = ne[0] + 1; i < ne.length - 2; i += 2) {
      for (int i = ne[0] + 1; i < ne.length - 1; i += 2) {
        dn = FTUnion.calculateFTOr(dn, 
            getAllNodesWithLevel(ne[i], l1 - ne[0], l2 - ne[0], dn, false));
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
    //if (c < ub.length) return true;
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
    //if (c < ub.length) return true;
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
  private int [][] getAllNodes(final int cn, final int b, final int[][] dt) {
    if (cn !=  b) {
      int[][] newData = dt;
      int[] ne = getNodeEntry(cn);
      long tdid = did;
      //int[][] tmp 
      //= getDataFromDataArray(ne[ne.length - 2], ne[ne.length - 1]);
      int[][] tmp = getDataFromDataArray(ne[ne.length - 1], tdid);
      if (tmp != null) {
        newData = FTUnion.calculateFTOr(dt, tmp); 
      }
    
      if (hasNextNodes(ne)) {
        //ne[ne[0] + 1] * -1 > 0) {
        // node is not a leaf not; has no data
        // get children of current node
        //final int[] nn = new int[ne[ne[0] + 1] * -1];
        //System.arraycopy(getDataEntry(id), 0, nn, 0, nn.length);
          
        for (int i = ne[0] + 1; i < ne.length - 1; i += 2) {
          //newData = FTUnion.calculateFTOr(newData, 
          newData = getAllNodes(ne[i], b, newData);
        }
      }
      return newData;
    }
    return dt;
  }
    
  /**
   * Read node entry from disk.
   * @param id on node array (in main memory)
   * @return node entry from disk
   */
  private int[] getNodeEntry(final int id) {
    int sp = inS.readInt(id * 4L);
    final int ep = inS.readInt((id + 1) * 4L);
    int[] ne = new int [ep - sp - (int) 5L];
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
    int[] r = new int[c];
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
    if(s == 0 && ldid <= 0) return null;
    int[][] dt = new int[2][s];
   
    if (data.meta.fcompress) {
      dt[0][0] = inD.readNum(ldid);
      for(int i = 1; i < s; i++) dt[0][i] = inD.readNum();
      for(int i = 0; i < s; i++) dt[1][i] = inD.readNum();
    } else {
      dt[0] = inD.readInts(ldid, s * 4L + ldid);
      dt[1] = inD.readInts(s * 4L + ldid, s * 8L + ldid);
    }
    return dt;
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
    //return ne[0] + 1 < ne.length - 2;
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
    //return getInsertingPositionBinary(cne, toInsert);
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
    //int s = cne.length - 2;
    int s = cne.length - 1;
    if (s == i) 
      return i;

    while (i < s && cne[i + 1] < toInsert) i += 2;
    if (i < s && cne[i + 1] == toInsert) {
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
    int s = cne.length - 1;
    if (s == i) 
      return i;
    while (i < s && Token.lc(cne[i + 1]) < Token.lc(toInsert)) i += 2;
    
    if (i < s) {
      if(cne[i + 1] == toInsert) {
        found = true;
        return i;
      } else if(Token.lc(cne[i + 1]) 
          == Token.lc(toInsert)) {
        if (cne[i + 1] == Token.uc(toInsert)) {
          return i;
          }
        if (i + 3 < s && cne[i + 3] == toInsert) { 
          found = true;
        }
        return i + 2;
      }
    }
    return i;
  }

  /*
   * Uses linear search for finding inserting positions in a 
   * case sensitive trie.
   * returns:
   * null if any successor exists, 
   * an int[1] if one successor exists and an int[2] if 2 successors exists
   * n if nth item is occupied
   *
   * @param cne current node entry
   * @param toInsert byte looking for
   * @return inserting position
  private int[] getSuccessorPosSensitiveTrie(final int[] cne,
      final int toInsert) {
    int r = -1;
    int i = cne[0] + 1;
    int s = cne.length - 1;
    if (s == i)  return null;

    while (i < s && Token.lc(cne[i + 1]) < toInsert) i += 2;
    if (i == s) return null;
    if (Token.lc(cne[i + 1]) == toInsert)  r = i;

    while (i > s && Token.lc(cne[s - 1]) < toInsert) s -= 2;
    if (i == s) return new int[]{r};
    if (Token.lc(cne[i + 1]) == toInsert) return new int[]{r , i};
    if (r > -1) return new int[]{r};
    
    return null;
  }
   */
  
  /*
   * Uses linear search for finding inserting position.
   * returns:
   * 0 if any successor exists, or 0 is inserting position
   * n here to insert
   * n and found = true, if nth item is occupied and here to insert
   *
   * @param cne current node entry
   * @param toin value to be inserted
   * @return inserting position
  private int getInsertingPositionBinary(final int[] cne, final int toin) {
    found = false;
    int l = cne[0] + 1;
    int r = cne.length - 2;
    int m = r - l / 2;
    while (l < r) {
      m = r - l / 2;
      if (cne[m + 1] < toin) l = m + 2;
      else if (cne[m + 1] > toin) r = m - 2;
      else {
        found = true;
        return m;
      }
    }
    
    if (l < cne.length - 2 
        && cne[m + 1] == toin) {
      found = true;
      return l + 2;
    }
    return l;
  }
   */
  
  /** saves astericsWildCardTraversing result
  has to be re-init each time (before calling method). */
  private int[][] adata;

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
      adata = FTUnion.calculateFTOr(adata, 
          //getDataFromDataArray(ne[ne.length - 2], ne[ne.length - 1]));
          getDataFromDataArray(ne[ne.length - 1], tdid));
      
      if (hasNextNodes(ne)) {
        // preorder traversal through trie
        //for (int t = ne[0] + 1; t < ne.length - 2; t += 2) {
        for (int t = ne[0] + 1; t < ne.length - 1; t += 2) {
          astericsWildCardTraversing(ne[t], null, last, 0, 0);
        }
      }
      return;
    }

    // compare chars current node and ending
    //if(ne != null) {
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
/*    } 
    else {
      countSkippedChars = 0;
      return;
    }
  */
    // not processed all chars from node, but all chars from
    // ending were processed or root
    if(node == 0 || j == ending.length && i < ne[0] + 1) {
      if (!hasNextNodes(ne)) {
      //if(ne[ne[0] + 1] > 0) {
        countSkippedChars = 0;
        return;
      }
  
      //final int[] nextNodes = getNextNodes(ne);
      // preorder search in trie
      //for(final int n : nextNodes) {
      //for (int t = ne[0] + 1; t < ne.length - 2; t += 2) {
      for (int t = ne[0] + 1; t < ne.length - 1; t += 2) {
        astericsWildCardTraversing(ne[t], ending, false, 1, 0);
      }
      countSkippedChars = 0;
      return;
    } else if(j == ending.length && i == ne[0] + 1) {
      // all chars form node and all chars from ending done
      /*final int[][] d = getDataFromDataArray(ne, curDataEntry);
      if(d != null) {
        adata = CTArrayX.ftOR(adata, d);
      }*/
      adata = FTUnion.calculateFTOr(adata, 
          //getDataFromDataArray(ne[ne.length - 2], ne[ne.length - 1]));
          getDataFromDataArray(ne[ne.length - 1], tdid));
      
      countSkippedChars = 0;
  
      //final int[] nextNodes = getNextNodes(ne);
      // node has successors and is leaf node
      //if(nextNodes != null) {
      if (hasNextNodes(ne)) {
        // preorder search in trie
        //for (int t = ne[0] + 1; t < ne.length - 2; t += 2) {
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
      //if(ne[ne[0] + 1] > 0) {
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
      //if(ne[ne[0] + 1] > 0) {
        countSkippedChars = 0;
        return;
      }

      //final int[] nextNodes = getNextNodes(ne);
      // preorder search in trie
      //for(final int n : nextNodes) {
      //for (int t = ne[0] + 1; t < ne.length - 2; t += 2) {
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
  private int[][] getNodeFromTrieWithWildCard(
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
  private int[][] getNodeFromTrieWithWildCard(
      final int cn, final byte[] sn,
      final int posw, final boolean recCall) {

    final byte[] vsn = sn;
    byte[] aw = null;
    byte[] bw = null;

    final int currentLength = 0;
    int resultNode;

    int[][] d = null;
    // wildcard not at beginning
    if(posw > 0) {
      // copy part before wildcard
      bw = new byte[posw];
      System.arraycopy(vsn, 0, bw, 0, posw);
      resultNode = getNodeFromTrieRecursiveWildcard(cn, bw);
      if(resultNode == -1) return null;
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
      d = FTUnion.calculateFTOr(d, 
          getNodeFromTrieWithWildCard(0, sc, posw, false));
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
        //System.out.println("searchChar:" + new String(searchChar));
        // all chars from valueSearchNode are contained in trie
        if(bw != null && counter[1] != bw.length) {
          return d;
        }
      }

      // delete data
      adata = null;
      astericsWildCardTraversing(resultNode, aw, false, counter[0], 0);
      return FTUnion.calculateFTOr(d, adata);
    } else if(wildcard == '+') {
      
      
      // append 1 or more symbols
      final int[] rne = getNodeEntry(resultNode);
      byte[] nvsn = new byte[vsn.length + 1];
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
      int[][] tmpres = null;
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
          return null;
        }

        for (int t = rne[0] + 1; t < rne.length - 1; t += 2) {
          nvsn[l] = (byte) rne[t + 1];
          tmpres = FTUnion.calculateFTOr(
              getNodeFromTrieWithWildCard(nvsn, l + 1), tmpres);
        }
      } 
      return tmpres;
    } else {
      final int[] rne = getNodeEntry(resultNode);
      //final long rdid = did;
      // append 1 symbol
      // not completely processed (value current node)
      if(rne[0] > counter[0] && resultNode > 0) {
        // replace wildcard with value from currentCompressedTrieNode
        //valueSearchNode[posWildcard] = nodes[resultNode][counter[0] + 1];
        vsn[posw] = (byte) rne[counter[0] + 1];

        // . wildcards left
        final int [][] resultData = getNodeFromTrieRecursive(0, vsn, false);
        // save nodeValues for recursive method call
        if(resultData != null && recCall) {
          valuesFound = new byte[] {(byte) rne[counter[0] + 1]};
        }
        return resultData;

      } else if(rne[0] == counter[0] || resultNode == 0) {
        // all chars from nodes[resultNode] are computed
        // any next values existing
        if(!hasNextNodes(rne)) {
        //if(rne[rne[0] + 1] > 0) {
          return null;
        }

        int[][] tmpNode = null;
        aw = new byte[vsn.length - posw];
        System.arraycopy(vsn, posw + 1, aw, 1, aw.length - 1);

        //final int[] nextNodes = getNextNodes(rne);

        // simple method call
        if(!recCall) {
          //for (int t = rne[0] + 1; t < rne.length - 2; t += 2) {
          for (int t = rne[0] + 1; t < rne.length - 1; t += 2) {
          //for(final int n : nextNodes) {
            // replace first letter
            //ne = getNodeEntry(n);
            //aw[0] = ne[1];
            aw[0] = (byte) rne[t + 1];

            tmpNode = FTUnion.calculateFTOr(tmpNode, 
                getNodeFromTrieRecursive(rne[t], aw, false));
          }

          return tmpNode;
        } else {
          // method call for .+ wildcard
          valuesFound = new byte[rne.length - 1 - rne[0] - 1];
          //valuesFound = new byte[rne.length - 2 - rne[0] - 1];
          //for(int i = 0; i < nextNodes.length; i++) {
          //for (int t = rne[0] + 1; t < rne.length - 2; t += 2) {
          for (int t = rne[0] + 1; t < rne.length - 1; t += 2) {
            // replace first letter
            //ne = getNodeEntry(nextNodes[i]);
            aw[0] = (byte) rne[t + 1]; //ne[1];
            //valuesFound[i] = ne[1];
            valuesFound[t - rne[0] - 1] = (byte) rne[t + 1];

            tmpNode = FTUnion.calculateFTOr(tmpNode,
                getNodeFromTrieRecursive(rne[t], aw, false));
          }
        }
      }
    }
    return null;
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
          byte[] tmp = new byte[vsn.length - i];
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
   private int[][] getNodeFuzzy(final int cn, final int[] crne, 
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
      while(i < vsn.length && i < cne[0] && cne[i + 1] == vsn[i]) {
        i++;
      }
      
      if(cne[0] == i) {
        // node entry processed complete 
        if(vsn.length == i) {
          // leafnode found with appropriate value
          if (c >= d + p + r) {
            int[][] ld = null;
            //ld = 
            //getDataFromDataArray(cne[cne.length - 2], cne[cne.length - 1]);
            ld = getDataFromDataArray(cne[cne.length - 1], cdid);
            if (hasNextNodes(cne)) {
              //for (int t = cne[0] + 1; t < cne.length - 2; t++) {
              for (int t = cne[0] + 1; t < cne.length - 1; t++) {
                ld = FTUnion.calculateFTOr(ld, getNodeFuzzy(cne[t], null, -1,
                    new byte[]{(byte) cne[t + 1]}, d, p + 1, r, c));
                t++;
              }
            }
            return ld;
          } else return null;
        } else {
          int[][] ld = null;
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
            //for (int k = cne[0] + 1; k < cne.length - 2; k += 2) {
            for (int k = cne[0] + 1; k < cne.length - 1; k += 2) {
              //if (c > d + p + r) {
                if (cne[k + 1] == vsn[0]) {
                  ne = getNodeEntry(cne[k]);
                  tdid = did;
                  b = new byte[vsn.length];
                  System.arraycopy(vsn, 0, b, 0, vsn.length); 
                  ld = FTUnion.calculateFTOr(ld, 
                      getNodeFuzzy(cne[k], ne, tdid, b, d, p, r, c));
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
                ld = FTUnion.calculateFTOr(ld, getNodeFuzzy(cne[k], ne, tdid,
                    b, d, p + 1, r, c));
                
                if (vsn.length > 0) {
                  // delete char
                  b = new byte[vsn.length - 1];
                  System.arraycopy(vsn, 1, b, 0, b.length);
                  ld = FTUnion.calculateFTOr(ld, getNodeFuzzy(cne[k], ne, tdid,
                      b, d + 1, p, r, c));
                  // replace char
                  b = new byte[vsn.length];
                  System.arraycopy(vsn, 1, b, 1, vsn.length - 1);
                  b[0] = (byte) ne[1];
                  ld = FTUnion.calculateFTOr(ld, getNodeFuzzy(cne[k], ne, tdid,
                      b, d, p, r + 1, c));
                }
              }    
            }
          }
          return ld;
        }
      } else {
        int[][] ld = null;
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
            ld = FTUnion.calculateFTOr(ld, getNodeFuzzy(cn, cne, cdid,
                b, d, p, r + 1, c));
            if (vsn.length > 1) {
              // delete char
              b = new byte[vsn.length - 1];
              System.arraycopy(vsn, 0, b, 0, i);
              System.arraycopy(vsn, i + 1, b, i, vsn.length - i - 1);
              ld = FTUnion.calculateFTOr(ld, getNodeFuzzy(cn, cne, cdid,
                  b, d + 1, p, r, c));
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
      int[][] ld = null;

      byte[] b;
      if(hasNextNodes(cne)) {
        //for (int k = cne[0] + 1; k < cne.length - 2; k += 2) {
          for (int k = cne[0] + 1; k < cne.length - 1; k += 2) {
          //if (c > d + p + r) {
            if (cne[k + 1] == vsn[0]) {
              ne = getNodeEntry(cne[k]);
              tdid = did;
              b = new byte[vsn.length];
              System.arraycopy(vsn, 0, b, 0, vsn.length);
              ld = FTUnion.calculateFTOr(ld, 
                  getNodeFuzzy(cne[k], ne, tdid, b, d, p, r, c));
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
            ld = FTUnion.calculateFTOr(ld, getNodeFuzzy(cne[k], ne, tdid,
                b, d, p + 1, r, c));
            
            if (vsn.length > 0) {
              // delete char
              b = new byte[vsn.length - 1];
              System.arraycopy(vsn, 1, b, 0, b.length);
              ld = FTUnion.calculateFTOr(ld, getNodeFuzzy(cne[k], ne, tdid,
                  b, d + 1, p, r, c));
                // replace
              b = new byte[vsn.length];
              System.arraycopy(vsn, 1, b, 1, vsn.length - 1);
                b[0] = (byte) ne[1];
                ld = FTUnion.calculateFTOr(ld, getNodeFuzzy(cne[k], ne, tdid,
                    b, d, p, r + 1, c));
            }
          } 
        }
      }
      return ld;
    }
  }
}
