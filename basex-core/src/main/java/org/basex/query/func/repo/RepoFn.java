package org.basex.query.func.repo;

import org.basex.core.locks.*;
import org.basex.query.func.*;
import org.basex.query.util.*;

/**
 * Functions on EXPath packages.
 *
 * @author BaseX Team 2005-16, BSD License
 * @author Christian Gruen
 */
abstract class RepoFn extends StandardFunc {
  @Override
  public final boolean accept(final ASTVisitor visitor) {
    return visitor.lock(DBLocking.REPO) && super.accept(visitor);
  }
}
