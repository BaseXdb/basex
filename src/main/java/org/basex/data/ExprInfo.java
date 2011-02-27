package org.basex.data;

import java.io.IOException;
import org.basex.util.Util;

/**
 * Expression information.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public abstract class ExprInfo {
  /**
   * Returns a string description of the expression. This method is only
   * called by error messages. Contrary to the {@link #toString()} method,
   * arguments are not included in the output.
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
