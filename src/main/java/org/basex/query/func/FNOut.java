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
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class FNOut extends StandardFunc {
  /** Newline character. */
  private static final Str NL = Str.get("\n");
  /** Tab character. */
  private static final Str TAB = Str.get("\t");

  /**
   * Constructor.
   * @param ii input info
   * @param f function definition
   * @param e arguments
   */
  public FNOut(final InputInfo ii, final Function f, final Expr... e) {
    super(ii, f, e);
  }

  @Override
  public Item item(final QueryContext ctx, final InputInfo ii) throws QueryException {
    switch(sig) {
      case _OUT_NL:         return NL;
      case _OUT_TAB:        return TAB;
      case _OUT_FORMAT:     return format(ctx);
      default:               return super.item(ctx, ii);
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
    final Object[] args = new Object[expr.length - 1];
    for(int e = 1; e < expr.length; e++) {
      args[e - 1] = expr[e].item(ctx, info).toJava();
    }
    try {
      return Str.get(String.format(form, args));
    } catch(final RuntimeException ex) {
      throw ERRFORM.thrw(info, Util.name(ex), ex);
    }
  }

  /**
   * Dumps the memory consumption.
   * @param min initial memory usage
   * @param msg message (can be {@code null})
   * @param ctx query context
   */
  static void dump(final long min, final byte[] msg, final QueryContext ctx) {
    Performance.mandatoryGC(2);
    final long max = Performance.memory();
    final long mb = Math.max(0, max - min);
    FNInfo.dump(token(Performance.format(mb)), msg, ctx);
  }
}
