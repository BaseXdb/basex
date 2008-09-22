package org.basex.query.xpath.func;

import static org.basex.query.xpath.XPText.*;
import java.io.IOException;
import org.basex.core.proc.Check;
import org.basex.query.QueryException;
import org.basex.query.xpath.XPContext;
import org.basex.query.xpath.expr.Expr;
import org.basex.query.xpath.values.NodeSet;
import org.basex.util.Token;

/**
 * Constructor for the doc() and document() function.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class Doc extends Func {
  /**
   * Function Constructor.
   * @param arg expression array
   */
  public Doc(final Expr[] arg) {
    super(arg, "doc(item)");
  }

  @Override
  public NodeSet eval(final XPContext ctx) throws QueryException {
    final String db = Token.string(evalArgs(ctx)[0].str());
    try {
      ctx.local = new NodeSet(Check.check(db));
      return ctx.local;
    } catch(final IOException ex) {
      throw new QueryException(UNKNOWNDOC, db);
    }
  }

  @Override
  public boolean checkArguments() {
    return args.length == 1;
  }
}
