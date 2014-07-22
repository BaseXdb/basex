package org.basex.query.util.pkg;

import static org.basex.query.util.pkg.PkgText.*;
import static org.basex.util.Token.*;

import org.basex.core.*;
import org.basex.io.*;
import org.basex.query.*;
import org.basex.query.util.pkg.Package.Component;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * EXPath repository context.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Rositsa Shadura
 */
public final class Repo {
  /** Database context. */
  final Context context;

  /** Namespace-dictionary with all namespaces (unique names) available
   * in the repository, and the packages in which they are found. */
  private final TokenObjMap<TokenSet> nsDict = new TokenObjMap<>();
  /** Package dictionary with installed packages and their directories. */
  private final TokenMap pkgDict = new TokenMap();
  /** Repository path; will be initialized after first call. */
  private IOFile path;

  /**
   * Constructor.
   * @param ctx database context
   */
  public Repo(final Context ctx) {
    context = ctx;
  }

  /**
   * Returns the path to the repository.
   * @return dictionary
   */
  public IOFile path() {
    return init().path;
  }

  /**
   * Returns the namespace dictionary.
   * @return dictionary
   */
  public TokenObjMap<TokenSet> nsDict() {
    return init().nsDict;
  }

  /**
   * Returns the package dictionary.
   * @return dictionary
   */
  public TokenMap pkgDict() {
    return init().pkgDict;
  }

  /**
   * Returns the path to the specified repository package.
   * @param pkg package
   * @return file reference
   */
  public IOFile path(final String pkg) {
    return new IOFile(init().path, pkg);
  }

  /**
   * Adds a newly installed package to the namespace and package dictionaries.
   * @param pkg new package
   * @param dir new package directory
   */
  synchronized void add(final Package pkg, final String dir) {
    init();

    final byte[] name = pkg.uniqueName();
    // update namespace dictionary
    for(final Component comp : pkg.comps) {
      final TokenSet dict = nsDict.get(comp.uri);
      if(dict != null) {
        dict.add(name);
      } else {
        nsDict.put(comp.uri, new TokenSet(name));
      }
    }
    // update package dictionary
    pkgDict.put(name, token(dir));
  }

  /**
   * Deletes a package from the namespace and package dictionaries when it is deleted.
   * @param pkg deleted package
   */
  synchronized void delete(final Package pkg) {
    init();

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
   * Initializes the package repository.
   * @return self reference
   */
  private Repo init() {
    if(path == null) {
      path = new IOFile(context.globalopts.get(GlobalOptions.REPOPATH));
      for(final IOFile dir : path.children()) {
        if(dir.isDir()) readPkg(dir);
      }
    }
    return this;
  }

  /**
   * Reads a package descriptor and adds components namespaces to
   * namespace-dictionary and packages - to package dictionary.
   * @param dir package directory
   */
  private void readPkg(final IOFile dir) {
    final IOFile desc = new IOFile(dir, DESCRIPTOR);
    if(!desc.exists()) return;
    try {
      final Package pkg = new PkgParser(null).parse(desc);
      final byte[] name = pkg.uniqueName();
      // read package components
      for(final Component comp : pkg.comps) {
        // add component's namespace to namespace dictionary
        if(comp.uri != null) {
          final TokenSet dict = nsDict.get(comp.uri);
          if(dict != null) {
            dict.add(name);
          } else {
            nsDict.put(comp.uri, new TokenSet(name));
          }
        }
      }
      // add package to package dictionary
      pkgDict.put(name, token(dir.name()));
    } catch(final QueryException ex) {
      Util.errln(ex);
    }
  }
}
