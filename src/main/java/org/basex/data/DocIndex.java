package org.basex.data;

import static org.basex.util.Token.*;

import java.io.IOException;

import org.basex.core.Prop;
import org.basex.io.DataInput;
import org.basex.io.DataOutput;
import org.basex.util.Array;
import org.basex.util.IntList;

/**
 * This class contains references to all document nodes in a database.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
final class DocIndex {
  /** Sorted document paths. */
  private byte[][] paths;
  /** Mapping between document paths and pre values. */
  private int[] order;
  /** Pre values of document nodes. */
  private int[] docs;

  /**
   * Opens the metadata for the current database and returns the table size.
   * @param in input stream
   * @return success flag
   * @throws IOException I/O exception
   */
  public boolean read(final DataInput in) throws IOException {
    docs = in.readDiffs();
    return true;
  }

  /**
   * Writes permissions to disk.
   * @param data data reference
   * @param out output stream; if set to null, the global rights are written
   * @throws IOException I/O exception
   */
  public void write(final Data data, final DataOutput out) throws IOException {
    doc(data);
    out.writeDiffs(docs);
  }

  /**
   * Returns the pre values of all document nodes.
   * @param data data reference
   * A single dummy node is returned if the database is empty.
   * @return document nodes
   */
  int[] doc(final Data data) {
    if(docs == null || !data.meta.docindex) {
      docs = null;
      order = null;
      paths = null;
      final IntList il = new IntList();
      final int is = data.meta.size;
      for(int i = 0; i < is; i += data.size(i, Data.DOC)) il.add(i);
      docs = il.toArray();
      data.meta.docindex = true;
      data.meta.dirty = true;
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

    // empty path: return all documents
    doc(data);
    if(path.isEmpty()) return docs;

    // initialize and sort document paths
    final int ds = docs.length;
    if(paths == null) {
      paths = new byte[ds][];
      for(int d = 0; d < ds; d++) {
        final byte[] txt = data.text(docs[d], true);
        paths[d] = concat(SLASH, Prop.WIN ? lc(txt) : txt);
      }
      order = Array.createOrder(paths, false, true);
    }

    // normalize paths
    final String np = path.replaceAll("[\\\\//]+", "/").replaceAll("^/|/$", "");
    final byte[] exact = lc(concat(SLASH, token(np)));
    final byte[] start = endsWith(exact, SLASH) ? exact : concat(exact, SLASH);

    // relevant paths: start from the first hit and return all subsequent hits
    final IntList il = new IntList();
    for(int p = find(exact); p < paths.length; p++) {
      if(eq(paths[p], exact) || startsWith(paths[p], start))
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
