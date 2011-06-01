package org.basex.query.util;

import static org.basex.query.util.Err.*;
import static org.basex.util.Token.*;
import static org.basex.query.util.Package.*;

import java.io.File;

import org.basex.core.Context;
import org.basex.core.Prop;
import org.basex.io.IOFile;
import org.basex.query.QueryException;
import org.basex.query.util.Package.Component;
import org.basex.query.util.Package.Dependency;
import org.basex.util.InputInfo;
import org.basex.util.TokenList;
import org.basex.util.TokenSet;

/**
 * Package validator. This class executes some essential checks before
 * installation of a package.
 * @author BaseX Team 2005-11, BSD License
 * @author Rositsa Shadura
 */
public final class PkgValidator {
  /** Package descriptor. */
  private static final String PKGDESC = "expath-pkg.xml";

  /** Constructor. */
  private PkgValidator() { }

  /**
   * Checks package descriptor and if packages involved in dependencies are
   * already installed.
   * @param pkg package
   * @param ctx context
   * @param ii input info
   * @throws QueryException query exception
   */
  public static void check(final Package pkg, final Context ctx,
      final InputInfo ii) throws QueryException {
    // Check mandatory attributes
    if(pkg.uri == null) PKGDESCINV.thrw(ii,
        "Mandatory attribute name is missing");
    else if(pkg.abbrev == null) PKGDESCINV.thrw(ii,
        "Mandatory attribute abbrev is missing");
    else if(pkg.version == null) PKGDESCINV.thrw(ii,
        "Mandatory attribute version is missing");
    else if(pkg.spec == null) PKGDESCINV.thrw(ii,
        "Mandatory attribute spec is missing");
    // Check if package is already installed
    if(ctx.repo.pkgDict.get(pkg.getName()) != null) PKGINSTALLED.thrw(ii);
    // Check package dependencies
    checkDepends(pkg, ctx, ii);
    // Check package components - currently only xquery modules are taken into
    // account
    checkComps(pkg, ctx, ii);
  }

  /**
   * Checks dependency elements and if packages involved in dependencies are
   * already installed.
   * @param pkg package
   * @param ctx context
   * @param ii input info
   * @throws QueryException query exception
   */
  private static void checkDepends(final Package pkg, final Context ctx,
      final InputInfo ii) throws QueryException {
    for(final Dependency dep : pkg.dep) {
      // First check of dependency elements are consistently defined in the
      // descriptor
      if(dep.pkg == null) PKGDESCINV.thrw(ii,
          "Name of secondary package is missing");
      // If dependency element is defined consistently, check if it is already
      // installed
      if(!isInstalled(dep, ctx)) PKGNOTINSTALLED.thrw(ii, dep.pkg);
    }
  }

  /**
   * Checks if secondary package, i.e. package involved in a dependency is
   * already installed.
   * @param dep dependency
   * @param ctx context
   * @return result
   */
  private static boolean isInstalled(final Dependency dep, final Context ctx) {
    // Get installed versions of secondary package
    final TokenSet instVers = getInstalledVersions(dep.pkg, ctx);
    if(instVers.size() == 0) return false;
    if(dep.versions != null) {
      // Get acceptable versions for secondary package
      final TokenSet accept = getAcceptVersions(dep.versions);
      // Check if any acceptable version is already installed
      for(final byte[] nextVer : accept)
        if(instVers.id(nextVer) != 0) return true;
    } else if(dep.semver != null) {
      // Version template - if secondary package is installed, its version must
      // be compatible with the defined template
      final Version semVer = new Version(dep.semver);
      for(final byte[] v : instVers)
        if(new Version(v).isCompatible(semVer)) return true;
    } else if(dep.semverMin != null && dep.semverMax == null) {
      // Version template for minimal acceptable version - if secondary package
      // is installed, its version must be either compatible with this template
      // or greater than it
      final Version semVer = new Version(dep.semverMin);
      for(final byte[] nextVer : instVers) {
        final Version v = new Version(nextVer);
        if(v.isCompatible(semVer) || v.compareTo(semVer) >= 0) return true;
      }
    } else if(dep.semverMin == null && dep.semverMax != null) {
      // Version template for maximal acceptable version - if secondary package
      // is installed, its version must be either compatible with this template
      // or smaller than it
      final Version semVer = new Version(dep.semverMax);
      for(final byte[] nextVer : instVers) {
        final Version v = new Version(nextVer);
        if(v.isCompatible(semVer) || v.compareTo(semVer) <= 0) return true;
      }
    } else if(dep.semverMin != null && dep.semverMax != null) {
      // Version templates for minimal and maximal acceptable version - if
      // secondary package is installed, its version must be equal or above the
      // minimal and strictly below the maximal
      final Version min = new Version(dep.semverMin);
      final Version max = new Version(dep.semverMax);
      for(final byte[] nextVer : instVers) {
        final Version v = new Version(nextVer);
        if(v.compareTo(min) >= 0 && v.compareTo(max) < 0) return true;
      }
    } else if(dep.versions == null && dep.semver == null
        && dep.semverMin == null && dep.semverMax == null) {
      // No versioning attribute is specified => any version of the secondary
      // package is acceptable
      return true;
    }
    return false;
  }

