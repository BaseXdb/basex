package org.basex.query.xquery.expr;

import static org.basex.query.xquery.XQTokens.*;
import java.io.IOException;
import org.basex.data.Serializer;
import org.basex.query.ExprInfo;
import org.basex.query.xquery.XQContext;
import org.basex.query.xquery.XQException;
import org.basex.query.xquery.util.Var;
import org.basex.util.Token;

/**
 * User defined function.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class Func extends ExprInfo {
  /** Function name, including return type. */
  public Var var;
  /** Arguments. */
  public Var[] args;
  /** Function expression. */
  public Expr expr;
  /** Declaration flag. */
  public boolean decl;
  
  /**
   * Function constructor.
   * @param v function name
   * @param a arguments
   * @param d declaration flag
   */
  public Func(final Var v, final Var[] a, final boolean d) {
    var = v;
    args = a;
    decl = d;
  }
  
  /**
   * Compiles the function.
   * @param ctx xquery context
   * @throws XQException xquery exception
   */
  public void comp(final XQContext ctx) throws XQException {
    final int s = ctx.vars.size();
    for(int a = 0; a < args.length; a++) ctx.vars.add(args[a]);
    expr = ctx.comp(expr);
    ctx.vars.reset(s);
  }

  @Override
  public String toString() {
    return Token.string(var.name.str()) + "(...)" +
      (var.type != null ? " as " + var.type : "");
  }

  @Override
  public void plan(final Serializer ser) throws IOException {
    ser.startElement(this);
    ser.attribute(NAM, var.name.str());
    for(int i = 0; i < args.length; i++) {
      ser.attribute(Token.token(ARG + i), args[i].name.str());
    }
    ser.finishElement();
    expr.plan(ser);
    ser.closeElement();
  }
}
