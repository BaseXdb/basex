package org.basex.query.func.array;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.expr.path.*;
import org.basex.query.iter.*;
import org.basex.query.value.array.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class ArrayOfMembers extends ArrayFn {
  @Override
  protected XQArray item(final QueryContext qc) throws QueryException {
    final Iter input = arg(0).iter(qc);

    final ArrayBuilder ab = new ArrayBuilder(qc, input.size());
    for(Item item; (item = qc.next(input)) != null;) {
      ab.add(toJNode(item).value);
    }
    return ab.array(this);
  }

  @Override
  protected Expr opt(final CompileContext cc) {
    final Type type = arg(0).seqType().type;
    if(type instanceof final NodeType nt && nt.test instanceof final JNodeTest jnt) {
      exprType.assign(ArrayType.get(jnt.valueType));
    }
    return this;
  }

  @Override
  public long structSize() {
    return arg(0).size();
  }
}
