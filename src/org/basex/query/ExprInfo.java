package org.basex.query;

import java.io.IOException;
import org.basex.data.Serializer;

/**
 * Expression Information.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public abstract class ExprInfo {
  /**
   * Returns a string description of the expression. Contrary to the
   * {@link #toString()} method, the current expressions aren't included
   * in the output.
   * @return result of check
   */
  public String color() {
    return null;
  }

  /**
   * Returns a string description of the expression. Contrary to the
   * {@link #toString()} method, the current expressions aren't included
   * in the output.
   * @return result of check
   */
  public String info() {
    return name() + " expression";
  }

  /**
   * Returns the simplified class name (for debugging).
   * @return class name
   */
  public String name() {
    return getClass().getSimpleName();
  }
  
  /**
   * Recursively sends the abstract syntax of this expression to the
   * specified serializer.
   * @param ser serializer
   * @throws IOException exception
   */
  public abstract void plan(Serializer ser) throws IOException;

  @Override
  public abstract String toString();
}
