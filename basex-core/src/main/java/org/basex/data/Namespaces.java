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
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
public final class Namespaces {
  /** Namespace prefixes. */
  private final TokenSet prefixes;
  /** Namespace URIs. */
  private final TokenSet uris;
  /** Root node. */
  private final NSNode root;

  /** Stack with references to current default namespaces. */
  private final IntList defaults = new IntList(2);
  /** Current level. Index starts at 1 (required by XQUF operations). */
  private int level = 1;
  /** Current namespace node. */
  private NSNode cursor;

  // Creating and Writing Namespaces ==============================================================

  /**
   * Empty constructor.
   */
  public Namespaces() {
    prefixes = new TokenSet();
    uris = new TokenSet();
    root = new NSNode(-1);
    cursor = root;
  }

  /**
   * Constructor, specifying an input stream.
   * @param in input stream
   * @throws IOException I/O exception
   */
  Namespaces(final DataInput in) throws IOException {
    prefixes = new TokenSet(in);
    uris = new TokenSet(in);
    root = new NSNode(in, null);
    cursor = root;
  }

  /**
   * Writes the namespaces to disk.
   * @param out output stream
   * @throws IOException I/O exception
   */
  void write(final DataOutput out) throws IOException {
    prefixes.write(out);
    uris.write(out);
    root.write(out);
  }

  // Requesting Namespaces Globally ===============================================================

  /**
   * Returns if no namespaces exist.
   * Note that the container size does not change if namespaces are deleted.
   * This function is mainly used to decide namespaces need to be considered in query optimizations.
   * @return result of check
   */
  public boolean isEmpty() {
    return uris.isEmpty();
  }

  /**
   * Returns the number of namespaces that have been stored so far.
   * @return number of entries
   */
  public int size() {
    return uris.size();
  }

  /**
   * Returns a prefix for the specified id.
   * @param id id of prefix
   * @return prefix
   */
  public byte[] prefix(final int id) {
    return prefixes.key(id);
  }

  /**
   * Returns a namespace uri for the specified id.
   * @param id id of namespace uri
   * @return namespace uri
   */
  public byte[] uri(final int id) {
    return uris.key(id);
  }

  /**
   * Returns the default namespace uri for all documents of the database.
   * @return global default namespace, or {@code null} if there is more than one such namespace
   */
  public byte[] globalUri() {
    // no namespaces defined: default namespace is empty
    final int ch = root.children();
    if(ch == 0) return Token.EMPTY;
    // give up if more than one namespace is defined
    if(ch > 1) return null;
    // give up if child node has more children or more than one namespace
    final NSNode child = root.child(0);
    final int[] values = child.values();
    if(child.children() > 0 || child.pre() != 1 || values.length != 2) return null;
    // give up if namespace has a non-empty prefix
    if(prefix(values[0]).length != 0) return null;
    // return default namespace
    return uri(values[1]);
  }

  /**
   * Returns the id of the specified namespace uri.
   * @param uri namespace URI
   * @return id, or {@code 0} if the uri is empty or if no entry is found
   */
  public int uriId(final byte[] uri) {
    return uri.length == 0 ? 0 : uris.id(uri);
  }

  /**
   * Returns the id of the specified prefix.
   * @param prefix prefix
   * @return id, or {@code 0} if no entry is found
   */
  public int prefixId(final byte[] prefix) {
    return prefixes.id(prefix);
  }

  // Requesting Namespaces Based on Context =======================================================

  /**
   * Returns the id of a namespace uri for the specified element/attribute name.
   * @param name element/attribute name
   * @param element indicates if this is an element or attribute name
   * @return id of namespace uri, or {@code 0} if no entry is found
   */
  public int uriId(final byte[] name, final boolean element) {
    if(isEmpty()) return 0;
    final byte[] pref = Token.prefix(name);
    return pref.length != 0 ? uriId(pref, cursor) : element ? defaults.get(level) : 0;
  }

  /**
   * Returns the id of a namespace uri for an element/attribute name and a specific pre value.
   * @param name element/attribute name
   * @param pre pre value
   * @param data data reference
   * @return id of namespace uri, or {@code 0} if no entry is found
   */
  public int uriId(final byte[] name, final int pre, final Data data) {
    return uriId(Token.prefix(name), cursor.find(pre, data));
  }

