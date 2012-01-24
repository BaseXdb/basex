package org.basex.query.func;

import static org.basex.util.Token.*;

import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.expr.Expr;
import org.basex.query.item.Item;
import org.basex.query.item.Str;
import org.basex.query.iter.ItemCache;
import org.basex.query.iter.Iter;
import org.basex.query.util.pkg.Package;
import org.basex.query.util.pkg.RepoManager;
import org.basex.util.InputInfo;

/**
 * Functions on EXPath packages.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Rositsa Shadura
 */
public final class FNRepo extends FuncCall {
  /**
   * Constructor.
   * @param ii input info
   * @param f function definition
   * @param e arguments
   */
  protected FNRepo(final InputInfo ii, final Function f, final Expr[] e) {
    super(ii, f, e);
  }

  @Override
  public Iter iter(final QueryContext ctx) throws QueryException {
    checkAdmin(ctx);
    switch(def) {
      case _REPO_LIST:
        final ItemCache cache = new ItemCache();
        for(final byte[] p : ctx.context.repo.pkgDict())
          if(p != null) cache.add(Str.get(Package.name(p)));
        return cache;
      default:
        return super.iter(ctx);
    }
  }

  @Override
  public Item item(final QueryContext ctx, final InputInfo ii)
      throws QueryException {

    checkAdmin(ctx);
    final RepoManager repoMng = new RepoManager(ctx.context.repo);
    // either path to package or package name
    final String pkg = expr.length == 0 ? null : string(checkStr(expr[0], ctx));
    switch(def) {
      case _REPO_INSTALL:
        repoMng.install(pkg, ii);
        return null;
      case _REPO_DELETE:
        repoMng.delete(pkg, ii);
        return null;
      default:
        return super.item(ctx, ii);
    }
  }

  @Override
  public boolean uses(final Use u) {
    // don't allow pre-evaluation
    return u == Use.CTX || super.uses(u);
  }
}
