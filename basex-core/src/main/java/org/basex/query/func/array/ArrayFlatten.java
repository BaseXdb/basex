package org.basex.query.func.array;

import java.util.*;

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
    return new Iter() {
      private final LinkedList<Iterator<Value>> iters = new LinkedList<>();
      private final Iter iter = exprs[0].iter(qc);
      private Iter curr = iter;

      @Override
      public Item next() throws QueryException {
        while(true) {
          final Item item = qc.next(curr);
          if(item instanceof Array) {
            iters.add(((Array) item).iterator(0));
          } else if(item != null) {
            return item;
          } else if(iters.isEmpty()) {
            return null;
          }
          curr = nextIter();
        }
      }

      private Iter nextIter() {
        for(; !iters.isEmpty(); iters.removeLast()) {
          final Iterator<Value> ir = iters.getLast();
          if(ir.hasNext()) return ir.next().iter();
        }
        return iter;
      }
    };
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    exprType.assign(type(exprs[0].seqType().type));
    return this;
  }

  /**
   * Recursive helper method for retrieving result type.
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
