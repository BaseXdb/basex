package org.basex.query.util;

import static org.basex.query.util.Err.*;
import static org.basex.util.Token.*;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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
  private PkgValidator() {

  }

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
    final Iterator<Dependency> depIt = pkg.dep.iterator();
    Dependency dep;
    while(depIt.hasNext()) {
      dep = depIt.next();
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
    final Iterator<byte[]> instIt = instVers.iterator();
    if(dep.versions != null) {
      // Get acceptable versions for secondary package
      final TokenSet accept = getAcceptVersions(dep.versions);
      final Iterator<byte[]> acceptIt = accept.iterator();
      // Check if any acceptable version is already installed
      byte[] nextVer = null;
      while(acceptIt.hasNext()) {
        nextVer = acceptIt.next();
        if(instVers.id(nextVer) != 0) return true;
      }
    } else if(dep.semver != null) {
      // Version template - if secondary package is installed, its version must
      // be compatible with the defined template
      final Version semVer = new Version(dep.semver);
      while(instIt.hasNext()) {
        if(new Version(instIt.next()).isCompatible(semVer)) return true;
      }
    } else if(dep.semverMin != null && dep.semverMax == null) {
      // Version template for minimal acceptable version - if secondary package
      // is installed, its version must be either compatible with this template
      // or greater than it
      final Version semVer = new Version(dep.semverMin);
      Version v;
      while(instIt.hasNext()) {
        v = new Version(instIt.next());
        if(v.isCompatible(semVer) || v.compareTo(semVer) >= 0) return true;
      }
    } else if(dep.semverMin == null && dep.semverMax != null) {
      // Version template for maximal acceptable version - if secondary package
      // is installed, its version must be either compatible with this template
      // or smaller than it
      final Version semVer = new Version(dep.semverMax);
      Version v;
      while(instIt.hasNext()) {
        v = new Version(instIt.next());
        if(v.isCompatible(semVer) || v.compareTo(semVer) <= 0) return true;
      }
    } else if(dep.semverMin != null && dep.semverMax != null) {
      // Version templates for minimal and maximal acceptable version - if
      // secondary package is installed, its version must be equal or above the
      // minimal and strictly below the maximal
      final Version min = new Version(dep.semverMin);
      final Version max = new Version(dep.semverMax);
      Version v;
      while(instIt.hasNext()) {
        v = new Version(instIt.next());
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
    final Iterator<byte[]> pkgIt = ctx.repo.pkgDict.iterator();
    byte[] nextPkg;
    while(pkgIt.hasNext()) {
      nextPkg = pkgIt.next();
      if(startsWith(nextPkg, pkgName)) versions.add(getPkgVersion(nextPkg));
    }
    return versions;
  }

  /**
   * Extracts the acceptable versions for a secondary package.
   * @param versions versions' set
   * @return list with acceptable versions
   */
  private static TokenSet getAcceptVersions(final byte[] versions) {
    final TokenSet versList = new TokenSet();
    for(final byte[] v : split(versions, ' ')) {
      versList.add(v);
    }
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
    final Iterator<Component> compIt = pkg.comps.iterator();
    Component comp;
    while(compIt.hasNext()) {
      comp = compIt.next();
      if(comp.namespace == null) PKGDESCINV.thrw(ii,
          "Component namespace is not specified.");
      else if(comp.file == null) PKGDESCINV.thrw(ii,
          "Component file is not specified");
      if(isInstalled(comp, pkg.uri, ctx, ii)) MODISTALLED.thrw(ii,
          getCompName(string(comp.file)));
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
    if(pkgs != null) {
      Iterator<byte[]> pkgsIt = pkgs.iterator();
      byte[] nextPkg;
      while(pkgsIt.hasNext()) {
        nextPkg = pkgsIt.next();
        if(!eq(getPkgName(nextPkg), pkgName)) {
          // Installed package is a different one, not just a different version
          // of the current one
          byte[] pkgDir = ctx.repo.pkgDict.get(nextPkg);
          File pkgDesc = new File(ctx.prop.get(Prop.REPOPATH) + Prop.DIRSEP
              + string(pkgDir) + Prop.DIRSEP + PKGDESC);
          final IOFile io = new IOFile(pkgDesc);
          final Package pkg = PackageParser.parse(io, ctx, ii);
          final Iterator<Component> compIt = pkg.comps.iterator();
          Component nextComp;
          while(compIt.hasNext()) {
            nextComp = compIt.next();
            if(getCompName(string(nextComp.file)).equals(
                getCompName(string(comp.file)))) return true;
          }
        }
      }
    }
    return false;
  }

  /**
   * Extracts package version from unique package name.
   * @param pkgName unique package name: name-version
   * @return package version
   */
  private static byte[] getPkgVersion(final byte[] pkgName) {
    final int idx = lastIndexOf(pkgName, '-');
    if(idx == -1) return null;
    return subtoken(pkgName, idx + 1, pkgName.length);
  }

  /**
   * Extracts package name form unique package name.
   * @param pkgName unique package name: name-version
   * @return package name
   */
  private static byte[] getPkgName(final byte[] pkgName) {
    final int idx = lastIndexOf(pkgName, '-');
    return idx == -1 ? pkgName : subtoken(pkgName, 0, idx);
  }

  /**
   * Extracts component's file name from component's path.
   * @param compPath component's path
   * @return component's name
   */
  private static String getCompName(final String compPath) {
    final int idx = compPath.lastIndexOf(Prop.DIRSEP);
    return idx == -1 ? compPath
        : compPath.substring(idx, compPath.length() - 1);
  }
}
