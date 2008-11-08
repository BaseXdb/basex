package org.basex.query.xpath.func;

import static org.basex.Text.*;
import org.basex.query.QueryException;
import org.basex.query.xpath.XPContext;
import org.basex.query.xpath.expr.Expr;
import org.basex.query.xpath.item.NodeBuilder;
import org.basex.query.xpath.item.Nod;
import org.basex.util.Token;

/**
 * Constructor for the implementation specific distinct() function.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class Distinct extends Func {
  /** Name of function. */
  public static final String NAME = NAMESPACE + ":distinct";

  /**
   * Function Constructor.
   * @param arg expression array
   */
  public Distinct(final Expr[] arg) {
    super(arg, NAME + "(nodeset)");
  }

  @Override
  public Nod eval(final XPContext ctx) 
      throws QueryException {
    
    final Nod local = ctx.item;
    final int[] n = ((Nod) evalArgs(ctx)[0]).nodes;
    final byte[][] v = new byte[n.length][];
    for(int i = 0; i != v.length; i++) v[i] = local.data.atom(n[i]);
    
    final NodeBuilder tmp = new NodeBuilder();
    for(int i = 0; i != v.length; i++) {
      if(v[i] == null) continue;
      tmp.add(n[i]);

      for(int j = i + 1; j < v.length; j++) {
        if(v[j] != null && Token.eq(v[i], v[j])) v[j] = null;
      }
    }
    return new Nod(tmp.finish(), ctx);
  }

  @Override
  public boolean checkArguments() {
    return args.length == 1 && args[0].returnedValue() == Nod.class;
  }
}
