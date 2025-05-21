package org.basex.query.func.fn;

import static org.basex.query.func.Function.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.iter.*;
import org.basex.query.util.collation.*;
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
public final class FnSort extends FnSortBy {
  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    final Value input = arg(0).value(qc), value = quickValue(input);
    return value != null ? value.iter() : iter(input, qc);
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final Value input = arg(0).value(qc), value = quickValue(input);
    return value != null ? value : iter(input, qc).value(qc, this);
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    // optimize sort on sequences
    final Expr input = arg(0);
    final SeqType st = input.seqType();
    if(st.zero()) return input;

    if(defined(2)) {
      arg(2, arg -> refineFunc(arg, cc, st.with(Occ.EXACTLY_ONE)));
    } else if(!defined(1)) {
      if(st.zeroOrOne() && st.type.isSortable()) {
        return input;
      } else if(input instanceof Value) {
        final Value value = quickValue((Value) input);
        if(value != null) return value;
      } else if(REVERSE.is(input) || SORT.is(input)) {
        // sort(reverse(EXPR))  ->  sort(EXPR)
        final Expr[] args = exprs.clone();
        args[0] = args[0].arg(0);
        return cc.function(SORT, info, args);
      } else if(REPLICATE.is(input) && ((FnReplicate) input).singleEval(false)) {
        // sort(replicate(10, 5))  ->  replicate(10, 5)
        final SeqType rst = input.arg(0).seqType();
        if(rst.zeroOrOne() && rst.type.isSortable()) return input;
      } else if(_DB_NODE_PRE.is(input) && input.arg(0).ddo()) {
        // sort(db:node-pre(db:text(...)))  ->  db:node-pre(db:text(...))
        return input;
      }
    }
    return adoptType(input);
  }

  /**
   * Evaluates value arguments.
   * @param input input value
   * @return sorted value or {@code null}
   */
  private Value quickValue(final Value input) {
    if(exprs.length == 1) {
      // range values
      if(input instanceof final RangeSeq rs) {
        return rs.ascending() ? rs : rs.reverse(null);
      }
      // sortable single or singleton values
      final SeqType st = input.seqType();
      if(st.type.isSortable() && (st.one() || input instanceof SingletonSeq &&
          ((SingletonSeq) input).singleItem())) return input;
    }
    return null;
  }

  @Override
  protected Integer[] index(final Value[] values, final QueryContext qc) throws QueryException {
    final FItem[] keys = { toFunctionOrNull(arg(2), 1, qc) };
    final Collation[] collations = { toCollation(arg(1), qc) };
    final boolean[] invert = { false };
    return index(values, keys, collations, invert, qc);
  }

  @Override
  public int hofIndex() {
    return 2;
  }
}
