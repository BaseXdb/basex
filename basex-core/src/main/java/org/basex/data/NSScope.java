package org.basex.data;

import java.util.*;

import org.basex.util.*;
import org.basex.util.list.*;

/**
 * This class organizes namespace scopes.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
final class NSScope {
  /** Data reference. */
  private final Data data;
  /** Namespaces. */
  private final Namespaces nspaces;
  /** Stack with pre values. */
  private final IntList preStack = new IntList();
  /** Root namespace. */
  private final NSNode root;
  /** Tracks existing nodes. */
  private final ArrayList<NSNode> cache;

  /**
   * Default constructor.
   * @param pre pre value
   * @param data data reference
   */
  NSScope(final int pre, final Data data) {
    this.data = data;
    nspaces = data.nspaces;
    root = nspaces.cursor();
    cache = nspaces.cache(pre);
  }

  /**
   * Refreshes the namespace structure.
   * @param nsPre pre value with namespaces
   * @param c insertion counter
   */
  void loop(final int nsPre, final int c) {
    if(c == 0) nspaces.root(nsPre, data);
    while(!preStack.isEmpty() && preStack.peek() > nsPre) nspaces.close(preStack.pop());
  }

  /**
   * Opens a new level.
   * @param pre pre value
   */
  void open(final int pre) {
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
    // collect new namespaces
    final Atts ns = new Atts();
    final int as = nsp.size();
    for(int a = 0; a < as; a++) {
      final byte[] prefix = nsp.name(a), uri = nsp.value(a);
      final int uriId = nspaces.uriIdForPrefix(prefix, true);
      if(uriId == 0 || uriId != nspaces.uriId(uri)) ns.add(prefix, uri);
    }
    nspaces.open(pre, ns);
    preStack.push(pre);
    return !ns.isEmpty();
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
    nspaces.cursor(root);
  }
}
