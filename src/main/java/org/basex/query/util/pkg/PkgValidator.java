package org.basex.query.util.pkg;

import static org.basex.query.util.Err.*;
import static org.basex.query.util.pkg.Package.*;
import static org.basex.query.util.pkg.PkgText.*;
import static org.basex.util.Token.*;

import java.util.ArrayList;
import java.util.List;

import org.basex.core.Context;
import org.basex.core.Text;
import org.basex.io.IO;
import org.basex.io.IOFile;
import org.basex.query.QueryException;
import org.basex.query.util.pkg.Package.Component;
import org.basex.query.util.pkg.Package.Dependency;
import org.basex.util.InputInfo;
import org.basex.util.TokenSet;

/**
 * Package validator. This class executes some essential checks before the
 * installation of a package.
 * @author BaseX Team 2005-11, BSD License
 * @author Rositsa Shadura
 */
public final class PkgValidator {
  /** Database context. */
  private final Context context;
  /** Input info. */
  private final InputInfo input;

  /**
   * Constructor.
   * @param ctx database context
   * @param ii input info
   */
  public PkgValidator(final Context ctx, final InputInfo ii) {
    context = ctx;
    input = ii;
  }

  /**
   * Checks package descriptor and if packages involved in dependencies are
   * already installed.
   * @param pkg package
   * @throws QueryException query exception
   */
  public void check(final Package pkg) throws QueryException {
    // Check if package is already installed
    if(context.repo.pkgDict().get(pkg.getUniqueName()) != null)
      PKGINST.thrw(input);
    // Check package dependencies
    checkDepends(pkg);
    // Check package components
    checkComps(pkg);
  }

  /**
   * Checks dependency elements, if packages involved in dependencies are
   * already installed and if processor dependencies are fulfilled.
   * @param pkg package
   * @throws QueryException query exception
   */
  private void checkDepends(final Package pkg) throws QueryException {
    final List<Dependency> procs = new ArrayList<Package.Dependency>();
    for(final Dependency dep : pkg.dep) {
      // First check of dependency elements are consistently defined in the
      // descriptor
      if(dep.pkg == null && dep.processor == null) PKGDESCINV.thrw(input,
          MISSSECOND);
      // If dependency involves a package, check if this package or an
      // appropriate version of it is installed
      if(dep.pkg != null && getDepPkg(dep) == null) NECPKGNOTINST.thrw(input,
          dep.pkg);
      // If dependency involves a processor, add it to the list with processor
      // dependencies
      if(dep.processor != null) procs.add(dep);
    }
    if(procs.size() != 0) checkProcs(procs);
  }

  /**
   * Checks if secondary package, i.e. package involved in a dependency is
   * already installed.
   * @param dep dependency
   * @return result
   */
  public byte[] getDepPkg(final Dependency dep) {
    // Get installed versions of secondary package
    final TokenSet instVers = new TokenSet();
    for(final byte[] nextPkg : context.repo.pkgDict().keys())
      if(nextPkg != null && startsWith(nextPkg, dep.pkg))
        instVers.add(getVersion(nextPkg));
    // Check if an appropriate version is already installed
    final byte[] version = getAvailVersion(dep, instVers);
    return version == null ? null : dep.getName(version);
  }

  /**
   * Checks if current version of BaseX is among the processor dependencies.
   * @param procs processor dependencies
   * @throws QueryException query exception
   */
  private void checkProcs(final List<Dependency> procs) throws QueryException {
    boolean isSupported = false;
    for(final Dependency d : procs) {
      if(!eq(lc(d.processor), token(Text.NAMELC))) {
        isSupported = false;
        break;
      }
      // extract basex version
      final int idx = Text.VERSION.indexOf(" ");
      final String v = idx == -1 ? Text.VERSION
          : Text.VERSION.substring(0, idx);
      final TokenSet currentVers = new TokenSet();
      currentVers.add(token(v));
      // Check if current version of basex is acceptable for the dependency
      isSupported = (getAvailVersion(d, currentVers) == null) ? false : true;
    }
    if(!isSupported) PKGNOTSUPP.thrw(input);
  }

