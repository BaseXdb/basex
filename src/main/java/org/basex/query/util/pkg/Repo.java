package org.basex.query.util.pkg;

import static org.basex.query.util.pkg.PkgText.*;
import static org.basex.util.Token.*;

import org.basex.core.MainProp;
import org.basex.core.Context;
import org.basex.io.IOFile;
import org.basex.query.QueryException;
import org.basex.query.util.pkg.Package.Component;
import org.basex.util.Util;
import org.basex.util.hash.TokenMap;
import org.basex.util.hash.TokenObjMap;
import org.basex.util.hash.TokenSet;

/**
 * Repository manager.
 * @author BaseX Team 2005-12, BSD License
 * @author Rositsa Shadura
 */
public final class Repo {
  /** Context. */
  public final Context context;

  /**
   * Namespace-dictionary with all namespaces (unique names) available
   * in the repository, and the packages in which they are found.
   */
  private final TokenObjMap<TokenSet> nsDict = new TokenObjMap<TokenSet>();
  /** Package dictionary with installed packages and their directories. */
  private final TokenMap pkgDict = new TokenMap();

  /** Initialization flag (the repository can only be initialized once). */
  private boolean init;
  /** Repository path. */
  private IOFile path;

  /**
   * Constructor.
   * @param ctx database context
   */
  public Repo(final Context ctx) {
    context = ctx;
    path = new IOFile(ctx.mprop.get(MainProp.REPOPATH));
  }

  /**
   * Returns the namespace dictionary. Initializes the repository if not done
   * yet.
   * @return dictionary
   */
  public TokenObjMap<TokenSet> nsDict() {
    init(null);
    return nsDict;
  }

  /**
   * Returns the package dictionary. Initializes the repository if not done yet.
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

    if(repo != null) {
      context.mprop.set(MainProp.REPOPATH, repo);
      path = new IOFile(repo);
    }
    for(final IOFile dir : path.children()) {
      if(dir.isDir()) readPkg(dir);
    }
  }

  /**
   * Returns the path to the specified repository package.
   * @param pkg package
   * @return file reference
   */
  public IOFile path(final String pkg) {
    return new IOFile(path, pkg);
  }

  /**
   * Removes a package from the namespace and package dictionaries when it is
   * deleted.
   * @param pkg deleted package
   */
  public synchronized void remove(final Package pkg) {
    final byte[] name = pkg.uniqueName();
    // delete package from namespace dictionary
    for(final Component comp : pkg.comps) {
      final byte[] uri = comp.uri;
      final TokenSet pkgs = nsDict.get(uri);
      if(pkgs.size() > 1) {
        pkgs.delete(name);
      } else {
        nsDict.delete(uri);
      }
    }
    // delete package from package dictionary
    pkgDict.delete(name);
  }

  /**
   * Adds a newly installed package to the namespace and package dictionaries.
   * @param pkg new package
   * @param dir new package directory
   */
  public synchronized void add(final Package pkg, final String dir) {
    final byte[] name = pkg.uniqueName();
    // update namespace dictionary
    for(final Component comp : pkg.comps) {
      if(nsDict.id(comp.uri) == 0) {
        nsDict.add(comp.uri, new TokenSet(name));
      } else {
        nsDict.get(comp.uri).add(name);
      }
    }
    // update package dictionary
    pkgDict.add(name, token(dir));
  }

  /**
   * Reads a package descriptor and adds components namespaces to
   * namespace-dictionary and packages - to package dictionary.
   * @param dir package directory
   */
  private void readPkg(final IOFile dir) {
    final IOFile desc = new IOFile(dir, DESCRIPTOR);
    if(desc.exists()) {
      try {
        final Package pkg = new PkgParser(context.repo, null).parse(desc);
        final byte[] name = pkg.uniqueName();
        // read package components
        for(final Component comp : pkg.comps) {
          // add component's namespace to namespace dictionary
          if(comp.uri != null) {
            if(nsDict.id(comp.uri) != 0) {
              nsDict.get(comp.uri).add(name);
            } else {
              nsDict.add(comp.uri, new TokenSet(name));
            }
          }
        }
        // add package to package dictionary
        pkgDict.add(name, token(dir.name()));
      } catch(final QueryException ex) {
        Util.errln(ex.getMessage());
      }
    } else {
      Util.errln(MISSDESC, dir);
    }
  }
}
