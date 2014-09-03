package org.basex.index.resource;

import static org.basex.data.DataText.*;
import static org.basex.util.Token.*;

import java.io.*;

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
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 * @author Lukas Kircher
 */
final class Docs {
  /** Data reference. */
  private final Data data;
  /** Pre values of document nodes (may be {@code null}).
   * This variable should always be requested via {@link #docs()}. */
  private IntList docList;
  /** Sorted document paths (may be {@code null}).
   * This variable should always be requested via {@link #paths()}. */
  private TokenList pathList;
  /** Ordered path indexes (may be {@code null}).
   * This variable should always be requested via {@link #order()}. */
  private int[] pathOrder;
  /** Dirty flag. */
  private boolean dirty;
  /** Indicates if a path index is available. */
  private boolean pathIndex;

  /**
   * Constructor.
   * @param data data reference
   */
  Docs(final Data data) {
    this.data = data;
  }

  /**
   * Reads the document index.
   * @param in input stream
   * @throws IOException I/O exception
   */
  synchronized void read(final DataInput in) throws IOException {
    docList = in.readDiffs();
    pathIndex = data.meta.dbfile(DATADOC).exists();
  }

  /**
   * Writes the document index.
   * @param out output stream
   * @throws IOException I/O exception
   */
  void write(final DataOutput out) throws IOException {
    out.writeDiffs(docs());
    if(dirty && pathIndex) {
      // write paths
      try(final DataOutput doc = new DataOutput(data.meta.dbfile(DATADOC))) {
        final TokenList paths = paths();
        doc.writeNum(paths.size());
        for(final byte[] path : paths) doc.writeToken(path);
      }
      dirty = false;
    }
  }

  /**
   * Returns the {@code pre} values of all document nodes.
   * @return document nodes (internal representation!)
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
      update();
      docList = il;
    }
    return docList;
  }

  /**
   * Returns the document paths, and initializes them if necessary.
   * @return document paths (internal representation!)
   */
  private synchronized TokenList paths() {
    if(pathList == null && pathIndex) {
      // try to read paths from disk
      try(final DataInput in = new DataInput(data.meta.dbfile(DATADOC))) {
        pathList = new TokenList(in.readTokens());
      } catch(final IOException ignore) { }
    }

    // generate paths
    if(pathList == null) {
      // paths have not been stored to disk yet; scan table
      final IntList docs = docs();
      final int ds = docs.size();
      final TokenList paths = new TokenList(ds);
      for(int d = 0; d < ds; d++) {
        paths.add(normalize(data.text(docs.get(d), true)));
      }
      pathIndex = true;
      pathList = paths;
      update();
    }
    return pathList;
  }

  /**
   * Returns the document path order, and initialize the array if necessary.
   * @return path order (internal representation!)
   */
  private synchronized int[] order() {
    if(pathOrder == null) pathOrder = Array.createOrder(paths().toArray(), false, true);
    return pathOrder;
  }

  /**
   * Adds entries to the index and updates subsequent nodes.
   * @param pre insertion position
   * @param clip data clip
   */
  void insert(final int pre, final DataClip clip) {
    // find all document nodes in the given data instance
    final IntList il = new IntList();
    final Data src = clip.data;
    for(int dpre = clip.start; dpre < clip.end;) {
      final int k = src.kind(dpre);
      if(k == Data.DOC) il.add(pre + dpre);
      dpre += src.size(dpre, k);
    }
    final int[] pres = il.finish();
    final int ps = pres.length;

    // find insertion offset
    final IntList docs = docs();
    int i = docs.sortedIndexOf(pre);
    if(i < 0) i = -i - 1;

    // insert paths from given data instance
    if(pathIndex) {
      final TokenList paths = paths();
      final byte[][] tmp = new byte[ps][];
      for(int t = 0; t < ps; t++) tmp[t] = normalize(clip.data.text(pres[t] - pre, true));
      paths.insert(i, tmp);
    }

    // insert pre values
    docs.insert(i, pres);
    // adjust pre values of following document nodes
    docs.incFrom(clip.size(), i + ps);

    update();
  }

  /**
   * Deletes the specified entry and updates subsequent nodes.
   * @param pre pre value
   * @param size number of deleted nodes
   */
  void delete(final int pre, final int size) {
    // find insertion offset
    final IntList docs = docs();
    int doc = docs.sortedIndexOf(pre);

    // pre value points to a document node...
    if(doc >= 0) {
      if(pathIndex) paths().deleteAt(doc);
      docs.deleteAt(doc);
    }

    // adjust pre values of following document nodes
    docs.incFrom(-size, doc < 0 ? -doc - 1 : doc);
    update();
  }

  /**
   * Updates the index after a document has been renamed.
   * @param pre pre value of updated document
   * @param value new name
   */
  void rename(final int pre, final byte[] value) {
    if(pathIndex) paths().set(docs().sortedIndexOf(pre), normalize(value));
    update();
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
   * Notifies the meta structures of an update and invalidates the indexes.
   */
  private synchronized void update() {
    pathOrder = null;
    data.meta.dirty = true;
    dirty = true;
  }

  /**
   * Returns the pre values of all document nodes matching the specified path.
   * @param path input path
   * @param exact exact (no prefix) matches
   * @return root nodes (internal representation!)
   */
  synchronized IntList docs(final String path, final boolean exact) {
    // invalid path, or no documents: return empty list
    final String pth = MetaData.normPath(path);
    if(pth == null) return new IntList(0);

    // empty path: return all documents
    final IntList docs = docs();
    if(pth.isEmpty()) return docs;

    // normalize paths
    byte[] exct = EMPTY, pref = normalize(token(pth));
    // check for explicit directory indicator
    if(!pth.endsWith("/")) {
      exct = pref;
      pref = concat(exct, SLASH);
    }

    // relevant paths: exact hits and prefixes
    final IntList il = new IntList();
    final TokenList paths = paths();
    final int ps = paths.size();
    for(int p = 0; p < ps; p++) {
      final byte[] pt = paths.get(p);
      if(eq(pt, exct) || !exact && startsWith(pt, pref)) il.add(docs.get(p));
    }
    return il.sort();
  }

  /**
   * Returns the pre value of a document node that matches the specified path.
   * @param path input path
   * @return pre value of document node, or {@code -1}
   */
  synchronized int doc(final String path) {
    // invalid or empty path, or no documents: return -1
    final String pth = MetaData.normPath(path);
    // find path; return -1 if path is empty or does not exist
    return pth == null || pth.isEmpty() ? -1 : Math.max(-1, find(normalize(token(pth))));
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
  synchronized void children(final byte[] path, final boolean dir, final TokenBoolMap tbm) {
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
   * Returns the pre value of the addressed resource.
   * @param path path to be found
   * @return pre value, or {@code -1}
   */
  private int find(final byte[] path) {
    // binary search
    final TokenList paths = paths();
    final int[] order = order();
    int l = 0, h = order.length - 1;
    while(l <= h) {
      int m = l + h >>> 1;
      final int c = diff(paths.get(order[m]), path);
      if(c == 0) return docs().get(m);
      if(c < 0) l = m + 1;
      else h = m - 1;
    }
    return -1;
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
