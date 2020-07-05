package org.basex.query.expr;

import org.basex.query.*;
import org.basex.query.iter.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Simple map expression: iterative evaluation (no positional access).
 *
 * @author BaseX Team 2005-20, BSD License
 * @author Christian Gruen
 */
public final class IterMap extends SimpleMap {
  /** Item-based or iterative processing. */
  private final boolean[] items;

  /**
   * Constructor.
   * @param info input info
   * @param exprs expressions
   */
  IterMap(final InputInfo info, final Expr... exprs) {
    super(info, exprs);

    final int el = exprs.length;
    items = new boolean[el];
    for(int e = 0; e < el; e++) items[e] = exprs[e].seqType().zeroOrOne();
  }

  @Override
  public Iter iter(final QueryContext qc) {
    final QueryFocus qf = qc.focus, focus = new QueryFocus();
    final int el = exprs.length;
    final Iter[] iter = new Iter[el];
    final Value[] values = new Value[el];
    values[0] = qc.focus.value;

    return new Iter() {
      private int pos;

      @Override
      public Item next() throws QueryException {
        qc.focus = focus;
        try {
          do {
            focus.value = values[pos];
            Iter ir = iter[pos];
            if(items[pos]) {
              // item-based processing
              if(ir == Empty.ITER) {
                iter[pos--] = null;
              } else {
                final Item item = exprs[pos].item(qc, info);
                if(item == Empty.VALUE) {
                  pos--;
                } else {
                  iter[pos] = Empty.ITER;
                  if(pos < el - 1) {
                    values[++pos] = item;
                  } else {
                    return item;
                  }
                }
              }
            } else {
              // iterative processing
              if(ir == null) {
                ir = exprs[pos].iter(qc);
                iter[pos] = ir;
              }
              final Item item = qc.next(ir);
              if(item == null) {
                iter[pos--] = null;
              } else if(pos < el - 1) {
                values[++pos] = item;
              } else {
                return item;
              }
            }
          } while(pos != -1);
          return null;
        } finally {
          qc.focus = qf;
        }
      }
    };
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    return iter(qc).value(qc, this);
  }

  @Override
  public IterMap copy(final CompileContext cc, final IntObjMap<Var> vm) {
    return copyType(new IterMap(info, Arr.copyAll(cc, vm, exprs)));
  }

  @Override
  public String description() {
    return "iterative " + super.description();
  }
}