  /**
   * Returns the id of a namespace uri for the specified prefix and node,
   * or {@code 0} if no namespace is found.
   * @param pref prefix
   * @param node node to start with
   * @return id of the namespace uri
   */
  private int uriId(final byte[] pref, final NSNode node) {
    final int prefId = prefixes.id(pref);
    if(prefId == 0) return 0;

    NSNode nd = node;
    while(nd != null) {
      final int uriId = nd.uri(prefId);
      if(uriId != 0) return uriId;
      nd = nd.parent();
    }
    return 0;
  }

  /**
   * Returns all namespace prefixes and uris that are declared for the specified pre value.
   * Should only be called for element nodes.
   * @param pre pre value
   * @param data data reference
   * @return key and value ids
   */
  public Atts values(final int pre, final Data data) {
    final Atts as = new Atts();
    final int[] values = cursor.find(pre, data).values();
    final int nl = values.length;
    for(int n = 0; n < nl; n += 2) {
      as.add(prefix(values[n]), uri(values[n + 1]));
    }
    return as;
  }

  /**
   * Returns a map with all namespaces that are valid for the specified pre value.
   * @param pre pre value
   * @param data data reference
   * @return scope
   */
  TokenMap scope(final int pre, final Data data) {
    final TokenMap nsScope = new TokenMap();
    NSNode node = cursor;
    do {
      final int[] values = node.values();
      final int vl = values.length;
      for(int v = 0; v < vl; v += 2) {
        nsScope.put(prefix(values[v]), uri(values[v + 1]));
      }
      final int pos = node.find(pre);
      if(pos < 0) break;
      node = node.child(pos);
    } while(pre < node.pre() + data.size(node.pre(), Data.ELEM));
    return nsScope;
  }

  /**
   * Finds the nearest namespace node on the ancestor axis of the insert location and sets it as new
   * root. Possible candidates for this node are collected and the match with the highest pre value
   * between ancestors and candidates is determined.
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
      node = node.child(p);
      cand.add(0, node);
    }

    node = root;
    if(cand.size() > 1) {
      // compare candidates to ancestors of pre value
      int ancPre = pre;
      // take first candidate from stack
      NSNode curr = cand.remove(0);
      while(ancPre > -1 && node == root) {
        // if the current candidate's pre value is lower than the current ancestor of par or par
        // itself, we have to look for a potential match for this candidate. therefore we iterate
        // through ancestors until we find one with a lower than or the same pre value as the
        // current candidate.
        while(ancPre > curr.pre()) {
          ancPre = data.parent(ancPre, data.kind(ancPre));
        }
        // this is the new root
        if(ancPre == curr.pre()) node = curr;
        // no potential for infinite loop, because dummy root is always a match,
        // in this case ancPre ends iteration
        if(!cand.isEmpty()) curr = cand.remove(0);
      }
    }

    final int uriId = uriId(Token.EMPTY, pre, data);
    defaults.set(level, uriId);
    // remind uri before insert of first node n to connect siblings of n to according namespace
    defaults.set(level - 1, uriId);
    cursor = node;
  }

  /**
   * Caches and returns all namespace nodes in the namespace structure with a minimum pre value.
   * @param pre minimum pre value of a namespace node.
   * @return list of namespace nodes
   */
  ArrayList<NSNode> cache(final int pre) {
    final ArrayList<NSNode> list = new ArrayList<>();
    addNsNodes(root, list, pre);
    return list;
  }

  /**
   * Recursively adds namespace nodes to the given list, starting at the children of the given node.
   * @param curr current namespace node
   * @param list list with namespace nodes
   * @param pre pre value
   */
  private static void addNsNodes(final NSNode curr, final List<NSNode> list, final int pre) {
    final int sz = curr.children();
    int i = find(curr, pre);
    while(i > 0 && (i == sz || curr.child(i).pre() >= pre)) i--;
    for(; i < sz; i++) {
      final NSNode ch = curr.child(i);
      if(ch.pre() >= pre) list.add(ch);
      addNsNodes(ch, list, pre);
    }
  }

