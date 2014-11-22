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
 * This class organizes the namespaces of a database.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class Namespaces {
  /** Stack with references to default namespaces. */
  private final IntList defaults = new IntList(2);
  /** Namespace prefixes. */
  private final TokenSet prefs;
  /** Namespace URIs. */
  private final TokenSet uris;
  /** Root node. */
  private final NSNode root;

  /** Indicates if new namespaces have been added for an XML node. */
  private boolean newns;
  /** Current level. Index starts at 1 (required by XQUF operations). */
  private int level = 1;
  /** Current namespace node. */
  private NSNode current;

  // Building Namespaces ================================================================

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
    final int nu = defaults.get(level);
    defaults.set(++level, nu);
    newns = false;
  }

  /**
   * Adds the specified namespace to the namespace structure of the current element.
   * @param pref prefix
   * @param uri uri
   * @param pre pre value
   * @return new NSNode if a new one has been created, {@code null} otherwise
   */
  public NSNode add(final byte[] pref, final byte[] uri, final int pre) {
    NSNode node = null;
    if(!newns) {
      node = new NSNode(pre);
      current.add(node);
      current = node;
      newns = true;
    }
    final int k = prefs.put(pref);
    final int v = uris.put(uri);
    current.add(k, v);
    if(pref.length == 0) defaults.set(level, v);
    return node;
  }

  /**
   * Closes a namespace node.
   * @param pre current pre value
   */
  public void close(final int pre) {
    while(current.pr >= pre && current.parent != null) current = current.parent;
    --level;
  }

  // Requesting Namespaces ==============================================================

  /**
   * Returns the size of the uri container.
   * Note that the container size does not change if namespaces are deleted.
   * This function is basically used to decide if there are any namespaces at all,
   * and if namespaces need to be considered in query optimizations.
   * @return number of uri references
   */
  public int size() {
    return uris.size();
  }

  /**
   * Returns the default namespace of the database or {@code null}
   * if several (default or prefixed) namespaces are defined.
   * @return global default namespace
   */
  public byte[] globalNS() {
    // no namespaces defined: default namespace is empty
    if(root.sz == 0) return Token.EMPTY;
    // more than one namespace defined: skip test
    if(root.sz > 1) return null;
    // check namespaces of first child
    final NSNode n = root.children[0];

    // namespace has more children; skip traversal
    if(n.sz != 0 || n.pr != 1 || n.values.length != 2) return null;
    // return default namespace or null
    return prefix(n.values[0]).length == 0 ? uri(n.values[1]) : null;
  }

  /**
   * Returns the specified namespace URI.
   * @param id namespace URI reference
   * @return prefix
   */
  public byte[] uri(final int id) {
    return uris.key(id);
  }

  /**
   * Returns the namespace URI reference for the specified name and pre value,
   * or {@code 0} if namespace cannot be found.
   * @param name element/attribute name
   * @param pre pre value
   * @param data data reference
   * @return namespace URI reference
   */
  public int uri(final byte[] name, final int pre, final Data data) {
    return uri(Token.prefix(name), current.find(pre, data));
  }

  /**
   * Returns a reference to the specified namespace URI,
   * or {@code 0} if the URI is empty or no namespace is found.
   * @param uri namespace URI
   * @return reference, or {@code 0}
   */
  public int uri(final byte[] uri) {
    return uri.length == 0 ? 0 : uris.id(uri);
  }

  /**
   * Returns the namespace URI reference for the specified name,
   * or {@code 0} if no namespace is found.
   * @param name element/attribute name
   * @param elem element flag
   * @return namespace
   */
  public int uri(final byte[] name, final boolean elem) {
    if(uris.isEmpty()) return 0;
    final byte[] pref = Token.prefix(name);
    int nu = elem ? defaults.get(level) : 0;
    if(pref.length != 0) nu = uri(pref, current);
    return nu;
  }

  /**
   * Returns the current namespaces node.
   * @return current namespace node
   */
  NSNode getCurrent() {
    return current;
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
   * Returns the prefix and URI references of all namespaces defined for the node with
   * the specified pre value.
   * @param pre pre value
   * @param data data reference
   * @return namespace references
   */
  int[] get(final int pre, final Data data) {
    return current.find(pre, data).values;
  }

  /**
   * Returns a map with all namespaces that are valid for the specified pre value.
   * @param pre pre value
   * @param data data reference
   * @return scope
   */
  TokenMap scope(final int pre, final Data data) {
    final TokenMap nsScope = new TokenMap();
    NSNode node = current;
    do {
      final int[] values = node.values;
      final int vl = values.length;
      for(int v = 0; v < vl; v += 2) {
        nsScope.put(prefix(values[v]), uri(values[v + 1]));
      }
      final int pos = node.find(pre);
      if(pos < 0) break;
      node = node.children[pos];
    } while(node.pr <= pre && pre < node.pr + data.size(node.pr, Data.ELEM));
    return nsScope;
  }

  /**
   * Finds the nearest namespace node on the ancestor axis of the insert
   * location and sets it as new root. Possible candidates for this node are collected
   * and the match with the highest pre value between ancestors and candidates
   * is determined.
   * @param pre pre value
   * @param data data reference
   */
  void root(final int pre, final Data data) {
    // collect possible candidates for namespace root
    final List<NSNode> cand = new LinkedList<>();
    NSNode node = root;
    cand.add(node);
    for(int p; (p = node.find(pre)) > -1;) {
      // add candidate to stack
      node = node.children[p];
      cand.add(0, node);
    }

    node = root;
    if(cand.size() > 1) {
      // compare candidates to ancestors of pre value
      int ancPre = pre;
      // take first candidate from stack
      NSNode curr = cand.remove(0);
      while(ancPre > -1 && node == root) {
        // this is the new root
        if(curr.pr == ancPre) node = curr;
        // if the current candidate's pre value is lower than the current
        // ancestor of par or par itself, we have to look for a potential
        // match for this candidate. therefore we iterate through ancestors
        // until we find one with a lower than or the same pre value as the
        // current candidate.
        else if(curr.pr < ancPre) {
          while((ancPre = data.parent(ancPre, data.kind(ancPre))) > curr.pr);
          if(curr.pr == ancPre) node = curr;
        }
        // no potential for infinite loop, because dummy root is always a match,
        // in this case ancPre ends iteration
        if(!cand.isEmpty()) curr = cand.remove(0);
      }
    }

    final int uri = uri(Token.EMPTY, pre, data);
    defaults.set(level, uri);
    // remind uri before insert of first node n to connect siblings of n to
    // according namespace
    defaults.set(level - 1, uri);
    current = node;
  }

  /**
   * Returns the namespace URI reference for the specified prefix and node,
   * or {@code 0} if no namespace is found.
   * @param pref prefix
   * @param node node to start with
   * @return namespace URI reference
   */
  private int uri(final byte[] pref, final NSNode node) {
    final int id = prefs.id(pref);
    if(id == 0) return 0;

    NSNode n = node;
    while(n != null) {
      final int u = n.uri(id);
      if(u != 0) return u;
      n = n.parent;
    }
    return 0;
  }

  /**
   * Returns all namespace nodes in the namespace structure with a minimum
   * PRE value.
   * @param pre minimum PRE value of a namespace node.
   * @return List of namespace nodes with a minimum PRE value of pre
   */
  List<NSNode> getNSNodes(final int pre) {
    final List<NSNode> l = new ArrayList<>();
    addNSNodes(root, l, pre);
    return l;
  }

  /**
   * Recursively adds namespace nodes to the given list, starting at the children of the given node.
   * @param curr current namespace node
   * @param list list with namespace nodes
   * @param pre pre value
   * @return list with namespace nodes
   */
  private static List<NSNode> addNSNodes(final NSNode curr, final List<NSNode> list,
      final int pre) {

    final int sz = curr.sz;
    int s = find(curr, pre);
    while(s > 0 && (s == sz || curr.children[s].pr >= pre)) s--;
    for(; s < sz; s++) {
      final NSNode ch = curr.children[s];
      if(ch.pr >= pre) list.add(ch);
      addNSNodes(ch, list, pre);
    }
    return list;
  }

  /**
   * Returns the index of the specified pre value.
   * @param curr current namespace node
   * @param pre int pre value
   * @return index
   */
  private static int find(final NSNode curr, final int pre) {
    // binary search
    int l = 0, h = curr.sz - 1;
    while(l <= h) {
      final int m = l + h >>> 1;
      final int c = curr.children[m].pr - pre;
      if(c == 0) return m;
      if(c < 0) l = m + 1;
      else h = m - 1;
    }
    return l;
  }

  // Updating Namespaces ================================================================
  /**
   * Deletes the specified namespace URI from the root node.
   * @param uri namespace URI reference
   */
  public void delete(final byte[] uri) {
    final int id = uris.id(uri);
    if(id != 0) current.delete(id);
  }

  /**
   * Deletes the specified number of entries from the namespace structure.
   * @param pre pre value of the first node to delete
   * @param data data reference
   * @param size number of entries to be deleted
   */
  void delete(final int pre, final int size, final Data data) {
    NSNode nd = current.find(pre, data);
    if(nd.pr == pre) nd = nd.parent;
    while(nd != null) {
      nd.delete(pre, size);
      nd = nd.parent;
    }

    decrementPre(root, pre, size);
  }

  /**
   * Recursive shifting of pre values after delete operations.
   * @param node current namespace node which is updated if necessary
   * @param pre update location
   * @param size size of inserted/deleted node
   */
  private static void decrementPre(final NSNode node, final int pre, final int size) {
    if(node.pr >= pre + size) node.pr -= size;
    final int ns = node.sz;
    for(int n = 0; n < ns; n++) decrementPre(node.children[n], pre, size);
  }

  /**
   * Increments the PRE value of all namespace nodes in the given list by the given size.
   * @param list list of namespace nodes
   * @param inc size to increment
   */
  static void incrementPre(final List<NSNode> list, final int inc) {
    for(final NSNode node : list) node.pr += inc;
  }

  /**
   * Adds a namespace for the specified pre value.
   * @param pre pre value
   * @param par parent value
   * @param pref prefix
   * @param uri uri
   * @param data data reference
   * @return uri reference
   */
  public int add(final int pre, final int par, final byte[] pref, final byte[] uri,
      final Data data) {

    // don't store XML namespace
    if(Token.eq(pref, Token.XML)) return 0;

    final NSNode nd = current.find(par, data);
    final NSNode t = new NSNode(pre);

    final int k = prefs.put(pref);
    final int v = uris.put(uri);
    if(nd.pr == pre) {
      nd.add(k, v);
    } else {
      t.add(k, v);
      nd.add(t);
    }
    return v;
  }

  /**
   * Setter for namespaces root node.
   * @param node new root
   */
  void setCurrent(final NSNode node) {
    current = node;
  }

  // Printing Namespaces ================================================================

  /**
   * Returns a tabular representation of the namespaces.
   * @param start start pre value
   * @param end end pre value
   * @return namespaces
   */
  public byte[] table(final int start, final int end) {
    if(root.sz == 0) return Token.EMPTY;

    final Table t = new Table();
    t.header.add(TABLEID);
    t.header.add(TABLEPRE);
    t.header.add(TABLEDIST);
    t.header.add(TABLEPREF);
    t.header.add(TABLEURI);
    for(int i = 0; i < 3; ++i) t.align.add(true);
    table(t, root, start, end);
    return t.contents.isEmpty() ? Token.EMPTY : t.finish();
  }

  /**
   * Adds the namespace structure for a node to the specified table.
   * @param table table
   * @param node namespace node
   * @param start start pre value
   * @param end end pre value
   */
  private void table(final Table table, final NSNode node, final int start, final int end) {
    final int[] values = node.values;
    final int vl = values.length;
    for(int i = 0; i < vl; i += 2) {
      if(node.pr < start || node.pr > end) continue;
      final TokenList tl = new TokenList();
      tl.add(values[i + 1]);
      tl.add(node.pr);
      tl.add(node.pr - node.parent.pr);
      tl.add(prefs.key(values[i]));
      tl.add(uris.key(values[i + 1]));
      table.contents.add(tl);
    }
    for(int i = 0; i < node.sz; i++) table(table, node.children[i], start, end);
  }

  /**
   * Returns namespace information.
   * @return info string
   */
  public byte[] info() {
    final TokenObjMap<TokenList> map = new TokenObjMap<>();
    info(map, root);
    final TokenBuilder tb = new TokenBuilder();
    for(final byte[] key : map) {
      tb.add("  ");
      final TokenList values = map.get(key);
      values.sort(false);
      final int ks = values.size();
      if(ks > 1 || values.get(0).length != 0) {
        if(values.size() != 1) tb.add("(");
        for(int k = 0; k < ks; ++k) {
          if(k != 0) tb.add(", ");
          tb.add(values.get(k));
        }
        if(ks != 1) tb.add(")");
        tb.add(" = ");
      }
      tb.addExt("\"%\"" + NL, key);
    }
    return tb.finish();
  }

  /**
   * Adds namespace information for the specified node to a map.
   * @param map namespace map
   * @param node namespace node
   */
  private void info(final TokenObjMap<TokenList> map, final NSNode node) {
    final int[] values = node.values;
    final int vl = values.length;
    for(int v = 0; v < vl; v += 2) {
      final byte[] key = uris.key(values[v + 1]);
      final byte[] val = prefs.key(values[v]);
      TokenList old = map.get(key);
      if(old == null) {
        old = new TokenList();
        map.put(key, old);
      }
      if(!old.contains(val)) old.add(val);
    }
    for(final NSNode c : node.children) info(map, c);
  }

  /**
   * Returns a string representation of the namespaces.
   * @param start start pre value
   * @param end end pre value
   * @return string
   */
  public String toString(final int start, final int end) {
    return root.print(this, start, end);
  }

  @Override
  public String toString() {
    return toString(0, Integer.MAX_VALUE);
  }
}