  /**
   * Checks compatibility of dependency version with installed version.
   * @param dep dependency
   * @param currentVers current versions - either currently installed versions
   *          for a package or current version of BaseX
   * @return available appropriate version
   */
  private byte[] getAvailVersion(final Dependency dep,
      final TokenSet currentVers) {
    if(currentVers.size() == 0) return null;
    if(dep.versions != null) {
      // Get acceptable versions for secondary package/processor
      final TokenSet versList = new TokenSet();
      for(final byte[] v : split(dep.versions, ' '))
        versList.add(v);
      // Check if any acceptable version is already installed
      for(final byte[] v : versList)
        if(currentVers.id(v) != 0) return v;
    } else if(dep.semver != null) {
      // Version template - version of secondary package or BaseX version must
      // be compatible with the defined template
      final PkgVersion semVer = new PkgVersion(dep.semver);
      for(final byte[] v : currentVers)
        if(new PkgVersion(v).isCompatible(semVer)) return v;
    } else if(dep.semverMin != null && dep.semverMax != null) {
      // Version templates for minimal and maximal acceptable version - version
      // of secondary package or BaseX version must be equal or above
      // the minimal and strictly below the maximal
      final PkgVersion min = new PkgVersion(dep.semverMin);
      final PkgVersion max = new PkgVersion(dep.semverMax);
      for(final byte[] nextVer : currentVers) {
        final PkgVersion v = new PkgVersion(nextVer);
        if(v.compareTo(min) >= 0 && v.compareTo(max) < 0) return nextVer;
      }
    } else if(dep.semverMin != null) {
      // Version template for minimal acceptable version - version of secondary
      // package or BaseX version must be either compatible with this template
      // or greater than it
      final PkgVersion semVer = new PkgVersion(dep.semverMin);
      for(final byte[] nextVer : currentVers) {
        final PkgVersion v = new PkgVersion(nextVer);
        if(v.isCompatible(semVer) || v.compareTo(semVer) >= 0) return nextVer;
      }
    } else if(dep.semverMax != null) {
      // Version template for maximal acceptable version - version of secondary
      // package or BaseX version must be either compatible with this template
      // or smaller than it
      final PkgVersion semVer = new PkgVersion(dep.semverMax);
      for(final byte[] nextVer : currentVers) {
        final PkgVersion v = new PkgVersion(nextVer);
        if(v.isCompatible(semVer) || v.compareTo(semVer) <= 0) return nextVer;
      }
    } else {
      // No versioning attribute is specified => any version of the secondary
      // package is acceptable
      return currentVers.keys()[0];
    }
    return null;
  }

  /**
   * Checks consistency of components and if components are already installed as
   * part of other packages.
   * @param pkg package
   * @throws QueryException query exception
   */
  private void checkComps(final Package pkg) throws QueryException {
    // modules other than xquery could be supported in future
    for(final Component comp : pkg.comps) {
      if(isInstalled(comp, pkg.name)) MODISTALLED.thrw(input, comp.getName());
    }
  }

  /**
   * Checks if an XQuery component is already installed as part of another
   * package.
   * @param comp component
   * @param name component's package
   * @return result
   * @throws QueryException query exception
   */
  private boolean isInstalled(final Component comp, final byte[] name)
      throws QueryException {
    // Get packages in which the module's namespace is found
    final TokenSet pkgs = context.repo.nsDict().get(comp.uri);
    if(pkgs == null) return false;

    for(final byte[] nextPkg : pkgs) {
      if(nextPkg != null && !eq(Package.getName(nextPkg), name)) {
        // Installed package is a different one, not just a different version
        // of the current one
        final String pkgDir = string(context.repo.pkgDict().get(nextPkg));
        final IO pkgDesc = new IOFile(context.repo.path(pkgDir), DESCRIPTOR);
        final Package pkg = new PkgParser(context, input).parse(pkgDesc);
        for(final Component nextComp : pkg.comps) {
          if(nextComp.getName().equals(comp.getName())) return true;
        }
      }
    }
    return false;
  }

  /**
   * Returns installed versions of package.
   * @param pkgName package name
   * @return installed versions
   */
  public TokenSet getInstalledVersions(final byte[] pkgName) {
    final TokenSet versions = new TokenSet();
    for(final byte[] nextPkg : context.repo.pkgDict().keys())
      if(nextPkg != null && startsWith(nextPkg, pkgName))
        versions.add(getVersion(nextPkg));
    return versions;
  }

  /**
   * Extracts the acceptable versions for a secondary package.
   * @param versions versions' set
   * @return list with acceptable versions
   */
  public static TokenSet getAcceptVersions(final byte[] versions) {
    final TokenSet versList = new TokenSet();
    for(final byte[] v : split(versions, ' '))
      versList.add(v);
    return versList;
  }
}
