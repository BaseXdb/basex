package org.basex.query.util;

import org.basex.query.*;
import org.basex.util.*;

/**
 * Dynamic namespace context (stack), owned by a QueryContext and duplicated on fork.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class NSDynContext {
  /** Dynamically added namespaces (can be {@code null}). */
  private Atts stack;

  /**
   * Copy constructor.
   * @param ns dynamic context to copy (can be {@code null})
   */
  public NSDynContext(final NSDynContext ns) {
    stack = ns != null && ns.stack != null ? new Atts(ns.stack) : null;
  }

  /**
   * Resolves a prefix in the dynamic stack, statically declared, and predefined namespaces.
   * @param prefix prefix
   * @param sc static context
   * @return uri or {@code null} if not found
   */
  public byte[] resolve(final byte[] prefix, final StaticContext sc) {
    final byte[] uri = stack != null ? stack.lastValue(prefix) : null;
    return uri != null ? uri : sc.ns.resolveStatic(prefix);
  }

  /**
   * Returns the number of dynamic namespaces.
   * @return namespaces
   */
  public int size() {
    return stack != null ? stack.size() : 0;
  }

  /**
   * Sets the number of dynamic namespaces.
   * @param size number of namespaces
   */
  public void size(final int size) {
    if(size == 0 && stack == null) return;
    stack().size(size);
  }

  /**
   * Adds a namespace to the namespace stack.
   * @param prefix namespace prefix
   * @param uri namespace URI
   */
  public void add(final byte[] prefix, final byte[] uri) {
    stack().add(prefix, uri);
  }

  /**
   * Adds the namespaces that are currently in scope.
   * @param atts namespaces
   */
  public void inScope(final Atts atts) {
    if(stack == null) return;
    final int ss = stack.size();
    for(int s = 0; s < ss; s++) {
      // later bindings of a prefix shadow earlier ones
      final byte[] prefix = stack.name(s);
      if(!atts.contains(prefix)) atts.add(prefix, stack.lastValue(prefix));
    }
  }

  /**
   * Returns the namespace stack.
   * @return stack
   */
  private Atts stack() {
    if(stack == null) stack = new Atts();
    return stack;
  }
}
