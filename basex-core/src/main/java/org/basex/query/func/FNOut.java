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
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class FNOut extends BuiltinFunc {
  /** Newline character. */
  private static final Str NL = Str.get("\n");
  /** Tab character. */
  private static final Str TAB = Str.get("\t");

  /**
   * Constructor.
   * @param sc static context
   * @param info input info
   * @param func function definition
   * @param args arguments
   */
  public FNOut(final StaticContext sc, final InputInfo info, final Function func,
      final Expr... args) {
    super(sc, info, func, args);
  }

  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    switch(func) {
      case _OUT_NL:     return NL;
      case _OUT_TAB:    return TAB;
      case _OUT_FORMAT: return format(qc);
      default:          return super.item(qc, ii);
    }
  }

  /**
   * Formats a string according to the specified format.
   * @param qc query context
   * @return formatted string
   * @throws QueryException query exception
   */
  private Str format(final QueryContext qc) throws QueryException {
    final String form = string(toToken(exprs[0], qc));
    final int es = exprs.length;
    final Object[] args = new Object[es - 1];
    for(int e = 1; e < es; e++) {
      final Item it = exprs[e].item(qc, info);
      args[e - 1] = it.type.isUntyped() ? string(it.string(info)) : it.toJava();
    }
    try {
      return Str.get(String.format(form, args));
    } catch(final RuntimeException ex) {
      throw ERRFORMAT_X_X.get(info, Util.className(ex), ex);
    }
  }
}
