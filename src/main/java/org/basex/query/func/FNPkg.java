package org.basex.query.func;

import static org.basex.util.Token.*;

import java.util.Iterator;

import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.expr.Expr;
import org.basex.query.item.Item;
import org.basex.query.item.Str;
import org.basex.query.iter.Iter;
import org.basex.query.util.pkg.RepoManager;
import org.basex.util.InputInfo;

/**
 * Functions on EXPath packages
 * 
 * @author BaseX Team 2005-11, BSD License
 * @author Rositsa Shadura
 * 
 */
public final class FNPkg extends FuncCall {
  /**
   * Constructor.
   * @param ii input info
   * @param f function definition
   * @param e arguments
   */
  protected FNPkg(InputInfo ii, Function f, Expr[] e) {
    super(ii, f, e);
  }

  @Override
  public Iter iter(final QueryContext ctx) throws QueryException {
    checkAdmin(ctx);
    final Iterator<byte[]> i = ctx.context.repo.pkgDict().iterator();
    switch(def) {
      case _PKG_LIST:
        return new Iter() {
          @Override
          public Item next() throws QueryException {
            final byte[] next = i.next();
            return next == null ? null : Str.get(next);
          }
        };
      default:
        return super.iter(ctx);
    }
  }

  @Override
  public Item item(final QueryContext ctx, final InputInfo ii)
      throws QueryException {
    checkAdmin(ctx);
    final RepoManager repoMng = new RepoManager(ctx.context.repo);
    // Either path to package or package name
    final String pkg = string(checkStr(expr[0], ctx));
    switch(def) {
      case _PKG_INSTALL:
        repoMng.install(pkg, ii);
        return null;
      case _PKG_DELETE:
        repoMng.delete(pkg, ii);
        return null;
      default:
        return super.item(ctx, ii);
    }
  }

}
