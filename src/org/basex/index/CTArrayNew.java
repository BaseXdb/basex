package org.basex.index;

import org.basex.util.ByteArrayList;
import org.basex.util.IntArrayList;

/**
 * Preserves a compressed trie index structure and useful functionality.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Sebastian Gath
 */
public final class CTArrayNew {
  /** counts all nodes in the trie. **/
  public int count;
  /** Flag for finishing the trie. **/
  private boolean finished;
 
  /** THE LISTS ARE USED TO CREATE THE STRUCTURE. **/
  /** List saving the token values. **/
  private ByteArrayList tokensL;
  /** List saving the structure: [t, n1, ..., nk, s, p] 
   * t = pointer on tokens; n1, ..., nk are the children of the node
   * saved as pointer on nextN; s = size of pre values; p = pointer 
   * on pre/pos where the data is stored. t, s, p are saved for every node. */
  private IntArrayList nextL;
  /** List with pre values. */
  private IntArrayList preL;
  /** List with pos values. */
  private IntArrayList posL;
  /** THE ARRAYS ARE USED TO KEEP THE STRUCTURE IN MAINMEMORY. **/
  /** Indexed tokens. */
  public byte[][] tokens;
  /** Trie structure. */
  public int[][] next;
  /** FTData: Pre values. */
  public int[][] pre;
  /** FTData: Position values. */
  public int[][] pos;
  /** Flag for bulkload option. */
  public boolean bl = true;
 
  /**
   * Constructor.
   */
  public CTArrayNew() {
    nextL = new IntArrayList();
    preL = new IntArrayList();
    posL = new IntArrayList();
    tokensL = new ByteArrayList();
    // add root node with k, t, s
    nextL.add(new int[]{-1, 0, 0});
    count = 1;
    finished = false;
  }
  
  /**
   * Finish the structure arrays.
   */
  public void finish() {
    finished = true;
    tokens = tokensL.finish();
    tokensL = null;
    next = nextL.finish();
    nextL = null;
    pre = preL.finish();
    preL = null;
    pos = posL.finish();
    posL = null;   
  }
  
  /**
   * Finish the structure arrays.
   * tokens and next
   */
  public void finishTN() {
    tokens = tokensL.finish();
    tokensL = null;
    next = nextL.finish();
    nextL = null;
  }
  
  /**
   * Finish the structure arrays.
   * pre and pos
   */
  public void finishPP() {
    finished = true;
    pre = preL.finish();
    preL = null;
    pos = posL.finish();
    posL = null;   
  }
  

  /**
   * Inserts a node in the next array.
   *
   * @param cn int current node
   * @param ti int id to insert
   * @param ip int position where to insert ti
   * @return Id on next[currentNode]
   */
  private int insertNodeInNextArray(final int cn, final int ti, final int ip) {
    int[] tmp = new int[nextL.list[cn].length + 1];
    System.arraycopy(nextL.list[cn], 0, tmp, 0, ip);
    // insert node
    tmp[ip] = ti;
    // copy remain
    System.arraycopy(nextL.list[cn], ip, tmp, ip + 1, tmp.length - ip - 1);
    nextL.list[cn] = tmp;
    return ip;
  }


