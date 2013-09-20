package org.basex.query.func;

import static org.basex.query.util.Err.*;
import static org.basex.util.Token.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Output functions.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Christian Gruen
 */
public final class FNOut extends StandardFunc {
  /** Newline character. */
  private static final Str NL = Str.get("\n");
  /** Tab character. */
  private static final Str TAB = Str.get("\t");

  /**
   * Constructor.
   * @param sctx static context
   * @param ii input info
   * @param f function definition
   * @param e arguments
   */
  public FNOut(final StaticContext sctx, final InputInfo ii, final Function f,
      final Expr... e) {
    super(sctx, ii, f, e);
  }

  @Override
  public Item item(final QueryContext ctx, final InputInfo ii) throws QueryException {
    switch(sig) {
      case _OUT_NL:     return NL;
      case _OUT_TAB:    return TAB;
      case _OUT_FORMAT: return format(ctx);
      default:          return super.item(ctx, ii);
    }
  }

  /**
   * Formats a string according to the specified format.
   * @param ctx query context
   * @return formatted string
   * @throws QueryException query exception
   */
  private Str format(final QueryContext ctx) throws QueryException {
    final String form = string(checkStr(expr[0], ctx));
    final int es = expr.length;
    final Object[] args = new Object[es - 1];
    for(int e = 1; e < es; e++) {
      final Item it = expr[e].item(ctx, info);
      args[e - 1] = it.type.isUntyped() ? string(it.string(info)) : it.toJava();
    }
    try {
      return Str.get(String.format(form, args));
    } catch(final RuntimeException ex) {
      throw ERRFORM.thrw(info, Util.className(ex), ex);
    }
  }
}
