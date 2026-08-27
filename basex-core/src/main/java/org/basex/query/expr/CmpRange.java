package org.basex.query.expr;

import org.basex.query.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Range comparison.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public abstract class CmpRange extends Single {
  /**
   * Constructor.
   * @param expr expression
   * @param info input info (can be {@code null})
   */
  CmpRange(final Expr expr, final InputInfo info) {
    super(info, expr, Types.BOOLEAN_O);
  }

  /**
   * Creates a comparison with the same range and the specified operand.
   * @param operand operand
   * @param cc compilation context
   * @return expression
   * @throws QueryException query exception
   */
  abstract Expr with(Expr operand, CompileContext cc) throws QueryException;
}
