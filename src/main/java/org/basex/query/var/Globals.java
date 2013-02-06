package org.basex.query.var;

import java.util.*;

import org.basex.data.*;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.util.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

import static org.basex.query.QueryText.*;

/**
 * Container of global variables of a module.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Leo Woerteler
 */
public final class Globals extends ExprInfo {
  /** The global variables. */
  private final HashMap<QNm, GlobalVar> globals = new HashMap<QNm, GlobalVar>();

  /**
   * Looks for a variable with the given name in the globally defined variables.
   * @param name variable name
   * @return declaration if found, {@null} otherwise
   */
  public GlobalVar get(final QNm name) {
    return globals.get(name);
  }

  /**
   * Sets the value of an external variable.
   * @param nm variable name
   * @param e expression
   * @param ctx query context
   * @return if the value could be bound
   * @throws QueryException query exception
   */
  public boolean bind(final QNm nm, final Expr e, final QueryContext ctx)
      throws QueryException {
    final GlobalVar var = globals.get(nm);
    if(var != null) return var.bind(e, ctx);

    // add new variable
    globals.put(nm, new GlobalVar(nm, e));
    return true;
  }

  /**
   * Declares a new static variable.
   * @param nm variable name
   * @param t type
   * @param a annotations
   * @param e bound expression, possibly {@code null}
   * @param ext {@code external} flag
   * @param ctx query context
   * @param ii input info
   * @throws QueryException query exception
   */
  public void declare(final QNm nm, final SeqType t, final Ann a, final Expr e,
      final boolean ext, final QueryContext ctx, final InputInfo ii)
          throws QueryException {
    final GlobalVar var = globals.get(nm);
    if(var != null) var.declare(t, a, e, ext, ctx, ii);
    else globals.put(nm, new GlobalVar(ii, a, nm, t, e, ext));
  }

  /**
   * Checks if none of the variables contains an updating expression.
   * @throws QueryException query exception
   */
  public void checkUp() throws QueryException {
    for(final GlobalVar var : globals.values()) var.checkUp();
  }

  /**
   * Checks if no global variables are declared.
   * @return {@code true} if no global variables are used, {@code false} otherwise
   */
  public boolean isEmpty() {
    return globals.isEmpty();
  }

  @Override
  public void plan(final FElem plan) {
    if(globals.isEmpty()) return;
    final FElem e = planElem();
    for(final GlobalVar v : globals.values()) v.plan(e);
    plan.add(e);
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder();
    for(final GlobalVar v : globals.values()) {
      sb.append(DECLARE).append(' ');
      if(!v.ann.isEmpty()) sb.append(v.ann).append(' ');
      sb.append(VARIABLE).append(' ').append(v).append(' ');
      if(v.expr() != null) sb.append(ASSIGN).append(' ').append(v.expr());
      else sb.append(EXTERNAL);
      return sb.append(';').toString();
    }
    return sb.toString();
  }
}