  /**
   * Inserts a node into the trie.
   *
   * @param cn current node, which gets a new node
   * appended; start with root (0)
   * @param v value, which is to be inserted
   * @param d int[][] data, which are to be added at new node
   * @return nodeId, parent node of new node
   */
  private int insertNodeIntoTrie(final int cn, final byte[] v, 
      final int[][] d) {
    // currentNode is root node
    if(cn == 0) {
      // root has successors
      if(nextL.list[cn].length > 3) {
        final int p = getInsertingPosition(cn, v[0]);
        if(!found) {
          // any child has an appropriate value to valueToInsert;
          // create new node and append it; save data
          
          int[] e;
          e = new int[3];
          e[0] = tokensL.size;
          tokensL.add(v);
          if (d == null) {
            e[1] = 0;
            e[2] = 0;
          } else {
            e[1] = d[0].length;
            e[2] = addDataToNode(0, d, 0);            
          }
          
          nextL.add(e);
          insertNodeInNextArray(cn, nextL.size - 1, p);
          //System.out.println("fall1");
          return nextL.size - 1;
        } else {
          return insertNodeIntoTrie(nextL.list[cn][p], v, d);
        }
      }
    }

    final byte[] is = (nextL.list[cn][0] == -1) ? 
        null : calculateIntersection(tokensL.list[nextL.list[cn][0]], v);
    byte[] r1 = (nextL.list[cn][0] == -1) ? 
        null : tokensL.list[nextL.list[cn][0]]; 
    byte[] r2 = v;

    if(is != null) {
      r1 = getBytes(r1, is.length, r1.length);
      r2 = getBytes(v, is.length, v.length);
    }

    if(is !=  null) {
      if(r1 == null) {
        if(r2 == null) {
          // char1 == null && char2 == null
          // same value has to be inserted; add data at current node
          if(d != null) {
            if (nextL.list[cn][nextL.list[cn].length - 2] == 0) {
              nextL.list[cn][nextL.list[cn].length - 1] =
                addDataToNode(nextL.list[cn][nextL.list[cn].length - 2], d, 
                    nextL.list[cn][nextL.list[cn].length - 1]);
              nextL.list[cn][nextL.list[cn].length - 2] = d[0].length;
            } else {
              addDataToNode(nextL.list[cn][nextL.list[cn].length - 2], d, 
                  nextL.list[cn][nextL.list[cn].length - 1]);
              nextL.list[cn][nextL.list[cn].length - 2] += d[0].length;
            }
          }
          //System.out.println("fall2");
          return cn;
        } else {
          // char1 == null && char2 != null
          // value of currentNode equals valueToInsert,
          //but valueToInset is longer
          final int posti = getInsertingPosition(cn, r2[0]);
          if(!found) {
            // create new node and append it, because any child from curretnNode
            // start with the same letter than reamin2.
            int[] e;
            e = new int[3];
            e[0] = tokensL.size;
            tokensL.add(r2);
            if (d == null) {
              e[1] = 0;
              e[2] = 0;
            } else {
              e[1] = d[0].length;
              e[2] = addDataToNode(0, d, 0);            
            }
            nextL.add(e);
            insertNodeInNextArray(cn, nextL.size - 1, posti);
            //System.out.println("fall3");
            return nextL.size - 1;
          } else {
            return insertNodeIntoTrie(nextL.list[cn][posti], r2, d);
          }
        }
      
      } else {
        if(r2 == null) {
          // char1 != null &&  char2 == null
          // value of currentNode equals valuteToInsert,
          // but current has a longer value
          // update value of currentNode.value with intersection
          int[] oe = new int[4];
          tokensL.list[nextL.list[cn][0]] = is;
          oe[0] = nextL.list[cn][0];
          int did = nextL.list[cn][nextL.list[cn].length - 1];
          if(d != null) {
            oe[3] = addDataToNode(0, d, 0);
            oe[2] = d[0].length;
          } else {
            oe[3] = 0;
            oe[2] = 0;
          }
          // next data from currentNode.next update
          int[] ne = new int[nextL.list[cn].length];
          System.arraycopy(nextL.list[cn], 0, ne, 0, ne.length);
          ne[0] = tokensL.size; 
          tokensL.add(r1);
          ne[ne.length - 1] = did;
          ne[ne.length - 2] = preL.list[did].length;
          
          //next[countNodes] = next[cn];
          nextL.add(ne);
          oe[1] = nextL.size - 1;
          nextL.list[cn] = oe;
          //System.out.println("fall4");
          return nextL.size - 1;
        } else {
          // char1 != null && char2 != null
          // value of current node and value to insert have only one common
          // letter update value of current node with intersection
          tokensL.list[nextL.list[cn][0]] = is;
          int[] one = nextL.list[cn];
          int[] ne = new int[5];
          ne[0] = one[0];
          if (r2[0] < r1[0]) {
            ne[1] = nextL.size;
            ne[2] = nextL.size + 1;
          } else {
            ne[1] = nextL.size + 1;
            ne[2] = nextL.size;
          }
          ne[3] = 0;
          ne[4] = 0;
          nextL.list[cn] = ne;

          ne = new int[3];
          ne[0] = tokensL.size;
          tokensL.add(r2);

          if(d != null) {
            ne[1] = d[0].length;
            ne[2] = addDataToNode(0, d, 0);
          } else {
            ne[1] = 0;
            ne[2] = 0;
          }
          nextL.add(ne);
          
          ne = new int[one.length];
          System.arraycopy(one, 0, ne, 0, ne.length);
          ne[0] = tokensL.size;
          tokensL.add(r1);
          nextL.add(ne);
          //System.out.println("fall5");
          return nextL.size - 1;
          
        }
      }
    }  else {
      // abort recursion
      // no intersection between current node an valuetoinsert
      int[] ne = new int[3];
      ne[0] = tokensL.size;
      tokensL.add(v);
      if(d !=  null) {
        ne[2] = addDataToNode(0, d, 0);
        ne[1] = d[0].length;
      } else {
        ne[1] = 0;
        ne[2] = 0;
      }
      
      nextL.add(ne);
      
      final int ip = getInsertingPosition(cn, v[0]);
      insertNodeInNextArray(cn, nextL.size - 1, ip);
          
      //System.out.println("fall6");
      return nextL.size - 1;
    }
  }