  /**
   * Returns the index of the specified pre value.
   * @param curr current namespace node
   * @param pre int pre value
   * @return index, or possible insertion position
   */
  private static int find(final NSNode curr, final int pre) {
    // binary search
    int l = 0, h = curr.children() - 1;
    while(l <= h) {
      final int m = l + h >>> 1, c = curr.child(m).pre() - pre;
      if(c == 0) return m;
      if(c < 0) l = m + 1;
      else h = m - 1;
    }
    return l;
  }

  // Updating Namespaces ==========================================================================

  /**
   * Sets a namespace cursor.
   * @param node namespace node
   */
  void current(final NSNode node) {
    cursor = node;
  }

  /**
   * Returns the current namespace cursor.
   * @return current namespace node
   */
  NSNode cursor() {
    return cursor;
  }

  /**
   * Initializes an update operation by increasing the level counter.
   */
  public void open() {
    final int nu = defaults.get(level);
    defaults.set(++level, nu);
  }

  /**
   * Adds namespaces to a new namespace node.
   * @param pre pre value
   * @param atts namespaces
   */
  public void open(final int pre, final Atts atts) {
    open();
    if(!atts.isEmpty()) {
      final NSNode node = new NSNode(pre);
      cursor.add(node);
      cursor = node;

      final int as = atts.size();
      for(int a = 0; a < as; a++) {
        final byte[] pref = atts.name(a), uri = atts.value(a);
        final int prefId = prefixes.put(pref), uriId = uris.put(uri);
        node.add(prefId, uriId);
        if(pref.length == 0) defaults.set(level, uriId);
      }
    }
  }

  /**
   * Closes a namespace node.
   * @param pre current pre value
   */
  public void close(final int pre) {
    while(cursor.pre() >= pre) {
      final NSNode par = cursor.parent();
      if(par == null) break;
      cursor = par;
    }
    --level;
  }

  /**
   * Deletes the specified namespace URI from the root node.
   * @param uri namespace URI reference
   */
  public void delete(final byte[] uri) {
    final int id = uris.id(uri);
    if(id != 0) cursor.delete(id);
  }

  /**
   * Deletes the specified number of entries from the namespace structure.
   * @param pre pre value of the first node to delete
   * @param data data reference
   * @param size number of entries to be deleted
   */
  void delete(final int pre, final int size, final Data data) {
    NSNode nd = cursor.find(pre, data);
    if(nd.pre() == pre) nd = nd.parent();
    while(nd != null) {
      nd.delete(pre, size);
      nd = nd.parent();
    }
    root.decrementPre(pre, size);
  }

  /**
   * Adds a namespace for the specified pre value.
   * @param pre pre value
   * @param par parent value
   * @param prefix prefix
   * @param uri namespace uri
   * @param data data reference
   * @return namespace uri id
   */
  public int add(final int pre, final int par, final byte[] prefix, final byte[] uri,
      final Data data) {

    // don't store XML namespace
    if(Token.eq(prefix, Token.XML)) return 0;

    final int prefId = prefixes.put(prefix), uriId = uris.put(uri);
    NSNode nd = cursor.find(par, data);
    if(nd.pre() != pre) {
      final NSNode child = new NSNode(pre);
      nd.add(child);
      nd = child;
    }
    nd.add(prefId, uriId);
    return uriId;
  }

  // Printing Namespaces ==========================================================================

  /**
   * Returns a tabular representation of the namespace entries.
   * @param start first pre value
   * @param end last pre value
   * @return namespaces
   */
  public byte[] table(final int start, final int end) {
    if(root.children() == 0) return Token.EMPTY;

    final Table t = new Table();
    t.header.add(TABLEID);
    t.header.add(TABLEPRE);
    t.header.add(TABLEDIST);
    t.header.add(TABLEPREF);
    t.header.add(TABLEURI);
    for(int i = 0; i < 3; ++i) t.align.add(true);
    root.table(t, start, end, this);
    return t.contents.isEmpty() ? Token.EMPTY : t.finish();
  }

  /**
   * Returns namespace information.
   * @return info string
   */
  public byte[] info() {
    final TokenObjMap<TokenList> map = new TokenObjMap<>();
    root.info(map, this);
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
   * Returns a string representation of the namespaces.
   * @param start start pre value
   * @param end end pre value
   * @return string
   */
  public String toString(final int start, final int end) {
    return root.toString(this, start, end);
  }

  @Override
  public String toString() {
    return toString(0, Integer.MAX_VALUE);
  }
}
