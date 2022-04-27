package org.basex.query.func.fn;

import static org.basex.query.value.type.AtomType.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-22, BSD License
 * @author Christian Gruen
 */
public final class FnNumber extends ContextFn {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Item item = ctxArg(0, qc).atomItem(qc, info);
    if(item == Empty.VALUE) return Dbl.NAN;
    if(item.type == DOUBLE) return item;
    try {
      if(info != null) info.internal(true);
      return DOUBLE.cast(item, qc, sc, info);
    } catch(final QueryException ex) {
      Util.debug(ex);
      return Dbl.NAN;
    } finally {
      if(info != null) info.internal(false);
    }
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    final boolean context = contextAccess();
    final Expr expr = context ? cc.qc.focus.value : exprs[0];
    final Type type = argType(cc);

    // number(1e1)  ->  1e1
    // $double[number() = 1]  ->  $double[. = 1]
    if(type == DOUBLE) return context && cc.nestedFocus() ? ContextValue.get(cc, info) : expr;

    // number(string(ITEM))
    // number(xs:untypedAtomic(ITEM))
    // number(ITEM promote to xs:untypedAtomic)  ->  number(ITEM)
    if(type != null) {
      final Expr arg = simplify(expr, cc);
      if(arg != null) return cc.function(Function.NUMBER, info, arg);
    }
    return this;
  }

  /**
   * Returns the atomic argument type.
   * @param cc compilation context
   * @return argument type or {@code null}
   */
  private Type argType(final CompileContext cc) {
    final Expr expr = contextAccess() ? cc.qc.focus.value : exprs[0];
    if(expr != null) {
      final SeqType st = expr.seqType();
      if(st.one()) return st.type.atomic();
    }
    return null;
  }

  /**
   * Simplifies a numeric cast.
   * @param arg argument
   * @param cc compilation context
   * @return embedded argument or {@code null}
   */
  public static Expr simplify(final Expr arg, final CompileContext cc) {
    final Type type = arg.seqType().type;
    if(Function.STRING.is(arg)) {
      return ((FnString) arg).contextAccess() ? ContextValue.get(cc, arg.info()) : arg.arg(0);
    } else if(arg instanceof Cast && type.isStringOrUntyped()) {
      return ((Cast) arg).expr;
    } else if(arg instanceof TypeCheck && (type.isUntyped() || type == ANY_ATOMIC_TYPE)) {
      final Expr expr = ((TypeCheck) arg).expr;
      if(expr.seqType().one() && expr.seqType().type.isUntyped()) return expr;
    }
    return null;
  }
}
