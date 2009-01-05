package org.basex.query.xpath.func;

import static org.basex.query.xpath.XPText.*;
import org.basex.index.Names;
import org.basex.query.QueryException;
import org.basex.query.xpath.XPContext;
import org.basex.query.xpath.expr.Expr;
import org.basex.query.xpath.item.Dbl;
import org.basex.query.xpath.item.Item;
import org.basex.query.xpath.item.Nod;
import org.basex.query.xpath.path.Axis;
import org.basex.query.xpath.path.LocPathAbs;
import org.basex.query.xpath.path.Steps;

/**
 * Constructor for count() function.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Tim Petrowsky
 */
public final class Count extends Func {
  /**
   * Function Constructor.
   * @param arg expression array
   */
  public Count(final Expr[] arg) {
    super(arg, "count(item)");
  }

  @Override
  public Dbl eval(final XPContext ctx) throws QueryException {
    final Item[] v = evalArgs(ctx);
    return v[0] instanceof Nod ?
        new Dbl(((Nod) v[0]).nodes.length) : Dbl.ONE;
  }

  @Override
  public boolean checkArguments() {
    return args.length == 1;
  }

  @Override
  public Expr comp(final XPContext ctx) throws QueryException {
    super.comp(ctx);
    
    // pre-evaluate absolute location paths
    if(args[0] instanceof LocPathAbs) {
      // check if statistics are available
      if(!ctx.item.data.meta.uptodate) return this;
      
      final LocPathAbs path = (LocPathAbs) args[0];
      final Steps steps = path.steps;
      if(steps.size() > 2) return this;
      
      final Names tags = ctx.item.data.tags;
      
      int num = 0;
      if(steps.size() == 1) {
        // return number of descendant tags
        final int n1 = steps.get(0).simpleName(Axis.DESC, true);
        if(n1 == Integer.MIN_VALUE) return this;
        num = tags.stat(n1).counter;
      } else {
        // return number of descendant tags, excluding the root node
        final int n1 = steps.get(0).simpleName(Axis.CHILD, true);
        if(n1 == Integer.MIN_VALUE) return this;
        final int n2 = steps.get(1).simpleName(Axis.DESC, true);
        if(n2 == Integer.MIN_VALUE) return this;
        num = tags.stat(n2).counter - (n1 == n2 ? 1 : 0);
      }
      ctx.compInfo(OPTFUNC, desc);
      return new Dbl(num);
    }
    
    // return number of static items
    if(args[0] instanceof Item) {
      ctx.compInfo(OPTFUNC, desc);
      return args[0] instanceof Nod ?
          new Dbl(((Nod) args[0]).nodes.length) : Dbl.ONE;
    }
    return this;
  }
}
