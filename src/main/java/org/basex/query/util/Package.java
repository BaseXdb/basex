package org.basex.query.util;

import static org.basex.util.Token.*;

import java.util.ArrayList;
import java.util.List;

import org.basex.core.Prop;
import org.basex.util.TokenBuilder;

/**
 * Package.
 * @author BaseX Team 2005-11, BSD License
 * @author Rositsa Shadura
 */
public final class Package {
  /** Separator between name and version in unique package name. */
  private static final char NAMESEP = '-';
  /** Package uri. */
  public byte[] uri;
  /** Package short name. */
  public byte[] abbrev;
  /** Package version. */
  public byte[] version;
  /** Version of packaging specification the package conforms to. */
  public byte[] spec;
  /** Package description. */
  public byte[] title;
  /** URI to find more information about the package. */
  public byte[] home;
  /** List of dependencies. */
  public final List<Dependency> dep = new ArrayList<Package.Dependency>();
  /** Package components. */
  public final List<Component> comps = new ArrayList<Package.Component>();

  /**
   * Returns unique package name consisting of package uri and package version.
   * @return result
   */
  public byte[] getName() {
    return new TokenBuilder().add(uri).add(NAMESEP).add(version).finish();
  }

  /**
   * Extracts package name form unique package name.
   * @param pkgName unique package name: name-version
   * @return package name
   */
  public static byte[] getPkgName(final byte[] pkgName) {
    final int idx = lastIndexOf(pkgName, NAMESEP);
    return idx == -1 ? pkgName : subtoken(pkgName, 0, idx);
  }

  /**
   * Extracts package version from unique package name.
   * @param pkgName unique package name: name-version
   * @return package version
   */
  public static byte[] getPkgVersion(final byte[] pkgName) {
    final int idx = lastIndexOf(pkgName, NAMESEP);
    return subtoken(pkgName, idx + 1, pkgName.length);
  }

  /**
   * Package dependency.
   * @author BaseX Team 2005-11, BSD License
   * @author Rositsa Shadura
   */
  public static class Dependency {
    /** Name of package a package depends on. */
    public byte[] pkg;
    /** Name of processor a package depends on. */
    public byte[] processor;
    /** Package version. */
    public byte[] versions;
    /** SemVer template. */
    public byte[] semver;
    /** Minimum acceptable version. */
    public byte[] semverMin;
    /** Maximum acceptable version. */
    public byte[] semverMax;
  }

  /**
   * Package component.
   * @author BaseX Team 2005-11, BSD License
   * @author Rositsa Shadura
   */
  public static class Component {
    /** Component type. */
    public byte[] type;
    /** Namespace URI. */
    public byte[] namespace;
    /** Public import URI. */
    public byte[] importUri;
    /** Component file. */
    public byte[] file;

    /**
     * Extracts component's file name from component's path.
     * @return component's name
     */
    public String getName() {
      final String compPath = string(file);
      final int idx = compPath.lastIndexOf(Prop.DIRSEP);
      return idx == -1 ? compPath : compPath.substring(idx,
          compPath.length() - 1);
    }
  }
}
