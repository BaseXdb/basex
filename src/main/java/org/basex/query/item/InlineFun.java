package org.basex.query.item;

import static org.basex.query.QueryTokens.*;

import java.io.IOException;

import org.basex.data.Serializer;
import org.basex.query.expr.Expr;
import org.basex.util.Token;

/**
 * An inline function item.
 * @author Leo
 */
public class InlineFun extends Fun {

  /** Function definition. */
  final Expr def;

  /**
   * Constructor.
   * @param fun function
   */
  public InlineFun(final Expr fun) {
    def = fun;
  }

  @Override
  public Value apply(final Expr[] args) {
    return null;
  }

  @Override
  public void plan(final Serializer ser) throws IOException {
    ser.openElement(this, TYPE, Token.token(name()));
    def.plan(ser);
    ser.closeElement();
  }

}
