package org.basex.query.expr;

import static org.basex.query.QueryText.*;

import org.basex.data.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Expression information, used for debugging and logging.
 *
 * @author BaseX Team 2005-19, BSD License
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
    final TokenBuilder tb = new TokenBuilder();
    boolean sep = false;
    for(final byte b : Token.token(Util.className(this))) {
      if(Character.isLowerCase(b)) {
        sep = true;
      } else if(sep) {
        tb.add(' ');
      }
      tb.add(Character.toLowerCase(b));
    }
    return tb.toString();
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
  protected FElem planElem(final Object... atts) {
    final FElem elem = new FElem(Util.className(this));
    final int al = atts.length;
    for(int a = 0; a < al - 1; a += 2) {
      if(atts[a + 1] != null) elem.add(planAttr(atts[a], atts[a + 1]));
    }
    return elem;
  }

  /**
   * Adds trees of the specified expressions to the root node.
   * @param plan root node
   * @param child new element
   * @param exprs expressions ({@code null} references will be ignored)
   * @return child element
   */
  protected static FElem addPlan(final FElem plan, final FElem child, final Object... exprs) {
    plan.add(child);
    for(final Object expr : exprs) {
      if(expr instanceof ExprInfo) {
        ((ExprInfo) expr).plan(child);
      } else if(expr instanceof ExprInfo[]) {
        for(final ExprInfo ex : (ExprInfo[]) expr) {
          if(ex != null) ex.plan(child);
        }
      } else if(expr instanceof byte[]) {
        child.add((byte[]) expr);
      } else if(expr != null) {
        child.add(expr.toString());
      }
    }
    return child;
  }

  /**
   * Adds trees of the specified expressions to the root node.
   * @param plan root node
   * @param child new element
   * @param expr expressions ({@code null} references will be ignored)
   * @return child element
   */
  protected static FElem addPlan(final FElem plan, final FElem child, final ExprInfo... expr) {
    return addPlan(plan, child, (Object[]) expr);
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

  /**
   * Returns the query plan attribute.
   * @param elem element to which attributes will be added
   * @param seqType sequence type
   * @param size result size
   * @param data data reference (can be {@code null})
   * @return specified element
   */
  protected FElem addPlanType(final FElem elem, final SeqType seqType, final long size,
      final Data data) {
    if(!seqType.eq(SeqType.ITEM_ZM)) elem.add(planAttr(TYPE, seqType));
    if(size != -1) elem.add(planAttr(SIZE, size));
    if(data != null) elem.add(planAttr(DATABASE, data.meta.name));
    return elem;
  }
}
