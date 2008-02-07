package org.basex.query.xpath.func;

import static org.basex.query.xpath.XPText.*;
import org.basex.data.Data;
import org.basex.query.QueryException;
import org.basex.query.xpath.XPContext;
import org.basex.query.xpath.expr.Expr;
import org.basex.query.xpath.values.NodeBuilder;
import org.basex.query.xpath.values.NodeSet;
import org.basex.query.xpath.values.Item;
import org.basex.util.Token;

/**
 * Constructor for implementation  specific nodes() function; returns
 * a node set with the pre nodes specified in the argument literal.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class Nodes extends Func {
  /**
   * Function Constructor.
   * @param arg expression array
   */
  public Nodes(final Expr[] arg) {
    super(arg, "nodes(item)");
  }

  @Override
  public NodeSet eval(final XPContext ctx) throws QueryException {
    final Data data = ctx.local.data;
    final int size = data.size;
    final NodeBuilder tmp = new NodeBuilder();

    final Item arg = evalArgs(ctx)[0];
    for(final byte[] v : Token.split(arg.str(), ' ')) {
      final int pre = Token.toInt(v);
      if(pre < 0 || pre >= size) throw new QueryException(INVALIDPRE, v);
      tmp.add(pre);
    }
    return new NodeSet(tmp.finish(), ctx);
  }

  @Override
  public boolean checkArguments() {
    return args.length == 1;
  }
}
