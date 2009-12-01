package org.basex.data;

import static org.basex.core.Text.*;
import static org.basex.data.DataText.*;
import static org.basex.util.Token.*;
import java.io.IOException;
import org.basex.io.DataInput;
import org.basex.io.DataOutput;
import org.basex.util.ObjectMap;
import org.basex.util.TokenSet;
import org.basex.util.StringList;
import org.basex.util.Table;
import org.basex.util.TokenBuilder;
import org.basex.util.TokenList;

/**
 * This class contains the namespaces of a database.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public final class Namespaces extends TokenSet {
  /** Root node. */
  private NSNode root;
  /** Current node. */
  private NSNode tmp;

  // Building Namespaces ======================================================

  /**
   * Default constructor.
   */
  public Namespaces() {
    root = new NSNode();
  }

  /**
   * Opens a node.
   * @param pre pre value
   * @return true if a new namespace has been added
   */
  public boolean open(final int pre) {
    if(tmp == null) return false;
    tmp.par = root;
    tmp.pre = pre;
    root.add(tmp);
    root = tmp;
    tmp = null;
    return true;
  }

  /**
   * Closes a node.
   * @param pre current pre value
   */
  public void close(final int pre) {
    while(root.pre >= pre) root = root.par;
  }

  /**
   * Adds the specified namespace.
   * @param p prefix
   * @param u uri
   * @return uri reference
   */
  public int add(final byte[] p, final byte[] u) {
    if(tmp == null) tmp = new NSNode();
    final int k = Math.abs(add(p));
    final int v = Math.abs(add(u));
    tmp.add(k, v);
    return v;
  }

  /**
   * Returns the namespace URI reference for the specified QName,
   * or 0 if namespace cannot be found.
   * @param n tag/attribute name
   * @return namespace
   */
  public int uri(final byte[] n) {
    final byte[] pref = pref(n);
    return pref.length == 0 ? 0 : ns(pref, root);
  }

  // Managing Namespaces ======================================================

  /**
   * Constructor, specifying an input stream.
   * @param in input stream
   * @throws IOException I/O exception
   */
  Namespaces(final DataInput in) throws IOException {
    keys = in.readBytesArray();
    next = in.readNums();
    bucket = in.readNums();
    size = in.readNum();
    root = new NSNode(in, null);
  }

  /**
   * Writes the namespaces to disk.
   * @param out output stream
   * @throws IOException I/O exception
   */
  void write(final DataOutput out) throws IOException {
    out.writeBytesArray(keys);
    out.writeNums(next);
    out.writeNums(bucket);
    out.writeNum(size);
    root.write(out);
  }

  // Requesting Namespaces ====================================================

  /**
   * Returns the prefix and URI references for the specified pre value.
   * @param pre pre value
   * @return namespace references
   */
  int[] get(final int pre) {
    return root.find(pre).vals;
  }

  /**
   * Returns the namespace URI reference for the specified QName and pre value.
   * @param name tag/attribute name
   * @param pre pre value
   * @return namespace URI reference or 0 if no namespace was found
   */
  public int uri(final byte[] name, final int pre) {
    return ns(pref(name), root.find(pre));
  }

  /**
   * Returns the namespace URI reference for the specified prefix and node,
   * or 0 if namespace cannot be found.
   * @param pref prefix
   * @param node node to start with
   * @return namespace
   */
  private int ns(final byte[] pref, final NSNode node) {
    if(eq(XML, pref)) return 0;
    final int k = id(pref);
    if(k == 0) return 0;

    NSNode nd = node;
    while(nd != null) {
      final int i = nd.uri(k);
      if(i != 0) return i;
      nd = nd.par;
    }
    return 0;
  }

  /**
   * Deletes the specified number of entries from the namespace structure.
   * @param pre pre value of the first node to delete
   * @param nr number of entries to be deleted
   */
  public void delete(final int pre, final int nr) {
    NSNode nd = root.find(pre);
    if(nd.pre == pre) nd = nd.par;
    if(nd == null) root = new NSNode();

    while(nd != null) {
      nd.delete(pre, nr);
      nd = nd.par;
    }
  }

  /**
   * Adds a namespace for the specified pre value.
   * @param p prefix
   * @param u uri
   * @param pre pre value
   */
  public void add(final byte[] p, final byte[] u, final int pre) {
    final int k = Math.abs(add(p));
    final int v = Math.abs(add(u));
    final NSNode nd = root.find(pre);

    if(nd.pre != pre) {
      final NSNode t = new NSNode();
      t.pre = pre;
      t.add(k, v);
      t.par = nd;
      nd.add(t);
    } else {
      nd.add(k, v);
    }
  }

  // Printing Namespaces ======================================================

  /**
   * Returns a tabular representation of the namespaces.
   * @param all print all namespaces or just the root entries
   * @return namespaces
   */
  public byte[] table(final boolean all) {
    if(root.ch.length == 0) return null;
    //System.out.println(this);

    final Table t = new Table();
    t.header.add(TABLEID);
    t.header.add(TABLEPRE);
    t.header.add(TABLEDIST);
    t.header.add(TABLEPREF);
    t.header.add(TABLEURI);
    for(int i = 0; i < 3; i++) t.align.add(true);
    table(t, root, all);
    return t.finish();
  }

  /**
   * Adds the namespace structure for a node to the specified table.
   * @param t table
   * @param n namespace node
   * @param all print all namespaces or just the root entries
   */
  private void table(final Table t, final NSNode n, final boolean all) {
    for(int i = 0; i < n.vals.length; i += 2) {
      final StringList sl = new StringList();
      sl.add(n.vals[i + 1]);
      sl.add(n.pre);
      sl.add(n.pre - n.par.pre);
      sl.add(string(keys[n.vals[i]]));
      sl.add(string(keys[n.vals[i + 1]]));
      t.contents.add(sl);
    }
    if(all || n.vals.length == 0) for(final NSNode c : n.ch) table(t, c, all);
  }

  /**
   * Returns namespace information.
   * @return info string
   */
  public byte[] info() {
    final ObjectMap<TokenList> map = new ObjectMap<TokenList>();
    info(map, root);
    final TokenBuilder tb = new TokenBuilder();
    for(final byte[] val : map.keys()) {
      tb.add("  ");
      final TokenList key = map.get(val);
      key.sort(false);
      final int ks = key.size();
      if(ks > 1 || key.get(0).length != 0) {
        if(key.size() != 1) tb.add("(");
        for(int k = 0; k < ks; k++) {
          if(k != 0) tb.add(", ");
          tb.add(key.get(k));
        }
        if(ks != 1) tb.add(")");
        tb.add(" = ");
      }
      tb.add("\"%\"" + NL, val);
    }
    return tb.finish();
  }

  /**
   * Adds namespace information for the specified node to a map.
   * @param map namespace map
   * @param n namespace node
   */
  private void info(final ObjectMap<TokenList> map, final NSNode n) {
    for(int i = 0; i < n.vals.length; i += 2) {
      final byte[] key = keys[n.vals[i + 1]];
      final byte[] val = keys[n.vals[i]];
      TokenList old = map.get(key);
      if(old == null) {
        old = new TokenList();
        map.put(key, old);
      }
      if(!old.contains(val)) old.add(val);
      //map.put(key, old);
    }
    for(final NSNode c : n.ch) info(map, c);
  }

  @Override
  public String toString() {
    return root.print(this);
  }
}
