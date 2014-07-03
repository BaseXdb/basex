package org.basex.query.expr;

import static org.basex.query.QueryText.*;
import static org.basex.query.util.Err.*;
import static org.basex.util.Token.*;

import org.basex.core.*;
import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.util.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.map.*;
import org.basex.query.value.node.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Abstract parse expression. All non-value expressions are derived from
 * this class.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public abstract class ParseExpr extends Expr {
  /** Input information. */
  public final InputInfo info;
  /** Static type. */
  public SeqType type;
  /** Cardinality of result; unknown if set to -1. */
  protected long size = -1;

  /**
   * Constructor.
   * @param info input info
   */
  protected ParseExpr(final InputInfo info) {
    this.info = info;
  }

  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    final Item it = item(qc, info);
    return it != null ? it.iter() : Empty.ITER;
  }

  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Iter ir = iter(qc);
    final Item it = ir.next();
    if(it == null || ir.size() == 1) return it;
    Item n = ir.next();
    if(n != null) {
      final ValueBuilder vb = new ValueBuilder().add(it).add(n);
      n = ir.next();
      if(n != null) vb.add(Str.get("..."));
      throw SEQCAST.get(ii, vb.value());
    }
    return it;
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    if(type().zeroOrOne()) {
      final Value v = item(qc, info);
      return v == null ? Empty.SEQ : v;
    }
    return qc.iter(this).value();
  }

  /**
   * Copies this expression's return type and size to the given expression.
   * @param <T> expression type
   * @param e expression
   * @return the expression for convenience
   */
  protected final <T extends ParseExpr> T copyType(final T e) {
    e.type = type;
    e.size = size;
    return e;
  }

  @Override
  public final Item ebv(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Item it;
    if(type().zeroOrOne()) {
      it = item(qc, info);
    } else {
      final Iter ir = iter(qc);
      it = ir.next();
      if(!(it == null || it instanceof ANode || ir.next() == null))
        throw CONDTYPE.get(info, this);
    }
    return it == null ? Bln.FALSE : it;
  }

  @Override
  public final Item test(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Item it = ebv(qc, info);
    return (it instanceof ANum ? it.dbl(info) == qc.pos : it.bool(info)) ? it : null;
  }

  @Override
  public SeqType type() {
    return type != null ? type : SeqType.ITEM_ZM;
  }

  @Override
  public final long size() {
    return size == -1 ? type().occ() : size;
  }

  // OPTIMIZATIONS ============================================================

  /**
   * Pre-evaluates the specified expression.
   * @param qc query context
   * @return optimized expression
   * @throws QueryException query exception
   */
  protected final Expr preEval(final QueryContext qc) throws QueryException {
    return optPre(item(qc, info), qc);
  }

  /**
   * Adds an optimization info for pre-evaluating the specified expression.
   * @param opt optimized expression
   * @param qc query context
   * @return optimized expression
   */
  protected final Expr optPre(final Expr opt, final QueryContext qc) {
    if(opt != this) qc.compInfo(OPTPRE, this);
    return opt == null ? Empty.SEQ : opt;
  }

  /**
   * Returns a boolean equivalent for the specified expression.
   * If the specified expression yields a boolean value anyway, it will be
   * returned as is. Otherwise, it will be wrapped into a boolean function.
   * @param e expression to be rewritten
   * @param info input info
   * @return expression
   */
  protected static Expr compBln(final Expr e, final InputInfo info) {
    return e.type().eq(SeqType.BLN) ? e : Function.BOOLEAN.get(null, info, e);
  }

  // VALIDITY CHECKS ==========================================================

  /**
   * Ensures that the specified expression performs no updates.
   * Otherwise, throws an exception.
   * @param expr expression (may be {@code null})
   * @throws QueryException query exception
   */
  protected void checkNoUp(final Expr expr) throws QueryException {
    if(expr == null) return;
    expr.checkUp();
    if(expr.has(Flag.UPD)) throw UPNOT.get(info, description());
  }

  /**
   * Ensures that none of the specified expressions performs an update.
   * Otherwise, throws an exception.
   * @param expr expressions (may be {@code null}, and may contain {@code null} references)
   * @throws QueryException query exception
   */
  protected final void checkNoneUp(final Expr... expr) throws QueryException {
    if(expr == null) return;
    checkAllUp(expr);
    for(final Expr e : expr) {
      if(e != null && e.has(Flag.UPD)) throw UPNOT.get(info, description());
    }
  }

  /**
   * Ensures that all specified expressions are vacuous or either updating or non-updating.
   * Otherwise, throws an exception.
   * @param expr expressions to be checked
   * @throws QueryException query exception
   */
  void checkAllUp(final Expr... expr) throws QueryException {
    // updating state: 0 = initial state, 1 = updating, -1 = non-updating
    int s = 0;
    for(final Expr e : expr) {
      e.checkUp();
      if(e.isVacuous()) continue;
      final boolean u = e.has(Flag.UPD);
      if(u && s == -1 || !u && s == 1) throw UPALL.get(info, description());
      s = u ? 1 : -1;
    }
  }

  /**
   * Checks if the specified expression yields a boolean.
   * Returns the boolean or throws an exception.
   * @param e expression to be checked
   * @param qc query context
   * @return boolean
   * @throws QueryException query exception
   */
  protected final boolean checkBln(final Expr e, final QueryContext qc) throws QueryException {
    final Item it = checkNoEmpty(e.item(qc, info), AtomType.BLN);
    final Type ip = it.type;
    if(ip == AtomType.BLN || ip.isUntyped()) return it.bool(info);
    throw INVCAST.get(info, it.type, AtomType.BLN);
  }

  /**
   * Checks if the specified expression yields a double.
   * Returns the double or throws an exception.
   * @param e expression to be checked
   * @param qc query context
   * @return double
   * @throws QueryException query exception
   */
  protected final double checkDbl(final Expr e, final QueryContext qc) throws QueryException {
    final Item it = checkNoEmpty(e.item(qc, info), AtomType.DBL);
    if(it.type.isNumberOrUntyped()) return it.dbl(info);
    throw numberError(this, it);
  }

  /**
   * Checks if the specified expression yields a float.
   * Returns the float or throws an exception.
   * @param e expression to be checked
   * @param qc query context
   * @return double
   * @throws QueryException query exception
   */
  protected final float checkFlt(final Expr e, final QueryContext qc) throws QueryException {
    final Item it = checkNoEmpty(e.item(qc, info), AtomType.FLT);
    if(it.type.isNumberOrUntyped()) return it.flt(info);
    throw numberError(this, it);
  }

  /**
   * Checks if the specified expression is an integer.
   * Returns a token representation or an exception.
   * @param e expression to be checked
   * @param qc query context
   * @return integer value
   * @throws QueryException query exception
   */
  protected final long checkItr(final Expr e, final QueryContext qc) throws QueryException {
    return checkItr(checkNoEmpty(e.item(qc, info), AtomType.ITR));
  }

  /**
   * Checks if the specified item is a number.
   * Returns a token representation or an exception.
   * @param it item to be checked
   * @return item
   * @throws QueryException query exception
   */
  protected final long checkItr(final Item it) throws QueryException {
    final Type ip = it.type;
    if(ip.instanceOf(AtomType.ITR) || ip.isUntyped()) return it.itr(info);
    throw INVCAST.get(info, it.type, AtomType.ITR);
  }

  /**
   * Checks if the specified expression yields a node.
   * Returns the boolean or throws an exception.
   * @param e expression to be checked
   * @param qc query context
   * @return boolean
   * @throws QueryException query exception
   */
  protected final ANode checkNode(final Expr e, final QueryContext qc) throws QueryException {
    return checkNode(checkItem(e, qc));
  }

  /**
   * Checks if the specified item is a node.
   * Returns the node or an exception.
   * @param it item to be checked
   * @return item
   * @throws QueryException query exception
   */
  protected final ANode checkNode(final Item it) throws QueryException {
    if(it instanceof ANode) return (ANode) it;
    throw INVCAST.get(info, it.type, NodeType.NOD);
  }

  /**
   * Checks if the specified expression is a database node.
   * Returns the node or an exception.
   * @param it item to be checked
   * @return item
   * @throws QueryException query exception
   */
  protected final DBNode checkDBNode(final Item it) throws QueryException {
    if(it instanceof DBNode) return (DBNode) it;
    throw BXDB_NODB.get(info, this);
  }

  /**
   * Checks if the specified collation is supported.
   * @param e expression to be checked
   * @param qc query context
   * @param sc static context
   * @return collator, or {@code null} (default collation)
   * @throws QueryException query exception
   */
  protected final Collation checkColl(final Expr e, final QueryContext qc, final StaticContext sc)
      throws QueryException {
    return Collation.get(e == null ? null : checkStr(e, qc), qc, sc, info, WHICHCOLL);
  }

  /**
   * Checks if the specified expression yields a string.
   * Returns a token representation or an exception.
   * @param e expression to be evaluated
   * @param qc query context
   * @return string representation
   * @throws QueryException query exception
   */
  protected final byte[] checkStr(final Expr e, final QueryContext qc) throws QueryException {
    return checkStr(checkItem(e, qc));
  }

  /**
   * Checks if the specified item is a string.
   * Returns a token representation or an exception.
   * @param it item to be checked
   * @return string representation
   * @throws QueryException query exception
   */
  protected final byte[] checkStr(final Item it) throws QueryException {
    final Type ip = it.type;
    if(ip.isStringOrUntyped()) return it.string(info);
    throw INVCAST.get(info, it.type, AtomType.STR);
  }

  /**
   * Checks if the specified item is a string or an empty sequence.
   * Returns a token representation or an exception.
   * @param it item to be checked
   * @return string representation
   * @throws QueryException query exception
   */
  protected final byte[] checkEStr(final Item it) throws QueryException {
    if(it == null) return EMPTY;
    final Type ip = it.type;
    if(ip.isStringOrUntyped()) return it.string(info);
    throw it instanceof FItem ? FIATOM.get(info, ip) : INVCAST.get(info, it.type, AtomType.STR);
  }

  /**
   * Throws an exception if the context value is not set.
   * @param qc query context
   * @return context
   * @throws QueryException query exception
   */
  protected final Value checkCtx(final QueryContext qc) throws QueryException {
    final Value v = qc.value;
    if(v != null) return v;
    throw NOCTX.get(info, this);
  }

  /**
   * Throws an exception if the context value is not a node.
   * @param qc query context
   * @return context
   * @throws QueryException query exception
   */
  protected final ANode checkNode(final QueryContext qc) throws QueryException {
    final Value v = qc.value;
    if(v == null) throw NOCTX.get(info, this);
    if(v instanceof ANode) return (ANode) v;
    throw STEPNODE.get(info, this, v.type);
  }

  /**
   * Checks if the specified expression yields a non-empty item.
   * @param e expression to be evaluated
   * @param qc query context
   * @return item
   * @throws QueryException query exception
   */
  protected final Item checkItem(final Expr e, final QueryContext qc) throws QueryException {
    return checkNoEmpty(e.item(qc, info));
  }

  /**
   * Checks if the specified expression yields a binary item.
   * @param e expression to be evaluated
   * @param qc query context
   * @return binary item
   * @throws QueryException query exception
   */
  protected final Bin checkBinary(final Expr e, final QueryContext qc) throws QueryException {
    final Item it = checkItem(e, qc);
    if(it instanceof Bin) return (Bin) it;
    throw BINARYTYPE.get(info, it.type);
  }

  /**
   * Checks if the specified expression yields a string or binary item.
   * @param it item to be checked
   * @return byte representation
   * @throws QueryException query exception
   */
  protected final byte[] checkStrBin(final Item it) throws QueryException {
    if(it instanceof AStr) return it.string(info);
    if(it instanceof Bin) return ((Bin) it).binary(info);
    throw STRBINTYPE.get(info, it.type);
  }

  /**
   * Checks if the specified expression has the specified type; if no, throws an exception.
   * @param e expression to be checked
   * @param qc query context
   * @param sc static context
   * @return specified item
   * @throws QueryException query exception
   */
  protected final QNm checkQNm(final Expr e, final QueryContext qc, final StaticContext sc)
      throws QueryException {

    final Item it = checkItem(e, qc);
    if(it.type == AtomType.QNM) return (QNm) it;
    if(it.type.isUntyped() && sc.xquery3()) throw NSSENS.get(info, it.type, AtomType.QNM);
    throw INVCAST.get(info, it.type, AtomType.QNM);
  }

  /**
   * Checks if the specified expression yields a string or binary item.
   * Checks the type of the given function item.
   * @param e expression to be evaluated
   * @param qc query context
   * @return function item
   * @throws QueryException query exception
   */
  protected FItem checkFunc(final Expr e, final QueryContext qc) throws QueryException {
    return (FItem) checkType(checkItem(e, qc), FuncType.ANY_FUN);
  }

  /**
   * Checks if the specified expression is an empty sequence; if yes, throws
   * an exception.
   * @param it item to be checked
   * @param t type to be checked
   * @return specified item
   * @throws QueryException query exception
   */
  protected final Item checkType(final Item it, final Type t) throws QueryException {
    if(checkNoEmpty(it, t).type.instanceOf(t)) return it;
    throw INVCAST.get(info, it.type, t);
  }

  /**
   * Checks if the specified item is no empty sequence.
   * @param it item to be checked
   * @return specified item
   * @throws QueryException query exception
   */
  protected final Item checkNoEmpty(final Item it) throws QueryException {
    if(it != null) return it;
    throw INVEMPTY.get(info, description());
  }

  /**
   * Checks if the specified item is no empty sequence.
   * @param it item to be checked
   * @param t expected type
   * @return specified item
   * @throws QueryException query exception
   */
  private Item checkNoEmpty(final Item it, final Type t) throws QueryException {
    if(it != null) return it;
    throw INVEMPTYEX.get(info, description(), t);
  }

  /**
   * Checks if the specified expression yields a string or empty sequence.
   * Returns a token representation or an exception.
   * @param e expression to be evaluated
   * @param qc query context
   * @return item
   * @throws QueryException query exception
   */
  protected final byte[] checkEStr(final Expr e, final QueryContext qc) throws QueryException {
    return checkEStr(e.item(qc, info));
  }

  /**
   * Checks if the current user has create permissions. If negative, an
   * exception is thrown.
   * @param qc query context
   * @throws QueryException query exception
   */
  protected final void checkAdmin(final QueryContext qc) throws QueryException {
    checkPerm(qc, Perm.ADMIN);
  }

  /**
   * Checks if the current user has create permissions. If negative, an
   * exception is thrown.
   * @param qc query context
   * @throws QueryException query exception
   */
  protected final void checkCreate(final QueryContext qc) throws QueryException {
    checkPerm(qc, Perm.CREATE);
  }

  /**
   * Checks if the current user has given permissions. If negative, an
   * exception is thrown.
   * @param qc query context
   * @param p permission
   * @throws QueryException query exception
   */
  private void checkPerm(final QueryContext qc, final Perm p) throws QueryException {
    if(!qc.context.user.has(p)) throw BASX_PERM.get(info, p);
  }

  /**
   * Assures that the given (non-{@code null}) item is a map.
   * @param it item to check
   * @return the map
   * @throws QueryException if the item is not a map
   */
  protected Map checkMap(final Item it) throws QueryException {
    if(it instanceof Map) return (Map) it;
    throw INVCAST.get(info, it.type, SeqType.ANY_MAP);
  }
}
