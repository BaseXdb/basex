package org.basex.query.func.fn;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
public class FnIntersperse extends StandardFunc {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final Iter input = arg(0).iter(qc);
    final Value separator = arg(1).value(qc);

    final ValueBuilder vb = new ValueBuilder(qc);
    Item item = input.next();
    if(item != null) {
      vb.add(item);
      while((item = qc.next(input)) != null) vb.add(separator).add(item);
    }
    return vb.value(this);
  }

  @Override
  protected Expr opt(final CompileContext cc) {
    final Expr values = arg(0), separator = arg(1);
    final SeqType st = values.seqType(), stSep = separator.seqType();
    if(st.zeroOrOne() || separator == Empty.VALUE) return values;

    final long size = values.size(), sizeSep = separator.size();
    final long sz = size != -1 && sizeSep != -1 ? size + sizeSep * (size - 1) : -1;
    exprType.assign(st.union(stSep), st.occ, sz).data(values, separator);

    return this;
  }
}
