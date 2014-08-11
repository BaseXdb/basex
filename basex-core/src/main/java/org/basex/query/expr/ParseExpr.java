package org.basex.query.expr;

import static org.basex.query.QueryText.*;
import static org.basex.query.util.Err.*;
import static org.basex.util.Token.*;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.value.*;
import org.basex.query.value.array.Array;
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
      throw SEQFOUND_X.get(ii, vb.value());
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

  @Override
  public Value atomValue(final QueryContext qc, final InputInfo ii) throws QueryException {
    return value(qc).atomValue(ii);
  }

  @Override
  public final Item atomItem(final QueryContext qc, final InputInfo ii) throws QueryException {
    Item it1 = item(qc, ii);
    return it1 == null ? null : it1.atomItem(ii);
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
          throw EBV_X.get(ii, vb.value());
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
    if(expr.has(Flag.UPD)) throw UPNOT_X.get(info, description());
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
      if(e != null && e.has(Flag.UPD)) throw UPNOT_X.get(info, description());
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
      if(u && s == -1 || !u && s == 1) throw UPALL_X.get(info, description());
      s = u ? 1 : -1;
    }
  }

  /**
   * Returns the current context value or throws an exception if the context value is not set.
   * @param qc query context
   * @return context
   * @throws QueryException query exception
   */
  protected final Value ctxValue(final QueryContext qc) throws QueryException {
    final Value v = qc.value;
    if(v != null) return v;
    throw NOCTX_X.get(info, this);
  }

  /**
   * Checks if the specified expression yields a string.
   * Returns its value as token or throws an exception.
   * @param ex expression to be evaluated
   * @param qc query context
   * @return token
   * @throws QueryException query exception
   */
  protected final byte[] toToken(final Expr ex, final QueryContext qc) throws QueryException {
    return toToken(ex, qc, false);
  }

  /**
   * Checks if the specified expression yields a string.
   * Returns its value as token or throws an exception.
   * Returns a token representation or an exception.
   * @param ex expression to be evaluated
   * @param qc query context
   * @param empty convert empty sequence ({@code item = null}) to string
   * @return token
   * @throws QueryException query exception
   */
  protected final byte[] toToken(final Expr ex, final QueryContext qc, final boolean empty)
      throws QueryException {
    final Item it = ex.atomItem(qc, info);
    if(it == null) {
      if(empty) return EMPTY;
      throw EMPTYFOUND_X.get(info, AtomType.STR);
    }
    return toToken(it);
  }

  /**
   * Checks if the specified non-empty item is a string.
   * Returns its value as token or throws an exception.
   * @param it item to be checked
   * @return token
   * @throws QueryException query exception
   */
  protected final byte[] toToken(final Item it) throws QueryException {
    final Type ip = it.type;
    if(ip.isStringOrUntyped()) return it.string(info);
    throw it instanceof FItem ? FIATOM_X.get(info, it) : castError(info, it, AtomType.STR);
  }

  /**
   * Checks if the specified expression yields a boolean.
   * Returns the boolean or throws an exception.
   * @param ex expression to be checked
   * @param qc query context
   * @return boolean
   * @throws QueryException query exception
   */
  protected final boolean toBoolean(final Expr ex, final QueryContext qc) throws QueryException {
    return toBoolean(ex.atomItem(qc, info));
  }

  /**
   * Checks if the specified item is a boolean.
   * Returns the boolean or throws an exception.
   * @param it item be checked
   * @return boolean
   * @throws QueryException query exception
   */
  protected final boolean toBoolean(final Item it) throws QueryException {
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
  protected final double toDouble(final Expr ex, final QueryContext qc) throws QueryException {
    return toDouble(ex.atomItem(qc, info));
  }

  /**
   * Checks if the specified item is a double.
   * Returns the double or throws an exception.
   * @param it item
   * @return double
   * @throws QueryException query exception
   */
  protected final double toDouble(final Item it) throws QueryException {
    if(checkNoEmpty(it, AtomType.DBL).type.isNumberOrUntyped()) return it.dbl(info);
    throw numberError(this, it);
  }

  /**
   * Checks if the specified expression yields a float.
   * Returns the float or throws an exception.
   * @param ex expression to be checked
   * @param qc query context
   * @return float
   * @throws QueryException query exception
   */
  protected final float toFloat(final Expr ex, final QueryContext qc) throws QueryException {
    final Item it = ex.atomItem(qc, info);
    if(checkNoEmpty(it, AtomType.FLT).type.isNumberOrUntyped()) return it.flt(info);
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
  protected final long toLong(final Expr ex, final QueryContext qc) throws QueryException {
    return toLong(ex.atomItem(qc, info));
  }

  /**
   * Checks if the specified item is a number.
   * Returns a token representation or an exception.
   * @param it item to be checked
   * @return item
   * @throws QueryException query exception
   */
  protected final long toLong(final Item it) throws QueryException {
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
  protected final ANode toNode(final Expr ex, final QueryContext qc) throws QueryException {
    return toNode(checkNoEmpty(ex.item(qc, info), NodeType.NOD));
  }

  /**
   * Checks if the specified item is a node or {@code null}.
   * Returns the node, {@code null}, or an exception.
   * @param it item to be checked
   * @return node or {@code null}
   * @throws QueryException query exception
   */
  protected final ANode toNode(final Item it) throws QueryException {
    if(it == null || it instanceof ANode) return (ANode) it;
    throw castError(info, it, NodeType.NOD);
  }

  /**
   * Checks if the evaluated expression yields a non-empty item.
   * Returns the item or an exception.
   * @param ex expression to be evaluated
   * @param qc query context
   * @return item
   * @throws QueryException query exception
   */
  protected final Item toItem(final Expr ex, final QueryContext qc) throws QueryException {
    return checkNoEmpty(ex.item(qc, info));
  }

  /**
   * Checks if the specified expression yields a non-empty item.
   * Returns the item or an exception.
   * @param ex expression to be evaluated
   * @param qc query context
   * @param t expected type
   * @return item
   * @throws QueryException query exception
   */
  protected final Item toItem(final Expr ex, final QueryContext qc, final Type t)
      throws QueryException {
    return checkNoEmpty(ex.item(qc, info), t);
  }

  /**
   * Checks if the evaluated expression yields a non-empty item.
   * Returns the atomized item or an exception.
   * @param ex expression to be evaluated
   * @param qc query context
   * @return atomized item
   * @throws QueryException query exception
   */
  protected final Item toAtomItem(final Expr ex, final QueryContext qc) throws QueryException {
    return checkNoEmpty(ex.atomItem(qc, info));
  }

  /**
   * Checks if the specified expression yields an element.
   * Returns the element or an exception.
   * @param ex expression to be evaluated
   * @param qc query context
   * @return binary item
   * @throws QueryException query exception
   */
  protected final ANode toElem(final Expr ex, final QueryContext qc) throws QueryException {
    return (ANode) checkType(ex.item(qc, info), NodeType.ELM);
  }

  /**
   * Checks if the specified expression yields a binary item.
   * Returns the item or an exception.
   * @param ex expression to be evaluated
   * @param qc query context
   * @return binary item
   * @throws QueryException query exception
   */
  protected final Bin toBin(final Expr ex, final QueryContext qc) throws QueryException {
    return toBin(ex.atomItem(qc, info));
  }

  /**
   * Checks if the specified item is a binary item.
   * Returns the item or an exception.
   * @param it item to be checked
   * @return binary item
   * @throws QueryException query exception
   */
  protected final Bin toBin(final Item it) throws QueryException {
    if(checkNoEmpty(it) instanceof Bin) return (Bin) it;
    throw BINARY_X.get(info, it.type);
  }

  /**
   * Checks if the specified expression yields a string or binary item.
   * @param ex expression to be evaluated
   * @param qc query context
   * @return byte array
   * @throws QueryException query exception
   */
  protected final byte[] toBinary(final Expr ex, final QueryContext qc) throws QueryException {
    return toBinary(ex.atomItem(qc, info));
  }

  /**
   * Checks if the specified expression yields a Base64 item.
   * Returns the item or an exception.
   * @param empty allow empty result
   * @param ex expression to be evaluated
   * @param qc query context
   * @return Bas64 item
   * @throws QueryException query exception
   */
  protected final B64 toB64(final Expr ex, final QueryContext qc, final boolean empty)
      throws QueryException {
    return toB64(ex.atomItem(qc, info), empty);
  }

  /**
   * Checks if the specified item is a Base64 item.
   * Returns the item or an exception.
   * @param empty allow empty result
   * @param it item
   * @return Bas64 item
   * @throws QueryException query exception
   */
  protected final B64 toB64(final Item it, final boolean empty) throws QueryException {
    if(empty && it == null) return null;
    return (B64) checkType(it, AtomType.B64);
  }

  /**
   * Checks if the specified item is a string or binary item.
   * @param it item to be checked
   * @return byte array
   * @throws QueryException query exception
   */
  protected final byte[] toBinary(final Item it) throws QueryException {
    if(it instanceof AStr) return it.string(info);
    if(it instanceof Bin) return ((Bin) it).binary(info);
    throw STRBIN_X_X.get(info, it.type, it);
  }

  /**
   * Checks if the specified expression yields a QName.
   * Returns the item or an exception.
   * @param ex expression to be checked
   * @param qc query context
   * @param sc static context
   * @param empty allow empty result
   * @return QNm item
   * @throws QueryException query exception
   */
  protected final QNm toQNm(final Expr ex, final QueryContext qc, final StaticContext sc,
      final boolean empty) throws QueryException {
    return toQNm(ex.atomItem(qc, info), sc, empty);
  }

  /**
   * Checks if the specified item is a QName.
   * Returns the item or an exception.
   * @param it item
   * @param sc static context
   * @param empty allow empty result
   * @return QNm item
   * @throws QueryException query exception
   */
  protected final QNm toQNm(final Item it, final StaticContext sc, final boolean empty)
      throws QueryException {

    if(empty && it == null) return null;
    final Type ip = checkNoEmpty(it, AtomType.QNM).type;
    if(ip == AtomType.QNM) return (QNm) it;
    if(ip.isUntyped() && sc.xquery3()) throw NSSENS_X_X.get(info, ip, AtomType.QNM);
    throw castError(info, it, AtomType.QNM);
  }

  /**
   * Checks if the specified expression yields a function.
   * Returns the function or an exception.
   * @param ex expression to be evaluated
   * @param qc query context
   * @return function item
   * @throws QueryException query exception
   */
  protected FItem toFunc(final Expr ex, final QueryContext qc) throws QueryException {
    return (FItem) checkType(toItem(ex, qc, FuncType.ANY_FUN), FuncType.ANY_FUN);
  }

  /**
   * Checks if the specified expression yields a map.
   * Returns the map or an exception.
   * @param ex expression
   * @param qc query context
   * @return map
   * @throws QueryException query exception
   */
  protected Map toMap(final Expr ex, final QueryContext qc) throws QueryException {
    return toMap(toItem(ex, qc, SeqType.ANY_MAP));
  }

  /**
   * Checks if the specified item is a map.
   * Returns the map or an exception.
   * @param it item to check
   * @return the map
   * @throws QueryException if the item is not a map
   */
  protected Map toMap(final Item it) throws QueryException {
    if(it instanceof Map) return (Map) it;
    throw castError(info, it, SeqType.ANY_MAP);
  }

  /**
   * Checks if the specified expression yields an array.
   * @param e expression
   * @param qc query context
   * @return array
   * @throws QueryException query exception
   */
  protected Array toArray(final Expr e, final QueryContext qc) throws QueryException {
    return toArray(toItem(e, qc, SeqType.ANY_ARRAY));
  }

  /**
   * Assures that the specified item item is an array.
   * @param it item to check
   * @return the array
   * @throws QueryException if the item is not a array
   */
  protected Array toArray(final Item it) throws QueryException {
    if(it instanceof Array) return (Array) it;
    throw castError(info, it, SeqType.ANY_ARRAY);
  }

  /**
   * Checks if the specified expression yields an item of the specified atomic type.
   * Returns the item or an exception.
   * @param ex expression to be evaluated
   * @param qc query context
   * @param t type to be checked
   * @return item
   * @throws QueryException query exception
   */
  protected Item checkAtomic(final Expr ex, final QueryContext qc, final Type t)
      throws QueryException {
    return checkType(ex.atomItem(qc, info), t);
  }

  /**
   * Checks if the specified expression is an empty sequence; if yes, throws
   * an exception.
   * @param it item to be checked
   * @param t type to be checked
   * @return specified item
   * @throws QueryException query exception
   */
  protected Item checkType(final Item it, final Type t) throws QueryException {
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
    throw EMPTYFOUND.get(info);
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
    throw EMPTYFOUND_X.get(info, t);
  }
}
