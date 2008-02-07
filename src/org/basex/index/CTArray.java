package org.basex.index;

import org.basex.util.Array;

/**
 * Preserves a compressed trie index structure and useful functionality.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Sebastian Gath
 */
final class CTArray {
  /** saves for each node with nodeId its next nodes
   * (next[nodeId]) as pointer array on nodes[]. **/
  int[][] next;
  /** saves for each node with nodeId its data at data[nodeId],
   * as an int[][]. **/
  Object[] data;
  /** saves for each node with nodeId its value at nodes[nodeId]. **/
  byte[][] nodes;
  /** counts all nodes in the trie. **/
  int countNodes;
  /** saves numbers of ids, saved in data[nodeId]. **/
  private int[] dataSize;
  /** count total number of entries in data[]. **/
  private int totDataSize;

  /**
   * Constructor.
   */
  CTArray() {
    // maximal number of next nodes, for 1 node
    next = new int[26][];
    data = new Object[next.length];
    dataSize = new int[next.length];
    totDataSize = next.length;
    nodes = new byte[26][];
    // root node
    nodes[0] = new byte[0];
    countNodes = 1;
  }

  /**
   * Inserts a node in the next array.
   *
   * @param currentNode int
   * @param compressedTrieNodeToInsert int
   * @param insertingPosition int
   * @return Id on next[currentNode]
   */
  private int insertNodeInNextArray(final int currentNode,
      final int compressedTrieNodeToInsert, final int insertingPosition) {

    totDataSize++;

    if(next[currentNode] == null) {
      // set compressedTrieNodeToInsert as successor
      next[currentNode] = new int[] {compressedTrieNodeToInsert};
      return currentNode;
    }

    // expand array
    final int[] tmp = new int[(next[currentNode]).length + 1];

    // copy values before insertingPosition
    System.arraycopy(next[currentNode], 0, tmp, 0, insertingPosition);

    // insert node
    tmp[insertingPosition] = compressedTrieNodeToInsert;

    // copy remain
    System.arraycopy(next[currentNode], insertingPosition, tmp,
        insertingPosition + 1, tmp.length - insertingPosition - 1);
    next[currentNode] = tmp;

    return insertingPosition;
  }


  /**
   * Inserts a node into the trie.
   *
   * @param currentCompressedTrieNode current node, which gets a new node
   * appended; start with root (0)
   * @param valueToInsert value, which is to be inserted
   * @param d int[][] data, which are to be added at new node
   * @return nodeId, parent node of new node
   */
  private int insertNodeIntoTrie(final int currentCompressedTrieNode,
      final byte[] valueToInsert, final int[][] d) {
    // currentNode is root node
    if(currentCompressedTrieNode == 0) {
      // root has successors
      if(next[0] != null) {

        final int positionToInsert = getInsertingPosition(
            currentCompressedTrieNode, valueToInsert[0]);
        if(!found) {
          // System.out.println("-1. Fall");
          // any child has an appropriate value to valueToInsert;
          //create new node and append it; save data

          // enough space in array
          if(!(countNodes < nodes.length - 1)) {
            reSize();
          }
          nodes[countNodes] = valueToInsert;

          if(d != null) {
            //addDataToNode(countNodes, data, data.length);
            addDataToNode(countNodes, d, d[0].length);
          }
          insertNodeInNextArray(currentCompressedTrieNode, countNodes,
              positionToInsert);
          countNodes++;
          return countNodes - 1;
        } else {
          return insertNodeIntoTrie(
              next[currentCompressedTrieNode][positionToInsert],
              valueToInsert, d);
        }
      }
    }

    final byte[] intersection = calculateIntersection(
        nodes[currentCompressedTrieNode], valueToInsert);
    byte[] remain1 = nodes[currentCompressedTrieNode];
    byte[] remain2 = valueToInsert;

    if(intersection != null) {
      remain1 = getBytes(nodes[currentCompressedTrieNode],
          intersection.length, nodes[currentCompressedTrieNode].length);
      remain2 = getBytes(valueToInsert,
          intersection.length, valueToInsert.length);
    }

    if(intersection !=  null) {
      if(remain1 == null) {
        if(remain2 == null) {
          // char1 == null && char2 == null
          // same value has to be inserted; add data at current node

          // System.out.println("0. Fall");

          if(d != null) {
            //addDataToNode(currentCompressedTrieNode, data, data.length);
            addDataToNode(currentCompressedTrieNode, d, d[0].length);
          }
          return currentCompressedTrieNode;
        } else {
          // char1 == null && char2 != null
          // value of currentNode equals valueToInsert,
          //but valueToInset is longer

          final int positionToInsert =
            getInsertingPosition(currentCompressedTrieNode, remain2[0]);
          if(!found) {
            // create new node and append it, because any child from curretnNode
            // start with the same letter than reamin2.

            // enough space in array
            if(!(countNodes < nodes.length - 1)) {
              reSize();
            }
            nodes[countNodes] = remain2;

            if(d != null) {
              //addDataToNode(countNodes, data, data.length);
              addDataToNode(countNodes, d, d[0].length);
            }
            insertNodeInNextArray(currentCompressedTrieNode,
                countNodes, positionToInsert);
            countNodes++;
            // System.out.println("1. FAll->!found");

            return countNodes - 1;
          } else {
            // System.out.println("1. FAll->found");
            return insertNodeIntoTrie(
                next[currentCompressedTrieNode][positionToInsert],
                remain2, d);
          }
        }
      } else {
        if(remain2 == null) {
          // char1 != null &&  char2 == null
          // value of currentNode equals valuteToInsert,
          //but current has a longer value

          // update value of currentNode.value with intersection
          nodes[currentCompressedTrieNode] = intersection;
          // System.out.println("2. nodes=" + byteArrayToString(nodes));

          final int[][] dataCurrentNode = (int[][])
            this.data[currentCompressedTrieNode];
          // save number of data from currentCompressedTrieNode
          final int dataCurrentNodeSize = dataSize[currentCompressedTrieNode];
          this.data[currentCompressedTrieNode] = null;
          dataSize[currentCompressedTrieNode] = 0;
          if(d != null) {
            //addDataToNode(currentCompressedTrieNode, data, data.length);
            addDataToNode(currentCompressedTrieNode, d, d[0].length);
          }

          if(!found) {
            // create new node and append; because no child from currentNode
            // starts with the same letter, than remain1

            // append next data from currentNode at new node
            if(!(countNodes < nodes.length - 1)) {
              reSize();
            }
            // <SG> FindBugs... remain2 always null?
            // nodes[countNodes] = remain2;
            addDataToNode(countNodes, dataCurrentNode, dataCurrentNodeSize);

            // next data from currentNode.next update
            next[countNodes] = next[currentCompressedTrieNode];
            next[currentCompressedTrieNode] = new int[] {countNodes};
            totDataSize++;


            // System.out.println("2. FAll->!found");
            countNodes++;
            return countNodes - 1;
          } else {
            // System.out.println("2. FAll->found");
            // create new node with value remain1 and append under currentnode;
            // applya data from current node; set data current with data

            if(!(countNodes < nodes.length - 1)) {
              reSize();
            }

            nodes[countNodes] = remain1;
            this.data[countNodes] = dataCurrentNode;
            this.dataSize[countNodes] = dataCurrentNodeSize;
            next[countNodes] = next[currentCompressedTrieNode];
            next[currentCompressedTrieNode] = new int[] {countNodes};
            totDataSize++;


            this.data[currentCompressedTrieNode] = null;
            dataSize[currentCompressedTrieNode] = 0;
            if(d != null) {
              //addDataToNode(currentCompressedTrieNode, data, data.length);
              addDataToNode(currentCompressedTrieNode, d, d[0].length);
            }

            countNodes++;
            return countNodes - 1;
          }
        } else {
          // char1 != null && char2 != null
          // value of current node and value to insert have only one common
          // letter update value of current node with intersection

          final int[][] dataCurrentNode = (int[][])
            this.data[currentCompressedTrieNode];

          final int dataCurrentNodeSize = dataSize[currentCompressedTrieNode];
          this.data[currentCompressedTrieNode] = null;
          dataSize[currentCompressedTrieNode] = 0;

          nodes[currentCompressedTrieNode] = intersection;
          if(!(countNodes < nodes.length - 2)) {
            reSize();
          }

          nodes[countNodes] = remain2;
          if(d != null) {
            //addDataToNode(countNodes, data, data.length);
            addDataToNode(countNodes, d, d[0].length);
          }
          nodes[countNodes + 1] = remain1;
          addDataToNode(countNodes + 1, dataCurrentNode, dataCurrentNodeSize);

          next[countNodes + 1] = next[currentCompressedTrieNode];
          next[currentCompressedTrieNode] = new int[] {countNodes + 1};
          totDataSize++;

          final int insertingPos = getInsertingPosition(
              currentCompressedTrieNode, nodes[countNodes][0]);
          insertNodeInNextArray(currentCompressedTrieNode, countNodes,
              insertingPos);
          countNodes += 2;
          return countNodes - 1;
        }
      }
    }  else {
      // abort recursion
      // no intersection between current node an valuetoinsert
      // System.out.println("4. Fall");

      nodes[countNodes] = valueToInsert;
      if(d !=  null) {
        //addDataToNode(countNodes, data, data.length);
        addDataToNode(countNodes, d, d[0].length);
      }
      final int insertingPos = getInsertingPosition(currentCompressedTrieNode,
          valueToInsert[0]);
      insertNodeInNextArray(currentCompressedTrieNode, countNodes,
          insertingPos);
      countNodes++;
      return countNodes - 1;
    }
  }

