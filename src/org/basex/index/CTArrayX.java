package org.basex.index;

import org.basex.util.Array;
import org.basex.util.IntArrayList;

/**
 * Preserves an extended compressed trie index structure and useful
 * functionality.
 * <pre>
 * nodes: [[k, v1, .., vk, t, b1, b2, b3, b4], [...]]
 * data: [[n1, ..., nt, pre1, ..., prej, pos1, ..., posj], [...]]
 * </pre>
 * b1, b2, b3, b4 have to be converted to int and represent the index
 * on data array, for a record in nodes array.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Sebastian Gath
 */
public final class CTArrayX {
  /** Node array - saves node values. */
  byte[][] nodes;
  /** Data array - saves ids and pos. */
  int[][] data;
  /** size of each data entry in data[][]. */
  int[] sizeData;
  /** size of each node entry in nodes[][]. */
  int[] sizeNodes;
  /** Source index for transport. */
  private CTArray cta;

  /**
   * First build and fill FTIndexCTArray, then do transport.
   * @param ct CTArray as base for CTArrayX
   */
  CTArrayX(final CTArray ct) {
    cta = ct;
    //doTransport();
  }

  /**
   * Returns index statistics.
   * @return statistics string
   */
  String info() {
    //System.exit(0);
    return "Number nodes=" + nodes.length + "\n number index entries="
    + nodes.length;
  }

  /**
   * Returns the number of index entries.
   * @return number of index entries
   */
  int size() {
    return nodes.length;
  }

  /**
   * Extracts words from the specified byte array and returns its ids and pos.
   * @param tok token to be extracted and indexed
   * @return ids
   */
  int[][] getIDPos(final byte[] tok) {
    return getNodeFromTrieRecursive(0, tok);
  }

  /**
   * Extracts words from the specified byte array and returns its ids and
   * positions. The use of wildcard in tok at position posWildCard is supplied.
   *
   * @param tok token
   * @param posWildCard wildcard has position
   * @return ids and pos
   */
  int[][] getIDWithWildcard(final byte[] tok, final int posWildCard) {
    return getNodeFromTrieWithWildCard(tok, posWildCard);
  }

  /**
   *  Is called from BaseXData and saves content values in the compressed trie.
   *
   * @param content content of token
   * @param id id of token
  void index(final byte[] content, final int id) {
    cta.index(content, id);
  }
   */

  /**
   * Finish compressed trie and do transport for compressed trie X.
   */
  void finish() {
    cta.finish();
    //doTransport();
    //cta = null;
    //printNodeStatistic();
    /*System.gc();
        System.gc();
        System.gc();
        System.gc();*/
    //printNewTrie();
  }

  /**
   * Reduce memory usage for node entry.
   * @param nodeEntry value of node entry
   * @return nodeEntry shrinked value of node entry
   */
  byte[] shrinkNodeEntry(final byte[] nodeEntry) {
    final byte[] b = calculateFirst2Byte(nodeEntry[0], nodeEntry[1],
        nodeEntry[nodeEntry[0] + 1]);
    final byte[] newEntry = new byte[nodeEntry.length - 1];
    newEntry[0] = b[0];
    newEntry[1] = b[1];
    if(nodeEntry[0] > 1)
      System.arraycopy(nodeEntry, 2, newEntry, 2, nodeEntry[0] - 1);

    System.arraycopy(nodeEntry, nodeEntry[0] + 2, newEntry,
        nodeEntry[0] + 1, nodeEntry.length - nodeEntry[0] - 2);
    return newEntry;
  }


