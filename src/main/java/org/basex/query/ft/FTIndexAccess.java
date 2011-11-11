package org.basex.query.ft;

import static org.basex.query.QueryText.*;
import static org.basex.util.Token.*;
import java.io.IOException;

import org.basex.io.serial.Serializer;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.expr.Expr;
import org.basex.query.expr.Simple;
import org.basex.query.func.Function;
import org.basex.query.item.FTNode;
import org.basex.query.item.ANode;
import org.basex.query.item.Str;
import org.basex.query.iter.FTIter;
import org.basex.query.iter.NodeIter;
import org.basex.query.util.IndexContext;
import org.basex.query.util.Var;
import org.basex.util.InputInfo;

/**
 * FTContains expression with index access.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public final class FTIndexAccess extends Simple {
  /** Full-text expression. */
  private final FTExpr ftexpr;
  /** Index context. */
  private final IndexContext ictx;

  /**
   * Constructor.
   * @param ii input info
   * @param ex contains, select and optional ignore expression
   * @param ic index context
   */
  public FTIndexAccess(final InputInfo ii, final FTExpr ex,
      final IndexContext ic) {

    super(ii);
    ftexpr = ex;
    ictx = ic;
  }

  @Override
  public NodeIter iter(final QueryContext ctx) throws QueryException {
    final FTIter ir = ftexpr.iter(ctx);

    return new NodeIter() {
      @Override
      public ANode next() throws QueryException {
        final FTNode it = ir.next();
        if(it != null) {
          // add entry to visualization
          if(ctx.ftpos != null) ctx.ftpos.add(it.data, it.pre, it.all);
          // assign scoring, if not done yet
          it.score();
          // remove matches reference to save memory
          it.all = null;
        }
        return it;
      }
    };
  }

  @Override
  public boolean uses(final Use u) {
    return ftexpr.uses(u);
  }

  @Override
  public int count(final Var v) {
    return ftexpr.count(v);
  }

  @Override
  public boolean removable(final Var v) {
    return ftexpr.removable(v);
  }

  @Override
  public Expr remove(final Var v) {
    ftexpr.remove(v);
    return this;
  }

  @Override
  public void plan(final Serializer ser) throws IOException {
    ser.openElement(this, DATA, token(ictx.data.meta.name));
    ftexpr.plan(ser);
    ser.closeElement();
  }

  @Override
  public boolean iterable() {
    return ictx.iterable;
  }

  @Override
  public String toString() {
    return Function._DB_FULLTEXT.get(input, Str.get(ictx.data.meta.name),
        ftexpr).toString();
  }
}
