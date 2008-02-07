package org.basex.query.xpath.func;

import org.basex.query.xpath.XPContext;
import org.basex.query.xpath.expr.Expr;
import org.basex.query.xpath.values.Literal;
import org.basex.query.xpath.values.NodeSet;
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
  public Literal eval(final XPContext ctx) {
    // Namespaces not supported
    return new Literal(Token.EMPTY);
  }

  @Override
  public boolean checkArguments() {
    return args.length == 0 || args.length == 1 &&
      args[0].returnedValue() == NodeSet.class;
  }
}
