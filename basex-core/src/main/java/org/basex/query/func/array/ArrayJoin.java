package org.basex.query.func.array;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.iter.*;
import org.basex.query.util.list.*;
import org.basex.query.value.*;
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
public final class ArrayJoin extends ArrayFn {
  @Override
  public XQArray item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Expr arrays = arg(0);
    final Item separator = arg(1).item(qc, info);

    // if possible, retrieve single item
    if(arrays.seqType().zeroOrOne()) {
      final Item item = arrays.item(qc, info);
      return item.isEmpty() ? XQArray.empty() : toArray(item);
    }
    final Iter iter = arrays.iter(qc);
    Item item = iter.next();
    if(item == null) return XQArray.empty();

    final ValueList sep = new ValueList();
    if(!separator.isEmpty()) {
      for(final Value value : toArray(separator).iterable()) sep.add(value);
    }

    final ArrayBuilder ab = new ArrayBuilder();
    for(final Value value : toArray(item).iterable()) ab.add(value);
    while((item = qc.next(iter)) != null) {
      for(final Value value : sep) ab.add(value);
      for(final Value value : toArray(item).iterable()) ab.add(value);
    }
    return ab.array(this);
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    final Expr arrays = arg(0), separator = arg(1);
    final SeqType ast = arrays.seqType(), sst = separator.seqType();

    if(ast.type instanceof ArrayType) {
      // remove empty entries
      final Expr[] args = arrays.args();
      if(arrays instanceof List && ((Checks<Expr>) arg -> arg == XQArray.empty()).any(args)) {
        final ExprList list = new ExprList();
        for(final Expr arg : args) {
          if(arg != XQArray.empty()) list.add(arg);
        }
        arg(0, arg -> List.get(cc, info, list.finish()));
      }
      // return simple arguments
      final SeqType st = arg(0).seqType();
      if(st.one()) return arg(0);

      final Type type = sst.type instanceof ArrayType ? st.type.union(sst.type) :
        sst.zero() ? st.type : null;
      if(type != null) exprType.assign(type);
    }
    return this;
  }
}
