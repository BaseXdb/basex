package org.basex.data;

import java.util.*;

import org.basex.util.*;
import org.basex.util.list.*;

/**
 * This class organizes namespace scopes.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
final class NSScope {
  /** Data reference. */
  private final Data data;
  /** Namespaces. */
  private final Namespaces nspaces;
  /** Prefix and uri ids. */
  private final IntList values = new IntList();
  /** Stack with pre values. */
  private final IntList preStack = new IntList();
  /** Root namespace. */
  private final NSNode root;
  /** Tracks existing nodes. */
  private final ArrayList<NSNode> cache;

  /**
   * Default constructor.
   * @param par pre value
   * @param pre pre value
   * @param data data reference
   */
  NSScope(final int pre, final int par, final Data data) {
    this.data = data;
    nspaces = data.nspaces;
    root = nspaces.cursor();
    cache = nspaces.cache(pre);

    NSNode node = nspaces.cursor();
    do {
      values.add(node.values());
      final int pos = node.find(par);
      if(pos < 0) break;
      node = node.child(pos);
    } while(par < node.pre() + data.size(node.pre(), Data.ELEM));
    values.add(-1);
  }

  /**
   * Returns the id of a namespace uri.
   * @param prefixId prefix id
   * @return id of namespace uri, or {@code 0}
   */
  int uriId(final int prefixId) {
    for(int s = values.size() - 1; s > 0; s--) {
      final int uriId = values.get(s);
      if(uriId != -1 && values.get(--s) == prefixId) return uriId;
    }
    return 0;
  }

  /**
   * Refreshes the namespace structure.
   * @param nsPre pre value with namespaces
   * @param c insertion counter
   */
  void loop(final int nsPre, final int c) {
    if(c == 0) nspaces.root(nsPre, data);
    while(!preStack.isEmpty() && preStack.peek() > nsPre) {
      nspaces.close(preStack.pop());
      for(int s = values.size() - 1; s >= 0; s--) {
        if(values.get(s) == -1) {
          values.size(s);
          break;
        }
      }
    }
  }

  /**
   * Opens a new level.
   * @param pre pre value
   */
  void open(final int pre) {
    values.add(-1);
    nspaces.open();
    preStack.push(pre);
  }

  /**
   * Parses the specified namespaces and returns all namespaces that are not declared yet.
   * @param pre pre value
   * @param nsp source namespaces
   * @return {@code true} if new namespaces were added
   */
  boolean open(final int pre, final Atts nsp) {
    values.add(-1);

    final Atts ns = new Atts();
    final int as = nsp.size();
    for(int a = 0; a < as; a++) {
      final byte[] prefix = nsp.name(a), uri = nsp.value(a);
      final int uriId = uriId(nspaces.prefixId(prefix));
      if(uriId == 0 || uriId != nspaces.uriId(uri)) {
        ns.add(prefix, uri);
      }
    }
    nspaces.open(pre, ns);
    preStack.push(pre);
    for(int a = 0; a < as; a++) {
      values.add(nspaces.prefixId(nsp.name(a)));
      values.add(nspaces.uriId(nsp.value(a)));
    }
    return !ns.isEmpty();
  }

  /**
   * Parses the specified namespaces and returns all namespaces that have not been declared yet.
   * @param pre pre value
   * @param prefix prefix
   * @param uri namespace uri
   * @return {@code true} if new namespace was added
   */
  boolean openAttr(final int pre, final byte[] prefix, final byte[] uri) {
    final int uriId = uriId(nspaces.prefixId(prefix));
    if(uriId != 0) return false;

    nspaces.add(pre, -1, prefix, uri, data);
    return true;
  }

  /**
   * Shifts cached namespaces by the specified value.
   * @param diff shift
   */
  void shift(final int diff) {
    for(final NSNode node : cache) node.incrementPre(diff);
  }

  /**
   * Closes the namespace scope.
   */
  void close() {
    while(!preStack.isEmpty()) nspaces.close(preStack.pop());
    nspaces.current(root);
  }
}
