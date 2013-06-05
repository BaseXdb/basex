package org.basex.query.expr;

import static org.basex.query.QueryText.*;

import org.basex.core.*;
import org.basex.query.*;
import org.basex.query.iter.*;
import org.basex.query.path.*;
import org.basex.query.util.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Context item.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class Context extends Simple {
  /**
   * Constructor.
   * @param ii input info
   */
  public Context(final InputInfo ii) {
    super(ii);
    type = SeqType.ITEM;
    size = 1;
  }

  @Override
  public Context compile(final QueryContext ctx, final VarScope scp) {
    if(ctx.value != null) {
      type = ctx.value.type();
      size = ctx.value.size();
    }
    return this;
  }

  @Override
  public Iter iter(final QueryContext ctx) throws QueryException {
    return checkCtx(ctx).iter();
  }

  @Override
  public Value value(final QueryContext ctx) throws QueryException {
    return checkCtx(ctx);
  }

  @Override
  public Item item(final QueryContext ctx, final InputInfo ii) throws QueryException {
    return checkCtx(ctx).item(ctx, info);
  }

  @Override
  public boolean uses(final Use u) {
    return u == Use.CTX;
  }

  @Override
  public boolean removable(final Var v) {
    return false;
  }

  @Override
  public Expr copy(final QueryContext ctx, final VarScope scp, final IntObjMap<Var> vs) {
    return copyType(new Context(info));
  }

  @Override
  public boolean accept(final ASTVisitor visitor) {
    return visitor.lock(DBLocking.CTX) && super.accept(visitor);
  }

  @Override
  public Expr addText(final QueryContext ctx) {
    // replacing context node with text() node to facilitate index rewritings
    if(!ctx.leaf) return this;
    ctx.compInfo(OPTTEXT);
    return Path.get(info, null, Step.get(info, Axis.CHILD, Test.TXT));
  }

  @Override
  public boolean sameAs(final Expr cmp) {
    return cmp instanceof Context;
  }

  @Override
  public String toString() {
    return ".";
  }
}
