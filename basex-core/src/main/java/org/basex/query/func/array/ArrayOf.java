package org.basex.query.func.array;

import static org.basex.query.QueryError.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.iter.*;
import org.basex.query.value.*;
import org.basex.query.value.array.*;
import org.basex.query.value.item.*;
import org.basex.query.value.map.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-23, BSD License
 * @author Christian Gruen
 */
public final class ArrayOf extends ArrayFn {
  @Override
  public XQArray item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Iter members = arg(0).iter(qc);

    final ArrayBuilder ab = new ArrayBuilder();
    for(Item item; (item = qc.next(members)) != null;) ab.append(toMember(item));
    return ab.array(this);
  }

  @Override
  protected Expr opt(final CompileContext cc) {
    final FuncType ft = arg(0).funcType();
    if(ft instanceof MapType) exprType.assign(ArrayType.get(ft.declType));
    return this;
  }

  /**
   * Returns an array member.
   * @param item item to check
   * @return member
   * @throws QueryException query exception
   */
  private Value toMember(final Item item) throws QueryException {
    final XQMap map = toMap(item);
    if(map.mapSize() == 1 && map.contains(Str.VALUE, info)) return map.get(Str.VALUE, info);
    throw INVCONVERT_X_X_X.get(info, item.type, "record(value as item()*)", item);
  }
}
