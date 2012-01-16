package org.basex.query.util.pkg;

import static org.basex.query.util.Err.*;
import static org.basex.query.util.pkg.Package.*;
import static org.basex.query.util.pkg.PkgText.*;
import static org.basex.util.Token.*;

import org.basex.core.Text;
import org.basex.io.IO;
import org.basex.io.IOFile;
import org.basex.query.QueryException;
import org.basex.query.util.pkg.Package.Component;
import org.basex.query.util.pkg.Package.Dependency;
import org.basex.util.InputInfo;
import org.basex.util.Version;
import org.basex.util.hash.TokenSet;
import org.basex.util.list.ObjList;

/**
 * Package validator. This class executes some essential checks before the
 * installation of a package.
 * @author BaseX Team 2005-12, BSD License
 * @author Rositsa Shadura
 */
public final class PkgValidator {
  /** Repository context. */
  private final Repo repo;
  /** Input info. */
  private final InputInfo input;

  /**
   * Constructor.
   * @param r repository context
   * @param ii input info
   */
  public PkgValidator(final Repo r, final InputInfo ii) {
    repo = r;
    input = ii;
  }

  /**
   * Checks package descriptor and if packages involved in dependencies are
   * already installed.
   * @param pkg package
   * @throws QueryException query exception
   */
  public void check(final Package pkg) throws QueryException {
    // check if package is already installed
    final byte[] name = pkg.uniqueName();
    if(repo.pkgDict().get(name) != null) PKGINST.thrw(input, name);
    // check package dependencies
    checkDepends(pkg);
    // check package components
    checkComps(pkg);
  }

  /**
   * Checks dependency elements, if packages involved in dependencies are
   * already installed and if processor dependencies are fulfilled.
   * @param pkg package
   * @throws QueryException query exception
   */
  private void checkDepends(final Package pkg) throws QueryException {
    final ObjList<Dependency> procs = new ObjList<Package.Dependency>();
    for(final Dependency dep : pkg.dep) {
      // first check of dependency elements are consistently defined in the
      // descriptor
      if(dep.pkg == null && dep.processor == null)
        PKGDESCINV.thrw(input, MISSSECOND);
      // if dependency involves a package, check if this package or an
      // appropriate version of it is installed
      if(dep.pkg != null && depPkg(dep) == null)
        NECPKGNOTINST.thrw(input, dep.pkg);
      // if dependency involves a processor, add it to the list with processor
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
  public byte[] depPkg(final Dependency dep) {
    // get installed versions of secondary package
    final TokenSet instVers = new TokenSet();
    for(final byte[] nextPkg : repo.pkgDict().keys())
      if(nextPkg != null && startsWith(nextPkg, dep.pkg))
        instVers.add(version(nextPkg));
    // check if an appropriate version is already installed
    final byte[] version = availVersion(dep, instVers);
    return version == null ? null : dep.name(version);
  }

  /**
   * Checks if current version of BaseX is among the processor dependencies.
   * @param procs processor dependencies
   * @throws QueryException query exception
   */
  private void checkProcs(final ObjList<Dependency> procs)
      throws QueryException {

    boolean supported = false;
    for(final Dependency d : procs) {
      if(!eq(lc(d.processor), token(Text.NAMELC))) {
        supported = false;
        break;
      }
      // extract version
      final int i = Text.VERSION.indexOf(" ");
      final String v = i == -1 ? Text.VERSION : Text.VERSION.substring(0, i);
      // check if current version is acceptable for the dependency
      supported = availVersion(d, new TokenSet(token(v))) != null;
    }
    if(!supported) PKGNOTSUPP.thrw(input);
  }

  /**
   * Checks compatibility of dependency version with installed version.
   * @param dep dependency
   * @param currentVers current versions - either currently installed versions
   *          for a package or current version of BaseX
   * @return available appropriate version
   */
  private byte[] availVersion(final Dependency dep,
      final TokenSet currentVers) {
    if(currentVers.size() == 0) return null;
    if(dep.versions != null) {
      // get acceptable versions for secondary package/processor
      final TokenSet versList = new TokenSet(split(dep.versions, ' '));
      // check if any acceptable version is already installed
      for(final byte[] v : versList) if(currentVers.id(v) != 0) return v;
    } else if(dep.semver != null) {
      // version template - version of secondary package or BaseX version must
      // be compatible with the defined template
      final Version semVer = new Version(dep.semver);
      for(final byte[] v : currentVers)
        if(new Version(v).isCompatible(semVer)) return v;
    } else if(dep.semverMin != null && dep.semverMax != null) {
      // version templates for minimal and maximal acceptable version - version
      // of secondary package or BaseX version must be equal or above
      // the minimal and strictly below the maximal
      final Version min = new Version(dep.semverMin);
      final Version max = new Version(dep.semverMax);
      for(final byte[] nextVer : currentVers) {
        final Version v = new Version(nextVer);
        if(v.compareTo(min) >= 0 && v.compareTo(max) < 0) return nextVer;
      }
    } else if(dep.semverMin != null) {
      // version template for minimal acceptable version - version of secondary
      // package or BaseX version must be either compatible with this template
      // or greater than it
      final Version semVer = new Version(dep.semverMin);
      for(final byte[] nextVer : currentVers) {
        final Version v = new Version(nextVer);
        if(v.isCompatible(semVer) || v.compareTo(semVer) >= 0) return nextVer;
      }
    } else if(dep.semverMax != null) {
      // version template for maximal acceptable version - version of secondary
      // package or BaseX version must be either compatible with this template
      // or smaller than it
      final Version semVer = new Version(dep.semverMax);
      for(final byte[] nextVer : currentVers) {
        final Version v = new Version(nextVer);
        if(v.isCompatible(semVer) || v.compareTo(semVer) <= 0) return nextVer;
      }
    } else {
      // no versioning attribute is specified => any version of the secondary
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
      if(isInstalled(comp, pkg.name)) MODISTALLED.thrw(input, comp.name());
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
    // get packages in which the module's namespace is found
    final TokenSet pkgs = repo.nsDict().get(comp.uri);
    if(pkgs == null) return false;

    for(final byte[] nextPkg : pkgs) {
      if(nextPkg != null && !eq(Package.name(nextPkg), name)) {
        // installed package is a different one, not just a different version
        // of the current one
        final String pkgDir = string(repo.pkgDict().get(nextPkg));
        final IO pkgDesc = new IOFile(repo.path(pkgDir), DESCRIPTOR);
        final Package pkg = new PkgParser(repo, input).parse(pkgDesc);
        for(final Component nextComp : pkg.comps) {
          if(nextComp.name().equals(comp.name())) return true;
        }
      }
    }
    return false;
  }
}
