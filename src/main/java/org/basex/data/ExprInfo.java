package org.basex.data;

import java.io.IOException;
import org.basex.util.Util;

/**
 * Expression information.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public abstract class ExprInfo {
  /**
   * Returns a string description of the expression. Contrary to the
   * {@link #toString()} method, arguments are not included
   * in the output.
   * @return result of check
   */
  public String desc() {
    return name() + " expression";
  }

  /**
   * Returns the simplified class name.
   * @return class name
   */
  public String name() {
    return Util.name(this);
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
