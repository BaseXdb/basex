package org.basex.query.expr;

import static org.basex.query.QueryError.*;
import static org.basex.query.value.type.BasicType.*;
import static org.basex.query.value.type.NodeType.*;
import static org.basex.util.Token.*;

import java.math.*;
import java.util.*;

import org.basex.core.users.*;
import org.basex.data.*;
import org.basex.io.*;
import org.basex.query.*;
import org.basex.query.ann.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.util.*;
import org.basex.query.util.collation.*;
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
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public abstract class ParseExpr extends Expr {
  /** Indicates which expressions have an iterator implementation. */
  private static final ClassValue<Boolean> ITERBASED = new ClassValue<>() {
    @Override
    protected Boolean computeValue(final Class<?> clazz) {
      for(Class<?> clz = clazz; clz != ParseExpr.class; clz = clz.getSuperclass()) {
        try {
          if(clz.getMethod("iter", QueryContext.class).getDeclaringClass() == clz) return true;
        } catch(final Exception ignore) { }
      }
      return false;
    }
  };
  /** Expression type. */
  public final ExprType exprType;
  /** Input information (can be {@code null}). */
  protected InputInfo info;
  /** Iterator-based implementation. */
  private final boolean iterBased;

  /**
   * Constructor.
   * @param info input info (can be {@code null})
   * @param seqType sequence type
   */
  protected ParseExpr(final InputInfo info, final SeqType seqType) {
    this.info = info;
    exprType = new ExprType(seqType);
    iterBased = ITERBASED.get(getClass());
  }

  @Override
  public Expr optimize(final CompileContext cc) throws QueryException {
    return this;
  }

  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    return value(qc).iter();
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    return iter(qc).value(qc, this);
  }

  @Override
  public boolean eager() {
    return !iterBased;
  }

  @Override
  public final Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    if(iterBased) {
      final Iter iter = iter(qc);
      final Item item1 = iter.next();
      if(item1 == null) return Empty.VALUE;
      final Item item2 = iter.next();
      if(item2 == null) return item1;
      throw typeError(item1.append(item2, qc), BasicType.ITEM, info);
    }
    return value(qc).item(qc, info);
  }

  @Override
  public final Iter atomIter(final QueryContext qc, final InputInfo ii) throws QueryException {
    return toAtomIter(qc, info);
  }

  @Override
  public final Item atomItem(final QueryContext qc, final InputInfo ii) throws QueryException {
    return atomValue(qc, info).item(qc, info);
  }

  @Override
  public final Value atomValue(final QueryContext qc, final InputInfo ii) throws QueryException {
    return value(qc).atomValue(qc, info);
  }

  @Override
  public final Value unwrappedValue(final QueryContext qc) throws QueryException {
    return value(qc).unwrappedValue(qc);
  }

  /**
   * Computes the effective boolean value of this expression.
   * @param qc query context
   * @return result of check
   * @throws QueryException query exception
   */
  protected boolean ebv(final QueryContext qc) throws QueryException {
    // single item
    if(seqType().zeroOrOne()) return item(qc, info).ebv(qc, info);
    // empty sequence?
    final Iter iter = iter(qc);
    final Item item1 = iter.next();
    if(item1 == null) return false;
    // sequence starting with node?
    if(item1 instanceof GNode) return true;
    // single item?
    final Item item2 = iter.next();
    if(item2 == null) return item1.ebv(qc, info);
    throw testError(item1.append(item2, qc), false, info);
  }

  @Override
  public final boolean ebv(final QueryContext qc, final InputInfo ii) throws QueryException {
    return ebv(qc);
  }

  @Override
  public final boolean predicate(final QueryContext qc, final InputInfo ii, final long pos)
      throws QueryException {
    return seqType().mayBeNumber() ? value(qc).predicate(qc, info, pos) : ebv(qc);
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
  public final InputInfo info() {
    return info;
  }

  /**
   * Returns the current static context.
   * @return static context (can be {@code null})
   */
  public final StaticContext sc() {
    return info != null ? info.sc() : null;
  }

  /**
   * Returns the default collation of the static context.
   * @return collation (can be {@code null})
   */
  public final Collation defaultCollation() {
    return info != null ? info.sc().collation : null;
  }

  // OPTIMIZATIONS ================================================================================

  /**
   * Assigns this expression's type to the specified expression.
   * @param <T> expression type
   * @param expr expression to be modified
   * @return specified expression
   */
  public final <T extends Expr> T copyType(final T expr) {
    if(expr instanceof final ParseExpr pe) pe.exprType.assign(this);
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
   * @param expr expression
   * @param updating indicates if expression is expected to be updating
   * @return specified expression
   * @throws QueryException query exception
   */
  protected final <T extends XQFunctionExpr> T checkUp(final T expr, final boolean updating)
      throws QueryException {
    if(updating != expr.annotations().contains(Annotation.UPDATING)) {
      if(!updating) throw FUNCUP_X.get(info, expr);
      if(!expr.vacuousBody()) throw FUNCNOTUP_X.get(info, expr);
    }
    return expr;
  }

  /**
   * Ensures that none of the specified expressions performs an update.
   * Otherwise, throws an exception.
   * @param exprs expressions, possibly with {@code null} entries (can be {@code null})
   * @throws QueryException query exception
   */
  protected final void checkNoUp(final Expr... exprs) throws QueryException {
    if(exprs == null) return;
    for(final Expr expr : exprs) {
      if(expr == null) continue;
      expr.checkUp();
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
    Boolean updating = null;
    for(final Expr expr : exprs) {
      expr.checkUp();
      final boolean upd = expr.has(Flag.UPD), vacuous = expr.vacuous();
      if(upd ? updating == Boolean.FALSE : !vacuous && updating == Boolean.TRUE) {
        throw UPALL.get(info);
      }
      if(!vacuous) updating = upd;
    }
  }

  /**
   * Raises an error if the current user or the code of this expression lacks the given permission.
   * @param qc query context
   * @param perm minimum permission required
   * @throws QueryException query exception
   */
  protected final void checkPerm(final QueryContext qc, final Perm perm) throws QueryException {
    qc.checkPerm(perm, null, this, info);
  }

  /**
   * Returns the maximum permission of the code of this expression.
   * @param qc query context
   * @return permission
   */
  protected final Perm perm(final QueryContext qc) {
    return qc.perm(info);
  }

  /**
   * Indicates if the code of this expression may access external resources.
   * @param qc query context
   * @return result of check
   */
  protected final boolean trusted(final QueryContext qc) {
    return qc.trusted(info);
  }

  /**
   * Raises an error if the current user or the code of this expression lacks the given permission
   * for the specified database.
   * @param qc query context
   * @param perm minimum permission required
   * @param name name of database
   * @throws QueryException query exception
   */
  protected final void checkPerm(final QueryContext qc, final Perm perm, final String name)
      throws QueryException {
    qc.checkPerm(perm, name, this, info);
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
    return toToken(toAtomItem(expr, qc), expr);
  }

  /**
   * Evaluates an expression to a token.
   * @param expr expression
   * @param qc query context
   * @return token (zero-length if the expression yields an empty sequence)
   * @throws QueryException query exception
   */
  protected final byte[] toZeroToken(final Expr expr, final QueryContext qc) throws QueryException {
    final Item item = atomItem(expr, qc);
    return item.isEmpty() ? Token.EMPTY : toToken(item, expr);
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
    final Item item = atomItem(expr, qc);
    return item.isEmpty() ? null : toToken(item, expr);
  }

  /**
   * Converts an item to a token.
   * @param item item to be converted
   * @return token
   * @throws QueryException query exception
   */
  protected final byte[] toToken(final Item item) throws QueryException {
    return toToken(item, (Expr) null);
  }

  /**
   * Converts an item to a token.
   * @param item item to be converted
   * @param expr expression that yielded the item (can be {@code null})
   * @return token
   * @throws QueryException query exception
   */
  protected final byte[] toToken(final Item item, final Expr expr) throws QueryException {
    final Type type = item.type;
    if(type.isStringOrUntyped()) return item.string(info);
    throw item instanceof final FItem fitem ? atomizeError(fitem, argName(expr), info) :
      argTypeError(item, STRING, expr);
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
   * Converts an item to a string.
   * @param item item to be converted
   * @return string
   * @throws QueryException query exception
   */
  protected final String toString(final Item item) throws QueryException {
    return Token.string(toToken(item));
  }

  /**
   * Converts an item to a string.
   * @param item item to be converted
   * @param expr expression that yielded the item (can be {@code null})
   * @return string
   * @throws QueryException query exception
   */
  protected final String toString(final Item item, final Expr expr) throws QueryException {
    return Token.string(toToken(item, expr));
  }

  /**
   * Evaluates an expression to a string.
   * @param expr expression
   * @param qc query context
   * @return string (zero-length if the expression yields an empty sequence)
   * @throws QueryException query exception
   */
  protected final String toZeroString(final Expr expr, final QueryContext qc)
      throws QueryException {
    final Item item = atomItem(expr, qc);
    return item.isEmpty() ? "" : toString(item, expr);
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
    return toStringOrNull(expr.value(qc), expr, qc);
  }

  /**
   * Converts a value to a string.
   * @param value value
   * @param expr expression that yielded the value
   * @param qc query context
   * @return string, or {@code null} if the value is empty
   * @throws QueryException query exception
   */
  protected final String toStringOrNull(final Value value, final Expr expr,
      final QueryContext qc) throws QueryException {
    final Item item = atomItem(value, expr, qc);
    return item.isEmpty() ? null : toString(item, expr);
  }

  /**
   * Evaluates an expression to a boolean.
   * @param expr expression
   * @param qc query context
   * @return boolean
   * @throws QueryException query exception
   */
  protected final boolean toBoolean(final Expr expr, final QueryContext qc) throws QueryException {
    return toBoolean(atomItem(expr, qc), expr);
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
    final Item item = atomItem(expr, qc);
    return !item.isEmpty() && toBoolean(item, expr);
  }

  /**
   * Converts an item to a boolean.
   * @param item item to be converted
   * @return boolean
   * @throws QueryException query exception
   */
  protected final boolean toBoolean(final Item item) throws QueryException {
    return toBoolean(item, (Expr) null);
  }

  /**
   * Converts an item to a boolean.
   * @param item item to be converted
   * @param expr expression that yielded the item (can be {@code null})
   * @return boolean
   * @throws QueryException query exception
   */
  protected final boolean toBoolean(final Item item, final Expr expr) throws QueryException {
    final Type type = item.type;
    if(type == BOOLEAN) return item.bool(info);
    if(type.isUntyped()) return Bln.parse(item, info);
    throw argTypeError(item, BOOLEAN, expr);
  }

  /**
   * Evaluates an expression to a double number.
   * @param expr expression
   * @param qc query context
   * @return double
   * @throws QueryException query exception
   */
  protected final double toDouble(final Expr expr, final QueryContext qc) throws QueryException {
    return toDouble(atomItem(expr, qc), expr);
  }

  /**
   * Evaluates an expression to a double number.
   * @param expr expression
   * @param qc query context
   * @return double, or {@code null} if the expression yields an empty sequence
   * @throws QueryException query exception
   */
  protected final Double toDoubleOrNull(final Expr expr, final QueryContext qc)
      throws QueryException {
    final Item item = atomItem(expr, qc);
    return item.isEmpty() ? null : toDouble(item, expr);
  }

  /**
   * Converts an item to a double number.
   * @param item item
   * @return double
   * @throws QueryException query exception
   */
  protected final double toDouble(final Item item) throws QueryException {
    return toDouble(item, (Expr) null);
  }

  /**
   * Converts an item to a double number.
   * @param item item
   * @param expr expression that yielded the item (can be {@code null})
   * @return double
   * @throws QueryException query exception
   */
  protected final double toDouble(final Item item, final Expr expr) throws QueryException {
    if(item.type.isNumberOrUntyped()) return item.dbl(info);
    throw argTypeError(item, NUMERIC, expr);
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
    final Item item = atomItem(expr, qc);
    return item.isEmpty() ? null : toNumber(item, expr);
  }

  /**
   * Converts an item to a number.
   * @param item item to be converted
   * @param expr expression that yielded the item (can be {@code null})
   * @return number
   * @throws QueryException query exception
   */
  protected final ANum toNumber(final Item item, final Expr expr) throws QueryException {
    if(item instanceof final ANum num) return num;
    if(item.type.isUntyped()) return (ANum) sc().numericType().cast(item, null, info);
    throw argTypeError(item, NUMERIC, expr);
  }

  /**
   * Evaluates an expression to a long number.
   * @param expr expression
   * @param qc query context
   * @return long number
   * @throws QueryException query exception
   */
  protected final long toLong(final Expr expr, final QueryContext qc) throws QueryException {
    return toLong(atomItem(expr, qc), expr);
  }

  /**
   * Evaluates an expression to a long number.
   * @param expr expression
   * @param qc query context
   * @return long number, or {@code null} if the expression yields an empty sequence
   * @throws QueryException query exception
   */
  protected final Long toLongOrNull(final Expr expr, final QueryContext qc) throws QueryException {
    final Item item = atomItem(expr, qc);
    return item.isEmpty() ? null : toLong(item, expr);
  }

  /**
   * Converts an item to a long number.
   * @param item item to be converted
   * @return long number
   * @throws QueryException query exception
   */
  protected final long toLong(final Item item) throws QueryException {
    return toLong(item, (Expr) null);
  }

  /**
   * Converts an item to a long number.
   * @param item item to be converted
   * @param expr expression that yielded the item (can be {@code null})
   * @return long number
   * @throws QueryException query exception
   */
  protected final long toLong(final Item item, final Expr expr) throws QueryException {
    final Type type = item.type;
    if(type.instanceOf(INTEGER) || type.isUntyped()) return item.itr(info);
    if(type == DECIMAL) {
      // relabel integral decimals (round trip fails for fractional digits and long overflows)
      final BigDecimal dec = item.dec(info);
      final long l = dec.longValue();
      if(dec.compareTo(BigDecimal.valueOf(l)) == 0) return l;
    }
    throw argTypeError(item, INTEGER, expr);
  }

  /**
   * Evaluates an expression to a long number that is not smaller than the specified minimum.
   * @param expr expression
   * @param min minimum allowed value ({@code 0} or {@code 1})
   * @param qc query context
   * @return long number
   * @throws QueryException query exception
   */
  protected final long toLong(final Expr expr, final long min, final QueryContext qc)
      throws QueryException {
    final Item item = atomItem(expr, qc);
    final long value = toLong(item, expr);
    if(value >= min) return value;
    throw argTypeError(item, min == 0 ? NON_NEGATIVE_INTEGER : POSITIVE_INTEGER, expr);
  }

  /**
   * Evaluates an expression to a GNode.
   * @param expr expression
   * @param qc query context
   * @return GNode, or {@code null} if the expression yields an empty sequence
   * @throws QueryException query exception
   */
  protected final GNode toGNodeOrNull(final Expr expr, final QueryContext qc)
      throws QueryException {
    final Item item = expr.item(qc, info);
    return item.isEmpty() ? null : toGNode(item, expr);
  }

  /**
   * Converts an item to a GNode.
   * @param item item to be converted
   * @return GNode
   * @throws QueryException query exception
   */
  protected final GNode toGNode(final Item item) throws QueryException {
    return toGNode(item, null);
  }

  /**
   * Converts an item to a GNode.
   * @param item item to be converted
   * @param expr expression that yielded the item (can be {@code null})
   * @return GNode
   * @throws QueryException query exception
   */
  protected final GNode toGNode(final Item item, final Expr expr) throws QueryException {
    if(item instanceof final GNode node) return node;
    throw argTypeError(item, NODE, expr);
  }

  /**
   * Evaluates an expression to a JNode.
   * @param expr expression
   * @param qc query context
   * @return JNode, or {@code null} if the expression yields an empty sequence
   * @throws QueryException query exception
   */
  protected final JNode toJNodeOrNull(final Expr expr, final QueryContext qc)
      throws QueryException {
    final Item item = expr.item(qc, info);
    return item.isEmpty() ? null : toJNode(item, expr);
  }

  /**
   * Converts an item to a JNode.
   * @param item item
   * @param expr expression that yielded the item (can be {@code null})
   * @return JNode
   * @throws QueryException query exception
   */
  protected final JNode toJNode(final Item item, final Expr expr) throws QueryException {
    if(item instanceof final JNode node) return node;
    throw argTypeError(item, JNODE, expr);
  }

  /**
   * Evaluates an expression to a node.
   * @param expr expression
   * @param qc query context
   * @return node
   * @throws QueryException query exception
   */
  protected final XNode toNode(final Expr expr, final QueryContext qc) throws QueryException {
    return toNode(expr.unwrappedItem(qc, info), expr);
  }

  /**
   * Evaluates an expression to a node.
   * @param expr expression
   * @param qc query context
   * @return node, or {@code null} if the expression yields an empty sequence
   * @throws QueryException query exception
   */
  protected final XNode toNodeOrNull(final Expr expr, final QueryContext qc) throws QueryException {
    final Item item = expr.unwrappedItem(qc, info);
    return item.isEmpty() ? null : toNode(item, expr);
  }

  /**
   * Converts an item to a node.
   * @param item item to be converted
   * @param expr expression that yielded the item (can be {@code null})
   * @return node
   * @throws QueryException query exception
   */
  protected final XNode toNode(final Item item, final Expr expr) throws QueryException {
    if(item instanceof final XNode node) return node;
    throw argTypeError(item, XNODE, expr);
  }

  /**
   * Evaluates an expression to an atomized item.
   * @param expr expression
   * @param qc query context
   * @return atomized item
   * @throws QueryException query exception
   */
  protected final Item toAtomItem(final Expr expr, final QueryContext qc) throws QueryException {
    final Item item = atomItem(expr, qc);
    if(item == Empty.VALUE) throw argTypeError(item, ITEM, expr);
    return item;
  }

  /**
   * Evaluates an expression to an atomized item or an empty sequence.
   * @param expr expression
   * @param qc query context
   * @return atomized item or {@link Empty#VALUE}
   * @throws QueryException query exception
   */
  protected final Item atomItem(final Expr expr, final QueryContext qc) throws QueryException {
    return atomItem(expr.value(qc), expr, qc);
  }

  /**
   * Atomizes a value to a single item or an empty sequence.
   * @param value value
   * @param expr expression that yielded the value
   * @param qc query context
   * @return atomized item or {@link Empty#VALUE}
   * @throws QueryException query exception
   */
  protected final Item atomItem(final Value value, final Expr expr, final QueryContext qc)
      throws QueryException {
    final Value atoms = atomValue(value, expr, qc);
    if(atoms.size() > 1 && argName(expr) instanceof final QNm name) {
      throw typeError(atoms, Types.ITEM_O, name, info);
    }
    return atoms.item(qc, expr.info(info));
  }

  /**
   * Evaluates an expression to atomized items.
   * @param expr expression
   * @param qc query context
   * @return atomized items
   * @throws QueryException query exception
   */
  protected final Value atomValue(final Expr expr, final QueryContext qc) throws QueryException {
    return atomValue(expr.value(qc), expr, qc);
  }

  /**
   * Atomizes a value.
   * @param value value
   * @param expr expression that yielded the value
   * @param qc query context
   * @return atomized items
   * @throws QueryException query exception
   */
  protected final Value atomValue(final Value value, final Expr expr, final QueryContext qc)
      throws QueryException {
    try {
      return value.atomValue(qc, expr.info(info));
    } catch(final QueryException ex) {
      throw atomizeError(ex, value, argName(expr), info);
    }
  }

  /**
   * Evaluates an expression to an element.
   * @param expr expression
   * @param qc query context
   * @return element
   * @throws QueryException query exception
   */
  protected final XNode toElem(final Expr expr, final QueryContext qc) throws QueryException {
    return toElem(expr.unwrappedItem(qc, info), expr);
  }

  /**
   * Converts an item to an element.
   * @param item item to be converted
   * @param expr expression that yielded the item
   * @return element
   * @throws QueryException query exception
   */
  protected final XNode toElem(final Item item, final Expr expr) throws QueryException {
    if(item.type.instanceOf(ELEMENT)) return (XNode) item;
    throw argTypeError(item, ELEMENT, expr);
  }

  /**
   * Converts a value to a context node.
   * @param value value or {@code null}
   * @return context node
   * @throws QueryException query exception
   */
  protected final GNode toContextNode(final Value value) throws QueryException {
    if(value instanceof final GNode gnode) return gnode;
    if(value instanceof XQStruct) return new JNode(value);
    if(value == null) throw NOCTX_X.get(info, this);
    throw PATHNODE_X_X_X.get(info, this, value.seqType(), value);
  }

  /**
   * Evaluates an expression to a binary item.
   * @param expr expression
   * @param qc query context
   * @return binary item
   * @throws QueryException query exception
   */
  protected final Bin toBin(final Expr expr, final QueryContext qc) throws QueryException {
    return toBin(atomItem(expr, qc), expr);
  }

  /**
   * Converts an item to a binary item.
   * @param item item to be converted
   * @param expr expression that yielded the item (can be {@code null})
   * @return binary item
   * @throws QueryException query exception
   */
  protected final Bin toBin(final Item item, final Expr expr) throws QueryException {
    if(item instanceof final Bin bin) return bin;
    throw argTypeError(item, BINARY, expr);
  }

  /**
   * Evaluates an expression to a binary item.
   * @param expr expression
   * @param qc query context
   * @return binary item, or {@code null} if the expression yields an empty sequence
   * @throws QueryException query exception
   */
  protected final Bin toBinOrNull(final Expr expr, final QueryContext qc) throws QueryException {
    final Item item = atomItem(expr, qc);
    return item.isEmpty() ? null : toBin(item, expr);
  }

  /**
   * Evaluates an expression (token, binary item) to a byte array.
   * @param expr expression
   * @param qc query context
   * @return byte array
   * @throws QueryException query exception
   */
  protected final byte[] toBytes(final Expr expr, final QueryContext qc) throws QueryException {
    return toBytes(atomItem(expr, qc), expr);
  }

  /**
   * Converts an item (token, binary item) to a byte array.
   * @param item item to be converted
   * @param expr expression that yielded the item (can be {@code null})
   * @return byte array
   * @throws QueryException query exception
   */
  protected final byte[] toBytes(final Item item, final Expr expr) throws QueryException {
    if(item.type.isStringOrUntyped()) return item.string(info);
    if(item instanceof final Bin bin) return bin.binary(info);
    throw argTypeError(item, Types.STRING_OR_BINARY, expr);
  }

  /**
   * Extracts a binary source from the supplied item:
   * <ul>
   *   <li>binary items ({@code xs:base64Binary}, {@code xs:hexBinary}) are returned verbatim,</li>
   *   <li>string-typed/untyped atomic items are interpreted as URIs pointing to a file.</li>
   * </ul>
   * @param input input item
   * @param expr expression that yielded the item (can be {@code null})
   * @param qc query context
   * @return binary source (a {@link Bin} or an {@link IO} reference)
   * @throws QueryException query exception
   */
  protected final Object toBinarySource(final Item input, final Expr expr, final QueryContext qc)
      throws QueryException {
    if(input instanceof Bin) return input;
    if(!input.type.isStringOrUntyped()) throw argTypeError(input, Types.STRING_OR_BINARY, expr);
    final String string = string(input.string(info));
    final IO io = IO.get(string);
    if(io.isExternal()) checkPerm(qc, Perm.ADMIN);
    if(!io.exists() || io.isDir()) throw WHICHRES_X.get(info, string);
    return io;
  }

  /**
   * Evaluates an expression to a QName.
   * @param expr expression
   * @param qc query context
   * @return QName, or {@code null} if the expression yields an empty sequence
   * @throws QueryException query exception
   */
  protected final QNm toQNmOrNull(final Expr expr, final QueryContext qc) throws QueryException {
    final Item item = atomItem(expr, qc);
    return item.isEmpty() ? null : toQNm(item, expr);
  }

  /**
   * Converts an item to a QName.
   * @param item item
   * @param expr expression that yielded the item (can be {@code null})
   * @return QName
   * @throws QueryException query exception
   */
  protected final QNm toQNm(final Item item, final Expr expr) throws QueryException {
    final Type type = item.type;
    if(type == QNAME) return (QNm) item;
    if(type.isUntyped()) throw NSSENS_X_X.get(info, type, QNAME);
    throw argTypeError(item, QNAME, expr);
  }

  /**
   * Evaluates an expression to a function item.
   * @param expr expression
   * @param qc query context
   * @return function item
   * @throws QueryException query exception
   */
  protected final FItem toFunction(final Expr expr, final QueryContext qc) throws QueryException {
    return toFunction(expr.unwrappedItem(qc, info), expr);
  }

  /**
   * Converts an item to a function item.
   * @param item item to be converted
   * @param expr expression that yielded the item
   * @return function item
   * @throws QueryException query exception
   */
  protected final FItem toFunction(final Item item, final Expr expr) throws QueryException {
    if(item instanceof final FItem function) return function;
    throw argTypeError(item, Types.FUNCTION, expr);
  }

  /**
   * Evaluates an expression to a map.
   * @param expr expression
   * @param qc query context
   * @return map
   * @throws QueryException query exception
   */
  protected final XQMap toMap(final Expr expr, final QueryContext qc) throws QueryException {
    return toMap(expr.unwrappedItem(qc, info), expr);
  }

  /**
   * Converts an item to a map.
   * @param item item to check
   * @return map
   * @throws QueryException query exception
   */
  protected final XQMap toMap(final Item item) throws QueryException {
    return toMap(item, (Expr) null);
  }

  /**
   * Converts an item to a map.
   * @param item item to check
   * @param expr expression that yielded the item (can be {@code null})
   * @return map
   * @throws QueryException query exception
   */
  protected final XQMap toMap(final Item item, final Expr expr) throws QueryException {
    if(item instanceof final XQMap map) return map;
    throw argTypeError(item, Types.MAP, expr);
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
    final T key = Enums.get(keys, toString(item));
    if(key != null) return key;
    throw EXP_FOUND_X_X.get(info, Arrays.toString(keys.getEnumConstants()), item);
  }

  /**
   * Evaluates an expression to an array.
   * @param expr expression
   * @param qc query context
   * @return array
   * @throws QueryException query exception
   */
  protected final XQArray toArray(final Expr expr, final QueryContext qc) throws QueryException {
    return toArray(expr.unwrappedItem(qc, info), expr);
  }

  /**
   * Converts an item to an array.
   * @param item item to check
   * @param expr expression that yielded the item (can be {@code null})
   * @return array
   * @throws QueryException query exception
   */
  protected final XQArray toArray(final Item item, final Expr expr) throws QueryException {
    if(item instanceof final XQArray array) return array;
    throw argTypeError(item, Types.ARRAY, expr);
  }

  /**
   * Evaluates an expression and coerces the resulting item to the specified type.
   * @param expr expression
   * @param type expected type
   * @param qc query context
   * @return item
   * @throws QueryException query exception
   */
  protected final Item coerce(final Expr expr, final BasicType type, final QueryContext qc)
      throws QueryException {
    return (Item) coerce(expr, type.seqType(), qc);
  }

  /**
   * Coerces an item to the specified type.
   * @param item item
   * @param type expected type
   * @param expr expression that yielded the item (can be {@code null})
   * @param qc query context
   * @return item
   * @throws QueryException query exception
   */
  protected final Item coerce(final Item item, final BasicType type, final Expr expr,
      final QueryContext qc) throws QueryException {
    return (Item) coerce(item, type.seqType(), expr, qc);
  }

  /**
   * Evaluates an expression and coerces the resulting value to the specified type.
   * @param expr expression
   * @param type expected type
   * @param qc query context
   * @return coerced value
   * @throws QueryException query exception
   */
  protected final Value coerce(final Expr expr, final SeqType type, final QueryContext qc)
      throws QueryException {
    return coerce(expr.value(qc), type, expr, qc);
  }

  /**
   * Coerces a value to the specified type.
   * @param value value
   * @param type expected type
   * @param expr expression that yielded the value (can be {@code null})
   * @param qc query context
   * @return coerced value
   * @throws QueryException query exception
   */
  protected final Value coerce(final Value value, final SeqType type, final Expr expr,
      final QueryContext qc) throws QueryException {
    return type.coerce(value, qc, info, argName(expr), null);
  }

  /**
   * Returns a type exception for an item that does not have the expected type.
   * @param item item
   * @param type expected type
   * @param expr expression that yielded the item (can be {@code null})
   * @return query exception
   */
  protected QueryException argTypeError(final Item item, final Type type,
      @SuppressWarnings("unused") final Expr expr) {
    return typeError(item, type, info);
  }

  /**
   * Returns the name of the parameter to which an argument is bound.
   * @param expr argument expression (can be {@code null})
   * @return parameter name (can be {@code null})
   */
  protected QNm argName(@SuppressWarnings("unused") final Expr expr) {
    return null;
  }
}
