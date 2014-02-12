package org.basex.query.func;

import static org.basex.query.func.Function.*;
import static org.basex.query.util.Err.*;
import static org.basex.util.Token.*;

import org.basex.core.*;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.iter.*;
import org.basex.query.util.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Accessor functions.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Christian Gruen
 */
public final class FNAcc extends StandardFunc {
  /**
   * Constructor.
   * @param sctx static context
   * @param ii input info
   * @param f function definition
   * @param e arguments
   */
  public FNAcc(final StaticContext sctx, final InputInfo ii, final Function f, final Expr... e) {
    super(sctx, ii, f, e);
  }

  @Override
  public Item item(final QueryContext ctx, final InputInfo ii) throws QueryException {
    final Expr e = expr.length == 0 ? checkCtx(ctx) : expr[0];
    switch(sig) {
      case POSITION:
        return Int.get(ctx.pos);
      case LAST:
        return Int.get(ctx.size);
      case STRING:
        return string(e, ii, ctx);
      case NUMBER:
        return number(ctx.iter(e), ctx);
      case STRING_LENGTH:
        return Int.get(len(checkEStr(expr.length == 0 ? string(e, ii, ctx) : e, ctx)));
      case NORMALIZE_SPACE:
        return Str.get(norm(checkEStr(e, ctx)));
      case NAMESPACE_URI_FROM_QNAME:
        final Item it = e.item(ctx, info);
        return it == null ? null : Uri.uri(checkQNm(it, ctx, sc).uri());
      default:
        return super.item(ctx, ii);
    }
  }

  /**
   * Converts the specified item to a string.
   * @param e expression
   * @param ii input info
   * @param ctx query context
   * @return double iterator
   * @throws QueryException query exception
   */
  private Item string(final Expr e, final InputInfo ii, final QueryContext ctx)
      throws QueryException {

    final Item it = e.item(ctx, info);
    if(it == null) return Str.ZERO;
    if(it instanceof FItem) throw FISTR.get(ii, it.type);
    return it.type == AtomType.STR ? it : Str.get(it.string(ii));
  }

  /**
   * Converts the specified item to a double.
   * @param ir iterator
   * @param ctx query context
   * @return double iterator
   * @throws QueryException query exception
   */
  private Item number(final Iter ir, final QueryContext ctx) throws QueryException {
    final Item it = ir.next();
    if(it == null || ir.next() != null) return Dbl.NAN;
    if(it instanceof FItem) throw FIATOM.get(info, it.type);
    try {
      return it.type == AtomType.DBL ? it : AtomType.DBL.cast(it, ctx, sc, info);
    } catch(final QueryException ex) {
      return Dbl.NAN;
    }
  }

  @Override
  public boolean has(final Flag flag) {
    return flag == Flag.CTX && expr.length == 0 || super.has(flag);
  }

  @Override
  public boolean accept(final ASTVisitor visitor) {
    return !(!oneOf(sig, POSITION, LAST) && expr.length == 0 && !visitor.lock(DBLocking.CTX)) &&
      super.accept(visitor);
  }
}