  /**
   * Indexes the specified token.
   * 
   * @param token token to be indexex
   * @param id pre value of the token
   * @param tokenStart position value token start
   */
  public void index(final byte[] token, final int id, final int tokenStart) {
    count++;
    int[][] d = new int[2][1];
    d[0][0] = id;
    d[1][0] = tokenStart;

    insertNodeIntoTrie(0, token, d);
  }

  /**
   * Indexes the specified token.
   * 
   * @param token token to be indexex
   * @param data  int[][] pre and pos value of the token
   */
  public void index(final byte[] token, final int[][] data) {
    count++;
    insertNodeIntoTrie(0, token, data);
  }
  
  
  /**
   * Adds data to node.
   *
   * @param s current size of ftdata
   * @param dataToAdd int[][]
   * @param p int pointer on ftdata
   * @return pointer on data
   */
  private int addDataToNode(final int s, final int[][] dataToAdd,
      final int p) {
    if (dataToAdd == null) return p;
    
    if (s == 0 && p == 0) {
      // node has no data yet
      preL.add(dataToAdd[0]);
      posL.add(dataToAdd[1]);
      return preL.size - 1;
    } else {
      int[] tmp = new int[preL.list[p].length + dataToAdd[0].length];
      System.arraycopy(preL.list[p], 0, tmp, 0,  preL.list[p].length);
      System.arraycopy(dataToAdd[0], 0, tmp, preL.list[p].length, 
          dataToAdd[0].length);
      preL.list[p] = tmp;
      tmp = new int[posL.list[p].length + dataToAdd[1].length];
      System.arraycopy(posL.list[p], 0, tmp, 0,  posL.list[p].length);
      System.arraycopy(dataToAdd[1], 0, tmp, posL.list[p].length, 
          dataToAdd[1].length);
      posL.list[p] = tmp;
      return p;
    }
  }


  /**
   * Save whether a corresponding node was found in method
   * getInsertingPosition.
   */
  private boolean found;

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
    int i;
    
