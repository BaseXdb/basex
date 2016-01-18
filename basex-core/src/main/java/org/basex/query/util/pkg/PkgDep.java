package org.basex.query.util.pkg;

/**
 * Package dependency.
 *
 * @author BaseX Team 2005-16, BSD License
 * @author Rositsa Shadura
 */
final class PkgDep {
  /** Name of package a package depends on. */
  String name;
  /** Name of processor a package depends on. */
  String processor;
  /** Set of acceptable version. */
  String versions;
  /** SemVer template. */
  String semver;
  /** Minimum acceptable version. */
  String semverMin;
  /** Maximum acceptable version. */
  String semverMax;

  /**
   * Returns the unique package name, suffixed by the specified version.
   * @param version version
   * @return unique name
   */
  String id(final String version) {
    return name + '-' + version;
  }
}
