package org.basex.query.util.pkg;

import java.util.*;

import org.basex.io.*;

/**
 * EXPath or internal package. Internal packages have no version.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Rositsa Shadura
 */
public final class Pkg {
  /** List of dependencies. */
  final ArrayList<PkgDep> dep = new ArrayList<>();
  /** Package components. */
  final ArrayList<PkgComponent> comps = new ArrayList<>();

  /** Package uri. */
  final String name;

  /** Path to package. */
  private String path;
  /** Package type. */
  PkgType type = PkgType.EXPATH;
  /** Package short name. */
  String abbrev;
  /** Version of packaging specification the package conforms to. */
  String spec;
  /** Package version. */
  String version = "";

  /**
   * Constructor.
   * @param name name of package
   */
  public Pkg(final String name) {
    this.name = name;
  }

  /**
   * Sets the package path and updates the package type.
   * @param pth path
   * @return self reference
   */
  public Pkg path(final String pth) {
    path = pth;
    type = IO.checkSuffix(path, IO.XQSUFFIXES) ? PkgType.XQUERY :
      IO.checkSuffix(path, IO.JARSUFFIX) ? PkgType.JAVA : PkgType.EXPATH;
    return this;
  }

  /**
   * Returns the package id, consisting of the package name/URI and, optionally, its version.
   * @return id
   */
  public String id() {
    return type == PkgType.EXPATH ? (name + '-' + version) : name;
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
   * Returns the path to the package.
   * @return directory
   */
  public String path() {
    return path;
  }

  /**
   * Returns the package version.
   * @return version ("{@code -}" if this is no EXPath type)
   */
  public String version() {
    return type == PkgType.EXPATH ? version : "-";
  }

  /**
   * Returns the package type.
   * @return package type
   */
  public PkgType type() {
    return type;
  }

  /**
   * Merges information of two packages.
   * @param pkg package to merge
   * @return reference to package with merged information
   */
  public Pkg merge(final Pkg pkg) {
    if(IO.checkSuffix(pkg.path, IO.XQSUFFIXES) && IO.checkSuffix(path, IO.JARSUFFIX)) {
      pkg.type = PkgType.COMBINED;
      return pkg;
    }
    if(IO.checkSuffix(path, IO.XQSUFFIXES) && IO.checkSuffix(pkg.path, IO.JARSUFFIX))
      type = PkgType.COMBINED;
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
    return pkg.substring(pkg.lastIndexOf('-') + 1);
  }
}
