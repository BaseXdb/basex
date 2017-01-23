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
 * @author BaseX Team 2005-17, BSD License
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
   * @param cont contents
   */
  CName(final String desc, final StaticContext sc, final InputInfo info, final Expr name,
      final Expr... cont) {
    super(sc, info, cont);
    this.name = name;
    this.desc = desc;
  }

  @Override
  public final void checkUp() throws QueryException {
    checkNoUp(name);
    super.checkUp();
  }

  @Override
  public Expr compile(final CompileContext cc) throws QueryException {
    name = name.compile(cc);
    return super.compile(cc);
  }

  /**
   * Returns the atomized value of the constructor.
   * @param qc query context
   * @return resulting value
   * @throws QueryException query exception
   */
  final byte[] atomValue(final QueryContext qc) throws QueryException {
    final TokenBuilder tb = new TokenBuilder();
    for(final Expr expr : exprs) {
      final Value v = qc.value(expr);
      boolean m = false;
      for(final Item it : v.atomValue(info)) {
        if(m) tb.add(' ');
        tb.add(it.string(info));
        m = true;
      }
    }
    return tb.finish();
  }

  /**
   * Returns an updated name expression.
   * @param qc query context
   * @param elem element
   * @return result
   * @throws QueryException query exception
   */
  final QNm qname(final QueryContext qc, final boolean elem) throws QueryException {
    final Item it = checkNoEmpty(name.atomItem(qc, info), AtomType.QNM);
    final Type ip = it.type;
    if(ip == AtomType.QNM) return (QNm) it;
    if(!ip.isStringOrUntyped() || ip == AtomType.URI) throw STRQNM_X_X.get(info, ip, it);

    // create and update namespace
    final byte[] str = it.string(info);
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
  public Expr inline(final Var var, final Expr ex, final CompileContext cc) throws QueryException {
    final boolean changed = inlineAll(exprs, var, ex, cc);
    final Expr sub = name.inline(var, ex, cc);
    if(sub != null) name = sub;
    return sub != null || changed ? optimize(cc) : null;
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
