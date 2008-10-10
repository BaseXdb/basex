package org.basex.query;

import java.io.IOException;

import org.basex.data.Serializer;
import org.basex.util.Token;

/**
 * Expression Information.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
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
    return "C0C0C0";
  }
  
  /**
   * Recursively sends the abstract syntax of this expression to the
   * specified serializer.
   * @param ser serializer
   * @throws IOException exception
   */
  public abstract void plan(Serializer ser) throws IOException;

  /**
   * Returns a string description of the expression. Contrary to the
   * {@link #toString()} method, the current expressions aren't included
   * in the output.
   * @return result of check
   */
  public String info() {
    return Token.string(name()) + " expression";
  }

  /**
   * Returns the simplified class name (for debugging).
   * @return class name
   */
  public byte[] name() {
    return Token.token(getClass().getSimpleName());
  }

  @Override
  public abstract String toString();
}
