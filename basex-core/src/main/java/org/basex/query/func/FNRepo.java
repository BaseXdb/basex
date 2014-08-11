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
 * @author BaseX Team 2005-14, BSD License
 * @author Rositsa Shadura
 */
public final class FNRepo extends BuiltinFunc {
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
   * @param sc static context
   * @param info input info
   * @param func function definition
   * @param args arguments
   */
  public FNRepo(final StaticContext sc, final InputInfo info, final Function func,
      final Expr... args) {
    super(sc, info, func, args);
  }

  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    switch(func) {
      case _REPO_LIST: return list(qc);
      default:         return super.iter(qc);
    }
  }

  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    checkCreate(qc);

    final RepoManager rm = new RepoManager(qc.context, ii);
    // either path to package or package name
    final String pkg = exprs.length == 0 ? null : Token.string(toToken(exprs[0], qc));
    switch(func) {
      case _REPO_INSTALL:
        rm.install(pkg);
        return null;
      case _REPO_DELETE:
        rm.delete(pkg);
        return null;
      default:
        return super.item(qc, ii);
    }
  }

  /**
   * Performs the list function.
   * @param qc query context
   * @return iterator
   */
  private static Iter list(final QueryContext qc) {
    final NodeSeqBuilder cache = new NodeSeqBuilder();
    for(final byte[] p : qc.context.repo.pkgDict()) {
      if(p == null) continue;
      final FElem elem = new FElem(PACKAGE);
      elem.add(NAME, Package.name(p));
      elem.add(VERSION, Package.version(p));
      elem.add(TYPE, PkgText.EXPATH);
      cache.add(elem);
    }
    // traverse all directories, ignore root entries with dashes
    for(final IOFile dir : qc.context.repo.path().children()) {
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
  public boolean accept(final ASTVisitor visitor) {
    return visitor.lock(DBLocking.REPO) && super.accept(visitor);
  }
}
