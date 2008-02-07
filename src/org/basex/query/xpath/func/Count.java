package org.basex.query.xpath.func;

import org.basex.query.QueryException;
import org.basex.query.xpath.XPContext;
import org.basex.query.xpath.expr.Expr;
import org.basex.query.xpath.locpath.Axis;
import org.basex.query.xpath.locpath.LocPathAbs;
import org.basex.query.xpath.locpath.Step;
import org.basex.query.xpath.locpath.Steps;
import org.basex.query.xpath.values.NodeSet;
import org.basex.query.xpath.values.Num;
import org.basex.query.xpath.values.Item;
import static org.basex.query.xpath.XPText.*;

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
  public Num eval(final XPContext ctx) throws QueryException {
    final Item[] v = evalArgs(ctx);
    return v[0] instanceof NodeSet ?
        new Num(((NodeSet) v[0]).nodes.length) : Num.ONE;
  }

  @Override
  public boolean checkArguments() {
    return args.length == 1;
  }

  @Override
  public Expr compile(final XPContext ctx) throws QueryException {
    super.compile(ctx);
    if(args[0] instanceof LocPathAbs) {
      final LocPathAbs path = (LocPathAbs) args[0];
      final Steps steps = path.steps;
      if(steps.size() == 1) {
        final Step step = steps.get(0);
        final int nameID = step.simpleName(Axis.DESC, true);
        if(nameID != Integer.MIN_VALUE) {
          ctx.compInfo(OPTFUNC, desc);
          return new Num(ctx.local.data.nrTags(nameID));
        }
      } else if(steps.size() == 2) {
        Step step = steps.get(0);
        final int name1 = step.simpleName(Axis.CHILD, true);
        if(name1 != Integer.MIN_VALUE) {
          step = steps.get(1);
          final int name2 = step.simpleName(Axis.DESC, true);
          if(name2 != Integer.MIN_VALUE) {
            ctx.compInfo(OPTFUNC, desc);
            return new Num(ctx.local.data.nrTags(name2) -
                (name1 == name2 ? 1 : 0));
          }
        }
      }
    }
    if(args[0] instanceof Item) {
      ctx.compInfo(OPTFUNC, desc);
      return args[0] instanceof NodeSet ?
          new Num(((NodeSet) args[0]).nodes.length) : Num.ONE;
    }
    return this;
  }
}
