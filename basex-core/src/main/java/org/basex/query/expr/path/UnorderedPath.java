package org.basex.query.expr.path;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Iterative expression for paths that return distinct nodes in arbitrary order.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class UnorderedPath extends IterPath {
  /**
   * Constructor.
   * @param info input info (can be {@code null})
   * @param root root expression (can be {@code null})
   * @param steps axis steps
   */
  UnorderedPath(final InputInfo info, final Expr root, final Expr... steps) {
    super(info, root, steps);
  }

  @Override
  public boolean ddo() {
    return false;
  }

  @Override
  public UnorderedPath copy(final CompileContext cc, final IntObjectMap<Var> vm) {
    final Expr rt = root == null ? null : root.copy(cc, vm);
    return copyCache(new UnorderedPath(info, rt, Arr.copyAll(cc, vm, steps)));
  }
}
