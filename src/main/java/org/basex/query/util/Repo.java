package org.basex.query.util;

import static org.basex.util.Token.*;
import java.io.File;

import org.basex.core.Context;
import org.basex.core.Prop;
import org.basex.io.IOFile;
import org.basex.query.QueryException;
import org.basex.query.util.Package.Component;
import org.basex.util.TokenList;
import org.basex.util.TokenMap;
import org.basex.util.TokenObjMap;
import org.basex.util.Util;

/**
 * Repository.
 * @author BaseX Team 2005-11, BSD License
 * @author Rositsa Shadura
 */
public final class Repo {
  /** Package descriptor. */
  private static final String PKGDESC = "expath-pkg.xml";
  /**
   * Namespace-dictionary - contains all namespaces available in the repository
   * and the packages in which they are found.
   */
  public final TokenObjMap<TokenList> nsDict;
  /**
   * Package dictionary - contains all installed packages and their directories.
   */
  public final TokenMap pkgDict;
  /** Context. */
  private final Context ctx;

  /**
   * Constructor.
   * @param c context
   */
  public Repo(final Context c) {
    nsDict = new TokenObjMap<TokenList>();
    pkgDict = new TokenMap();
    ctx = c;
    final File repoDir = new File(ctx.prop.get(Prop.REPOPATH));
    if(repoDir.exists()) {
      final File[] pkgDirs = repoDir.listFiles();
      for(final File pkgDir : pkgDirs) {
        if(pkgDir.isDirectory()) readPkg(pkgDir);
      }
    }
  }

  /**
   * Reads a package descriptor and adds components' namespaces to
   * namespace-dictionary and packages - to package dictionary.
   * @param pkgDir package directory
   */
  private void readPkg(final File pkgDir) {
    try {
      final File pkgDesc = new File(pkgDir.getPath() + Prop.DIRSEP + PKGDESC);
      if(pkgDesc.exists()) {
        final IOFile io = new IOFile(pkgDesc);
        final Package pkg = PackageParser.parse(io, ctx, null);
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
        pkgDict.add(pkg.getName(), token(pkgDir.getName()));
      } else Util.notexpected("Missing package descriptor for package "
          + pkgDir.getName());

    } catch(final QueryException ex) {
      Util.debug(ex);
    }
  }
}
