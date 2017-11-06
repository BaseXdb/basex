package org.basex.query.func.fn;

import org.basex.core.locks.*;
import org.basex.query.func.*;
import org.basex.query.util.*;

/**
 * Context-based function.
 *
 * @author BaseX Team 2005-17, BSD License
 * @author Christian Gruen
 */
public abstract class ContextFn extends StandardFunc {
  @Override
  public final boolean has(final Flag flag) {
    return flag == Flag.CTX && exprs.length == 0 || super.has(flag);
  }

  @Override
  public final boolean accept(final ASTVisitor visitor) {
    return (exprs.length != 0 || visitor.lock(Locking.CONTEXT)) && super.accept(visitor);
  }
}
