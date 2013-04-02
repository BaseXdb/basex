package org.basex.data;

import static org.basex.core.Text.*;
import static org.basex.data.DataText.*;

import java.io.*;
import java.util.*;

import org.basex.io.in.DataInput;
import org.basex.io.out.DataOutput;
import org.basex.util.*;
import org.basex.util.hash.*;
import org.basex.util.list.*;

/**
 * This class contains the namespaces of a database.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class Namespaces {
  /** Stack with references to default namespaces. */
  private final IntList defaults = new IntList();
  /** Namespace prefixes. */
  private final TokenSet prefs;
  /** Namespace URIs. */
  private final TokenSet uris;
  /** Root node. */
  final NSNode root;

  /** New namespace flag. */
  private boolean newns;
  /** Current level. Index starts at 1 to reserve an additional level for
   * XQUP insert operations. */
  private int uriL = 1;
  /** Current namespace node. */
  NSNode current;

  // Building Namespaces ======================================================

  /**
   * Empty constructor.
   */
  public Namespaces() {
    prefs = new TokenSet();
    uris = new TokenSet();
    root = new NSNode(-1);
    current = root;
  }

  /**
   * Constructor, specifying an input stream.
   * @param in input stream
   * @throws IOException I/O exception
   */
  Namespaces(final DataInput in) throws IOException {
    prefs = new TokenSet(in);
    uris = new TokenSet(in);
    root = new NSNode(in, null);
    current = root;
  }

  /**
   * Writes the namespaces to disk.
   * @param out output stream
   * @throws IOException I/O exception
   */
  void write(final DataOutput out) throws IOException {
    prefs.write(out);
    uris.write(out);
    root.write(out);
  }

  /**
   * Prepares the generation of new namespaces.
   */
  public void prepare() {
    final int nu = defaults.get(uriL);
    defaults.set(++uriL, nu);
    newns = false;
  }

  /**
   * Adds the specified namespace to the namespace structure of the current element.
   * @param p prefix
   * @param u uri
   * @param pre pre value
   * @return new NSNode if a new one has been created, or null otherwise
   */
  public NSNode add(final byte[] p, final byte[] u, final int pre) {
    NSNode newNode = null;
    if(!newns) {
      newNode = new NSNode(pre);
      current = current.add(newNode);
      newns = true;
    }
    final int k = addPrefix(p);
    final int v = addURI(u);
    current.add(k, v);
    if(p.length == 0) defaults.set(uriL, v);
    return newNode;
  }

  /**
   * Finishes the namespace generation for the current element.
   * @return true if new namespaces have been added
   */
  public boolean finish() {
    return newns;
  }

  /**
   * Closes a namespace node.
   * @param pre current pre value
   */
  public void close(final int pre) {
    while(current.pre >= pre && current.parent != null) current = current.parent;
    --uriL;
  }

  /**
   * Returns the namespace uri reference for the specified name,
   * or 0 if namespace cannot be found.
   * @param n tag/attribute name
   * @param elem element flag
   * @return namespace
   */
  public int uri(final byte[] n, final boolean elem) {
    if(uris.size() == 0) return 0;
    final byte[] pref = Token.prefix(n);
    int nu = elem ? defaults.get(uriL) : 0;
    if(pref.length != 0) nu = uri(pref, current);
    return nu;
  }

  // Requesting Namespaces ====================================================

  /**
   * Returns the number of uri references.
   * @return number of uri references
   */
  public int size() {
    /* returns the size of the uri container - if we delete nodes from
     * the namespace structure via delete(pre,s) the container size isn't
     * changed at all, as only NSNodes in the range pre,pre+s-1 are deleted.
     * COUNTERINTUITIVE?
     */
    return uris.size();
  }

  /**
   * Returns the default namespace of the database, or {@code null}
   * if several (default or prefixed) namespaces are defined.
   * @return global default namespace
   */
  public byte[] globalNS() {
    // no namespaces defined: default namespace is empty
    if(root.size == 0) return Token.EMPTY;
    // more than one namespace defined: skip test
    if(root.size > 1) return null;
    // check namespaces of first child
    final NSNode n = root.children[0];

    // namespace has more children; skip traversal
    if(n.size != 0 || n.pre != 1 || n.values.length != 2) return null;
    // return default namespace or null
    return prefs.key(n.values[0]).length == 0 ? uris.key(n.values[1]) : null;
  }

  /**
   * Returns the specified namespace uri.
   * @param id namespace uri reference
   * @return prefix
   */
  public byte[] uri(final int id) {
    return uris.key(id);
  }

  /**
   * Returns the namespace URI reference for the specified QName and pre value.
   * @param name tag/attribute name
   * @param pre pre value
   * @return namespace URI reference or 0 if no namespace was found
   */
  public int uri(final byte[] name, final int pre) {
    return uri(Token.prefix(name), current.find(pre));
  }

  /**
   * Deletes the specified namespace URI from the root node.
   * @param u namespace URI reference
   */
  public void delete(final byte[] u) {
    final int id = uris.id(u);
    if(id != 0) current.delete(id);
  }

  /**
   * Returns the specified prefix.
   * @param id prefix reference
   * @return prefix
   */
  byte[] prefix(final int id) {
    return prefs.key(id);
  }

  /**
   * Returns the prefix and URI references for the specified pre value.
   * @param pre pre value
   * @return namespace references
   */
  int[] get(final int pre) {
    return current.find(pre).values;
  }

  /**
   * Returns the namespace URI reference for the specified prefix and node,
   * or 0 if namespace cannot be found.
   * @param pr prefix
   * @param nd node to start with
   * @return namespace
   */
  private int uri(final byte[] pr, final NSNode nd) {
    if(Token.eq(Token.XML, pr)) return 0;
    final int id = prefs.id(pr);
    if(id == 0) return 0;

    NSNode n = nd;
    while(n != null) {
      final int u = n.uri(id);
      if(u != 0) return u;
      n = n.parent;
    }
    return 0;
  }

  // Updating Namespaces ======================================================

  /**
   * Deletes the specified number of entries from the namespace structure.
   * @param pre pre value of the first node to delete
   * @param size number of entries to be deleted
   */
  void delete(final int pre, final int size) {
    NSNode nd = current.find(pre);
    if(nd.pre == pre) nd = nd.parent;
    while(nd != null) {
      nd.delete(pre, size);
      nd = nd.parent;
    }
    delete(root, pre, size);
  }

  /**
   * Recursive shifting of pre values after delete operations.
   * @param n current namespace node which is updated if necessary
   * @param pre update location
   * @param ms size of inserted/deleted node
   */
  private static void delete(final NSNode n, final int pre, final int ms) {
    if(n.pre >= pre + ms) n.pre -= ms;
    for(int c = 0; c < n.size; c++) delete(n.children[c], pre, ms);
  }

  /**
   * Updates the pre values of all nodes on the following axis after a
   * structural update at location pre.
   * @param pre update location
   * @param ms size of inserted/deleted node
   * @param cache added nodes
   */
  void insert(final int pre, final int ms, final Set<NSNode> cache) {
    insert(root, pre, ms, cache);
  }

  /**
   * Recursive shifting of pre values after insert operations.
   * @param n current namespace node
   * @param pre update location
   * @param ms size of inserted/deleted node
   * @param cache added nodes
   */
  private static void insert(final NSNode n, final int pre, final int ms,
      final Set<NSNode> cache) {
    if(!cache.contains(n) && n.pre >= pre) n.pre += ms;
    for(int c = 0; c < n.size; c++) insert(n.children[c], pre, ms, cache);
  }

  /**
   * Adds a namespace for the specified pre value.
   * @param pre pre value
   * @param par parent
   * @param p prefix
   * @param u uri
   * @return uri reference
   */
  public int add(final int pre, final int par, final byte[] p, final byte[] u) {
    final NSNode nd = current.find(par);
    final NSNode t = new NSNode(pre);

    final int k = addPrefix(p);
    final int v = addURI(u);
    if(nd.pre == pre) {
      nd.add(k, v);
    } else {
      t.add(k, v);
      nd.add(t);
    }
    return v;
  }

  /**
   * Adds the specified namespace uri.
   * @param u namespace uri to be added
   * @return reference
   */
  public int addURI(final byte[] u) {
    return Math.abs(uris.add(u));
  }

  /**
   * Adds the specified prefix.
   * @param p prefix to be added
   * @return reference
   */
  private int addPrefix(final byte[] p) {
    return Math.abs(prefs.add(p));
  }

  /**
   * This is only called when a MemData instance is inserted. The
   * namespace node which is next on the ancestor axis of the insert location
   * is set as new root.
   * @param n nearest namespace node on ancestor axis
   * @param pre pre value to find nearest namespace node for
   */
  void setNearestRoot(final NSNode n, final int pre) {
    final int uriI = uri(Token.EMPTY, pre);
    defaults.set(uriL, uriI);
    // remind uri before insert of first node n to connect siblings of n to
    // according namespace
    defaults.set(uriL - 1, uriI);
    current = n;
  }

  /**
   * Setter for namespaces root node.
   * @param n new root
   */
  void setRoot(final NSNode n) {
    current = n;
  }

  // Printing Namespaces ======================================================

  /**
   * Returns a tabular representation of the namespaces.
   * @param s start pre value
   * @param e end pre value
   * @return namespaces
   */
  public byte[] table(final int s, final int e) {
    if(root.size == 0) return Token.EMPTY;

    final Table t = new Table();
    t.header.add(TABLEID);
    t.header.add(TABLEPRE);
    t.header.add(TABLEDIST);
    t.header.add(TABLEPREF);
    t.header.add(TABLEURI);
    for(int i = 0; i < 3; ++i) t.align.add(true);
    table(t, root, s, e);
    return t.contents.isEmpty() ? Token.EMPTY : t.finish();
  }

  /**
   * Adds the namespace structure for a node to the specified table.
   * @param t table
   * @param n namespace node
   * @param s start pre value
   * @param e end pre value
   */
  private void table(final Table t, final NSNode n, final int s, final int e) {
    for(int i = 0; i < n.values.length; i += 2) {
      if(n.pre < s || n.pre > e) continue;
      final TokenList tl = new TokenList();
      tl.add(n.values[i + 1]);
      tl.add(n.pre);
      tl.add(n.pre - n.parent.pre);
      tl.add(prefs.key(n.values[i]));
      tl.add(uris.key(n.values[i + 1]));
      t.contents.add(tl);
    }
    for(int i = 0; i < n.size; i++) table(t, n.children[i], s, e);
  }

  /**
   * Returns namespace information.
   * @return info string
   */
  public byte[] info() {
    final TokenObjMap<TokenList> map = new TokenObjMap<TokenList>();
    info(map, root);
    final TokenBuilder tb = new TokenBuilder();
    for(final byte[] val : map.keys()) {
      tb.add("  ");
      final TokenList key = map.get(val);
      key.sort(false);
      final int ks = key.size();
      if(ks > 1 || key.get(0).length != 0) {
        if(key.size() != 1) tb.add("(");
        for(int k = 0; k < ks; ++k) {
          if(k != 0) tb.add(", ");
          tb.add(key.get(k));
        }
        if(ks != 1) tb.add(")");
        tb.add(" = ");
      }
      tb.addExt("\"%\"" + NL, val);
    }
    return tb.finish();
  }

  /**
   * Adds namespace information for the specified node to a map.
   * @param map namespace map
   * @param n namespace node
   */
  private void info(final TokenObjMap<TokenList> map, final NSNode n) {
    for(int i = 0; i < n.values.length; i += 2) {
      final byte[] key = uris.key(n.values[i + 1]);
      final byte[] val = prefs.key(n.values[i]);
      TokenList old = map.get(key);
      if(old == null) {
        old = new TokenList();
        map.add(key, old);
      }
      if(!old.contains(val)) old.add(val);
    }
    for(final NSNode c : n.children) info(map, c);
  }

  /**
   * Returns a string representation of the namespaces.
   * @param s start pre value
   * @param e end pre value
   * @return string
   */
  public String toString(final int s, final int e) {
    return root.print(this, s, e);
  }

  @Override
  public String toString() {
    return toString(0, Integer.MAX_VALUE);
  }
}
