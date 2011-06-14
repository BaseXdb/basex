package org.basex.query.util.pkg;

import static org.basex.query.util.Err.*;
import static org.basex.query.util.pkg.PkgText.*;
import static org.basex.util.Token.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.basex.core.Context;
import org.basex.core.Prop;
import org.basex.io.IO;
import org.basex.io.IOContent;
import org.basex.io.IOFile;
import org.basex.query.QueryException;
import org.basex.query.func.FNZip;
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

    // Check repository
    createRepo();
    // Check package existence
    final File pkgFile = new File(path);
    if(!pkgFile.exists()) PKGNOTEXIST.thrw(ii, path);
    // Check package name - must be a .xar file
    checkPkgName(path, ii);

    try {
      final ZipFile xar = new ZipFile(pkgFile);
      final byte[] cont = FNZip.read(xar, DESCRIPTOR);
      final Package pkg = new PkgParser(ctx, ii).parse(new IOContent(cont));
      new PkgValidator(ctx, ii).check(pkg);
      unzip(xar);
      ctx.repo.add(pkg, extractPkgName(xar.getName()));
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
          // Check if package to be deleted participates in a dependency
          final byte[] primPkg = getPrimary(nextPkg, ii);
          if(primPkg == null) {
            // Clean package repository
            final File f = new File(ctx.prop.get(Prop.REPOPATH), string(dir));
            final File desc = new File(f, DESCRIPTOR);
            ctx.repo.remove(new PkgParser(ctx, ii).parse(new IOFile(desc)));
            // Package does not participate in a dependency => delete it
            deleteFromDisc(f, ii);
          } else PKGDEP.thrw(ii, string(primPkg), pkg);
        }
      }
    }
    if(!found) PKGNOTINST.thrw(ii, pkg);
  }

  /**
   * Checks if repository already exists and if not creates it.
   */
  private void createRepo() {
    repoPath().mkdirs();
  }

  /**
   * Unzips a package in the package repository.
   * @param xar package archive
   * @throws IOException I/O exception
   */
  private void unzip(final ZipFile xar) throws IOException {
    final File dir = new File(repoPath(), extractPkgName(xar.getName()));
    dir.mkdir();

    final Enumeration<? extends ZipEntry> en = xar.entries();
    while(en.hasMoreElements()) {
      final ZipEntry entry = en.nextElement();
      final File f = new File(dir, entry.getName());
      if(entry.isDirectory()) {
        f.mkdirs();
      } else {
        f.getParentFile().mkdirs();
        final OutputStream out = new FileOutputStream(f);
        final InputStream in = xar.getInputStream(entry);
        try {
          final byte[] data = new byte[IO.BLOCKSIZE];
          for(int c; (c = in.read(data)) != -1;)
            out.write(data, 0, c);
        } finally {
          try {
            out.close();
          } catch(final IOException e) { }
          try {
            in.close();
          } catch(final IOException e) { }
        }
      }
    }
  }

  /**
   * Returns the path to the repository.
   * @return repository path
   */
  private File repoPath() {
    return new File(ctx.prop.get(Prop.REPOPATH));
  }

  /**
   * Checks if package to be installed is a .xar archive.
   * @param pkgName package name
   * @param ii input info
   * @throws QueryException query exception
   */
  private static void checkPkgName(final String pkgName, final InputInfo ii)
      throws QueryException {

    if(!pkgName.endsWith(IO.XARSUFFIX)) INVPKGNAME.thrw(ii);
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
  private byte[] getPrimary(final byte[] pkgName, final InputInfo ii)
      throws QueryException {
    for(final byte[] nextPkg : ctx.repo.pkgDict()) {
      if(nextPkg != null && !eq(nextPkg, pkgName)) {
        // Check only packages different from the current one
        final File desc = new File(new File(ctx.prop.get(Prop.REPOPATH),
            string(ctx.repo.pkgDict().get(nextPkg))), DESCRIPTOR);
        final Package pkg = new PkgParser(ctx, ii).parse(new IOFile(desc));
        for(final Dependency dep : pkg.dep)
          if(eq(dep.pkg, Package.getName(pkgName)))
            return Package.getName(nextPkg);
      }
    }
    return null;
  }

  /**
   * Deletes a package recursively.
   * @param dir package directory
   * @param ii input info
   * @throws QueryException query exception
   */
  private void deleteFromDisc(final File dir, final InputInfo ii)
      throws QueryException {
    final File[] files = dir.listFiles();
    if(files != null) for(final File f : files) deleteFromDisc(f, ii);
    if(!dir.delete()) CANNOTDELPKG.thrw(ii);
  }


}
