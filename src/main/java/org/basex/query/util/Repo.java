package org.basex.query.util;

import java.io.File;
import java.util.Iterator;

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
      File pkgDir = null;
      for(int i = 0; i < pkgDirs.length; i++) {
        pkgDir = pkgDirs[i];
        if(pkgDir.isDirectory()) {
          final File pkgDesc = new File(pkgDir.getPath() + Prop.DIRSEP
              + PKGDESC);
          if(!pkgDesc.exists()) {
            Util.notexpected("Missing package descriptor for package "
                + pkgDir.getName());
          } else readPkg(pkgDesc);
        }
      }
    }
  }

  /**
   * Reads a package descriptor and adds components' namespaces to
   * namespace-dictionary and package - to package dictionary.
   * @param pkgDesc package descriptor
   */
  private void readPkg(final File pkgDesc) {
    try {
      final IOFile io = new IOFile(pkgDesc);
      final Package pkg = PackageParser.parse(io, ctx, null);
      // Read package components
      final Iterator<Component> compIt = pkg.comps.iterator();
      Component comp;
      byte[] compNs;
      while(compIt.hasNext()) {
        comp = compIt.next();
        compNs = comp.namespace;
        // Add component's namespace to namespace dictionary
        if(compNs != null) {
          if(nsDict.get(compNs) != null) nsDict.get(compNs).add(pkg.getName());
          else {
            final TokenList vals = new TokenList();
            vals.add(pkg.getName());
            nsDict.add(compNs, vals);
          }
        }
      }
      // Add package to package dictionary
      pkgDict.add(pkg.getName(), pkg.abbrev);
    } catch(QueryException ex) {
      Util.debug(ex);
    }
  }
}
