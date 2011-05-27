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

  /** Context. */
  private Context ctx;
  /** Input info. */
  private InputInfo ii;

  /**
   * Constructor.
   * @param c context
   * @param input input info
   */
  public RepoManager(final Context c, final InputInfo input) {
    ctx = c;
    ii = input;
  }

  /**
   * Installs a new package.
   * @param path package path
   * @throws QueryException query exception
   */
  public void installPackage(final String path) throws QueryException {
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
      unzip(xar);
    } catch(IOException e) {
      throw PKGREADFAIL.thrw(ii);
    }
  }

  /**
   * Uninstalls a package.
   */
  public static void removePackage() {}

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
   */
  private void unzip(final ZipFile xar) {
    try {
      String dirPath = ctx.prop.get(Prop.REPOPATH) + Prop.DIRSEP
          + extractPkgName(xar.getName());
      new File(dirPath).mkdir();
      BufferedOutputStream out = null;
      BufferedInputStream in = null;
      ZipEntry entry;
      @SuppressWarnings("rawtypes")
      Enumeration e = xar.entries();
      while(e.hasMoreElements()) {
        entry = (ZipEntry) e.nextElement();
        if(entry.isDirectory()) {
          new File(dirPath + Prop.DIRSEP + entry.getName()).mkdir();
        } else {
          in = new BufferedInputStream(xar.getInputStream(entry));
          int c;
          byte[] data = new byte[IO.BLOCKSIZE];
          File f = new File(dirPath + Prop.DIRSEP + entry.getName());
          out = new BufferedOutputStream(new FileOutputStream(f), IO.BLOCKSIZE);
          while((c = in.read(data)) != -1) {
            out.write(data, 0, c);
          }
        }
      }
      out.flush();
      out.close();
      in.close();
    } catch(Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * Extracts package name from package path.
   * @param pkgPath package path
   * @return package name
   */
  private String extractPkgName(final String pkgPath) {
    final int idx = pkgPath.lastIndexOf(Prop.DIRSEP);
    return idx == -1 ? pkgPath : pkgPath.substring(idx + 1,
        pkgPath.length() - 4); // 4 for .xar
  }
}
