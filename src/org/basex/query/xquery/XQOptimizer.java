package org.basex.query.xquery;

import org.basex.query.xpath.XPText;
import org.basex.query.xquery.expr.Expr;
import org.basex.query.xquery.item.DNode;
import org.basex.query.xquery.item.Type;
import org.basex.query.xquery.path.Axis;
import org.basex.query.xquery.path.KindTest;
import org.basex.query.xquery.path.Path;
import org.basex.query.xquery.path.Step;
import org.basex.query.xquery.path.Test;



/**
 * This class assembles optimization methods for the XQuery parser.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class XQOptimizer {
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
    if (step.expr != null && step.expr.length > 0) return ex;
    
    if((step.axis == Axis.DESC ||
        step.axis == Axis.SELF ||
        step.axis == Axis.DESCORSELF ||
        step.axis == Axis.CHILD) &&
        step.test.kind  != Test.Kind.NAME &&
        step.test.type != Type.TXT &&
        ((DNode) ctx.item).data.tags.uptodate) {
      final Step s = new Step(Axis.CHILD, 
          new KindTest(Type.TXT), new Expr[]{});
      final Path p = new Path(ex, new Expr[]{s});
      ctx.compInfo(XPText.OPTTEXT);
      return p;
    }
    return ex;
  }
}
