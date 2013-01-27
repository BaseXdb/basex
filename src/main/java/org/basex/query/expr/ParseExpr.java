package org.basex.query.expr;

import static org.basex.query.QueryText.*;
import static org.basex.query.util.Err.*;
import static org.basex.util.Token.*;

import org.basex.core.*;
import org.basex.data.*;
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
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public abstract class ParseExpr extends Expr {
  /** Input information. */
  public final InputInfo info;
  /** Cardinality of result; unknown if set to -1. */
  public long size = -1;
  /** Static type. */
  public SeqType type;

  /**
   * Constructor.
   * @param ii input info
   */
  protected ParseExpr(final InputInfo ii) {
    info = ii;
  }

  @Override
  public Iter iter(final QueryContext ctx) throws QueryException {
    final Item it = item(ctx, info);
    return it != null ? it.iter() : Empty.ITER;
  }

  @Override
  public Item item(final QueryContext ctx, final InputInfo ii) throws QueryException {
    final Iter ir = iter(ctx);
    final Item it = ir.next();
    if(it == null || ir.size() == 1) return it;
    Item n = ir.next();
    if(n != null) {
      final ValueBuilder vb = new ValueBuilder();
      vb.add(it);
      vb.add(n);
      n = ir.next();
      if(n != null) vb.add(Str.get("..."));
      XPSEQ.thrw(ii, vb.value());
    }
    return it;
  }

  @Override
  public Value value(final QueryContext ctx) throws QueryException {
    if(type().zeroOrOne()) {
      final Value v = item(ctx, info);
      return v == null ? Empty.SEQ : v;
    }
    return ctx.iter(this).value();
  }

  @Override
  public final Item ebv(final QueryContext ctx, final InputInfo ii)
      throws QueryException {

    final Item it;
    if(type().zeroOrOne()) {
      it = item(ctx, info);
    } else {
      final Iter ir = iter(ctx);
      it = ir.next();
      if(!(it == null || it instanceof ANode || ir.next() == null))
        CONDTYPE.thrw(info, this);
    }
    return it == null ? Bln.FALSE : it;
  }

  @Override
  public final Item test(final QueryContext ctx, final InputInfo ii)
      throws QueryException {

    final Item it = ebv(ctx, info);
    return (it instanceof ANum ? it.dbl(info) == ctx.pos : it.bool(info)) ? it : null;
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
   * @param ctx query context
   * @return optimized expression
   * @throws QueryException query exception
   */
  public final Expr preEval(final QueryContext ctx) throws QueryException {
    return optPre(item(ctx, info), ctx);
  }

  /**
   * Adds an optimization info for pre-evaluating the specified expression.
   * @param opt optimized expression
   * @param ctx query context
   * @return optimized expression
   */
  protected final Expr optPre(final Expr opt, final QueryContext ctx) {
    if(opt != this) ctx.compInfo(OPTPRE, this);
    return opt == null ? Empty.SEQ : opt;
  }

  /**
   * Returns a boolean equivalent for the specified expression.
   * If the specified expression yields a boolean value anyway, it will be
   * returned as is. Otherwise, it will be wrapped into a boolean function.
   * @param e expression to be rewritten
   * @return expression
   */
  protected final Expr compBln(final Expr e) {
    return e.type().eq(SeqType.BLN) ? e : Function.BOOLEAN.get(info, e);
  }

  // VALIDITY CHECKS ==========================================================

  /**
   * Ensures that the specified expression performs no updates.
   * Otherwise, throws an exception.
   * @param e expression (may be {@code null})
   * @throws QueryException query exception
   */
  public void checkNoUp(final Expr e) throws QueryException {
    if(e != null && e.uses(Use.UPD)) UPNOT.thrw(info, description());
  }

  /**
   * Ensures that none of the specified expressions performs an update.
   * Otherwise, throws an exception.
   * @param expr expressions (may contain {@code null} references)
   * @throws QueryException query exception
   */
  public final void checkNoneUp(final Expr... expr) throws QueryException {
    if(expr != null) for(final Expr e : expr) checkNoUp(e);
  }

  /**
   * Ensures that all specified expressions are either updating or vacuous.
   * Otherwise, throws an exception.
   * @param expr expressions to be checked
   * @throws QueryException query exception
   */
  public void checkAllUp(final Expr... expr) throws QueryException {
    int s = 0;
    for(final Expr e : expr) {
      e.checkUp();
      if(e.isVacuous()) continue;
      final boolean u = e.uses(Use.UPD);
      if(u && s == 2 || !u && s == 1) UPALL.thrw(info, description());
      s = u ? 1 : 2;
    }
  }

  /**
   * Checks if the specified expression yields a boolean.
   * Returns the boolean or throws an exception.
   * @param e expression to be checked
   * @param ctx query context
   * @return boolean
   * @throws QueryException query exception
   */
  public final boolean checkBln(final Expr e, final QueryContext ctx)
      throws QueryException {

    final Item it = checkNoEmpty(e.item(ctx, info), AtomType.BLN);
    final Type ip = it.type;
    if(ip == AtomType.BLN || ip.isUntyped()) return it.bool(info);
    throw Err.type(this, AtomType.BLN, it);
  }

  /**
   * Checks if the specified expression yields a double.
   * Returns the double or throws an exception.
   * @param e expression to be checked
   * @param ctx query context
   * @return double
   * @throws QueryException query exception
   */
  public final double checkDbl(final Expr e, final QueryContext ctx)
      throws QueryException {

    final Item it = checkNoEmpty(e.item(ctx, info), AtomType.DBL);
    if(it.type.isNumberOrUntyped()) return it.dbl(info);
    throw number(this, it);
  }

  /**
   * Checks if the specified expression is an integer.
   * Returns a token representation or an exception.
   * @param e expression to be checked
   * @param ctx query context
   * @return integer value
   * @throws QueryException query exception
   */
  public final long checkItr(final Expr e, final QueryContext ctx) throws QueryException {
    return checkItr(checkNoEmpty(e.item(ctx, info), AtomType.ITR));
  }

  /**
   * Checks if the specified item is a number.
   * Returns a token representation or an exception.
   * @param it item to be checked
   * @return item
   * @throws QueryException query exception
   */
  public final long checkItr(final Item it) throws QueryException {
    final Type ip = it.type;
    if(ip.instanceOf(AtomType.ITR) || ip.isUntyped()) return it.itr(info);
    throw Err.type(this, AtomType.ITR, it);
  }

  /**
   * Checks if the specified expression is a node.
   * Returns the node or an exception.
   * @param it item to be checked
   * @return item
   * @throws QueryException query exception
   */
  public final ANode checkNode(final Item it) throws QueryException {
    if(it instanceof ANode) return (ANode) it;
    throw Err.type(this, NodeType.NOD, it);
  }

  /**
   * Checks if the specified expression is a database node.
   * Returns the node or an exception.
   * @param it item to be checked
   * @return item
   * @throws QueryException query exception
   */
  public final DBNode checkDBNode(final Item it) throws QueryException {
    if(it instanceof DBNode) return (DBNode) it;
    throw BXDB_NODB.thrw(info, this);
  }

  /**
   * Checks if the specified collation is supported.
   * @param e expression to be checked
   * @param ctx query context
   * @throws QueryException query exception
   */
  public final void checkColl(final Expr e, final QueryContext ctx)
      throws QueryException {

    final byte[] u = checkStr(e, ctx);
    if(eq(URLCOLL, u)) return;
    final Uri uri = Uri.uri(u);
    if(uri.isAbsolute() || !eq(ctx.sc.baseURI().resolve(uri).string(), URLCOLL))
      IMPLCOL.thrw(info, e);
    }

  /**
   * Checks if the specified expression yields a string.
   * Returns a token representation or an exception.
   * @param e expression to be evaluated
   * @param ctx query context
   * @return string representation
   * @throws QueryException query exception
   */
  public final byte[] checkStr(final Expr e, final QueryContext ctx)
      throws QueryException {

    final Item it = checkItem(e, ctx);
    final Type ip = it.type;
    if(ip.isStringOrUntyped()) return it.string(info);
    throw Err.type(this, AtomType.STR, it);
  }

  /**
   * Checks if the specified item is a string or an empty sequence.
   * Returns a token representation or an exception.
   * @param it item to be checked
   * @return string representation
   * @throws QueryException query exception
   */
  public final byte[] checkEStr(final Item it) throws QueryException {
    if(it == null) return EMPTY;
    final Type ip = it.type;
    if(ip.isStringOrUntyped()) return it.string(info);
    throw Err.type(this, AtomType.STR, it);
  }

  /**
   * Throws an exception if the context value is not set.
   * @param ctx query context
   * @return context
   * @throws QueryException query exception
   */
  public final Value checkCtx(final QueryContext ctx) throws QueryException {
    final Value v = ctx.value;
    if(v != null) return v;
    throw XPNOCTX.thrw(info, this);
  }

  /**
   * Throws an exception if the context value is not a node.
   * @param ctx query context
   * @return context
   * @throws QueryException query exception
   */
  public final ANode checkNode(final QueryContext ctx) throws QueryException {
    final Value v = ctx.value;
    if(v == null) XPNOCTX.thrw(info, this);
    if(!(v instanceof ANode)) STEPNODE.thrw(info, this, v.type);
    return (ANode) v;
  }

  /**
   * Checks if the specified expression yields a non-empty item.
   * @param e expression to be evaluated
   * @param ctx query context
   * @return item
   * @throws QueryException query exception
   */
  public final Item checkItem(final Expr e, final QueryContext ctx)
      throws QueryException {
    return checkNoEmpty(e.item(ctx, info));
  }

  /**
   * Checks if the specified expression yields a binary item.
   * @param e expression to be evaluated
   * @param ctx query context
   * @return binary item
   * @throws QueryException query exception
   */
  public final Bin checkBinary(final Expr e, final QueryContext ctx)
      throws QueryException {

    final Item it = checkItem(e, ctx);
    if(it instanceof Bin) return (Bin) it;
    throw BINARYTYPE.thrw(info, it.type);
  }

  /**
   * Checks if the specified expression yields a string or binary item.
   * @param it item to be checked
   * @return byte representation
   * @throws QueryException query exception
   */
  public final byte[] checkStrBin(final Item it) throws QueryException {
    if(it instanceof AStr) return it.string(info);
    if(it instanceof Bin) return ((Bin) it).binary(info);
    throw STRBINTYPE.thrw(info, it.type);
  }

  /**
   * Checks if the specified expression is an empty sequence; if yes, throws
   * an exception.
   * @param it item to be checked
   * @param t type to be checked
   * @return specified item
   * @throws QueryException query exception
   */
  public final Item checkType(final Item it, final Type t) throws QueryException {
    if(checkNoEmpty(it).type.instanceOf(t)) return it;
    throw Err.type(this, t, it);
  }

  /**
   * Checks if the specified item is no empty sequence.
   * @param it item to be checked
   * @return specified item
   * @throws QueryException query exception
   */
  public final Item checkNoEmpty(final Item it) throws QueryException {
    if(it != null) return it;
    throw XPEMPTY.thrw(info, description());
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
    throw XPEMPTYPE.thrw(info, description(), t);
  }

  /**
   * Checks if the specified expression yields a string or empty sequence.
   * Returns a token representation or an exception.
   * @param e expression to be evaluated
   * @param ctx query context
   * @return item
   * @throws QueryException query exception
   */
  public final byte[] checkEStr(final Expr e, final QueryContext ctx)
      throws QueryException {
    return checkEStr(e.item(ctx, info));
  }

  /**
   * Checks if the current user has create permissions. If negative, an
   * exception is thrown.
   * @param ctx query context
   * @throws QueryException query exception
   */
  public final void checkAdmin(final QueryContext ctx) throws QueryException {
    checkPerm(ctx, Perm.ADMIN);
  }

  /**
   * Checks if the current user has create permissions. If negative, an
   * exception is thrown.
   * @param ctx query context
   * @throws QueryException query exception
   */
  public final void checkCreate(final QueryContext ctx) throws QueryException {
    checkPerm(ctx, Perm.CREATE);
  }

  /**
   * Checks if the current user has write permissions for the specified database.
   * If negative, an exception is thrown.
   * @param data data reference
   * @param ctx query context
   * @return data reference
   * @throws QueryException query exception
   */
  public final Data checkWrite(final Data data, final QueryContext ctx)
      throws QueryException {

    if(!ctx.context.perm(Perm.WRITE, data.meta)) BASX_PERM.thrw(info, Perm.WRITE);
    return data;
  }

  /**
   * Checks if the current user has given permissions. If negative, an
   * exception is thrown.
   * @param ctx query context
   * @param p permission
   * @throws QueryException query exception
   */
  private void checkPerm(final QueryContext ctx, final Perm p) throws QueryException {
    if(!ctx.context.user.has(p)) throw BASX_PERM.thrw(info, p);
  }

  /**
   * Assures that the given (non-{@code null}) item is a map.
   * @param it item to check
   * @return the map
   * @throws QueryException if the item is not a map
   */
  public Map checkMap(final Item it) throws QueryException {
    if(it instanceof Map) return (Map) it;
    throw Err.type(this, SeqType.ANY_MAP, it);
  }
}
