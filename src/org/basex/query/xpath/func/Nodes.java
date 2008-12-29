package org.basex.query.xpath.func;

import static org.basex.Text.*;
import static org.basex.query.xpath.XPText.*;
import org.basex.data.Data;
import org.basex.query.QueryException;
import org.basex.query.xpath.XPContext;
import org.basex.query.xpath.expr.Expr;
import org.basex.query.xpath.item.Item;
import org.basex.query.xpath.item.NodeBuilder;
import org.basex.query.xpath.item.Nod;
import org.basex.util.Token;

/**
 * Constructor for implementation  specific nodes() function; returns
 * a node set with the pre nodes specified in the argument literal.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class Nodes extends Func {
  /** Name of function. */
  public static final String NAME = NAMESPACE + ":nodes";

  /**
   * Function Constructor.
   * @param arg expression array
   */
  public Nodes(final Expr[] arg) {
    super(arg, NAME + "(item)");
  }

  @Override
  public Nod eval(final XPContext ctx) throws QueryException {
    final Data data = ctx.item.data;
    final int size = data.meta.size;
    final NodeBuilder tmp = new NodeBuilder();

    final Item arg = evalArgs(ctx)[0];
    for(final byte[] v : Token.split(arg.str(), ' ')) {
      final int pre = Token.toInt(v);
      if(pre < 0 || pre >= size) throw new QueryException(INVALIDPRE, v);
      tmp.add(pre);
    }
    return new Nod(tmp.finish(), ctx);
  }

  @Override
  public boolean checkArguments() {
    return args.length == 1;
  }
}
