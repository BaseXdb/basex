package org.basex.index;

import static org.basex.data.DataText.*;
import static org.basex.Text.*;
import java.io.IOException;
import org.basex.data.DiskData;
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
public final class WordsCTA implements Index {
  /** Number of hash entries. */
  public int size;
  /** last/current number of ids. **/
  public int nrIds = 1;
  /** Values file. */
  //private DataDiskAccess txt;
  /** Index file. */
  private final DataAccess idxv;
  /** Index file. */
  private final DataAccess idxd;
  /** Index file. */
  private final DataAccess idxx;
  /** Index file. */
  private final DataAccess idxy;
  /** Reference on disk data. **/
  private final DiskData dd;
  /** Index file. */
  //private DataDiskAccess id;

  /**
   * Constructor, initializing the index structure.
   * @param db name of the database
   * @param d disk data reference
   * @throws IOException IO Exception
   */
  public WordsCTA(final String db, final DiskData d) throws IOException {
    final String file = DATAFTX;
    //id   = new DataDiskAccess(db, file + 'l');
    idxv = new DataAccess(db, file + 'v');
    idxx = new DataAccess(db, file + 'x');
    idxy = new DataAccess(db, file + 'y');
    idxd = new DataAccess(db, file + 'd');
    dd = d;
    //size = id.read4(0);
  }

  /**
   * Returns information on the index structure.
   * @param out output stream
   * @throws IOException in case of write errors
   */
  public void info(final PrintOutput out) throws IOException {
    out.println(FTINDEX);
    out.println(TRIE);
    final long l = idxv.length() + idxd.length();
    out.println(SIZEDISK + Performance.formatSize(l, true) + NL);
  }

