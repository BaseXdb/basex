package org.basex.query.xpath.func;

import org.basex.query.xpath.XPContext;
import org.basex.query.xpath.expr.Expr;
import org.basex.query.xpath.item.Nod;
import org.basex.query.xpath.item.Str;
import org.basex.util.Token;

/**
 * Constructor for namespace-uri() function.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Tim Petrowsky
 */
public final class NamespaceURI extends Func {
  /**
   * Function Constructor.
   * @param arg expression array
   */
  public NamespaceURI(final Expr[] arg) {
    super(arg, "namespace-uri(nodeset?)");
  }

  @Override
  public Str eval(final XPContext ctx) {
    // Namespaces not supported
    return new Str(Token.EMPTY);
  }

  @Override
  public boolean checkArguments() {
    return args.length == 0 || args.length == 1 &&
      args[0].returnedValue() == Nod.class;
  }
}
