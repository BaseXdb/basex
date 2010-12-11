package org.basex.data;

import static org.basex.util.Token.*;
import org.basex.util.Array;
import org.basex.util.IntList;

/**
 * This class organizes data references used by queries.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
final class DataPaths {
  /** Pre values of document nodes. */
  int[] docs;
  /** Sorted document paths. */
  byte[][] paths;
  /** Mapping between document paths and pre values. */
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
   * Returns the pre values of all document nodes.
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
   * Returns the pre values of all document nodes matching the specified path.
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
    for(int p = find(exact); p < paths.length; p++) {
      if(!eq(paths[p], exact) && !startsWith(paths[p], start)) break;
      il.add(docs[order[p]]);
    }
    return il.sort().toArray();
  }

  /**
   * Returns the first position matching the specified path.
   * @param v value to be found
   * @return position or negative insertion value - 1
   */
  private int find(final byte[] v) {
    // binary search
    int l = 0, h = order.length - 1;
    while(l <= h) {
      int m = l + h >>> 1;
      final int c = diff(paths[m], v);
      if(c == 0) {
        // find first entry
        while(m > 0 && eq(paths[m - 1], v)) --m;
        return m;
      }
      if(c < 0) l = m + 1;
      else h = m - 1;
    }
    return l;
  }
}
