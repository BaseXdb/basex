package org.basex.query.util.repo;

import static org.basex.query.util.repo.PkgText.*;
import static org.basex.util.Token.*;

import java.io.File;

import org.basex.core.Context;
import org.basex.core.Prop;
import org.basex.io.IOFile;
import org.basex.query.QueryException;
import org.basex.query.util.repo.Package.Component;
import org.basex.util.TokenList;
import org.basex.util.TokenMap;
import org.basex.util.TokenObjMap;
import org.basex.util.Util;

/**
 * Repository.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Rositsa Shadura
 */
public final class Repo {
  /**
   * Namespace-dictionary - contains all namespaces available in the repository
   * and the packages in which they are found.
   */
  public final TokenObjMap<TokenList> nsDict = new TokenObjMap<TokenList>();
  /**
   * Package dictionary - contains all installed packages and their directories.
   */
  public final TokenMap pkgDict = new TokenMap();
  /** Context. */
  private final Context ctx;

  /**
   * Constructor.
   * @param c context
   */
  public Repo(final Context c) {
    ctx = c;
    final File repoDir = new File(ctx.prop.get(Prop.REPOPATH));
    final File[] dirs = repoDir.listFiles();
    if(dirs == null) return;
    for(final File dir : dirs) if(dir.isDirectory()) readPkg(dir);
  }

  /**
   * Reads a package descriptor and adds components' namespaces to
   * namespace-dictionary and packages - to package dictionary.
   * @param dir package directory
   */
  private void readPkg(final File dir) {
    try {
      final File pkgDesc = new File(dir.getPath(), DESCRIPTOR);
      if(pkgDesc.exists()) {
        final IOFile io = new IOFile(pkgDesc);
        final Package pkg = new PkgParser(ctx).parse(io, null);
        // Read package components
        for(final Component comp : pkg.comps) {
          // Add component's namespace to namespace dictionary
          if(comp.namespace != null) {
            if(nsDict.get(comp.namespace) != null) {
              nsDict.get(comp.namespace).add(pkg.getName());
            } else {
              final TokenList vals = new TokenList();
              vals.add(pkg.getName());
              nsDict.add(comp.namespace, vals);
            }
          }
        }
        // Add package to package dictionary
        pkgDict.add(pkg.getName(), token(dir.getName()));
      } else {
        Util.errln(NOTEXP, dir);
      }
    } catch(final QueryException ex) {
      Util.errln(ex);
    }
  }
}
