package org.basex.query.xquery;

import org.basex.query.xquery.expr.Expr;
import org.basex.query.xquery.item.DBNode;
import org.basex.query.xquery.item.Type;
import org.basex.query.xquery.path.Axis;
import org.basex.query.xquery.path.AxisPath;
import org.basex.query.xquery.path.KindTest;
import org.basex.query.xquery.path.Step;
import org.basex.query.xquery.path.Test;

/**
 * This class assembles optimization methods for the XQuery parser.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class XQOptimizer {
  /** Flag for optimizing AndOr expression.*/
  public static boolean optAndOr = false;
  /** Private constructor, preventing class instantiation. */
  private XQOptimizer() { }

  /**
   * Adds a text step to the specified path.
   * @param ex location path expression
   * @param ctx query context
   * @return Expression
   */
  public static Expr addText(final Expr ex, final XQContext ctx) {
    if(!(ex instanceof Step)) return ex;
    final Step step = (Step) ex;
    if(step.pred != null && step.pred.length > 0) return ex;
    
    if(1 == 1) return ex;

    /* [SG] should be revised; not all necessary tests are included,
     *   ctx.item can refer to another database, ... */
    if(step.axis.down && step.test.kind != Test.Kind.NAME &&
        step.test.type != Type.TXT && ((DBNode) ctx.item).data.tags.uptodate) {

      final Step s = Step.get(Axis.CHILD, new KindTest(Type.TXT));
      return new AxisPath(ex, new Step[] { s });
    }
    return ex;
  }
}
