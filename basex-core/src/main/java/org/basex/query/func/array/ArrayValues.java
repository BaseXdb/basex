package org.basex.query.func.array;

import java.util.*;

import org.basex.query.*;
import org.basex.query.CompileContext.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.value.*;
import org.basex.query.value.array.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-23, BSD License
 * @author Christian Gruen
 */
public class ArrayValues extends StandardFunc {
  @Override
  public final Iter iter(final QueryContext qc) throws QueryException {
    final XQArray array = toArray(arg(0), qc);

    return new Iter() {
      final Iterator<Value> members = array.members().iterator();
      Iter ir;

      @Override
      public Item next() throws QueryException {
        while(true) {
          if(ir != null) {
            final Item item = qc.next(ir);
            if(item != null) return item;
          }
          if(!members.hasNext()) return null;
          ir = members.next().iter();
        }
      }
      @Override
      public Item get(final long i) throws QueryException {
        return array.get(i).item(qc, info);
      }
      @Override
      public long size() {
        return array.funcType().declType.one() ? array.arraySize() : -1;
      }
    };
  }

  @Override
  public final Value value(final QueryContext qc) throws QueryException {
    final XQArray array = toArray(arg(0), qc);

    final ValueBuilder vb = new ValueBuilder(qc);
    for(final Value member : array.members()) vb.add(member);
    return vb.value(this);
  }

  @Override
  protected final Expr opt(final CompileContext cc) {
    final FuncType ft = arg(0).funcType();
    if(ft instanceof ArrayType) exprType.assign(ft.declType.with(Occ.ZERO_OR_MORE));
    return this;
  }

  @Override
  public Expr simplifyFor(final Simplify mode, final CompileContext cc) throws QueryException {
    final Expr array = arg(0);
    final Expr expr = mode.oneOf(Simplify.STRING, Simplify.NUMBER, Simplify.DATA) &&
        array.funcType() instanceof ArrayType ? array : this;
    return cc.simplify(this, expr, mode);
  }
}
