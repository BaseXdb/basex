package org.basex.query.func.fn;

import static org.basex.query.QueryError.*;
import static org.basex.query.func.Function.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-23, BSD License
 * @author Christian Gruen
 */
public final class FnError extends StandardFunc {
  @Override
  public Iter iter(final QueryContext qc) {
    return new Iter() {
      @Override
      public Item next() throws QueryException {
        return item(qc, info);
      }
    };
  }

  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final QNm code = toQNm(arg(0), true, qc);
    final String desc = toStringOrNull(arg(1), qc);
    final Value object = defined(2) ? exprs[2].value(qc) : null;
    throw new QueryException(info, code != null ? code : FUNERR1.qname(),
      desc != null ? desc : FUNERR1.message).value(object);
  }

  @Override
  public boolean vacuous() {
    return true;
  }

  @Override
  protected Expr typeCheck(final TypeCheck tc, final CompileContext cc) {
    return this;
  }

  /**
   * Creates an error function instance.
   * @param ex query exception
   * @param st type of the expression that caused the error message
   * @param sc static context
   * @return function
   */
  public static StandardFunc get(final QueryException ex, final SeqType st,
      final StaticContext sc) {
    Util.debug(ex);
    final Str desc = Str.get(ex.getLocalizedMessage());
    final StandardFunc sf = ERROR.get(sc, ex.info(), ex.qname(), desc);
    sf.exprType.assign(st);
    return sf;
  }
}
