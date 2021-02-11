package org.basex.query.util.pkg;

import static org.basex.query.QueryError.*;
import static org.basex.query.util.pkg.Pkg.*;
import static org.basex.query.util.pkg.PkgText.*;

import java.util.*;

import org.basex.io.*;
import org.basex.query.*;
import org.basex.util.*;

/**
 * Package validator. This class executes some essential checks before installing a new package.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Rositsa Shadura
 */
public final class PkgValidator {
  /** Repository context. */
  private final EXPathRepo repo;
  /** Input info. */
  private final InputInfo info;

  /**
   * Constructor.
   * @param repo repository context
   * @param info input info
   */
  public PkgValidator(final EXPathRepo repo, final InputInfo info) {
    this.repo = repo;
    this.info = info;
  }

  /**
   * Checks package descriptor and if packages involved in dependencies are
   * already installed.
   * @param pkg package
   * @throws QueryException query exception
   */
  public void check(final Pkg pkg) throws QueryException {
    // check package dependencies
    checkDepends(pkg);
    // check package components
    checkComps(pkg);
  }

  /**
   * Checks dependency elements if packages involved in dependencies are
   * already installed and if processor dependencies are fulfilled.
   * @param pkg package
   * @throws QueryException query exception
   */
  private void checkDepends(final Pkg pkg) throws QueryException {
    final ArrayList<PkgDep> procs = new ArrayList<>();
    for(final PkgDep dep : pkg.dep) {
      // first check of dependency elements are consistently defined in the
      // descriptor
      if(dep.name == null && dep.processor == null) throw REPO_DESCRIPTOR_X.get(info, MISSSECOND);
      // if dependency involves a package, check if this package or an
      // appropriate version of it is installed
      if(dep.name != null && depPkg(dep) == null) throw REPO_NOTFOUND_X.get(info, dep.name);
      // if dependency involves a processor, add it to the list with processor
      // dependencies
      if(dep.processor != null) procs.add(dep);
    }
    if(!procs.isEmpty()) checkProcs(procs);
  }

  /**
   * Checks if a secondary package, i.e. package involved in a dependency is already installed.
   * @param dep dependency
   * @return result or {@code null}
   */
  String depPkg(final PkgDep dep) {
    // get installed versions of secondary package
    final HashSet<String> instVers = new HashSet<>();
    for(final String nextPkg : repo.pkgDict().keySet()) {
      if(nextPkg.startsWith(dep.name)) instVers.add(version(nextPkg));
    }
    // check if an appropriate version is already installed
    final String version = availVersion(dep, instVers);
    return version == null ? null : dep.id(version);
  }

  /**
   * Checks if current database version of is among the processor dependencies.
   * @param deps processor dependencies
   * @throws QueryException query exception
   */
  private void checkProcs(final ArrayList<PkgDep> deps) throws QueryException {
    // extract database version
    final HashSet<String> versions = new HashSet<>();
    final int version = Prop.VERSION.indexOf(' ');
    versions.add(version == -1 ? Prop.VERSION : Prop.VERSION.substring(0, version));

    // check if any of the dependencies math
    for(final PkgDep dep : deps) {
      if(dep.processor.toLowerCase(Locale.ENGLISH).equals(Prop.PROJECT) &&
          availVersion(dep, versions) != null) return;
    }
    throw REPO_VERSION.get(info);
  }

  /**
   * Checks compatibility of dependency version with installed version.
   * @param dep dependency
   * @param versions current versions - either currently installed versions
   * for a package or current version of BaseX
   * @return available appropriate version or {@code null}
   */
  private static String availVersion(final PkgDep dep, final HashSet<String> versions) {
    if(versions.isEmpty()) return null;
    if(dep.versions != null) {
      // get acceptable versions for secondary package/processor
      final HashSet<String> versList = new HashSet<>();
      Collections.addAll(versList, Strings.split(dep.versions, ' '));
      // check if any acceptable version is already installed
      for(final String v : versList) {
        if(versions.contains(v)) return v;
      }
    } else if(dep.semver != null) {
      // version template - version of secondary package or BaseX version must
      // be compatible with the defined template
      final Version semVer = new Version(dep.semver);
      for(final String v : versions)
        if(new Version(v).isCompatible(semVer)) return v;
    } else if(dep.semverMin != null && dep.semverMax != null) {
      // version templates for minimal and maximal acceptable version - version
      // of secondary package or BaseX version must be equal or above
      // the minimal and strictly below the maximal
      final Version min = new Version(dep.semverMin);
      final Version max = new Version(dep.semverMax);
      for(final String nextVer : versions) {
        final Version v = new Version(nextVer);
        if(v.compareTo(min) >= 0 && v.compareTo(max) < 0) return nextVer;
      }
    } else if(dep.semverMin != null) {
      // version template for minimal acceptable version - version of secondary
      // package or BaseX version must be either compatible with this template
      // or greater than it
      final Version semVer = new Version(dep.semverMin);
      for(final String nextVer : versions) {
        final Version v = new Version(nextVer);
        if(v.isCompatible(semVer) || v.compareTo(semVer) >= 0) return nextVer;
      }
    } else if(dep.semverMax != null) {
      // version template for maximal acceptable version - version of secondary
      // package or BaseX version must be either compatible with this template
      // or smaller than it
      final Version semVer = new Version(dep.semverMax);
      for(final String nextVer : versions) {
        final Version v = new Version(nextVer);
        if(v.isCompatible(semVer) || v.compareTo(semVer) <= 0) return nextVer;
      }
    } else {
      // no versioning attribute is specified => any version of the secondary
      // package is acceptable
      return versions.iterator().next();
    }
    return null;
  }

  /**
   * Checks consistency of components and if components are already installed as
   * part of other packages.
   * @param pkg package
   * @throws QueryException query exception
   */
  private void checkComps(final Pkg pkg) throws QueryException {
    // modules other than xquery could be supported in future
    for(final PkgComponent comp : pkg.comps) {
      if(isInstalled(comp, pkg.name())) throw REPO_INSTALLED_X.get(info, comp.name());
    }
  }

  /**
   * Checks if an XQuery component is already installed as part of another package.
   * @param comp component
   * @param name component's package
   * @return result
   * @throws QueryException query exception
   */
  private boolean isInstalled(final PkgComponent comp, final String name) throws QueryException {
    // get packages in which the module's namespace is found
    final HashSet<String> ids = repo.nsDict().get(comp.uri);
    if(ids == null) return false;

    for(final String id : ids) {
      if(id != null && !name(id).equals(name)) {
        // installed package is a different one, not just a different version of the current one
        final String pkgPath = repo.pkgDict().get(id).path();
        final IO pkgDesc = new IOFile(repo.path(pkgPath), DESCRIPTOR);
        final Pkg pkg = new PkgParser(info).parse(pkgDesc);
        for(final PkgComponent nextComp : pkg.comps) {
          if(nextComp.name().equals(comp.name())) return true;
        }
      }
    }
    return false;
  }
}