  /**
   * Transport a compressedTrieArray to compressedTrieArrayX.
   */
  void doTransport() {
    //int sum = 0;
    nodes = new byte[cta.countNodes][];
    sizeNodes = new int[cta.countNodes];
    data = new int[cta.countNodes][];
    sizeData = new int[cta.countNodes];
    int count = 0;
    boolean sub;
    
  /*  
    for (int i=0; i<cta.countNodes; i++) {
      if (cta.next[i] != null) {
        boolean f = false;
        for (int k: cta.next[i]) if (k > cta.countNodes) {
          f = true;
          break;
        }
        if (f) {
          System.out.println(i);
        }
      } 
    }
*/
    // copy root info
    nodes[0] = new byte[3];
    nodes[0][0] = 0;
    nodes[0][1] = (byte) -cta.next[0].length;
    // set id on data
    nodes[0][2] = (byte) count;
    // set size info
    sizeNodes[0] = nodes[0].length;

    // copy data
    data[0] = new int[cta.next[0].length];
    System.arraycopy(cta.next[0], 0, data[0], 0, cta.next[0].length);

    
    // set size info
    sizeData[0] = data[0].length;

    //sum += cta.next[0].length;
    count++;

    for(int i = 1; i < nodes.length; i++) {
      sub = false;
      if(cta.next[i] != null) {
        // convert id to byte[]
        final byte[] id = Array.intToByteNN(count);
        
        // space for nodeinfo
        nodes[i] = new byte[2 + cta.nodes[i].length + id.length];

        // set size info
        sizeNodes[i] = nodes[i].length;

        // set number nextnodes
        nodes[i][cta.nodes[i].length + 1] = (byte) -cta.next[i].length;
        
        // set pointer on data array (id) for nextPointer
        // and pre/pos data
        System.arraycopy(id, 0, nodes[i], cta.nodes[i].length + 2,
            id.length);
        
        // copy all pointer next nodes
        if(cta.data[i] != null) {
          data[count] = new int[cta.next[i].length
                                + ((int[][]) cta.data[i])[0].length * 2];

          // set size info
          sizeData[count] = data[count].length;
        } else {
          data[count] = new int[cta.next[i].length];
          // set size info
          sizeData[count] = data[count].length;
        }
        System.arraycopy(cta.next[i], 0, data[count], 0,
            cta.next[i].length);
        count++;
        sub = true;

        // copy nodevalues
        System.arraycopy(cta.nodes[i], 0, nodes[i], 1,
            cta.nodes[i].length);
        // set nodevalue length
        nodes[i][0] = (byte) cta.nodes[i].length;

      }

      // copy data
      if(cta.data[i] != null) {
        if(sub) {

          // has nextids
          //data[count-1] = new int[((int[][])cta.data[i]).length*2];
          // copy pre-value
          System.arraycopy(((int[][]) cta.data[i])[0], 0,
              data[count - 1], cta.next[i].length,
              ((int[][]) cta.data[i])[0].length);
          // copy position-value
          System.arraycopy(((int[][]) cta.data[i])[1], 0,
              data[count - 1], cta.next[i].length +
              ((int[][]) cta.data[i])[0].length,
              ((int[][]) cta.data[i])[0].length);
        } else {
          // convert id to byte[]
          final byte[] id =  Array.intToByteNN(count);

          // space nodevalue
          nodes[i] = new byte[cta.nodes[i].length + id.length + 2];

          // set size info
          sizeNodes[i] = nodes[i].length;

          // copy nodevalues
          System.arraycopy(cta.nodes[i], 0, nodes[i], 1,
              cta.nodes[i].length);

          // set nodevalue length
          nodes[i][0] = (byte) cta.nodes[i].length;

          // set number nextnodes
          nodes[i][cta.nodes[i].length + 1] = 0;

          // set pointer on data array (id) for nextPointer
          // and pre/pos data
          System.arraycopy(id, 0, nodes[i], cta.nodes[i].length + 2,
              id.length);

          data[count] = new int[((int[][]) cta.data[i])[0].length * 2];

           // set size info
           sizeData[count] = data[count].length;
          // copy pre-value
          System.arraycopy(((int[][]) cta.data[i])[0], 0,
              data[count], 0, ((int[][]) cta.data[i])[0].length);
              // copy position-value
          System.arraycopy(((int[][]) cta.data[i])[1], 0,
              data[count], ((int[][]) cta.data[i])[0].length,
                  ((int[][]) cta.data[i])[0].length);
          count++;
        }
      } 
    }
    
    // clear reference
    cta = null;
    
    //System.out.println("sum="+sum);
    //System.out.println("cta.totDataSize="+cta.totDataSize);
/*    IntArrayList bl = new IntArrayList();
    int[][] list = doPreOrderTravWI(0, new StringBuffer(), 
        new IntArrayList(), bl);
    // add dummy at the end of the index, that points on last data set in il
    bl.add(new int[]{Integer.MAX_VALUE, list.length - 1});
    int[][] ia = bl.finish();
    
    byte[] tmp;
    int[] ndata;
    for (int i = 0; i < ia.length; i++) 
      System.out.println(ia[i][0] + ":" + ia[i][1]);
    for(int[] i : list) {
      tmp = new byte[i[0]];
      for (int k = 0; k < tmp.length; k++) tmp[k] = (byte) i[k + 2];
      System.out.print("length=" + i[0] + ": nb=" + i[1] + ":" 
          + new String(tmp) + " s:" + i[i[0]+2] + ":");
      ndata = new int[i[i[0]+2]];
      System.arraycopy(i, 1 + tmp.length, ndata, 0, ndata.length);
      for (int k:ndata) System.out.print(k + ",");
      System.arraycopy(i, 1 + tmp.length + ndata.length, 
          ndata, 0, ndata.length);
      for (int k:ndata) System.out.print(k + ",");
      System.out.println();
    }
*/
    /*
    int[] index = bl.finish();
    byte[] b;
    for (int i=0; i < index.length; i++) {
      b = new byte[index.length - 1]; 
      System.arraycopy(index[i], 1, b, 0, b.length);
      b = nodes[Array.byteToIntNN(b)];
      byte[] v = new byte[b[0]];
      System.arraycopy(b, 1, v, 0, v.length);
      System.out.println((char)(index[i]) + ":" + new String(v)); 
    }
    */
  }

  /**
   * Save, whether an corresponding node was found in method
   * getInsertingPosition.
   */
  private boolean found;


  /**
   * Get nextNodes for node.
   * @param node  int id on nodes[][]
   * @return int[] id array on nodes[][]
   */
  private int[] getNextNodes(final int node) {
    if(nodes[node][nodes[node][0] + 1] >= 0) {
      return null;
    }

    final int[] nextNodes = new int[nodes[node][nodes[node][0] + 1] * -1];
    final int id = getIdOnDataArray(node);
    System.arraycopy(data[id], 0, nextNodes, 0, nextNodes.length);
    return nextNodes;
  }

  /**
   * Uses linear search for finding inserting position.
   * returns:
   * 0 if any successor exists, or 0 is inserting position
   * n here to insert
   * n and found = true, if nth item is occupied and here to insert
   *
   * @param currentPosition current Position
   * @param toInsert first byte of inserted value
   * @return inserting position
   */
  private int getInsertingPositionLinear(final int currentPosition,
      final byte toInsert) {
    // init value
    found = false;

    // node has no successors
    //if (next[currentPosition] == null ) {
    if(nodes[currentPosition][nodes[currentPosition][0] + 1] > 0) {
      return 0;
    }

    /*if (nodes[currentPosition]
             return 0;
         }
     */
    // load existing successors
    final int[] nextNodes = getNextNodes(currentPosition);
    // first successors > toInsert
    if(nodes[nextNodes[0]][1] > toInsert) {
      return 0;
    }
    int i = 0;
    for(; i < nextNodes.length; i++) {
      // toInsert already exists; return Id
      if(nodes[nextNodes[i]][1] == toInsert) {
        found = true;
        return  i;
      }
      if(nodes[nextNodes[i]][1] > toInsert) {
        return i;
      }
    }
    // next free space in node[]
    return i;
  }


  /**
   * Extracts the id on data[][] for a given id on node[][].
   *
   * @param currentNode current node
   * @return id on dataArray
   */
  private int getIdOnDataArray(final int currentNode) {
    byte[] bId;

    // extract id on data array
    // next nodes existing
    bId = new byte[nodes[currentNode].length - nodes[currentNode][0]
         - 2];
    System.arraycopy(nodes[currentNode],
        nodes[currentNode].length - bId.length, bId, 0, bId.length);

    return Array.byteToInt(bId);
  }

  /**
   * Extracts data form data[][] in
   * [[pre1, pos1], [pre2, pos2], ...] representation.
   *
   * @param currentNode id on node Array
   * @return  int[][] data
   */
  private int[][] getDataFromDataArray(final int currentNode) {
    // extract id on data array form node array

    byte jumpOver = 0;
    int id;

    // extract id on data array
    if(nodes[currentNode][nodes[currentNode][0] + 1] < 0) {
      // next nodes existing
      jumpOver = (byte) (-1 * nodes[currentNode][nodes[currentNode][0] + 1]);
    }

    id = getIdOnDataArray(currentNode);

    if(data[id].length == jumpOver) {
      return null;
    } else {
      final int[][] prePos = new int[2][(data[id].length - jumpOver) / 2];
      // copy pre, position values
      System.arraycopy(data[id], jumpOver, prePos[0], 0, prePos[0].length);
      System.arraycopy(data[id], jumpOver + prePos[0].length, prePos[1], 0,
          prePos[0].length);
      return prePos;
    }
  }

