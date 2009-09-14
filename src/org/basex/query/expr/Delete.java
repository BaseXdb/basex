package org.basex.query.expr;

import static org.basex.query.QueryTokens.*;
import static org.basex.util.Token.*;

import java.io.IOException;

import org.basex.data.Serializer;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.item.Item;
import org.basex.query.item.Nod;
import org.basex.query.iter.Iter;
import org.basex.query.up.DeletePrimitive;

/**
 * Delete expression.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Lukas Kircher
 */
public final class Delete extends Expr {
  /** Expression list. */
  private Expr expr;

  /**
   * Constructor.
   * @param r return expression
   */
  public Delete(final Expr r) {
    expr = r;
  }

  @Override
  public Expr comp(final QueryContext ctx) throws QueryException {
    expr = expr.comp(ctx);
    return this;
  }
  
  @Override
  public Iter iter(final QueryContext ctx) throws QueryException {
    final Iter it = expr.iter(ctx);
    Item i;
    while((i = it.next()) != null)
      if(i instanceof Nod)
        ctx.updates.addPrimitive(new DeletePrimitive((Nod) i)); 
    return Iter.EMPTY;
  }

  @Override
  public void plan(final Serializer ser) throws IOException {
    ser.openElement(token(DELETE));
    ser.closeElement();
  }

  @Override
  public String toString() {
    return DELETE + NODES + expr;
  }

  @Override
  public boolean uses(final Use u, final QueryContext ctx) {
    return false;
  }
}
