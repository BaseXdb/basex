package org.basex.data;

import static org.basex.util.Token.*;
import org.basex.util.Array;
import org.basex.util.IntList;
import org.basex.util.Token;

/**
 * This class organizes data references used by queries.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
final class DataPaths {
  /** Document nodes. */
  int[] docs;
  /** Document paths. */
  byte[][] paths;
  /** Order array. */
  int[] order;

  /**
   * Invalidates the paths.
   */
  void update() {
    docs = null;
    paths = null;
    order = null;
  }

  /**
   * Returns all document nodes.
   * @param data data reference
   * A single dummy node is returned if the database is empty.
   * @return document nodes
   */
  int[] doc(final Data data) {
    if(docs == null) {
      final IntList il = new IntList();
      final int is = data.meta.size;
      for(int i = 0; i < is; i += data.size(i, Data.DOC)) il.add(i);
      docs = il.toArray();
    }
    return docs;
  }

  /**
   * Returns the document nodes for the specified path.
   * @param path input path
   * @param data data reference
   * @return root nodes
   */
  int[] doc(final String path, final Data data) {
    // no documents: return empty list
    if(data.empty()) return new int[] {};

    // empty path: return empty list
    if(docs == null) doc(data);
    if(path.isEmpty()) return docs;

    // initialize and sort document paths
    final int ds = docs.length;
    final byte[] slash = token("/");
    if(paths == null) {
      paths = new byte[ds][];
      for(int d = 0; d < ds; d++) {
        paths[d] = concat(slash, lc(data.text(docs[d], true)));
      }
      order = Array.createOrder(paths, false, true);
    }

    // exact path: remove redundant slashes and switch to lower case
    final String np = path.contains("//") ? path.replaceAll("/+", "/") : path;
    final byte[] exact = lc(concat(slash, token(np)));
    // root path
    final byte[] start = endsWith(exact, slash) ? exact : concat(exact, slash);

    // relevant paths: start from the first hit and return all subsequent hits
    final IntList il = new IntList();
    for(int o = find(exact); o < order.length; o++) {
      final int pre = docs[order[o]];
      // add documents which match specified input path
      final byte[] pth = concat(slash, lc(data.text(pre, true)));
      if(!eq(pth, exact) && !startsWith(pth, start)) break;
      il.add(pre);
    }
    return il.sort().toArray();
  }

  /**
   * Returns the position of the specified value.
   * @param v value to be found
   * @return position or negative insertion value - 1
   */
  private int find(final byte[] v) {
    // binary search
    int l = 0, h = order.length - 1;
    while(l <= h) {
      final int m = l + h >>> 1;
      final int c = Token.diff(paths[order[m]], v);
      if(c == 0) return m;
      if(c < 0) l = m + 1;
      else h = m - 1;
    }
    return l;
  }
}
