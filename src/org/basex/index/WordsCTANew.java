package org.basex.index;

import static org.basex.data.DataText.*;
import static org.basex.Text.*;
import java.io.IOException;
import org.basex.data.Data;
import org.basex.io.DataAccess;
import org.basex.io.PrintOutput;
import org.basex.util.Array;
import org.basex.util.Performance;
import org.basex.query.xpath.expr.FTOption;
import org.basex.query.xpath.expr.FTUnion;
import org.basex.util.Token;

/**
 * This class indexes text contents in a compressed trie on disk.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 * @author Sebastian Gath
 */
public final class WordsCTANew implements Index {
  // save each node: l, t1, ..., tl, u, n1, v1, ..., nu, vu, s, p
  // l = length of the token t1, ..., tl
  // u = number of next nodes n1, ..., nu
  // v1= the first byte of each token n1 points, ...
  // s = size of pre values saved at pointer p
  // [byte, byte[l], byte, int, byte, ..., int, int]
  /** Trie structure on disk. */
  private final DataAccess inN;
  // ftdata is stored here, with pre1, ..., preu, pos1, ..., posu
  /** FTData on disk. */
  private final DataAccess inD;
  // each nodeetries size is stored here
  /** FTData sizes on disk. */
  private final DataAccess inS;

  /**
   * Constructor, initializing the index structure.
   * @param db name of the database
   * @throws IOException IO Exception
   */
  public WordsCTANew(final String db) throws IOException {
    final String file = DATAFTX;
    inN = new DataAccess(db, file + 'a');
    inD = new DataAccess(db, file + 'b');
    inS = new DataAccess(db, file + 'c');
  }

  /** {@inheritDoc} */
  public void info(final PrintOutput out) throws IOException {
    out.println(FTINDEX);
    out.println(TRIE);
    final long l = inN.length() + inD.length() + inS.length();
    out.println(SIZEDISK + Performance.formatSize(l, true) + NL);
  }

  /** {@inheritDoc} */
  public int[][] idPos(final byte[] tok, final FTOption ftO, final Data dd) {
    // init no wildcard included in token
    int posW = -1;

    // backup original token
    byte[] bTok = new byte[tok.length];
    System.arraycopy(tok, 0, bTok, 0, tok.length);

    // token to lower case
    for (int i = 0; i < tok.length; i++) {
      tok[i] = (byte) Token.lc(tok[i]);
      // check for wildcards
      if (tok[i] == '.') {
        posW = i;
      }
    }

    // check wildcards
    if (ftO.ftWild == FTOption.WILD.WITH && posW > -1)  {
      return getNodeFromTrieWithWildCard(tok, posW);
    }


    if (ftO.ftCase == FTOption.CASE.INSENSITIVE) {
      // index request with pre-values as result
      return getNodeFromTrieRecursive(0, tok);
      //ids(tok);
    }

    // index request with pre-values and positions as result
    int[][] ids = getNodeFromTrieRecursive(0, tok);
    if (ids == null) {
      return null;
    }

    if (ftO.ftCase == FTOption.CASE.UPPERCASE) {
      // convert search string to upper case and use case sensitive search
      //System.out.println("newToken:" + new String(bTok));
      bTok = Token.uc(bTok);
      //System.out.println("newToken:" + new String(bTok));
      //for (int i = 0; i < tok.length; i++) bTok[i] = (byte) Token.uc(bTok[i]);
    } else if (ftO.ftCase == FTOption.CASE.LOWERCASE) {
      // carry search string to upper case and use case sensitive search
      //System.out.println("newToken:" + new String(bTok));
      bTok = Token.lc(bTok);
      //System.out.println("newToken:" + new String(bTok));
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
      textFromDB = dd.text(ids[0][i]);
      tokenFromDB = new byte[tok.length];

      System.arraycopy(textFromDB, ids[1][i], tokenFromDB, 0, tok.length);

      readId = ids[0][i];

      // check unique node ones
      while (i < ids[0].length && readId == ids[0][i]) {
        System.arraycopy(textFromDB, ids[1][i], tokenFromDB, 0, tok.length);

        readId = ids[0][i];

        // check unique node ones
        // compare token from db with token from query
        if (Token.eq(tokenFromDB, bTok)) {
          rIds[0][count] = ids[0][i];
          rIds[1][count++] = ids[1][i];

          // jump over same ids
          while (i < ids[0].length && readId == ids[0][i]) i++;
          break;
        }
        i++;
      }
    }

    if (count == 0) return null;
    
    int[][] tmp = new int[2][count];
    System.arraycopy(rIds[0], 0, tmp[0], 0, count);
    System.arraycopy(rIds[1], 0, tmp[1], 0, count);

    return tmp;
  }
  
