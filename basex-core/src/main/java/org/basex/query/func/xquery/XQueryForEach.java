package org.basex.query.func.xquery;

import java.util.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.func.xquery.TaskContext.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class XQueryForEach extends StandardFunc {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final Value input = arg(0).value(qc);
    final FItem action = toFunction(arg(1), 2, qc);
    final TaskOptions options = toOptions(arg(2), new TaskOptions(), qc);

    final long size = input.size();
    if(size == 0) return Empty.VALUE;

    // pass on positional parameter if the action expects two arguments
    final boolean pos = action.arity() == 2;
    final ArrayList<Call> calls = new ArrayList<>(Seq.initialCapacity(size));
    long p = 0;
    for(final Item item : input) {
      calls.add(new Call(action, pos ? new Value[] { item, Itr.get(++p) } : new Value[] { item }));
    }
    final TaskContext tc = new TaskContext(options, qc, info);
    return tc.invoke(new XQueryTask(tc, calls));
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    final Expr input = arg(0), action = arg(1);
    if(input.seqType().zero()) return input;
    // default options: results are collected in input order
    if(arg(2) == Empty.UNDEFINED) {
      final FuncType ft = action.funcType();
      if(ft != null) {
        final SeqType dt = ft.refinedType;
        exprType.assign(dt.with(dt.occ.multiply(input.seqType().occ)));
      }
    }
    return this;
  }
}
