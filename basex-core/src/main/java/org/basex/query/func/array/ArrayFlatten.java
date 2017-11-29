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
    final ValueBuilder vb = new ValueBuilder();
    final Iter iter = qc.iter(exprs[0]);
    for(Item it; (it = iter.next()) != null;) add(vb, it, qc);
    return vb.value();
  }

  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    return new Iter() {
      @SuppressWarnings("unchecked")
      private Iterator<Value>[] iters = new Iterator[2];
      private int p = -1;
      private Iter curr = qc.iter(exprs[0]);

      @Override
      public Item next() throws QueryException {
        while(true) {
          final Item it = curr.next();

          if(it != null) {
            if(!(it instanceof Array)) return it;
            final Array arr = (Array) it;
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
   * @param t type
   * @return result type
   */
  private static Type type(final Type t) {
    return t instanceof ArrayType ? type(((ArrayType) t).declType.type) : t;
  }

  /**
   * Recursive helper method for flattening nested arrays.
   * @param vb sequence builder
   * @param item item to be added
   * @param qc query context
   */
  private static void add(final ValueBuilder vb, final Item item, final QueryContext qc) {
    qc.checkStop();
    if(item instanceof Array) {
      for(final Value val : ((Array) item).members()) {
        for(final Item it : val) add(vb, it, qc);
      }
    }
    else vb.add(item);
  }
}
