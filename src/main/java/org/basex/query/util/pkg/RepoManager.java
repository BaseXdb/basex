package org.basex.query.util.pkg;

import static org.basex.query.util.Err.*;
import static org.basex.query.util.pkg.PkgText.*;
import static org.basex.util.Token.*;

import java.io.File;
import java.io.IOException;

import org.basex.core.Context;
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
  /** Database context. */
  private final Context ctx;

  /**
   * Constructor.
   * @param c database context
   */
  public RepoManager(final Context c) {
    ctx = c;
  }

  /**
   * Installs a new package.
   * @param path package path
   * @param ii input info
   * @throws QueryException query exception
   */
  public void install(final String path, final InputInfo ii)
      throws QueryException {

    // check package existence
    final File pkgFile = new File(path);
    if(!pkgFile.exists()) PKGNOTEXIST.thrw(ii, path);
    // check package name - must be a .xar file
    if(!path.endsWith(IO.XARSUFFIX)) INVPKGNAME.thrw(ii);

    // check repository if not done yet
    try {
      final Zip zip = new Zip(pkgFile);
      final byte[] cont = zip.read(DESCRIPTOR);
      final Package pkg = new PkgParser(ctx, ii).parse(new IOContent(cont));
      new PkgValidator(ctx, ii).check(pkg);
      zip.unzip(ctx.repo.path(extractPkgName(pkgFile.getPath())));
      ctx.repo.add(pkg, extractPkgName(pkgFile.getPath()));
    } catch(final IOException ex) {
      Util.debug(ex);
      throw PKGREADFAIL.thrw(ii, ex.getMessage());
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
    for(final byte[] nextPkg : ctx.repo.pkgDict()) {
      if(nextPkg != null) {
        final byte[] dir = ctx.repo.pkgDict().get(nextPkg);
        if(eq(Package.getName(nextPkg), token(pkg)) || eq(dir, token(pkg))) {
          // A package can be deleted either by its name or by its directory
          // name
          found = true;
          // check if package to be deleted participates in a dependency
          final byte[] primPkg = primary(nextPkg, ii);
          if(primPkg == null) {
            // clean package repository
            final IOFile f = ctx.repo.path(string(dir));
            final IOFile desc = new IOFile(f, DESCRIPTOR);
            ctx.repo.remove(new PkgParser(ctx, ii).parse(desc));
            // package does not participate in a dependency => delete it
            if(!f.delete()) CANNOTDELPKG.thrw(ii);
          } else PKGDEP.thrw(ii, string(primPkg), pkg);
        }
      }
    }
    if(!found) PKGNOTINST.thrw(ii, pkg);
  }

  /**
   * Extracts package name from package path.
   * @param path package path
   * @return package name
   */
  private static String extractPkgName(final String path) {
    final int i = path.lastIndexOf(File.separator);
    return path.substring(i + 1, path.length() - IO.XARSUFFIX.length());
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
    for(final byte[] nextPkg : ctx.repo.pkgDict()) {
      if(nextPkg != null && !eq(nextPkg, pkgName)) {
        // check only packages different from the current one
        final IOFile desc = new IOFile(ctx.repo.path(
            string(ctx.repo.pkgDict().get(nextPkg))), DESCRIPTOR);
        final Package pkg = new PkgParser(ctx, ii).parse(desc);
        final byte[] name = Package.getName(pkgName);
        for(final Dependency dep : pkg.dep)
          if(eq(dep.pkg, name)) return Package.getName(nextPkg);
      }
    }
    return null;
  }
}
