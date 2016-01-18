package org.basex.query.util.pkg;

import java.util.*;

/**
 * Package.
 *
 * @author BaseX Team 2005-16, BSD License
 * @author Rositsa Shadura
 */
public final class Pkg {
  /** List of dependencies. */
  final ArrayList<PkgDep> dep = new ArrayList<>();
  /** Package components. */
  final ArrayList<PkgComponent> comps = new ArrayList<>();

  /** Package short name. */
  String abbrev;
  /** Package uri. */
  String name;
  /** Package version. */
  String version;
  /** Version of packaging specification the package conforms to. */
  String spec;
  /** Package directory. */
  String dir;

  /**
   * Returns the package id, consisting of the package URI and its version.
   * @return id
   */
  String id() {
    return version == null ? name : (name + '-' + version);
  }

  /**
   * Returns the package name.
   * @return name
   */
  public String name() {
    return name;
  }

  /**
   * Returns the short name.
   * @return short name
   */
  public String abbrev() {
    return abbrev;
  }

  /**
   * Returns the package spec.
   * @return spec
   */
  public String spec() {
    return spec;
  }

  /**
   * Returns the package directory.
   * @return directory
   */
  public String dir() {
    return dir;
  }

  /**
   * Returns the package version.
   * @return version ({@code null} if package is an internal one)
   */
  public String version() {
    return version;
  }

  /**
   * Assigns the package directory.
   * @param d directory
   * @return self reference
   */
  Pkg dir(final String d) {
    dir = d;
    return this;
  }

  /**
   * Extracts the package name from a unique package name.
   * @param pkg unique package name: name-version
   * @return package name
   */
  public static String name(final String pkg) {
    final int idx = pkg.lastIndexOf('-');
    return idx == -1 ? pkg : pkg.substring(0, idx);
  }

  /**
   * Extracts the package version from a unique package name.
   * @param pkg unique package name: name-version
   * @return package version
   */
  public static String version(final String pkg) {
    final int idx = pkg.lastIndexOf('-');
    return pkg.substring(idx + 1, pkg.length());
  }
}
