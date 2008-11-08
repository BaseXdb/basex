package org.basex.query.xpath.func;

import org.basex.data.Data;
import org.basex.query.QueryException;
import org.basex.query.xpath.XPContext;
import org.basex.query.xpath.expr.Expr;
import org.basex.query.xpath.item.Bln;
import org.basex.util.Token;

/**
 * Constructor for local-name() function.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Tim Petrowsky
 */
public final class Lang extends Func {
  /**
   * Function Constructor.
   * @param arg expression array
   */
  public Lang(final Expr[] arg) {
    super(arg, "lang(item)");
  }

  @Override
  public Bln eval(final XPContext ctx)
      throws QueryException {

    // should actually be treated as XML namespace
    final Data data = ctx.item.data;
    final int lang = data.attNameID(Token.token("xml:lang"));
    // no lang attribute specified - return false
    if(lang == 0) return Bln.FALSE;
    final byte[] lng = evalArgs(ctx)[0].str();

    // parse current node and all ancestors for the lang attribute
    int r = ctx.item.nodes[0];
    while(r != 0) {
      final int pre = r;
      final byte[] att = data.attValue(lang, pre);
      if(att != null) return Bln.get(found(att, lng));
      r = data.parent(r, data.kind(r));
    }
    return Bln.FALSE;
  }

  /**
   * Simple method to find language attribute inside specified token. 
   * Not really efficient, but not often used anyway.
   * @param tok token 
   * @param lng language
   * @return if language attribute has been found
   */
  private boolean found(final byte[] tok, final byte[] lng) {
    final String t = Token.string(tok).toLowerCase();
    final String s = Token.string(lng).toLowerCase();
    return t.matches('^' + s + "($|-)|-" + s + "($|-)");
  }
  
  @Override
  public boolean checkArguments() {
    return args.length == 1;
  }
}
