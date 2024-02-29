package org.basex.query.func.array;

import java.util.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.value.*;
import org.basex.query.value.array.*;
import org.basex.query.value.item.*;
import org.basex.query.value.map.*;
import org.basex.query.value.type.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
public final class ArrayMembers extends StandardFunc {
  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    final XQArray array = toArray(arg(0), qc);

    return new Iter() {
      final Iterator<Value> members = array.members().iterator();

      @Override
      public XQMap next() throws QueryException {
        return members.hasNext() ? record(members.next()) : null;
      }
      @Override
      public Item get(final long i) throws QueryException {
        return record(array.get(i));
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
    for(final Value member : array.members()) vb.add(record(member));
    return vb.value(this);
  }

  @Override
  protected Expr opt(final CompileContext cc) {
    final FuncType ft = arg(0).funcType();
    if(ft instanceof ArrayType) exprType.assign(MapType.get(AtomType.STRING, ft.declType));
    return this;
  }

  /**
   * Creates a value record.
   * @param value value of the record
   * @return map
   * @throws QueryException query exception
   */
  private static XQMap record(final Value value) throws QueryException {
    return XQMap.singleton(Str.VALUE, value);
  }
}
