package org.basex.query.xpath;

import static org.basex.query.xpath.XPText.OPTTEXT;
import org.basex.query.xpath.expr.Expr;
import org.basex.query.xpath.expr.Path;
import org.basex.query.xpath.locpath.Axis;
import org.basex.query.xpath.locpath.LocPath;
import org.basex.query.xpath.locpath.Step;
import org.basex.query.xpath.locpath.TestName;
import org.basex.query.xpath.locpath.TestNode;

/**
 * This class assembles optimization methods for the XPath parser.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class XPOptimizer {
  /** Private constructor, preventing class instantiation. */
  private XPOptimizer() { }
  
  /**
   * Checks if all path expressions are the same; if yes, a path has to be
   * only evaluated once.
   * @param ex expression array
   * @return result of check
   */
  public static Expr[] getIndexExpr(final Expr[] ex) {
    if(!(ex[0] instanceof Path)) return null;
    final Path pe = (Path) ex[0];
    
    final Expr[] tmp = new Expr[ex.length];
    tmp[0] = pe.expr1;
    
    for(int e = 1; e != ex.length; e++) {
      if(!(ex[e] instanceof Path)) return null;
      final Path pe2 = (Path) ex[e];
      if(!pe.expr2.sameAs(pe2.expr2)) return null;
      tmp[e] = pe2.expr1;
    }
    return tmp;
  }

  /**
   * Adds a text step to the specified path.
   * @param ex location path expression
   * @param ctx query context
   */
  public static void addText(final Expr ex, final XPContext ctx) {
    if(!(ex instanceof LocPath)) return;

    final LocPath path = (LocPath) ex;
    final int size = path.steps.size();
    if(size == 0) return;

    final Step step = path.steps.last();
    if((step.axis == Axis.DESC ||
        step.axis == Axis.SELF ||
        step.axis == Axis.DESCORSELF ||
        step.axis == Axis.CHILD) &&
        step.test instanceof TestName &&
        ((TestName) step.test).id != TestName.ALL &&
        !ctx.local.data.noLeaf(((TestName) step.test).id)) {
      path.steps.add(Axis.create(Axis.CHILD, TestNode.TEXT));
      ctx.compInfo(OPTTEXT);
    }
  }
}
