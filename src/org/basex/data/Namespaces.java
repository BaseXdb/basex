package org.basex.data;

import static org.basex.Text.*;
import static org.basex.data.DataText.*;
import static org.basex.util.Token.*;
import java.io.IOException;
import org.basex.io.DataInput;
import org.basex.io.DataOutput;
import org.basex.io.PrintOutput;
import org.basex.util.Map;
import org.basex.util.Set;
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
   * Default Constructor.
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
   * Returns the namespace for the specified qname.
   * @param n tag/attribute name
   * @return namespace
   */
  public int get(final byte[] n) {
    final byte[] pre = pre(n);
    return pre.length == 0 ? 0 : ns(pre, root);
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
    return ns(pre(n), root.find(p));
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
   * Returns the namespace for the specified qname.
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
   * Prints the namespace structure to the specified output stream.
   * @param out output stream
   * @param s space for pre value
   * @param all print all namespaces or just the root entries
   * @throws IOException I/O exception
   */
  public void print(final PrintOutput out, final int s, final boolean all)
      throws IOException {

    if(root.ch.length == 0) return;
    out.print(s, token(TABLEPRE));
    out.print(s + 1, token(TABLEDIST));
    out.print(' ');
    out.print(token(TABLEPREF), 11);
    out.println(token(TABLEURI));
    print(out, s, root, all);
    out.print(NL);
  }

  /**
   * Prints the namespace structure to the specified node.
   * @param out output stream
   * @param s space for pre value
   * @param n namespace node
   * @param all print all namespaces or just the root entries
   * @throws IOException I/O exception
   */
  private void print(final PrintOutput out, final int s, final NSNode n,
      final boolean all) throws IOException {

    final byte[] quote = { '"' };
    for(int i = 0; i < n.key.length; i++) {
      out.print(s, token(n.pre));
      out.print(s + 1, token(n.pre - n.par.pre));
      out.print(' ');
      out.print(concat(quote, keys[n.key[i]], quote), 11);
      out.print(keys[n.val[i]]);
      out.println(" (" + n.val[i] + ")");
    }
    if(all || n.key.length == 0) {
      for(final NSNode c : n.ch) print(out, s, c, all);
    }
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
      if(key.size > 1 || key.list[0].length != 0) {
        if(key.size != 1) tb.add("(");
        for(int k = 0; k < key.size; k++) {
          if(k != 0) tb.add(", ");
          tb.add(key.list[k]);
        }
        if(key.size != 1) tb.add(")");
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
