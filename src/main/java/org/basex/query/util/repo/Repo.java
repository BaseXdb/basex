package org.basex.query.util.repo;

import static org.basex.query.util.repo.PkgText.*;
import static org.basex.util.Token.*;

import java.io.File;

import org.basex.core.Context;
import org.basex.core.Prop;
import org.basex.io.IOFile;
import org.basex.query.QueryException;
import org.basex.query.util.repo.Package.Component;
import org.basex.util.TokenMap;
import org.basex.util.TokenObjMap;
import org.basex.util.TokenSet;
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
  private final TokenObjMap<TokenSet> nsDict = new TokenObjMap<TokenSet>();
  /** Package dictionary with installed packages and their directories. */
  private final TokenMap pkgDict = new TokenMap();
  /** Context. */
  private final Context context;
  /** Initialization flag (the repository can only be initialized once). */
  private boolean init;

  /**
   * Constructor.
   * @param ctx context
   */
  public Repo(final Context ctx) {
    context = ctx;
  }

  /**
   * Returns the namespace dictionary.
   * Initializes the repository if not done yet.
   * @return dictionary
   */
  public TokenObjMap<TokenSet> nsDict() {
    init(null);
    return nsDict;
  }

  /**
   * Returns the package dictionary.
   * Initializes the repository if not done yet.
   * @return dictionary
   */
  public TokenMap pkgDict() {
    init(null);
    return pkgDict;
  }

  /**
   * Initializes the package repository.
   * @param repo repository. The default path is used if set to {@code null}
   */
  public void init(final String repo) {
    if(init) return;
    init = true;

    if(repo != null) context.prop.set(Prop.REPOPATH, repo);
    final File repoDir = new File(context.prop.get(Prop.REPOPATH));
    final File[] dirs = repoDir.listFiles();
    if(dirs == null) return;
    for(final File dir : dirs) if(dir.isDirectory()) readPkg(dir);
  }

  /**
   * Reads a package descriptor and adds components namespaces to
   * namespace-dictionary and packages - to package dictionary.
   * @param dir package directory
   */
  private void readPkg(final File dir) {
    final File pkgDesc = new File(dir, DESCRIPTOR);
    if(pkgDesc.exists()) {
      final IOFile io = new IOFile(pkgDesc);
      try {
        final Package pkg = new PkgParser(context, null).parse(io);
        // Read package components
        for(final Component comp : pkg.comps) {
          // Add component's namespace to namespace dictionary
          if(comp.namespace != null) {
            if(nsDict.get(comp.namespace) != null) {
              nsDict.get(comp.namespace).add(pkg.getUniqueName());
            } else {
              final TokenSet vals = new TokenSet();
              vals.add(pkg.getUniqueName());
              nsDict.add(comp.namespace, vals);
            }
          }
        }
        // Add package to package dictionary
        pkgDict.add(pkg.getUniqueName(), token(dir.getName()));
      } catch(final QueryException ex) {
        Util.errln(ex.getMessage());
      }
    } else {
      Util.errln(NOTEXP, dir);
    }
  }
}
