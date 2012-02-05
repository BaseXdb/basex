package org.basex.query.func;

import static org.basex.query.QueryText.*;
import static org.basex.query.util.Err.*;
import static org.basex.util.Token.*;
import java.util.Arrays;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.expr.Expr;
import org.basex.query.expr.Expr.Use;
import org.basex.query.item.QNm;
import org.basex.query.util.NSGlobal;
import org.basex.util.InputInfo;
import org.basex.util.Levenshtein;
import org.basex.util.TokenBuilder;
import org.basex.util.Util;
import org.basex.util.hash.TokenSet;

/**
 * Static functions.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class Functions extends TokenSet {
  /** Singleton instance. */
  private static final Functions INSTANCE = new Functions();
  /** Function classes. */
  private Function[] funcs;

  /**
   * Returns the singleton instance.
   * @return instance
   */
  public static Functions get() {
    return INSTANCE;
  }

  /**
   * Constructor, registering XQuery functions.
   */
  private Functions() {
    funcs = new Function[CAP];
    for(final Function def : Function.values()) {
      final String dsc = def.desc;
      final byte[] ln = token(dsc.substring(0, dsc.indexOf(PAR1)));
      final int i = add(full(def.uri(), ln));
      if(i < 0) Util.notexpected("Function defined twice:" + def);
      funcs[i] = def;
    }
  }

  /**
   * Returns the specified function.
   * @param name function name
   * @param uri function uri
   * @param args optional arguments
   * @param ctx query context
   * @param ii input info
   * @return function instance
   * @throws QueryException query exception
   */
  public StandardFunc get(final byte[] name, final byte[] uri,
      final Expr[] args, final QueryContext ctx, final InputInfo ii)
          throws QueryException {

    final int id = id(full(uri, name));
    if(id == 0) return null;

    // create function
    final Function fl = funcs[id];
    if(!eq(fl.uri(), uri)) return null;

    final StandardFunc f = fl.get(ii, args);
    if(!ctx.xquery3 && f.uses(Use.X30)) FEATURE30.thrw(ii);
    // check number of arguments
    if(args.length < fl.min || args.length > fl.max) XPARGS.thrw(ii, fl);
    return f;
  }

  /**
   * Throws an error if one of the pre-defined functions is similar to the
   * specified function name.
   * @param name function name
   * @param ii input info
   * @throws QueryException query exception
   */
  public void error(final QNm name, final InputInfo ii) throws QueryException {
    // compare specified name with names of predefined functions
    final byte[] ln = name.local();
    final Levenshtein ls = new Levenshtein();
    for(int k = 1; k < size; ++k) {
      final int i = indexOf(keys[k], '}');
      final byte[] u = substring(keys[k], 1, i);
      final byte[] l = substring(keys[k], i + 1);
      if(eq(ln, l)) {
        final byte[] ur = name.uri();
        FUNSIMILAR.thrw(ii,
            new TokenBuilder(NSGlobal.prefix(ur)).add(':').add(l),
            new TokenBuilder(NSGlobal.prefix(u)).add(':').add(l));
      } else if(ls.similar(ln, l, 0)) {
        FUNSIMILAR.thrw(ii, name.string(), l);
      }
    }
  }

  /**
   * Returns a unique name representation of the function,
   * including the URI and function name.
   * @param uri namespace uri
   * @param ln local name
   * @return full name
   */
  private static byte[] full(final byte[] uri, final byte[] ln) {
    return new TokenBuilder().add('{').add(uri).add('}').add(ln).finish();
  }

  @Override
  protected void rehash() {
    super.rehash();
    funcs = Arrays.copyOf(funcs, size << 1);
  }
}
