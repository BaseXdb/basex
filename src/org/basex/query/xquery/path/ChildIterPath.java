package org.basex.query.xquery.path;

import static org.basex.query.xquery.XQText.*;

import org.basex.query.xquery.XQContext;
import org.basex.query.xquery.XQException;
import org.basex.query.xquery.expr.Expr;
import org.basex.query.xquery.item.Item;
import org.basex.query.xquery.iter.Iter;
import org.basex.query.xquery.util.Err;

/**
 * Iterative path expression for only child steps.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Dennis Stratmann
 */
public final class ChildIterPath extends AxisPath {
  /**
   * Constructor.
   * @param r root expression
   * @param s location steps
   */
  public ChildIterPath(final Expr r, final Step[] s) {
    super(r, s);
  }

  @Override
  public Iter iter(final XQContext ctx) {
    return new Iter() {
      final Item c = ctx.item;
      final int cs = ctx.size;
      final int cp = ctx.pos;
      boolean firstTime = true;
      Iter iter;
      int stepCount = 0;
      Item it;

      @Override
      public Item next() throws XQException  {
        while(true) {
          // first call of method
          if(firstTime) {
            it = root != null ? ctx.iter(root).finish() : ctx.item;
            ctx.item = it;            
            firstTime = false;
          }

          // if it is a new step
          if(iter == null) {
            iter = ctx.iter(step[stepCount]);
            ctx.size = it.size();
            ctx.pos = 1;
          }

          final Item i = iter.next();
          if(i == null) {
            // if it is the end of all steps
            if (stepCount == step.length - 1) {
              ctx.item = c;
              ctx.size = cs;
              ctx.pos = cp;
              return null;
            }
            // prepare for next step
            stepCount++;
            iter = null;
          } else {
            if(!i.node()) Err.or(NODESPATH, this, i.type);
            // dosen't check yet if results are only nodes or only atomic values
            ctx.item = i;
            ctx.pos++;
            return i;
          }
        }
      }
    };
  }
}