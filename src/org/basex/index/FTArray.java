package org.basex.index;

import org.basex.util.IntArrayList;
import org.basex.util.Token;
import org.basex.util.TokenList;

/**
 * Preserves a compressed trie index structure and useful functionality.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Sebastian Gath
 */
final class FTArray {
  /** counts all nodes in the trie. */
  int count;
  /** THE LISTS ARE USED TO CREATE THE STRUCTURE. */
  /** List saving the token values. */
  TokenList tokens;
  /** List saving the structure: [t, n1, ..., nk, s, p0, p1]
   * t = pointer on tokens; n1, ..., nk are the children of the node
   * saved as pointer on nextN; s = size of pre values; p = pointer
   * if p is a long value, it is split into 2 ints with p0 < 0
   * on pre/pos where the data is stored. t, s, p are saved for every node. */
  IntArrayList next;

  /**
   * Constructor.
   * @param is index size, number of tokens to index.
   */
  FTArray(final int is) {
    next = new IntArrayList(is);
    tokens = new TokenList(is);
    // add root node with k, t, s
    next.add(new int[] { -1, 0, 0 });
    count = 1;
  }

  /**
   * Inserts a node in the next array.
   * @param cn int current node
   * @param ti int id to insert
   * @param ip int position where to insert ti
   * @return Id on next[currentNode]
   */
  private int insertNodeInNextArray(final int cn, final int ti, final int ip) {
    final int[] cnn = next.get(cn);
    final int[] tmp = new int[cnn.length + 1];
    System.arraycopy(cnn, 0, tmp, 0, ip);
    // insert node
    tmp[ip] = ti;
    // copy remain
    System.arraycopy(cnn, ip, tmp, ip + 1, tmp.length - ip - 1);
    next.set(tmp, cn);
    return ip;
  }

  /**
   * Inserts a token into the trie. The tokens have to be sorted!!
   * @param v value, which is to be inserted
   * @param s size of the data the node will have
   * @param offset file offset where to read the data
   */
  void insertSorted(final byte[] v, final int s, final long offset) {
    count++;
    insertNodeSorted(0, v, s, toArray(offset));
    return;
  }

  /**
   * Converts a long value to a int-array.
   * The first 3 bytes are stores in int[1] and
   * the fourth and fifth byte are stored in int[0]
   * as a negative value.
   * @param l long value to convert
   * @return int-array with the long value
   */
  private static int[] toArray(final long l) {
    if(l <= Integer.MAX_VALUE) return new int[]{(int) l};
    final int w = -(int) (l & 0xFFFF);
    // 0xFFFFFF0000
    final int v = (int) (l >> 16 & 0XFFFFFF);
    return new int[] { v, w };
    //11111111 11111111 11111111 00000000 00000000
    //                           11111111 11111111
  }

  /**
   * Inserts a node into the trie.
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
    final int[] cnn = next.get(cn);
    if(cn == 0) {
      // root has successors
      if(cnn.length > 3) {
        final int p = getPointer(cn);
        if(Token.diff(tokens.get(next.get(cnn[p])[0])[0], v[0])
            != 0) {
          // any child has an appropriate value to valueToInsert;
          // create new node and append it; save data
          int[] e;
          e = new int[2 + offset.length];
          e[0] = tokens.size();
          tokens.add(v);
          e[1] = s;
          System.arraycopy(offset, 0, e, 2, offset.length);

          next.add(e);
          insertNodeInNextArray(cn, next.size() - 1, p + 1);
          return next.size() - 1;
        }
        return insertNodeSorted(cnn[p], v, s, offset);
      }
    }

    final byte[] is = cnn[0] == -1 ?
        null : calculateIntersection(tokens.get(cnn[0]), v);
    byte[] r1 = cnn[0] == -1 ? null : tokens.get(next.get(cn)[0]);
    byte[] r2 = v;

    if(is != null) {
      r1 = getBytes(r1, is.length, r1.length);
      r2 = getBytes(v, is.length, v.length);
    }

    if(is != null) {
      if(r1 == null) {
        if(r2 != null) {
          // value of currentNode equals valueToInsert,
          // but valueToInset is longer
          final int p = getPointer(cn);
          if(p == 0 ||
            Token.diff(
                tokens.get(next.get(cnn[p])[0])[0], r2[0]) != 0) {
            // create new node and append it, because any child from curretnNode
            // start with the same letter than reamin2.
            int[] e;
            e = new int[2 + offset.length];
            e[0] = tokens.size();
            tokens.add(r2);
            e[1] = s;
            System.arraycopy(offset, 0, e, 2, offset.length);
            next.add(e);
            insertNodeInNextArray(cn, next.size() - 1, p + 1);
            return next.size() - 1;
          }
          return insertNodeSorted(cnn[p], r2, s, offset);
        }

      } else {
        if(r2 == null) {
          // char1 != null && char2 == null
          // value of currentNode equals valuteToInsert,
          // but current has a longer value
          // update value of currentNode.value with intersection
          final int[] oe = new int [3 + offset.length];
          tokens.set(is, cnn[0]);
          oe[0] = cnn[0];
          System.arraycopy(offset, 0, oe, 3, offset.length);
          oe[2] = s;

          cnn[0] = tokens.size();
          tokens.add(r1);
          next.add(cnn);
          oe[1] = next.size() - 1;
          next.set(oe, cn);
          return next.size() - 1;
        }
        // char1 != null && char2 != null
        // value of current node and value to insert have only one common
        // letter update value of current node with intersection
        tokens.set(is, cnn[0]);
        int[] ne = new int[5];
        ne[0] = cnn[0];
        //if(r2[0] < r1[0]) {
        if(Token.diff(r2[0], r1[0]) < 0) {
          ne[1] = next.size();
          ne[2] = next.size() + 1;
        } else {
          ne[1] = next.size() + 1;
          ne[2] = next.size();
        }
        ne[3] = 0;
        ne[4] = 0;
        next.set(ne, cn);

        ne = new int[2 + offset.length];
        ne[0] = tokens.size();
        tokens.add(r2);

        ne[1] = s;
        System.arraycopy(offset, 0, ne, 2, offset.length);

        next.add(ne);

        ne = new int[cnn.length];
        System.arraycopy(cnn, 0, ne, 0, ne.length);
        ne[0] = tokens.size();
        tokens.add(r1);
        next.add(ne);
        return next.size() - 1;
      }
    } else {
      // abort recursion
      // no intersection between current node a value to insert
      final int[] ne = new int[2 + offset.length];
      ne[0] = tokens.size();
      tokens.add(v);
      System.arraycopy(offset, 0, ne, 2, offset.length);
      ne[1] = s;

      next.add(ne);
      final int p = cnn.length - 2;
      insertNodeInNextArray(cn, next.size() - 1, p);
      return next.size() - 1;
    }
    return -1;
  }

  /**
   * Calculates the intersection.
   * @param b1 input array one
   * @param b2 input array two
   * @return intersection of b1 and b2
   */
  private byte[] calculateIntersection(final byte[] b1, final byte[] b2) {
    if(b1 == null || b2 == null) return null;

    final int ml = b1.length < b2.length ? b1.length : b2.length;
    int i = -1;
    while(++i < ml && Token.diff(b1[i], b2[i]) == 0);
    return getBytes(b1, 0, i);
  }

  /**
   * Extracts all data from start - to end position out of data.
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
    final int[] nl = next.get(cn);
    return nl[nl.length - 1] >= 0 ? nl.length - 3 : nl.length - 4;
  }
}