  /**
   * Returns installed versions of package.
   * @param pkgName package name
   * @param ctx context
   * @return installed versions
   */
  private static TokenSet getInstalledVersions(final byte[] pkgName,
      final Context ctx) {
    final TokenSet versions = new TokenSet();
    for(final byte[] nextPkg : ctx.repo.pkgDict)
      if(startsWith(nextPkg, pkgName)) versions.add(getPkgVersion(nextPkg));
    return versions;
  }

  /**
   * Extracts the acceptable versions for a secondary package.
   * @param versions versions' set
   * @return list with acceptable versions
   */
  private static TokenSet getAcceptVersions(final byte[] versions) {
    final TokenSet versList = new TokenSet();
    for(final byte[] v : split(versions, ' '))
      versList.add(v);
    return versList;
  }

  /**
   * Checks consistency of components and if components are already installed as
   * part of other packages.
   * @param pkg package
   * @param ctx context
   * @param ii input info
   * @throws QueryException query exception
   */
  private static void checkComps(final Package pkg, final Context ctx,
      final InputInfo ii) throws QueryException {
    for(final Component comp : pkg.comps) {
      if(comp.namespace == null) PKGDESCINV.thrw(ii,
          "Component namespace is not specified.");
      else if(comp.file == null) PKGDESCINV.thrw(ii,
          "Component file is not specified");
      if(isInstalled(comp, pkg.uri, ctx, ii)) MODISTALLED.thrw(ii,
          comp.getName());
    }
  }

  /**
   * Checks if a XQuery component is already installed as part of another
   * package.
   * @param comp component
   * @param pkgName component's package
   * @param ctx context
   * @param ii input info
   * @return result
   * @throws QueryException query exception
   */
  private static boolean isInstalled(final Component comp,
      final byte[] pkgName, final Context ctx, final InputInfo ii)
      throws QueryException {
    // Get packages in which the module's namespace is found
    final TokenList pkgs = ctx.repo.nsDict.get(comp.namespace);
    if(pkgs == null) return false;
    for(final byte[] nextPkg : pkgs) {
      if(!eq(getPkgName(nextPkg), pkgName)) {
        // Installed package is a different one, not just a different version
        // of the current one
        final byte[] pkgDir = ctx.repo.pkgDict.get(nextPkg);
        final File pkgDesc = new File(ctx.prop.get(Prop.REPOPATH) + Prop.DIRSEP
            + string(pkgDir) + Prop.DIRSEP + PKGDESC);
        final IOFile io = new IOFile(pkgDesc);
        final Package pkg = PackageParser.parse(io, ctx, ii);
        for(final Component nextComp : pkg.comps)
          if(nextComp.getName().equals(comp.getName())) return true;
      }
    }
    return false;
  }
}
