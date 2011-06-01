package org.basex.query.util.repo;

import static org.basex.query.util.Err.*;
import static org.basex.query.util.repo.Package.*;
import static org.basex.query.util.repo.PkgText.*;
import static org.basex.util.Token.*;

import java.io.File;

import org.basex.core.Context;
import org.basex.core.Prop;
import org.basex.io.IOFile;
import org.basex.query.QueryException;
import org.basex.query.util.repo.Package.Component;
import org.basex.query.util.repo.Package.Dependency;
import org.basex.util.InputInfo;
import org.basex.util.TokenList;
import org.basex.util.TokenSet;
import org.basex.util.Util;

/**
 * Package validator. This class executes some essential checks before
 * installation of a package.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Rositsa Shadura
 */
public final class PkgValidator {
  /** Database context. */
  private final Context context;

  /**
   * Constructor.
   * @param ctx database context
   */
  public PkgValidator(final Context ctx) {
    context = ctx;
  }

  /**
   * Checks package descriptor and if packages involved in dependencies are
   * already installed.
   * @param pkg package
   * @param ii input info
   * @throws QueryException query exception
   */
  public void check(final Package pkg, final InputInfo ii)
      throws QueryException {

    // Check mandatory attributes
    if(pkg.uri == null)
      PKGDESCINV.thrw(ii, Util.info(MISSATTR, "name"));
    else if(pkg.abbrev == null)
      PKGDESCINV.thrw(ii, Util.info(MISSATTR, "abbrev"));
    else if(pkg.version == null)
      PKGDESCINV.thrw(ii, Util.info(MISSATTR, "version"));
    else if(pkg.spec == null)
      PKGDESCINV.thrw(ii, Util.info(MISSATTR, "spec"));

    // Check if package is already installed
    if(context.repo.pkgDict().get(pkg.getName()) != null) PKGINSTALLED.thrw(ii);
    // Check package dependencies
    checkDepends(pkg, ii);
    // Check package components - currently only xquery modules are taken into
    // account
    checkComps(pkg, ii);
  }

  /**
   * Checks dependency elements and if packages involved in dependencies are
   * already installed.
   * @param pkg package
   * @param ii input info
   * @throws QueryException query exception
   */
  private void checkDepends(final Package pkg, final InputInfo ii)
      throws QueryException {

    for(final Dependency dep : pkg.dep) {
      // First check of dependency elements are consistently defined in the
      // descriptor
      if(dep.pkg == null) PKGDESCINV.thrw(ii, MISSSECOND);
      // If dependency element is defined consistently, check if it is already
      // installed
      if(!isInstalled(dep)) PKGNOTINSTALLED.thrw(ii, dep.pkg);
    }
  }

  /**
   * Checks if secondary package, i.e. package involved in a dependency is
   * already installed.
   * @param dep dependency
   * @return result
   */
  private boolean isInstalled(final Dependency dep) {
    // Get installed versions of secondary package
    final TokenSet instVers = installedVersions(dep.pkg);
    if(instVers.size() == 0) return false;
    if(dep.versions != null) {
      // Get acceptable versions for secondary package
      final TokenSet accept = acceptVersions(dep.versions);
      // Check if any acceptable version is already installed
      for(final byte[] nextVer : accept)
        if(instVers.id(nextVer) != 0) return true;
    } else if(dep.semver != null) {
      // Version template - if secondary package is installed, its version must
      // be compatible with the defined template
      final PkgVersion semVer = new PkgVersion(dep.semver);
      for(final byte[] v : instVers)
        if(new PkgVersion(v).isCompatible(semVer)) return true;
    } else if(dep.semverMin != null && dep.semverMax == null) {
      // Version template for minimal acceptable version - if secondary package
      // is installed, its version must be either compatible with this template
      // or greater than it
      final PkgVersion semVer = new PkgVersion(dep.semverMin);
      for(final byte[] nextVer : instVers) {
        final PkgVersion v = new PkgVersion(nextVer);
        if(v.isCompatible(semVer) || v.compareTo(semVer) >= 0) return true;
      }
    } else if(dep.semverMin == null && dep.semverMax != null) {
      // Version template for maximal acceptable version - if secondary package
      // is installed, its version must be either compatible with this template
      // or smaller than it
      final PkgVersion semVer = new PkgVersion(dep.semverMax);
      for(final byte[] nextVer : instVers) {
        final PkgVersion v = new PkgVersion(nextVer);
        if(v.isCompatible(semVer) || v.compareTo(semVer) <= 0) return true;
      }
    } else if(dep.semverMin != null && dep.semverMax != null) {
      // Version templates for minimal and maximal acceptable version - if
      // secondary package is installed, its version must be equal or above the
      // minimal and strictly below the maximal
      final PkgVersion min = new PkgVersion(dep.semverMin);
      final PkgVersion max = new PkgVersion(dep.semverMax);
      for(final byte[] nextVer : instVers) {
        final PkgVersion v = new PkgVersion(nextVer);
        if(v.compareTo(min) >= 0 && v.compareTo(max) < 0) return true;
      }
    } else {
      // No versioning attribute is specified => any version of the secondary
      // package is acceptable
      return true;
    }
    return false;
  }

  /**
   * Returns installed versions of package.
   * @param pkgName package name
   * @return installed versions
   */
  private TokenSet installedVersions(final byte[] pkgName) {
    final TokenSet versions = new TokenSet();
    for(final byte[] nextPkg : context.repo.pkgDict())
      if(startsWith(nextPkg, pkgName)) versions.add(getVersion(nextPkg));
    return versions;
  }

  /**
   * Extracts the acceptable versions for a secondary package.
   * @param versions versions' set
   * @return list with acceptable versions
   */
  private TokenSet acceptVersions(final byte[] versions) {
    final TokenSet versList = new TokenSet();
    for(final byte[] v : split(versions, ' ')) versList.add(v);
    return versList;
  }

  /**
   * Checks consistency of components and if components are already installed as
   * part of other packages.
   * @param pkg package
   * @param ii input info
   * @throws QueryException query exception
   */
  private void checkComps(final Package pkg, final InputInfo ii)
      throws QueryException {

    for(final Component comp : pkg.comps) {
      if(comp.namespace == null)
        PKGDESCINV.thrw(ii, Util.info(MISSCOMP, "namespace"));
      else if(comp.file == null)
        PKGDESCINV.thrw(ii, Util.info(MISSCOMP, "file"));
      if(isInstalled(comp, pkg.uri, ii))
        MODISTALLED.thrw(ii, comp.getName());
    }
  }

  /**
   * Checks if an XQuery component is already installed as part of another
   * package.
   * @param comp component
   * @param name component's package
   * @param ii input info
   * @return result
   * @throws QueryException query exception
   */
  private boolean isInstalled(final Component comp, final byte[] name,
      final InputInfo ii) throws QueryException {

    // Get packages in which the module's namespace is found
    final TokenList pkgs = context.repo.nsDict().get(comp.namespace);
    if(pkgs == null) return false;

    for(final byte[] nextPkg : pkgs) {
      if(!eq(getName(nextPkg), name)) {
        // Installed package is a different one, not just a different version
        // of the current one
        final byte[] pkgDir = context.repo.pkgDict().get(nextPkg);
        final File pkgDesc = new File(new File(
            context.prop.get(Prop.REPOPATH), string(pkgDir)), DESCRIPTOR);
        final IOFile io = new IOFile(pkgDesc);
        final Package pkg = new PkgParser(context).parse(io, ii);
        for(final Component nextComp : pkg.comps) {
          if(nextComp.getName().equals(comp.getName())) return true;
        }
      }
    }
    return false;
  }
}
