package org.basex.query.util.repo;

import static org.basex.query.util.Err.*;
import static org.basex.query.util.repo.PkgText.*;

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
import org.basex.query.QueryException;
import org.basex.query.func.FNZip;
import org.basex.util.InputInfo;

/**
 * Repository manager.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Rositsa Shadura
 */
public final class RepoManager {
  /** Database context. */
  private final Context context;

  /**
   * Constructor.
   * @param ctx database context
   */
  public RepoManager(final Context ctx) {
    context = ctx;
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
    if(!pkgFile.exists()) PKGNOTEXIST.thrw(ii);
    // Check package name - must be a .xar file
    checkPkgName(path, ii);

    try {
      final ZipFile xar = new ZipFile(pkgFile);
      final byte[] cont = FNZip.read(xar, DESCRIPTOR);
      final Package pkg = new PkgParser(context).parse(
          new IOContent(cont), ii);
      new PkgValidator(context).check(pkg, ii);
      unzip(xar);
    } catch(final IOException e) {
      throw PKGREADFAIL.thrw(ii);
    }
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

    final Enumeration<? extends ZipEntry> e = xar.entries();
    while(e.hasMoreElements()) {
      final ZipEntry entry = e.nextElement();
      if(entry.isDirectory()) {
        new File(dir, entry.getName()).mkdir();
      } else {
        final File f = new File(dir, entry.getName());
        final InputStream in = xar.getInputStream(entry);
        final OutputStream out = new FileOutputStream(f);
        try {
          final byte[] data = new byte[IO.BLOCKSIZE];
          for(int c; (c = in.read(data)) != -1;) out.write(data, 0, c);
        } finally {
          out.close();
          in.close();
        }
      }
    }
  }

  /**
   * Returns the path to the repository.
   * @return repository path
   */
  private File repoPath() {
    return new File(context.prop.get(Prop.REPOPATH));
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
}
