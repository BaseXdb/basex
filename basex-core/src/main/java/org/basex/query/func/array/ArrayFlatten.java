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
 * @author BaseX Team 2005-17, BSD License
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
      @SuppressWarnings("unchecked")
      private Iterator<Value>[] iters = new Iterator[2];
      private int p = -1;
      private Iter curr = exprs[0].iter(qc);

      @Override
      public Item next() throws QueryException {
        while(true) {
          final Item item = qc.next(curr);
          if(item != null) {
            if(!(item instanceof Array)) return item;
            final Array arr = (Array) item;
            if(++p == iters.length) {
              @SuppressWarnings("unchecked")
              final Iterator<Value>[] temp = new Iterator[2 * p];
              System.arraycopy(iters, 0, temp, 0, p);
              iters = temp;
            }
            iters[p] = arr.iterator(0);
          } else if(p < 0) {
            return null;
          }

          while(!iters[p].hasNext()) {
            iters[p] = null;
            if(--p < 0) return null;
          }

          curr = iters[p].next().iter();
        }
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
