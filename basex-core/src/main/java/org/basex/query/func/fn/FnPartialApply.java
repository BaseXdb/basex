package org.basex.query.func.fn;

import java.util.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.util.list.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.map.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Gunther Rademacher
 */
public class FnPartialApply extends StandardFunc {
  /** The type of parameter "arguments". */
  private static final SeqType ARGS_TYPE = MapType.get(AtomType.POSITIVE_INTEGER,
      Types.ITEM_ZM).seqType();
  /** The name of parameter "arguments". */
  private static final QNm ARGS_NAME = new QNm("arguments");

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final FItem function = toFunction(arg(0), qc);
    final XQMap arguments = toMap(ARGS_TYPE.coerce(arg(1).value(qc), ARGS_NAME, qc, null, info),
        qc);
    final int arity = function.arity();
    if(arity == 0 || arguments == XQMap.empty()) return function;

    final FuncType ft = function.funcType();
    final Expr[] funcArgs = new Expr[arity];
    Arrays.fill(funcArgs, Empty.UNDEFINED);
    int placeholders = arity;
    for(final Item key : arguments.keys()) {
      final long index = toLong(key);
      if(index <= arity) {
        final int i = (int) index - 1;
        funcArgs[i] = ft.argTypes[i].coerce(arguments.get(key), function.paramName(i), qc, null,
            info);
        --placeholders;
      }
    }
    if(placeholders == arity) return function;

    final DynFuncCall funcCall;
    final Var[] params = new Var[placeholders];
    final SeqType[] argTypes = new SeqType[placeholders];
    if(placeholders == 0) {
      funcCall = new DynFuncCall(info, function, funcArgs);
    } else {
      final VarScope vs = new VarScope();
      final Expr[] args = new Expr[placeholders];
      int p = 0;
      for(int i = 0; i < arity; ++i) {
        if(funcArgs[i] == Empty.UNDEFINED) {
          final Var var = vs.addNew(function.paramName(i), ft.argTypes[i], qc, info);
          params[p] = var;
          args[p++] = new VarRef(info, var);
        }
      }
      funcCall = new DynFuncCall(info,
          new PartFunc(info, ExprList.concat(funcArgs, function), placeholders, null), args);
    }
    return new FuncItem(info, funcCall, params, AnnList.EMPTY, FuncType.get(ft.declType, argTypes),
        params.length, null);
  }

  @Override
  public int hofIndex() {
    return 0;
  }
}