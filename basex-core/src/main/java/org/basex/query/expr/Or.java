package org.basex.query.expr;

import static org.basex.query.QueryText.*;

import org.basex.query.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Or expression.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
public final class Or extends Logical {
  /**
   * Constructor.
   * @param info input info (can be {@code null})
   * @param exprs expressions
   */
  public Or(final InputInfo info, final Expr... exprs) {
    super(info, exprs);
  }

  @Override
  boolean or() {
    return true;
  }

  @Override
  public Or copy(final CompileContext cc, final IntObjMap<Var> vm) {
    return copyType(new Or(info, copyAll(cc, vm, exprs)));
  }

  @Override
  public boolean equals(final Object obj) {
    return this == obj || obj instanceof Or && super.equals(obj);
  }

  @Override
  public void toString(final QueryString qs) {
    qs.tokens(exprs, ' ' + OR + ' ', true);
  }
}
