package org.basex.query.func.array;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.iter.*;
import org.basex.query.value.array.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class ArrayMembers extends ArrayFn {
  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    final XQArray array = toArray(arg(0), qc);
    final JNode root = new JNode(array);

    return new BasicIter<>(array.structSize()) {
      @Override
      public Item get(final long i) {
        return new JNode(root, (int) i);
      }
    };
  }

  @Override
  protected Expr opt(final CompileContext cc) {
    final Expr array = arg(0);
    final SeqType st = array.seqType().type instanceof final ArrayType at ?
      NodeType.get(null, at.valueType()).seqType(Occ.ZERO_OR_MORE) : seqType();
    exprType.assign(st, Occ.ZERO_OR_MORE, array.structSize());
    return this;
  }
}
