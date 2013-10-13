package org.basex.index.resource;

import static org.basex.util.Token.*;

import java.io.*;

import org.basex.core.*;
import org.basex.data.*;
import org.basex.data.atomic.*;
import org.basex.io.in.DataInput;
import org.basex.io.out.DataOutput;
import org.basex.util.*;
import org.basex.util.hash.*;
import org.basex.util.list.*;

/**
 * <p>This data structure contains references to all document nodes in a
 * database. The document nodes are incrementally updated.</p>
 *
 * <p>If updates are performed, the path order is discarded, as its continuous
 * update would be more expensive in some cases (e.g. when bulk insertions of
 * new documents are performed). A tree structure could be introduced to
 * offer better general performance.</p>
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Christian Gruen
 * @author Lukas Kircher
 */
final class Docs {
  /** Data reference. */
  private final Data data;
  /** Pre values of document nodes (can be {@code null}).
   * This variable should always be requested via {@link #docs()}. */
  private IntList docList;
  /** Sorted document paths (can be {@code null}).
   * This variable should always be requested via {@link #paths()}. */
  private TokenList pathList;
  /** Ordered path indexes (can be {@code null}).
   * This variable should always be requested via {@link #order()}. */
  private int[] pathOrder;

  /**
   * Constructor.
   * @param d data reference
   */
  Docs(final Data d) {
    data = d;
  }

  /**
   * Reads the document index.
   * @param in input stream
   * @throws IOException I/O exception
   */
  synchronized void read(final DataInput in) throws IOException {
    docList = in.readDiffs();
  }

  /**
   * Writes the document index.
   * @param out output stream
   * @throws IOException I/O exception
   */
  void write(final DataOutput out) throws IOException {
    out.writeDiffs(docs());
  }

  /**
   * Initializes the document index. Currently, will only be called if the database is
   * optimized, and the resource index will be rebuilt.
   */
  synchronized void init() {
    docList = null;
    pathList = null;
    docs();
  }

  /**
   * Returns the {@code pre} values of all document nodes.
   * @return document nodes
   */
  synchronized IntList docs() {
    if(docList == null) {
      final IntList il = new IntList();
      final int is = data.meta.size;
      for(int i = 0; i < is;) {
        final int k = data.kind(i);
        if(k == Data.DOC) il.add(i);
        i += data.size(i, k);
      }
      data.meta.dirty = true;
      docList = il;
    }
    return docList;
  }

  /**
   * Returns the document paths, and initializes them if necessary.
   * @return document paths
   */
  private synchronized TokenList paths() {
    if(pathList == null) {
      final IntList docs = docs();
      final int ds = docs.size();
      final TokenList paths = new TokenList(ds);
      for(int d = 0; d < ds; d++) {
        paths.add(normalize(data.text(docs.get(d), true)));
      }
      pathList = paths;
    }
    return pathList;
  }

  /**
   * Returns the document path order, and initialize the array if necessary.
   * @return path order
   */
  private synchronized int[] order() {
    if(pathOrder == null) {
      pathOrder = Array.createOrder(paths().toArray(), false, true);
    }
    return pathOrder;
  }

  /**
   * Adds entries to the index and updates subsequent nodes.
   * @param pre insertion position
   * @param clip data clip
   */
  void insert(final int pre, final DataClip clip) {
    // find all document nodes in the given data instance
    final IntList pres = new IntList();
    for(int dpre = clip.start; dpre < clip.end;) {
      final int k = clip.data.kind(dpre);
      if(k == Data.DOC) pres.add(pre + dpre);
      dpre += clip.data.size(dpre, k);
    }

    // insert DOC nodes and move pre values of following DOC nodes
    final int[] presA = pres.toArray();
    final IntList docs = docs();
    final TokenList paths = paths();

    int i = docs.sortedIndexOf(pre);
    if(i < 0) i = -i - 1;
    docs.insert(i, presA);
    docs.move(clip.size(), i + pres.size());

    final byte[][] t = new byte[presA.length][];
    for(int j = 0; j < t.length; j++) {
      // subtract pre to retrieve paths from given data instance
      t[j] = normalize(clip.data.text(presA[j] - pre, true));
    }
    paths.insert(i, t);
    pathOrder = null;
  }

  /**
   * Deletes the specified entry and updates subsequent nodes.
   * @param pre pre value
   * @param size number of deleted nodes
   */
  void delete(final int pre, final int size) {
    final IntList docs = docs();
    final TokenList paths = paths();

    int i = docs.sortedIndexOf(pre);
    final boolean found = i >= 0;
    if(i < 0) i = -i - 1;
    else docs.deleteAt(i);
    docs.move(-size, i);

    if(!found) return;
    paths.deleteAt(i);
    pathOrder = null;
  }

