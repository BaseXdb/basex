package org.basex.query.func.map;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.map.*;
import org.basex.query.value.type.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-23, BSD License
 * @author Leo Woerteler
 */
public final class MapKeys extends StandardFunc {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final XQMap map = toMap(arg(0), qc);
    if(!defined(1)) return map.keys();

    final FItem predicate = toFunction(arg(1), 1, qc);
    final ValueBuilder vb = new ValueBuilder(qc);
    map.apply((key, value) -> {
      if(toBoolean(eval(predicate, qc, value).item(qc, info))) vb.add(key);
    });
    return vb.value(this);
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    final FuncType type = arg(0).funcType();
    if(type instanceof MapType) {
      if(defined(1)) arg(1, arg -> coerceFunc(arg, cc, SeqType.BOOLEAN_O, type.declType));
      exprType.assign(((MapType) type).keyType());
    }
    return this;
  }
}
