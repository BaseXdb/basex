package org.basex.query.func.array;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.iter.*;
import org.basex.query.value.*;
import org.basex.query.value.array.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-18, BSD License
 * @author Christian Gruen
 * @author Leo Woerteler
 */
public final class ArrayFlatten extends ArrayFn {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final ValueBuilder vb = new ValueBuilder(qc);
    add(vb, exprs[0], qc);
    return vb.value();
  }

  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    return value(qc).iter();
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    final Expr expr = exprs[0];
    final SeqType st = expr.seqType();
    if(!st.mayBeArray()) return expr;
    exprType.assign(type(st.type));
    return this;
  }

  /**
   * Recursive helper method for retrieving static result type.
   * @param type type
   * @return result type
   */
  private static Type type(final Type type) {
    return type instanceof ArrayType ? type(((ArrayType) type).declType.type) : type;
  }

  /**
   * Recursive helper method for flattening nested arrays.
   * @param vb sequence builder
   * @param expr expression
   * @param qc query context
   * @throws QueryException query exception
   */
  private static void add(final ValueBuilder vb, final Expr expr, final QueryContext qc)
      throws QueryException {
    final Iter iter = expr.iter(qc);
    for(Item item; (item = qc.next(iter)) != null;) {
      if(item instanceof Array) {
        for(final Value value : ((Array) item).members()) add(vb, value, qc);
      }
      else vb.add(item);
    }
  }
}
