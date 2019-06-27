package org.basex.query.func.fn;

import org.basex.core.locks.*;
import org.basex.query.func.*;
import org.basex.query.util.*;

/**
 * Context-based function.
 *
 * @author BaseX Team 2005-19, BSD License
 * @author Christian Gruen
 */
public abstract class ContextFn extends StandardFunc {
  /**
   * Indicates if the function access the current context.
   * @return result of check
   */
  boolean contextAccess() {
    return exprs.length == 0;
  }

  @Override
  public final boolean has(final Flag... flags) {
    return Flag.CTX.in(flags) && contextAccess() || super.has(flags);
  }

  @Override
  public final boolean accept(final ASTVisitor visitor) {
    return (!contextAccess() || visitor.lock(Locking.CONTEXT, false)) && super.accept(visitor);
  }
}
