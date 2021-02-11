package org.basex.query.util.pkg;

import static org.basex.query.util.pkg.PkgText.*;

import java.util.*;

import org.basex.core.*;
import org.basex.io.*;
import org.basex.query.*;
import org.basex.util.*;

/**
 * EXPath repositories.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Rositsa Shadura
 */
public final class EXPathRepo {
  /** Namespace dictionary with namespace URIs and package IDs. */
  private final HashMap<String, HashSet<String>> nsDict = new HashMap<>();
  /** Package dictionary with package IDs and instances. */
  private final HashMap<String, Pkg> pkgDict = new HashMap<>();
  /** Static options. */
  private final StaticOptions sopts;
  /** Repository path (lazy instantiation). */
  private IOFile repo;

  /**
   * Constructor.
   * @param sopts static options
   */
  public EXPathRepo(final StaticOptions sopts) {
    this.sopts = sopts;
  }

  /**
   * Resets the repository.
   * @return self reference
   */
  public EXPathRepo reset() {
    repo = null;
    nsDict.clear();
    pkgDict.clear();
    return this;
  }

  /**
   * Returns the path to the repository.
   * @return dictionary
   */
  public IOFile path() {
    return init().repo;
  }

  /**
   * Returns the namespace dictionary.
   * @return dictionary
   */
  public HashMap<String, HashSet<String>> nsDict() {
    return init().nsDict;
  }

  /**
   * Returns the package dictionary.
   * @return dictionary
   */
  public HashMap<String, Pkg> pkgDict() {
    return init().pkgDict;
  }

  /**
   * Returns the path to the specified repository package.
   * @param path package path
   * @return file reference
   */
  public IOFile path(final String path) {
    return new IOFile(path(), path);
  }

  /**
   * Adds a newly installed package to the namespace and package dictionaries.
   * @param pkg new package
   */
  void add(final Pkg pkg) {
    init();
    addPkg(pkg);
  }

  /**
   * Deletes a package from the namespace and package dictionaries when it is deleted.
   * @param pkg deleted package
   */
  void delete(final Pkg pkg) {
    init();

    final String id = pkg.id();
    // delete package from namespace dictionary
    for(final PkgComponent comp : pkg.comps) {
      final String uri = comp.uri;
      final HashSet<String> pkgs = nsDict.get(uri);
      if(pkgs != null) {
        if(pkgs.size() > 1) {
          pkgs.remove(id);
        } else {
          nsDict.remove(uri);
        }
      }
    }
    // delete package from package dictionary
    pkgDict.remove(id);
  }

  /**
   * Initializes the package repository.
   * @return self reference
   */
  private synchronized EXPathRepo init() {
    if(repo == null) {
      repo = new IOFile(sopts.get(StaticOptions.REPOPATH));
      // ignore directories starting with dot (#1122)
      for(final IOFile path : repo.children(IOFile.NO_HIDDEN)) {
        if(path.isDir()) readPkg(path);
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
      addPkg(new PkgParser(null).parse(desc).path(dir.name()));
    } catch(final QueryException ex) {
      Util.errln(ex);
    }
  }

  /**
   * Adds a package to the package dictionary.
   * @param pkg package
   */
  private void addPkg(final Pkg pkg) {
    // read package components
    final String id = pkg.id();
    for(final PkgComponent comp : pkg.comps) {
      // add component's namespace to namespace dictionary
      if(comp.uri != null) {
        nsDict.computeIfAbsent(comp.uri, k -> new HashSet<>()).add(id);
      }
    }
    pkgDict.put(id, pkg);
  }
}
