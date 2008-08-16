package org.basex.index;

import org.basex.util.IntArrayList;
import org.basex.util.Token;
import org.basex.util.TokenList;

/**
 * Preserves a compressed trie index structure and useful functionality.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Sebastian Gath
 */
final class FTArray {
  /** counts all nodes in the trie. **/
  int count;

  /** THE LISTS ARE USED TO CREATE THE STRUCTURE. **/
  /** List saving the token values. **/
  TokenList tokens;
  /** List saving the structure: [t, n1, ..., nk, s, p0, p1]
   * t = pointer on tokens; n1, ..., nk are the children of the node
   * saved as pointer on nextN; s = size of pre values; p = pointer
   * if p is a long value, it is split into 2 ints with p0 < 0
   * on pre/pos where the data is stored. t, s, p are saved for every node. */
  IntArrayList next;
  /** List with pre values. */
  IntArrayList pre;
  /** List with pos values. */
  IntArrayList pos;
  /** Flag for bulk load option. */
  boolean bl = true;
  /** Flag for creating a case sensitive index. */
  boolean cs = false;

  /**
   * Constructor.
   * @param is index size, number of tokens to index.
   * @param sens flag for case sensitive index
   */
  FTArray(final int is, final boolean sens) {
    next = new IntArrayList(is);
    pre = new IntArrayList(is);
    pos = new IntArrayList(is);
    tokens = new TokenList(is);
    // add root node with k, t, s
    next.add(new int[]{-1, 0, 0});
    count = 1;
    cs = sens;
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
    final int[] tmp = new int[next.list[cn].length + 1];
    System.arraycopy(next.list[cn], 0, tmp, 0, ip);
    // insert node
    tmp[ip] = ti;
    // copy remain
    System.arraycopy(next.list[cn], ip, tmp, ip + 1, tmp.length - ip - 1);
    next.list[cn] = tmp;
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
      if(next.list[cn].length > 3) {
        final int p = getInsertingPosition(cn, v[0]);
        if(!found) {
          // any child has an appropriate value to valueToInsert;
          // create new node and append it; save data

          int[] e;
          e = new int[3];
          e[0] = tokens.size;
          tokens.add(v);
          if (d == null) {
            e[1] = 0;
            e[2] = 0;
          } else {
            e[1] = d[0].length;
            e[2] = addDataToNode(0, d, 0);
          }

          next.add(e);
          insertNodeInNextArray(cn, next.size - 1, p);
          //System.out.println("fall1");
          return next.size - 1;
        } else {
          return insertNodeIntoTrie(next.list[cn][p], v, d);
        }
      }
    }

