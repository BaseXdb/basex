package org.basex.query.func.array;

import static org.basex.query.func.Function.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.util.collation.*;
import org.basex.query.value.*;
import org.basex.query.value.array.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class ArraySort extends ArraySortBy {
  @Override
  protected XQArray item(final QueryContext qc) throws QueryException {
    final XQArray array = toArray(arg(0), qc), value = quickValue(array, qc);
    return value != null ? value : sort(array, qc);
  }

  @Override
  protected Integer[] index(final Value[] values, final QueryContext qc) throws QueryException {
    // identical to {@link FnSort#index}
    final FItem[] keys = { toFunctionOrNull(arg(2), 1, qc) };
    final Collation[] collations = { toCollation(arg(1), qc) };
    final boolean[] invert = { false };
    return index(values, keys, collations, invert, qc);
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    // optimize sort on arrays
    final Expr array = arg(0);
    final long size = structSize();
    if(size == 0) return array;

    if(array.seqType().type instanceof final ArrayType at) {
      if(defined(2)) {
        arg(2, arg -> refineFunc(arg, cc, at.valueType()));
      } else if(!defined(1)) {
        if(size == 1) {
          // array:sort([ MEMBER ]) → [ MEMBER ]
          return array;
        } else if(array instanceof final XQArray value) {
          final XQArray quick = quickValue(value, cc.qc);
          if(quick != null) return quick;
        } else if(_ARRAY_SORT.is(array) && array.args().length == 1) {
          // array:sort(array:sort(EXPR)) → array:sort(EXPR)
          return array;
        }
      }
      exprType.assign(at);
    }
    return this;
  }

  /**
   * Evaluates value arguments.
   * @param array input array
   * @param qc query context
   * @return sorted array or {@code null}
   */
  private XQArray quickValue(final XQArray array, final QueryContext qc) {
    if(exprs.length == 1) {
      // arrays with at most one member
      if(array.structSize() < 2) return array;
      if(array instanceof final ItemArray ia) {
        final Value members = ia.items(qc);
        // range values
        if(members instanceof final RangeSeq rs) {
          return XQArray.items(rs.ascending() ? rs : rs.reverse(null));
        }
        // integers, strings, etc.
        if(members instanceof final NativeSeq ns && sc().collation == null) {
          return XQArray.items(ns.sort());
        }
        // repeated sortable member
        if(members instanceof final SingletonSeq ss && ss.singleItem() &&
            members.seqType().type.isSortable()) return array;
      }
    }
    return null;
  }
}
