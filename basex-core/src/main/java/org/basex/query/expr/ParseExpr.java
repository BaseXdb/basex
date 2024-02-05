package org.basex.query.expr;

import static org.basex.query.QueryError.*;
import static org.basex.query.value.type.AtomType.*;
import static org.basex.query.value.type.NodeType.*;

import java.util.*;

import org.basex.data.*;
import org.basex.query.*;
import org.basex.query.ann.*;
import org.basex.query.expr.path.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.util.*;
import org.basex.query.value.*;
import org.basex.query.value.array.*;
import org.basex.query.value.item.*;
import org.basex.query.value.map.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Abstract parse expression. All non-value expressions are derived from this class.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
public abstract class ParseExpr extends Expr {
  /** Expression type. */
  public final ExprType exprType;
  /** Input information. */
  protected InputInfo info;

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
    return value(qc).item(qc, info);
  }

  @Override
  public final Value atomValue(final QueryContext qc, final InputInfo ii) throws QueryException {
    return value(qc).atomValue(qc, info);
  }

  @Override
  public final boolean test(final QueryContext qc, final InputInfo ii, final boolean pred)
      throws QueryException {

    Item item;
    if(seqType().zeroOrOne()) {
      item = item(qc, info);
    } else {
      final Iter iter = iter(qc);
      item = iter.next();
      if(item == null) return false;

      // effective boolean value is only defined for node sequences or single items
      if(!(item instanceof ANode)) {
        final Item next = iter.next();
        if(next != null) throw ebvError(ValueBuilder.concat(item, next), info);
      }
    }
    return pred && item instanceof ANum ? item.dbl(info) == qc.focus.pos : item.bool(info);
  }

  @Override
  public final SeqType seqType() {
    return exprType.seqType();
  }

  @Override
  public final long size() {
    return exprType.size();
  }

  @Override
  public Data data() {
    return exprType.data();
  }

  @Override
  public final void refineType(final Expr expr) {
    exprType.refine(expr);
  }

  @Override
  public InputInfo info() {
    return info;
  }

  // OPTIMIZATIONS ================================================================================

  /**
   * Assigns this expression's type to the specified expression.
   * @param <T> expression type
   * @param expr expression to be modified
   * @return specified expression
   */
  public final <T extends Expr> T copyType(final T expr) {
    if(expr instanceof ParseExpr) ((ParseExpr) expr).exprType.assign(this);
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

  // VALIDITY CHECKS ==============================================================================

  /**
   * Ensures that the specified function expression is (not) updating.
   * Otherwise, throws an exception.
   * @param <T> expression type
   * @param expr expression (can be {@code null})
   * @param updating indicates if expression is expected to be updating
   * @param sc static context
   * @return specified expression
   * @throws QueryException query exception
   */
  protected final <T extends XQFunctionExpr> T checkUp(final T expr, final boolean updating,
      final StaticContext sc) throws QueryException {

    if(!(sc.mixUpdates || updating == expr.annotations().contains(Annotation.UPDATING))) {
      if(!updating) throw FUNCUP_X.get(info, expr);
      if(!expr.vacuousBody()) throw FUNCNOTUP_X.get(info, expr);
    }
    return expr;
  }

  /**
   * Ensures that the specified expression performs no updates.
   * Otherwise, throws an exception.
   * @param expr expression (can be {@code null})
   * @throws QueryException query exception
   */
  protected final void checkNoUp(final Expr expr) throws QueryException {
    if(expr == null) return;
    expr.checkUp();
    if(expr.has(Flag.UPD)) throw UPNOT_X.get(info, description());
  }

  /**
   * Ensures that none of the specified expressions performs an update.
   * Otherwise, throws an exception.
   * @param exprs expressions (can be {@code null})
   * @throws QueryException query exception
   */
  protected final void checkNoneUp(final Expr... exprs) throws QueryException {
    if(exprs == null) return;
    checkAllUp(exprs);
    for(final Expr expr : exprs) {
      if(expr.has(Flag.UPD)) throw UPNOT_X.get(info, description());
    }
  }

  /**
   * Ensures that all specified expressions are vacuous or either updating or non-updating.
   * Otherwise, throws an exception.
   * @param exprs expressions to be checked
   * @throws QueryException query exception
   */
  protected final void checkAllUp(final Expr... exprs) throws QueryException {
    // updating state: 0 = initial state, 1 = updating, -1 = non-updating
    int state = 0;
    for(final Expr expr : exprs) {
      expr.checkUp();
      if(expr.vacuous()) continue;
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
   * Evaluates an expression to a token.
   * @param expr expression
   * @param qc query context
   * @return token
   * @throws QueryException query exception
   */
  protected final byte[] toToken(final Expr expr, final QueryContext qc) throws QueryException {
    return toToken(toAtomItem(expr, qc));
  }

  /**
   * Evaluates an expression to a token.
   * @param expr expression
   * @param qc query context
   * @return token (zero-length if the expression yields an empty sequence)
   * @throws QueryException query exception
   */
  protected final byte[] toZeroToken(final Expr expr, final QueryContext qc) throws QueryException {
    final Item item = expr.atomItem(qc, info);
    return item.isEmpty() ? Token.EMPTY : toToken(item);
  }

  /**
   * Evaluates an expression to a token.
   * @param expr expression
   * @param qc query context
   * @return token, or {@code null} if the expression yields an empty sequence
   * @throws QueryException query exception
   */
  protected final byte[] toTokenOrNull(final Expr expr, final QueryContext qc)
      throws QueryException {
    final Item item = expr.atomItem(qc, info);
    return item.isEmpty() ? null : toToken(item);
  }

  /**
   * Converts an item to a token.
   * @param item item to be converted
   * @return token
   * @throws QueryException query exception
   */
  protected final byte[] toToken(final Item item) throws QueryException {
    final Type type = item.type;
    if(type.isStringOrUntyped()) return item.string(info);
    throw item instanceof FItem ? FIATOMIZE_X.get(info, item) : typeError(item, STRING, info);
  }

  /**
   * Evaluates an expression to a string.
   * @param expr expression
   * @param qc query context
   * @return string
   * @throws QueryException query exception
   */
  protected final String toString(final Expr expr, final QueryContext qc) throws QueryException {
    return Token.string(toToken(expr, qc));
  }

  /**
   * Evaluates an expression to a string.
   * @param item item to be converted
   * @return string
   * @throws QueryException query exception
   */
  protected final String toString(final Item item) throws QueryException {
    return Token.string(toToken(item));
  }

  /**
   * Evaluates an expression to a string.
   * @param expr expression
   * @param qc query context
   * @return string, or {@code null} if the expression yields an empty sequence
   * @throws QueryException query exception
   */
  protected final String toStringOrNull(final Expr expr, final QueryContext qc)
      throws QueryException {
    final Item item = expr.atomItem(qc, info);
    return item.isEmpty() ? null : toString(item);
  }

  /**
   * Evaluates an expression to a boolean.
   * @param expr expression
   * @param qc query context
   * @return boolean
   * @throws QueryException query exception
   */
  protected final boolean toBoolean(final Expr expr, final QueryContext qc) throws QueryException {
    return toBoolean(expr.atomItem(qc, info));
  }

  /**
   * Evaluates an expression to a boolean.
   * @param expr expression
   * @param qc query context
   * @return boolean, or {@code false} if the expression yields an empty sequence
   * @throws QueryException query exception
   */
  protected final boolean toBooleanOrFalse(final Expr expr, final QueryContext qc)
      throws QueryException {
    final Item item = expr.atomItem(qc, info);
    return !item.isEmpty() && toBoolean(item);
  }

  /**
   * Converts an item to a boolean.
   * @param item item to be converted
   * @return boolean
   * @throws QueryException query exception
   */
  protected final boolean toBoolean(final Item item) throws QueryException {
    final Type type = item.type;
    if(type == BOOLEAN) return item.bool(info);
    if(type.isUntyped()) return Bln.parse(item, info);
    throw typeError(item, BOOLEAN, info);
  }

  /**
   * Evaluates an expression to a double number.
   * @param expr expression
   * @param qc query context
   * @return double
   * @throws QueryException query exception
   */
  protected final double toDouble(final Expr expr, final QueryContext qc) throws QueryException {
    return toDouble(expr.atomItem(qc, info));
  }

  /**
   * Converts an item to a double number.
   * @param item item
   * @return double
   * @throws QueryException query exception
   */
  protected final double toDouble(final Item item) throws QueryException {
    if(item.type.isNumberOrUntyped()) return item.dbl(info);
    throw numberError(this, item);
  }

  /**
   * Evaluates an expression to a number.
   * @param expr expression
   * @param qc query context
   * @return number, or {@code null} if the expression yields an empty sequence
   * @throws QueryException query exception
   */
  protected final ANum toNumberOrNull(final Expr expr, final QueryContext qc)
      throws QueryException {
    final Item item = expr.atomItem(qc, info);
    return item.isEmpty() ? null : toNumber(item);
  }

  /**
   * Converts an item to a number.
   * @param expr expression
   * @param qc query context
   * @return number
   * @throws QueryException query exception
   */
  protected final ANum toNumber(final Expr expr, final QueryContext qc) throws QueryException {
    return toNumber(expr.atomItem(qc, info));
  }

  /**
   * Converts an item to a number.
   * @param item item to be converted
   * @return number
   * @throws QueryException query exception
   */
  protected final ANum toNumber(final Item item) throws QueryException {
    if(item.type.isUntyped()) return Dbl.get(item.dbl(info));
    if(item instanceof ANum) return (ANum) item;
    throw numberError(this, item);
  }

  /**
   * Evaluates an expression to a float number.
   * @param expr expression
   * @param qc query context
   * @return float
   * @throws QueryException query exception
   */
  protected final float toFloat(final Expr expr, final QueryContext qc) throws QueryException {
    final Item item = expr.atomItem(qc, info);
    if(item.type.isNumberOrUntyped()) return item.flt(info);
    throw numberError(this, item);
  }

  /**
   * Evaluates an expression to a long number.
   * @param expr expression
   * @param qc query context
   * @return integer value
   * @throws QueryException query exception
   */
  protected final long toLong(final Expr expr, final QueryContext qc) throws QueryException {
    return toLong(expr.atomItem(qc, info));
  }

  /**
   * Converts an item to an integer.
   * @param item item to be converted
   * @return integer
   * @throws QueryException query exception
   */
  protected final long toLong(final Item item) throws QueryException {
    final Type type = item.type;
    if(type.instanceOf(INTEGER) || type.isUntyped()) return item.itr(info);
    throw typeError(item, INTEGER, info);
  }

  /**
   * Converts an item to a specific integer.
   * @param item item to be converted
   * @param min minimum allowed value
   * @return integer
   * @throws QueryException query exception
   */
  protected final long toLong(final Item item, final long min) throws QueryException {
    final long v = toLong(item);
    if(v >= min) return v;
    throw typeError(item, INTEGER, info);
  }

  /**
   * Converts an item to a node.
   * @param expr expression
   * @param qc query context
   * @return node
   * @throws QueryException query exception
   */
  protected final ANode toNode(final Expr expr, final QueryContext qc) throws QueryException {
    return toNode(expr.item(qc, info));
  }

  /**
   * Evaluates an expression to a node.
   * @param expr expression
   * @param qc query context
   * @return node, or {@code null} if the expression yields an empty sequence
   * @throws QueryException query exception
   */
  protected final ANode toNodeOrNull(final Expr expr, final QueryContext qc) throws QueryException {
    final Item item = expr.item(qc, info);
    return item.isEmpty() ? null : toNode(item);
  }

  /**
   * Converts an item to a node.
   * @param item item to be converted
   * @return node
   * @throws QueryException query exception
   */
  protected final ANode toNode(final Item item) throws QueryException {
    if(item instanceof ANode) return (ANode) item;
    throw typeError(item, NODE, info);
  }

  /**
   * Evaluates an expression to an atomized item of the given type.
   * @param expr expression
   * @param qc query context
   * @return atomized item
   * @throws QueryException query exception
   */
  protected final Item toAtomItem(final Expr expr, final QueryContext qc) throws QueryException {
    final Item item = expr.atomItem(qc, info);
    if(item.isEmpty()) throw EMPTYFOUND.get(info);
    return item;
  }

  /**
   * Evaluates an expression to an element.
   * @param expr expression
   * @param qc query context
   * @return element
   * @throws QueryException query exception
   */
  protected final ANode toElem(final Expr expr, final QueryContext qc) throws QueryException {
    return (ANode) checkType(expr.item(qc, info), ELEMENT);
  }

  /**
   * Evaluates an expression to an element with the specified name.
   * @param expr expression
   * @param name name
   * @param qc query context
   * @param error error code
   * @return element
   * @throws QueryException query exception
   */
  protected final ANode toElem(final Expr expr, final QNm name, final QueryContext qc,
      final QueryError error) throws QueryException {
    final ANode node = toElem(expr, qc);
    if(new NameTest(name).matches(node)) return node;
    throw error.get(info, name.prefixId(), node.type, node);
  }

  /**
   * Evaluates an expression to a binary item.
   * @param expr expression
   * @param qc query context
   * @return binary item
   * @throws QueryException query exception
   */
  protected final Bin toBin(final Expr expr, final QueryContext qc) throws QueryException {
    return toBin(expr.atomItem(qc, info));
  }

  /**
   * Converts an item to a binary item.
   * @param item item to be converted
   * @return binary item
   * @throws QueryException query exception
   */
  protected final Bin toBin(final Item item) throws QueryException {
    if(item instanceof Bin) return (Bin) item;
    throw BINARY_X.get(info, item.seqType());
  }

  /**
   * Evaluates an expression (token, binary item) to a byte array.
   * @param expr expression
   * @param qc query context
   * @return byte array
   * @throws QueryException query exception
   */
  protected final byte[] toBytes(final Expr expr, final QueryContext qc) throws QueryException {
    return toBytes(expr.atomItem(qc, info));
  }

  /**
   * Evaluates an expression to a Base64 item.
   * @param expr expression
   * @param qc query context
   * @return Base64 item
   * @throws QueryException query exception
   */
  protected final B64 toB64(final Expr expr, final QueryContext qc) throws QueryException {
    return toB64(expr.atomItem(qc, info));
  }

  /**
   * Evaluates an expression to a Base64 item.
   * @param expr expression
   * @param qc query context
   * @return Base64 item, or {@code null} if the expression yields an empty sequence
   * @throws QueryException query exception
   */
  protected final B64 toB64OrNull(final Expr expr, final QueryContext qc) throws QueryException {
    final Item item = expr.atomItem(qc, info);
    return item.isEmpty() ? null : toB64(item);
  }

  /**
   * Converts an item to a base64 item.
   * @param item item
   * @return Base64 item
   * @throws QueryException query exception
   */
  protected final B64 toB64(final Item item) throws QueryException {
    return (B64) checkType(item, BASE64_BINARY);
  }

  /**
   * Converts an item (token, binary item) to a byte array.
   * @param item item to be converted
   * @return byte array
   * @throws QueryException query exception
   */
  protected final byte[] toBytes(final Item item) throws QueryException {
    if(item.type.isStringOrUntyped()) return item.string(info);
    if(item instanceof Bin) return ((Bin) item).binary(info);
    throw STRBIN_X_X.get(info, item.seqType(), item);
  }

  /**
   * Evaluates an expression to a QName.
   * @param expr expression
   * @param qc query context
   * @return QName, or {@code null} if the expression yields an empty sequence
   * @throws QueryException query exception
   */
  protected final QNm toQNmOrNull(final Expr expr, final QueryContext qc) throws QueryException {
    final Item item = expr.atomItem(qc, info);
    return item.isEmpty() ? null : toQNm(item);
  }

  /**
   * Converts an item to a QName.
   * @param item item
   * @return QName
   * @throws QueryException query exception
   */
  protected final QNm toQNm(final Item item) throws QueryException {
    final Type type = item.type;
    if(type == QNAME) return (QNm) item;
    if(type.isUntyped()) throw NSSENS_X_X.get(info, type, QNAME);
    throw typeError(item, QNAME, info);
  }

  /**
   * Evaluates an expression to a function item.
   * @param expr expression
   * @param qc query context
   * @return function item
   * @throws QueryException query exception
   */
  protected final FItem toFunction(final Expr expr, final QueryContext qc) throws QueryException {
    return (FItem) checkType(expr.item(qc, info), SeqType.FUNCTION);
  }

  /**
   * Evaluates an expression to a map.
   * @param expr expression
   * @param qc query context
   * @return map
   * @throws QueryException query exception
   */
  protected final XQMap toMap(final Expr expr, final QueryContext qc) throws QueryException {
    return toMap(expr.item(qc, info));
  }

  /**
   * Converts an item to a map.
   * @param item item to check
   * @return map
   * @throws QueryException query exception
   */
  protected final XQMap toMap(final Item item) throws QueryException {
    if(item instanceof XQMap) return (XQMap) item;
    throw typeError(item, SeqType.MAP, info);
  }

  /**
   * Converts an item to a map and checks its entries.
   * @param item item to check
   * @param keys record keys
   * @return map
   * @throws QueryException query exception
   */
  protected final XQMap toRecord(final Item item, final AStr... keys) throws QueryException {
    final XQMap map = toMap(item);
    for(final AStr key : keys) {
      if(!map.contains(key, info)) throw INVCONVERT_X_X_X.get(info, item.type, "record()", item);
    }
    return map;
  }

  /**
   * Returns an enumeration value.
   * @param <T> enumeration type
   * @param item item to check
   * @param keys record keys
   * @return enum value
   * @throws QueryException query exception
   */
  protected final <T extends Enum<T>> T toEnum(final Item item, final Class<T> keys)
      throws QueryException {
    final String value = toString(item);
    for(final T key : keys.getEnumConstants()) {
      if(value.equals(key.toString())) return key;
    }
    throw EXP_FOUND_X.get(info, Arrays.toString(keys.getEnumConstants()), value);
  }

  /**
   * Evaluates an expression to an array.
   * @param expr expression
   * @param qc query context
   * @return array
   * @throws QueryException query exception
   */
  protected final XQArray toArray(final Expr expr, final QueryContext qc) throws QueryException {
    return toArray(expr.item(qc, info));
  }

  /**
   * Converts an item to an array.
   * @param item item to check
   * @return the array
   * @throws QueryException if the item is not an array
   */
  protected final XQArray toArray(final Item item) throws QueryException {
    if(item instanceof XQArray) return (XQArray) item;
    throw typeError(item, SeqType.ARRAY, info);
  }

  /**
   * Evaluates an expression and returns an item if it has the specified type.
   * @param expr expression
   * @param type expected type
   * @param qc query context
   * @return item
   * @throws QueryException query exception
   */
  protected final Item checkType(final Expr expr, final AtomType type, final QueryContext qc)
      throws QueryException {
    return checkType(expr.atomItem(qc, info), type);
  }

  /**
   * Returns an item if it has the specified type.
   * @param item item
   * @param type expected type
   * @return item
   * @throws QueryException query exception
   */
  protected final Item checkType(final Item item, final Type type) throws QueryException {
    if(item.type.instanceOf(type)) return item;
    throw typeError(item, type, info);
  }

  /**
   * Returns a value as item if it has the specified type.
   * @param value value
   * @param type expected type
   * @return item
   * @throws QueryException query exception
   */
  protected final Item checkType(final Value value, final Type type) throws QueryException {
    if(value.seqType().instanceOf(type.seqType())) return (Item) value;
    throw typeError(value, type, info);
  }
}
