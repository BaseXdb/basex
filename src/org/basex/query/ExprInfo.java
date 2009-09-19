package org.basex.query;

import java.io.IOException;
import org.basex.core.Main;
import org.basex.data.Serializer;

/**
 * Expression information.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public abstract class ExprInfo {

  /**
   * Returns a color string for the expression.
   * @return color
   */
  public String color() {
    return null;
  }

  /**
   * Returns a string description of the expression. Contrary to the
   * {@link #toString()} method, arguments are not included
   * in the output.
   * @return result of check
   */
  public String info() {
    return name() + " expression";
  }

  /**
   * Returns the simplified class name.
   * @return class name
   */
  public String name() {
    return Main.name(this);
  }

  /**
   * Recursively sends the abstract syntax of this expression to the
   * specified serializer.
   * @param ser serializer
   * @throws IOException I/O exception
   */
  public abstract void plan(Serializer ser) throws IOException;

  @Override
  public abstract String toString();
}