  /**
   * Indexes the specified token.
   * 
   * @param token token to be indexex
   * @param id token id
   * @param tokenStart token start
   */
  void index(final byte[] token, final int id, final int tokenStart) {
    int[][] d = new int[2][1];
    d[0][0] = id;
    d[1][0] = tokenStart;

    insertNodeIntoTrie(0, token, d);
  }

  /**
   * Adds data to node.
   *
   * @param currentNode int
   * @param length int    number of places filled in dataToAdd
   * @param dataToAdd int[][]
   */
  private void addDataToNode(final int currentNode, final int[][] dataToAdd,
      final int length) {
    if(dataToAdd == null) {
      return;
    }

    if(data[currentNode] == null) {
      data[currentNode] = dataToAdd;
      dataSize[currentNode] = length;
      totDataSize += length;
      return;
    }

    // free space in data[currentNode] < data to be inserted
    if(((int[][]) data[currentNode])[0].length
        - dataSize[currentNode] < length) {
      // safe available data
      final int[][] dataOld = (int[][]) data[currentNode];

      // duplicate memory
      data[currentNode] = new int[2][
         ((int[][]) data[currentNode])[0].length * 2 + length];

      // add existing data
      System.arraycopy(dataOld[0], 0,
          ((int[][]) data[currentNode])[0], 0, dataSize[currentNode]);
      System.arraycopy(dataOld[1], 0,
          ((int[][]) data[currentNode])[1], 0, dataSize[currentNode]);
    }

    System.arraycopy(dataToAdd[0], 0,
        ((int[][]) data[currentNode])[0], dataSize[currentNode], length);
    System.arraycopy(dataToAdd[1], 0,
        ((int[][]) data[currentNode])[1], dataSize[currentNode], length);

    dataSize[currentNode] += length;
    totDataSize += length;
  }

  /**
   * Reduces array size, used for building trie structure, at realy filled.
   */
  private void finishTrieArrays() {
    final byte[][] tmpNodes = new byte[countNodes][];
    System.arraycopy(nodes, 0, tmpNodes, 0, countNodes);
    nodes = tmpNodes;

    final Object[] tmpObject = new Object[countNodes];
    System.arraycopy(data, 0, tmpObject, 0, countNodes);
    data = tmpObject;

    final int[][] tmpIntA = new int[countNodes][];
    System.arraycopy(next, 0, tmpIntA, 0, countNodes);
    next = tmpIntA;

    /*        int[] tmpInt = new int[countNodes];
        System.arraycopy(dataSize, 0, tmpInt, 0, countNodes);
        dataSize = tmpInt;
     */
  }

