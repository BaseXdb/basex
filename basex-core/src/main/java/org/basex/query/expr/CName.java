package org.basex.query.expr;

import static org.basex.query.util.Err.*;

import org.basex.query.*;
import org.basex.query.iter.*;
import org.basex.query.util.*;
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
public abstract class CName extends CNode {
  /** Description. */
  private final String desc;
  /** QName. */
  Expr name;

  /**
   * Constructor.
   * @param desc description
   * @param sctx static context
   * @param info input info
   * @param name name
   * @param v attribute values
   */
  CName(final String desc, final StaticContext sctx, final InputInfo info, final Expr name,
      final Expr... v) {
    super(sctx, info, v);
    this.name = name;
    this.desc = desc;
  }

  @Override
  public final void checkUp() throws QueryException {
    checkNoUp(name);
    super.checkUp();
  }

  @Override
  public Expr compile(final QueryContext ctx, final VarScope scp) throws QueryException {
    name = name.compile(ctx, scp);
    return super.compile(ctx, scp);
  }

  /**
   * Returns the atomized value of the constructor.
   * @param ctx query context
   * @param ii input info
   * @return resulting value
   * @throws QueryException query exception
   */
  final byte[] value(final QueryContext ctx, final InputInfo ii) throws QueryException {
    final TokenBuilder tb = new TokenBuilder();
    for(final Expr e : exprs) {
      final Iter ir = ctx.iter(e);
      boolean m = false;
      for(Item it; (it = ir.next()) != null;) {
        if(m) tb.add(' ');
        tb.add(it.string(ii));
        m = true;
      }
    }
    return tb.finish();
  }

  /**
   * Returns an updated name expression.
   * @param ctx query context
   * @param ii input info
   * @return result
   * @throws QueryException query exception
   */
  final QNm qname(final QueryContext ctx, final InputInfo ii) throws QueryException {
    final Item it = checkItem(name, ctx);
    final Type ip = it.type;
    if(ip == AtomType.QNM) return (QNm) it;

    // create and update namespace
    final byte[] str = it.string(ii);
    if(XMLToken.isQName(str)) return new QNm(str, sc);
    throw (ip.isStringOrUntyped() ? INVNAME : INVQNAME).get(info, str);
  }

  @Override
  public boolean removable(final Var v) {
    return name.removable(v) && super.removable(v);
  }

  @Override
  public final boolean has(final Flag flag) {
    return name.has(flag) || super.has(flag);
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
    return toString(desc + (name.type().eq(SeqType.QNM) ? " " + name :
      " { " + name + " }"));
  }

  @Override
  public final boolean accept(final ASTVisitor visitor) {
    return name.accept(visitor) && visitAll(visitor, exprs);
  }

  @Override
  public final VarUsage count(final Var v) {
    return name.count(v).plus(super.count(v));
  }

  @Override
  public Expr inline(final QueryContext ctx, final VarScope scp, final Var v, final Expr e)
      throws QueryException {

    final boolean ex = inlineAll(ctx, scp, exprs, v, e);
    final Expr sub = name.inline(ctx, scp, v, e);
    if(sub != null) name = sub;
    return sub != null || ex ? optimize(ctx, scp) : null;
  }

  @Override
  public final int exprSize() {
    int sz = 1;
    for(final Expr e : exprs) sz += e.exprSize();
    return sz + name.exprSize();
  }
}
