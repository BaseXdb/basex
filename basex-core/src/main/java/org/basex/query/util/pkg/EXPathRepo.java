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
 * @author BaseX Team, BSD License
 * @author Rositsa Shadura
 */
public final class EXPathRepo {
  /** Static options. */
  private final StaticOptions sopts;
  /** Current repository state, replaced on updates (can be {@code null}; lazy instantiation). */
  private volatile State state;

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
  public synchronized EXPathRepo reset() {
    state = null;
    return this;
  }

  /**
   * Returns the path to the repository.
   * @return path
   */
  public IOFile path() {
    return state().path;
  }

  /**
   * Returns the namespace dictionary.
   * @return dictionary
   */
  public HashMap<String, HashSet<String>> nsDict() {
    return state().nsDict;
  }

  /**
   * Returns the package dictionary.
   * @return dictionary
   */
  public HashMap<String, Pkg> pkgDict() {
    return state().pkgDict;
  }

  /**
   * Returns the packages that contain components with the specified namespace URI.
   * @param uri namespace URI
   * @return packages
   */
  public ArrayList<Pkg> pkgs(final String uri) {
    final State st = state();
    final ArrayList<Pkg> pkgs = new ArrayList<>();
    final HashSet<String> ids = st.nsDict.get(uri);
    if(ids != null) {
      for(final String id : ids) pkgs.add(st.pkgDict.get(id));
    }
    return pkgs;
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
  synchronized void add(final Pkg pkg) {
    final State st = state();
    final HashMap<String, Pkg> pkgDict = new HashMap<>(st.pkgDict);
    pkgDict.put(pkg.id(), pkg);
    state = new State(st.path, pkgDict);
  }

  /**
   * Deletes a package from the namespace and package dictionaries when it is deleted.
   * @param pkg deleted package
   */
  synchronized void delete(final Pkg pkg) {
    final State st = state();
    final HashMap<String, Pkg> pkgDict = new HashMap<>(st.pkgDict);
    pkgDict.remove(pkg.id());
    state = new State(st.path, pkgDict);
  }

  /**
   * Returns the current repository state, initializing it if necessary.
   * @return state
   */
  private State state() {
    final State st = state;
    return st != null ? st : init();
  }

  /**
   * Initializes the repository state.
   * @return state
   */
  private synchronized State init() {
    if(state == null) {
      final IOFile path = sopts.repoPath();
      final HashMap<String, Pkg> pkgDict = new HashMap<>();
      // ignore directories starting with dot (#1122)
      path.children((name, attrs) -> {
        if(attrs.isDirectory() && !IOFile.isDotFile(name)) {
          final IOFile desc = new IOFile(new IOFile(path, name + '/'), DESCRIPTOR);
          if(desc.exists()) {
            try {
              final Pkg pkg = new PkgParser(null).parse(desc).path(name);
              pkgDict.put(pkg.id(), pkg);
            } catch(final QueryException ex) {
              Util.errln(ex);
            }
          }
        }
      });
      state = new State(path, pkgDict);
    }
    return state;
  }

  /**
   * Immutable repository state.
   * @param path repository path
   * @param pkgDict package dictionary with package IDs and instances
   * @param nsDict namespace dictionary with namespace URIs and package IDs
   */
  private record State(IOFile path, HashMap<String, Pkg> pkgDict,
      HashMap<String, HashSet<String>> nsDict) {
    /**
     * Constructor, deriving the namespace dictionary.
     * @param path repository path
     * @param pkgDict package dictionary
     */
    State(final IOFile path, final HashMap<String, Pkg> pkgDict) {
      this(path, pkgDict, new HashMap<>());
      for(final Pkg pkg : pkgDict.values()) {
        for(final PkgComponent comp : pkg.comps) {
          if(comp.uri != null) nsDict.computeIfAbsent(comp.uri, k -> new HashSet<>()).add(pkg.id());
        }
      }
    }
  }
}