  /**
   * Duplicate allocated memory for arrays, used for building trie structure.
   */
  private void reSize() {
    final byte[][] tmpNodes = new byte[countNodes * 2][];
    System.arraycopy(nodes, 0, tmpNodes, 0, countNodes);
    nodes = tmpNodes;

    final Object[] tmpObject = new Object[countNodes * 2];
    System.arraycopy(data, 0, tmpObject, 0, countNodes);
    data = tmpObject;

    final int[][] tmpIntA = new int[countNodes * 2][];
    System.arraycopy(next, 0, tmpIntA, 0, countNodes);
    next = tmpIntA;

    final int[] tmpInt = new int[countNodes * 2];
    System.arraycopy(dataSize, 0, tmpInt, 0, countNodes);
    dataSize = tmpInt;
  }


  /**
   * Save whether a corresponding node was found in method
   * getInsertingPosition.
   */
  private boolean found;

  /**
   * counts number of touched elements.
   */
  private int touchedElements;

  /**
   * counts number of passed times.
   */
  private int times;


  /**
   * Looks up the inserting position in next[][] for valueToInsert. Comparing
   * only the first letter its sufficient because every child has a
   * different first letter.
   *
   * @param currentPosition int
   * @param toInsert byte
   * @return int Position als zeiger auf das nodeArray
   */
  private int getInsertingPosition(final int currentPosition,
      final byte toInsert) {
    times++;
    int i;
    //initTimer();
    //i = getInsertingPositionBinary(currentPosition, toInsert);
    i = getInsertingPositionLinear(currentPosition, toInsert);
    //timer = getTimer();

    return i;
  }

  /**
   * Uses linear search for finding inserting position.
   * returns:
   * 0 if any successor exists, or 0 is inserting position
   * n here to insert
   * n and found = true, if nth item is occupied and here to insert
   *
   * @param currentPosition pointer on next array
   * @param toInsert value to be inserted
   * @return inserting position
   */
  private int getInsertingPositionLinear(final int currentPosition,
      final byte toInsert) {
    // init value
    found = false;

    // node has no successors
    if(next[currentPosition] == null) {
      return 0;
    }

    // load existing successors
    final int[] nextNodes = next[currentPosition];
    // first successors > toInsert
    if(nodes[nextNodes[0]][0] > toInsert) {
      touchedElements++;
      return 0;
    }
    int i = 0;
    for(; i < nextNodes.length; i++) {
      touchedElements++;
      // toInsert already exists; return Id
      if(nodes[nextNodes[i]][0] == toInsert) {
        found = true;
        return  i;
      }
      if(nodes[nextNodes[i]][0] > toInsert) {
        return i;
      }
    }
    // next free space in node[]
    return i;
  }

  /**
   * Set allocated memory for nodes to real.
   * @param node memory optimization for node
   */
  private void finishNode(final int node) {
    // node has no data oder size already fits
    if(data[node] == null
        || dataSize[node] == ((int[][]) data[node])[0].length) {
      return;
    }
    
    //int[][] newData = new int[dataSize[node]][2];
    final int[][] newData = new int[2][dataSize[node]];
    //System.arraycopy(data[node],0,newData,0,dataSize[node]);
    System.arraycopy(((int[][]) data[node])[0], 0, newData[0], 0,
        dataSize[node]);
    System.arraycopy(((int[][]) data[node])[1], 0, newData[1], 0,
        dataSize[node]);
    data[node] = newData;
  }


  /**
   * Lookup for nodes.
   * @param valueSearchNode search nodes value
   * @return int[][] dataResultNode data of result node
   */
  private int[][] getNodeFromTrie(final byte[] valueSearchNode) {
    return getNodeFromTrieRecursive(0, valueSearchNode);
  }

  /**
   * Traverse trie and return found node for searchValue.
   * Returns data from node or null
   *
   * @param currentCompressedTrieNode int
   * @param searchNode search nodes value
   * @return int[][]
   */
  private int[][] getNodeFromTrieRecursive(
      final int currentCompressedTrieNode, final byte[] searchNode) {
    byte[] vvalueSearchNode = searchNode;
    if(currentCompressedTrieNode != 0) {
      int i = 0;
      while(i < vvalueSearchNode.length
          && i < nodes[currentCompressedTrieNode].length
          && nodes[currentCompressedTrieNode][i] == vvalueSearchNode[i]) {
        i++;
      }

      if(nodes[currentCompressedTrieNode].length == i) {
        if(vvalueSearchNode.length == i) {
          // leaf node found with appropriate value
          return (int[][]) data[currentCompressedTrieNode];
        } else {
          // cut valueSearchNode for value current node
          final byte[] tmp = new byte[vvalueSearchNode.length - i];
          for(int j = 0; j < tmp.length; j++) {
            tmp[j] = vvalueSearchNode[i + j];
          }
          vvalueSearchNode = tmp;

          // scan successors currentNode
          final int position = getInsertingPositionLinear(
              currentCompressedTrieNode, vvalueSearchNode[0]);
          if(!found) {
            // node not contained
            //System.out.println("1. Fall nicht gefunden");
            return null;
          } else {
            return getNodeFromTrieRecursive(
                next[currentCompressedTrieNode][position],
                vvalueSearchNode);
          }
        }
      } else {
        // node not contained
        //System.out.println("2. Fall nicht gefunden");
        return null;
      }
    } else {
      // scan successors current node
      final int position = getInsertingPositionLinear(
          currentCompressedTrieNode, vvalueSearchNode[0]);
      if(!found) {
        // node not contained
        //System.out.println("3. Fall nicht gefunden");
        return null;
      } else {
        return getNodeFromTrieRecursive(
            next[currentCompressedTrieNode][position], vvalueSearchNode);
      }
    }
  }


  /**
   * Save number compared chars at wildcard search.
   * counter[0] = total number compared chars
   * counter[1] = number current methodcall (gets initialized before each call)
   */
  private int[] counter;