  /**
   * Returns all ids from trie stored for tok, with repsect to ftO.
   * @param tok token to be found
   * @param ftO FTOption for tok
   * @return number of ids
   */
  public int[][] idPos(final byte[] tok, final FTOption ftO) {
    // init no wildcard included in token
    int posW = -1;
    //System.out.println("token:" + new String(tok));
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
    if (ftO.ftWild.equals(FTOption.WILD.WITH) && posW > -1)  {
      return getNodeFromTrieWithWildCard(tok, posW);
    }


    if (ftO.ftCase.equals(FTOption.CASE.INSENSITIVE)) {
      // index request with pre-values as result
      return getNodeFromTrieRecursive(0, tok);
      //ids(tok);
    }

    // index request with pre-values and positions as result
    int[][] ids = getNodeFromTrieRecursive(0, tok);
    if (ids == null) {
      return null;
    }

    if (ftO.ftCase.equals(FTOption.CASE.UPPERCASE)) {
      // convert search string to upper case and use case sensitive search
      //System.out.println("newToken:" + new String(bTok));
      bTok = Token.uc(bTok);
      //System.out.println("newToken:" + new String(bTok));
      //for (int i = 0; i < tok.length; i++) bTok[i] = (byte) Token.uc(bTok[i]);
    } else if (ftO.ftCase.equals(FTOption.CASE.LOWERCASE)) {
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
  

  /**
   * Returns all ids that are in the range of tok0 and tok1.
   * @param tokFrom token defining range start
   * @param itok0 token included in rangebounderies
   * @param tokTo token defining range end
   * @param itok1 token included in rangebounderies
   * @return number of ids
   */
  public int[][] idPosRange(final byte[] tokFrom, final boolean itok0,
      final byte[] tokTo, final boolean itok1) {
    int[][] data = null;
    fromi = itok0;
    toi = itok1;
    
    if (Token.ftdigit(tokFrom) && Token.ftdigit(tokTo)) {
      byte[] ne = getNodeEntry(0);
      int[] nn = getNextNodes(ne);
      if (nn == null) return null;
      
      for (int i = 0; i < nn.length; i++) {
        ne = getNodeEntry(nn[i]);
        if (ne[1] != tokFrom[0] && ne[1] != tokTo[0]) {
          if (tokTo.length == tokFrom.length) {
            if (ne[1] > tokFrom[0] && ne[1] < tokTo[0]) {
              data = getAllNodesWithLevel(nn[i], tokFrom.length, 
                  tokTo.length, data);
            }
          } else {
            int lb = (ne[1] < tokFrom[0]) ? tokFrom.length + 1 : tokFrom.length;
            int ub = (ne[1] < tokTo[0]) ? tokTo.length : tokTo.length + 1;
            data = getAllNodesWithLevel(nn[i], lb, ub, data);
          }
        } else {
          data = getAllNodesWithinBounds(
              nn[i], new byte[tokTo.length], tokTo, tokFrom, 0, data);
        }
      }
    } else if (Token.letterOrDigit(tokFrom) && Token.letterOrDigit(tokTo)) {
      data = idPosRangeText(tokFrom, tokTo);
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
  public int[][] idPosRangeText(final byte[] tokFrom, final byte[] tokTo) {
    // you have to backup all passed nodes 
    // and maybe the next child of the current node.
    int[][] idNNF = new int[2][tokFrom.length + 1];
    int[][] idNNT = new int[2][tokTo.length];
    int c = 0;
    int b; 
    int[][] data = null;
    
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
      data = getDataFromDataArray(getNodeEntry(b), getDataEntry(b));
      data = getAllNodes(b, b, data);
    }
    
    c = 0;
    cl = 0; 
    byte[] nodeEntry;
    int id;
    id = getNodeIdsForRQ(0, tokFrom, c, idNNF);
    c = 0;
    // start travesing with id of tokFrom
    // reduce minimul level, every following node has to be chacked 
    // with same or bigger length than tokFrom
    if (id > -1 && fromi) {
      int[][] tmp = getDataFromDataArray(getNodeEntry(id), getDataEntry(id));
      data = FTUnion.calculateFTOr(data, getAllNodes(id, b, tmp));
    } 
    
    for (int i = 0; i < idNNF[0].length; i++) {
      if (i > 0 && idNNF[0][i] == 0 && idNNF[1][i] == 0) break;
      nodeEntry = getNodeEntry(idNNF[0][i]);
      if (-1 * nodeEntry[nodeEntry[0] + 1] >= idNNF[1][i]) {
        for (int k = idNNF[1][i]; k < -1 * nodeEntry[nodeEntry[0] + 1]; k++)
          data = FTUnion.calculateFTOr(data, 
              getAllNodes(getDataEntry(idNNF[0][i], k), b, data)); 
       } 
    }
      
    return data;
  }
  

  /**
   * Returns all ids from trie stored for tok, with repsect to ftO.
   * @param tok token to be found
   * @param ftO FTOption for tok
   * @return number of ids
   */
  public int[] ids(final byte[] tok, final FTOption ftO) {
    return CTArrayX.getIDsFromData(idPos(tok, ftO));
  }

  /**
   * Returns all ids from trie stored for token.
   * @param tok token to be found
   * @return number of ids
   */
  public int[] ids(final byte[] tok) {
    int nId = getNodeIdFromTrieRecursive(0, tok);
    // no entry found in trie
    if (nId == -1) return new int[0];

    byte[] nodeEntry = getNodeEntry(nId);
    int [] dataEntry = getDataEntry(nId);
    return getPreValues(nodeEntry, dataEntry);
  }

  /**
   * Returns the decompressed ids for the specified token.
   * @param tok token to be found
   * @return ids
   */
  public int nrIDs(final byte[] tok) {
    return ids(tok).length;
  }

  /**
   * Close the index.
   * @throws IOException in case of write errors
   */
  public synchronized void close() throws IOException {
    //id.close();
    idxv.close();
    idxx.close();
    idxy.close();
    idxd.close();
    //txt.close();
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

    int nId = getNodeIdFromTrieRecursive(cNode, searchNode);
    if (nId == -1) {
      // no entry found in trie
      return null;
    }
    byte[] nodeEntry = getNodeEntry(nId);
    int [] dataEntry = getDataEntry(nId);
    return getDataFromDataArray(nodeEntry, dataEntry);
  }

  /**
   * Traverse trie and return found node for searchValue.
   * Returns data from node or null.
   *
   * @param currentCompressedTrieNode int
   * @param searchNode search nodes value
   * @return int id on node saving the data
   */
  private int getNodeIdFromTrieRecursive(final int currentCompressedTrieNode,
      final byte[] searchNode) {

    byte[] valueSearchNode = searchNode;

    // read data entry from disk
    byte[] nodeEntry = getNodeEntry(currentCompressedTrieNode);
    //int[] dataEntry;

    if(currentCompressedTrieNode != 0) {
      int i = 0;
      // read data entry from disk
      //dataEntry = getDataEntry(currentCompressedTrieNode);

      while(i < valueSearchNode.length
          //&& i < nodes[currentCompressedTrieNode][0]
          && i < nodeEntry[0]
          //&& nodes[currentCompressedTrieNode][i + 1] == valueSearchNode[i]) {
          && nodeEntry[i + 1] == valueSearchNode[i]) {
        i++;
      }

      //if (nodes[currentCompressedTrieNode][0] == i) {
      if(nodeEntry[0] == i) {
        if(valueSearchNode.length == i) {
          // leaf node found with appropriate value
          //return getDataFromDataArray(currentCompressedTrieNode);
          //return getDataFromDataArray(nodeEntry, dataEntry);
          return currentCompressedTrieNode;
        } else {
          // cut valueSearchNode for value current node
          byte[] tmp = new byte[valueSearchNode.length - i];
          for(int j = 0; j < tmp.length; j++) {
            tmp[j] = valueSearchNode[i + j];
          }
          valueSearchNode = tmp;

          // scan successors currentNode
          int position = getInsertingPositionLinear(valueSearchNode[0],
              nodeEntry);
          if(!found) {
            // node not contained
            //System.out.println("1. Fall nicht gefunden");
            return -1;
          } else {
            int id = getIdOnDataArray(nodeEntry);
            //dataEntry = getDataEntry(id);
            // return getNodeFromTrieRecursive(dataEntry[position],
            //    valueSearchNode);
            return getNodeIdFromTrieRecursive(getDataEntry(id, position),
                valueSearchNode);
          }
        }
      } else {
        // node not contained
        //System.out.println("2. Fall nicht gefunden");
        return -1;
      }
    } else {
      // scan successors current node
      //int position = getInsertingPositionLinear(currentCompressedTrieNode,
      //    valueSearchNode[0]);

      int position = getInsertingPositionLinear(valueSearchNode[0], nodeEntry);
      if(!found) {
        // node not contained
        //System.out.println("3. Fall nicht gefunden");
        return -1;
      } else {
        //int id = getIdOnDataArray(currentCompressedTrieNode);
        int id = getIdOnDataArray(nodeEntry);
        //dataEntry = getDataEntry(id);
        //return getNodeFromTrieRecursive(dataEntry[position], valueSearchNode);
        return getNodeIdFromTrieRecursive(getDataEntry(id, position),
            valueSearchNode);
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
      final byte[] searchNode, final int c, final int[][] idNN) {

    byte[] valueSearchNode = searchNode;

    // read data entry from disk
    byte[] nodeEntry = getNodeEntry(cn);

    if(cn != 0) {
      int i = 0;
      // read data entry from disk
      while(i < valueSearchNode.length
          && i < nodeEntry[0]
          && nodeEntry[i + 1] == valueSearchNode[i]) {
        i++;
        cl++;
      }

      if(nodeEntry[0] == i) {
        if(valueSearchNode.length == i) {
          // leaf node found with appropriate value
          if(nodeEntry[nodeEntry[0] + 1] < 0) {
            // node is not a leaf node
            //if (mnl <= cl && cl < mxl) {
              idNN[0][c] = cn;
              idNN[1][c] = 0;
            //}
          }
          return cn;
        } else {
          // cut valueSearchNode for value current node
          byte[] tmp = new byte[valueSearchNode.length - i];
          for(int j = 0; j < tmp.length; j++) {
            tmp[j] = valueSearchNode[i + j];
          }
          valueSearchNode = tmp;

          // scan successors currentNode
          int position = getInsertingPositionLinear(valueSearchNode[0],
              nodeEntry);
          if(!found) {
            // node not contained
            return -1;
          } else {
            //if (mnl <= cl && cl >= mxl) return -1;
            int id = getIdOnDataArray(nodeEntry);
            id = getDataEntry(id, position);
            idNN[0][c] = cn;
            idNN[1][c] = position + 1; 
            return getNodeIdsForRQ(id,
                valueSearchNode, c + 1, idNN);
          }
        }
      } else {
        // node not contained
        return -1; 
      }
    } else {
      // scan successors current node
      int position = getInsertingPositionLinear(valueSearchNode[0], nodeEntry);
      if(!found) {
        // node not contained
        return -1;
      } else {
        //if (cl >= mxl) return -1;
        int id = getIdOnDataArray(nodeEntry);
        id = getDataEntry(id, position);
        idNN[0][c] = cn;
        idNN[1][c] = position + 1;
        return getNodeIdsForRQ(id,
            valueSearchNode, c + 1, idNN);
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
   * @param lb lower bound of the range
   * @param c int count number of passed nodes
   * @param data data found in the trie
   * @return int id on node saving the data
   */
  public int[][] getAllNodesWithinBounds(final int id, final byte[] v, 
      final byte[] ub, final byte[] lb, final int c, final int[][] data) {
    int[][] dn = data;
    byte[] ne = getNodeEntry(id);
    
    if (ne[0] + c < lb.length) {
      // process children
      int[] nn = getNextNodes(ne);
      if (nn == null) return data;
      System.arraycopy(ne, 1, v, c, ne[0]);
      for (int i = 0; i < nn.length; i++) {
        dn = getAllNodesWithinBounds(nn[i], v, ub, lb, c + ne[0], dn);
      }
    } else if (ne[0] + c > ub.length) {
      return dn;
    } else {
      System.arraycopy(ne, 1, v, c, ne[0]);
      if (checkLBConstrain(v, c + ne[0], lb) 
          && checkUBConstrain(v, c + ne[0], ub)) {
        // add any existing data
        int[] de = getDataEntry(getIdOnDataArray(ne));
        dn = FTUnion.calculateFTOr(dn, getDataFromDataArray(ne, de));
      }
      if (c + ne[0] + 1 <= ub.length) {
        // process children
        int[] nn = getNextNodes(ne);
        if (nn == null) return data;
        for (int i = 0; i < nn.length; i++) {
          dn = getAllNodesWithinBounds(nn[i], v, ub, lb, c + ne[0], dn);
        } 
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
   * @param data data found in the trie
   * @return int[][] data
   */ 
  public int[][] getAllNodesWithLevel(final int id, final int l1, 
      final int l2, final int[][] data) {
    int[][] dn = data;
    byte[] ne = getNodeEntry(id);
    
    if (!Token.digit(ne[1]) || ne[0] > l2) return dn;

    if (l1 <= ne[0] && ne[0] <= l2)  {
      int[] de = getDataEntry(getIdOnDataArray(ne));
      dn = FTUnion.calculateFTOr(dn, getDataFromDataArray(ne, de));
      int[] nn = getNextNodes(ne);
      if (nn == null) return data;
      for (int i = 0; i < nn.length; i++) {
        dn = FTUnion.calculateFTOr(dn, 
            getAllNodesWithLevel(nn[i], l1 - ne[0], l2 - ne[0], data));
      }
    } else if (ne[0] < l1) {
      int[] nn = getNextNodes(ne);
      if (nn == null) return data;
      for (int i = 0; i < nn.length; i++) {
        dn = FTUnion.calculateFTOr(dn, 
            getAllNodesWithLevel(nn[i], l1 - ne[0], l2 - ne[0], data));
      }  
    }
    return dn;
  }

  /**
   * Checks whether lb < b.
   * 
   * @param v byte[] value
   * @param c counter for used bytes in v
   * @param lb lower bound of a range
   * @return boolean result of constraintcheck
   */
  private boolean checkLBConstrain(final byte[] v, final int c, 
      final byte[] lb) {
    if (c < lb.length) return false;
    else if (c > lb.length) return true;
    
    int i = 0;
    while (i < lb.length)  {
      if (v[i] > lb[i]) return true;
      else if (v[i] < lb[i]) return false;
      i++;
    }
        
    return fromi;
  }
  
  /**
   * Checks whether ub > v.
   * 
   * @param v byte[] value
   * @param c counter for used bytes in v
   * @param ub upper bound of a range
   * @return boolean result of constraintcheck
   */
  private boolean checkUBConstrain(final byte[] v, final int c, 
      final  byte[] ub) {
    if (c < ub.length) return true;

    int i = 0;
    while (i < ub.length)  {
      if (v[i] < ub[i]) return true;
      else if (v[i] > ub[i]) return false;
      i++;
    }
        
    return toi;
  }

  
  /**
   * Collects and returns all data found at nodes in the range of cn and b.
   * 
   * @param cn id on nodeArray, current node
   * @param b id on nodeArray, lastNode to check (upper bound of range)
   * @param data data found in trie in earlier recursionsteps
   * @return int[][] idpos
   */
  public int [][] getAllNodes(final int cn, final int b, final int[][] data) {
    if (cn !=  b) {
      int[][] newData = data;
      byte[] ne = getNodeEntry(cn);
      int id = getIdOnDataArray(ne);
      int[][] tmp = getDataFromDataArray(ne, getDataEntry(id));
      if (tmp != null) {
        newData = FTUnion.calculateFTOr(data, tmp); 
      }
    
      if (ne[ne[0] + 1] * -1 > 0) {
        // node is not a leaf not; has no data
        // get children of current node
        final int[] nn = new int[ne[ne[0] + 1] * -1];
        System.arraycopy(getDataEntry(id), 0, nn, 0, nn.length);
          
        for (int i = 0; i < nn.length; i++) {
          //newData = FTUnion.calculateFTOr(newData, 
          newData = getAllNodes(nn[i], b, newData);
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
  private byte[] getNodeEntry(final int id) {
    final int ep = idxx.readInt(id * 4L);
    int sp = 0;

    if(id > 0) {
      sp = idxx.readInt((id - 1) * 4L);
    }
    //System.out.println("Token:" + Token.string(idxv.readBytes(sp, ep)));
    return idxv.readBytes(sp, ep);
  }

  /**
   * Read data entry from disk.
   * @param id on data array (in main memory)
   * @return data entry from disk
   */
  private int[] getDataEntry(final int id) {
    final int ep = idxy.readInt(id * 4L) * 4;
    int sp = 0;

    if(id > 0) {
      sp = idxy.readInt((id - 1) * 4L) * 4;
    }

    //for (int i : idxd.readInts(sp, ep)) System.out.print(i+ ",");
    //System.out.println();

    return idxd.readInts(sp, ep);
  }

  /**
   * Read data entry from disk.
   * @param id on data array (in main memory)
   * @param position where value to be read is located
   * @return data entry from disk
   */
  private int getDataEntry(final int id, final int position) {
    int sp = 0;

    if(id > 0) {
      sp = idxy.readInt((id - 1) * 4) * 4;
    }

    return idxd.readInt(sp + position * 4);
  }


  /**
   * Extracts data from data[][] in
   * [[pre1, pos1], [pre2, pos2], ...] representation.
   *
   * @param nodeEntry pointer array storing node entry
   * @param dataEntry pointer array storing all data
   * @return  int[][] data
   */
  private int[][] getDataFromDataArray(final byte[] nodeEntry,
      final int[] dataEntry) {
    // extract id on data array form nodearray

    byte jumpOver = 0;
    //int id;

    // extract id on data array
    //if (nodes[currentNode][nodes[currentNode][0] + 1] < 0) {
    if(nodeEntry[nodeEntry[0] + 1] < 0) {
      // next nodes existing
      //jumpOver = (byte) (-1 * nodes[currentNode][nodes[currentNode][0] + 1]);
      jumpOver = (byte) (-1 * nodeEntry[nodeEntry[0] + 1]);
    }

    //id = getIdOnDataArray(currentNode, nodeEntry);

    //if (data[id].length == jumpOver) {
    if(dataEntry.length == jumpOver) {
      return null;
    }

    final int[][] prePos = new int[2][(dataEntry.length - jumpOver) / 2];
    System.arraycopy(dataEntry, jumpOver, prePos[0], 0, prePos[0].length);
    System.arraycopy(dataEntry, jumpOver + prePos[0].length, prePos[1], 0,
        prePos[0].length);

    return prePos;
  }

  /**
   * Gets all pre-values from trie for node with nodeEntry as value.
   *
   * @param nodeEntry pointer array storing node entry
   * @param dataEntry pointer array storing all data
   * @return  int[] pre-values
   */
  private int[] getPreValues(final byte[] nodeEntry,
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

  /**
   * Extracts the id on data[][] for a given id on node[][].
   *
   * @param nodeEntry array storing node entry
   * @return id on dataArray
   */
  private int getIdOnDataArray(final byte[] nodeEntry) {
    byte[] bId;

    // extract id on data array
    // next nodes existing
    //bId = new byte[nodes[currentNode].length - nodes[currentNode][0]
    bId = new byte[nodeEntry.length - nodeEntry[0] - 2];
    //System.arraycopy(nodes[currentNode],
    //    nodes[currentNode].length - bId.length, bId, 0, bId.length);
    System.arraycopy(nodeEntry,
        nodeEntry.length - bId.length, bId, 0, bId.length);

    //return Array.byteToInt(bId);
    return Array.byteToIntNN(bId);
  }

  /**
   * Save whether a corresponding node was found in method getInsertingPosition.
   */
  private boolean found;

  /**
   * Uses linear search for finding inserting position.
   * returns:
   * 0 if any successor exists, or 0 is inserting position
   * n here to insert
   * n and found = true, if nth item is occupied and here to insert
   *
   * @param toInsert first byte of inserted value
   * @param entry array storing node entry
   * @return inserting position
   */
  private int getInsertingPositionLinear(final byte toInsert,
      final byte[] entry) {
    byte[] nodeEntry = entry;

    // init value
    found = false;

    // node has no successors
    //if (nodes[currentPosition][nodes[currentPosition][0] + 1] > 0) {
    // <SG> look at me strange things happen here...
    if(nodeEntry[nodeEntry[0] + 1] >= 0) return 0;

    // load existing successors
    //int[] nextNodes = getNextNodes(currentPosition);
    final int[] nextNodes = getNextNodes(nodeEntry);
    // first successors > toInsert
    //if (nodes[nextNodes[0]][1] > toInsert) {
    // disk access
    if(getNodeEntry(nextNodes[0])[1] > toInsert) return 0;
    int i = 0;

    for(; i < nextNodes.length; i++) {
      // toInsert already exists; return Id
      // disk access
      nodeEntry = getNodeEntry(nextNodes[i]);
      //if (nodes[nextNodes[i]][1] == toInsert) {
      if(nodeEntry[1] == toInsert) {
        found = true;
        return  i;
      }
      //if (nodes[nextNodes[i]][1] > toInsert) {
      if(nodeEntry[1] > toInsert) {
        return i;
      }
    }
    // next free space in node[]
    return i;
  }

  /**
   * Get nextNodes for node.
   * @param nodeEntry array storing node entry
   * @return int[] id array on nodes[][]
   */
  private int[] getNextNodes(final byte[] nodeEntry) {
    //if (nodes[node][nodes[node][0] + 1] > 0) {
    if(nodeEntry[nodeEntry[0] + 1] > 0) {
      return null;
    }

    //int[] nextNodes = new int[nodes[node][nodes[node][0] + 1] * -1];
    final int[] nextNodes = new int[nodeEntry[nodeEntry[0] + 1] * -1];
    final int id = getIdOnDataArray(nodeEntry);
    //System.arraycopy(data[id], 0, nextNodes, 0, nextNodes.length);
    System.arraycopy(getDataEntry(id), 0, nextNodes, 0, nextNodes.length);
    return nextNodes;
  }

  /** saves astericsWildCardTraversing result
  has to be re-init each time (before calling method). */
  private int[][] astericsWildCardData;

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
    final byte[] curNodeEntry = getNodeEntry(node);
    final int[] curDataEntry = getDataEntry(node);

    // wildcard at the end
    if(ending == null || ending.length == 0) {
      // save data current node
      astericsWildCardData = CTArrayX.ftOR(astericsWildCardData,
          //getDataFromDataArray(node));
          getDataFromDataArray(curNodeEntry, curDataEntry));
      
      //int[] nextNodes = getNextNodes(node);
      final int[] nextNodes = getNextNodes(curNodeEntry);
      if(nextNodes != null) {
        //if (next[node] != null) {
        // preorder traversal through trie
        for(final int n : nextNodes) {
          astericsWildCardTraversing(n, null, last, 0, 0);
        }
      }
      return;
    }

    // compare chars current node and ending
    //if(nodes[node] != null) {
    if(curNodeEntry != null) {
      // skip all unlike chars, if any suitable was found
      //while(!last && i < nodes[node][0] + 1
      //    && nodes[node][i] != ending[j]) {
      while(!last && i < curNodeEntry[0] + 1
          && curNodeEntry[i] != ending[j]) {
  
        i++;
      }
  
      // skip all chars, equal to first char
      //while(i + ending.length < nodes[node][0] + 1
      //    && nodes[node][i + 1] == ending[0]) {
      while(i + ending.length < curNodeEntry[0] + 1
          && curNodeEntry[i + 1] == ending[0]) {
        i++;
      }
  
      countSkippedChars = countSkippedChars + i - pointerNode - 1;
  
      //while(i < nodes[node][0] + 1 && j < ending.length
      //    && nodes[node][i] == ending[j]) {
      while(i < curNodeEntry[0] + 1 && j < ending.length
          && curNodeEntry[i] == ending[j]) {
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
    //if(node == 0 || j == ending.length && i < nodes[node][0] + 1) {
    if(node == 0 || j == ending.length && i < curNodeEntry[0] + 1) {
      // pointer = 0; restart search
      //if(nodes[node][nodes[node][0] + 1] > 0) {
      if(curNodeEntry[curNodeEntry[0] + 1] > 0) {
        countSkippedChars = 0;
        return;
      }
  
      //int[] nextNodes = getNextNodes(node);
      final int[] nextNodes = getNextNodes(curNodeEntry);
      // preorder search in trie
      for(final int n : nextNodes) {
        astericsWildCardTraversing(n, ending, false, 1, 0);
      }
      countSkippedChars = 0;
      return;
    //} else if(j == ending.length && i == nodes[node][0] + 1) {
    } else if(j == ending.length && i == curNodeEntry[0] + 1) {
      // all chars form node and all chars from ending done
      //int[][] d = getDataFromDataArray(node);
      final int[][] d = getDataFromDataArray(curNodeEntry, curDataEntry);
      if(d != null) {
        astericsWildCardData = CTArrayX.ftOR(astericsWildCardData, d);
      }
      countSkippedChars = 0;
  
      //int[] nextNodes = getNextNodes(node);
      final int[] nextNodes = getNextNodes(curNodeEntry);
      // node has successors and is leaf node
      if(nextNodes != null) {
        // preorder search in trie
        for(final int n : nextNodes) {
          if(j == 1) {
            astericsWildCardTraversing(n, ending, false, 0, 0);
          }
          astericsWildCardTraversing(n, ending, last, 0, j);
        }
      }
  
      return;
    //} else if(j < ending.length && i < nodes[node][0] + 1) {
    } else if(j < ending.length && i < curNodeEntry[0] + 1) {
      // still chars from node and still chars from ending left, pointer = 0 and
      // restart searching
      //if(nodes[node][nodes[node][0] + 1] > 0) {
      if(curNodeEntry[curNodeEntry[0] + 1] > 0) {
        countSkippedChars = 0;
        return;
      }
  
      // restart searching at node, but value-position i
      astericsWildCardTraversing(node, ending, false, i + 1, 0);
      return;
  
    //} else if(j < ending.length &&  i == nodes[node][0] + 1) {
    } else if(j < ending.length &&  i == curNodeEntry[0] + 1) {
      // all chars form node processed, but not all chars from processed
  
      // move pointer and go on
      //if(nodes[node][nodes[node][0] + 1] > 0) {
      if(curNodeEntry[curNodeEntry[0] + 1] > 0) {
        //if (next[node] == null) {
        countSkippedChars = 0;
        return;
      }
  
      //int[] nextNodes = getNextNodes(node);
      final int[] nextNodes = getNextNodes(curNodeEntry);
  
      // preorder search in trie
      for(final int n : nextNodes) {
        // compare only first char from ending
        if(j == 1) {
          astericsWildCardTraversing(n, ending, last, 1, 0);
        }
        astericsWildCardTraversing(n, ending, last, 1, j);
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
   * @param currentCompressedTrieNode current node
   * @param searchNode value looking for
   * @param posWildcard wildcards position
   * @param recCall first call??
   * @return data result ids
   */
  private int[][] getNodeFromTrieWithWildCard(
      final int currentCompressedTrieNode, final byte[] searchNode,
      final int posWildcard, final boolean recCall) {

    final byte[] valueSearchNode = searchNode;
    byte[] afterWildcard = null;
    byte[] beforeWildcard = null;
    //byte[] searchValue = null;
    final int currentLength = 0;
    int resultNode;
    //int workerNode;
    int[][] d = null;
    // wildcard not at beginning
    if(posWildcard > 0) {
      // copy part before wildcard
      beforeWildcard = new byte[posWildcard];
      System.arraycopy(valueSearchNode, 0, beforeWildcard, 0, posWildcard);
      resultNode = getNodeFromTrieRecursiveWildcard(currentCompressedTrieNode,
          beforeWildcard);
      if(resultNode == -1) {
        return null;
      }
    } else {
      resultNode = 0;
    }

    byte wildcard;
    if(posWildcard + 1 >= valueSearchNode.length) {
      wildcard = '.';
    } else {
      wildcard = valueSearchNode[posWildcard + 1];
    }

    if(wildcard == '?') {
      // append 0 or 1 symbols

      // look in trie without wildcard
      byte[] searchChar = new byte[valueSearchNode.length - 2 - currentLength];
      // copy unprocessed part before wildcard
      if(beforeWildcard != null) {
        System.arraycopy(beforeWildcard, 0, searchChar, 0,
            beforeWildcard.length);
      }
      // copy part after wildcard
      if(beforeWildcard == null) {
        System.arraycopy(valueSearchNode, posWildcard + 2, searchChar,
            0, searchChar.length);
      } else {
        System.arraycopy(valueSearchNode, posWildcard + 2, searchChar,
            beforeWildcard.length, searchChar.length - beforeWildcard.length);
      }

      d = getNodeFromTrieRecursive(0, searchChar);
      //System.out.println("searchChar:" + new String(searchChar));

      // lookup in trie with . as wildcard
      searchChar = new byte[valueSearchNode.length - 1];
      if(beforeWildcard != null) {
        // copy unprocessed part before wildcard
        System.arraycopy(beforeWildcard, 0, searchChar, 0,
            beforeWildcard.length);
        searchChar[beforeWildcard.length] = '.';

        // copy part after wildcard
        System.arraycopy(valueSearchNode, posWildcard + 2, searchChar,
            beforeWildcard.length + 1, searchChar.length -
            beforeWildcard.length - 1);
      } else {
        // copy unprocessed part before wildcard
        searchChar[0] = '.';

        // copy part after wildcard
        System.arraycopy(valueSearchNode, posWildcard + 2, searchChar, 1,
            searchChar.length - 1);
      }

      // attach both result
      //d = ftOR(d, getNodeFromTrieWithWildCard(0, searchChar,
      d = CTArrayX.ftOR(d, getNodeFromTrieWithWildCard(0, searchChar,
          posWildcard, false));
      return d;
    } else if(wildcard == '*') {
      // append 0 or n symbols

      // valueSearchNode == .*
      if(!(posWildcard == 0 && valueSearchNode.length == 2)) {
        // lookup in trie without wildcard
        final byte[] searchChar = new byte[valueSearchNode.length
                                     - 2 - currentLength];
        // copy unprocessed part before wildcard
        if(beforeWildcard != null) {
          System.arraycopy(beforeWildcard, 0, searchChar, 0,
              beforeWildcard.length);
        }
        // copy part after wildcard
        if(beforeWildcard == null) {
          afterWildcard = new byte[searchChar.length];
          System.arraycopy(valueSearchNode, posWildcard + 2, searchChar, 0,
              searchChar.length);
          System.arraycopy(valueSearchNode, posWildcard + 2, afterWildcard, 0,
              searchChar.length);
        } else {
          afterWildcard = new byte[searchChar.length - beforeWildcard.length];
          System.arraycopy(valueSearchNode, posWildcard + 2, searchChar,
              beforeWildcard.length, searchChar.length - beforeWildcard.length);
          System.arraycopy(valueSearchNode, posWildcard + 2, afterWildcard,
              0, searchChar.length - beforeWildcard.length);
        }

        d = getNodeFromTrieRecursive(0, searchChar);
        //System.out.println("searchChar:" + new String(searchChar));

        // all chars from valueSearchNode are contained in trie
        if(beforeWildcard != null && counter[1] != beforeWildcard.length) {
          return d;
        }
      }

      // delete data
      astericsWildCardData = null;

      astericsWildCardTraversing(resultNode, afterWildcard, false,
          counter[0], 0);
      //return ftOR(d, astericsWildCardData);
      return CTArrayX.ftOR(d, astericsWildCardData);
    } else if(wildcard == '+') {
      // append 1 or more symbols
      // lookup in trie with . as wildcard
      final byte[] searchChar = new byte[valueSearchNode.length - 1 -
                                         currentLength];
      // copy unprocessed part before wildcard
      if(beforeWildcard != null) {
        System.arraycopy(beforeWildcard, 0, searchChar, 0,
            beforeWildcard.length);
      }
      // set . as wildcard
      searchChar[posWildcard] = '.';

      // copy part after wildcard
      if(beforeWildcard == null) {
        // valueSearchNode == .+
        if(!(posWildcard == 0 && valueSearchNode.length == 2)) {
          afterWildcard = new byte[searchChar.length];
          System.arraycopy(valueSearchNode, posWildcard + 2, searchChar, 1,
              searchChar.length);
          System.arraycopy(valueSearchNode, posWildcard + 2, afterWildcard, 1,
              searchChar.length);
        }
      } else {
        afterWildcard = new byte[searchChar.length - beforeWildcard.length - 1];
        System.arraycopy(valueSearchNode, posWildcard + 2, searchChar,
            beforeWildcard.length +  1,
            searchChar.length - beforeWildcard.length - 1);
        System.arraycopy(valueSearchNode, posWildcard + 2,
            afterWildcard, 0, searchChar.length - beforeWildcard.length - 1);
      }

      // wildcard search with . as wildcard
      d = getNodeFromTrieWithWildCard(0, searchChar, posWildcard, true);

      // at least one char has to be added
      if (d == null) {
        return null;
      }

      byte[] newValue;
      if(afterWildcard != null) {
        newValue = new byte[beforeWildcard.length + 3 + afterWildcard.length];
        System.arraycopy(afterWildcard, 0, newValue, beforeWildcard.length + 3,
            afterWildcard.length);
      } else {
        if (beforeWildcard == null)
          newValue = new byte[3];
        else
          newValue = new byte[beforeWildcard.length + 3];
      }
      newValue[beforeWildcard.length + 1] = '.';
      newValue[beforeWildcard.length + 2] = '*';

      System.arraycopy(beforeWildcard, 0, newValue, 0, beforeWildcard.length);
      // take resultNodes from wildcard search with . as wildcard and start
      // new wildcard search with * as wildcard
      for(final byte v : valuesFound) {
        if(v != 0) {
          newValue[beforeWildcard.length] = v;
          //System.out.println(Token.string(newValue));
          d = CTArrayX.ftOR(d, getNodeFromTrieWithWildCard(newValue,
              beforeWildcard.length + 1));
        }
      }

      return d;
    } else {
      final byte[] resNodeEntry = getNodeEntry(resultNode);
      //System.out.println(Token.string(resNodeEntry));
      // append 1 symbol
      // not completely processed (value current node)
      //if(nodes[resultNode][0] > counter[0]) {
      if(resNodeEntry[0] > counter[0]) {
        // replace wildcard with value from currentCompressedTrieNode
        //valueSearchNode[posWildcard] = nodes[resultNode][counter[0] + 1];
        valueSearchNode[posWildcard] = resNodeEntry[counter[0] + 1];

        // . wildcards left
        final int [][] resultData = getNodeFromTrieRecursive(0,
            valueSearchNode);
        // save nodeValues for recursive method call
        if(resultData != null && recCall) {
          //valuesFound = new byte[] {nodes[resultNode][counter[0] + 1]};
          valuesFound = new byte[] {resNodeEntry[counter[0] + 1]};
        }
        return resultData;

      //} else if(nodes[resultNode][0] == counter[0]) {
      } else if(resNodeEntry[0] == counter[0]) {
        // all chars from nodes[resultNode] are computed

        // any next values existing

        //if(nodes[resultNode][nodes[resultNode][0] + 1] > 0) {
        if(resNodeEntry[resNodeEntry[0] + 1] > 0) {
          return null;
        }

        int[][] tmpNode = null;
        afterWildcard = new byte[valueSearchNode.length - posWildcard];
        System.arraycopy(valueSearchNode, posWildcard + 1, afterWildcard,
            1, afterWildcard.length - 1);

        //int[] nextNodes = getNextNodes(resultNode);
        final int[] nextNodes = getNextNodes(resNodeEntry);

        // simple method call
        if(!recCall) {
          byte[] ne;
          for(final int n : nextNodes) {
            // replace first letter
            //afterWildcard[0] = nodes[nextNodes[i]][1];
            ne = getNodeEntry(n);
            afterWildcard[0] = ne[1];

            //tmpNode = ftOR(tmpNode, getNodeFromTrieRecursive(nextNodes[i],
            tmpNode = CTArrayX.ftOR(tmpNode,
                getNodeFromTrieRecursive(n,
                afterWildcard));
          }

          return tmpNode;
        } else {
          // method call for .+ wildcard
          valuesFound = new byte[nextNodes.length];
          byte[] ne;
          for(int i = 0; i < nextNodes.length; i++) {
            // replace first letter
            ne = getNodeEntry(nextNodes[i]);
            //afterWildcard[0] = nodes[nextNodes[i]][1];
            //valuesFound[i] = nodes[nextNodes[i]][1];
            afterWildcard[0] = ne[1];
            valuesFound[i] = ne[1];

            tmpNode = CTArrayX.ftOR(tmpNode,
                getNodeFromTrieRecursive(nextNodes[i],
                afterWildcard));
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
   * @param currentCompressedTrieNode int
   * @param searchNode int
   * @return id int last touched node
   */
  private int getNodeFromTrieRecursiveWildcard(
      final int currentCompressedTrieNode, final byte[] searchNode) {

    byte[]valueSearchNode = searchNode;
    final byte[] curNodeEntry = getNodeEntry(currentCompressedTrieNode);
    if(currentCompressedTrieNode != 0) {
      //counter[1] += nodes[currentCompressedTrieNode][0];
      counter[1] += curNodeEntry[0];

      int i = 0;
      while(i < valueSearchNode.length
          //&& i < nodes[currentCompressedTrieNode][0] &&
          && i < curNodeEntry[0] &&
          curNodeEntry[i + 1] == valueSearchNode[i]) {
        i++;
      }

      //if(nodes[currentCompressedTrieNode][0] == i) {
      if(curNodeEntry[0] == i) {
        if(valueSearchNode.length == i) {
          counter[0] = i;
          // leafnode found with appropriate value
          return currentCompressedTrieNode;
        } else {
          // cut valueSearchNode for value current node
          final byte[] tmp = new byte[valueSearchNode.length - i];
          for(int j = 0; j < tmp.length; j++) {
            tmp[j] = valueSearchNode[i + j];
          }
          valueSearchNode = tmp;

          // scan successors currentNode
          // int position = getInsertingPositionLinear(
          //    currentCompressedTrieNode,
          // valueSearchNode[0]);
          final int position = getInsertingPositionLinear(valueSearchNode[0],
              curNodeEntry);
          if(!found) {
            // node not contained
            counter[0] = i;
            //counter[1] = counter[1] - nodes[currentCompressedTrieNode][0] + i;
            counter[1] = counter[1] - curNodeEntry[0] + i;

            // System.out.println("1. Fall nicht gefunden");
            return currentCompressedTrieNode;
          } else {
            //int id = getIdOnDataArray(currentCompressedTrieNode);
            final int id = getIdOnDataArray(curNodeEntry);

            //return getNodeFromTrieRecursiveWildcard(data[id][position],
            return getNodeFromTrieRecursiveWildcard(getDataEntry(id)[position],
                valueSearchNode);
          }
        }
      } else {
        // node not contained
        counter[0] = i;
        //counter[1] = counter[1] - nodes[currentCompressedTrieNode][0] + i;
        counter[1] = counter[1] - curNodeEntry[0] + i;

        //System.out.println("2. Fall nicht gefunden");
        return currentCompressedTrieNode;
      }
    } else {
      // scan successors current node
      //int position = getInsertingPositionLinear(currentCompressedTrieNode,
      //    valueSearchNode[0]);
      final int position = getInsertingPositionLinear(valueSearchNode[0],
          curNodeEntry);

      if(!found) {
        // node not contained
        //System.out.println("3. Fall nicht gefunden");
        counter[0] = -1;
        counter[1] = -1;

        return -1;
      } else {
        //int id = getIdOnDataArray(currentCompressedTrieNode);
        final int id = getIdOnDataArray(curNodeEntry);
        //return getNodeFromTrieRecursiveWildcard(data[id][position],
        return getNodeFromTrieRecursiveWildcard(getDataEntry(id)[position],
            valueSearchNode);
      }
    }
  }

  /**
   * Traverse trie and return found node for searchValue.
   * Returns data from node or null.
   *
   * @param currentCompressedTrieNode int
   * @param searchNode searchnodes value
   * @return int[][]
  private int[][] getNodeFromTrieRecursiveOld(final int
      currentCompressedTrieNode,  final byte[] searchNode) {

    byte[] valueSearchNode = searchNode;

    // read data entry from disk
    final byte[] nodeEntry = getNodeEntry(currentCompressedTrieNode);
    int[] dataEntry;

    if(currentCompressedTrieNode != 0) {
      int i = 0;
      // read data entry from disk
      dataEntry = getDataEntry(currentCompressedTrieNode);

      while(i < valueSearchNode.length
          //&& i < nodes[currentCompressedTrieNode][0]
          && i < nodeEntry[0]
          //&& nodes[currentCompressedTrieNode][i + 1] == valueSearchNode[i]) {
          && nodeEntry[i + 1] == valueSearchNode[i]) {
        i++;
      }

      //if (nodes[currentCompressedTrieNode][0] == i) {
      if(nodeEntry[0] == i) {
        if(valueSearchNode.length == i) {
          // leaf node found with appropriate value
          //return getDataFromDataArray(currentCompressedTrieNode);
          return getDataFromDataArray(nodeEntry, dataEntry);
        } else {
          // cut valueSearchNode for value current node
          final byte[] tmp = new byte[valueSearchNode.length - i];
          for(int j = 0; j < tmp.length; j++) {
            tmp[j] = valueSearchNode[i + j];
          }
          valueSearchNode = tmp;

          // scan successors currentNode
          final int position = getInsertingPositionLinear(valueSearchNode[0],
              nodeEntry);
          if(!found) {
            // node not contained
            //System.out.println("1. Fall nicht gefunden");
            return null;
          } else {
            final int id = getIdOnDataArray(nodeEntry);

            //dataEntry = getDataEntry(id);
            // return getNodeFromTrieRecursive(dataEntry[position],
            //   valueSearchNode);
            return getNodeFromTrieRecursive(getDataEntry(id, position),
            //  valueSearchNode);
          }
        }
      } else {
        // node not contained
        //System.out.println("2. Fall nicht gefunden");
        return null;
      }
    } else {
      // scan successors current node
      //int position = getInsertingPositionLinear(currentCompressedTrieNode,
      //    valueSearchNode[0]);

      final int position = getInsertingPositionLinear(valueSearchNode[0],
          nodeEntry);
      if(!found) {
        // node not contained
        //System.out.println("3. Fall nicht gefunden");
        return null;
      } else {
        //int id = getIdOnDataArray(currentCompressedTrieNode);
        final int id = getIdOnDataArray(nodeEntry);
        //dataEntry = getDataEntry(id);
        //return getNodeFromTrieRecursive(dataEntry[position], valueSearchNode);
        return getNodeFromTrieRecursive(getDataEntry(id, position),
            valueSearchNode);
      }
    }
  }
   */

  /**
  public void printTables() {
     System.out.println("nodesSize:");
     System.out.println(java.util.Arrays.toString(idxx.readInts(0, 32)));
     System.out.println("nodes:");
     System.out.println(java.util.Arrays.toString(idxv.readBytes(0, 56)));
     System.out.println(idxv.readBytes(0, 224).length);
     System.out.println("dataSize:");
     System.out.println(java.util.Arrays.toString(idxy.readInts(0, 32)));
     System.out.println("data:");
     System.out.println(java.util.Arrays.toString(idxd.readInts(0, 84)));
  }
  */
  
  /**
   * Determines and returns position of '.' in token tok. Return -1 if '.'
   * wasn't found.
   * @param tok token with wildcard
   * @return pos position of wildcard
  private int getPosWildCard(final byte[] tok) {
    for(int i = 0; i < tok.length; i++) {
      if (tok[i] == '.') {
        return i;
      }
    }
    return -1;
  }
   */
}
