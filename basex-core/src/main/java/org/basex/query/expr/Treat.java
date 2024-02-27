package org.basex.query.expr;

import static org.basex.query.QueryError.*;

import org.basex.query.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Treat as expression.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
public final class Treat extends TypeCheck {
  /**
   * Constructor.
   * @param info input info (can be {@code null})
   * @param expr expression
   * @param seqType sequence type
   */
  public Treat(final InputInfo info, final Expr expr, final SeqType seqType) {
    super(info, expr, seqType, false);
  }

  @Override
  public QueryError error() {
    return NOTREAT_X_X_X;
  }

  @Override
  TypeCheck get(final Expr ex, final SeqType st) {
    return new Treat(info, ex, st);
  }
}