  /**
   * Traverse trie and return found node for searchValue; returns data
   * from node or null.
   *
   * @param currentCompressedTrieNode int
   * @param searchNode search nodes value
   * @return int[][]
   */
  private int[][] getNodeFromTrieRecursive(final int currentCompressedTrieNode,
      final byte[] searchNode) {
    byte[] valueSearchNode = searchNode;

    if(currentCompressedTrieNode != 0) {
      int i = 0;
      while(i < valueSearchNode.length
          && i < nodes[currentCompressedTrieNode][0]
          && nodes[currentCompressedTrieNode][i + 1] == valueSearchNode[i]) {
        i++;
      }

      if(nodes[currentCompressedTrieNode][0] == i) {
        if(valueSearchNode.length == i) {
          // leafnode found with appropriate value
          return getDataFromDataArray(currentCompressedTrieNode);
        } else {
          // cut valueSearchNode for value current node
          final byte[] tmp = new byte[valueSearchNode.length - i];
          for(int j = 0; j < tmp.length; j++) {
            tmp[j] = valueSearchNode[i + j];
          }
          valueSearchNode = tmp;

          // scan successors currentNode
          final int position = getInsertingPositionLinear(
              currentCompressedTrieNode, valueSearchNode[0]);
          if(!found) {
            // node not contained
            //System.out.println("1. Fall nicht gefunden");
            return null;
          } else {
            final int id = getIdOnDataArray(currentCompressedTrieNode);
            return getNodeFromTrieRecursive(data[id][position],
                valueSearchNode);
          }
        }
      } else {
        // node not contained
        //System.out.println("2. Fall nicht gefunden");
        return null;
      }
    } else {
      // scan successors current node
      final int position = getInsertingPositionLinear(currentCompressedTrieNode,
          valueSearchNode[0]);
      if(!found) {
        // node not contained
        //System.out.println("3. Fall nicht gefunden");
        return null;
      } else {
        final int id = getIdOnDataArray(currentCompressedTrieNode);
        return getNodeFromTrieRecursive(data[id][position], valueSearchNode);
      }
    }
  }


  /** saves astericsWildCardTraversing result
      has to be reinitialized each time (before calling method). */
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

