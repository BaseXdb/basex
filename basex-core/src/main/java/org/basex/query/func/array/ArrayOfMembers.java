package org.basex.query.func.array;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.iter.*;
import org.basex.query.value.array.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class ArrayOfMembers extends ArrayFn {
  @Override
  public XQArray item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Iter input = arg(0).iter(qc);

    final ArrayBuilder ab = new ArrayBuilder(qc);
    for(Item item; (item = qc.next(input)) != null;) {
      ab.add(toRecord(item, SeqType.MEMBER, qc).get(Str.VALUE));
    }
    return ab.array(this);
  }

  @Override
  protected Expr opt(final CompileContext cc) {
    final Expr array = arg(0);
    final Type type = array.seqType().type;
    if(type instanceof MapType) {
      exprType.assign(ArrayType.get(((MapType) type).valueType()));
    }
    return this;
  }

  @Override
  public long structSize() {
    return arg(0).size();
  }
}
