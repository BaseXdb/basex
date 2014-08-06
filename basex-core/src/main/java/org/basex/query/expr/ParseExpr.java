package org.basex.query.expr;

import static org.basex.query.QueryText.*;
import static org.basex.query.util.Err.*;
import static org.basex.util.Token.*;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.map.*;
import org.basex.query.value.node.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Abstract parse expression. All non-value expressions are derived from this class.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public abstract class ParseExpr extends Expr {
  /** Input information. */
  public final InputInfo info;
  /** Static type. */
  protected SeqType seqType;
  /** Cardinality of result; {@code -1} if unknown. */
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
    final Item n = ir.next();
    if(n != null) {
      final ValueBuilder vb = new ValueBuilder(3).add(it).add(n);
      if(ir.next() != null) vb.add(Str.get(DOTS));
      throw NOITEM.get(ii, vb.value());
    }
    return it;
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    if(seqType().zeroOrOne()) {
      final Value v = item(qc, info);
      return v == null ? Empty.SEQ : v;
    }
    return qc.iter(this).value();
  }

  /**
   * Copies this expression's return type and size to the given expression.
   * @param <T> expression type
   * @param ex expression
   * @return the expression for convenience
   */
  protected final <T extends ParseExpr> T copyType(final T ex) {
    ex.seqType = seqType;
    ex.size = size;
    return ex;
  }

  @Override
  public final Item ebv(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Item it;
    if(seqType().zeroOrOne()) {
      it = item(qc, info);
    } else {
      final Iter ir = iter(qc);
      it = ir.next();
      if(it != null && !(it instanceof ANode)) {
        final Item n = ir.next();
        if(n != null) {
          final ValueBuilder vb = new ValueBuilder(3).add(it).add(n);
          if(ir.next() != null) vb.add(Str.get(DOTS));
          throw EBV.get(ii, vb.value());
        }
      }
    }
    return it == null ? Bln.FALSE : it;
  }

  @Override
  public final Item test(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Item it = ebv(qc, info);
    return (it instanceof ANum ? it.dbl(info) == qc.pos : it.bool(info)) ? it : null;
  }

  @Override
  public SeqType seqType() {
    return seqType != null ? seqType : SeqType.ITEM_ZM;
  }

  @Override
  public final long size() {
    return size == -1 ? seqType().occ() : size;
  }

  // OPTIMIZATIONS ============================================================

  /**
   * Assigns a sequence type.
   * @param type sequence type
   */
  public void seqType(final SeqType type) {
    seqType = type;
  }

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
   * @param qc query context
   * @return optimized expression
   */
  protected final Expr optPre(final QueryContext qc) {
    return optPre(null, qc);
  }

  /**
   * Adds an optimization info for pre-evaluating the specified expression.
   * @param ex optimized expression
   * @param qc query context
   * @return optimized expression
   */
  protected final Expr optPre(final Expr ex, final QueryContext qc) {
    if(ex != this) qc.compInfo(OPTPRE, this);
    return ex == null ? Empty.SEQ : ex;
  }

  /**
   * Returns a boolean equivalent for the specified expression.
   * If the specified expression yields a boolean value anyway, it will be
   * returned as is. Otherwise, it will be wrapped into a boolean function.
   * @param ex expression to be rewritten
   * @param info input info
   * @return expression
   */
  protected static Expr compBln(final Expr ex, final InputInfo info) {
    return ex.seqType().eq(SeqType.BLN) ? ex : Function.BOOLEAN.get(null, info, ex);
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
   * @param ex expressions (may be {@code null}, and may contain {@code null} references)
   * @throws QueryException query exception
   */
  protected final void checkNoneUp(final Expr... ex) throws QueryException {
    if(ex == null) return;
    checkAllUp(ex);
    for(final Expr e : ex) {
      if(e != null && e.has(Flag.UPD)) throw UPNOT.get(info, description());
    }
  }

  /**
   * Ensures that all specified expressions are vacuous or either updating or non-updating.
   * Otherwise, throws an exception.
   * @param ex expressions to be checked
   * @throws QueryException query exception
   */
  void checkAllUp(final Expr... ex) throws QueryException {
    // updating state: 0 = initial state, 1 = updating, -1 = non-updating
    int s = 0;
    for(final Expr e : ex) {
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
   * @param ex expression to be checked
   * @param qc query context
   * @return boolean
   * @throws QueryException query exception
   */
  protected final boolean checkBln(final Expr ex, final QueryContext qc) throws QueryException {
    return checkBln(ex.item(qc, info));
  }

  /**
   * Checks if the specified item is a boolean.
   * Returns the boolean or throws an exception.
   * @param it item be checked
   * @return boolean
   * @throws QueryException query exception
   */
  protected final boolean checkBln(final Item it) throws QueryException {
    final Type ip = checkNoEmpty(it, AtomType.BLN).type;
    if(ip == AtomType.BLN || ip.isUntyped()) return it.bool(info);
    throw castError(info, it, AtomType.BLN);
  }

  /**
   * Checks if the specified expression yields a double.
   * Returns the double or throws an exception.
   * @param ex expression to be checked
   * @param qc query context
   * @return double
   * @throws QueryException query exception
   */
  protected final double checkDbl(final Expr ex, final QueryContext qc) throws QueryException {
    return checkDbl(ex.item(qc, info));
  }

  /**
   * Checks if the specified expression yields a double.
   * Returns the double or throws an exception.
   * @param it item
   * @return double
   * @throws QueryException query exception
   */
  protected final double checkDbl(final Item it) throws QueryException {
    checkNoEmpty(it, AtomType.DBL);
    if(it.type.isNumberOrUntyped()) return it.dbl(info);
    throw numberError(this, it);
  }

  /**
   * Checks if the specified expression yields a float.
   * Returns the float or throws an exception.
   * @param ex expression to be checked
   * @param qc query context
   * @return double
   * @throws QueryException query exception
   */
  protected final float checkFlt(final Expr ex, final QueryContext qc) throws QueryException {
    final Item it = checkNoEmpty(ex.item(qc, info), AtomType.FLT);
    if(it.type.isNumberOrUntyped()) return it.flt(info);
    throw numberError(this, it);
  }

  /**
   * Checks if the specified expression yields an integer.
   * Returns a token representation or an exception.
   * @param ex expression to be checked
   * @param qc query context
   * @return integer value
   * @throws QueryException query exception
   */
  protected final long checkItr(final Expr ex, final QueryContext qc) throws QueryException {
    return checkItr(ex.item(qc, info));
  }

  /**
   * Checks if the specified item is a number.
   * Returns a token representation or an exception.
   * @param it item to be checked
   * @return item
   * @throws QueryException query exception
   */
  protected final long checkItr(final Item it) throws QueryException {
    final Type ip = checkNoEmpty(it, AtomType.ITR).type;
    if(ip.instanceOf(AtomType.ITR) || ip.isUntyped()) return it.itr(info);
    throw castError(info, it, AtomType.ITR);
  }

  /**
   * Checks if the specified expression yields a node.
   * Returns the boolean or throws an exception.
   * @param ex expression to be checked
   * @param qc query context
   * @return boolean
   * @throws QueryException query exception
   */
  protected final ANode checkNode(final Expr ex, final QueryContext qc) throws QueryException {
    return checkNode(ex.item(qc, info));
  }

  /**
   * Checks if the specified item is a node.
   * Returns the node or an exception.
   * @param it item to be checked
   * @return item
   * @throws QueryException query exception
   */
  protected final ANode checkNode(final Item it) throws QueryException {
    if(checkNoEmpty(it, NodeType.NOD) instanceof ANode) return (ANode) it;
    throw castError(info, it, NodeType.NOD);
  }

  /**
   * Checks if the specified expression yields a string.
   * Returns a token representation or an exception.
   * @param ex expression to be evaluated
   * @param qc query context
   * @return string representation
   * @throws QueryException query exception
   */
  protected final byte[] checkStr(final Expr ex, final QueryContext qc) throws QueryException {
    return checkStr(checkNoEmpty(ex.item(qc, info), AtomType.STR));
  }

  /**
   * Checks if the specified item is a string or an empty sequence.
   * Returns a token representation or an exception.
   * @param it item to be checked
   * @return string representation
   * @throws QueryException query exception
   */
  protected final byte[] checkEStr(final Item it) throws QueryException {
    return it == null ? EMPTY : checkStr(it);
  }

  /**
   * Checks if the specified expression yields a string or empty sequence.
   * Returns a token representation or an exception.
   * @param ex expression to be evaluated
   * @param qc query context
   * @return item
   * @throws QueryException query exception
   */
  protected final byte[] checkEStr(final Expr ex, final QueryContext qc) throws QueryException {
    return checkEStr(ex.item(qc, info));
  }

  /**
   * Checks if the specified, non-empty item is a string.
   * Returns a token representation or an exception.
   * @param it item to be checked (must not be {@code null})
   * @return string representation
   * @throws QueryException query exception
   */
  protected final byte[] checkStr(final Item it) throws QueryException {
    final Type ip = it.type;
    if(ip.isStringOrUntyped()) return it.string(info);
    throw it instanceof FItem ? FIATOM.get(info, ip) : castError(info, it, AtomType.STR);
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
   * Checks if the specified expression yields a non-empty item.
   * @param ex expression to be evaluated
   * @param qc query context
   * @return item
   * @throws QueryException query exception
   */
  protected final Item checkItem(final Expr ex, final QueryContext qc) throws QueryException {
    return checkNoEmpty(ex.item(qc, info));
  }

  /**
   * Checks if the specified expression yields a binary item.
   * @param ex expression to be evaluated
   * @param qc query context
   * @return binary item
   * @throws QueryException query exception
   */
  protected final Bin checkBin(final Expr ex, final QueryContext qc) throws QueryException {
    return checkBin(ex.item(qc, info));
  }

  /**
   * Checks if the specified expression yields a binary item.
   * @param it item to be checked
   * @return binary item
   * @throws QueryException query exception
   */
  protected final Bin checkBin(final Item it) throws QueryException {
    if(checkNoEmpty(it) instanceof Bin) return (Bin) it;
    throw BINARYTYPE.get(info, it.type);
  }

  /**
   * Checks if the specified expression yields a string or binary item.
   * @param ex expression to be evaluated
   * @param qc query context
   * @return binary item
   * @throws QueryException query exception
   */
  protected final byte[] checkStrBin(final Expr ex, final QueryContext qc) throws QueryException {
    return checkStrBin(ex.item(qc, info));
  }

  /**
   * Checks if the specified item is a string or binary item.
   * @param it item to be checked
   * @return byte representation
   * @throws QueryException query exception
   */
  protected final byte[] checkStrBin(final Item it) throws QueryException {
    if(it instanceof AStr) return it.string(info);
    if(it instanceof Bin) return ((Bin) it).binary(info);
    throw STRBINTYPE.get(info, it.type, it);
  }

  /**
   * Checks if the specified expression is a QName.
   * Returns the QName or an exception.
   * @param ex expression to be checked
   * @param qc query context
   * @param sc static context
   * @return specified item
   * @throws QueryException query exception
   */
  protected final QNm checkQNm(final Expr ex, final QueryContext qc, final StaticContext sc)
      throws QueryException {

    final Item it = checkNoEmpty(ex.item(qc, info), AtomType.QNM);
    final Type ip = it.type;
    if(ip == AtomType.QNM) return (QNm) it;
    if(ip.isUntyped() && sc.xquery3()) throw NSSENS.get(info, ip, AtomType.QNM);
    throw castError(info, it, AtomType.QNM);
  }

  /**
   * Checks if the specified expression yields a string or binary item.
   * Checks the type of the given function item.
   * @param ex expression to be evaluated
   * @param qc query context
   * @return function item
   * @throws QueryException query exception
   */
  protected FItem checkFunc(final Expr ex, final QueryContext qc) throws QueryException {
    return (FItem) checkType(ex.item(qc, info), FuncType.ANY_FUN);
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
    throw castError(info, it, t);
  }

  /**
   * Checks if the specified item is no empty sequence.
   * @param it item to be checked
   * @return specified item
   * @throws QueryException query exception
   */
  protected final Item checkNoEmpty(final Item it) throws QueryException {
    if(it != null) return it;
    throw SEQEMPTY.get(info);
  }

  /**
   * Checks if the specified item is no empty sequence.
   * @param it item to be checked
   * @param t expected type
   * @return specified item
   * @throws QueryException query exception
   */
  protected Item checkNoEmpty(final Item it, final Type t) throws QueryException {
    if(it != null) return it;
    throw SEQEXP.get(info, t);
  }

  /**
   * Assures that the given (non-{@code null}) item is a map.
   * @param it item to check
   * @return the map
   * @throws QueryException if the item is not a map
   */
  protected Map checkMap(final Item it) throws QueryException {
    if(it instanceof Map) return (Map) it;
    throw castError(info, it, SeqType.ANY_MAP);
  }
}