    // wildcard at the end
    if(ending == null || ending.length == 0) {
      // save data current node
      astericsWildCardData = ftOR(astericsWildCardData,
          getDataFromDataArray(node));

      final int[] nextNodes = getNextNodes(node);
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
    if(nodes[node] != null) {

      // skip all unlike chars, if any suitable was found
      while(!last && i < nodes[node][0] + 1
          && nodes[node][i] != ending[j]) {
        i++;
      }

      // skip all chars, equal to first char
      while(i + ending.length < nodes[node][0] + 1
          && nodes[node][i + 1] == ending[0]) {
        i++;
      }

      countSkippedChars = countSkippedChars + i - pointerNode - 1;

      while(i < nodes[node][0] + 1 && j < ending.length
          && nodes[node][i] == ending[j]) {
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
    if(node == 0 || j == ending.length && i < nodes[node][0] + 1) {
      // pointer = 0; restart search
      if(nodes[node][nodes[node][0] + 1] > 0) {
        countSkippedChars = 0;
        return;
      }

      final int[] nextNodes = getNextNodes(node);
      // preorder search in trie
      for(final int n : nextNodes) {
        astericsWildCardTraversing(n, ending, false, 1, 0);
      }
      countSkippedChars = 0;
      return;
    } else if(j == ending.length && i == nodes[node][0] + 1) {

      // all chars form node and all chars from ending done
      final int[][] d = getDataFromDataArray(node);
      if(d != null) {
        astericsWildCardData = ftOR(astericsWildCardData, d);
      }
      countSkippedChars = 0;

      final int[] nextNodes = getNextNodes(node);
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
    } else if(j < ending.length && i < nodes[node][0] + 1) {
      // still chars from node and still chars from ending left, pointer = 0 and
      // restart searching
      if(nodes[node][nodes[node][0] + 1] > 0) {
        countSkippedChars = 0;
        return;
      }

      // restart searching at node, but value-position i
      astericsWildCardTraversing(node, ending, false, i + 1, 0);
      return;

    } else if(j < ending.length &&  i == nodes[node][0] + 1) {
      // all chars form node processed, but not all chars from processed

      // move pointer and go on
      if(nodes[node][nodes[node][0] + 1] > 0) {
        //if (next[node] == null) {
        countSkippedChars = 0;
        return;
      }

      final int[] nextNodes = getNextNodes(node);

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
   * counter[1] = number current method call (gets inited before each call)
   */
  int[] counter;

  /**
   * Traverses trie and returns found node for searchValue; returns last
   * touched node.
   *
   * @param currentCompressedTrieNode int
   * @param searchNode int
   * @return id int last touched node
   */
  private int getNodeFromTrieRecursiveWildcard(
      final int currentCompressedTrieNode, final byte[] searchNode) {
    byte[]valueSearchNode = searchNode;
    if(currentCompressedTrieNode != 0) {
      counter[1] += nodes[currentCompressedTrieNode][0];

      int i = 0;
      while(i < valueSearchNode.length
          && i < nodes[currentCompressedTrieNode][0] &&
          nodes[currentCompressedTrieNode][i + 1] == valueSearchNode[i]) {
        i++;
      }

      if(nodes[currentCompressedTrieNode][0] == i) {
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
          final int position = getInsertingPositionLinear(
              currentCompressedTrieNode, valueSearchNode[0]);
          if(!found) {
            // node not contained
            counter[0] = i;
            counter[1] = counter[1] - nodes[currentCompressedTrieNode][0] + i;

            // System.out.println("1. Fall nicht gefunden");
            return currentCompressedTrieNode;
          } else {
            final int id = getIdOnDataArray(currentCompressedTrieNode);
            return getNodeFromTrieRecursiveWildcard(data[id][position],
                valueSearchNode);
          }
        }
      } else {
        // node not contained
        counter[0] = i;
        counter[1] = counter[1] - nodes[currentCompressedTrieNode][0] + i;

        //System.out.println("2. Fall nicht gefunden");
        return currentCompressedTrieNode;
      }
    } else {
      // scan successors current node
      final int position = getInsertingPositionLinear(currentCompressedTrieNode,
          valueSearchNode[0]);
      if(!found) {
        // node not contained
        //System.out.println("3. Fall nicht gefunden");
        counter[0] = -1;
        counter[1] = -1;

        return -1;
      } else {
        final int id = getIdOnDataArray(currentCompressedTrieNode);
        return getNodeFromTrieRecursiveWildcard(data[id][position],
            valueSearchNode);
      }
    }
  }

  /**
   * Returns all ids, saved in the trie, less those out of resultFTContent.
   *
   * @param resultFTContent ids not contained in result
   * @return result Ids out of trie
   *
   */
  int[][] getAllIDPos(final int[][] resultFTContent) {
    int[][] maxResult = null;
    int[][] tmpResult;
    int k = 1;
    while(k < data.length) {
      tmpResult = ftUnaryNot(getDataFromDataArray(k), resultFTContent);
      maxResult = ftOR(maxResult, tmpResult);
      k++;
    }
    return maxResult;
  }


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
   * Support different wildcard operators: ., .+, .* and .?.
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
      d = ftOR(d, getNodeFromTrieWithWildCard(0, searchChar,
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

        // all chars from valueSearchNode are contained in trie
        if(beforeWildcard != null && counter[1] != beforeWildcard.length) {
          return d;
        }
      }

      // delete data
      astericsWildCardData = null;

      astericsWildCardTraversing(resultNode, afterWildcard, false,
          counter[0], 0);
      return ftOR(d, astericsWildCardData);
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
      byte[] newValue;
      
      if(afterWildcard != null) {
        int length = 0;
        if (beforeWildcard == null) 
          length = 3;
        else 
          length = beforeWildcard.length + 3;
        
        newValue = new byte[length + afterWildcard.length];
        System.arraycopy(afterWildcard, 0, newValue, length,
            afterWildcard.length);
      } else {
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
          d = ftOR(d, getNodeFromTrieWithWildCard(newValue,
              beforeWildcard.length + 1));
        }
      }

      return d;
    } else {
      // append 1 symbols
      // not completely processed (value current node)
      if(nodes[resultNode][0] > counter[0]) {
        // replace wildcard with value from currentCompressedTrieNode
        valueSearchNode[posWildcard] = nodes[resultNode][counter[0] + 1];

        // . wildcards left
        final int [][] resultData = getNodeFromTrieRecursive(0,
            valueSearchNode);
        // save nodeValues for recursive method call
        if(resultData != null && recCall) {
          valuesFound = new byte[] {nodes[resultNode][counter[0] + 1]};
        }
        return resultData;

      } else if(nodes[resultNode][0] == counter[0]) {
        // all chars from nodes[resultNode] are computed

        // any next values existing

        if(nodes[resultNode][nodes[resultNode][0] + 1] > 0) {
          return null;
        }

        int[][] tmpNode = null;
        afterWildcard = new byte[valueSearchNode.length - posWildcard];
        System.arraycopy(valueSearchNode, posWildcard + 1, afterWildcard,
            1, afterWildcard.length - 1);

        final int[] nextNodes = getNextNodes(resultNode);

        // simple method call
        if(!recCall) {
          for(final int n : nextNodes) {
            // replace first letter
            afterWildcard[0] = nodes[n][1];

            tmpNode = ftOR(tmpNode, getNodeFromTrieRecursive(n,
                afterWildcard));
          }

          return tmpNode;
        } else {
          // method call for .+ wildcard
          valuesFound = new byte[nextNodes.length];
          for(int i = 0; i < nextNodes.length; i++) {
            // replace first letter
            afterWildcard[0] = nodes[nextNodes[i]][1];
            valuesFound[i] = nodes[nextNodes[i]][1];

            tmpNode = ftOR(tmpNode, getNodeFromTrieRecursive(nextNodes[i],
                afterWildcard));
          }

        }
      }
    }
    return null;
  }

  /**
   * Calculate value length.
   * @param length old length
   * @param b7b8 last two bits
   * @return length new length
   */
  private byte calculateValueLength(final byte length, final byte b7b8) {
    if(length > 63) {
      System.out.println("error - can't index word longer than 63 bytes!!!!");
      System.exit(111);
    }

    // extract first 6 bit
    return (byte) ((length & 0x3f) | b7b8);
  }

  /**
   * Extract first 6 bits from byte k.
   * @param k value
   * @return length number used bit
   */
  int getValueLength(final byte k) {
    return k & 0x3f;
  }

  /**
   * Extract first 5 bits out of byte v1.
   * @param v1 value
   * @return value value out of v1
   */
  byte getValueFromV1(final byte v1) {
    return (byte) ((v1 & 0x1f) + 97);
  }

  /**
   * Extract first 5 bits from byte v1.
   * @param k number values
   * @param v1 value
   * @return number number next nodes
   */
  byte getNumberNextNodes(final byte k, final byte v1) {
    // cut bit7 and bit8 form k
    int length = k & 0xc0;
    // move 6 bits
    length = length >> 6;
    // cut bit6, bit7 and bit8 form v1
    int length1 = v1 & 0xe0;
    // move 3 bits
    length1 = length1 >> 3;
    return (byte) (length | length1);
  }

  /**
   * Calculates value v1 from input data.
   * @param value input value
   * @param b6b7b8 three last bits
   * @return v1 first value
   */
  private byte calculateValueV1(final byte value, final byte b6b7b8) {
    if(value > 'z' || value < 'a') {
      System.out.println("can't index word - no alphanumeric digit");
      System.exit(112);
    }

    // cut 97 => only bit1 to bit5 are used
    // get bit6, bit7, bit8 are free
    return (byte) ((value - 97) | b6b7b8);
  }

  /**
   * Calculates the bytes, saving the number of next nodes for a node.
   * @param numberNextNodes number of next nodes
   * @return byte[2] byte[0] = first 2 bits; byte[1] = bit6 bit7 bit8 are filled
   */
  private byte[] calculateBytesNumberNextNodes(final byte numberNextNodes) {
    if(numberNextNodes > 26) {
      System.out.println("can't index word - " +
          "more than 26 child for one node!!");
      System.exit(113);
    }
    final byte[] returnByte = new byte[2]; //{(byte)0x3F, (byte)0x1F};
    byte tmp;
    // extract bit1 and bit2
    tmp = (byte) (numberNextNodes & 0x3);
    // move at position 7 and 8
    tmp = (byte) (tmp << 6);
    returnByte[0] = (byte) (returnByte[0] | tmp);

    // extract bit3, bit4 and bit5
    tmp = (byte) (numberNextNodes & 0x1c);
    // move at position 6, 7 and 8
    tmp = (byte) (tmp << 3);
    returnByte[1] = (byte) (returnByte[1] | tmp);
    return returnByte;
  }

  /**
   * Calculates the first 2 bytes.
   * @param valueLength length of value
   * @param v1 value v1
   * @param numberNextNodes number of next nodes
   * @return byte[2]: byte[0] = length; byte[1] = v1
   */
  private byte[] calculateFirst2Byte(final byte valueLength, final byte v1,
      final byte numberNextNodes) {
    final byte[] t = calculateBytesNumberNextNodes(numberNextNodes);

    return new byte[] {calculateValueLength(valueLength, t[0]),
        calculateValueV1(v1, t[1])};
  }

  /**
   * Extracts ids out of data array.
   * @param data data
   * @return ids int[]
   */
  static int[] getIDsFromData(final int[][] data) {
    //if (data == null || data.length == 0) {
    if(data == null || data[0].length == 0) {
      return Array.NOINTS;
    }

    //int[] maxResult = new int[data.length];
    final int[] maxResult = new int[data[0].length];
    int counter = 1;
    maxResult[0] = data[0][0];
    //for (int i=1; i<data.length; i++) {
    for(int i = 1; i < data[0].length; i++) {
      //if(maxResult[counter-1] != data[i][0]){
      //  maxResult[counter] = data[i][0];
      //  counter++;
      //}

      if(maxResult[counter - 1] != data[0][i]) {
        maxResult[counter] = data[0][i];
        counter++;
      }
    }

    final int[] result = new int[counter];
    System.arraycopy(maxResult, 0, result, 0, counter);
    return result;
  }

  /**
   * Each result wordA, is not allowed to be contained in result wordB.
   * Each result for wordA, that is not contained in result set wordB,
   * is added to returned.
   * result set
   *
   * @param resultWordA result allowed to be contained
   * @param resultWordB result not allowed to be contained
   * @return data []
   */
  private static int[][] ftUnaryNot(final int[][] resultWordA,
      final int[][] resultWordB) {
    if((resultWordA == null && resultWordB == null) || resultWordA == null) {
      return null;
    }

    // all resultWordA are hits
    if(resultWordB == null) {
      return resultWordA;
    }

    // pointer on resultWordA
    int i = 0;
    // pointer on resultWordB
    int k = 0;
    // counter for result set
    int count = 0;
    // array for result set
    //int[][] maxResult = new int[resultWordA.length][2];
    final int[][] maxResult = new int[2][resultWordA[0].length];
    //for(;i<resultWordA.length; i++) {
    for(; i < resultWordA[0].length; i++) {
      // ignore all minor values
      while(k < resultWordB[0].length
          && resultWordA[0][i] > resultWordB[0][k]) {
        k++;
      }

      // all done for resultWordB
      if(k == resultWordB[0].length) {
        break;
      }

      // same elements -> duplicated elements are possible (in both arrays)
      if(resultWordA[0][i] == resultWordB[0][k]) {

        // ignore following same elements in resultWordA
        while(i < resultWordA[0].length - 1
            && resultWordA[0][i] == resultWordA[0][i + 1]) {
          i++;
        }

        // ignore following same elements in resultWordA
        while(k < resultWordB[0].length - 1
            && resultWordB[0][k] == resultWordB[0][k + 1]) {
          k++;
        }

        // pointer on next element
        k++;
      } else {
        // apply result
        maxResult[0][count] = resultWordA[0][i];
        maxResult[1][count] = resultWordA[1][i];
        count++;
      }
    }

    int[][] result;
    // all done for resultWordA
    if(i == resultWordA[0].length) {
      if(count == 0) {
        return null;
      }

      // copy only filled cells
      result = new int[2][count];
      System.arraycopy(maxResult[0], 0, result[0], 0, count);
      System.arraycopy(maxResult[1], 0, result[1], 0, count);
    } else {
      result = new int[2][count + resultWordA[0].length - i];
      // copy only filled cells
      System.arraycopy(maxResult[0], 0, result[0], 0, count);
      System.arraycopy(maxResult[1], 0, result[1], 0, count);
      // copy not processed
      System.arraycopy(resultWordA[0], i, result[0], count,
          resultWordA[0].length - i);
      System.arraycopy(resultWordA[1], i, result[1], count,
          resultWordA[0].length - i);
    }
    return result;
  }


  /**
   * Buils an or-conjunction of values1 and values2.
   *
   * @param values1 inputset
   * @param values2 inputset
   * @return unionset int[][]
   */
  // <SG> use fulltext function later on
  public static int[][] ftOR(final int[][] values1, final int[][] values2) {
    int[][] val1 = values1;
    int[][] val2 = values2;

    if(val1 == null) {
      return val2;
    } else if(val2 == null) {
      return val1;
    }

    final int[][] maxResult = new int[2][val1[0].length + val2[0].length];

    // calculate maximum
    final int max = Math.max(val1[0].length, val2[0].length);
    if(max == val1.length) {
      final int[][] tmp = val1;
      val1 = val2;
      val2 = tmp;
    }

    // runvariable for values1
    int i = 0;
    // runvariable for values2
    int k = 0;
    // count inserted elements
    int counter = 0;

    int cmpResult;
    // process smaller set
    while(val1[0].length > i) {
      if(k >= val2[0].length) {
        break;
      }
      cmpResult = compareIntArrayEntry(val1[0][i],
          val1[1][i], val2[0][k], val2[1][k]);
      if(cmpResult == 1 || cmpResult == 2) {
        // same Id, pos0 < pos1 oder id0 < id1
        //maxResult[counter] = values2[k];
        maxResult[0][counter] = val2[0][k];
        maxResult[1][counter] = val2[1][k];
        counter++;
        k++;
      } else if(cmpResult == -1 || cmpResult == -2) {
        // same Id, pos0 > pos1 oder id0 > id1
        //maxResult[counter] = values1[i];
        maxResult[0][counter] = val1[0][i];
        maxResult[1][counter] = val1[1][i];
        counter++;
        i++;
        //k++;
      } else {
        // ids and pos identical
        //maxResult[counter] = values1[i];
        maxResult[0][counter] = val1[0][i];
        maxResult[1][counter] = val1[1][i];
        counter++;
        i++;
        k++;
      }
    }


    if(counter == 0) {
      return null;
    }

    int[][] returnArray;

    // all elements form values2 are processed
    //if (k==values2.length && i < values1.length) {
    if(k == val2[0].length && i < val1[0].length) {
      //returnArray = new int[counter+values1.length-i][2];
      returnArray = new int[2][counter + val1[0].length - i];
      // copy left values (bigger than last element values2) from values1
      //System.arraycopy(values1,i,returnArray,counter,values1.length-i);
      System.arraycopy(val1[0], i, returnArray[0], counter,
          val1[0].length - i);
      System.arraycopy(val1[1], i, returnArray[1], counter,
          val1[0].length - i);
    } else {
      // all elements form values1 are processed
      //returnArray = new int[counter+values2.length-k][2];
      returnArray = new int[2][counter + val2[0].length - k];
      // copy left values (bigger than last element values1) from values2
      //System.arraycopy(values2,k,returnArray,counter,values2.length-k);
      System.arraycopy(val2[0], k, returnArray[0], counter,
          val2[0].length - k);
      System.arraycopy(val2[1], k, returnArray[1], counter,
          val2[0].length - k);
    }

    //System.arraycopy(maxResult,0,returnArray,0,counter);
    System.arraycopy(maxResult[0], 0, returnArray[0], 0, counter);
    System.arraycopy(maxResult[1], 0, returnArray[1], 0, counter);
    return returnArray;
  }

  /**
   * Compares 2 int[1][2] arrayentries and returns.
   * * 0 for equality
   * * -1 if intArrayEntry1 < intArrayEntry2 (same id) or
   * * 1  if intArrayEntry1 > intArrayEntry2 (same id)
   * * 2  real bigger (different id)
   * * -2 real smaller (different id)
   *
   * @param data1ID int
   * @param data1Pos int
   * @param data2ID int
   * @param data2Pos int
   * @return int result [0|-1|1|2|-2]
   */
  private static int compareIntArrayEntry(final int data1ID, final int data1Pos,
      final int data2ID, final int data2Pos) {
    if(data1ID == data2ID) {
      if(data1Pos == data2Pos) {
        // equal
        return 0;
      } else if(data1Pos > data2Pos) {
        // equal Id, data1 behind data2
        return 1;
      } else {
        // equal Id, insert data1 before data2
        return -1;
      }
    } else if(data1ID > data2ID) {
      // real bigger
      return 2;
    } else {
      // real smaller
      return -2;
    }
  }

  /**
   * Traverse trie and return found node for searchValue; returns data
   * from node or null.
   *
   * @param cn int current node
   * @param sn search nodes value
   * @param d int number deletes occured
   * @param p int number pastes occured
   * @param r int number replaces occured
   * @param c int sum number of errors 
   * @return int[][] pre and pos values collected
   */
 /*  private int[][] getNodeFuzzy(final int cn, final byte[] sn, final int d, 
       final int p, final int r, final int c) {
    byte[] vsn = sn;

    if(cn != 0) {
      int i = 0;
      
      while(i < vsn.length && i < nodes[cn][0] && nodes[cn][i + 1] == vsn[i]) {
        i++;
      }
      
      if(nodes[cn][0] == i) {
        // node entry complete processed 
        if(vsn.length == i) {
          // leafnode found with appropriate value
          return getDataFromDataArray(cn);
        } else {
          // cut valueSearchNode for value current node
          final byte[] tmp = new byte[vsn.length - i];
          System.arraycopy(vsn, i, tmp, 0, tmp.length);
          vsn = tmp;

          // scan successors currentNode
          final int position = getInsertingPositionLinear(cn, vsn[0]);
          if(!found) {
            // fuzzy search
            if(nodes[cn][nodes[cn][0] + 1] > 0) {
              // node has no successors
              return null;
            }
            final int[] nn = getNextNodes(cn);
            int[][] ld = null;
            byte[] b;
            for (int k = 0; k < nn.length; k++) {
              if (c >= d + p + 2 * r) {
                // delete char
                b = new byte[vsn.length - 1];
                System.arraycopy(vsn, 1, b, 0, b.length);
                ld = FTUnion.calculateFTOr(ld, getNodeFuzzy(nn[k], 
                    b, d + 1, p, r, c));
                // paste char
                b = new byte[vsn.length + 1];
                b[0] = nodes[nn[k]][1];
                System.arraycopy(vsn, 1, b, 1, vsn.length);
                ld = FTUnion.calculateFTOr(ld, getNodeFuzzy(nn[k], 
                    b, d, p + 1, r, c));
                if (c >= d + p + 2 * (r + 1)) {
                  // replace
                  vsn[0] = nodes[nn[k]][1];
                  ld = FTUnion.calculateFTOr(ld, getNodeFuzzy(nn[k], 
                      vsn, d, p, r + 1, c));
                }
              } else {
                return ld;
              }    
            }
            return ld;
          } else {
            final int id = getIdOnDataArray(cn);
            return getNodeFuzzy(data[id][position], vsn, d, p, r, c);
          }
        }
      } else {
        // fuzz search

        // cut valueSearchNode for value current node
        final byte[] tmp = new byte[vsn.length - i];
        System.arraycopy(vsn, i, tmp, 1, tmp.length);
        vsn = tmp;
        int[][] ld = null;
        byte[] b;
        if (c >= d + p + 2 * r) {
          // delete char
          b = new byte[vsn.length - 1];
          System.arraycopy(vsn, 1, b, 0, b.length);
          ld = FTUnion.calculateFTOr(ld, getNodeFuzzy(cn, 
              b, d + 1, p, r, c));
          // paste char
          b = new byte[vsn.length + 1];
          b[0] = nodes[cn][i + 1];
          System.arraycopy(vsn, 1, b, 1, vsn.length);
          ld = FTUnion.calculateFTOr(ld, getNodeFuzzy(cn, 
              b, d, p + 1, r, c));
          if (c >= d + p + 2 * (r + 1)) {
            // replace
            vsn[0] = nodes[cn][i + 1];
            ld = FTUnion.calculateFTOr(ld, getNodeFuzzy(cn, 
                vsn, d, p, r + 1, c));
          }
        } else {
          return ld;
        }    
        return ld;
      }
    } else {
      // scan successors current node (root node)
      final int position = getInsertingPositionLinear(cn,
          vsn[0]);
      if(!found) {
        // fuzzy search
        if(nodes[cn][nodes[cn][0] + 1] > 0) {
          // node has no successors
          return null;
        }
        
        final int[] nn = getNextNodes(cn);
        int[][] ld = null;
        byte[] b;
        for (int k = 0; k < nn.length; k++) {
          if (c >= d + p + 2 * r) {
            // delete char
            b = new byte[vsn.length - 1];
            System.arraycopy(vsn, 1, b, 0, b.length);
            ld = FTUnion.calculateFTOr(ld, getNodeFuzzy(nn[k], 
                b, d + 1, p, r, c));
            // paste char
            b = new byte[vsn.length + 1];
            b[0] = nodes[nn[k]][1];
            System.arraycopy(vsn, 1, b, 1, vsn.length);
            ld = FTUnion.calculateFTOr(ld, getNodeFuzzy(nn[k], 
                b, d, p + 1, r, c));
            if (c >= d + p + 2 * (r + 1)) {
              // replace
              vsn[0] = nodes[nn[k]][1];
              ld = FTUnion.calculateFTOr(ld, getNodeFuzzy(nn[k], 
                  vsn, d, p, r + 1, c));
            }
          } else {
            return ld;
          }    
        }
        return ld;
      } else {
        final int id = getIdOnDataArray(cn);
        return getNodeFuzzy(data[id][position], vsn, d, p, r, c);
      }
    }
  }
  */ 
   /**
    * Does a preorder traversal of the trie and collectes each 
    * nodevalue and its data.
    * 
    * [l, t, o, k, e, n, pre1, pre2, ..., pos1, pos2, ...]
    * 
    * @param nid int current node id
    * @param sb StringBuffer token value 
    * @param il IntArrayList collecting the results
    * @return int[][] with the Results
    */
   public int[][] doPreOrderTrav(final int nid, final StringBuffer sb, 
       final IntArrayList il) {
     int[][] d;
     int[] ds;
     byte[] sbb = new byte[]{};
     byte[] b;

     if (nodes[nid][0] > 0) {
       sbb = new byte[nodes[nid][0]];
       System.arraycopy(nodes[nid], 1, sbb, 0, sbb.length);
       sb.append(new String(sbb));
     
     }
     d = getDataFromDataArray(nid);
     if (d != null) {
       ds = new int[1 + sb.length() + 2 * d[0].length];
       ds[0] = sb.length();
       b = sb.toString().getBytes();
       for (int k = 0; k < b.length; k++) {
         ds[k + 1] = b[k];
       }
       System.arraycopy(d[0], 0, ds, 1 + sb.length(), d[0].length);
       System.arraycopy(d[1], 0, ds, 1 + sb.length() + d[0].length, 
           d[1].length);
       il.add(ds);
       
       ds = getNextNodes(nid);
       if (ds != null) {
         for (int i : ds) { 
           doPreOrderTrav(i, sb, il);
         }
       } 
       sb.delete(sb.length() - sbb.length, sb.length());
     } else {
       ds = getNextNodes(nid);
       if (ds != null) {
         for (int i : ds) {
           doPreOrderTrav(i, sb, il);
         }
       } 
       sb.delete(sb.length() - sbb.length, sb.length());
     }
     
     return il.finish();
   }

   /**
    * Does a preorder traversal of the trie and collectes each 
    * nodevalue and its data.
    * Result has the following format:
    * {[L, NB, t1, t2, ..., tL, pre1, pre2, ..., preN, pos1, pos2, ..., posN],
    *  ...}
    * L = length of the token
    * NB = number of int buckets used in result befor token
    * t1, ..., tL Token
    * pre1, ..., preN prevalues of the Token
    * pos1, ..., posN position values of the Token (pre)
    * 
    * Index has the following format:
    * {[v, s], ...}
    * v = index value (first char of a token)
    * s = number of entries befor current entry
    * 
    * @param nid int current node id
    * @param sb StringBuffer token value 
    * @param il IntArrayList collecting the results
    * @param index IntArrayList collection for indexentries
    * @return int[][] with the Results
    */
   public int[][] doPreOrderTravWI(final int nid, final StringBuffer sb, 
       final IntArrayList il, final IntArrayList index) {
     int[][] d;
     int[] ds;
     byte[] sbb = new byte[]{};
     byte[] b;

     if (nodes[nid][0] > 0) {
       sbb = new byte[nodes[nid][0]];
       System.arraycopy(nodes[nid], 1, sbb, 0, sbb.length);
       sb.append(new String(sbb));
     }
     
     d = getDataFromDataArray(nid);
     if (d != null) {
       ds = new int[3 + sb.length() + 2 * d[0].length];
       // save tokenvalue length
       ds[0] = sb.length();
       // save number of ints written befor
       ds[1] = il.nb;
       // convert byte to int and save tokenvalue
       b = sb.toString().getBytes();
       for (int k = 0; k < b.length; k++) {
         ds[k + 2] = b[k];
       }
       // save number pre values
       ds[sb.length() + 2] = d[0].length;
       System.arraycopy(d[0], 0, ds, 3 + sb.length(), d[0].length);
       System.arraycopy(d[1], 0, ds, 3 + sb.length() + d[0].length, 
           d[1].length);
       il.add(ds);
       
       if (index.size == 0 || 
           index.list[index.size - 1][0] != sb.charAt(0)) {
         index.add(new int[]{sb.charAt(0), il.size - 1});
       }
       
       ds = getNextNodes(nid);
       if (ds != null) {
         for (int i : ds) { 
           doPreOrderTravWI(i, sb, il, index);
         }
       } 
       sb.delete(sb.length() - sbb.length, sb.length());
     } else {
       ds = getNextNodes(nid);
       if (ds != null) {
         for (int i : ds) {
           doPreOrderTravWI(i, sb, il, index);
         }
       } 
       sb.delete(sb.length() - sbb.length, sb.length());
     }
     
     return il.finish();
   }

   /**
    * Does a preorder traversal of the trie and collectes each 
    * nodevalue and its data, sorted by size, and chars
    * Result has the following format:
    * object[0] is an int, safing the number of used buckets of object.
    * object[i] = int[][] collecting all tokens with legnth i.
    * each int[][] entry looks like:
    * {[t0, t1, ..., ti, N, pre1, pre2, ..., preN, pos1, pos2, ..., posN], ...}
    * t0, t1, ... ti :  Token
    * N : number of prevalues
    * pre0, ..., preN prevalues of the Token
    * pos0, ..., posN position values of the Token (pre)
    * @param nid int current node id
    * @param sb StringBuffer token value 
    * @param index IntArrayList collection for indexentries
    * @return Object with the Results
    */
   public Object[] doPreOrderTravWISS(final int nid, final StringBuffer sb, 
       final Object[] index) {
       Object[] ind = doPreOrderTravWISSRec(nid, sb, index);
       
       int c = 0;
       int size = ((Integer) ind[0]).intValue();
       for (int i = 1; i < ind.length; i++) {
         if (ind[i] != null) {
           c++;
           ind[i] = ((IntArrayList) ind[i]).finish();
           if (c == size) break;
         }
       }
       
       return ind;
   }
   
   /**
    * See doPreOrderTravWISS().
    * 
    * @param nid NodeId
    * @param sb Stringbuffer for token
    * @param index saves data
    * @return index
    */
   public Object[] doPreOrderTravWISSRec(final int nid, final StringBuffer sb, 
       final Object[] index) {
     int[][] d;
     int[] ds;
     byte[] sbb = new byte[]{};
     byte[] b;

     if (nodes[nid][0] > 0) {
       sbb = new byte[nodes[nid][0]];
       System.arraycopy(nodes[nid], 1, sbb, 0, sbb.length);
       sb.append(new String(sbb));
     }
     
     d = getDataFromDataArray(nid);
     if (d != null) {
       if (sb.length() < index.length - 1 && index[sb.length()] == null) {
         index[sb.length()] = new IntArrayList();
         index[0] = ((Integer) index[0]).intValue() + 1;
       }
 
       // ds = (t,o,k,e,n,l,pre0,...,prel,pos0,...,posl)
       ds = new int[1 + sb.length() + 2 * d[0].length];
       // save tokenvalue length
       //ds[0] = sb.length();
       // save number of ints written befor
       //ds[1] = il.nb;
       // convert byte to int and save tokenvalue
       b = sb.toString().getBytes();
       for (int k = 0; k < b.length; k++) {
         ds[k] = b[k];
       }
       // save number pre values
       ds[sb.length()] = d[0].length;
       System.arraycopy(d[0], 0, ds, 1 + sb.length(), d[0].length);
       System.arraycopy(d[1], 0, ds, 1 + sb.length() + d[0].length, 
           d[1].length);
       ((IntArrayList) index[sb.length()]).add(ds);
       ds = getNextNodes(nid);
       if (ds != null) {
         for (int i : ds) { 
           doPreOrderTravWISSRec(i, sb, index);
         }
       } 
       sb.delete(sb.length() - sbb.length, sb.length());
     } else {
       ds = getNextNodes(nid);
       if (ds != null) {
         for (int i : ds) {
           doPreOrderTravWISSRec(i, sb, index);
         }
       } 
       sb.delete(sb.length() - sbb.length, sb.length());
     }
     return index;
   }

  /**
   * Extracts words from the specified byte array and returns its ids.
   * @param tok token to be extracted and indexed
   * @return ids
    /* TODO integrieren oder rausnehmen
    private int[] getIDs(byte[] tok) {
      int[] data = getIDsFromData(ftLibrary.fulltext(Token.toString(tok)));
      if(data == null) return new int[0];
      return data;
   }

    private void printNodeStatistic() {
        int sum = 0;
        int length = 0;
        int count = 0;
        for(int i=0; i<nodes.length; i++) {
            if(nodes[i][nodes[i][0]+1] < 0){
                sum = sum + nodes[i][nodes[i][0]+1]*-1;
                count++;
            }
            if(i>0) {
                length = length + nodes[i][0];
            }
        }

        double avg = (double)sum/(double)count;
        double v=0;
        for(int i=0; i<nodes.length; i++) {
            if(nodes[i][nodes[i][0]+1] < 0){
              //System.out.println(avg - (double) (nodes[i][nodes[i][0]+1]*-1));
              v = v + Math.pow(avg - (double) (nodes[i][nodes[i][0]+1]*-1), 2);
            }
        }


        System.out.println("AVG number nextNodes=" + (double)sum/(double)count);
        System.out.println("AVG node length="+ (double)length/
          ((double)nodes.length-1));
        //System.out.println("v=" + v);///(double)count);
        System.out.println("v=" + v/(double)count);///(double)count);
        System.out.println("s=" + Math.sqrt(v/(double)count));

    }
    private void test() {

        for(int i=1; i<nodes.length; i++) {
            nodes[i] = shrinkNodeEntry(nodes[i]);
        }

        System.gc();
        System.gc();
        System.gc();
        System.gc();
        System.gc();
        System.gc();
    }
   */
  
  /**
   * Print whole trie on sysout.
  private void printNewTrie() {
    int j;
    for(final byte[] n : nodes) {

      System.out.print("(" + n[0] + ")");
      j = 1;
      // print value
      while(j <= n[0]) {
        System.out.print((char) n[j]);
        j++;
      }

      if(j < n.length) {
        // next nodes existing
        if(n[j] < 0) {
          System.out.print("\t #next=" + (n[j]) * -1);
        }
        j++;

        final byte[] id = new byte[n.length - j];
        System.arraycopy(n, j, id, 0, id.length);
        final int intId = byteToInt(id);
        System.out.print("\t id=" + intId);
        System.out.print("\t" + intArrayToString(data[intId]));
      }
      System.out.println();
    }
  }
   */

  /**
   * Print entry with id i on sysout.
   * @param i id on nodes[][]
  private void printTrieEntry(final int i) {
    System.out.print("(" + nodes[i][0] + ")");
    int j = 1;
    // print value
    while(j <= nodes[i][0]) {
      System.out.print((char) nodes[i][j]);
      j++;
    }

    if(j < nodes[i].length) {
      // next nodes existing
      if(nodes[i][j] < 0) {
        System.out.print("\t #next=" + (nodes[i][j]) * -1);

      }

      j++;

      final byte[] id = new byte[nodes[i].length - j];
      System.arraycopy(nodes[i], j, id, 0, id.length);
      final int intId = byteToInt(id);
      System.out.print("\t id=" + intId);
      System.out.print("\t" + intArrayToString(data[intId]));
    }
    System.out.println();
  }
   */
  

  /**
   * Print byte out on sysout.
   * @param in byte
  private void printByte(final byte in) {
    System.out.println(in);
    byte b = in;
    if(b % 64 > 0) {
      System.out.print("1");
    } else {
      System.out.print("0");
    }
    b = (byte) (b >> 1);

    /*        for(byte i=0; i<8; i++) {
            //System.out.println(":"+(b&(byte)Math.pow(2,i)));
            if(b%(byte)Math.pow(2,i) > 0) {
                System.out.print("1");
            } else {
                System.out.print("0");
            }
            b=(byte) (b>>1);
        }
        System.out.println();
  }
   */


  /**
   * Transform byte array to lowercase.
   * @param input byte[] in uppercase
   * @return byte[] in lowercase
  static byte[] byteArrayToLowerCase(final byte[] input) {
    for(int i = 0; i < input.length; i++) input[i] = (byte) Token.lc(input[i]);
    return input;
  }
   */

  /**
   * Converts an int[2][*] to string [v0, v1, ...].
   * @param input int array
   * @return string int array as string
  private static String intArrayToString(final int[] input) {
    final StringBuilder sb = new StringBuilder();
    if(input != null && input.length > 0) {
      sb.append("[" + input[0]);
      for(int i = 1; i < input.length; i++) {
        sb.append("," + input[i]);
      }
      sb.append("]");
    }

    return sb.toString();
  }
   */

  /**
   * Converts an int[2][*] to string [v00,v01][v10,v11] ...
   * @param input int[2][*]
   * @return String result string
  private static String intArrayToString(final int[][] input) {
    final StringBuilder sb = new StringBuilder();
    if(input != null && input[0].length > 0) {
      sb.append("[");

      for(int i = 0; i < input[0].length; i++) {
        sb.append("(" + input[0][i] + "," + input[1][i] + ")");
      }
      sb.append("]");
    }
    return sb.toString();
  }
   */
}

