package org.basex.query.util.repo;

import static org.basex.util.Token.*;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.basex.util.TokenBuilder;

/**
 * Package.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Rositsa Shadura
 */
public final class Package {
  /** Separator between name and version in unique package name. */
  private static final char NAMESEP = '-';

  /** List of dependencies. */
  public final List<Dependency> dep = new ArrayList<Dependency>();
  /** Package components. */
  public final List<Component> comps = new ArrayList<Component>();
  /** Package uri. */
  byte[] name;
  /** Package short name. */
  public byte[] abbrev;
  /** Package version. */
  byte[] version;
  /** Version of packaging specification the package conforms to. */
  byte[] spec;
  /** Package description. */
  byte[] title;
  /** URI to find more information about the package. */
  byte[] home;

  /**
   * Returns unique package name consisting of package uri and package version.
   * @return result
   */
  byte[] getUniqueName() {
    return new TokenBuilder(name).add(NAMESEP).add(version).finish();
  }

  /**
   * Extracts package name from unique package name.
   * @param pkgName unique package name: name-version
   * @return package name
   */
  public static byte[] getName(final byte[] pkgName) {
    final int idx = lastIndexOf(pkgName, NAMESEP);
    return idx == -1 ? pkgName : subtoken(pkgName, 0, idx);
  }

  /**
   * Extracts package version from unique package name.
   * @param pkgName unique package name: name-version
   * @return package version
   */
  public static byte[] getVersion(final byte[] pkgName) {
    final int idx = lastIndexOf(pkgName, NAMESEP);
    return subtoken(pkgName, idx + 1, pkgName.length);
  }

  /**
   * Package dependency.
   * @author BaseX Team 2005-11, BSD License
   * @author Rositsa Shadura
   */
  public static final class Dependency {
    /** Name of package a package depends on. */
    public byte[] pkg;
    /** Name of processor a package depends on. */
    byte[] processor;
    /** Set of acceptable version. */
    byte[] versions;
    /** SemVer template. */
    byte[] semver;
    /** Minimum acceptable version. */
    byte[] semverMin;
    /** Maximum acceptable version. */
    byte[] semverMax;

    /**
     * Returns unique package name for secondary package using the given
     * version.
     * @param version version
     * @return unique name
     */
    public byte[] getName(final byte[] version) {
      final TokenBuilder tb = new TokenBuilder();
      return tb.add(pkg).add(NAMESEP).add(version).finish();
    }
  }

  /**
   * Package component.
   * @author BaseX Team 2005-11, BSD License
   * @author Rositsa Shadura
   */
  public static final class Component {
    /** Component type. */
    byte[] type;
    /** Namespace URI. */
    public byte[] namespace;
    /** Public import URI. */
    byte[] importUri;
    /** Component file. */
    public byte[] file;

    /**
     * Extracts component's file name from component's path.
     * @return component's name
     */
    String getName() {
      final String path = string(file);
      final int i = path.lastIndexOf(File.separator);
      return i == -1 ? path : path.substring(i + 1, path.length());
    }
  }
}
