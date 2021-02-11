package org.basex.query.expr;

import org.basex.query.*;
import org.basex.util.*;

/**
 * Expression information, used for debugging and logging.
 *
 * @author BaseX Team 2005-21, BSD License
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
   * Creates a query plan.
   * @param plan expression plan
   */
  public abstract void plan(QueryPlan plan);

  /**
   * Creates a query string.
   * @param qs query string builder
   */
  public abstract void plan(QueryString qs);

  /**
   * Returns a string representation of the expression that can be embedded in error messages.
   * Defaults to {@link #toString()}.
   * @return class name
   */
  public String toErrorString() {
    return toString();
  }

  @Override
  public final String toString() {
    return new QueryString().token(this).toString();
  }
}