  /** {@inheritDoc} */
  public int[][] idPosRange(final byte[] tokFrom, final boolean itok0,
      final byte[] tokTo, final boolean itok1) {
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
    
    int[][] data = null;
    fromi = itok0;
    toi = itok1;
    
    if (Token.ftdigit(tokFrom) && Token.ftdigit(tokTo)) {
      int td = tokTo.length - tokFrom.length;
      int[] ne = getNodeEntry(0);
      if (hasNextNodes(ne)) {
        for (int i = ne[0] + 1; i < ne.length - 2; i += 2) {
          if (Token.letter(ne[i + 1])) return data;
          if (ne[i + 1] != tokFrom[0] && ne[i + 1] != tokTo[0]) {
            if (tokTID == tokFID) {
            //if (tokTo.length == tokFrom.length) {
              if (ne[i + 1] > tokFrom[0] && ne[i + 1] < tokTo[0]) {
                //data = getAllNodesWithLevel(ne[i], tokFrom.length, 
                //    tokTo.length, data, false);
                data = getAllNodesWithLevel(ne[i], tokFID, 
                    tokTID, data, false);            
              }
            } else {
              int lb;
              int ub = (ne[i + 1] < tokTo[0]) ? tokTID : tokTID - 1;
              if (td > 1) {
                lb = (ne[i + 1] < tokFrom[0]) ? tokFID + 1 : tokFID;
                data = getAllNodesWithLevel(ne[i], lb, ub, data, false);
              } else {
                lb = (ne[i + 1] < tokFrom[0]) ? 
                    ((ne[i + 1] < tokTo[0]) ? tokFID + 1 : -1) : tokFID;
                if (lb > -1) 
                  data = getAllNodesWithLevel(ne[i], lb, ub, data, false);
              }
            }
          } else {
            data = getAllNodesWithinBounds(
                ne[i], new int[tokTo.length], tokT, 
                tokTID, tokF, tokFID, 0, data);
          }    
        }
      }
    } else if (Token.letterOrDigit(tokFrom) && Token.letterOrDigit(tokTo)) {
      data = idPosRangeText(tokF, tokT);
    }
    return data;
  }
  
