package org.basex.query.xpath.func;

import org.basex.data.Data;
import org.basex.query.QueryException;
import org.basex.query.xpath.XPContext;
import org.basex.query.xpath.expr.Expr;
import org.basex.query.xpath.values.Literal;
import org.basex.query.xpath.values.NodeSet;
import org.basex.query.xpath.values.Item;
import org.basex.util.Token;

/**
 * Constructor for local-name() function.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Tim Petrowsky
 */
public final class LocalName extends Func {
  /**
   * Function Constructor.
   * @param arg expression array
   */
  public LocalName(final Expr[] arg) {
    super(arg, "local-name(nodeset?)");
  }

  @Override
  public Literal eval(final XPContext ctx) throws QueryException {
    final Item[] v = evalArgs(ctx);
    final NodeSet set = v.length != 0 && v[0].size() != 0 ?
        (NodeSet) v[0] : ctx.item;

    final Data data = ctx.item.data;
    final int node = set.nodes[0];
    final int kind = data.kind(node);
    if(kind == Data.ELEM) return new Literal(data.tag(node));
    if(kind == Data.ATTR) return new Literal(data.attName(node));
    return new Literal(Token.EMPTY);
  }

  @Override
  public boolean checkArguments() {
    return args.length == 0 || args.length == 1 &&
        args[0].returnedValue() == NodeSet.class;
  }
}
