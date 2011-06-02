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
final class Package {
  /** Separator between name and version in unique package name. */
  private static final char NAMESEP = '-';

  /** List of dependencies. */
  final List<Dependency> dep = new ArrayList<Dependency>();
  /** Package components. */
  final List<Component> comps = new ArrayList<Component>();
  /** Package uri. */
  byte[] name;
  /** Package short name. */
  byte[] abbrev;
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
  byte[] getName() {
    return new TokenBuilder(name).add(NAMESEP).add(version).finish();
  }

  /**
   * Extracts package name from unique package name.
   * @param pkgName unique package name: name-version
   * @return package name
   */
  static byte[] getName(final byte[] pkgName) {
    final int idx = lastIndexOf(pkgName, NAMESEP);
    return idx == -1 ? pkgName : subtoken(pkgName, 0, idx);
  }

  /**
   * Extracts package version from unique package name.
   * @param pkgName unique package name: name-version
   * @return package version
   */
  static byte[] getVersion(final byte[] pkgName) {
    final int idx = lastIndexOf(pkgName, NAMESEP);
    return subtoken(pkgName, idx + 1, pkgName.length);
  }

  /**
   * Package dependency.
   * @author BaseX Team 2005-11, BSD License
   * @author Rositsa Shadura
   */
  static final class Dependency {
    /** Name of package a package depends on. */
    byte[] pkg;
    /** Name of processor a package depends on. */
    byte[] processor;
    /** Package version. */
    byte[] versions;
    /** SemVer template. */
    byte[] semver;
    /** Minimum acceptable version. */
    byte[] semverMin;
    /** Maximum acceptable version. */
    byte[] semverMax;
  }

  /**
   * Package component.
   * @author BaseX Team 2005-11, BSD License
   * @author Rositsa Shadura
   */
  static final class Component {
    /** Component type. */
    byte[] type;
    /** Namespace URI. */
    byte[] namespace;
    /** Public import URI. */
    byte[] importUri;
    /** Component file. */
    byte[] file;

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
