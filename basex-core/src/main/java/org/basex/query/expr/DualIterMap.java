package org.basex.query.expr;

import org.basex.query.*;
import org.basex.query.iter.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Simple map expression: iterative evaluation with two operands.
 *
 * @author BaseX Team 2005-23, BSD License
 * @author Christian Gruen
 */
public final class DualIterMap extends SimpleMap {
  /**
   * Constructor.
   * @param info input info
   * @param exprs expressions
   */
  DualIterMap(final InputInfo info, final Expr... exprs) {
    super(info, exprs);
  }

  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    return new Iter() {
      final Expr expr1 = exprs[0], expr2 = exprs[1];
      final Iter iter1 = expr1.iter(qc);
      Iter iter2;
      Item item1;

      @Override
      public Item next() throws QueryException {
        final QueryFocus qf = qc.focus;
        final Value value = qf.value;
        try {
          while(true) {
            // right operand
            if(iter2 != null) {
              qf.value = item1;
              final Item item2 = iter2.next();
              if(item2 != null) return item2;
            }
            // left operand
            qf.value = value;
            item1 = iter1.next();
            if(item1 == null) return null;
            qf.value = item1;
            iter2 = expr2.iter(qc);
          }
        } finally {
          qf.value = value;
        }
      }
    };
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final QueryFocus qf = qc.focus;
    final Value qv = qf.value;
    try {
      final ValueBuilder vb = new ValueBuilder(qc);
      final Iter iter1 = exprs[0].iter(qc);
      for(Item item1; (item1 = qc.next(iter1)) != null;) {
        qf.value = item1;
        final Iter iter2 = exprs[1].iter(qc);
        for(Item item2; (item2 = qc.next(iter2)) != null;) vb.add(item2);
      }
      return vb.value(this);
    } finally {
      qf.value = qv;
    }
  }

  @Override
  public DualIterMap copy(final CompileContext cc, final IntObjMap<Var> vm) {
    return copyType(new DualIterMap(info, Arr.copyAll(cc, vm, exprs)));
  }

  @Override
  public String description() {
    return "iter-iter " + super.description();
  }
}
