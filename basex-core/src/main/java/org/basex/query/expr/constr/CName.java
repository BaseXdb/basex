package org.basex.query.expr.constr;

import static org.basex.query.QueryError.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.iter.*;
import org.basex.query.util.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;

/**
 * Abstract fragment constructor with a QName argument.
 *
 * @author BaseX Team 2005-19, BSD License
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
   * @param seqType sequence type
   * @param name name
   * @param cont contents
   */
  CName(final String desc, final StaticContext sc, final InputInfo info, final SeqType seqType,
      final Expr name, final Expr... cont) {
    super(sc, info, seqType, cont);
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

  @Override
  public Expr optimize(final CompileContext cc) throws QueryException {
    name = cc.simplifyAtom(name);
    cc.simplifyAtom(exprs);
    return this;
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
      boolean more = false;
      final Iter iter = expr.atomIter(qc, info);
      for(Item item; (item = qc.next(iter)) != null;) {
        if(more) tb.add(' ');
        tb.add(item.string(info));
        more = true;
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
    final Item item = checkNoEmpty(name.atomItem(qc, info), AtomType.QNM);
    final Type type = item.type;
    if(type == AtomType.QNM) return (QNm) item;
    if(!type.isStringOrUntyped() || type == AtomType.URI) throw STRQNM_X_X.get(info, type, item);

    // create and update namespace
    final byte[] str = item.string(info);
    if(XMLToken.isQName(str)) {
      return elem || Token.contains(str, ':') ? new QNm(str, sc) : new QNm(str);
    }
    throw INVNAME_X.get(info, str);
  }

  @Override
  public boolean inlineable(final Var var) {
    return name.inlineable(var) && super.inlineable(var);
  }

  @Override
  public final boolean has(final Flag... flags) {
    return name.has(flags) || super.has(flags);
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
    boolean changed = inlineAll(var, ex, exprs, cc);
    final Expr sub = name.inline(var, ex, cc);
    if(sub != null) {
      name = sub;
      changed = true;
    }
    return changed ? optimize(cc) : null;
  }

  @Override
  public final int exprSize() {
    int size = 1;
    for(final Expr expr : exprs) size += expr.exprSize();
    return size + name.exprSize();
  }

  @Override
  public boolean equals(final Object obj) {
    return obj instanceof CName && name.equals(((CName) obj).name) && super.equals(obj);
  }

  @Override
  public final String description() {
    return info(desc);
  }

  @Override
  public final void plan(final QueryPlan plan) {
    plan.add(plan.create(this), name, exprs);
  }

  @Override
  public final String toString() {
    return toString(desc + (name.seqType().eq(SeqType.QNM_O) ? " " + name : " { " + name + " }"));
  }
}
