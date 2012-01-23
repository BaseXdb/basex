package org.basex.index;

import static org.basex.util.Token.*;

import java.io.IOException;
import java.util.Locale;

import org.basex.core.Prop;
import org.basex.data.Data;
import org.basex.data.MetaData;
import org.basex.io.in.DataInput;
import org.basex.io.out.DataOutput;
import org.basex.util.Array;
import org.basex.util.Util;
import org.basex.util.hash.TokenIntMap;
import org.basex.util.list.IntList;
import org.basex.util.list.TokenList;

/**
 * <p>This index contains references to all document nodes in a database.
 * The document nodes are incrementally updated.</p>
 *
 * <p>If updates are performed, the document paths and the pre/path mapping
 * variables are discarded, as their update would be more expensive in numerous
 * cases (e.g. when bulk insertions of new documents are performed). A tree
 * structure could be introduced to offer better general performance.</p>
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class DocIndex implements Index {
  /** Data reference. */
  private final Data data;
  /** Pre values of document nodes. */
  private IntList docs;
  /** Sorted document paths. */
  private TokenList paths;
  /** Initial and temporal list which holds the pre value of all documents.
   * will be deleted after the document index has been called for first time. */
  private IntList tmp;

  /** Ordered path indexes. */
  private int[] pathorder;
  /** Path order valid. */
  private boolean ordered;

  /**
   * Constructor.
   * @param d data reference
   */
  public DocIndex(final Data d) {
    data = d;
  }

  /**
   * Reads the document index.
   * @param in input stream
   * @throws IOException I/O exception
   */
  public synchronized void read(final DataInput in) throws IOException {
    tmp = in.readDiffs();
  }

  /**
   * Writes the document index.
   * @param out output stream
   * @throws IOException I/O exception
   */
  public void write(final DataOutput out) throws IOException {
    out.writeDiffs(docs());
  }

  /**
   * Returns the pre values of all document nodes.
   * A single dummy node is returned if the database is empty.
   * @return document nodes
   */
  public synchronized IntList docs() {
    if(docs == null) init(tmp);
    return docs;
  }

  /**
   * Initializes the document index.
   */
  public synchronized void init() {
    init(null);
  }

  /**
   * Initializes the document index. If the given argument is null, the
   * PRE values are determined via scanning the table.
   * @param pres sorted document pre values, may be null
   */
  private synchronized void init(final IntList pres) {
    // document PRE values
    if(pres == null) {
      docs = new IntList();
      final int is = data.meta.size;
      for(int i = 0; i < is;) {
        final int k = data.kind(i);
        if(k == Data.DOC) docs.add(i);
        i += data.size(i, k);
      }
    } else
      docs = pres;

    // document paths
    final int ds = docs.size();
    paths = new TokenList(ds);
    for(int d = 0; d < ds; d++) {
      final byte[] txt = data.text(docs.get(d), true);
      paths.add(concat(SLASH, Prop.WIN ? lc(txt) : txt));
    }

    data.meta.dirty = true;
  }

  /**
   * Adds entries to the index and updates subsequent nodes.
   * @param pre insertion position
   * @param d data reference to be inserted
   */
  public void insert(final int pre, final Data d) {
    // find all document nodes
    final int dsize = d.meta.size;
    final IntList pres = new IntList();
    for(int dpre = 0; dpre < dsize;) {
      final int k = d.kind(dpre);
      if(k == Data.DOC) pres.add(pre + dpre);
      dpre += d.size(dpre, k);
    }

    // init paths before modifying docs, otherwise paths would
    // be larger than docs after insert
    final int[] presA = pres.toArray();
    final IntList il = docs();
    int i = il.sortedIndexOf(pre);
    if(i < 0) i = -i - 1;
    il.insert(i, presA);
    il.move(dsize, i + pres.size());

    final byte[][] t = new byte[presA.length][];
    for(int j = 0; j < t.length; j++) {
      // substract pre to retrieve paths from given data instance
      final byte[] b = d.text(presA[j] - pre, true);
      t[j] = concat(SLASH, Prop.WIN ? lc(b) : b);
    }
    paths.insert(i, t);

    ordered = false;
  }

  /**
   * Deletes the specified entry and updates subsequent nodes.
   * @param pre pre value
   * @param size number of deleted nodes
   */
  public void delete(final int pre, final int size) {
    // init paths before modifying docs, otherwise paths would
    // be smaller than docs after delete
    final IntList il = docs();
    int i = il.sortedIndexOf(pre);
    final boolean found = i >= 0;
    if(i < 0) i = -i - 1;
    else il.delete(i);
    il.move(-size, i);

    if(!found) return;
    paths.delete(i);

    ordered = false;
  }

  /**
   * Updates the index after a document has been renamed.
   * @param pre pre value of updated document
   * @param value new name
   */
  public void rename(final int pre, final byte[] value) {
    docs();

    paths.set(docs.sortedIndexOf(pre),
        concat(SLASH, Prop.WIN ? lc(value) : value));

    ordered = false;
  }

  /**
   * Replaces entries in the index.
   * @param pre insertion position
   * @param size number of deleted nodes
   * @param d data reference to be copied
   */
  public void replace(final int pre, final int size, final Data d) {
    delete(pre, size);
    insert(pre, d);
  }

  /**
   * Returns the pre values of all document nodes matching the specified path.
   * Exact || prefix match!
   * @param path input path
   * @return root nodes
   */
  public synchronized IntList docs(final String path) {
    // invalid path, or no documents: return empty list
    final String pth = MetaData.normPath(path);
    if(pth == null || data.empty()) return new IntList(0);

    // empty path: return all documents
    final IntList doc = docs();
    if(pth.isEmpty()) return doc;

    // normalize paths
    final byte[] exct = concat(SLASH, Prop.WIN ? lc(token(pth)) : token(pth));
    final byte[] pref = concat(exct, SLASH);

    // relevant paths: exact hits and prefixes
    final IntList il = new IntList();
    /* could be optimized for future access by sorting the paths first
     * and then accessing only the relevant paths. Sorting might slow down
     * bulk operations like insert/delete/replace though.
     */
    for(int p = 0; p < paths.size(); p++) {
      final byte[] b = paths.get(p);
      if(eq(b, exct) || startsWith(b, pref))
        il.add(doc.get(p));
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
  public synchronized int doc(final String path, final boolean sort) {
    // invalid or empty path, or no documents: return -1
    final String pth = MetaData.normPath(path);
    if(pth == null || pth.isEmpty() || data.empty()) return -1;

    // normalize paths
    final byte[] exct = concat(SLASH, Prop.WIN ? lc(token(pth)) : token(pth));

    // relevant paths: exact match
    if(sort) {
      createOrder();
      final int p = find(exct);
      return p > -1 && p < paths.size() && eq(paths.get(pathorder[p]), exct) ?
          docs.get(pathorder[p]) : -1;
    }

    for(int i = 0; i < paths.size(); i++)
      if(eq(paths.get(i), exct)) return docs.get(i);

    return -1;
  }

  /**
   * Returns the references to all binary files matching the specified path.
   * @param path input path
   * @return root nodes
   */
  public synchronized TokenList files(final String path) {
    final TokenList tl = new TokenList();
    final String np = MetaData.normPath(path);
    if(np == null) return tl;

    final String exct = Prop.WIN ? np.toLowerCase(Locale.ENGLISH) : np;
    final String pref = exct + '/';
    for(final String f : data.meta.binaries().descendants()) {
      final String lc = Prop.WIN ? f.toLowerCase(Locale.ENGLISH) : f;
      if(exct.isEmpty() || lc.equals(exct) || lc.startsWith(pref)) tl.add(f);
    }
    return tl.sort(!Prop.WIN);
  }

  /**
   * Returns the first position matching the specified path
   * (might equal the array size).
   * @param v value to be found
   * @return position
   */
  private int find(final byte[] v) {
    // binary search
    int l = 0, h = pathorder.length - 1;
    while(l <= h) {
      int m = l + h >>> 1;
      final int c = diff(paths.get(pathorder[m]), v);
      if(c == 0) {
        // find first entry
        while(m > 0 && eq(paths.get(pathorder[m - 1]), v)) --m;
        return m;
      }
      if(c < 0) l = m + 1;
      else h = m - 1;
    }
    return l;
  }

  /**
   * Sorts the document paths.
   */
  private void createOrder() {
    if(ordered) return;

    pathorder = Array.createOrder(paths.toArray(), false, true);
    ordered = true;
  }

  /**
   * Determines whether the given path is the path to a document directory.
   * @param path given path (must be normalized, means one leading but
   * no trailing slash.
   * @return path to a directory or not
   */
  public synchronized boolean isDir(final byte[] path) {
    if(path == null || data.empty()) return false;

    final byte[] pa = concat(path, SLASH);
    for(final byte[] b : paths) if(startsWith(b, pa)) return true;
    return false;
  }

  /**
   * Returns the child documents for the given path.
   * @param path path
   * @param docsOnly search only for documents not document directories
   * @return child paths
   */
  public synchronized byte[][] children(final byte[] path,
      final boolean docsOnly) {

    final String pth = MetaData.normPath(string(path));
    if(pth == null || data.empty()) return new byte[][] {};

    final TokenList tl = new TokenList();
    // normalize path to one leading + one trailing slash!
    byte[] tp = concat(SLASH, token(pth));
    // if the given path is the root, don't add a trailing slash
    if(pth.length() > 0) tp = concat(tp, SLASH);
    for(final byte[] to : paths) {
      if(startsWith(to, tp)) {
        final byte[] toAdd = substring(to, tp.length, to.length);
        final int i = indexOf(toAdd, SLASH);
        // no more slashes means this must be a leaf
        if(docsOnly && i == -1) tl.add(toAdd);
        else if(!docsOnly && i >= 0) tl.add(split(toAdd, '/')[0]);
      }
    }
    return tl.toArray();
  }

  // Inherited methods ========================================================

  @Override
  public void close() { }

  @Override
  public IndexIterator iter(final IndexToken token) {
    throw Util.notexpected();
  }

  @Override
  public int count(final IndexToken token) {
    throw Util.notexpected();
  }

  @Override
  public byte[] info() {
    throw Util.notexpected();
  }

  @Override
  public TokenIntMap entries(final byte[] prefix) {
    throw Util.notexpected();
  }
}
