package org.basex.data;

import org.basex.util.*;
import org.basex.util.list.*;

/**
 * This class organizes namespace scopes.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
final class NSScope {
  /** Data reference. */
  private final Data data;
  /** Namespaces. */
  private final Namespaces nspaces;
  /** Stack with PRE values. */
  private final IntList preStack = new IntList();
  /** Root namespace. */
  private final NSNode root;

  /**
   * Default constructor.
   * @param pre PRE value
   * @param count number of nodes to be inserted
   * @param data data reference
   */
  NSScope(final int pre, final int count, final Data data) {
    this.data = data;
    nspaces = data.nspaces;
    root = nspaces.cursor();
    // move existing namespace nodes behind the inserted nodes
    for(final NSNode node : nspaces.cache(pre)) node.incrementPre(count);
  }

  /**
   * Refreshes the namespace structure.
   * @param nsPre PRE value with namespaces
   * @param c insertion counter
   */
  void loop(final int nsPre, final int c) {
    if(c == 0) nspaces.root(nsPre, data);
    while(!preStack.isEmpty() && preStack.peek() > nsPre) nspaces.close(preStack.pop());
  }

  /**
   * Opens a new level.
   * @param pre PRE value
   */
  void open(final int pre) {
    nspaces.open();
    preStack.push(pre);
  }

  /**
   * Parses the specified namespaces and returns all namespaces that are not declared yet.
   * @param pre PRE value
   * @param nsp source namespaces
   * @return {@code true} if new namespaces were added
   */
  boolean open(final int pre, final Atts nsp) {
    // collect new namespaces
    final Atts ns = new Atts();
    final int as = nsp.size();
    for(int a = 0; a < as; a++) {
      final byte[] prefix = nsp.name(a), uri = nsp.value(a);
      if(nspaces.declare(nspaces.uriIdForPrefix(prefix, true), uri)) ns.add(prefix, uri);
    }
    nspaces.open(pre, ns);
    preStack.push(pre);
    return !ns.isEmpty();
  }

  /**
   * Closes the namespace scope.
   */
  void close() {
    while(!preStack.isEmpty()) nspaces.close(preStack.pop());
    nspaces.cursor(root);
  }
}
