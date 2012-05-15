package org.basex.data;

import org.basex.query.item.*;
import org.basex.util.*;

/**
 * Expression information, used for debugging and logging.
 *
 * @author BaseX Team 2005-12, BSD License
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
    return Token.string(info()) + " expression";
  }

  /**
   * Returns the simplified class name.
   * @return class name
   */
  public byte[] info() {
    return Token.token(Util.name(this));
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
  protected FElem planElem(final Object... atts) {
    final FElem el = new FElem(info());
    for(int a = 0; a < atts.length - 1; a += 2) {
      if(atts[a + 1] != null) el.add(planAttr(atts[a], atts[a + 1]));
    }
    return el;
  }

  /**
   * Adds trees of the specified expressions to the root node.
   * @param plan root node
   * @param el new element
   * @param expr expressions
   */
  protected void addPlan(final FElem plan, final FElem el, final Object... expr) {
    plan.add(el);
    for(final Object o : expr) {
      if(o instanceof ExprInfo) {
        ((ExprInfo) o).plan(el);
      } else if(o instanceof ExprInfo[]) {
        for(final ExprInfo e : (ExprInfo[]) o) {
          if(e != null) e.plan(el);
        }
      } else if(o instanceof byte[]) {
        el.add((byte[]) o);
      } else if(o != null) {
        el.add(Token.token(o.toString()));
      }
    }
  }

  /**
   * Adds trees of the specified expressions to the root node.
   * @param plan root node
   * @param el new element
   * @param expr expressions
   */
  protected void addPlan(final FElem plan, final FElem el, final ExprInfo... expr) {
    addPlan(plan, el, (Object) expr);
  }

  /**
   * Creates a new attribute to be added to the expression tree.
   * @param n name of attribute
   * @param v value of attribute
   * @return tree node
   */
  protected FAttr planAttr(final Object n, final Object v) {
    return new FAttr(Util.inf(n), Util.inf(v));
  }
}
