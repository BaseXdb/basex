package org.basex.query.expr.constr;

import static org.basex.query.QueryError.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.util.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;

/**
 * Abstract fragment constructor with a QName argument.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
abstract class CName extends CNode {
  /** Description. */
  private final String desc;
  /** QName. */
  Expr name;

  /**
   * Constructor.
   * @param desc description
   * @param sc static context
   * @param info input info
   * @param name name
   * @param v attribute values
   */
  CName(final String desc, final StaticContext sc, final InputInfo info, final Expr name,
      final Expr... v) {
    super(sc, info, v);
    this.name = name;
    this.desc = desc;
  }

  @Override
  public final void checkUp() throws QueryException {
    checkNoUp(name);
    super.checkUp();
  }

  @Override
  public Expr compile(final QueryContext qc, final VarScope scp) throws QueryException {
    name = name.compile(qc, scp);
    return super.compile(qc, scp);
  }

  /**
   * Returns the atomized value of the constructor.
   * @param qc query context
   * @param ii input info
   * @return resulting value
   * @throws QueryException query exception
   */
  final byte[] value(final QueryContext qc, final InputInfo ii) throws QueryException {
    final TokenBuilder tb = new TokenBuilder();
    for(final Expr e : exprs) {
      final Value v = qc.value(e);
      boolean m = false;
      for(final Item it : v.atomValue(ii)) {
        if(m) tb.add(' ');
        tb.add(it.string(ii));
        m = true;
      }
    }
    return tb.finish();
  }

  /**
   * Returns an updated name expression.
   * @param qc query context
   * @param elem element
   * @param ii input info
   * @return result
   * @throws QueryException query exception
   */
  final QNm qname(final QueryContext qc, final boolean elem, final InputInfo ii)
      throws QueryException {

    final Item it = checkNoEmpty(name.atomItem(qc, info), AtomType.QNM);
    final Type ip = it.type;
    if(ip == AtomType.QNM) return (QNm) it;
    if(!ip.isStringOrUntyped() || ip == AtomType.URI) throw STRQNM_X_X.get(info, ip, it);

    // create and update namespace
    final byte[] str = it.string(ii);
    if(XMLToken.isQName(str)) {
      return elem || Token.contains(str, ':') ? new QNm(str, sc) : new QNm(str);
    }
    throw INVNAME_X.get(info, str);
  }

  @Override
  public boolean removable(final Var var) {
    return name.removable(var) && super.removable(var);
  }

  @Override
  public final boolean has(final Flag flag) {
    return name.has(flag) || super.has(flag);
  }

  @Override
  public final boolean accept(final ASTVisitor visitor) {
    return name.accept(visitor) && visitAll(visitor, exprs);
  }

  @Override
  public final VarUsage count(final Var var) {
    return name.count(var).plus(super.count(var));
  }

  @Override
  public Expr inline(final QueryContext qc, final VarScope scp, final Var var, final Expr ex)
      throws QueryException {

    final boolean changed = inlineAll(qc, scp, exprs, var, ex);
    final Expr sub = name.inline(qc, scp, var, ex);
    if(sub != null) name = sub;
    return sub != null || changed ? optimize(qc, scp) : null;
  }

  @Override
  public final int exprSize() {
    int sz = 1;
    for(final Expr expr : exprs) sz += expr.exprSize();
    return sz + name.exprSize();
  }

  @Override
  public final void plan(final FElem plan) {
    addPlan(plan, planElem(), name, exprs);
  }

  @Override
  public final String description() {
    return info(desc);
  }

  @Override
  public final String toString() {
    return toString(desc + (name.seqType().eq(SeqType.QNM) ? " " + name : " { " + name + " }"));
  }
}
