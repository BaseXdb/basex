package org.basex.query.expr;

import java.util.*;

import org.basex.query.value.node.*;
import org.basex.util.*;

/**
 * Expression information, used for debugging and logging.
 *
 * @author BaseX Team 2005-17, BSD License
 * @author Christian Gruen
 */
public abstract class ExprInfo {
  /**
   * Returns a string description of the expression. This method is only
   * called by error messages. Contrary to the {@link #toString()} method,
   * arguments are not included in the output.
   * @return result of check
   */
  public String description() {
    return Util.className(this).toLowerCase(Locale.ENGLISH);
  }

  /**
   * Returns a string representation of the expression that can be embedded in error messages.
   * Defaults to {@link #toString()}.
   * @return class name
   */
  public String toErrorString() {
    return toString();
  }

  @Override
  public abstract String toString();

  /**
   * Creates an expression tree.
   * @param e root element
   */
  public abstract void plan(FElem e);

  /**
   * Creates a new element node to be added to the expression tree.
   * @param atts optional attribute names and values
   * @return tree node
   */
  protected final FElem planElem(final Object... atts) {
    final FElem el = new FElem(Util.className(this));
    final int al = atts.length;
    for(int a = 0; a < al - 1; a += 2) {
      if(atts[a + 1] != null) el.add(planAttr(atts[a], atts[a + 1]));
    }
    return el;
  }

  /**
   * Adds trees of the specified expressions to the root node.
   * @param plan root node
   * @param el new element
   * @param exprs expressions
   */
  protected static final void addPlan(final FElem plan, final FElem el, final Object... exprs) {
    plan.add(el);
    for(final Object expr : exprs) {
      if(expr instanceof ExprInfo) {
        ((ExprInfo) expr).plan(el);
      } else if(expr instanceof ExprInfo[]) {
        for(final ExprInfo ex : (ExprInfo[]) expr) {
          if(ex != null) ex.plan(el);
        }
      } else if(expr instanceof byte[]) {
        el.add((byte[]) expr);
      } else if(expr != null) {
        el.add(expr.toString());
      }
    }
  }

  /**
   * Adds trees of the specified expressions to the root node.
   * @param plan root node
   * @param el new element
   * @param expr expressions
   */
  protected static final void addPlan(final FElem plan, final FElem el, final ExprInfo... expr) {
    addPlan(plan, el, (Object) expr);
  }

  /**
   * Creates a new attribute to be added to the expression tree.
   * @param name name of attribute
   * @param value value of attribute
   * @return tree node
   */
  protected static FAttr planAttr(final Object name, final Object value) {
    return new FAttr(Util.inf(name), Util.inf(value));
  }
}
