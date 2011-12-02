package org.basex.data;

import java.io.IOException;

import org.basex.io.serial.Serializer;
import org.basex.util.Util;

/**
 * Expression information, used for debugging and logging.
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
  public String description() {
    return info() + " expression";
  }

  /**
   * Returns the simplified class name.
   * @return class name
   */
  public String info() {
    return Util.name(this);
  }

  /**
   * Serializes the expression tree.
   * @param ser serializer
   * @throws IOException I/O exception
   */
  public abstract void plan(Serializer ser) throws IOException;

  @Override
  public abstract String toString();
}
