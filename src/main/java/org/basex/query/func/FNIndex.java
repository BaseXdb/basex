package org.basex.query.func;

import static org.basex.query.QueryTokens.*;
import static org.basex.query.util.Err.*;
import static org.basex.util.Token.*;
import java.util.Arrays;
import org.basex.query.QueryException;
import org.basex.query.QueryParser;
import org.basex.query.expr.Expr;
import org.basex.query.expr.Expr.Use;
import org.basex.query.item.QNm;
import org.basex.query.util.NSGlobal;
import org.basex.util.Levenshtein;
import org.basex.util.TokenBuilder;
import org.basex.util.TokenSet;
import org.basex.util.Util;

/**
 * Global expression context.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public final class FNIndex extends TokenSet {
  /** Singleton instance. */
  private static final FNIndex INSTANCE = new FNIndex();
  /** Function classes. */
  private FunDef[] funcs;

  /**
   * Gets the function instance.
   * @return instance
   */
  public static FNIndex get() {
    return INSTANCE;
  }

  /**
   * Constructor, registering XQuery functions.
   */
  private FNIndex() {
    funcs = new FunDef[CAP];
    for(final FunDef def : FunDef.values()) {
      final String dsc = def.desc;
      final byte[] ln = token(dsc.substring(0, dsc.indexOf(PAR1)));
      final int i = add(full(def.uri, ln));
      if(i < 0) Util.notexpected("Function defined twice:" + def);
      funcs[i] = def;
    }
  }

  /**
   * Returns the specified function.
   * @param name function name
   * @param uri function uri
   * @param args optional arguments
   * @param qp query parser
   * @return function instance
   * @throws QueryException query exception
   */
  public Fun get(final byte[] name, final byte[] uri, final Expr[] args,
      final QueryParser qp) throws QueryException {

    final int id = id(full(uri, name));
    if(id == 0) return null;

    // create function
    final FunDef fl = funcs[id];
    if(!eq(fl.uri, uri)) return null;

    final Fun f = fl.get(qp.input(), args);
    if(!qp.ctx.xquery3 && f.uses(Use.X30)) qp.error(FEATURE11);
    // check number of arguments
    if(args.length < fl.min || args.length > fl.max) qp.error(XPARGS, fl);
    return f;
  }

  /**
   * Throws an error if one of the pre-defined functions is similar to the
   * specified function name.
   * @param name function name
   * @param qp query parser
   * @throws QueryException query exception
   */
  public void error(final QNm name, final QueryParser qp)
      throws QueryException {

    // compare specified name with names of predefined functions
    final byte[] nm = name.ln();
    final Levenshtein ls = new Levenshtein();
    for(int k = 1; k < size; ++k) {
      final int i = indexOf(keys[k], '}');
      final byte[] uri = substring(keys[k], 1, i);
      final byte[] ln = substring(keys[k], i + 1);
      if(eq(nm, ln)) {
        qp.error(FUNSIMILAR, name, NSGlobal.prefix(uri));
      } else if(ls.similar(nm, ln, 0)) {
        qp.error(FUNSIMILAR, name, ln);
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
  private byte[] full(final byte[] uri, final byte[] ln) {
    return new TokenBuilder().add('{').add(uri).add('}').add(ln).finish();
  }

  @Override
  protected void rehash() {
    super.rehash();
    funcs = Arrays.copyOf(funcs, size << 1);
  }
}
