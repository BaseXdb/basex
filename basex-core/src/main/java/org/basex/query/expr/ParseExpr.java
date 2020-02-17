package org.basex.query.expr;

import static org.basex.query.QueryError.*;
import static org.basex.query.QueryText.*;
import static org.basex.util.Token.*;

import org.basex.data.*;
import org.basex.query.*;
import org.basex.query.ann.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.util.*;
import org.basex.query.value.*;
import org.basex.query.value.array.*;
import org.basex.query.value.item.*;
import org.basex.query.value.map.*;
import org.basex.query.value.node.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Abstract parse expression. All non-value expressions are derived from this class.
 *
 * @author BaseX Team 2005-20, BSD License
 * @author Christian Gruen
 */
public abstract class ParseExpr extends Expr {
  /** Expression type. */
  public final ExprType exprType;
  /** Input information. */
  public InputInfo info;

  /**
   * Constructor.
   * @param info input info (can be {@code null}
   * @param seqType sequence type
   */
  protected ParseExpr(final InputInfo info, final SeqType seqType) {
    this.info = info;
    exprType = new ExprType(seqType);
  }

  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    return value(qc).iter();
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    return item(qc, info);
  }

  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    return value(qc).item(qc, ii);
  }

  @Override
  public final Value atomValue(final QueryContext qc, final InputInfo ii) throws QueryException {
    return value(qc).atomValue(qc, info);
  }

  @Override
  public final Item ebv(final QueryContext qc, final InputInfo ii) throws QueryException {
    if(seqType().zeroOrOne()) {
      final Item item = item(qc, info);
      return item == Empty.VALUE ? Bln.FALSE : item;
    }

    final Iter iter = iter(qc);
    final Item item = iter.next();
    if(item == null) return Bln.FALSE;

    // effective boolean value is only defined for node sequences or single items
    if(!(item instanceof ANode)) {
      final Item next = iter.next();
      if(next != null) {
        final ValueBuilder vb = new ValueBuilder(qc, item, next);
        if(iter.next() != null) vb.add(Str.get(DOTS));
        throw EBV_X.get(info, vb.value());
      }
    }
    return item;
  }

  @Override
  public final Item test(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Item item = ebv(qc, info);
    return (item instanceof ANum ? item.dbl(info) == qc.focus.pos : item.bool(info)) ? item : null;
  }

  @Override
  public final SeqType seqType() {
    return exprType.seqType();
  }

  @Override
  public final long size() {
    return exprType.size();
  }

  // OPTIMIZATIONS ================================================================================

  /**
   * Copies this expression's type to the specified expression.
   * @param <T> expression type
   * @param expr expression
   * @return specified expression
   */
  protected final <T extends ParseExpr> T copyType(final T expr) {
    expr.exprType.assign(this);
    return expr;
  }

  /**
   * Assigns the type from the specified expression.
   * @param expr expression
   * @return self reference
   */
  public final ParseExpr adoptType(final Expr expr) {
    exprType.assign(expr);
    return this;
  }

  /**
   * Returns the common data reference for all expressions.
   * @param exprs expressions
   * @return data reference or {@code null}
   */
  public Data data(final Expr... exprs) {
    Data data1 = null;
    final int el = exprs.length;
    for(int e = 0; e < el; e++) {
      final Expr expr = exprs[e];
      if(expr.seqType().zero()) continue;
      final Data data2 = expr.data();
      if(data1 == null) data1 = data2;
      if(data1 == null || data1 != data2) return null;
    }
    return data1;
  }

  // VALIDITY CHECKS ==============================================================================

  /**
   * Ensures that the specified function expression is (not) updating.
   * Otherwise, throws an exception.
   * @param <T> expression type
   * @param expr expression (may be {@code null})
   * @param updating indicates if expression is expected to be updating
   * @param sc static context
   * @return specified expression
   * @throws QueryException query exception
   */
  protected <T extends XQFunctionExpr> T checkUp(final T expr, final boolean updating,
      final StaticContext sc) throws QueryException {

    if(!(sc.mixUpdates || updating == expr.annotations().contains(Annotation.UPDATING))) {
      if(!updating) throw FUNCUP_X.get(info, expr);
      if(!expr.isVacuousBody()) throw FUNCNOTUP_X.get(info, expr);
    }
    return expr;
  }

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
   * @param exprs expressions (may be {@code null}, and may contain {@code null} references)
   * @throws QueryException query exception
   */
  protected final void checkNoneUp(final Expr... exprs) throws QueryException {
    if(exprs == null) return;
    checkAllUp(exprs);
    for(final Expr expr : exprs) {
      if(expr != null && expr.has(Flag.UPD)) throw UPNOT_X.get(info, description());
    }
  }

  /**
   * Ensures that all specified expressions are vacuous or either updating or non-updating.
   * Otherwise, throws an exception.
   * @param exprs expressions to be checked
   * @throws QueryException query exception
   */
  protected void checkAllUp(final Expr... exprs) throws QueryException {
    // updating state: 0 = initial state, 1 = updating, -1 = non-updating
    int state = 0;
    for(final Expr expr : exprs) {
      expr.checkUp();
      if(expr.isVacuous()) continue;
      final boolean updating = expr.has(Flag.UPD);
      if(updating ? state == -1 : state == 1) throw UPALL.get(info);
      state = updating ? 1 : -1;
    }
  }

  /**
   * Returns the current context value or throws an exception if the context value is not set.
   * @param qc query context
   * @return context value
   * @throws QueryException query exception
   */
  protected final Value ctxValue(final QueryContext qc) throws QueryException {
    final Value value = qc.focus.value;
    if(value != null) return value;
    throw NOCTX_X.get(info, this);
  }

  // CONVERSIONS ==================================================================================

  /**
   * Checks if the specified expression yields a string.
   * Returns a value as token or throws an exception.
   * @param expr expression to be evaluated
   * @param qc query context
   * @return token
   * @throws QueryException query exception
   */
  protected final byte[] toToken(final Expr expr, final QueryContext qc) throws QueryException {
    final Item item = expr.atomItem(qc, info);
    if(item == Empty.VALUE) throw EMPTYFOUND_X.get(info, AtomType.STR);
    return toToken(item);
  }

  /**
   * Checks if the specified expression yields a string or an empty sequence.
   * Returns a value as token or throws an exception.
   * @param expr expression to be evaluated
   * @param qc query context
   * @return token (empty string if result is an empty sequence)
   * @throws QueryException query exception
   */
  protected final byte[] toEmptyToken(final Expr expr, final QueryContext qc)
      throws QueryException {
    final Item item = expr.atomItem(qc, info);
    return item == Empty.VALUE ? EMPTY : toToken(item);
  }

  /**
   * Checks if the specified expression yields a string or an empty sequence.
   * Returns a value as token or throws an exception.
   * @param expr expression to be evaluated
   * @param qc query context
   * @return token (empty string if result is an empty sequence)
   * @throws QueryException query exception
   */
  protected final byte[] toTokenOrNull(final Expr expr, final QueryContext qc)
      throws QueryException {
    final Item item = expr.atomItem(qc, info);
    return item == Empty.VALUE ? null : toToken(item);
  }

  /**
   * Checks if the specified non-empty item is a string.
   * Returns its value as token or throws an exception.
   * @param item item to be checked
   * @return token
   * @throws QueryException query exception
   */
  protected final byte[] toToken(final Item item) throws QueryException {
    final Type type = item.type;
    if(type.isStringOrUntyped()) return item.string(info);
    throw item instanceof FItem ? FIATOM_X.get(info, item.type) :
      typeError(item, AtomType.STR, info);
  }

  /**
   * Checks if the specified expression yields a boolean.
   * Returns the boolean or throws an exception.
   * @param expr expression to be evaluated
   * @param qc query context
   * @return boolean
   * @throws QueryException query exception
   */
  protected final boolean toBoolean(final Expr expr, final QueryContext qc) throws QueryException {
    return toBoolean(expr.atomItem(qc, info));
  }

  /**
   * Checks if the specified item is a boolean.
   * Returns the boolean or throws an exception.
   * @param item item be checked
   * @return boolean
   * @throws QueryException query exception
   */
  protected final boolean toBoolean(final Item item) throws QueryException {
    final Type type = checkNoEmpty(item, AtomType.BLN).type;
    if(type == AtomType.BLN) return item.bool(info);
    if(type.isUntyped()) return Bln.parse(item, info);
    throw typeError(item, AtomType.BLN, info);
  }

  /**
   * Checks if the specified expression yields a double.
   * Returns the double or throws an exception.
   * @param expr expression to be evaluated
   * @param qc query context
   * @return double
   * @throws QueryException query exception
   */
  protected final double toDouble(final Expr expr, final QueryContext qc) throws QueryException {
    return toDouble(expr.atomItem(qc, info));
  }

  /**
   * Checks if the specified item is a double.
   * Returns the double or throws an exception.
   * @param item item
   * @return double
   * @throws QueryException query exception
   */
  protected final double toDouble(final Item item) throws QueryException {
    if(checkNoEmpty(item, AtomType.DBL).type.isNumberOrUntyped()) return item.dbl(info);
    throw numberError(this, item);
  }

  /**
   * Checks if the specified expression yields a number or {@code null}.
   * Returns the number, {@code null}, or throws an exception.
   * @param expr expression to be evaluated
   * @param qc query context
   * @return double
   * @throws QueryException query exception
   */
  protected final ANum toNumber(final Expr expr, final QueryContext qc) throws QueryException {
    final Item item = expr.atomItem(qc, info);
    return item == Empty.VALUE ? null : toNumber(item);
  }

  /**
   * Checks if the specified, non-empty item is a double.
   * Returns the double or throws an exception.
   * @param item item to be checked
   * @return number
   * @throws QueryException query exception
   */
  protected ANum toNumber(final Item item) throws QueryException {
    if(item.type.isUntyped()) return Dbl.get(item.dbl(info));
    if(item instanceof ANum) return (ANum) item;
    throw numberError(this, item);
  }

  /**
   * Checks if the specified expression yields a float.
   * Returns the float or throws an exception.
   * @param expr expression to be evaluated
   * @param qc query context
   * @return float
   * @throws QueryException query exception
   */
  protected final float toFloat(final Expr expr, final QueryContext qc) throws QueryException {
    final Item item = expr.atomItem(qc, info);
    if(checkNoEmpty(item, AtomType.FLT).type.isNumberOrUntyped()) return item.flt(info);
    throw numberError(this, item);
  }

  /**
   * Checks if the specified expression yields an integer.
   * Returns a token representation or throws an exception.
   * @param expr expression to be evaluated
   * @param qc query context
   * @return integer value
   * @throws QueryException query exception
   */
  protected final long toLong(final Expr expr, final QueryContext qc) throws QueryException {
    return toLong(expr.atomItem(qc, info));
  }

  /**
   * Checks if the specified item is a number.
   * Returns a token representation or throws an exception.
   * @param item item to be checked
   * @return number
   * @throws QueryException query exception
   */
  protected final long toLong(final Item item) throws QueryException {
    final Type type = checkNoEmpty(item, AtomType.ITR).type;
    if(type.instanceOf(AtomType.ITR) || type.isUntyped()) return item.itr(info);
    throw typeError(item, AtomType.ITR, info);
  }

  /**
   * Checks if the specified expression yields a node.
   * Returns the boolean or throws an exception.
   * @param expr expression to be evaluated
   * @param qc query context
   * @return node
   * @throws QueryException query exception
   */
  protected final ANode toNode(final Expr expr, final QueryContext qc) throws QueryException {
    return toNode(checkNoEmpty(expr.item(qc, info), NodeType.NOD));
  }

  /**
   * Checks if the specified expression yields a node or {@code null}.
   * Returns the node, {@code null}, or throws an exception.
   * @param expr expression to be evaluated
   * @param qc query context
   * @return node or {@code null}
   * @throws QueryException query exception
   */
  protected final ANode toEmptyNode(final Expr expr, final QueryContext qc) throws QueryException {
    final Item item = expr.item(qc, info);
    return item == Empty.VALUE ? null : toNode(item);
  }

  /**
   * Checks if the specified non-empty item is a node.
   * Returns the node or throws an exception.
   * @param item item to be checked
   * @return node
   * @throws QueryException query exception
   */
  protected final ANode toNode(final Item item) throws QueryException {
    if(item instanceof ANode) return (ANode) item;
    throw typeError(item, NodeType.NOD, info);
  }

  /**
   * Checks if the evaluated expression yields a non-empty item.
   * Returns the item or throws an exception.
   * @param expr expression to be evaluated
   * @param qc query context
   * @return item
   * @throws QueryException query exception
   */
  protected final Item toItem(final Expr expr, final QueryContext qc) throws QueryException {
    return checkNoEmpty(expr.item(qc, info));
  }

  /**
   * Checks if the specified expression yields a non-empty item.
   * Returns the item or throws an exception.
   * @param expr expression to be evaluated
   * @param qc query context
   * @param type expected type
   * @return item
   * @throws QueryException query exception
   */
  protected final Item toItem(final Expr expr, final QueryContext qc, final Type type)
      throws QueryException {
    return checkNoEmpty(expr.item(qc, info), type);
  }

  /**
   * Checks if the evaluated expression yields a non-empty item.
   * Returns the atomized item or throws an exception.
   * @param expr expression to be evaluated
   * @param qc query context
   * @return atomized item
   * @throws QueryException query exception
   */
  protected final Item toAtomItem(final Expr expr, final QueryContext qc) throws QueryException {
    return checkNoEmpty(expr.atomItem(qc, info));
  }

  /**
   * Checks if the specified expression yields an element.
   * Returns the element or throws an exception.
   * @param expr expression to be evaluated
   * @param qc query context
   * @return binary item
   * @throws QueryException query exception
   */
  protected final ANode toElem(final Expr expr, final QueryContext qc) throws QueryException {
    return (ANode) checkType(expr.item(qc, info), NodeType.ELM);
  }

  /**
   * Checks if the specified expression yields a binary item.
   * Returns the binary item or throws an exception.
   * @param expr expression to be evaluated
   * @param qc query context
   * @return binary item
   * @throws QueryException query exception
   */
  protected final Bin toBin(final Expr expr, final QueryContext qc) throws QueryException {
    return toBin(expr.atomItem(qc, info));
  }

  /**
   * Checks if the specified item is a binary item.
   * Returns the binary item or throws an exception.
   * @param item item to be checked
   * @return binary item
   * @throws QueryException query exception
   */
  protected Bin toBin(final Item item) throws QueryException {
    if(checkNoEmpty(item) instanceof Bin) return (Bin) item;
    throw BINARY_X.get(info, item.type);
  }

  /**
   * Checks if the specified expression yields a string or binary item.
   * @param expr expression to be evaluated
   * @param qc query context
   * @return byte array
   * @throws QueryException query exception
   */
  protected final byte[] toBytes(final Expr expr, final QueryContext qc) throws QueryException {
    return toBytes(expr.atomItem(qc, info));
  }

  /**
   * Checks if the specified expression yields a Base64 item.
   * Returns the item or throws an exception.
   * @param empty allow empty result
   * @param expr expression to be evaluated
   * @param qc query context
   * @return Bas64 item
   * @throws QueryException query exception
   */
  protected final B64 toB64(final Expr expr, final QueryContext qc, final boolean empty)
      throws QueryException {
    return toB64(expr.atomItem(qc, info), empty);
  }

  /**
   * Checks if the specified item is a Base64 item.
   * @param empty allow empty result
   * @param item item
   * @return Bas64 item, or {@code null} if the argument is an empty sequence and if
   *   {@code empty} is true
   * @throws QueryException query exception
   */
  protected final B64 toB64(final Item item, final boolean empty) throws QueryException {
    if(empty && item == Empty.VALUE) return null;
    return (B64) checkType(item, AtomType.B64);
  }

  /**
   * Checks if the specified item is a string or binary item.
   * @param item item to be checked
   * @return byte array
   * @throws QueryException query exception
   */
  protected final byte[] toBytes(final Item item) throws QueryException {
    if(checkNoEmpty(item).type.isStringOrUntyped()) return item.string(info);
    if(item instanceof Bin) return ((Bin) item).binary(info);
    throw STRBIN_X_X.get(info, item.type, item);
  }

  /**
   * Checks if the specified expression yields a QName.
   * Returns the item or throws an exception.
   * @param expr expression to be evaluated
   * @param qc query context
   * @param empty allow empty result
   * @return QNm item
   * @throws QueryException query exception
   */
  protected final QNm toQNm(final Expr expr, final QueryContext qc, final boolean empty)
      throws QueryException {
    return toQNm(expr.atomItem(qc, info), empty);
  }

  /**
   * Checks if the specified item is a QName.
   * Returns the item or throws an exception.
   * @param item item
   * @param empty allow empty result
   * @return QName, or {@code null} if the item is {@code null} and {@code empty} is true
   * @throws QueryException query exception
   */
  protected final QNm toQNm(final Item item, final boolean empty) throws QueryException {
    if(empty && item == Empty.VALUE) return null;
    final Type type = checkNoEmpty(item, AtomType.QNM).type;
    if(type == AtomType.QNM) return (QNm) item;
    if(type.isUntyped()) throw NSSENS_X_X.get(info, type, AtomType.QNM);
    throw typeError(item, AtomType.QNM, info);
  }

  /**
   * Checks if the specified expression yields a function.
   * Returns the function or throws an exception.
   * @param expr expression to be evaluated
   * @param qc query context
   * @return function item
   * @throws QueryException query exception
   */
  protected FItem toFunc(final Expr expr, final QueryContext qc) throws QueryException {
    return (FItem) checkType(toItem(expr, qc, SeqType.ANY_FUNC), SeqType.ANY_FUNC);
  }

  /**
   * Checks if the specified expression yields a map.
   * Returns the map or throws an exception.
   * @param expr expression
   * @param qc query context
   * @return map
   * @throws QueryException query exception
   */
  protected XQMap toMap(final Expr expr, final QueryContext qc) throws QueryException {
    return toMap(toItem(expr, qc, SeqType.ANY_MAP));
  }

  /**
   * Checks if the specified item is a map.
   * Returns the map or throws an exception.
   * @param item item to check
   * @return the map
   * @throws QueryException if the item is not a map
   */
  protected XQMap toMap(final Item item) throws QueryException {
    if(item instanceof XQMap) return (XQMap) item;
    throw typeError(item, SeqType.ANY_MAP, info);
  }

  /**
   * Checks if the specified expression yields an array.
   * @param expr expression
   * @param qc query context
   * @return array
   * @throws QueryException query exception
   */
  protected XQArray toArray(final Expr expr, final QueryContext qc) throws QueryException {
    return toArray(toItem(expr, qc, SeqType.ANY_ARRAY));
  }

  /**
   * Assures that the specified item item is an array.
   * @param item item to check
   * @return the array
   * @throws QueryException if the item is not an array
   */
  protected XQArray toArray(final Item item) throws QueryException {
    if(item instanceof XQArray) return (XQArray) item;
    throw typeError(item, SeqType.ANY_ARRAY, info);
  }

  /**
   * Checks if the specified expression yields an item of the specified atomic type.
   * Returns the item or throws an exception.
   * @param expr expression to be evaluated
   * @param qc query context
   * @param type type to be checked
   * @return item
   * @throws QueryException query exception
   */
  protected Item checkType(final Expr expr, final QueryContext qc, final AtomType type)
      throws QueryException {
    return checkType(expr.atomItem(qc, info), type);
  }

  /**
   * Checks if the specified expression is an empty sequence; if yes, throws
   * an exception.
   * @param item item to be checked
   * @param type type to be checked
   * @return specified item
   * @throws QueryException query exception
   */
  protected Item checkType(final Item item, final Type type) throws QueryException {
    if(checkNoEmpty(item, type).type.instanceOf(type)) return item;
    throw typeError(item, type, info);
  }

  /**
   * Checks if the specified item is no empty sequence.
   * @param item item to be checked
   * @return specified item
   * @throws QueryException query exception
   */
  protected final Item checkNoEmpty(final Item item) throws QueryException {
    if(item != Empty.VALUE) return item;
    throw EMPTYFOUND.get(info);
  }

  /**
   * Checks if the specified item is no empty sequence.
   * @param item item to be checked
   * @param type expected type
   * @return specified item
   * @throws QueryException query exception
   */
  protected Item checkNoEmpty(final Item item, final Type type) throws QueryException {
    if(item != Empty.VALUE) return item;
    throw EMPTYFOUND_X.get(info, type);
  }
}