    //i = getInsertingPositionBinary(currentPosition, toInsert);
    if (bl) i = getInsertingPositionLinearUFBack(currentPosition, toInsert);
    else i = getInsertingPositionBinaryUF(currentPosition, toInsert);
    return i;
  }

  /**
   * Uses linear search for finding inserting position.
   * returns:
   * 0 if any successor exists, or 0 is inserting position
   * n here to insert
   * n and found = true, if nth item is occupied and here to insert
   * BASED ON UNFINISHED STRUCTURE ARRAYS!!!!
   * 
   * @param cn pointer on next array
   * @param toInsert value to be inserted
   * @return inserting position
   */
  private int getInsertingPositionLinearUF(final int cn,
      final byte toInsert) {
    // init value
    found = false;

    int i = 1;
    int s = nextL.list[cn].length - 2;
    if (s == i) 
      return i;

    while (i < s 
        && tokensL.list[nextL.list[nextL.list[cn][i]][0]][0] < toInsert) i++;
    
    if (i < s 
        && tokensL.list[nextL.list[nextL.list[cn][i]][0]][0] == toInsert) {
      found = true;
    }
    return i;
  }

  /**
   * Uses binary search for finding inserting position.
   * returns:
   * 0 if any successor exists, or 0 is inserting position
   * n here to insert
   * n and found = true, if nth item is occupied and here to insert
   * BASED ON UNFINISHED STRUCTURE ARRAYS!!!!

   * @param cn current node
   * @param toi char to insert
   * @return inserting poition
   */
  private int getInsertingPositionBinaryUF(final int cn, final byte toi) {
    found = false;
    int l = 1;
    int r = nextL.list[cn].length - 3;
    int m = l + (r - l) / 2;
    //if (r > 2) {
      while (l <= r) {
        m = l + (r - l) / 2;
        m = (m == 0) ? 1 : m;
        if (tokensL.list[nextL.list[nextL.list[cn][m]][0]][0] < toi) l = m + 1;
        else if (tokensL.list[nextL.list[nextL.list[cn][m]][0]][0] > toi) 
          r = m - 1;
        else {
          found = true;
          return m;
        }
      }
   /* } else if (r == 1) return 1;
    else {
      if (tokensL.list[nextL.list[nextL.list[cn][l]][0]][0] < toi) return 1;
      else if (tokensL.list[nextL.list[nextL.list[cn][l]][0]][0] == toi) {
        found = true;
        return 1;
      }
      else return 1;
    }
    */
    if (l < nextL.list[cn].length - 2 
        && tokensL.list[nextL.list[nextL.list[cn][m]][0]][0] == toi) {
      found = true;
      return l + 1;
    }
    return l;
  }
  
  /**
   * Performces the linear search backwards - used for bulkloading.
   * @param cn current node
   * @param toInsert byte to insert
   * @return index of inserting position
   */
  private int getInsertingPositionLinearUFBack(final int cn,
      final byte toInsert) {
    found = false;
    int i = nextL.list[cn].length - 3;
    int s = 0;
    if (s == i) 
      return 1;
    
    while (i > s 
        && tokensL.list[nextL.list[nextL.list[cn][i]][0]][0] > toInsert) i--;
    
    if (i > s 
        && tokensL.list[nextL.list[nextL.list[cn][i]][0]][0] == toInsert) {
      found = true;
      return i;
    }
     return i + 1;
  }

  
  /**
   * Uses linear search for finding inserting position.
   * returns:
   * 0 if any successor exists, or 0 is inserting position
   * n here to insert
   * n and found = true, if nth item is occupied and here to insert
   * BASED ON FINISHED STRCUTURE ARRAYS!!!!
   *
   * @param cn pointer on next array
   * @param toInsert value to be inserted
   * @return inserting position
   */
  private int getInsertingPositionLinearF(final int cn,
      final byte toInsert) {
    found = false;
    int i = 1;
    int s = next[cn].length - 2;
    if (s == i) 
      return i;

    while (i < s && tokens[next[next[cn][i]][0]][0] < toInsert) i++;
    
    if (i < s && tokens[next[next[cn][i]][0]][0] == toInsert) {
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
   * BASED ON FINISHED STRCUTURE ARRAYS!!!!
   *
   * @param cn pointer on next array
   * @param toi value to be inserted
   * @return inserting position
   */
  private int getInsertingPositionBinaryF(final int cn, final byte toi) {
    found = false;
    int l = 1;
    int r = next[cn].length - 2;
    int m = r - l / 2;
    while (l < r) {
      m = r - l / 2;
      if (tokens[next[next[cn][m]][0]][0] < toi) l = m + 1;
      else if (tokens[next[next[cn][m]][0]][0] > toi) 
        r = m - 1;
      else {
        found = true;
        return m;
      }
    }
    
    if (l < next[cn].length - 2 
        && tokens[next[next[cn][m]][0]][0] == toi) {
      found = true;
      return l + 1;
    }
    return l;
  }

  /**
   * Lookup for nodes.
   * @param valueSearchNode search nodes value
   * @return int[][] dataResultNode data of result node
   */
  public int[][] getNodeFromTrie(final byte[] valueSearchNode) {
    if (finished) return getNodeFromTrieRecursiveF(0, valueSearchNode);
    else return getNodeFromTrieRecursiveUF(0, valueSearchNode);
  }

  /**
   * Traverse trie and return found node for searchValue.
   * Returns data from node or null
   * BASED ON UNFINISHED STRUCTURE ARRAYS!!!!
   *
   * @param cn int
   * @param sn search nodes value
   * @return int[][]
   */
  private int[][] getNodeFromTrieRecursiveUF(final int cn, final byte[] sn) {
    byte[] vsn = sn;
    if(cn != 0) {
      int i = 0;
      while(i < vsn.length && i < tokensL.list[nextL.list[cn][0]].length 
          && tokensL.list[nextL.list[cn][0]][i] == vsn[i]) {
        i++;
      }

      if(tokensL.list[nextL.list[cn][0]].length == i) {  
        if(vsn.length == i) {
          // leaf node found with appropriate value
          int[][] tmp = new int[2][];
          tmp[0] = preL.list[nextL.list[cn][nextL.list[cn].length - 1]];
          tmp[1] = posL.list[nextL.list[cn][nextL.list[cn].length - 1]];
          return tmp;
        } else {
          // cut valueSearchNode for value current node
          final byte[] tmp = new byte[vsn.length - i];
          for(int j = 0; j < tmp.length; j++) {
            tmp[j] = vsn[i + j];
          }
          vsn = tmp;

          // scan successors currentNode
          final int p = getInsertingPositionLinearUF(cn, vsn[0]);
          if(!found) {
            // node not contained
            return null;
          } else {
            return getNodeFromTrieRecursiveUF(nextL.list[cn][p], vsn);
          }
        }
      } else {
        // node not contained
        return null;
      }
    } else {
      // scan successors current node
      final int p = getInsertingPositionLinearUF(cn, vsn[0]);
      if(!found) {
        // node not contained
        return null;
      } else {
        return getNodeFromTrieRecursiveUF(nextL.list[cn][p], vsn);
      }
    }
  }

  /**
   * Traverse trie and return found node for searchValue.
   * Returns data from node or null
   * BASED ON FINISHED STRUCTURE ARRAYS!!!!
   *
   * @param cn int
   * @param sn search nodes value
   * @return int[][]
   */
  private int[][] getNodeFromTrieRecursiveF(final int cn, final byte[] sn) {
    byte[] vsn = sn;
    if(cn != 0) {
      int i = 0;
      while(i < vsn.length && i < tokens[next[cn][0]].length 
          && tokens[next[cn][0]][i] == vsn[i]) {
        i++;
      }

      if(tokens[next[cn][0]].length == i) {  
        if(vsn.length == i) {
          // leaf node found with appropriate value
          int[][] tmp = new int[2][];
          tmp[0] = pre[next[cn][next[cn].length - 1]];
          tmp[1] = pos[next[cn][next[cn].length - 1]];
          return tmp;
        } else {
          // cut valueSearchNode for value current node
          final byte[] tmp = new byte[vsn.length - i];
          for(int j = 0; j < tmp.length; j++) {
            tmp[j] = vsn[i + j];
          }
          vsn = tmp;

          // scan successors currentNode
          final int p = getInsertingPositionLinearF(cn, vsn[0]);
          if(!found) {
            // node not contained
            return null;
          } else {
            return getNodeFromTrieRecursiveF(next[cn][p], vsn);
          }
        }
      } else {
        // node not contained
        return null;
      }
    } else {
      // scan successors current node
      final int p = getInsertingPositionLinearF(cn, vsn[0]);
      if(!found) {
        // node not contained
        return null;
      } else {
        return getNodeFromTrieRecursiveF(next[cn][p], vsn);
      }
    }
  }

  /**
   * Adds data to a node with the value sn.
   * If the node doesn't exist, nothing is added.
   *
   * @param cn int
   * @param sn search nodes value
   * @param data int[][] data to add
   */
  public void loadData(final int cn, final byte[] sn, final int[][] data) {
    byte[] vsn = sn;
    if(cn != 0) {
      int i = 0;
      while(i < vsn.length && i < tokens[next[cn][0]].length 
          && tokens[next[cn][0]][i] == vsn[i]) {
        i++;
      }

      if(tokens[next[cn][0]].length == i) {  
        if(vsn.length == i) {
          // leaf node found with appropriate value
          if (next[cn][next[cn].length - 1] == 0 
              && next[cn][next[cn].length - 2] == 0) {
            next[cn][next[cn].length - 1] = 
              addDataToNode(next[cn][next[cn].length - 2], 
                data, next[cn][next[cn].length - 1]);
            next[cn][next[cn].length - 2] = data[0].length;
          } else {
            addDataToNode(next[cn][next[cn].length - 2], 
                data, next[cn][next[cn].length - 1]);
            next[cn][next[cn].length - 2] += data[0].length;
          }
          
        } else {
          // cut valueSearchNode for value current node
          final byte[] tmp = new byte[vsn.length - i];
          for(int j = 0; j < tmp.length; j++) {
            tmp[j] = vsn[i + j];
          }
          vsn = tmp;

          // scan successors currentNode
          final int p = getInsertingPositionLinearUFBack(cn, vsn[0]);
          if(!found) {
            // node not contained
            return;
          } else {
            loadData(next[cn][p], vsn, data);
            return;
          }
        }
      } else {
        // node not contained
        return;
      }
    } else {
      // scan successors current node
      final int p = getInsertingPositionLinearUFBack(cn, vsn[0]);
      if(!found) {
        // node not contained
        return;
      } else {
        loadData(next[cn][p], vsn, data);
        return;
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
  
  /*
  public static void main(final String[] args) {
    CTArrayNew c = new CTArrayNew();
    c.insertNodeIntoTrie(0, "q".getBytes(), new int[][]{{3},{30}});
    c.insertNodeIntoTrie(0, "qqa".getBytes(), new int[][]{{1},{10}});
    c.insertNodeIntoTrie(0, "bqgwqc".getBytes(), new int[][]{{2},{20}});
    c.insertNodeIntoTrie(0, "zqgwqc".getBytes(), new int[][]{{2},{20}});
 //   c.insertNodeIntoTrie(0, "qvzdcttc".getBytes(), new int[][]{{4},{40}});
 //   c.insertNodeIntoTrie(0, "qudvt".getBytes(), new int[][]{{5},{50}});
 //   c.insertNodeIntoTrie(0, "q".getBytes(), new int[][]{{6},{60}});
    //c.insertNodeIntoTrie(0, "kt".getBytes(), new int[][]{{3},{30}});
    //c.insertNodeIntoTrie(0, "ktblxzziw".getBytes(), new int[][]{{2},{20}});
    System.out.println();
    c.printTrie();
    //int[][] d = c.getNodeFromTrie("ka".getBytes());
    //System.out.println(intArrayToString(d[0]));
    //System.out.println(intArrayToString(d[1]));
  }
  */
  
  /**
   * Prints the struture arrays.
   */
  public void printTrie() {
    System.out.println("NextN:");
    for(int[] i : nextL.list) {
      if (i == null) break;
      else System.out.print(intArrayToString(i));
    }
    System.out.println();
    System.out.println("Token:");
    for(byte[] b : tokensL.list) {
      if (b == null) break;
      else System.out.print(new String(b));
      System.out.print(",");
    }
    System.out.println();
    System.out.println("Pre:");
    for(int[] i : preL.list) {
      if (i == null) break;
      else System.out.print(intArrayToString(i));
    }
    System.out.println();
    System.out.println("Pos:");
    for(int[] i : posL.list) {
      if (i == null) break;
      else System.out.print(intArrayToString(i));
    }
    System.out.println();
  }
  
  /**
   * Converts an int array to string.
   * @param i array to convert
   * @return string with the values from i
   */
  public static String  intArrayToString(final int[] i) {
    StringBuffer sb = new StringBuffer();
    sb.append('[');
    for(int in : i) sb.append(in + ",");
    sb.deleteCharAt(sb.length() - 1);
    sb.append(']');
    return sb.toString();
  }
}
