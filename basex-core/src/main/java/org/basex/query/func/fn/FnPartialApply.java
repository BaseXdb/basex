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
public final class FnPartialApply extends StandardFunc {
  /** The type of parameter "arguments". */
  private static final SeqType ARGS_TYPE = MapType.get(BasicType.POSITIVE_INTEGER,
      Types.ITEM_ZM).seqType();
  /** The name of parameter "arguments". */
  private static final QNm ARGS_NAME = new QNm("arguments");

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    // pre-evaluate if arguments are values: the resulting function call can be inlined
    return values(false, cc) ? value(cc.qc, cc) : this;
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    return value(qc, null);
  }

  /**
   * Creates the partially applied function.
   * @param qc query context
   * @param cc compilation context ({@code null} during runtime)
   * @return function item
   * @throws QueryException query exception
   */
  private Value value(final QueryContext qc, final CompileContext cc) throws QueryException {
    final FItem function = toFunction(arg(0), qc);
    final XQMap arguments = toMap(ARGS_TYPE.coerce(arg(1).value(qc), qc, info, ARGS_NAME, null),
        qc);
    final int arity = function.arity();
    if(arity == 0 || arguments == XQMap.empty()) return function;

    final FuncType ft = function.funcType();
    final Expr[] funcArgs = new Expr[arity];
    Arrays.fill(funcArgs, Empty.UNDEFINED);
    int placeholders = arity;
    for(final XQMap.Entry arg : arguments.entries()) {
      final long index = toLong(arg.key());
      if(index <= arity) {
        final int i = (int) index - 1;
        funcArgs[i] = ft.argTypes[i].coerce(arg.value(), qc, info, function.paramName(i),
            null);
        --placeholders;
      }
    }
    if(placeholders == arity) return function;

    final Var[] params = new Var[placeholders];
    final VarScope vs = new VarScope();
    for(int i = 0, p = 0; i < arity; ++i) {
      if(funcArgs[i] == Empty.UNDEFINED) {
        final Var var = vs.addNew(function.paramName(i), ft.argTypes[i], qc, info);
        params[p++] = var;
        funcArgs[i] = new VarRef(info, var);
      }
    }
    final Expr body = function.funcBody(vs, funcArgs, null, cc, info);

    final FuncType type = FuncType.get(AnnList.EMPTY, ft.declType, params).
        withRefinedType(ft.refinedType);
    return new FuncItem(info, body, params, AnnList.EMPTY, type, vs.stackSize(), null);
  }
}
