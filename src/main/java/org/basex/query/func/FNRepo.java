package org.basex.query.func;

import static org.basex.util.Token.*;

import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.expr.Expr;
import org.basex.query.item.*;
import org.basex.query.iter.*;
import org.basex.query.util.pkg.Package;
import org.basex.query.util.pkg.RepoManager;
import org.basex.util.InputInfo;

/**
 * Functions on EXPath packages.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Rositsa Shadura
 */
public final class FNRepo extends StandardFunc {
  /** Element name. */
  private static final QNm PACKAGE = new QNm("package");
  /** Header attribute: name. */
  private static final QNm NAME = new QNm("name");
  /** Header attribute: name. */
  private static final QNm VERSION = new QNm("version");

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
    switch(sig) {
      case _REPO_LIST: return list(ctx);
      default:         return super.iter(ctx);
    }
  }

  @Override
  public Item item(final QueryContext ctx, final InputInfo ii) throws QueryException {
    checkCreate(ctx);

    final RepoManager rm = new RepoManager(ctx.context.repo);
    // either path to package or package name
    final String pkg = expr.length == 0 ? null : string(checkStr(expr[0], ctx));
    switch(sig) {
      case _REPO_INSTALL:
        rm.install(pkg, ii);
        return null;
      case _REPO_DELETE:
        rm.delete(pkg, ii);
        return null;
      default:
        return super.item(ctx, ii);
    }
  }

  /**
   * Performs the list function.
   * @param ctx query context
   * @return iterator
   */
  private Iter list(final QueryContext ctx) {
    final NodeCache cache = new NodeCache();
    for(final byte[] p : ctx.context.repo.pkgDict()) {
      if(p == null) continue;
      final FElem elem = new FElem(PACKAGE);
      elem.add(new FAttr(NAME, Package.name(p)));
      elem.add(new FAttr(VERSION, Package.version(p)));
      cache.add(elem);
    }
    return cache;
  }

  @Override
  public boolean uses(final Use u) {
    // don't allow pre-evaluation
    return u == Use.CTX || super.uses(u);
  }
}
