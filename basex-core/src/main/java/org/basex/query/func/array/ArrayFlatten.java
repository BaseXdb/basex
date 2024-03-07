package org.basex.query.func.array;

import java.util.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.iter.*;
import org.basex.query.value.*;
import org.basex.query.value.array.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 * @author Leo Woerteler
 */
public final class ArrayFlatten extends ArrayFn {
  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    final Deque<Iter> stack = new ArrayDeque<>();
    stack.push(arg(0).iter(qc));

    return new Iter() {
      @Override
      public Item next() throws QueryException {
        while(!stack.isEmpty()) {
          final Item next = stack.peek().next();
          if(next == null) {
            stack.pop();
          } else if(next instanceof XQArray) {
            final Iterator<Value> members = ((XQArray) next).members().iterator();
            stack.push(new Iter() {
              private Iter iter = Empty.ITER;

              @Override
              public Item next() throws QueryException {
                while(true) {
                  final Item item = iter.next();
                  if(item != null) return item;
                  if(!members.hasNext()) return null;
                  final Value value = members.next();
                  if(value.isItem()) {
                    iter = Empty.ITER;
                    return (Item) value;
                  }
                  iter = value.iter();
                }
              }
            });
          } else {
            return next;
          }
        }
        return null;
      }
    };
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final ValueBuilder vb = new ValueBuilder(qc);
    add(vb, arg(0), qc);
    return vb.value(this);
  }

  @Override
  protected Expr opt(final CompileContext cc) {
    final Expr input = arg(0);
    final SeqType st = input.seqType();
    if(!st.mayBeArray()) return input;
    exprType.assign(type(st.type));
    return this;
  }

  /**
   * Recursive helper method for retrieving static result type.
   * @param type type
   * @return result type
   */
  private static Type type(final Type type) {
    return type instanceof ArrayType ? type(((ArrayType) type).memberType.type) : type;
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
      if(item instanceof XQArray) {
        for(final Value value : ((XQArray) item).members()) add(vb, value, qc);
      } else {
        vb.add(item);
      }
    }
  }
}
