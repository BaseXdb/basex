package org.basex.query.func.fn;

import org.basex.core.locks.*;
import org.basex.query.func.*;
import org.basex.query.util.*;

/**
 * Context-based function.
 *
 * @author BaseX Team 2005-18, BSD License
 * @author Christian Gruen
 */
public abstract class ContextFn extends StandardFunc {
  @Override
  public final boolean has(final Flag... flags) {
    return Flag.CTX.in(flags) && exprs.length == 0 || super.has(flags);
  }

  @Override
  public final boolean accept(final ASTVisitor visitor) {
    return (exprs.length != 0 || visitor.lock(Locking.CONTEXT)) && super.accept(visitor);
  }
}