  /**
   * Updates the index after a document has been renamed.
   * @param pre pre value of updated document
   * @param value new name
   */
  void rename(final int pre, final byte[] value) {
    final IntList docs = docs();
    final TokenList paths = paths();
    paths.set(docs.sortedIndexOf(pre), normalize(value));
    pathOrder = null;
  }

  /**
   * Replaces entries in the index.
   * @param pre insertion position
   * @param size number of deleted nodes
   * @param clip data clip
   */
  void replace(final int pre, final int size, final DataClip clip) {
    delete(pre, size);
    insert(pre, clip);
  }

  /**
   * Returns the pre values of all document nodes matching the specified path.
   * @param path input path
   * @param exact exact (no prefix) matches
   * @return root nodes
   */
  synchronized IntList docs(final String path, final boolean exact) {
    // invalid path, or no documents: return empty list
    final String pth = MetaData.normPath(path);
    if(pth == null) return new IntList(0);

    // empty path: return all documents
    final IntList docs = docs();
    if(pth.isEmpty()) return docs;

    // normalize paths
    byte[] exct = EMPTY;
    byte[] pref = normalize(token(pth));
    // check for explicit directory indicator
    if(!pth.endsWith("/")) {
      exct = pref;
      pref = concat(exct, SLASH);
    }

    // relevant paths: exact hits and prefixes
    final IntList il = new IntList();
    /* could be optimized for future access by sorting the paths first
     * and then accessing only the relevant paths. Sorting might slow down
     * bulk operations like insert/delete/replace though. */
    final TokenList paths = paths();
    for(int p = 0; p < paths.size(); p++) {
      final byte[] b = paths.get(p);
      if(eq(b, exct) || !exact && startsWith(b, pref)) il.add(docs.get(p));
    }
    return il.sort();
  }

  /**
   * Returns the pre value of the document node matching the specified path.
   * Exact match! Document paths can be sorted for faster future access or
   * sorting can be disabled as it slows down bulk inserts/deletes/replaces.
   * @param path input path
   * @param sort sort paths before access
   * @return root nodes
   */
  synchronized int doc(final String path, final boolean sort) {
    // invalid or empty path, or no documents: return -1
    final String pth = MetaData.normPath(path);
    if(pth == null || pth.isEmpty()) return -1;

    // normalize paths
    final byte[] exct = normalize(token(pth));

    // relevant paths: exact match
    final IntList docs = docs();
    final TokenList paths = paths();
    final int ts = paths.size();

    if(sort) {
      final int[] order = order();
      final int p = find(exct);
      return p > -1 && p < ts && eq(paths.get(order[p]), exct) ? docs.get(order[p]) : -1;
    }
    for(int t = 0; t < ts; t++) {
      if(eq(paths.get(t), exct)) return docs.get(t);
    }
    return -1;
  }

  /**
   * Determines whether the given path is the path to a document directory.
   * @param path given path (will be normalized by adding a trailing slash)
   * @return path to a directory or not
   */
  synchronized boolean isDir(final byte[] path) {
    final byte[] pa = concat(path, SLASH);
    for(final byte[] b : paths()) if(startsWith(b, pa)) return true;
    return false;
  }

  /**
   * Adds the database paths for the child documents of the given path to the given map.
   * @param path path
   * @param dir returns directories instead of files
   * @param tbm map; values will be {@code false} to indicate documents
   */
  synchronized void children(final byte[] path, final boolean dir,
      final TokenBoolMap tbm) {

    final String pth = MetaData.normPath(string(path));
    if(pth == null) return;

    // normalize root path
    byte[] root = token(pth);
    if(root.length != 0) root = concat(root, SLASH);

    final IntList docs = docs();
    final int ds = docs.size();
    for(int d = 0; d < ds; d++) {
      byte[] np = data.text(docs.get(d), true);
      if(startsWith(np, root)) {
        np = substring(np, root.length, np.length);
        final int i = indexOf(np, SLASH);
        // no more slashes means this must be a leaf
        if(!dir && i == -1) tbm.put(np, false);
        else if(dir && i >= 0) tbm.put(substring(np, 0, i), false);
      }
    }
  }

  /**
   * Returns the first position matching the specified path
   * (might equal the array size).
   * @param v value to be found
   * @return position
   */
  private int find(final byte[] v) {
    // binary search
    final TokenList paths = paths();
    final int[] po = pathOrder;
    int l = 0, h = po.length - 1;
    while(l <= h) {
      int m = l + h >>> 1;
      final int c = diff(paths.get(po[m]), v);
      if(c == 0) {
        // find first entry
        while(m > 0 && eq(paths.get(po[m - 1]), v)) --m;
        return m;
      }
      if(c < 0) l = m + 1;
      else h = m - 1;
    }
    return l;
  }

  /**
   * Returns the normalized index path representation for the specified path.
   * @param path input path (without leading slash)
   * @return canonical path
   */
  private static byte[] normalize(final byte[] path) {
    return concat(SLASH, Prop.CASE ? path : lc(path));
  }
}