  /** Count current level. */
  private int cl = 0;
  /** Maximum level, equals the length of the upper bound of a range. */
  //private int mxl = 0;
  /** Minimum level, equals the length of the lowerbound of a range. */
  //private int mnl = 0;

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
    int[][] data = null;
    int[] ne;
    
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
    } else if (toi) {
      ne = getNodeEntry(b);
      data = getDataFromDataArray(ne[ne.length - 2], ne[ne.length - 1]);
      data = getAllNodes(b, b, data);
    }
    
    c = 0;
    cl = 0; 
    int id;
    id = getNodeIdsForRQ(0, tokFrom, c, idNNF);
    c = 0;
    // start travesing with id of tokFrom
    // reduce minimul level, every following node has to be chacked 
    // with same or bigger length than tokFrom
    if (id > -1 && fromi) {
      ne = getNodeEntry(id);
      int[][] tmp = getDataFromDataArray(ne[ne.length - 2], ne[ne.length - 1]);
      data = FTUnion.calculateFTOr(data, getAllNodes(id, b, tmp));
    } 
    
    for (int i = 0; i < idNNF[0].length; i++) {
      if (i > 0 && idNNF[0][i] == 0 && idNNF[1][i] == 0) break;
      ne = getNodeEntry(idNNF[0][i]);
      //if (-1 * ne[ne[0] + 1] >= idNNF[1][i]) {
      if (ne.length - 3 - ne[0] / 2 >= idNNF[1][i]) {
        //for (int k = idNNF[1][i]; k < -1 * ne[ne[0] + 1]; k++)
        for (int k = idNNF[1][i]; k < ne.length - 3 - ne[0]; k += 2)
          data = FTUnion.calculateFTOr(data, 
              //getAllNodes(getDataEntry(idNNF[0][i], k), b, data));
              getAllNodes(ne[k], b, data));
       } 
    }
      
    return data;
  }
  
  /** {@inheritDoc} */
  public int[][] fuzzyIDs(final byte[] tok, final int ne) {
    return getNodeFuzzy(0, null, tok, 0, 0, 0, ne);
    //return getNodesFuzzyWLev(0, new StringBuilder(), tok, 3, null);
  }

  /** {@inheritDoc} */
  public int[] ids(final byte[] tok) {
    int[] ne = getNodeIdFromTrieRecursive(0, getNodeEntry(0), 
        tok);
    if (ne == null) {
      return null;
    }
    return Array.extractIDsFromData(
        getDataFromDataArray(ne[ne.length - 2], ne[ne.length - 1]));
    
  }

  /** {@inheritDoc} */
  public int nrIDs(final byte[] tok) {
    return ids(tok).length;
  }

  /** {@inheritDoc} */
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
   * @return int[][] array with pre-values and corresponding positions
   * for each pre-value
   */
  private int[][] getNodeFromTrieRecursive(final int cNode,
      final byte[] searchNode) {
    if (searchNode == null || searchNode.length == 0) return null;
    int[] ne = getNodeIdFromTrieRecursive(cNode, getNodeEntry(cNode), 
        searchNode);
    if (ne == null) {
      return null;
    }
    return getDataFromDataArray(ne[ne.length - 2], ne[ne.length - 1]);
  }

  /**
   * Traverse trie and return found node for searchValue.
   * Returns data from node or null.
   *
   * @param cn int
   * @param ne byte[] node entry (cn)
   * @param sn search nodes value
   * @return int id on node saving the data
   */
  private int[] getNodeIdFromTrieRecursive(final int cn, final int[] ne, 
      final byte[] sn) {
    byte[] vsn = sn;

    // read data entry from disk
    //final byte[] ne = getNodeEntry(cn);

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
                ne[pos], getNodeEntry(ne[pos]), vsn);
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
        return getNodeIdFromTrieRecursive(ne[pos], getNodeEntry(ne[pos]), vsn);
      }
    }
  }
  
  

  /**
   * Parses the trie and backups each passed node its first/next child node 
   * in idNN. If searchNode is contained, the corresponding nodeid is 
   * retruned, else - 1.
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
  
 
  /** Lower bound included in bound range. */
  boolean fromi = false;
  /** Upper bound included in bound range. */
  boolean toi = false;
    
  
  /**
   * Parses the trie and backups each passed node that has a value
   * wihtin the range of lb and ub.
   *
   * @param id int current node
   * @param v appened value for the current node, starting from root
   * @param ub upper bound of the range
   * @param ubid index of dot in ub
   * @param lb lower bound of the range
   * @param lbid index of dot in lb
   * @param c int count number of passed nodes
   * @param data data found in the trie
   * @return int id on node saving the data
   */
  private int[][] getAllNodesWithinBounds(final int id, final int[] v,
      final int[] ub, final int ubid, final int[] lb, final int lbid, 
      final int c, final int[][] data) {
    int[][] dn = data;
    int[] ne = getNodeEntry(id);
    int in = getIndexDotNE(ne);
    
    //if (ne[0] + c < lb.length) {
    if (in + c < lbid) {
      // process children
      if (!hasNextNodes(ne)) return dn;
      System.arraycopy(ne, 1, v, c, ne[0]);
      for (int i = ne[0] + 1; i < ne.length - 2; i += 2) {
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
              getDataFromDataArray(ne[ne.length - 2], ne[ne.length - 1]));
        }        
      } else {
        if (checkLBConstrainDbl(vn, vid, lb, lbid) 
            && checkUBConstrainDbl(vn, vid, ub, ubid)) {
          dn = FTUnion.calculateFTOr(dn, 
              getDataFromDataArray(ne[ne.length - 2], ne[ne.length - 1]));
        }
      }

      if (!hasNextNodes(ne)) 
        return dn;
      for (int i = ne[0] + 1; i < ne.length - 2; i += 2) {
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
   * @param data data found in the trie
   * @param dotFound boolean flag to be set if dot was found (doubel values)
   * @return int[][] data
   */ 
  private int[][] getAllNodesWithLevel(final int id, final int l1, 
      final int l2, final int[][] data, final boolean dotFound) {
    int[][] dn = data;
    int[] ne = getNodeEntry(id);
    if(dotFound) {
      dn = FTUnion.calculateFTOr(dn, 
          getDataFromDataArray(ne[ne.length - 2], ne[ne.length - 1]));
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
          getDataFromDataArray(ne[ne.length - 2], ne[ne.length - 1]));
      //int[] nn = getNextNodes(ne);
      if (!hasNextNodes(ne)) return dn;
      for (int i = ne[0] + 1; i < ne.length - 2; i += 2) {
        dn = FTUnion.calculateFTOr(dn, 
          getAllNodesWithLevel(ne[i], l1 - ne[0], 
              l2 - ne[0], dn, ne[0] > neID));
      }
    } else if (ne[0] < l1 && ne [0] == neID) {
      //int[] nn = getNextNodes(ne);
      if (!hasNextNodes(ne)) return dn;
      for (int i = ne[0] + 1; i < ne.length - 2; i += 2) {
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
   * @return boolean result of constraintcheck
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
        
    return fromi;
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
    
    if (v.length == lb.length) 
      return fromi;
    return lb.length < v.length;
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
        
    return toi;
  }


  /**
   * Checks whether ub > v.
   * Used for double values.
   * 
   * @param v byte[] value
   * @param vid index of dot in v
   * @param ub upper bound of a range
   * @param ubid index of dot in ub
   * @return boolean result of constraintcheck
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
        
    if (v.length == ub.length) 
      return toi;
    return ub.length > v.length;
  }

  
  /**
   * Collects and returns all data found at nodes in the range of cn and b.
   * 
   * @param cn id on nodeArray, current node
   * @param b id on nodeArray, lastNode to check (upper bound of range)
   * @param data data found in trie in earlier recursionsteps
   * @return int[][] idpos
   */
  private int [][] getAllNodes(final int cn, final int b, final int[][] data) {
    if (cn !=  b) {
      int[][] newData = data;
      int[] ne = getNodeEntry(cn);
      int[][] tmp = getDataFromDataArray(ne[ne.length - 2], ne[ne.length - 1]);
      if (tmp != null) {
        newData = FTUnion.calculateFTOr(data, tmp); 
      }
    
      if (hasNextNodes(ne)) {
        //ne[ne[0] + 1] * -1 > 0) {
        // node is not a leaf not; has no data
        // get children of current node
        //final int[] nn = new int[ne[ne[0] + 1] * -1];
        //System.arraycopy(getDataEntry(id), 0, nn, 0, nn.length);
          
        for (int i = ne[0] + 1; i < ne.length - 2; i += 2) {
          //newData = FTUnion.calculateFTOr(newData, 
          newData = getAllNodes(ne[i], b, newData);
        }
      }
      return newData;
    }
    return data;
  }
    
  /**
   * Read node entry from disk.
   * @param id on node array (in main memory)
   * @return node entry from disk
   */
  private int[] getNodeEntry(final int id) {
    int sp = inS.readInt(id * 4L);
    final int ep = inS.readInt((id + 1) * 4L);
    int[] ne = new int [ep - sp];
    int c = 0;
    ne[c++] = inN.readBytes(sp, sp + 1L)[0];
    sp += 1L;
    for (int j = 0; j < ne[0]; j++)
      ne[c++] = inN.readBytes(sp + 1L * j, sp + 1L * j + 1L)[0];
    
    sp += ne[0];
    if (sp + 8L < ep) {
      // inner node
      while(sp < ep - 8L) {
        ne[c++] = inN.readInt(sp);
        sp += 4L;
        ne[c++] = inN.readBytes(sp, sp + 1L)[0];
        sp += 1L;
      }
    }
    ne[c++] = inN.readInt(ep - 8L);
    ne[c++] = inN.readInt(ep - 4L);
    int[] r = new int[c];
    System.arraycopy(ne, 0, r, 0, c);
    return r;
  }


  /**
   * Extracts data from disk and returns it in 
   * [[pre1, ..., pres], [pos1, ..., poss]] representation.
   *
   * @param s number of pre/pos values
   * @param p pointer on data
   * @return  int[][] data
   */
  private int[][] getDataFromDataArray(final int s, final int p) {
    if (s == 0 && p == 0) return null;
    int[][] data = new int[2][s];
    data[0] = inD.readInts(p, p + 4L * s);
    data[1] = inD.readInts(p + 4L * s, p + 8L * s);
    return data;
  }

  /**
   * Gets all pre-values from trie for node with nodeEntry as value.
   *
   * @param nodeEntry pointer array storing node entry
   * @param dataEntry pointer array storing all data
   * @return  int[] pre-values
   */
 /* private int[] getPreValues(final byte[] nodeEntry,
      final int[] dataEntry) {
    // extract id on data array form nodearray
    byte jumpOver = 0;

    // extract id on data array
    if(nodeEntry[nodeEntry[0] + 1] < 0) {
      // next nodes existing
      jumpOver = (byte) (-1 * nodeEntry[nodeEntry[0] + 1]);
    }

    if(dataEntry.length == jumpOver) {
      return new int[]{};
    }

    int[] pre = new int[(dataEntry.length - jumpOver) / 2];
    System.arraycopy(dataEntry, jumpOver, pre, 0, pre.length);

    pre = CTArrayX.getIDsFromData(new int[][]{pre, {}});

    return  pre;
  }
*/

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
    return ne[0] + 1 < ne.length - 2;
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
    return getInsertingPositionLinear(cne, toInsert);
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
    int s = cne.length - 2;
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
   * returns:
   * 0 if any successor exists, or 0 is inserting position
   * n here to insert
   * n and found = true, if nth item is occupied and here to insert
   *
   * @param cne current node entry
   * @param toin value to be inserted
   * @return inserting position
   */
/*
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
    //final int[] curDataEntry = getDataEntry(node);

    // wildcard at the end
    if(ending == null || ending.length == 0) {
      // save data current node
      adata = FTUnion.calculateFTOr(adata, 
          getDataFromDataArray(ne[ne.length - 2], ne[ne.length - 1]));
          //getDataFromDataArray(ne, curDataEntry));
      
      //final int[] nextNodes = getNextNodes(ne);
      //if(nextNodes != null) {
      if (hasNextNodes(ne)) {
        // preorder traversal through trie
        for (int t = ne[0] + 1; t < ne.length - 2; t += 2) {
        //for(final int n : nextNodes) {
          astericsWildCardTraversing(ne[t], null, last, 0, 0);
        }
      }
      return;
    }

    // compare chars current node and ending
    if(ne != null) {
      // skip all unlike chars, if any suitable was found
      while(!last && i < ne[0] + 1 && ne[i] != ending[j]) {
        i++;
      }
  
      // skip all chars, equal to first char
      while(i + ending.length < ne[0] + 1 && ne[i + 1] == ending[0]) {
        i++;
      }
  
      countSkippedChars = countSkippedChars + i - pointerNode - 1;
      while(i < ne[0] + 1 && j < ending.length && ne[i] == ending[j]) {
        i++;
        j++;
        if(!last) {
          last = true;
        }
      }
    } else {
      countSkippedChars = 0;
      return;
    }
  
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
      for (int t = ne[0] + 1; t < ne.length - 2; t += 2) {
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
          getDataFromDataArray(ne[ne.length - 2], ne[ne.length - 1]));
      
      countSkippedChars = 0;
  
      //final int[] nextNodes = getNextNodes(ne);
      // node has successors and is leaf node
      //if(nextNodes != null) {
      if (hasNextNodes(ne)) {
        // preorder search in trie
        //for(final int n : nextNodes) {
        for (int t = ne[0] + 1; t < ne.length - 2; t += 2) {
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
      for (int t = ne[0] + 1; t < ne.length - 2; t += 2) {
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
   * Method for wildcars search in trie.
   *
   * getNodeFromTrieWithWildCard(char[] valueSearchNode, int pos) is called
   * getNodeFromTrieWithWildCard(FTIndexCTNode currentCompressedTrieNode,
   *    char[] valueSearchNode, int posWildcard)
   * executes the wildcardsearch and works on trie via
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
      if(resultNode == -1) {
        return null;
      }
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

      d = getNodeFromTrieRecursive(0, sc);

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
        d = getNodeFromTrieRecursive(0, searchChar);
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
      // lookup in trie with . as wildcard
      final byte[] searchChar = new byte[vsn.length - 1 - currentLength];
      // copy unprocessed part before wildcard
      if(bw != null) {
        System.arraycopy(bw, 0, searchChar, 0, bw.length);
      }
      // set . as wildcard
      searchChar[posw] = '.';

      // copy part after wildcard
      if(bw == null) {
        // valueSearchNode == .+
        if(!(posw == 0 && vsn.length == 2)) {
          aw = new byte[searchChar.length];
          System.arraycopy(vsn, posw + 2, searchChar, 1, searchChar.length);
          System.arraycopy(vsn, posw + 2, aw, 1, searchChar.length);
        }
      } else {
        aw = new byte[searchChar.length - bw.length - 1];
        System.arraycopy(vsn, posw + 2, searchChar, bw.length +  1,
            searchChar.length - bw.length - 1);
        System.arraycopy(vsn, posw + 2,
            aw, 0, searchChar.length - bw.length - 1);
      }

      // wildcard search with . as wildcard
      d = getNodeFromTrieWithWildCard(0, searchChar, posw, true);

      // at least one char has to be added
      if (d == null) {
        return null;
      }

      byte[] newValue;
      if(aw != null) {
        newValue = new byte[bw.length + 3 + aw.length];
        System.arraycopy(aw, 0, newValue, bw.length + 3, aw.length);
      } else {
        if (bw == null)
          newValue = new byte[3];
        else
          newValue = new byte[bw.length + 3];
      }
      newValue[bw.length + 1] = '.';
      newValue[bw.length + 2] = '*';

      System.arraycopy(bw, 0, newValue, 0, bw.length);
      // take resultNodes from wildcard search with . as wildcard and start
      // new wildcard search with * as wildcard
      for(final byte v : valuesFound) {
        if(v != 0) {
          newValue[bw.length] = v;
          //System.out.println(Token.string(newValue));
          d = FTUnion.calculateFTOr(d, getNodeFromTrieWithWildCard(newValue,
              bw.length + 1));
        }
      }

      return d;
    } else {
      final int[] rne = getNodeEntry(resultNode);
      // append 1 symbol
      // not completely processed (value current node)
      if(rne[0] > counter[0]) {
        // replace wildcard with value from currentCompressedTrieNode
        //valueSearchNode[posWildcard] = nodes[resultNode][counter[0] + 1];
        vsn[posw] = (byte) rne[counter[0] + 1];

        // . wildcards left
        final int [][] resultData = getNodeFromTrieRecursive(0, vsn);
        // save nodeValues for recursive method call
        if(resultData != null && recCall) {
          valuesFound = new byte[] {(byte) rne[counter[0] + 1]};
        }
        return resultData;

      } else if(rne[0] == counter[0]) {
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
          for (int t = rne[0] + 1; t < rne.length - 2; t += 2) {
          //for(final int n : nextNodes) {
            // replace first letter
            //ne = getNodeEntry(n);
            //aw[0] = ne[1];
            aw[0] = (byte) rne[t + 1];

            tmpNode = FTUnion.calculateFTOr(tmpNode, 
                getNodeFromTrieRecursive(rne[t], aw));
          }

          return tmpNode;
        } else {
          // method call for .+ wildcard
          //valuesFound = new byte[nextNodes.length];
          valuesFound = new byte[rne.length - 2 - rne[0] - 1];
          //for(int i = 0; i < nextNodes.length; i++) {
          for (int t = rne[0] + 1; t < rne.length - 2; t += 2) {
            // replace first letter
            //ne = getNodeEntry(nextNodes[i]);
            aw[0] = (byte) rne[t + 1]; //ne[1];
            //valuesFound[i] = ne[1];
            valuesFound[t - rne[0] - 1] = (byte) rne[t + 1];

            tmpNode = FTUnion.calculateFTOr(tmpNode,
                getNodeFromTrieRecursive(rne[t], aw));
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
   * @param sn byte[] search nodes value
   * @param d int counter for deletions
   * @param p int counter for pastes
   * @param r int counter for replacepments 
   * @param c int counter sum of errors
   * @return int[][]
   */
   private int[][] getNodeFuzzy(final int cn, final int[] crne, 
       final byte[] sn, final int d, final int p, final int r, final int c) {
    byte[] vsn = sn;
    int[] cne = crne;
    if (crne == null)
      cne = getNodeEntry(cn);
      
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
            ld = getDataFromDataArray(cne[cne.length - 2], cne[cne.length - 1]);
            if (hasNextNodes(cne)) {
              for (int t = cne[0] + 1; t < cne.length - 2; t++) {
                ld = FTUnion.calculateFTOr(ld, getNodeFuzzy(cne[t], 
                    getNodeEntry(cne[t]), new byte[]{(byte) cne[t + 1]}, 
                    d, p + 1, r, c));
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
              ld = getNodeFuzzy(cn, cne, b, d + 1, p, r, c);
            }
          
          // cut valueSearchNode for value current node
          final byte[] tmp = new byte[vsn.length - i];
          System.arraycopy(vsn, i, tmp, 0, tmp.length);
          vsn = tmp;

          // scan successors currentNode
          int[] ne = null;
          if (hasNextNodes(cne)) {
            for (int k = cne[0] + 1; k < cne.length - 2; k += 2) {
              //if (c > d + p + r) {
                if (cne[k + 1] == vsn[0]) {
                  ne = getNodeEntry(cne[k]);
                  b = new byte[vsn.length];
                  System.arraycopy(vsn, 0, b, 0, vsn.length); 
                  ld = FTUnion.calculateFTOr(ld, 
                      getNodeFuzzy(cne[k], ne, b, d, p, r, c));
                }
                
              if (c > d + p + r) {
                if (ne == null) ne = getNodeEntry(cne[k]); 
                // paste char
                b = new byte[vsn.length + 1];
                b[0] = (byte) cne[k + 1];
                System.arraycopy(vsn, 0, b, 1, vsn.length);
                ld = FTUnion.calculateFTOr(ld, getNodeFuzzy(cne[k], ne,
                    b, d, p + 1, r, c));
                
                if (vsn.length > 0) {
                  // delete char
                  b = new byte[vsn.length - 1];
                  System.arraycopy(vsn, 1, b, 0, b.length);
                  ld = FTUnion.calculateFTOr(ld, getNodeFuzzy(cne[k], ne,
                      b, d + 1, p, r, c));
                  // replace char
                  b = new byte[vsn.length];
                  System.arraycopy(vsn, 1, b, 1, vsn.length - 1);
                  b[0] = (byte) ne[1];
                  ld = FTUnion.calculateFTOr(ld, getNodeFuzzy(cne[k], ne,
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
                 
          ld = getNodeFuzzy(cn, cne, b, d, p + 1, r, c);
          
          if (vsn.length > 0 && i < vsn.length) {
            // replace
            b = new byte[vsn.length];
            System.arraycopy(vsn, 0, b, 0, vsn.length);
            
            b[i] = (byte) cne[i + 1];
            ld = FTUnion.calculateFTOr(ld, getNodeFuzzy(cn, cne,
                b, d, p, r + 1, c));
            if (vsn.length > 1) {
              // delete char
              b = new byte[vsn.length - 1];
              System.arraycopy(vsn, 0, b, 0, i);
              System.arraycopy(vsn, i + 1, b, i, vsn.length - i - 1);
              ld = FTUnion.calculateFTOr(ld, getNodeFuzzy(cn, cne,
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
      int[][] ld = null;

      byte[] b;
      if(hasNextNodes(cne)) {
        for (int k = cne[0] + 1; k < cne.length - 2; k += 2) {
          //if (c > d + p + r) {
            if (cne[k + 1] == vsn[0]) {
              ne = getNodeEntry(cne[k]);
              b = new byte[vsn.length];
              System.arraycopy(vsn, 0, b, 0, vsn.length);
              ld = FTUnion.calculateFTOr(ld, 
                  getNodeFuzzy(cne[k], ne, b, d, p, r, c));
            }
          if (c > d + p + r) {
            if (ne == null) ne = getNodeEntry(cne[k]);
            // paste char
            b = new byte[vsn.length + 1];
            b[0] = (byte) ne[1];
            System.arraycopy(vsn, 0, b, 1, vsn.length);
            ld = FTUnion.calculateFTOr(ld, getNodeFuzzy(cne[k], ne,
                b, d, p + 1, r, c));
            
            if (vsn.length > 0) {
              // delete char
              b = new byte[vsn.length - 1];
              System.arraycopy(vsn, 1, b, 0, b.length);
              ld = FTUnion.calculateFTOr(ld, getNodeFuzzy(cne[k], ne,
                  b, d + 1, p, r, c));
                // replace
              b = new byte[vsn.length];
              System.arraycopy(vsn, 1, b, 1, vsn.length - 1);
                b[0] = (byte) ne[1];
                ld = FTUnion.calculateFTOr(ld, getNodeFuzzy(cne[k], ne,
                    b, d, p, r + 1, c));
            }
          } 
        }
      }
      return ld;
    }
  }
}