  /**
   * Traverse trie and return found node for searchValue.
   * Returns last touched node
   *
   * @param currentCompressedTrieNode int
   * @param searchNode int
   * @return id int last touched not
   */
  private int getNodeFromTrieRecursiveWildcard(
      final int currentCompressedTrieNode, final byte[] searchNode) {
    byte[] valueSearchNode = searchNode;
    if(currentCompressedTrieNode != 0) {
      counter[1] += nodes[currentCompressedTrieNode].length;
      int i = 0;
      while(i < valueSearchNode.length
          && i < nodes[currentCompressedTrieNode].length
          &&  nodes[currentCompressedTrieNode][i] == valueSearchNode[i]) {
        i++;
      }

      if(nodes[currentCompressedTrieNode].length == i) {
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
            counter[1] = counter[1]
               - nodes[currentCompressedTrieNode].length + i;

            // System.out.println("1. Fall nicht gefunden");
            return currentCompressedTrieNode;
          } else {
            return getNodeFromTrieRecursiveWildcard(
                next[currentCompressedTrieNode][position], valueSearchNode);
          }
        }
      } else {
        // node not contained
        counter[0] = i;
        counter[1] = counter[1] - nodes[currentCompressedTrieNode].length + i;

        //System.out.println("2. Fall nicht gefunden");
        return currentCompressedTrieNode;
      }
    } else {
      // scan successors current node
      final int position = getInsertingPositionLinear(
          currentCompressedTrieNode, valueSearchNode[0]);
      if(!found) {
        // node not contained
        //System.out.println("3. Fall nicht gefunden");
        counter[0] = -1;
        counter[1] = -1;

        return -1;
      } else {
        return getNodeFromTrieRecursiveWildcard(
            next[currentCompressedTrieNode][position], valueSearchNode);
      }
    }
  }

  /**
   * Reduces used memory for trie to real needed.
   */
  void finish() {
    finish(0);
    dataSize = null;
    //System.out.println("trie created with " + countNodes + " nodes");
    //System.out.println("trie has " + totDataSize + " data values");
  }

  /**
   * Reduces used memory for trie to real needed. - recursive method
   * @param currentCompressedTrieNode  id start node
   */
  private void finish(final int currentCompressedTrieNode) {
    // leaf node
    if(currentCompressedTrieNode !=  0
        && next[currentCompressedTrieNode] == null) {
      finishNode(currentCompressedTrieNode);
    } else {
      // root node; finish arrays building the trie structure
      if(currentCompressedTrieNode == 0) {
        finishTrieArrays();
        finishNode(currentCompressedTrieNode);
      }

      // not root, (non) leaf node
      if(currentCompressedTrieNode != 0
          && data[currentCompressedTrieNode] != null) {
        finishNode(currentCompressedTrieNode);
      }

      // preorder traversal for all successors
      if(next[currentCompressedTrieNode] != null) {
        for(int i = 0; i < next[currentCompressedTrieNode].length; i++) {
          finish(next[currentCompressedTrieNode][i]);
        }
      }
    }
  }

  /**
   * Calculates the intersection.
   * @param b1 input array one
   * @param b2 input array two
   * @return intersection of b1 and b2
   */
  private byte[] calculateIntersection(final byte[] b1, final byte[] b2) {
    int i = 0;
    // Calculate the intersection position.
    if(b1 != null && b2 != null) {
      final int minLength = Math.min(b1.length, b2.length);

      for(; i < minLength; i++) {
        if(b1[i] != b2[i]) {
          return getBytes(b1, 0, i);
        }
      }
      return getBytes(b1, 0, i);
    }

    // The char arrays have no intersection.
    return null;
  }

  /**
   * Extract all data from start - to end position out of data.
   * @param d byte[]
   * @param startPos int
   * @param endPos int
   * @return data byte[]
   */
  private byte[] getBytes(final byte[] d, final int startPos,
      final int endPos) {
    if(d == null || d.length < endPos || startPos < 0
        || startPos == endPos) {
      return null;
    }

    final byte[] newByte = new byte[endPos - startPos];

    System.arraycopy(d, startPos, newByte, 0, newByte.length);
    return newByte;
  }


  /**
   * Method for wildcards search in trie.
   * getNodeFromTrieWithWildCard(char[] valueSearchNode, int pos) is called
   * getNodeFromTrieWithWildCard(FTIndexCTNode currentCompressedTrieNode,
   * char[] valueSearchNode, int posWildcard)
   * executes the wildcard search and works on trie via
   * getNodeFromTrieRecursiveWildcard(FTIndexCTNode currentCompressedTrieNode,
   * byte[] valueSearchNode)
   * calls
   *
   * @param valueSearchNode search nodes value
   * @param pos position of wildcard in search nodes value
   * @return data int[][]
   */
  private int[][] getNodeFromTrieWithWildCard(final byte[] valueSearchNode,
      final int pos) {
    // init counter
    counter = new int[2];
    return getNodeFromTrieWithWildCard(0, valueSearchNode, pos);
  }

  /**
   * Support different wildcard operators: ., .+, .* and .?
   * PosWildCard points on bytes[], at position, where .  is situated
   *
   * @param currentCompressedTrieNode current node
   * @param valueSearchNode search nodes value
   * @param posWildcard position of wildcard in search nodes value
   * @return data
   */
  private int[][] getNodeFromTrieWithWildCard(
      final int currentCompressedTrieNode,
      final byte[] valueSearchNode, final int posWildcard) {
    byte[] afterWildcard = null;
    byte[] beforWildcard = null;
    final int currentLength = 0;
    int resultNode;
    int[][] d = null;
    // wildcard not at beginning
    if(posWildcard > 0) {
      // copy part before wildcard
      beforWildcard = new byte[posWildcard];
      System.arraycopy(valueSearchNode, 0, beforWildcard, 0, posWildcard);
      resultNode = getNodeFromTrieRecursiveWildcard(
          currentCompressedTrieNode, beforWildcard);
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
      if(beforWildcard != null) {
        System.arraycopy(beforWildcard, 0, searchChar, 0, beforWildcard.length);
      }
      // copy part after wildcard
      if(beforWildcard == null) {
        System.arraycopy(valueSearchNode, posWildcard + 2, searchChar, 0,
            searchChar.length);
      } else {
        System.arraycopy(valueSearchNode, posWildcard + 2, searchChar,
            beforWildcard.length, searchChar.length - beforWildcard.length);
      }

      d = getNodeFromTrie(searchChar);

      // lookup in trie with . as wildcard
      searchChar = new byte[valueSearchNode.length - 1];
      if(beforWildcard != null) {
        // copy unprocessed part before wildcard
        System.arraycopy(beforWildcard, 0, searchChar, 0, beforWildcard.length);
        searchChar[beforWildcard.length] = '.';

        // copy part after wildcard
        System.arraycopy(valueSearchNode, posWildcard + 2, searchChar,
            beforWildcard.length + 1,
            searchChar.length - beforWildcard.length - 1);
      } else {
        // copy unprocessed part before wildcard
        searchChar[0] = '.';

        // copy part after wildcard
        System.arraycopy(valueSearchNode, posWildcard + 2, searchChar, 1,
            searchChar.length - 1);
      }

      // attach both result
      d = ftOR(d, getNodeFromTrieWithWildCard(0, searchChar,
          posWildcard));
      return d;
    } else if(wildcard == '*') {
      // append 0 or n symbols

      // valueSearchNode == .*
      if(!(posWildcard == 0 && valueSearchNode.length == 2)) {
        // lookup in trie without wildcard
        final byte[] searchChar = new byte[
           valueSearchNode.length - 2 - currentLength];
        // copy unprocessed part before wildcard
        if(beforWildcard != null) {
          System.arraycopy(beforWildcard, 0, searchChar, 0,
              beforWildcard.length);
        }
        // copy part after wildcard
        if(beforWildcard == null) {
          afterWildcard = new byte[searchChar.length];
          System.arraycopy(valueSearchNode, posWildcard + 2, searchChar, 0,
              searchChar.length);
          System.arraycopy(valueSearchNode, posWildcard + 2, afterWildcard, 0,
              searchChar.length);
        } else {
          afterWildcard = new byte[searchChar.length - beforWildcard.length];
          System.arraycopy(valueSearchNode, posWildcard + 2, searchChar,
              beforWildcard.length, searchChar.length - beforWildcard.length);
          System.arraycopy(valueSearchNode, posWildcard + 2, afterWildcard,
              0, searchChar.length - beforWildcard.length);
        }

        d = getNodeFromTrie(searchChar);

        // all chars from valueSearchNode are contained in trie
        if(beforWildcard != null && counter[1] != beforWildcard.length) {
          return d;
        }
      }

      // delete data
      astericsWildCardData = null;
      astericsWildCardTraversing(resultNode, afterWildcard,
          false, counter[0], 0);
      return ftOR(d, astericsWildCardData);
    } else if(wildcard == '+') {
      // append 1 or more symbols

      // lookup in trie with . as wildcard
      final byte[] searchChar = new byte[valueSearchNode.length - 1 -
                                         currentLength];
      // copy unprocessed part before wildcard
      if(beforWildcard != null) {
        System.arraycopy(beforWildcard, 0, searchChar, 0, beforWildcard.length);
      }
      // set . as wildcard
      searchChar[posWildcard] = '.';

      // copy part after wildcard
      if(beforWildcard == null) {
        // valueSearchNode == .+
        if(!(posWildcard == 0 && valueSearchNode.length == 2)) {
          afterWildcard = new byte[searchChar.length];
          System.arraycopy(valueSearchNode, posWildcard + 2, searchChar, 1,
              searchChar.length);
          System.arraycopy(valueSearchNode, posWildcard + 2, afterWildcard, 1,
              searchChar.length);
        }
      } else {
        afterWildcard = new byte[searchChar.length - beforWildcard.length - 1];
        System.arraycopy(valueSearchNode, posWildcard + 2, searchChar,
            beforWildcard.length + 1,
            searchChar.length - beforWildcard.length - 1);
        System.arraycopy(valueSearchNode, posWildcard + 2, afterWildcard,
            0, searchChar.length - beforWildcard.length - 1);
      }

      d = getNodeFromTrieWithWildCard(searchChar, posWildcard);
      // all chars from valueSearchNode contained in Trie
      if(beforWildcard != null && counter[1] != beforWildcard.length) {
        return d;
      }

      // delete data
      astericsWildCardData = null;

      astericsWildCardTraversing(resultNode, afterWildcard, false,
          counter[0], 0);
      //astericsWildCardTraversing(resultNode, afterWildcard, false, 0, 0);
      return ftOR(d, astericsWildCardData);
    } else if(wildcard == '{') {
      final int from = Character.getNumericValue(
          (char) valueSearchNode[posWildcard + 2]);
      //int to = Character.getNumericValue ((char)
      //valueSearchNode[posWildcard+4]);
      byte[] dots;
      if(from == 0) {
        dots = new byte[1];
        dots[0] = '?';
      } else {
        dots = new byte[from - 1];
        if(from > 1) {
          dots[0] = '.';
        }
      }

      for(int i = 1; i < dots.length; i++) {
        dots[i] = '.';
      }

      final byte[] newSearchValue = new byte[
         posWildcard + valueSearchNode.length - posWildcard - 5 + dots.length];
      System.arraycopy(valueSearchNode, 0, newSearchValue, 0, posWildcard + 1);
      System.arraycopy(dots, 0, newSearchValue, posWildcard + 1, dots.length);
      System.arraycopy(valueSearchNode, posWildcard + 6, newSearchValue,
          dots.length + posWildcard + 1,
          valueSearchNode.length - posWildcard - 6);
      //System.out.println("new String=" + Token.toString(newSearchValue));
    } else {
      // append 1 symbols
      // not completely processed (value current node)
      if(nodes[resultNode].length > counter[0]) {
        // replace wildcard with value from currentCompressedTrieNode
        valueSearchNode[posWildcard] = nodes[resultNode][counter[0]];

        // . wildcards left
        if(valueSearchNode[posWildcard + 1] == '.') {
//        return getNodeFromTrieWithWildCard(valueSearchNode, posWildcard+1);
        } else  {
          return getNodeFromTrie(valueSearchNode);
        }
      } else if(nodes[resultNode].length == counter[0]) {
        // all chars from nodes[resultNode] are computed

        // any next values existing
        if(next[resultNode] == null) {
          return null;
        }

        int[][] tmpNode = null;
        afterWildcard = new byte[valueSearchNode.length - posWildcard];
        System.arraycopy(valueSearchNode, posWildcard + 1, afterWildcard,
            1, afterWildcard.length - 1);

        for(int i = 0; i < next[resultNode].length; i++) {
          // replace first letter
          afterWildcard[0] = nodes[next[resultNode][i]][0];

          // . wildcards left
          if(afterWildcard.length > 1 && afterWildcard[1] == '.') {
               tmpNode = ftOR(tmpNode,
                   getNodeFromTrieRecursive(next[resultNode][i],
                       afterWildcard));
             }
        }

        return tmpNode;
      }
    }
    return null;
  }


  /** saves astericsWildCardTraversing result
     has to be reinitialized each time (before calling method). */
  private int[][] astericsWildCardData;

  /** counts number of chars skip per astericsWildCardTraversing. */
  private int countSkippedChars;

  /**
   * Looking up node with value, which match ending.
   * The parameter lastFound shows, whether chars were found in last
   * recursive call, which correspond to the ending, consequently
   * those chars are considered, which occur successive in ending.
   * pointerNode shows the position comparison between value[nodeId] and
   * ending starts
   * pointerEnding shows the position comparison between ending and
   * value[nodeId] starts
   *
   * @param node start node
   * @param lastFound boolean
   * @param pointerNode pointer at current node
   * @param pointerEnding pointer at ending
   * @param ending ending value
   */

  private void astericsWildCardTraversing(final int node, final byte[] ending,
      final boolean lastFound, final int pointerNode, final int pointerEnding) {
    int j = pointerEnding;
    int i = pointerNode;
    boolean last = lastFound;

    // wildcard at the end
    if(ending == null || ending.length == 0) {
      // save data current node
      //TODO Umstellen auf FTOR
      astericsWildCardData = ftOROld(astericsWildCardData,
          (int[][]) data[node]);

      if(next[node] != null) {
        // preorder traversal through trie
        for(int k = 0; k < next[node].length; k++) {
          astericsWildCardTraversing(next[node][k], ending, last, 0, 0);
        }
      }
      return;
    }

    // compare chars current node and ending
    if(nodes[node] != null) {

      // skip all unlike chars, if any suitable was found
      while(!last && i < nodes[node].length
          && nodes[node][i] != ending[j]) {
        i++;
      }

      // skip all chars, equal to first char
      //          +1
      while(i + ending.length < nodes[node].length
          && nodes[node][i + 1] == ending[0]) {
        i++;
      }


      countSkippedChars = countSkippedChars + i - pointerNode;

      //System.out.println("i="+i);
      //       System.out.println("count=" + countSkippedChars);
      //count same succeeding chars
      while(i < nodes[node].length && j < ending.length
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

    /* not processed all chars from node, but all chars from ending
     * were processed or root */
    if(node == 0 || j == ending.length && i < nodes[node].length) {
      // pointer = 0; restart search
      if(next[node] == null) {
        countSkippedChars = 0;
        return;
      }

      // preorder search in trie
      for(int k = 0; k < next[node].length; k++) {
        astericsWildCardTraversing(next[node][k], ending, false, 0, 0);
      }
      countSkippedChars = 0;
      return;
    } else if(j == ending.length && i == nodes[node].length) {
      // all chars form node and all chars from ending done
      // ToDO added
      if(data[node] != null) {
        //ToDo Umstellen auf FTOR
        astericsWildCardData = ftOROld(astericsWildCardData,
            (int[][]) data[node]);
      }

      countSkippedChars = 0;

      if(next[node] != null) {
        // preorder search in trie
        for(int k = 0; k < next[node].length; k++) {
          // compare only first char from ending
          if(j == 1) {
            //astericsWildCardTraversing(next[node][k], ending, lastFound,0,0);
            astericsWildCardTraversing(next[node][k], ending, false, 0, 0);
          }
          astericsWildCardTraversing(next[node][k], ending, last, 0, j);
        }
      }
      return;


      /* astericsWildCardData = FTLibrary.FTOR(astericsWildCardData,
       *    (int[][]) data[node]);
         countSkippedChars = 0;
         return;
       */
    } else if(j < ending.length && i < nodes[node].length) {
      //System.out.println("ending<j und value<i");
      // still chars from node and still chars from ending left, pointer = 0 and
      // restart searching
      if(next[node] == null) {
        countSkippedChars = 0;
        return;
      }

      // restart searching at node, but value-position i
      astericsWildCardTraversing(node, ending, false, i, 0);
      return;

    } else if(j < ending.length &&  i == nodes[node].length) {
      // all chars form node processed, but not all chars from processed

      // move pointer and go on
      if(next[node] == null) {
        countSkippedChars = 0;
        return;
      }

      // preorder search in trie
      for(int k = 0; k < next[node].length; k++) {
        // compare only first char from ending
        if(j == 1) {
          astericsWildCardTraversing(next[node][k], ending, last, 0, 0);
        }

        astericsWildCardTraversing(next[node][k], ending, last, 0, j);
      }
    }
  }

  /**
   * Builds an or-conjunction of values1 and values2.
   *
   * @param values1 input set
   * @param values2 input set
   * @return union set
   */
  private static int[][] ftOR(final int[][] values1, final int[][] values2) {
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

    // run variable for values1
    int i = 0;
    // run variable for values2
    int k = 0;
    // count inserted elements
    int counter = 0;

    int cmpResult;
    // process smaller set
    while(val1[0].length > i) {
      if(k >= val2[0].length) {
        break;
      }
      cmpResult = Array.compareIntArrayEntry(val1[0][i],
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

    if(counter == 0) return null;

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
   * Builds an or conjunction of vlaues1 and values2.
   * @param values1 input set
   * @param values2 input set
   * @return union set
   */
  private static int[][] ftOROld(final int[][] values1, final int[][] values2) {
    int[][] val1 = values1;
    int[][] val2 = values2;

    if(val1 == null) {
      return val2;
    } else if(val2 == null) {
      return val1;
    }

    final int[][] maxResult = new int[val1.length + val2.length][2];

    // calculate maximum
    final int max = Math.max(val1.length, val2.length);
    if(max == val1.length) {
      final int[][] tmp = val1;
      val1 = val2;
      val2 = tmp;
    }

    // run variable for values1
    int i = 0;
    // run variable for values2
    int k = 0;
    // count inserted elements
    int counter = 0;

    int cmpResult;
    // process smaller set
    while(i < val1.length && k < val2.length) {
      cmpResult = Array.compareIntArrayEntry(val1[i][0],
          val1[i][1], val2[k][0], val2[k][1]);
      if(cmpResult == 1 || cmpResult == 2) {
        // same Id, pos0 < pos1 oder id0 < id1
        maxResult[counter] = val2[k];
        k++;
      } else if(cmpResult == -1 || cmpResult == -2) {
        // same Id, pos0 > pos1 oder id0 > id1
        maxResult[counter] = val1[i];
        i++;
        //k++;
      } else {
        // ids and pos identical
        maxResult[counter] = val1[i];
        i++;
        k++;
      }
      counter++;
    }

    if(counter == 0) return null;

    int[][] returnArray;

    // all elements form values2 are processed
    if(k == val2.length && i < val1.length) {
      returnArray = new int[counter + val1.length - i][2];
      // copy left values (bigger than last element values2) from values1
      System.arraycopy(val1, i, returnArray, counter, val1.length - i);
    } else {
      // all elements form values1 are processed
      returnArray = new int[counter + val2.length - k][2];
      // copy left values (bigger than last element values1) from values2
      System.arraycopy(val2, k, returnArray, counter, val2.length - k);
    }

    System.arraycopy(maxResult, 0, returnArray, 0, counter);
    return returnArray;
  }


  /**
   *  Is called from BaseXData and saves content values in the compressedTrie.
   *
   * @param content content of tag
   * @param id id of node
   *
   * TODO <SG> THIS METHOD SEEMS TO BE NOT NEEDED ANY MORE???</SG>
  private void index(final byte[] content, final int id) {
    for(int i = 0; i < content.length; i++) {
      // count read bytes
      int count = 0;

      // count number of fulltext characters
      while(Token.ftChar(content[i])) {
        count++;
        if(count + i == content.length) break;
      }

      if(count > 0) {
        // counts number used for one word
        final byte[] byteValue = new byte[count];
        for(int c = 0; c < count; c++) {
          byteValue[c] = (byte) Token.ftNorm(content[i + c]);
        }

        final int[][] d = new int[2][1];
        d[0][0] = id;
        d[1][0] = i;
        System.out.println("id:" + id);
        System.out.println("pos:" + i);
        insertNodeIntoTrie(0, byteValue, d);
      }
      i = i + count;
    }
  }
   */

  /**
   * Used for indenting the output on console. For every level new level,
   * \t is appended
  private final StringBuilder tab = new StringBuilder("\t");
   */


  /**
   * Prints the content of current compressedtrie on console.
   * * denotes a leaf node
   * (n) denotes the number of successors at next level (#children)
   *
   * @param currentCompressedTrieNode start note for printing
  private void printTrie(final int currentCompressedTrieNode) {
    String output;

    // non leaf node
    if(currentCompressedTrieNode !=  0
        && next[currentCompressedTrieNode] == null) {
      output = tab + "->" + Token.string(nodes[currentCompressedTrieNode]);
      // print data
      if(data[currentCompressedTrieNode] != null) {
        output = tab + output + "*";
        output = output
          + intArrayToString((int[][]) data[currentCompressedTrieNode]);
      }
      System.out.println(output);
    } else {
      // leaf node

      // root node
      if(currentCompressedTrieNode == 0) {
        // root
        BaseX.out("root("
            + next[currentCompressedTrieNode].length + ")");
      } else {
        // print data from leaf node
        if(data[currentCompressedTrieNode] != null) {
          output = tab + "->" + Token.string(
              nodes[currentCompressedTrieNode])
            + "*(" + next[currentCompressedTrieNode].length + ")";
          output = output + intArrayToString(
              (int[][]) data[currentCompressedTrieNode]);
        } else {
          output = tab + "->" + Token.string(
              nodes[currentCompressedTrieNode])
            + "(" + next[currentCompressedTrieNode].length + ")";
        }
        BaseX.out(output);
      }

      // preorder walk for all current node successors
      for(int i = 0; i < next[currentCompressedTrieNode].length; i++) {
        System.out.println("");
        tab.append("\t");
        printTrie(next[currentCompressedTrieNode][i]);
        tab.deleteCharAt(tab.length() - 1);
      }
    }
  }
   */

  /**
   * Prints memory info on sysout.
  private void getMemoryInfo() {
    System.out.println("*** MemoryInfo ***");
    System.out.println("count nodes=" + countNodes);
    int size = 0;
    int totalSize = 0;
    for(int i = 1; i < nodes.length; i++) {
      //System.out.println(Token.toString(nodes[i]));
      size += nodes[i].length;
    }
    System.out.println("count byte for nodevalues=" + size);

    totalSize += size;
    size = 0;
    for(int i = 1; i < data.length; i++) {
      if(data[i] != null) {
        size += ((int[][]) data[i]).length * 2;
      }
    }
    System.out.println("object[" + data.length + "]");
    System.out.println("count int for data=" + size);
    System.out.println("about used bytes=" + size * 4);

    totalSize += size * 4;
    size = 0;
    int empty = 0;
    for(final int[] n : next) {
      if(n != null) {
        size += n.length;
        for(final int n2 : n) {
          if(next.length < n2) {
            empty++;
          }
        }
      }
    }
    System.out.println("int [" + next.length + "][*]");
    System.out.println("count int for next=" + size);
    System.out.println("used byte=" + size * 4);
    System.out.println("touched Elements=" + touchedElements);
    System.out.println("times=" + times);
    System.out.println("avg=" + touchedElements / times);
    System.out.println("avg. number next=" + empty / countNodes);

    totalSize += size * 4;
    System.out.println("used bytes for dataSize=" + dataSize.length * 4);

    totalSize += data.length * 4;

    System.out.println("total Bytes used for triestructure="
        + (totalSize / 1024) + "kB");
  }
   */


  /**
   * Uses binary search for finding inserting position returns.
   * 0 if any successor exists, or 0 is inserting position
   * n here to insert
   * n and found = true, if nth item is occupied and here to insert
   *
   * @param currentPosition pointer on current position at next array
   * @param toInsert value to insert
   * @return inserting position
  private int getInsertingPositionBinary(final int currentPosition,
      final byte toInsert) {
    found = false;
    // node has no successors
    if(next[currentPosition] == null) {
      touchedElements++;
      return 0;
    }

    // load existing successors
    final int[] nextNodes = next[currentPosition];

    // array contains 1 node
    if(nextNodes.length == 1) {
      touchedElements++;
      if(nodes[nextNodes[0]][0] == toInsert) {
        found = true;
        return 0;
      } else if(nodes[nextNodes[0]][0] < toInsert) {
        return 1;
      } else {
        return 0;
      }
    }

    int pivot = (nextNodes.length) / 2;

    // trivial case, char toInsert < first element
    if(nodes[nextNodes[0]][0] > toInsert) {
      touchedElements++;
      return 0;
    } else if(nodes[nextNodes[nextNodes.length - 1]][0] < toInsert) {
      // trivial case, char toInset > last element
      touchedElements++;
      return nextNodes.length;
    } else {
      // find appropriate place to insert
      if((nextNodes.length) % 2 != 0) {
        pivot = pivot  + 1;
      }
      int leftBound = 0;
      int rightBound = nextNodes.length - 1;

      // binary search
      while(leftBound < rightBound && pivot <= rightBound) {
        touchedElements++;
        if(nodes[nextNodes[pivot]][0] > toInsert) {
          // table at pivot > char to add
          rightBound = pivot;
          pivot = pivot - (rightBound - leftBound) / 2;

          if(rightBound - leftBound % 2 != 0) {
            pivot = pivot  - 1;
          }
        } else if(nodes[nextNodes[pivot]][0] < toInsert) {
          // table at pivot < char to add
          leftBound = pivot;
          pivot = pivot + (rightBound - leftBound) / 2;
          if((rightBound - leftBound) % 2 != 0) {
            pivot = pivot  + 1;
          }
        } else {
          // element already exists in table
          found = true;
          return pivot;
        }

        if(pivot == rightBound) {
          break;
        }
      }
    }

    if(nodes[nextNodes[pivot]][0] == toInsert) {
      found = true;
    }
    return pivot;
  }
   */

  /**
   * Returns all ids, saved in the trie, less those out of resultFTContent.
   *
   * @param resultFTContent int[][] with pre and pos values
   * @return resultFTContent Ids
  private int[][] getAllIDPos(final int[][] resultFTContent) {
    int[][] maxResult = null;
    int[][] tmpResult;
    int k = 1;
    while(k < data.length) {
      tmpResult = ftUnaryNot((int[][]) data[k], resultFTContent);
      maxResult = ftOR(maxResult, tmpResult);
      k++;
    }

    return maxResult;
  }
  */

  /**
   * Prints input arrays content and c on console.
   * @param input input string
  void intArrayToStringX(final int[][] input) {
    if(input == null) {
      System.out.println("empty");
      return;
    }

    for(int i = 0; i < input.length; i++) {
      if(input[i] != null) {
        BaseX.out(i + ":" + Token.string(nodes[input[i][0]]));
        for(int j = 1; j < input[i].length; j++) {
          BaseX.out("," + Token.string(nodes[input[i][j]]));
        }
        System.out.println("");
      }
    }
  }
   */

  /**
   * Returns index statistics.
   * @return statistics string
  private String info() {
    double count1 = 0;
    double count2 = 0;
    int max = 0;
    for(int i = 0; i < data.length; i++) {
      if(data[i] == null || ((int[][]) data[i])[0].length == 0) {
        count1++;
      }
      if(next[i] == null ||  next[i].length == 0) {
        count2++;
      }
      if(max < nodes[i].length) {
        max = nodes[i].length;
      }
    }

    System.out.println("dateempty:" + count1 / nodes.length);
    System.out.println("nextempty:" + count2 / nodes.length);
    System.out.println("maxlength:" + max);
    return "Number nodes=" + nodes.length
    + "\n number index entries=" + countNodes;
  }
   */


  /**
   * Returns the number of index entries.
   * @return number of index entries
  private int size() {
    return countNodes;
  }
   */


  // TODO integrieren bzw. rausnehmen
  /**
   * Extracts words from the specified byte array and returns its ids.
   * @param tok token to be extracted and indexed
   * @return ids
   */
  /*    private int[] getIDs(byte[] tok) {
       int[] data = getIDsFromData(ftLibrary.fulltext(Token.toString(tok)));
       if(data == null) {
           return new int[0];
       }

       return data;
    }
   */
  /**
   * Extracts words from the specified byte array and returns its ids and pos.
   * @param tok token to be extracted and indexed
   * @return ids
  private int[][] getIDPos(final byte[] tok) {
    return getNodeFromTrie(tok);
  }
   */

  /**
   * Extracts words from the specified byte array and returns its ids and
   * positions. The use of wildcard in tok at position posWildCard is supplied.
   *
   * @param tok token with wildcard
   * @param posWildCard wildcard has position in token
   * @return ids and pos
  private int[][] getIDWithWildcard(final byte[] tok, final int posWildCard) {
    return getNodeFromTrieWithWildCard(tok, posWildCard);
  }
   */
  
  /**
   * Create a random filled trie.
   * @param maxWordLength maximum length of words
   * @param numberWords number of words
  private void createRandomFilledTrie(final int maxWordLength,
      final int numberWords) {
    final Random r = new Random();

    //int randomNextLength;
    int randomWordLength;
    byte[] word;
    for(int i = 0; i < numberWords; i++) {
      //randomNextLength = r.nextInt(maxNextLength);
      // Wortlaenge muss groesser 0 sein
      randomWordLength = r.nextInt(maxWordLength - 1) + 1;
      word = new byte[randomWordLength];
      for(int j = 0; j < randomWordLength; j++) {
        // 97-122 a-z
        word[j] = (byte) (r.nextInt(25) +  97);
      }
      System.out.println(Token.string(word));
      insertNodeIntoTrie(0, word, null);
    }

    //finish(0);
    //getMemoryInfo();
  }
   */

  /**
   * Extracts ids out of data array.
   * @param data data
   * @return ids int[]
  private static int[] getIDsFromData(final int[][] data) {
    //if (data == null || data.length == 0) {
    if(data == null || data[0].length == 0) {
      return null;
    }

    //int[] maxResult = new int[data.length];
    final int[] maxResult = new int[data[0].length];
    int counter = 1;
    maxResult[0] = data[0][0];
    //for (int i=1; i<data.length; i++) {
    for(int i = 1; i < data[0].length; i++) {
      /*            if(maxResult[counter-1] != data[i][0]){
                maxResult[counter] = data[i][0];
                counter++;
            }

      if(maxResult[counter - 1] != data[0][i]) {
        maxResult[counter] = data[0][i];
        counter++;
      }

    }

    final int[] result = new int[counter];
    System.arraycopy(maxResult, 0, result, 0, counter);
    return result;
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

  /**
   * Each result wordA, is not allowed to be contained in result wordB.
   * Each result for wordA, that is not contained in result set wordB,
   * is added to returned result set
   *
   * @param resultWordA result allowed to be contained
   * @param resultWordB result not allowed to be contained
   * @return data []
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
   */
}
