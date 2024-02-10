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
 */
public class ArraySplit extends ArrayFn {
  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    final XQArray array = toArray(arg(0), qc);

    return new Iter() {
      final Iterator<Value> members = array.members().iterator();

      @Override
      public XQArray next() {
        return members.hasNext() ? XQArray.singleton(members.next()) : null;
      }
      @Override
      public Item get(final long i) {
        return XQArray.singleton(array.get(i));
      }
      @Override
      public long size() {
        return array.arraySize();
      }
    };
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final XQArray array = toArray(arg(0), qc);

    final ValueBuilder vb = new ValueBuilder(qc);
    for(final Value member : array.members()) vb.add(XQArray.singleton(member));
    return vb.value();
  }

  @Override
  protected Expr opt(final CompileContext cc) {
    final Expr array = arg(0);
    if(array == XQArray.empty()) return Empty.VALUE;

    final FuncType ft = array.funcType();
    if(ft instanceof ArrayType) exprType.assign(ft.seqType(Occ.ZERO_OR_MORE));
    return this;
  }
}
