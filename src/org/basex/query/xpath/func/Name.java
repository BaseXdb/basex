package org.basex.query.xpath.func;

import org.basex.data.Data;
import org.basex.query.QueryException;
import org.basex.query.xpath.XPContext;
import org.basex.query.xpath.expr.Expr;
import org.basex.query.xpath.item.Item;
import org.basex.query.xpath.item.Str;
import org.basex.query.xpath.item.Nod;
import org.basex.util.Token;

/**
 * Constructor for name() function.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Tim Petrowsky
 */
public final class Name extends Func {
  /**
   * Function Constructor.
   * @param arg expression array
   */
  public Name(final Expr[] arg) {
    super(arg, "name(nodeset?)");
  }

  @Override
  public Str eval(final XPContext ctx) throws QueryException {
    final Item[] v = evalArgs(ctx);
    final Nod set = v.length != 0 && v[0].size() != 0 ?
        (Nod) v[0] : ctx.item;

    final Data data = ctx.item.data;
    final int node = set.nodes[0];
    final int kind = data.kind(node);
    if(kind == Data.ELEM) return new Str(data.tag(node));
    if(kind == Data.ATTR) return new Str(data.attName(node));
    return new Str(Token.EMPTY);
  }

  @Override
  public boolean checkArguments() {
    return args.length == 0 || args.length == 1 &&
      args[0].returnedValue() == Nod.class;
  }
}
