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
 * Simple map expression: iterative evaluation with two operands (the last one yielding items).
 *
 * @author BaseX Team 2005-21, BSD License
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
  public Iter iter(final QueryContext qc) {
    return new Iter() {
      Iter iter;

      @Override
      public Item next() throws QueryException {
        if(iter == null) iter = exprs[0].iter(qc);

        final QueryFocus qf = qc.focus;
        final Value value = qf.value;
        try {
          do {
            // evaluate left operand
            qf.value = value;
            Item item = qc.next(iter);
            if(item == null) return null;
            // evaluate right operand (yielding an item)
            qf.value = item;
            item = exprs[1].item(qc, info);
            if(item != Empty.VALUE) return item;
          } while(true);
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
      final Iter iter = exprs[0].iter(qc);
      for(Item item; (item = qc.next(iter)) != null;) {
        qf.value = item;
        item = exprs[1].item(qc, info);
        if(item != Empty.VALUE) vb.add(item);
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
