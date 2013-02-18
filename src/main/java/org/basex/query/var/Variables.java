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

/**
 * Container of global variables of a module.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Leo Woerteler
 */
public final class Variables extends ExprInfo implements Iterable<StaticVar> {
  /** The variables. */
  private final HashMap<QNm, StaticVar> vars = new HashMap<QNm, StaticVar>();

  /**
   * Looks for a variable with the given name in the defined variables.
   * @param name variable name
   * @return declaration if found, {@null} otherwise
   */
  public StaticVar get(final QNm name) {
    return vars.get(name);
  }

  /**
   * Sets the value of an external variable.
   * @param nm variable name
   * @param e expression
   * @param ctx query context
   * @param ii input info
   * @return if the value could be bound
   * @throws QueryException query exception
   */
  public StaticVar bind(final QNm nm, final Expr e, final QueryContext ctx,
      final InputInfo ii) throws QueryException {
    final StaticVar var = vars.get(nm);
    if(var != null) return var.bind(e, ctx, ii);

    // add new variable
    final StaticVar sv = new StaticVar(nm, e, ii);
    vars.put(nm, sv);
    return sv;
  }

  /**
   * Declares a new static variable.
   * @param nm variable name
   * @param t type
   * @param a annotations
   * @param e bound expression, possibly {@code null}
   * @param ext {@code external} flag
   * @param ctx query context
   * @param scp variable scope
   * @param ii input info
   * @throws QueryException query exception
   */
  public void declare(final QNm nm, final SeqType t, final Ann a, final Expr e,
      final boolean ext, final QueryContext ctx, final VarScope scp, final InputInfo ii)
          throws QueryException {
    final StaticVar var = vars.get(nm);
    if(var != null) var.declare(t, a, e, ext, ctx, ii);
    else vars.put(nm, new StaticVar(scp, ii, a, nm, t, e, ext));
  }

  /**
   * Checks if none of the variables contains an updating expression.
   * @throws QueryException query exception
   */
  public void checkUp() throws QueryException {
    for(final StaticVar var : vars.values()) var.checkUp();
  }

  /**
   * Checks if no static variables are declared.
   * @return {@code true} if no static variables are used, {@code false} otherwise
   */
  public boolean isEmpty() {
    return vars.isEmpty();
  }

  @Override
  public void plan(final FElem plan) {
    if(vars.isEmpty()) return;
    final FElem e = planElem();
    for(final StaticVar v : vars.values()) v.plan(e);
    plan.add(e);
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder();
    for(final StaticVar v : vars.values()) v.fullDesc(sb);
    return sb.toString();
  }

  @Override
  public Iterator<StaticVar> iterator() {
    return Collections.unmodifiableCollection(vars.values()).iterator();
  }
}
