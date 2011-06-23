package org.basex.query.util.pkg;

import static org.basex.query.util.pkg.PkgText.*;
import static org.basex.util.Token.*;

import java.io.File;

import org.basex.core.Context;
import org.basex.core.Prop;
import org.basex.io.IOFile;
import org.basex.query.QueryException;
import org.basex.query.util.pkg.Package.Component;
import org.basex.util.TokenMap;
import org.basex.util.TokenObjMap;
import org.basex.util.TokenSet;
import org.basex.util.Util;

/**
 * Repository.
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
  /** Repository path. */
  private File path;

  /**
   * Constructor.
   * @param ctx database context
   */
  public Repo(final Context ctx) {
    context = ctx;
    path = new File(ctx.prop.get(Prop.REPOPATH));
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
      context.prop.set(Prop.REPOPATH, repo);
      path = new File(repo);
    }
    final File[] dirs = path.listFiles();
    if(dirs == null) return;
    for(final File dir : dirs) if(dir.isDirectory()) readPkg(dir);
  }

  /**
   * Creates the repository directory, if not done yet.
   */
  public void create() {
    if(!path.exists()) path.mkdirs();
  }

  /**
   * Returns the path to the specified repository package.
   * @param pkg package
   * @return file reference
   */
  public File path(final String pkg) {
    return new File(path, pkg);
  }

  /**
   * Removes a package from the namespace and package dictionaries when it is
   * deleted.
   * @param pkg deleted package
   */
  public synchronized void remove(final Package pkg) {
    // Delete package from namespace dictionary
    for(final Component comp : pkg.comps) {
      final byte[] ns = comp.uri;
      final TokenSet pkgs = nsDict.get(ns);
      if(pkgs.size() > 1) {
        pkgs.delete(pkg.getUniqueName());
      } else {
        nsDict.delete(ns);
      }
    }
    // Delete package from package dictionary
    pkgDict.delete(pkg.getUniqueName());
  }

  /**
   * Adds a newly installed package to the namespace and package dictionaries.
   * @param pkg new package
   * @param dir new package directory
   */
  public synchronized void add(final Package pkg, final String dir) {
    // Update namespace dictionary
    for(final Component comp : pkg.comps) {
      if(nsDict.id(comp.uri) == 0) {
        final TokenSet vals = new TokenSet();
        vals.add(pkg.getUniqueName());
        nsDict.add(comp.uri, vals);
      } else {
        nsDict.get(comp.uri).add(pkg.getUniqueName());
      }
    }
    // Update package dictionary
    pkgDict.add(pkg.getUniqueName(), token(dir));
  }

  /**
   * Reads a package descriptor and adds components namespaces to
   * namespace-dictionary and packages - to package dictionary.
   * @param dir package directory
   */
  private void readPkg(final File dir) {
    final IOFile desc = new IOFile(dir, DESCRIPTOR);
    if(desc.exists()) {
      try {
        final Package pkg = new PkgParser(context, null).parse(desc);
        // Read package components
        for(final Component comp : pkg.comps) {
          // Add component's namespace to namespace dictionary
          if(comp.uri != null) {
            if(nsDict.get(comp.uri) != null) {
              nsDict.get(comp.uri).add(pkg.getUniqueName());
            } else {
              final TokenSet vals = new TokenSet();
              vals.add(pkg.getUniqueName());
              nsDict.add(comp.uri, vals);
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
