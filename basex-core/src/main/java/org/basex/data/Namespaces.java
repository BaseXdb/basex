package org.basex.data;

import static org.basex.core.Text.*;
import static org.basex.data.DataText.*;

import java.io.*;
import java.util.*;

import org.basex.io.*;
import org.basex.io.in.DataInput;
import org.basex.io.out.DataOutput;
import org.basex.util.*;
import org.basex.util.hash.*;
import org.basex.util.list.*;

/**
 * This class organizes the namespaces of a database.
 *
 * The namespaces of a database that has not been updated yet are kept in a compressed, read-only
 * structure (see {@link NSEntries}). The first update inflates this structure to a tree of
 * {@link NSNode} instances, which is written back in compressed form when the database is closed.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class Namespaces {
  /** Maximum number of nodes that are stored in the old, uncompressed format. */
  private static final int MAXLEGACY = 4096;

  /** Namespace prefixes. */
  private final TokenSet prefixes;
  /** Namespace URIs. */
  private final TokenSet uris;
  /** Sets of prefix/namespace URI pairs. */
  private final NSSets sets;

  /** Compressed namespace entries (can be {@code null}). */
  private NSEntries entries;
  /** Root node of the mutable namespace tree (can be {@code null}). */
  private NSNode root;

  /** Stack with references to current default namespaces. */
  private final IntList defaults = new IntList(2);
  /** Current level. Index starts at 1 (required by XQUF operations). */
  private int level = 1;
  /** Current namespace node (can be {@code null}). */
  private NSNode current;

  // Creating and Writing Namespaces ==============================================================

  /**
   * Empty constructor.
   */
  public Namespaces() {
    prefixes = new TokenSet();
    uris = new TokenSet();
    sets = new NSSets();
    root = new NSNode(-1);
    current = root;
  }

  /**
   * Constructor, specifying an input stream.
   * @param in input stream
   * @param file file with the leaf entries
   * @throws IOException I/O exception
   */
  Namespaces(final DataInput in, final IOFile file) throws IOException {
    prefixes = new TokenSet(in);
    uris = new TokenSet(in);
    sets = new NSSets(in);
    entries = new NSEntries(in, file);
  }

  /**
   * Constructor for databases that were created before version 13.
   * @param in input stream
   * @throws IOException I/O exception
   */
  Namespaces(final DataInput in) throws IOException {
    prefixes = new TokenSet(in);
    uris = new TokenSet(in);
    sets = new NSSets();
    root = new NSNode(in, null, sets);
    current = root;
  }

  /**
   * Indicates if the namespaces have been inflated and are small enough to be stored in the old,
   * uncompressed format. Such databases can still be opened by versions older than 13.
   * @return result of check
   */
  boolean legacy() {
    return root != null && root.count(MAXLEGACY) <= MAXLEGACY;
  }

  /**
   * Writes the namespaces to disk.
   * @param out output stream
   * @param legacy write the old, uncompressed format
   * @param file file for the leaf entries
   * @throws IOException I/O exception
   */
  void write(final DataOutput out, final boolean legacy, final IOFile file) throws IOException {
    prefixes.write(out);
    uris.write(out);
    if(root == null) {
      // structure has not been modified: leave the leaf entries untouched
      sets.write(out);
      entries.write(out);
    } else if(legacy) {
      file.delete();
      root.write(out, sets);
    } else {
      NSEntries.write(root, sets, out, file);
    }
  }

  /**
   * Closes the compressed namespace entries.
   */
  void close() {
    if(entries != null) entries.close();
  }

  /**
   * Returns the mutable namespace tree and inflates the compressed entries if necessary.
   * @return root node
   */
  private NSNode tree() {
    if(root == null) {
      root = entries.inflate();
      entries.close();
      entries = null;
      current = root;
    }
    return root;
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
   * Returns a prefix for the name with the specified ID.
   * @param id ID of prefix
   * @return prefix
   */
  byte[] prefix(final int id) {
    return prefixes.key(id);
  }

  /**
   * Returns a namespace URI for the name with the specified ID.
   * @param id ID of namespace URI ({@code 0}: no namespace)
   * @return namespace URI or {@code null}
   */
  public byte[] uri(final int id) {
    return uris.key(id);
  }

  /**
   * Returns the ID of the specified namespace URI.
   * @param uri namespace URI
   * @return ID, or {@code 0} if no entry is found
   */
  public int uriId(final byte[] uri) {
    return uris.index(uri);
  }

  /**
   * Returns the common default namespace of all documents of the database.
   * @param ndocs number of documents
   * @param data data reference
   * @return namespace, or {@code null} if there is no common namespace
   */
  byte[] defaultNs(final int ndocs, final Data data) {
    if(root == null) return entries.defaultNs(this, ndocs, data);

    // no namespaces defined: default namespace is empty
    final int ch = root.children();
    if(ch == 0) return Token.EMPTY;
    // give up if number of default namespaces differs from number of documents
    if(ch != ndocs) return null;

    int id = 0;
    for(int c = 0; c < ch; c++) {
      final NSNode child = root.child(c);
      // give up if the child node has more children
      if(child.children() > 0) return null;
      id = defaultNs(child.pre(), child.setId(), id, data);
      if(id == 0) return null;
    }
    // return common default namespace
    return uri(id);
  }

  /**
   * Checks if a namespace node declares the default namespace of a document.
   * @param pre PRE value of the node
   * @param setId set ID
   * @param id ID of the namespace URI of the preceding nodes ({@code 0}: no node yet)
   * @param data data reference
   * @return ID of the namespace URI, or {@code 0} if the node does not qualify
   */
  int defaultNs(final int pre, final int setId, final int id, final Data data) {
    // give up if the node is not attached to the root element of a document
    if(data.kind(data.parent(pre, Data.ELEM)) != Data.DOC) return 0;
    // give up if the node has more than one namespace, or if the prefix is not empty
    final int[] values = sets.get(setId);
    if(values.length != 2 || prefix(values[0]).length != 0) return 0;
    // check if all documents have the same default namespace
    return id == 0 || id == values[1] ? values[1] : 0;
  }

  /**
   * Checks if a default namespace is declared anywhere in the database.
   * @return result of check
   */
  public boolean usesDefaultNs() {
    if(isEmpty()) return false;
    // the sets of a compressed structure are compacted, i.e. all of them are referenced
    if(root == null) {
      final int ss = sets.size();
      for(int s = 1; s <= ss; s++) {
        if(usesDefaultNs(sets.get(s))) return true;
      }
      return false;
    }
    return usesDefaultNs(root);
  }

  /**
   * Recursively checks a namespace node and its descendants for a default namespace.
   * @param node namespace node
   * @return result of check
   */
  private boolean usesDefaultNs(final NSNode node) {
    if(usesDefaultNs(sets.get(node.setId()))) return true;
    final int ch = node.children();
    for(int c = 0; c < ch; c++) {
      if(usesDefaultNs(node.child(c))) return true;
    }
    return false;
  }

  /**
   * Checks a set of prefix/namespace URI pairs for a default namespace.
   * @param values prefix/URI pairs
   * @return result of check
   */
  private boolean usesDefaultNs(final int[] values) {
    final int vl = values.length;
    for(int v = 0; v < vl; v += 2) {
      if(prefix(values[v]).length == 0 && uri(values[v + 1]).length != 0) return true;
    }
    return false;
  }

  // Requesting Namespaces Based on Context =======================================================

  /**
   * Returns the ID of a namespace URI for the specified prefix.
   * @param prefix prefix
   * @param element indicates if the prefix belongs to an element or attribute name
   * @return ID of namespace URI, or {@code 0} if no entry is found
   */
  public int uriIdForPrefix(final byte[] prefix, final boolean element) {
    if(isEmpty()) return 0;
    tree();
    if(prefix.length == 0) return element ? defaults.get(level) : 0;
    final int prefId = prefixes.index(prefix);
    return prefId == 0 ? 0 : uriId(prefId, current);
  }

  /**
   * Returns the ID of a namespace URI for the specified prefix and PRE value.
   * @param prefix prefix
   * @param pre PRE value
   * @param data data reference
   * @return ID of namespace URI, or {@code 0} if no entry is found
   */
  public int uriIdForPrefix(final byte[] prefix, final int pre, final Data data) {
    final int prefId = prefixes.index(prefix);
    if(prefId == 0) return 0;
    return root == null ? entries.uriId(prefId, pre, data, sets) :
      uriId(prefId, current.find(pre, data));
  }

  /**
   * Returns the ID of a namespace URI for the specified prefix reference and node.
   * @param prefId prefix reference
   * @param node node to start with
   * @return ID of the namespace URI, or {@code 0} if namespace is not found
   */
  private int uriId(final int prefId, final NSNode node) {
    for(NSNode nd = node; nd != null; nd = nd.parent()) {
      final int uriId = sets.uri(nd.setId(), prefId);
      if(uriId != 0) return uriId;
    }
    return 0;
  }

  /**
   * Returns all namespace prefixes and URIs that are declared for the specified PRE value.
   * Should only be called for element nodes.
   * @param pre PRE value
   * @param data data reference
   * @return key and value IDs
   */
  Atts values(final int pre, final Data data) {
    final int[] values = sets.get(root == null ? entries.setId(pre, data) :
      current.find(pre, data).setId());
    final int nl = values.length;
    final Atts as = new Atts(nl / 2);
    for(int n = 0; n < nl; n += 2) as.add(prefix(values[n]), uri(values[n + 1]));
    return as;
  }

  /**
   * Finds the nearest namespace node on the ancestor axis of the insert location and sets it as new
   * root. Possible candidates for this node are collected and the match with the highest PRE value
   * between ancestors and candidates is determined.
   * @param pre PRE value
   * @param data data reference
   */
  void root(final int pre, final Data data) {
    // collect possible candidates for namespace root
    final List<NSNode> cand = new LinkedList<>();
    NSNode nd = tree();
    cand.add(nd);
    for(int p; (p = nd.find(pre)) > -1;) {
      // add candidate to stack
      nd = nd.child(p);
      cand.addFirst(nd);
    }

    nd = root;
    if(cand.size() > 1) {
      // compare candidates to ancestors of PRE value
      int ancPre = pre;
      // take first candidate from stack
      NSNode curr = cand.removeFirst();
      while(ancPre > -1 && nd == root) {
        // if the current candidate's PRE value is lower than the current ancestor of par or par
        // itself, we have to look for a potential match for this candidate. therefore we iterate
        // through ancestors until we find one with a lower than or the same PRE value as the
        // current candidate.
        while(ancPre > curr.pre()) ancPre = data.parent(ancPre, data.kind(ancPre));
        // this is the new root
        if(ancPre == curr.pre()) nd = curr;
        // no potential for infinite loop, because dummy root is always a match,
        // in this case ancPre ends iteration
        if(!cand.isEmpty()) curr = cand.removeFirst();
      }
    }

    final int uriId = uriIdForPrefix(Token.EMPTY, pre, data);
    defaults.set(level, uriId);
    // remember URI before insert of first node n to connect siblings of n to according namespace
    defaults.set(level - 1, uriId);
    current = nd;
  }

  /**
   * Caches and returns all namespace nodes in the namespace structure with a minimum PRE value.
   * @param pre minimum PRE value of a namespace node
   * @return list of namespace nodes
   */
  ArrayList<NSNode> cache(final int pre) {
    final ArrayList<NSNode> list = new ArrayList<>();
    addNodes(tree(), list, pre);
    return list;
  }

  /**
   * Recursively adds namespace nodes to a list, starting with the children of a node.
   * @param node current namespace node
   * @param list list with namespace nodes
   * @param pre PRE value
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
    current = node;
  }

  /**
   * Returns the current namespace cursor.
   * @return current namespace node
   */
  NSNode cursor() {
    tree();
    return current;
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
   * @param pre PRE value
   * @param atts namespaces
   */
  public void open(final int pre, final Atts atts) {
    open();
    final int as = atts.size();
    if(as == 0) return;

    final int[] values = new int[as << 1];
    for(int a = 0; a < as; a++) {
      final byte[] prefix = atts.name(a), uri = atts.value(a);
      final int prefId = prefixes.put(prefix), uriId = uris.put(uri);
      values[a << 1] = prefId;
      values[(a << 1) + 1] = uriId;
      if(prefix.length == 0) defaults.set(level, uriId);
    }
    final NSNode nd = new NSNode(pre, sets.put(values));
    tree();
    current.add(nd);
    current = nd;
  }

  /**
   * Adds a single namespace for the specified PRE value.
   * @param pre PRE value
   * @param prefix prefix
   * @param uri namespace URI
   * @param data data reference
   * @return ID of namespace URI
   */
  public int add(final int pre, final byte[] prefix, final byte[] uri, final Data data) {
    final int prefId = prefixes.put(prefix), uriId = uris.put(uri);
    tree();
    NSNode nd = current.find(pre, data);
    if(nd.pre() != pre) {
      final NSNode child = new NSNode(pre);
      nd.add(child);
      nd = child;
    }
    nd.add(sets, prefId, uriId);
    return uriId;
  }

  /**
   * Closes a namespace node.
   * @param pre current PRE value
   */
  public void close(final int pre) {
    tree();
    while(current.pre() >= pre) {
      final NSNode nd = current.parent();
      if(nd == null) break;
      current = nd;
    }
    --level;
  }

  /**
   * Deletes the specified namespace URI from the root node.
   * @param uri namespace URI reference
   */
  public void delete(final byte[] uri) {
    final int id = uris.index(uri);
    if(id != 0) {
      tree();
      current.delete(sets, id);
    }
  }

  /**
   * Deletes the specified number of entries from the namespace structure.
   * @param pre PRE value of the first node to delete
   * @param size number of entries to be deleted
   * @param data data reference
   */
  void delete(final int pre, final int size, final Data data) {
    tree();
    NSNode nd = current.find(pre, data);
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
   * @param start first PRE value
   * @param end last PRE value
   * @return namespaces
   */
  byte[] table(final int start, final int end) {
    if(isEmpty()) return Token.EMPTY;

    final Table t = new Table();
    t.header.add(TABLENS);
    t.header.add(TABLEPRE);
    t.header.add(TABLEDIST);
    t.header.add(TABLEPREF);
    t.header.add(TABLEURI);
    for(int i = 0; i < 3; i++) t.align.add(true);

    final IntList list = range(start, end);
    final int ls = list.size();
    for(int l = 0; l < ls; l += 4) {
      final int[] values = sets.get(list.get(l + 3));
      final int vl = values.length;
      for(int v = 0; v < vl; v += 2) {
        final TokenList tl = new TokenList();
        tl.add(values[v + 1]);
        tl.add(list.get(l));
        tl.add(list.get(l) - list.get(l + 1));
        tl.add(prefix(values[v]));
        tl.add(uri(values[v + 1]));
        t.contents.add(tl);
      }
    }
    return t.contents.isEmpty() ? Token.EMPTY : t.finish();
  }

  /**
   * Returns all namespace entries in the specified PRE range.
   * @param start first PRE value
   * @param end last PRE value
   * @return list with the PRE value, parent PRE value, level and set ID of each entry
   */
  private IntList range(final int start, final int end) {
    final IntList list = new IntList();
    if(root == null) entries.entries(list, start, end);
    else root.entries(list, 0, start, end);
    return list;
  }

  /**
   * Returns namespace information.
   * @return info string
   */
  public byte[] info() {
    final TokenObjectMap<TokenList> map = new TokenObjectMap<>();
    if(root == null) {
      final int ss = sets.size();
      for(int s = 1; s <= ss; s++) info(sets.get(s), map);
    } else {
      info(root, map);
    }
    final TokenBuilder tb = new TokenBuilder();
    for(final byte[] key : map) {
      tb.add("  ");
      final TokenList values = map.get(key).sort();
      final int ks = values.size();
      if(ks > 1 || values.get(0).length != 0) {
        if(values.size() != 1) tb.add("(");
        for(int k = 0; k < ks; k++) {
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
   * Recursively adds namespace information for a node and its descendants to a map.
   * @param node namespace node
   * @param map namespace map
   */
  private void info(final NSNode node, final TokenObjectMap<TokenList> map) {
    info(sets.get(node.setId()), map);
    final int ch = node.children();
    for(int c = 0; c < ch; c++) info(node.child(c), map);
  }

  /**
   * Adds namespace information for a set of prefix/URI pairs to a map.
   * @param values prefix/URI pairs
   * @param map namespace map
   */
  private void info(final int[] values, final TokenObjectMap<TokenList> map) {
    final int vl = values.length;
    for(int v = 0; v < vl; v += 2) {
      final byte[] prefix = prefix(values[v]), uri = uri(values[v + 1]);
      final TokenList prfs = map.computeIfAbsent(uri, () -> new TokenList(1));
      if(!prfs.contains(prefix)) prfs.add(prefix);
    }
  }

  /**
   * Returns a string representation of the namespaces.
   * @param start start PRE value
   * @param end end PRE value
   * @return string
   */
  String toString(final int start, final int end) {
    final TokenBuilder tb = new TokenBuilder();
    final IntList list = range(start, end);
    final int ls = list.size();
    for(int l = 0; l < ls; l += 4) {
      tb.add(NL);
      for(int i = list.get(l + 2); i > 0; i--) tb.add("  ");
      tb.add("Pre[").add(Integer.toString(list.get(l))).add("] ");
      final int[] values = sets.get(list.get(l + 3));
      final int vl = values.length;
      for(int v = 0; v < vl; v += 2) {
        if(v != 0) tb.add(' ');
        tb.add("xmlns");
        final byte[] p = prefix(values[v]);
        if(p.length != 0) tb.add(':');
        tb.add(p).add("=\"").add(uri(values[v + 1])).add('"');
      }
    }
    return tb.toString();
  }

  @Override
  public String toString() {
    return toString(0, Integer.MAX_VALUE);
  }
}