    final byte[] is = (next.list[cn][0] == -1) ?
        null : calculateIntersection(tokens.list[next.list[cn][0]], v);
    byte[] r1 = (next.list[cn][0] == -1) ?
        null : tokens.list[next.list[cn][0]];
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
            if (next.list[cn][next.list[cn].length - 2] == 0) {
              next.list[cn][next.list[cn].length - 1] =
                addDataToNode(next.list[cn][next.list[cn].length - 2], d,
                    next.list[cn][next.list[cn].length - 1]);
              next.list[cn][next.list[cn].length - 2] = d[0].length;
            } else {
              addDataToNode(next.list[cn][next.list[cn].length - 2], d,
                  next.list[cn][next.list[cn].length - 1]);
              next.list[cn][next.list[cn].length - 2] += d[0].length;
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
            e[0] = tokens.size;
            tokens.add(r2);
            if (d == null) {
              e[1] = 0;
              e[2] = 0;
            } else {
              e[1] = d[0].length;
              e[2] = addDataToNode(0, d, 0);
            }
            next.add(e);
            insertNodeInNextArray(cn, next.size - 1, posti);
            //System.out.println("fall3");
            return next.size - 1;
          } else {
            return insertNodeIntoTrie(next.list[cn][posti], r2, d);
          }
        }

      } else {
        if(r2 == null) {
          // char1 != null &&  char2 == null
          // value of currentNode equals valuteToInsert,
          // but current has a longer value
          // update value of currentNode.value with intersection
          final int[] oe = new int[4];
          tokens.list[next.list[cn][0]] = is;
          oe[0] = next.list[cn][0];
          final int did = next.list[cn][next.list[cn].length - 1];
          if(d != null) {
            oe[3] = addDataToNode(0, d, 0);
            oe[2] = d[0].length;
          } else {
            oe[3] = 0;
            oe[2] = 0;
          }
          // next data from currentNode.next update
          final int[] ne = new int[next.list[cn].length];
          System.arraycopy(next.list[cn], 0, ne, 0, ne.length);
          ne[0] = tokens.size;
          tokens.add(r1);
          ne[ne.length - 1] = did;
          ne[ne.length - 2] = pre.list[did].length;

          //next[countNodes] = next[cn];
          next.add(ne);
          oe[1] = next.size - 1;
          next.list[cn] = oe;
          //System.out.println("fall4");
          return next.size - 1;
        } else {
          // char1 != null && char2 != null
          // value of current node and value to insert have only one common
          // letter update value of current node with intersection
          tokens.list[next.list[cn][0]] = is;
          final int[] one = next.list[cn];
          int[] ne = new int[5];
          ne[0] = one[0];
          //if (r2[0] < r1[0]) {
          if (Token.lc(r2[0]) < Token.lc(r1[0]) ||
              (Token.lc(r2[0]) == Token.lc(r1[0])
                  && Token.uc(r2[0]) == r1[0])) {
            ne[1] = next.size;
            ne[2] = next.size + 1;
          } else {
            ne[1] = next.size + 1;
            ne[2] = next.size;
          }
          ne[3] = 0;
          ne[4] = 0;
          next.list[cn] = ne;

          ne = new int[3];
          ne[0] = tokens.size;
          tokens.add(r2);

          if(d != null) {
            ne[1] = d[0].length;
            ne[2] = addDataToNode(0, d, 0);
          } else {
            ne[1] = 0;
            ne[2] = 0;
          }
          next.add(ne);

          ne = new int[one.length];
          System.arraycopy(one, 0, ne, 0, ne.length);
          ne[0] = tokens.size;
          tokens.add(r1);
          next.add(ne);
          //System.out.println("fall5");
          return next.size - 1;

        }
      }
    }  else {
      // abort recursion
      // no intersection between current node an valuetoinsert
      final int[] ne = new int[3];
      ne[0] = tokens.size;
      tokens.add(v);
      if(d !=  null) {
        ne[2] = addDataToNode(0, d, 0);
        ne[1] = d[0].length;
      } else {
        ne[1] = 0;
        ne[2] = 0;
      }

      next.add(ne);

      final int ip = getInsertingPosition(cn, v[0]);
      insertNodeInNextArray(cn, next.size - 1, ip);

      //System.out.println("fall6");
      return next.size - 1;
    }
  }

  /**
   * Inserts a token into the trie. The tokens have to be sorted!!
   *
   * @param v value, which is to be inserted
   * @param s size of the data the node will have
   * @param offset file offset where to read the data
   */
  void insertSorted(final byte[] v, final int s, final long offset) {
    count++;
    insertNodeSorted(0, v, s, Token.longToInt(offset));
    return;
  }

  /**
   * Inserts a node into the trie.
   *
   * @param cn current node, which gets a new node
   * appended; start with root (0)
   * @param v value, which is to be inserted
   * @param s size of the data the node will have
   * @param offset file offset where to read the data
   * @return nodeId, parent node of new node
   */
  private int insertNodeSorted(final int cn, final byte[] v, final int s,
      final int[] offset) {
    // currentNode is root node
    if(cn == 0) {
      // root has successors
      if(next.list[cn].length > 3) {
        final int p = getPointer(cn); //nextL.list[cn].length - 3;
        if(tokens.list[next.list[next.list[cn][p]][0]][0] != v[0]) {
          //System.out.println("fall1");
          //if(!found) {
          // any child has an appropriate value to valueToInsert;
          // create new node and append it; save data

          int[] e;
          e = new int[2 + offset.length]; //new int[3];
          e[0] = tokens.size;
          tokens.add(v);
          e[1] = s;
          System.arraycopy(offset, 0, e, 2, offset.length);
          //e[2] = offset;

          next.add(e);
          //insertNodeInNextArray(cn, nextL.size - 1, p);
          insertNodeInNextArray(cn, next.size - 1, p + 1);
          //System.out.println("fall1");
          return next.size - 1;
        } else {
          //System.out.println("fall1a");
          return insertNodeSorted(next.list[cn][p], v, s, offset);
        }
      }
    }

    final byte[] is = (next.list[cn][0] == -1) ?
        null : calculateIntersection(tokens.list[next.list[cn][0]], v);
    byte[] r1 = (next.list[cn][0] == -1) ?
        null : tokens.list[next.list[cn][0]];
    byte[] r2 = v;

    if(is != null) {
      r1 = getBytes(r1, is.length, r1.length);
      r2 = getBytes(v, is.length, v.length);
    }

    if(is !=  null) {
      if(r1 == null) {
        if(r2 != null) {
          // char1 == null && char2 != null
          // value of currentNode equals valueToInsert,
          //but valueToInset is longer
          final int p = getPointer(cn); //nextL.list[cn].length - 3;
          if(p == 0 || //nextL.list[nextL.list[cn][p]][0] == -1 ||
              tokens.list[next.list[next.list[cn][p]][0]][0] != r2[0]) {
            //System.out.println("fall2");
            //(!found) {
            // create new node and append it, because any child from curretnNode
            // start with the same letter than reamin2.
            int[] e;
            e = new int[2 + offset.length]; // new int[3];
            e[0] = tokens.size;
            tokens.add(r2);
            e[1] = s;
            //e[2] = offset;
            System.arraycopy(offset, 0, e, 2, offset.length);

            next.add(e);
            insertNodeInNextArray(cn, next.size - 1, p + 1); //posti);
            //System.out.println("fall2");
            return next.size - 1;
          } else {
            //System.out.println("fall2a");
            //return insertNodeIntoTrie(nextL.list[cn][posti], r2, d);
            return insertNodeSorted(next.list[cn][p], r2, s, offset);
          }
        }

      } else {
        if(r2 == null) {
          //System.out.println("fall3");
          // char1 != null &&  char2 == null
          // value of currentNode equals valuteToInsert,
          // but current has a longer value
          // update value of currentNode.value with intersection
          final int[] oe = new int [3 + offset.length]; //new int[4];
          tokens.list[next.list[cn][0]] = is;
          oe[0] = next.list[cn][0];
          //int did = nextL.list[cn][nextL.list[cn].length - 1];
          //oe[3] = offset;
          System.arraycopy(offset, 0, oe, 3, offset.length);
          oe[2] = s;

          //int[] ne = new int[nextL.list[cn].length];
          //System.arraycopy(nextL.list[cn], 0, ne, 0, ne.length);
          next.list[cn][0] = tokens.size;
          tokens.add(r1);
          //ne[ne.length - 1] = did;
          //ne[ne.length - 2] = preL.list[did].length;

          //next[countNodes] = next[cn];
          //nextL.add(ne);
          next.add(next.list[cn]);
          oe[1] = next.size - 1;
          next.list[cn] = oe;
          //System.out.println("fall3");
          return next.size - 1;
        } else {
          //System.out.println("fall3a");
          // char1 != null && char2 != null
          // value of current node and value to insert have only one common
          // letter update value of current node with intersection
          tokens.list[next.list[cn][0]] = is;
          final int[] one = next.list[cn];
          int[] ne = new int[5];
          ne[0] = one[0];
          if (r2[0] < r1[0]) {
            ne[1] = next.size;
            ne[2] = next.size + 1;
          } else {
            ne[1] = next.size + 1;
            ne[2] = next.size;
          }
          ne[3] = 0;
          ne[4] = 0;
          next.list[cn] = ne;

          ne = new int[2 + offset.length]; //new int[3];
          ne[0] = tokens.size;
          tokens.add(r2);

          ne[1] = s;
          //ne[2] = offset;
          System.arraycopy(offset, 0, ne, 2, offset.length);

          next.add(ne);

          ne = new int[one.length];
          System.arraycopy(one, 0, ne, 0, ne.length);
          ne[0] = tokens.size;
          tokens.add(r1);
          next.add(ne);
          //System.out.println("fall3a");
          return next.size - 1;

        }
      }
    }  else {
      //System.out.println("fall4");
      // abort recursion
      // no intersection between current node an valuetoinsert
      final int[] ne = new int[2 + offset.length]; //new int[3];
      ne[0] = tokens.size;
      tokens.add(v);
      System.arraycopy(offset, 0, ne, 2, offset.length);
      //ne[2] = offset;
      ne[1] = s;

      next.add(ne);
      final int p = next.list[cn].length - 2;
      //final int ip = getInsertingPosition(cn, v[0]);
      insertNodeInNextArray(cn, next.size - 1, p);

      //System.out.println("fall4");
      return next.size - 1;
    }
    return -1;
  }

  /**
   * Indexes the specified token.
   *
   * @param token token to be indexex
   * @param id pre value of the token
   * @param tokenStart position value token start
   */
  void index(final byte[] token, final int id, final int tokenStart) {
    count++;
    final int[][] d = new int[2][1];
    d[0][0] = id;
    d[1][0] = tokenStart;

    insertNodeIntoTrie(0, token, d);
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
      pre.add(dataToAdd[0]);
      pos.add(dataToAdd[1]);
      return pre.size - 1;
    } else {
      int[] tmp = new int[pre.list[p].length + dataToAdd[0].length];
      System.arraycopy(pre.list[p], 0, tmp, 0,  pre.list[p].length);
      System.arraycopy(dataToAdd[0], 0, tmp, pre.list[p].length,
          dataToAdd[0].length);
      pre.list[p] = tmp;
      tmp = new int[pos.list[p].length + dataToAdd[1].length];
      System.arraycopy(pos.list[p], 0, tmp, 0,  pos.list[p].length);
      System.arraycopy(dataToAdd[1], 0, tmp, pos.list[p].length,
          dataToAdd[1].length);
      pos.list[p] = tmp;
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
    else {
      if (cs) i = getInsPosLinCSUF(currentPosition, toInsert);
      else
        i = getInsertingPositionBinaryUF(currentPosition, toInsert);
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
   * BASED ON UNFINISHED STRUCTURE ARRAYS!!!!
   *
   * @param cn pointer on next array
   * @param toInsert value to be inserted
   * @return inserting position
   */
  private int getInsPosLinCSUF(final int cn,
      final byte toInsert) {
    // init value
    found = false;

    int i = 1;
    final int s = next.list[cn].length - 2;
    if (s == i)
      return i;
    while (i < s
        && Token.lc(tokens.list[next.list[next.list[cn][i]][0]][0])
        < Token.lc(toInsert)) {
      i++;
    }

    if (i < s) {
      if(tokens.list[next.list[next.list[cn][i]][0]][0] == toInsert) {
        found = true;
        return i;
      } else if(Token.lc(tokens.list[next.list[next.list[cn][i]][0]][0])
          == Token.lc(toInsert)) {
        if (tokens.list[next.list[next.list[cn][i]][0]][0]
           == Token.uc(toInsert))
          return i;
        if (i + 1 < s &&
            tokens.list[next.list[next.list[cn][i + 1]][0]][0] == toInsert) {
          found = true;
        }
        return i + 1;
      }
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
    int r = next.list[cn].length - 3;
    int m = l + (r - l) / 2;
      while (l <= r) {
        m = l + (r - l) / 2;
        m = (m == 0) ? 1 : m;
        if (tokens.list[next.list[next.list[cn][m]][0]][0] < toi) l = m + 1;
        else if (tokens.list[next.list[next.list[cn][m]][0]][0] > toi)
          r = m - 1;
        else {
          found = true;
          return m;
        }
      }
      if (l < next.list[cn].length - 2
        && tokens.list[next.list[next.list[cn][m]][0]][0] == toi) {
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
    int i = next.list[cn].length - 3;
    final int s = 0;
    if (s == i)
      return 1;

    while (i > s
        && tokens.list[next.list[next.list[cn][i]][0]][0] > toInsert) i--;

    if (i > s
        && tokens.list[next.list[next.list[cn][i]][0]][0] == toInsert) {
      found = true;
      return i;
    }
     return i + 1;
  }

  /**
   * Calculates the intersection.
   * @param b1 input array one
   * @param b2 input array two
   * @return intersection of b1 and b2
   */
  private byte[] calculateIntersection(final byte[] b1, final byte[] b2) {
    if(b1 == null || b2 == null) return null;

    final int ml = Math.min(b1.length, b2.length);
    int i = -1;
    while(++i < ml && b1[i] == b2[i]);
    return getBytes(b1, 0, i);
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

    if(d == null || d.length < endPos || startPos < 0 || startPos == endPos) {
      return null;
    }

    final byte[] newByte = new byte[endPos - startPos];
    System.arraycopy(d, startPos, newByte, 0, newByte.length);
    return newByte;
  }

  /**
   * Returns the index of the last next pointer in the current node entry.
   * @param cn current node
   * @return index of the data pointer
   */
  private int getPointer(final int cn) {
    final int[] nl = next.list[cn]; 
    return nl[nl.length - 2] >= 0 ? nl.length - 3 : nl.length - 4;
  }
}
