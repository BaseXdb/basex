package org.basex.query.util;

import static org.basex.query.util.Err.*;
import static org.basex.util.Token.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.basex.build.MemBuilder;
import org.basex.build.Parser;
import org.basex.core.Context;
import org.basex.io.IO;
import org.basex.io.IOContent;
import org.basex.query.QueryException;
import org.basex.query.item.ANode;
import org.basex.query.item.DBNode;
import org.basex.query.util.Package.Dependency;
import org.basex.util.ByteList;
import org.basex.util.InputInfo;

/**
 * Repositiry manager.
 * @author BaseX Team 2005-11, BSD License
 * @author Rositsa Shadura
 */
public final class RepoManager {

  /** Context. */
  private Context ctx;
  /** Input info. */
  private InputInfo ii;

  /** Package descriptor. */
  private static final String PKGDESC = "expath-pkg.xml";

  /**
   * Constructor.
   * @param c context
   * @param input input info
   */
  private RepoManager(final Context c, final InputInfo input) {
    ctx = c;
    ii = input;
  }

  /**
   * Installs a new package.
   * @param path path to .xar file
   * @throws IOException IO exeception
   * @throws QueryException 
   */
  public void installPackage(final String path) throws IOException, QueryException {
    checkPkgName(path);
    final File pkgFile = new File(path);
    if(!pkgFile.exists()) {
      // TODO: Package archive does not exist
    }
    final ZipFile pkgXar = new ZipFile(pkgFile);

  }

  /***/
  public static void removePackage() {}

  /**
   * Reads the contents of the package descriptor.
   * @param zf xar archive
   * @return contents of package descriptor as byte array
   * @throws QueryException query exception
   */
  private byte[] readPkgDesc(final ZipFile zf) throws QueryException {

    try {
      final ZipEntry ze = zf.getEntry(PKGDESC);
      if(ze == null) {
        // TODO: error "Missing package descriptor."
      }
      final InputStream zis = zf.getInputStream(ze);
      final int s = (int) ze.getSize();
      if(s >= 0) {
        // known size: pre-allocate and fill array
        final byte[] data = new byte[s];
        int c, o = 0;
        while(s - o != 0 && (c = zis.read(data, o, s - o)) != -1)
          o += c;
        return data;
      }
      // unknown size: use byte list
      final byte[] data = new byte[IO.BLOCKSIZE];
      final ByteList bl = new ByteList();
      int c;
      while((c = zis.read(data)) != -1)
        bl.add(data, 0, c);
      return bl.toArray();

    } catch(IOException ex) {
      throw ZIPFAIL.thrw(ii, ex.getMessage());
    } finally {
      if(zf != null) try {
        zf.close();
      } catch(final IOException e) {}
    }
  }

  /**
   * Checks package consistency.
   * @param pkgXar package
   * @throws QueryException query exception
   * @throws IOException IO exception
   */
  private void checkPkg(final ZipFile pkgXar) throws QueryException,
      IOException {
    final IOContent io = new IOContent(readPkgDesc(pkgXar));
    final Parser p = Parser.xmlParser(io, ctx.prop, "");
    final ANode pkgNode = new DBNode(MemBuilder.build(p, ctx.prop, ""), 0).children().next();
    final Package pkg = PackageParser.parse(pkgNode);

    // Check mandatory attributes
    if(pkg.name == null || pkg.abbrev == null || pkg.version == null
        || pkg.spec == null) {
      // TODO: Error: "Package attribute % is missing."
    }

    // Check if package is not already installed
    if(ctx.repo.pkgDict.get(pkg.getName()) != null) {
      // TODO: Error: "Package is already installed."
    }

    // Check package dependencies
    final Iterator<Dependency> depIt = pkg.dep.iterator();
  }

  /**
   * : Checks a package dependency.
   * @param dep dependency
   */
  private void checkDep(final Dependency dep) {
    // Check name of secondary package is provided
    if(dep.pkg == null) {
      // TODO: Error: "Missing name of secondary package."
    }

    // Check version attributes, semver is mutually exclusive with semverMin and
    // semverMax

  }

  private void checkVersions(final Dependency dep) {
    // Get installed versions of secondary package
    final List<byte[]> instVers = getInstalledVersions(dep.pkg);

    if(instVers.size() == 0) {
      // TODO: error: "Secondary package % not installed"
    }
    final Iterator<byte[]> instIt = instVers.iterator();
    boolean found = false;
    if(dep.versions != null) {
      final List<byte[]> accept = getAcceptVersions(dep.versions);
      final Iterator<byte[]> acceptIt = accept.iterator();
      byte[] v;
      // Check if any acceptable version is already installed
      while(acceptIt.hasNext()) {
        v = acceptIt.next();
        if(instVers.contains(v)) {
          found = true;
          break;
        }
      }
    } else if(dep.semver != null) {
      while(instIt.hasNext()) {
        if(new Version(instIt.next()).isCompatible(new Version(dep.semver))) {
          found = true;
          break;
        }
      }
    } else if(dep.semverMin != null && dep.semverMax == null) {
      final Version semVer = new Version(dep.semverMin);
      Version v;
      while(instIt.hasNext()) {
        v = new Version(instIt.next());
        if(v.isCompatible(semVer) || v.compareTo(semVer) >= 0) {
          found = true;
          break;
        }
      }
    } else if(dep.semverMin == null && dep.semverMax != null) {
      final Version semVer = new Version(dep.semverMax);
      Version v;
      while(instIt.hasNext()) {
        v = new Version(instIt.next());
        if(v.isCompatible(semVer) || v.compareTo(semVer) <= 0) {
          found = true;
          break;
        }
      }
    } else if(dep.semverMin != null && dep.semverMax != null) {
      final Version min = new Version(dep.semverMin);
      final Version max = new Version(dep.semverMax);
      Version v;
      while(instIt.hasNext()) {
        v = new Version(instIt.next());
        if(v.compareTo(min) >= 0 && v.compareTo(max) < 0) {
          found = true;
          break;
        }
      }
    }
    if(!found) {
      // TODO: error}
    }
  }

  /**
   * Extracts the acceptable versions for a secondary package in case of
   * dependency.
   * @param versions versions' set
   * @return list with acceptable version
   */
  private List<byte[]> getAcceptVersions(final byte[] versions) {
    final List<byte[]> versList = new ArrayList<byte[]>();
    for(final byte[] v : split(versions, ' ')) {
      versList.add(v);
    }
    return versList;
  }

  /**
   * Returns the installed versions of the given package.
   * @param pkgName package name
   * @return installed versions
   */
  private List<byte[]> getInstalledVersions(final byte[] pkgName) {
    final List<byte[]> versions = new ArrayList<byte[]>();
    final Iterator<byte[]> pkgIt = ctx.repo.pkgDict.iterator();
    byte[] nextPkg;
    while(pkgIt.hasNext()) {
      nextPkg = pkgIt.next();
      if(startsWith(nextPkg, pkgName)) {
        versions.add(getPkgVersion(nextPkg));
      }
    }
    return versions;
  }

  /**
   * Extracts package version from package name.
   * @param pkgName package name
   * @return package version
   */
  private byte[] getPkgVersion(final byte[] pkgName) {
    final int idx = lastIndexOf(pkgName, '-');
    if(idx == -1) return null;
    return subtoken(pkgName, idx, pkgName.length - 1);
  }

  /**
   * Checks if package is a .xar archive.
   * @param pkgName package name
   */
  private void checkPkgName(final String pkgName) {
    if(!pkgName.endsWith(".xar")) {
      // TODO: error "Package must be a .xar file".
    }
  }
}
