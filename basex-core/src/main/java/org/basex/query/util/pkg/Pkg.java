package org.basex.query.util.pkg;

import java.util.*;

/**
 * EXPath or internal package. Internal packages have no version.
 *
 * @author BaseX Team 2005-17, BSD License
 * @author Rositsa Shadura
 */
public final class Pkg {
  /** List of dependencies. */
  final ArrayList<PkgDep> dep = new ArrayList<>();
  /** Package components. */
  final ArrayList<PkgComponent> comps = new ArrayList<>();

  /** Package version ({@code null} if this is not an EXPath package). */
  String version;
  /** Package short name. */
  String abbrev;
  /** Package uri. */
  String name;
  /** Version of packaging specification the package conforms to. */
  String spec;
  /** Package directory. */
  String dir;

  /**
   * Indicates if this is an EXPath package.
   * @return result of check
   */
  public boolean expath() {
    return version != null;
  }

  /**
   * Returns the package id, consisting of the package name/URI and, optionally, its version.
   * @return id
   */
  public String id() {
    return expath() ? (name + '-' + version) : name;
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
   * @return version ("{@code -}" if this is no EXPath type)
   */
  public String version() {
    return expath() ? version : "-";
  }

  /**
   * Returns the package type (EXPath/internal).
   * @return package type
   */
  public String type() {
    return expath() ? PkgText.EXPATH : PkgText.INTERNAL;
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
