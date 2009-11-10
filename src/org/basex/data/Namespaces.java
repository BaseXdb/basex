package org.basex.data;

import static org.basex.core.Text.*;
import static org.basex.data.DataText.*;
import static org.basex.util.Token.*;
import java.io.IOException;
import org.basex.io.DataInput;
import org.basex.io.DataOutput;
import org.basex.util.Map;
import org.basex.util.Set;
import org.basex.util.StringList;
import org.basex.util.Table;
import org.basex.util.TokenBuilder;
import org.basex.util.TokenList;

/**
 * This class organizes the namespaces of a database.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public final class Namespaces extends Set {
  /** Root node. */
  private NSNode root;
  /** Current node. */
  private NSNode tmp;

  /**
   * Default constructor.
   */
  public Namespaces() {
    root = new NSNode();
  }

  /**
   * Constructor, specifying an input stream.
   * @param in input stream
   * @throws IOException I/O exception
   */
  public Namespaces(final DataInput in) throws IOException {
    keys = in.readBytesArray();
    next = in.readNums();
    bucket = in.readNums();
    size = in.readNum();
    root = new NSNode(in, null);
  }

  // === Storing Namespaces ===================================================

  /**
   * Adds the specified namespace.
   * @param p namespace prefix
   * @param u namespace uri
   * @return value id
   */
  public int add(final byte[] p, final byte[] u) {
    if(tmp == null) tmp = new NSNode();
    final int k = Math.abs(add(p));
    final int v = Math.abs(add(u));
    tmp.add(k, v);
    return v;
  }

  /**
   * Opens a node.
   * @param p current pre value
   * @return true if new namespaces have been added
   */
  public boolean open(final int p) {
    if(tmp == null) return false;
    tmp.par = root;
    tmp.pre = p;
    root.add(tmp);
    root = tmp;
    tmp = null;
    return true;
  }

  /**
   * Returns the namespace offset for the specified qname,
   * or 0 if namespace cannot be found.
   * @param n tag/attribute name
   * @return namespace
   */
  public int get(final byte[] n) {
    final byte[] pref = pref(n);
    return pref.length == 0 ? 0 : ns(pref, root);
  }

  /**
   * Writes the namespaces to disk.
   * @param out output stream
   * @throws IOException I/O exception
   */
  public void write(final DataOutput out) throws IOException {
    out.writeBytesArray(keys);
    out.writeNums(next);
    out.writeNums(bucket);
    out.writeNum(size);
    root.finish(out);
  }

  /**
   * Closes a node.
   * @param p current pre value
   */
  public void close(final int p) {
    while(root.pre >= p) root = root.par;
  }

  // === Requesting Namespaces ================================================

  /**
   * Returns the namespace for the specified name and pre value.
   * @param n tag/attribute name
   * @param p pre value
   * @return namespace reference or 0 if no namespace was found
   */
  public int get(final byte[] n, final int p) {
    return ns(pref(n), root.find(p));
  }

  /**
   * Returns the namespace keys and values for the specified pre value.
   * @param p pre value
   * @return namespace reference or 0 if no namespace was found
   */
  public int[] get(final int p) {
    final NSNode node = root.find(p);
    final int[] ns = new int[node.key.length << 1];
    for(int n = 0; n < ns.length; n += 2) {
      ns[n] = node.key[n >> 1];
      ns[n + 1] = node.val[n >> 1];
    }
    return ns;
  }

  /**
   * Returns the namespace for the specified prefix,
   * or 0 if namespace cannot be found.
   * @param p prefix
   * @param node node to start with
   * @return namespace
   */
  private int ns(final byte[] p, final NSNode node) {
    if(eq(XML, p)) return 0;

    NSNode nd = node;
    final int k = id(p);
    if(k == 0) return 0;
    while(nd != null) {
      final int i = nd.get(k);
      if(i != 0) return i;
      nd = nd.par;
    }
    return 0;
  }

  // === Printing Namespaces ==================================================

  /**
   * Returns a tabular representation of the namespaces.
   * @param all print all namespaces or just the root entries
   * @return namespaces
   */
  public byte[] table(final boolean all) {
    if(root.ch.length == 0) return null;

    final Table t = new Table();
    t.header.add(TABLEPRE);
    t.header.add(TABLEDIST);
    t.header.add(TABLEPREF);
    t.header.add(TABLEURI);
    for(int i = 0; i < 2; i++) t.align.add(true);
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
    for(int i = 0; i < n.key.length; i++) {
      StringList sl = new StringList();
      sl.add(n.pre);
      sl.add(n.pre - n.par.pre);
      sl.add("\"" + string(keys[n.key[i]]) + "\"");
      sl.add(string(keys[n.val[i]]) + " (" + n.val[i] + ")");
      t.contents.add(sl);
    }
    if(all || n.key.length == 0) for(final NSNode c : n.ch) table(t, c, all);
  }

  /**
   * Prints some namespace info.
   * @return statistics string
   */
  public byte[] info() {
    if(size() == 0) return EMPTY;

    final Map<TokenList> map = new Map<TokenList>();
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
   * Prints some namespace info.
   * @param map namespace map
   * @param n namespace node
   */
  public void info(final Map<TokenList> map, final NSNode n) {
    for(int i = 0; i < n.key.length; i++) {
      final byte[] key = keys[n.val[i]];
      final byte[] val = keys[n.key[i]];
      TokenList old = map.get(key);
      if(old == null) old = new TokenList();
      if(!old.contains(val)) old.add(val);
      map.add(key, old);
    }
    for(final NSNode c : n.ch) info(map, c);
  }
}
