package org.basex.query.util.pkg;

import static org.basex.query.util.Err.*;
import static org.basex.query.util.pkg.PkgText.*;
import static org.basex.util.Token.*;

import java.io.IOException;

import org.basex.io.IO;
import org.basex.io.IOContent;
import org.basex.io.IOFile;
import org.basex.io.Zip;
import org.basex.query.QueryException;
import org.basex.query.util.pkg.Package.Dependency;
import org.basex.util.InputInfo;
import org.basex.util.Util;

/**
 * Repository manager.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Rositsa Shadura
 */
public final class RepoManager {
  /** Repository context. */
  private final Repo repo;

  /**
   * Constructor.
   * @param r repository context
   */
  public RepoManager(final Repo r) {
    repo = r;
  }

  /**
   * Installs a new package.
   * @param path package path
   * @param ii input info
   * @throws QueryException query exception
   */
  public void install(final String path, final InputInfo ii)
      throws QueryException {

    // check if package exists, and cache contents
    final IO io = IO.get(path);
    IOContent cont = null;
    try {
      cont = new IOContent(io.content());
    } catch(final IOException ex) {
      Util.debug(ex);
      PKGNOTEXIST.thrw(ii, path);
    }

    try {
      // parse and validate repository
      final Zip zip = new Zip(cont);
      final byte[] desc = zip.read(DESCRIPTOR);
      final Package pkg = new PkgParser(repo, ii).parse(new IOContent(desc));
      new PkgValidator(repo, ii).check(pkg);

      final String name = string(pkg.uniqueName()).replaceAll("[^\\w.-]", "");
      // unzip files and register repository
      zip.unzip(repo.path(name));
      repo.add(pkg, name);
    } catch(final IOException ex) {
      Util.debug(ex);
      PKGREADFAIL.thrw(ii, io.name(), ex.getMessage());
    }
  }

  /**
   * Removes a package from package repository.
   * @param pkg package
   * @param ii input info
   * @throws QueryException query exception
   */
  public void delete(final String pkg, final InputInfo ii)
      throws QueryException {
    boolean found = false;
    for(final byte[] nextPkg : repo.pkgDict()) {
      if(nextPkg != null) {
        final byte[] dir = repo.pkgDict().get(nextPkg);
        if(eq(Package.name(nextPkg), token(pkg)) || eq(dir, token(pkg))) {
          // A package can be deleted either by its name or by its directory
          // name
          found = true;
          // check if package to be deleted participates in a dependency
          final byte[] primPkg = primary(nextPkg, ii);
          if(primPkg == null) {
            // clean package repository
            final IOFile f = repo.path(string(dir));
            final IOFile desc = new IOFile(f, DESCRIPTOR);
            repo.remove(new PkgParser(repo, ii).parse(desc));
            // package does not participate in a dependency => delete it
            if(!f.delete()) CANNOTDELPKG.thrw(ii);
          } else PKGDEP.thrw(ii, string(primPkg), pkg);
        }
      }
    }
    if(!found) PKGNOTINST.thrw(ii, pkg);
  }

  /**
   * Checks if a package participates in a dependency.
   * @param pkgName package
   * @param ii input info
   * @return package depending on the current one
   * @throws QueryException query exception
   */
  private byte[] primary(final byte[] pkgName, final InputInfo ii)
      throws QueryException {
    for(final byte[] nextPkg : repo.pkgDict()) {
      if(nextPkg != null && !eq(nextPkg, pkgName)) {
        // check only packages different from the current one
        final IOFile desc = new IOFile(repo.path(
            string(repo.pkgDict().get(nextPkg))), DESCRIPTOR);
        final Package pkg = new PkgParser(repo, ii).parse(desc);
        final byte[] name = Package.name(pkgName);
        for(final Dependency dep : pkg.dep)
          // Check only package dependencies
          if(dep.pkg != null && eq(dep.pkg, name)) return Package.name(nextPkg);
      }
    }
    return null;
  }
}
