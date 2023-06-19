package org.basex.query.expr;

import org.basex.query.*;
import org.basex.query.iter.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Simple map expression: iterative evaluation with two operands (the last one yielding items).
 *
 * @author BaseX Team 2005-23, BSD License
 * @author Christian Gruen
 */
public final class DualMap extends SimpleMap {
  /**
   * Constructor.
   * @param info input info
   * @param exprs expressions
   */
  DualMap(final InputInfo info, final Expr... exprs) {
    super(info, exprs);
  }

  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    return new Iter() {
      final Expr expr1 = exprs[0], expr2 = exprs[1];
      final Iter iter1 = expr1.iter(qc);
      final long size = expr2.size() == 1 ? iter1.size() : -1;

      @Override
      public Item next() throws QueryException {
        qc.checkStop();

        final QueryFocus qf = qc.focus;
        final Value qv = qf.value;
        try {
          Item item;
          do {
            // left operand
            qf.value = qv;
            item = iter1.next();
            if(item == null) break;
            // right operand
            qf.value = item;
            item = expr2.item(qc, info);
          } while(item.isEmpty());
          return item;
        } finally {
          qf.value = qv;
        }
      }

      @Override
      public Item get(final long i) throws QueryException {
        final QueryFocus qf = qc.focus;
        final Value qv = qf.value;
        try {
          qf.value = iter1.get(i);
          return exprs[1].item(qc, info);
        } finally {
          qf.value = qv;
        }
      }

      @Override
      public long size() {
        return size;
      }
    };
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final QueryFocus qf = qc.focus;
    final Value qv = qf.value;
    try {
      final ValueBuilder vb = new ValueBuilder(qc);
      final Iter iter = exprs[0].iter(qc);
      for(Item item; (item = qc.next(iter)) != null;) {
        qf.value = item;
        item = exprs[1].item(qc, info);
        if(!item.isEmpty()) vb.add(item);
        qf.value = qv;
      }
      return vb.value(this);
    } finally {
      qf.value = qv;
    }
  }

  @Override
  public DualMap copy(final CompileContext cc, final IntObjMap<Var> vm) {
    return copyType(new DualMap(info, Arr.copyAll(cc, vm, exprs)));
  }

  @Override
  public String description() {
    return "iterative dual " + super.description();
  }
}
