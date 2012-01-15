package org.basex.query.util.pkg;

import static org.basex.query.util.Err.*;
import static org.basex.query.util.pkg.PkgText.*;
import static org.basex.util.Token.*;

import java.io.FileNotFoundException;
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
 * @author BaseX Team 2005-12, BSD License
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
      cont = new IOContent(io.read());
    } catch(final IOException ex) {
      Util.debug(ex);
      PKGNOTEXIST.thrw(ii, path);
    }

    try {
      // parse and validate repository
      final Zip zip = new Zip(cont);
      final byte[] dsc = zip.read(DESCRIPTOR);
      final Package pkg = new PkgParser(repo, ii).parse(new IOContent(dsc));
      new PkgValidator(repo, ii).check(pkg);

      // choose unique directory, unzip files and register repository
      final IOFile file = uniqueDir(
          string(pkg.uniqueName()).replaceAll("[^\\w.-]+", "-"));
      zip.unzip(file);
      repo.add(pkg, file.name());
    } catch(final FileNotFoundException ex) {
      Util.debug(ex);
      PKGREADFNF.thrw(ii, io.name(), ex.getMessage());
    } catch(final IOException ex) {
      Util.debug(ex);
      PKGREADFAIL.thrw(ii, io.name(), ex.getMessage());
    }
  }

  /**
   * Returns a unique directory for the specified package.
   * @param n name
   * @return unique directory
   */
  private IOFile uniqueDir(final String n) {
    String nm = n;
    int c = 0;
    do {
      final IOFile io = repo.path(nm);
      if(!io.exists()) return io;
      nm = n + '-' + ++c;
    } while(true);
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
      if(nextPkg == null) continue;

      final byte[] dir = repo.pkgDict().get(nextPkg);
      if(eq(Package.name(nextPkg), token(pkg)) || eq(dir, token(pkg))) {
        // a package can be deleted either by its name or by its directory name
        found = true;
        // check if package to be deleted participates in a dependency
        final byte[] primPkg = primary(nextPkg, ii);
        if(primPkg != null) PKGDEP.thrw(ii, string(primPkg), pkg);

        // clean package repository
        final IOFile f = repo.path(string(dir));
        final IOFile desc = new IOFile(f, DESCRIPTOR);
        repo.remove(new PkgParser(repo, ii).parse(desc));
        // package does not participate in a dependency => delete it
        if(!f.delete()) CANNOTDELPKG.thrw(ii);
      }
    }
    if(!found) PKGNOTEXIST.thrw(ii, pkg);
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
