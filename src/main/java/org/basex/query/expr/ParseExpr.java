package org.basex.query.expr;

import static org.basex.query.QueryText.*;
import static org.basex.query.util.Err.*;
import static org.basex.util.Token.*;

import org.basex.core.*;
import org.basex.io.*;
import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.item.*;
import org.basex.query.item.map.*;
import org.basex.query.iter.*;
import org.basex.query.util.*;
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
      if(it != null && !it.type.isNode() && ir.next() != null)
        CONDTYPE.thrw(info, this);
    }
    return it == null ? Bln.FALSE : it;
  }

  @Override
  public final Item test(final QueryContext ctx, final InputInfo ii)
      throws QueryException {

    final Item it = ebv(ctx, info);
    return (it.type.isNumber() ? it.dbl(info) == ctx.pos :
      it.bool(info)) ? it : null;
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
   * Checks if the specified expressions is no updating expression.
   * @param e expression
   * @param ctx query context
   * @return the specified expression
   * @throws QueryException query exception
   */
  public final Expr checkUp(final Expr e, final QueryContext ctx) throws QueryException {
    if(e != null && ctx.updating() && e.uses(Use.UPD)) UPNOT.thrw(info, description());
    return e;
  }

  /**
   * Tests if the specified expressions are updating or vacuous.
   * @param ctx query context
   * @param expr expression array
   * @throws QueryException query exception
   */
  public void checkUp(final QueryContext ctx, final Expr... expr) throws QueryException {
    if(!ctx.updating()) return;
    int s = 0;
    for(final Expr e : expr) {
      if(e.isVacuous()) continue;
      final boolean u = e.uses(Use.UPD);
      if(u && s == 2 || !u && s == 1) UPNOT.thrw(info, description());
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
    if(!ip.isUntyped() && ip != AtomType.BLN) Err.type(this, AtomType.BLN, it);
    return it.bool(info);
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
    final Type ip = it.type;
    if(!ip.isUntyped() && !ip.isNumber()) number(this, it);
    return it.dbl(info);
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
    if(!ip.isUntyped() && !ip.instanceOf(AtomType.ITR))
      Err.type(this, AtomType.ITR, it);
    return it.itr(info);
  }

  /**
   * Checks if the specified expression is a node.
   * Returns the node or an exception.
   * @param it item to be checked
   * @return item
   * @throws QueryException query exception
   */
  public final ANode checkNode(final Item it) throws QueryException {
    if(!it.type.isNode()) Err.type(this, NodeType.NOD, it);
    return (ANode) it;
  }

  /**
   * Checks if the specified expression is a database node.
   * Returns the node or an exception.
   * @param it item to be checked
   * @return item
   * @throws QueryException query exception
   */
  public final DBNode checkDBNode(final Item it) throws QueryException {
    if(!(it instanceof DBNode)) NODBCTX.thrw(info, this);
    return (DBNode) it;
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
   * @return item
   * @throws QueryException query exception
   */
  public final byte[] checkStr(final Expr e, final QueryContext ctx)
      throws QueryException {

    final Item it = checkItem(e, ctx);
    final Type ip = it.type;
    if(!ip.isString() && !ip.isUntyped()) Err.type(this, AtomType.STR, it);
    return it.string(info);
  }

  /**
   * Checks if the specified item is a string or an empty sequence.
   * Returns a token representation or an exception.
   * @param it item to be checked
   * @return item
   * @throws QueryException query exception
   */
  public final byte[] checkEStr(final Item it) throws QueryException {
    if(it == null) return EMPTY;
    final Type ip = it.type;
    if(!ip.isString() && !ip.isUntyped()) Err.type(this, AtomType.STR, it);
    return it.string(info);
  }

  /**
   * Throws an exception if the context item is not set.
   * @param ctx query context
   * @return result of check
   * @throws QueryException query exception
   */
  public final Value checkCtx(final QueryContext ctx) throws QueryException {
    final Value v = ctx.value;
    if(v == null) XPNOCTX.thrw(info, this);
    return v;
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
   * Checks if the specified expression is an empty sequence; if yes, throws
   * an exception.
   * @param it item to be checked
   * @param t type to be checked
   * @return specified item
   * @throws QueryException query exception
   */
  public final Item checkType(final Item it, final Type t) throws QueryException {
    if(!checkNoEmpty(it).type.instanceOf(t)) Err.type(this, t, it);
    return it;
  }

  /**
   * Checks if the specified item is no empty sequence.
   * @param it item to be checked
   * @return specified item
   * @throws QueryException query exception
   */
  public final Item checkNoEmpty(final Item it) throws QueryException {
    if(it == null) XPEMPTY.thrw(info, description());
    return it;
  }

  /**
   * Checks if the specified item is no empty sequence.
   * @param it item to be checked
   * @param t expected type
   * @return specified item
   * @throws QueryException query exception
   */
  private Item checkNoEmpty(final Item it, final Type t) throws QueryException {
    if(it == null) XPEMPTYPE.thrw(info, description(), t);
    return it;
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
   * Checks if an expression yields a valid and existing {@link IO} instance.
   * Returns the instance or an exception.
   * @param e expression to be evaluated
   * @param ctx query context
   * @return io instance
   * @throws QueryException query exception
   */
  public final IO checkIO(final Expr e, final QueryContext ctx) throws QueryException {
    checkCreate(ctx);
    final String name = string(checkStr(e, ctx));
    IO io = IO.get(name);
    if(!io.exists()) {
      final IO iob = ctx.sc.baseIO();
      if(iob != null) {
        io = new IOFile(iob.path(), name);
        if(!io.exists()) RESFNF.thrw(info, name);
      }
    }
    return io;
  }

  /**
   * Checks if the current user has admin permissions. If negative, an
   * exception is thrown.
   * @param ctx query context
   * @throws QueryException query exception
   */
  public final void checkCreate(final QueryContext ctx) throws QueryException {
    checkPerm(ctx, Perm.CREATE);
  }

  /**
   * Checks if the current user has write permissions. If negative, an
   * exception is thrown.
   * @param ctx query context
   * @throws QueryException query exception
   */
  public final void checkWrite(final QueryContext ctx) throws QueryException {
    checkPerm(ctx, Perm.WRITE);
  }

  /**
   * Checks if the current user has given permissions. If negative, an
   * exception is thrown.
   * @param ctx query context
   * @param p permission
   * @throws QueryException query exception
   */
  private void checkPerm(final QueryContext ctx, final Perm p)
      throws QueryException {
    if(!ctx.context.user.has(p)) throw PERMNO.thrw(info, p);
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
