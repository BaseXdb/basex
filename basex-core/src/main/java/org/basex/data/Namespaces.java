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
 * @author BaseX Team 2005-21, BSD License
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
   * Returns a prefix for the name with the specified id.
   * @param id id of prefix
   * @return prefix
   */
  byte[] prefix(final int id) {
    return prefixes.key(id);
  }

  /**
   * Returns a namespace URI for the name with the specified id.
   * @param id id of namespace URI ({@code 0}: no namespace)
   * @return namespace URI or {@code null}
   */
  public byte[] uri(final int id) {
    return uris.key(id);
  }

  /**
   * Returns the id of the specified namespace uri.
   * @param uri namespace URI
   * @return id, or {@code 0} if no entry is found
   */
  public int uriId(final byte[] uri) {
    return uris.id(uri);
  }

  /**
   * Returns the common default namespace of all documents of the database.
   * @param ndocs number of documents
   * @return namespace, or {@code null} if there is no common namespace
   */
  byte[] defaultNs(final int ndocs) {
    // no namespaces defined: default namespace is empty
    final int ch = root.children();
    if(ch == 0) return Token.EMPTY;
    // give up if number of default namespaces differs from number of documents
    if(ch != ndocs) return null;

    int id = 0;
    for(int c = 0; c < ch; c++) {
      final NSNode child = root.child(0);
      final int[] values = child.values();
      // give up if child node has more children or more than one namespace
      if(child.children() > 0 || child.pre() != 1 || values.length != 2) return null;
      // give up if namespace has a non-empty prefix
      if(prefix(values[0]).length != 0) return null;
      // check if all documents have the same default namespace
      if(c == 0) {
        id = values[1];
      } else if(id != values[1]) {
        return null;
      }
    }
    // return common default namespace
    return uri(id);
  }

  // Requesting Namespaces Based on Context =======================================================

  /**
   * Returns the id of a namespace URI for the specified prefix.
   * @param prefix prefix
   * @param element indicates if the prefix belongs to an element or attribute name
   * @return id of namespace uri, or {@code 0} if no entry is found
   */
  public int uriIdForPrefix(final byte[] prefix, final boolean element) {
    if(isEmpty()) return 0;
    return prefix.length == 0 ? element ? defaults.get(level) : 0 : uriId(prefix, cursor);
  }

  /**
   * Returns the id of a namespace URI for the specified prefix and pre value.
   * @param prefix prefix
   * @param pre pre value
   * @param data data reference
   * @return id of namespace uri, or {@code 0} if no entry is found
   */
  public int uriIdForPrefix(final byte[] prefix, final int pre, final Data data) {
    return uriId(prefix, cursor.find(pre, data));
  }

  /**
   * Returns the id of a namespace URI for the specified prefix and node.
   * @param prefix prefix
   * @param node node to start with
   * @return id of the namespace uri, or {@code 0} if namespace is not found
   */
  private int uriId(final byte[] prefix, final NSNode node) {
    final int prefId = prefixes.id(prefix);
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
  Atts values(final int pre, final Data data) {
    final Atts as = new Atts();
    final int[] values = cursor.find(pre, data).values();
    final int nl = values.length;
    for(int n = 0; n < nl; n += 2) as.add(prefix(values[n]), uri(values[n + 1]));
    return as;
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
    NSNode nd = root;
    cand.add(nd);
    for(int p; (p = nd.find(pre)) > -1;) {
      // add candidate to stack
      nd = nd.child(p);
      cand.add(0, nd);
    }

    nd = root;
    if(cand.size() > 1) {
      // compare candidates to ancestors of pre value
      int ancPre = pre;
      // take first candidate from stack
      NSNode curr = cand.remove(0);
      while(ancPre > -1 && nd == root) {
        // if the current candidate's pre value is lower than the current ancestor of par or par
        // itself, we have to look for a potential match for this candidate. therefore we iterate
        // through ancestors until we find one with a lower than or the same pre value as the
        // current candidate.
        while(ancPre > curr.pre()) ancPre = data.parent(ancPre, data.kind(ancPre));
        // this is the new root
        if(ancPre == curr.pre()) nd = curr;
        // no potential for infinite loop, because dummy root is always a match,
        // in this case ancPre ends iteration
        if(!cand.isEmpty()) curr = cand.remove(0);
      }
    }

    final int uriId = uriIdForPrefix(Token.EMPTY, pre, data);
    defaults.set(level, uriId);
    // remember uri before insert of first node n to connect siblings of n to according namespace
    defaults.set(level - 1, uriId);
    cursor = nd;
  }

  /**
   * Caches and returns all namespace nodes in the namespace structure with a minimum pre value.
   * @param pre minimum pre value of a namespace node
   * @return list of namespace nodes
   */
  ArrayList<NSNode> cache(final int pre) {
    final ArrayList<NSNode> list = new ArrayList<>();
    addNodes(root, list, pre);
    return list;
  }

  /**
   * Recursively adds namespace nodes to the a list, starting with the children of a node.
   * @param node current namespace node
   * @param list list with namespace nodes
   * @param pre pre value
   */
  private static void addNodes(final NSNode node, final List<NSNode> list, final int pre) {
    final int size = node.children();
    int n = Math.max(0, node.find(pre));
    while(n > 0 && (n == size || node.child(n).pre() >= pre)) n--;
    for(; n < size; n++) {
      final NSNode child = node.child(n);
      if(child.pre() >= pre) list.add(child);
      addNodes(child, list, pre);
    }
  }

  // Updating Namespaces ==========================================================================

  /**
   * Sets a namespace cursor.
   * @param node namespace node
   */
  void cursor(final NSNode node) {
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
   * Increases the level counter and sets a new default namespace.
   */
  public void open() {
    final int nu = defaults.get(level);
    defaults.set(++level, nu);
  }

  /**
   * Adds namespaces to a new namespace child node and sets this node as new cursor.
   * @param pre pre value
   * @param atts namespaces
   */
  public void open(final int pre, final Atts atts) {
    open();
    if(!atts.isEmpty()) {
      final NSNode nd = new NSNode(pre);
      cursor.add(nd);
      cursor = nd;

      final int as = atts.size();
      for(int a = 0; a < as; a++) {
        final byte[] pref = atts.name(a), uri = atts.value(a);
        final int prefId = prefixes.put(pref), uriId = uris.put(uri);
        nd.add(prefId, uriId);
        if(pref.length == 0) defaults.set(level, uriId);
      }
    }
  }

  /**
   * Adds a single namespace for the specified pre value.
   * @param pre pre value
   * @param prefix prefix
   * @param uri namespace uri
   * @param data data reference
   * @return id of namespace uri
   */
  public int add(final int pre, final byte[] prefix, final byte[] uri, final Data data) {
    final int prefId = prefixes.put(prefix), uriId = uris.put(uri);
    NSNode nd = cursor.find(pre, data);
    if(nd.pre() != pre) {
      final NSNode child = new NSNode(pre);
      nd.add(child);
      nd = child;
    }
    nd.add(prefId, uriId);
    return uriId;
  }

  /**
   * Closes a namespace node.
   * @param pre current pre value
   */
  public void close(final int pre) {
    while(cursor.pre() >= pre) {
      final NSNode nd = cursor.parent();
      if(nd == null) break;
      cursor = nd;
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

  // Printing Namespaces ==========================================================================

  /**
   * Returns a tabular representation of the namespace entries.
   * @param start first pre value
   * @param end last pre value
   * @return namespaces
   */
  byte[] table(final int start, final int end) {
    if(root.children() == 0) return Token.EMPTY;

    final Table t = new Table();
    t.header.add(TABLENS);
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
      final TokenList values = map.get(key).sort();
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
  String toString(final int start, final int end) {
    return root.toString(this, start, end);
  }

  @Override
  public String toString() {
    return toString(0, Integer.MAX_VALUE);
  }
}
