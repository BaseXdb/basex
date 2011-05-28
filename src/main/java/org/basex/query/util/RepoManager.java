package org.basex.query.util;

import static org.basex.query.util.Err.*;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.basex.core.Context;
import org.basex.core.Prop;
import org.basex.io.IO;
import org.basex.io.IOContent;
import org.basex.query.QueryException;
import org.basex.util.InputInfo;

/**
 * Repository manager.
 * @author BaseX Team 2005-11, BSD License
 * @author Rositsa Shadura
 */
public final class RepoManager {
  /** Constructor. */
  private RepoManager() { }

  /**
   * Installs a new package.
   * @param path package path
   * @param ctx context
   * @param ii input info
   * @throws QueryException query exception
   */
  public static void installPackage(final String path, final Context ctx,
      final InputInfo ii) throws QueryException {
    // Check repository
    checkRepo(ctx);
    // Check package existence
    final File pkgFile = new File(path);
    if(!pkgFile.exists()) PKGNOTEXIST.thrw(ii);
    // Check package name - must be a .xar file
    checkPkgName(path, ii);
    try {
      final ZipFile xar = new ZipFile(pkgFile);
      final Package pkg = PackageParser.parse(
          new IOContent(PackageParser.readPkgDesc(xar, ii)), ctx, ii);
      PkgValidator.check(pkg, ctx, ii);
      unzip(xar, ctx);
    } catch(IOException e) {
      throw PKGREADFAIL.thrw(ii);
    }
  }

  /**
   * Uninstalls a package.
   */
  public static void removePackage() { }

  /**
   * Checks if repository already exists and if not creates it.
   * @param ctx context
   */
  private static void checkRepo(final Context ctx) {
    final File repo = new File(ctx.prop.get(Prop.REPOPATH));
    if(!repo.exists()) repo.mkdir();
  }

  /**
   * Checks if package to be installed is a .xar archive.
   * @param pkgName package name
   * @param ii input info
   * @throws QueryException query exception
   */
  private static void checkPkgName(final String pkgName, final InputInfo ii)
      throws QueryException {
    if(!pkgName.endsWith(".xar")) INVPKGNAME.thrw(ii);
  }

  /**
   * Unzips a package in the package repository.
   * @param xar package archive
   * @param ctx context
   */
  private static void unzip(final ZipFile xar, final Context ctx) {
    try {
      final String dirPath = ctx.prop.get(Prop.REPOPATH) + Prop.DIRSEP
          + extractPkgName(xar.getName());
      new File(dirPath).mkdir();
      final Enumeration<? extends ZipEntry> e = xar.entries();
      while(e.hasMoreElements()) {
        final ZipEntry entry = e.nextElement();
        if(entry.isDirectory()) {
          new File(dirPath + Prop.DIRSEP + entry.getName()).mkdir();
        } else {
          final BufferedInputStream in = new BufferedInputStream(
              xar.getInputStream(entry));
          final File f = new File(dirPath + Prop.DIRSEP + entry.getName());
          final BufferedOutputStream out = new BufferedOutputStream(
              new FileOutputStream(f), IO.BLOCKSIZE);
          try {
            int c;
            final byte[] data = new byte[IO.BLOCKSIZE];
            while((c = in.read(data)) != -1)
              out.write(data, 0, c);
          } finally {
            out.close();
            in.close();
          }
        }
      }
    } catch(Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * Extracts package name from package path.
   * @param pkgPath package path
   * @return package name
   */
  private static String extractPkgName(final String pkgPath) {
    final int idx = pkgPath.lastIndexOf(Prop.DIRSEP);
    return idx == -1 ? pkgPath : pkgPath.substring(idx + 1,
        pkgPath.length() - 4); // 4 for .xar
  }
}
