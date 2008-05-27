package org.basex.query.xquery.func;

import static org.basex.query.xquery.XQText.*;
import org.basex.query.xquery.XQException;
import org.basex.query.xquery.expr.Expr;
import org.basex.query.xquery.item.Uri;
import org.basex.query.xquery.util.Err;
import org.basex.util.Array;
import org.basex.util.Levenshtein;
import org.basex.util.Set;
import org.basex.util.Token;

/**
 * Global expression context.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class FNIndex extends Set {
  /** Function classes. */
  private FunDef[] funcs;
  /** Singleton instance. */
  private static FNIndex instance = new FNIndex();

  /**
   * Gets the function instance.
   * @return instance
   */
  public static FNIndex get() {
    return instance;
  }

  /**
   * Constructor, registering XQuery functions.
   */
  private FNIndex() {
    funcs = new FunDef[CAP];
    for(final FunDef def : FunDef.values()) {
      final String dsc = def.desc;
      final byte[] key = Token.token(dsc.substring(0, dsc.indexOf("(")));
      final int i = add(key);
      if(i < 0) throw new RuntimeException("Function defined twice:" + def);
      funcs[i] = def;
    }
  }

  /**
   * Returns the specified function.
   * @param name function name
   * @param uri function uri
   * @param args optional arguments
   * @return function instance
   * @throws XQException evaluation exception
   */
  public Fun get(final byte[] name, final Uri uri, final Expr[] args)
      throws XQException {
    
    final int id = id(name);
    if(id != 0) {
      try {
        // create function
        final FunDef fl = funcs[id];
        if(fl.uri != uri) return null;
        
        final Fun f = fl.func.newInstance();
        f.args = args;
        f.func = fl;
        // check number of arguments
        if(args.length < fl.min || fl.max >= fl.min && args.length > fl.max)
          Err.or(XPARGS, fl);
        return f;
      } catch(final XQException ex) {
        throw ex;
      } catch(final Exception ex) {
        throw new RuntimeException("Can't run " + Token.string(name));
      }
    }
    return null;
  }

  /**
   * Finds similar function names for throwing an error message.
   * @param name function name
   * @throws XQException evaluation exception
   */
  public void error(final byte[] name) throws XQException {
    // check similar predefined function
    final byte[] nm = Token.lc(name);
    for(int k = 1; k < size; k++) {
      if(Levenshtein.similar(nm, Token.lc(keys[k]))) {
        Err.or(FUNSIMILAR, name, keys[k]);
      }
    }
  }

  @Override
  protected void rehash() {
    super.rehash();
    funcs = Array.extend(funcs);
  }
}
