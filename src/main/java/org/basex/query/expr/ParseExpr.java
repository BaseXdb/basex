package org.basex.query.expr;

import static org.basex.query.QueryText.*;
import static org.basex.query.util.Err.*;
import static org.basex.util.Token.*;

import org.basex.core.User;
import org.basex.io.IO;
import org.basex.io.IOFile;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.QueryText;
import org.basex.query.func.Function;
import org.basex.query.item.ANode;
import org.basex.query.item.AtomType;
import org.basex.query.item.Bln;
import org.basex.query.item.DBNode;
import org.basex.query.item.Empty;
import org.basex.query.item.Item;
import org.basex.query.item.NodeType;
import org.basex.query.item.SeqType;
import org.basex.query.item.Type;
import org.basex.query.item.Uri;
import org.basex.query.item.Value;
import org.basex.query.item.map.Map;
import org.basex.query.iter.Iter;
import org.basex.query.util.Err;
import org.basex.util.InputInfo;

/**
 * Abstract parse expression. All non-value expressions are derived from
 * this class.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public abstract class ParseExpr extends Expr {
  /** Input information. */
  public final InputInfo input;
  /** Cardinality of result; unknown if set to -1. */
  public long size = -1;
  /** Data type. */
  public SeqType type;

  /**
   * Constructor.
   * @param ii input info
   */
  public ParseExpr(final InputInfo ii) {
    input = ii;
  }

  @Override
  public Iter iter(final QueryContext ctx) throws QueryException {
    final Item it = item(ctx, input);
    return it != null ? it.iter() : Empty.ITER;
  }

  @Override
  public Item item(final QueryContext ctx, final InputInfo ii)
      throws QueryException {

    final Iter ir = iter(ctx);
    final Item it = ir.next();
    if(it == null || ir.size() == 1) return it;
    final Item n = ir.next();
    if(n != null) XPSEQ.thrw(ii, this);
    return it;
  }

  @Override
  public Value value(final QueryContext ctx) throws QueryException {
    if(type().zeroOrOne()) {
      final Value v = item(ctx, input);
      return v == null ? Empty.SEQ : v;
    }
    return ctx.iter(this).value();
  }

  @Override
  public final Item ebv(final QueryContext ctx, final InputInfo ii)
      throws QueryException {

    Item it = null;
    if(type().zeroOrOne()) {
      it = item(ctx, input);
    } else {
      final Iter ir = iter(ctx);
      it = ir.next();
      if(it != null && !it.type.isNode() && ir.next() != null)
        CONDTYPE.thrw(input, this);
    }
    return it == null ? Bln.FALSE : it;
  }

  @Override
  public final Item test(final QueryContext ctx, final InputInfo ii)
      throws QueryException {

    final Item it = ebv(ctx, input);
    return (it.type.isNumber() ? it.dbl(input) == ctx.pos :
      it.bool(input)) ? it : null;
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
    return optPre(item(ctx, input), ctx);
  }

  /**
   * Adds an optimization info for pre-evaluating the specified expression.
   * @param opt optimized expression
   * @param ctx query context
   * @return optimized expression
   */
  protected final Expr optPre(final Expr opt, final QueryContext ctx) {
    if(opt != this) ctx.compInfo(QueryText.OPTPRE, this);
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
    return e.type().eq(SeqType.BLN) ? e : Function.BOOLEAN.get(input, e);
  }

  // VALIDITY CHECKS ==========================================================

  /**
   * Checks if the specified expressions is no updating expression.
   * @param e expression
   * @param ctx query context
   * @return the specified expression
   * @throws QueryException query exception
   */
  public final Expr checkUp(final Expr e, final QueryContext ctx)
      throws QueryException {
    if(e != null && ctx.updating && e.uses(Use.UPD))
      UPNOT.thrw(input, description());
    return e;
  }

  /**
   * Tests if the specified expressions are updating or vacuous.
   * @param ctx query context
   * @param expr expression array
   * @throws QueryException query exception
   */
  public void checkUp(final QueryContext ctx, final Expr... expr)
      throws QueryException {

    if(!ctx.updating) return;
    int s = 0;
    for(final Expr e : expr) {
      if(e.isVacuous()) continue;
      final boolean u = e.uses(Use.UPD);
      if(u && s == 2 || !u && s == 1) UPNOT.thrw(input, description());
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

    final Item it = checkNoEmpty(e.item(ctx, input), AtomType.BLN);
    final Type ip = it.type;
    if(!ip.isUntyped() && ip != AtomType.BLN) Err.type(this, AtomType.BLN, it);
    return it.bool(input);
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

    final Item it = checkNoEmpty(e.item(ctx, input), AtomType.DBL);
    final Type ip = it.type;
    if(!ip.isUntyped() && !ip.isNumber()) Err.number(this, it);
    return it.dbl(input);
  }

  /**
   * Checks if the specified expression is an integer.
   * Returns a token representation or an exception.
   * @param e expression to be checked
   * @param ctx query context
   * @return integer value
   * @throws QueryException query exception
   */
  public final long checkItr(final Expr e, final QueryContext ctx)
      throws QueryException {
    return checkItr(checkNoEmpty(e.item(ctx, input), AtomType.ITR));
  }

  /**
   * Checks if the specified item is no empty sequence.
   * @param it item to be checked
   * @param t expected type
   * @return specified item
   * @throws QueryException query exception
   */
  private Item checkNoEmpty(final Item it, final Type t)
      throws QueryException {
    if(it == null) XPEMPTYPE.thrw(input, description(), t);
    return it;
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
    return it.itr(input);
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
    if(!(it instanceof DBNode)) NODBCTX.thrw(input, this);
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
    if(uri.isAbsolute() || !eq(ctx.sc.baseURI().resolve(uri).string(),
        QueryText.URLCOLL)) IMPLCOL.thrw(input, e);
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
    return it.string(input);
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
    return it.string(input);
  }

  /**
   * Throws an exception if the context item is not set.
   * @param ctx query context
   * @return result of check
   * @throws QueryException query exception
   */
  public final Value checkCtx(final QueryContext ctx) throws QueryException {
    final Value v = ctx.value;
    if(v == null) XPNOCTX.thrw(input, this);
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
    return checkEmpty(e.item(ctx, input));
  }

  /**
   * Checks if the specified expression is an empty sequence; if yes, throws
   * an exception.
   * @param it item to be checked
   * @param t type to be checked
   * @return specified item
   * @throws QueryException query exception
   */
  public final Item checkType(final Item it, final Type t)
      throws QueryException {

    if(!checkEmpty(it).type.instanceOf(t)) Err.type(this, t, it);
    return it;
  }

  /**
   * Checks if the specified item is an empty sequence; if yes, throws
   * an exception.
   * @param it item to be checked
   * @return specified item
   * @throws QueryException query exception
   */
  public final Item checkEmpty(final Item it) throws QueryException {
    if(it == null) XPEMPTY.thrw(input, description());
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
    return checkEStr(e.item(ctx, input));
  }

  /**
   * Checks if an expression yields a valid and existing {@link IO} instance.
   * Returns the instance or an exception.
   * @param e expression to be evaluated
   * @param ctx query context
   * @return io instance
   * @throws QueryException query exception
   */
  public final IO checkIO(final Expr e, final QueryContext ctx)
      throws QueryException {

    checkAdmin(ctx);
    final String name = string(checkStr(e, ctx));
    IO io = IO.get(name);
    if(!io.exists()) {
      final IO iob = ctx.sc.baseIO();
      if(iob != null) {
        io = new IOFile(iob.path(), name);
        if(!io.exists()) RESFNF.thrw(input, name);
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
  public final void checkAdmin(final QueryContext ctx) throws QueryException {
    checkPerm(ctx, User.ADMIN);
  }

  /**
   * Checks if the current user has write permissions. If negative, an
   * exception is thrown.
   * @param ctx query context
   * @throws QueryException query exception
   */
  public final void checkWrite(final QueryContext ctx) throws QueryException {
    checkPerm(ctx, User.WRITE);
  }

  /**
   * Checks if the current user has given permissions. If negative, an
   * exception is thrown.
   * @param ctx query context
   * @param p permission
   * @throws QueryException query exception
   */
  private void checkPerm(final QueryContext ctx, final byte p)
      throws QueryException {
    if(!ctx.context.user.perm(p)) PERMNO.thrw(input, p);
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
