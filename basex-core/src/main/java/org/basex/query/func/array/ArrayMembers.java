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
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class ArrayMembers extends StandardFunc {
  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    final XQArray array = toArray(arg(0), qc);

    return new Iter() {
      final Iterator<Value> values = array.iterable().iterator();

      @Override
      public XQMap next() {
        return values.hasNext() ? record(values.next()) : null;
      }
      @Override
      public Item get(final long i) {
        return record(array.get(i));
      }
      @Override
      public long size() {
        return array.structSize();
      }
    };
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final XQArray array = toArray(arg(0), qc);

    final ValueBuilder vb = new ValueBuilder(qc);
    for(final Value member : array.iterable()) vb.add(record(member));
    return vb.value(this);
  }

  @Override
  protected Expr opt(final CompileContext cc) {
    final Expr array = arg(0);
    final Type type = array.seqType().type;
    if(type instanceof ArrayType) {
      final MapType mt = MapType.get(AtomType.STRING, ((ArrayType) type).valueType());
      exprType.assign(mt.seqType(Occ.ZERO_OR_MORE), array.structSize());
    }
    return this;
  }

  /**
   * Creates a value record.
   * @param value value of the record
   * @return map
   */
  private static XQMap record(final Value value) {
    return XQMap.singleton(Str.VALUE, value);
  }
}
