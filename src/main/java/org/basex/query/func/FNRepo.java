package org.basex.query.func;

import org.basex.core.*;
import org.basex.io.*;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.iter.*;
import org.basex.query.util.*;
import org.basex.query.util.pkg.*;
import org.basex.query.util.pkg.Package;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.util.*;

/**
 * Functions on EXPath packages.
 * [JE] install() and delete() should be updating functions
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Rositsa Shadura
 */
public final class FNRepo extends StandardFunc {
  /** Element name. */
  private static final String PACKAGE = "package";
  /** Header attribute: name. */
  private static final String NAME = "name";
  /** Header attribute: type. */
  private static final String TYPE = "type";
  /** Header attribute: version. */
  private static final String VERSION = "version";

  /**
   * Constructor.
   * @param ii input info
   * @param f function definition
   * @param e arguments
   */
  public FNRepo(final InputInfo ii, final Function f, final Expr[] e) {
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

    final RepoManager rm = new RepoManager(ctx.context, ii);
    // either path to package or package name
    final String pkg = expr.length == 0 ? null : Token.string(checkStr(expr[0], ctx));
    switch(sig) {
      case _REPO_INSTALL:
        rm.install(pkg);
        return null;
      case _REPO_DELETE:
        rm.delete(pkg);
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
  private static Iter list(final QueryContext ctx) {
    final NodeSeqBuilder cache = new NodeSeqBuilder();
    for(final byte[] p : ctx.context.repo.pkgDict()) {
      if(p == null) continue;
      final FElem elem = new FElem(PACKAGE);
      elem.add(NAME, Package.name(p));
      elem.add(VERSION, Package.version(p));
      elem.add(TYPE, PkgText.EXPATH);
      cache.add(elem);
    }
    // traverse all directories, ignore root entries with dashes
    for(final IOFile dir : ctx.context.repo.path().children()) {
      if(dir.name().indexOf('-') != -1) continue;
      for(final String s : dir.descendants()) {
        final FElem elem = new FElem(PACKAGE);
        elem.add(NAME, dir.name() + '.' + s.replaceAll("\\..*", "").replace('/', '.'));
        elem.add(TYPE, PkgText.INTERNAL);
        cache.add(elem);
      }
    }
    return cache;
  }

  @Override
  public boolean uses(final Use u) {
    // don't allow pre-evaluation
    return u == Use.CTX || super.uses(u);
  }

  @Override
  public boolean accept(final ASTVisitor visitor) {
    return visitor.lock(DBLocking.REPO) && super.accept(visitor);
  }
}
