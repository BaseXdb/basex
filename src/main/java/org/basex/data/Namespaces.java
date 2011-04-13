package org.basex.data;

import static org.basex.core.Text.*;
import static org.basex.data.DataText.*;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.basex.io.DataInput;
import org.basex.io.DataOutput;
import org.basex.io.IO;
import org.basex.util.Table;
import org.basex.util.Token;
import org.basex.util.TokenBuilder;
import org.basex.util.TokenList;
import org.basex.util.TokenObjMap;
import org.basex.util.TokenSet;

/**
 * This class contains the namespaces of a database.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public final class Namespaces {
  /** Namespace stack. */
  private final int[] uriStack = new int[IO.MAXHEIGHT];
  /** Prefixes. */
  private final TokenSet pref;
  /** URIs. */
  private final TokenSet uri;
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
    current = new NSNode(-1);
    root = current;
    pref = new TokenSet();
    uri = new TokenSet();
  }

  /**
   * Constructor, specifying an input stream.
   * @param in input stream
   * @throws IOException I/O exception
   */
  Namespaces(final DataInput in) throws IOException {
    pref = new TokenSet(in);
    uri = new TokenSet(in);
    current = new NSNode(in, null);
    root = current;
  }

  /**
   * Writes the namespaces to disk.
   * @param out output stream
   * @throws IOException I/O exception
   */
  void write(final DataOutput out) throws IOException {
    pref.write(out);
    uri.write(out);
    current.write(out);
  }

  /**
   * Adds the specified namespace to the namespace structure
   * and changes the root node. Needed for building the namespace structure.
   * @param p prefix
   * @param u uri
   * @param pre pre value
   * @return the new NSNode if a new one has been created, or null otherwise
   */
  public NSNode add(final byte[] p, final byte[] u, final int pre) {
    NSNode newNode = null;
    // after open() -call, newns==false
    if(!newns) {
      newNode = new NSNode(pre);
      current = current.add(newNode);
      newns = true;
    }
    final int k = addPref(p);
    final int v = addURI(u);
    current.add(k, v);
    if(p.length == 0) uriStack[uriL] = v;
    return newNode;
  }

  /**
   * Opens an element. Needed for building the namespace structure.
   * @return true if a new namespace has been added
   */
  public boolean open() {
    uriStack[uriL + 1] = uriStack[uriL];
    ++uriL;
    final boolean n = newns;
    newns = false;
    return n;
  }

  /**
   * Closes a node. Needed for building the namespace structure.
   * @param pre current pre value
   */
  public void close(final int pre) {
    while(current.pre >= pre && current.par != null)
      current = current.par;
    uriStack[--uriL] = uriStack[uriL - 1];
  }

  /**
   * Returns the namespace uri reference for the specified name,
   * or 0 if namespace cannot be found.
   * @param n tag/attribute name
   * @param elem element flag
   * @return namespace
   */
  public int uri(final byte[] n, final boolean elem) {
    if(uri.size() == 0) return 0;
    final byte[] pr = Token.pref(n);
    int u = elem ? uriStack[uriL] : 0;
    if(pr.length != 0) u = uri(pr, current);
    return u;
  }

  // Requesting Namespaces ====================================================

  /**
   * Returns the number of uri references.
   * @return number of uri references
   */
  public int size() {
    return uri.size();
  }

  /**
   * Returns the default global namespace of the database, or {@code null}
   * if several default namespaces are defined.
   * @return global default namespace
   */
  public byte[] globalNS() {
    // no namespaces defined: default namespace is empty
    if(current.size == 0) return Token.EMPTY;
    // more than one namespace defined: skip test
    if(current.size > 1) return null;
    // check namespaces of first child
    final NSNode n = current.ch[0];
    // namespace has more children; skip traversal
    if(n.size != 0 || n.pre != 1) return null;
    // loop through all globally defined namespaces
    for(int i = 0; i < n.vals.length; i += 2) {
      // return default namespace found
      if(pref.key(n.vals[i]).length == 0) return uri.key(n.vals[i + 1]);
    }
    return null;
  }

  /**
   * Returns the specified namespace uri.
   * @param id namespace uri reference
   * @return prefix
   */
  public byte[] uri(final int id) {
    return uri.key(id);
  }

  /**
   * Returns the namespace URI reference for the specified QName and pre value.
   * @param name tag/attribute name
   * @param pre pre value
   * @return namespace URI reference or 0 if no namespace was found
   */
  public int uri(final byte[] name, final int pre) {
    return uri(Token.pref(name), current.find(pre));
  }

  /**
   * Returns the specified prefix.
   * @param id prefix reference
   * @return prefix
   */
  byte[] pref(final int id) {
    return pref.key(id);
  }

  /**
   * Returns the prefix and URI references for the specified pre value.
   * @param pre pre value
   * @return namespace references
   */
  int[] get(final int pre) {
    return current.find(pre).vals;
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
    final int i = pref.id(pr);
    if(i == 0) return 0;

    NSNode n = nd;
    while(n != null) {
      final int u = n.uri(i);
      if(u != 0) return u;
      n = n.par;
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
    if(nd.pre == pre) nd = nd.par;
    while(nd != root) {
      nd.delete(pre, size);
      nd = nd.par;
    }
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

    final int k = addPref(p);
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
    return Math.abs(uri.add(u));
  }

  /**
   * Adds the specified prefix.
   * @param p prefix to be added
   * @return reference
   */
  private int addPref(final byte[] p) {
    return Math.abs(pref.add(p));
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
    uriStack[uriL] = uriI;
    // remind uri before insert of first node n to connect siblings of n to
    // according namespace
    uriStack[uriL - 1] = uriI;
    setRoot(n);
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
    if(current.size == 0) return Token.EMPTY;

    final Table t = new Table();
    t.header.add(TABLEID);
    t.header.add(TABLEPRE);
    t.header.add(TABLEDIST);
    t.header.add(TABLEPREF);
    t.header.add(TABLEURI);
    for(int i = 0; i < 3; ++i) t.align.add(true);
    table(t, current, s, e);
    return t.contents.size() != 0 ? t.finish() : Token.EMPTY;
  }

  /**
   * Adds the namespace structure for a node to the specified table.
   * @param t table
   * @param n namespace node
   * @param s start pre value
   * @param e end pre value
   */
  private void table(final Table t, final NSNode n, final int s, final int e) {
    for(int i = 0; i < n.vals.length; i += 2) {
      if(n.pre < s || n.pre > e) continue;
      final TokenList tl = new TokenList();
      tl.add(n.vals[i + 1]);
      tl.add(n.pre);
      tl.add(n.pre - n.par.pre);
      tl.add(pref.key(n.vals[i]));
      tl.add(uri.key(n.vals[i + 1]));
      t.contents.add(tl);
    }
    for(int i = 0; i < n.size; i++) table(t, n.ch[i], s, e);
  }

  /**
   * Returns namespace information.
   * @return info string
   */
  public byte[] info() {
    final TokenObjMap<TokenList> map = new TokenObjMap<TokenList>();
    info(map, current);
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
    for(int i = 0; i < n.vals.length; i += 2) {
      final byte[] key = uri.key(n.vals[i + 1]);
      final byte[] val = pref.key(n.vals[i]);
      TokenList old = map.get(key);
      if(old == null) {
        old = new TokenList();
        map.add(key, old);
      }
      if(!old.contains(val)) old.add(val);
    }
    for(final NSNode c : n.ch) info(map, c);
  }

  /**
   * Returns a string representation of the namespaces.
   * @param s start pre value
   * @param e end pre value
   * @return string
   */
  public String toString(final int s, final int e) {
    return current.print(this, s, e);
  }

  @Override
  public String toString() {
    return toString(0, Integer.MAX_VALUE);
  }

  /**
   * Updates the pre values of all NSNodes on the following axis after a
   * structural update at location pre.
   * @param pre update location
   * @param ms size of inserted/deleted node
   * @param insert true if insert operation, false if delete
   * @param newNodes new NSNodes that have been added as part of delete/insert
   */
  public void updatePreValues(final int pre, final int ms, final boolean insert,
      final Set<NSNode> newNodes) {
    updateNodePre(root, pre, ms, insert, newNodes != null ? newNodes :
      new HashSet<NSNode>());
  }

  /**
   * Updates the pre values of all NSNodes on the following axis after a
   * structural update at location pre.
   * @param n current namespace node which is updated if necessary
   * @param pre update location
   * @param ms size of inserted/deleted node
   * @param insert true if insert operation, false if delete
   * @param newNodes new NSNodes that have been added as part of delete/insert
   */
  private void updateNodePre(final NSNode n, final int pre, final int ms,
      final boolean insert, final Set<NSNode> newNodes) {
    if(!newNodes.contains(n) && n.pre >= (insert ? pre : pre + ms))
      n.pre += insert ? ms : ms * -1;
    for(int c = 0; c < n.size; c++) updateNodePre(n.ch[c], pre, ms, insert,
        newNodes);
  }
}
