package org.basex.query.xpath.func;

import org.basex.query.QueryException;
import org.basex.query.xpath.XPContext;
import org.basex.query.xpath.expr.Expr;
import org.basex.query.xpath.values.NodeBuilder;
import org.basex.query.xpath.values.NodeSet;
import org.basex.util.Token;

/**
 * Constructor for the implementation specific distinct() function.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class Distinct extends Func {
  /**
   * Function Constructor.
   * @param arg expression array
   */
  public Distinct(final Expr[] arg) {
    super(arg, "distinct(nodeset)");
  }

  @Override
  public NodeSet eval(final XPContext ctx) 
      throws QueryException {
    
    final NodeSet local = ctx.item;
    final int[] n = ((NodeSet) evalArgs(ctx)[0]).nodes;
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
    return new NodeSet(tmp.finish(), ctx);
  }

  @Override
  public boolean checkArguments() {
    return args.length == 1 && args[0].returnedValue() == NodeSet.class;
  }
}
