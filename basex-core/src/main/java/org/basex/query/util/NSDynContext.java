package org.basex.query.util;

import static org.basex.util.Token.*;

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
    if(stack != null) {
      for(int s = stack.size() - 1; s >= 0; s--) {
        if(eq(stack.name(s), prefix)) return stack.value(s);
      }
    }
    return sc.ns.resolveStatic(prefix);
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
    if(stack != null) {
      for(int s = stack.size() - 1; s >= 0; s--) {
        final byte[] nm = stack.name(s);
        if(!atts.contains(nm)) atts.add(nm, stack.value(s));
      }
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
