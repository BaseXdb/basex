package org.basex.query.util.pkg;

import static org.basex.util.Token.*;

import java.io.*;
import java.util.*;

import org.basex.util.*;

/**
 * Package.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Rositsa Shadura
 */
public final class Package {
  /** List of dependencies. */
  public final ArrayList<Dependency> dep = new ArrayList<>();
  /** Package components. */
  public final ArrayList<Component> comps = new ArrayList<>();
  /** Package short name. */
  public byte[] abbrev;
  /** Package uri. */
  byte[] name;
  /** Package version. */
  byte[] version;
  /** Version of packaging specification the package conforms to. */
  byte[] spec;

  /**
   * Returns a unique package name, consisting of the package URI and its version,
   * separated by a hyphen.
   * @return result
   */
  byte[] uniqueName() {
    return new TokenBuilder(name).add('-').add(version).finish();
  }

  /**
   * Extracts the package name from a unique package name.
   * @param pkg unique package name: name-version
   * @return package name
   */
  public static byte[] name(final byte[] pkg) {
    final int idx = lastIndexOf(pkg, '-');
    return idx == -1 ? pkg : subtoken(pkg, 0, idx);
  }

  /**
   * Extracts the package version from a unique package name.
   * @param pkg unique package name: name-version
   * @return package version
   */
  public static byte[] version(final byte[] pkg) {
    final int idx = lastIndexOf(pkg, '-');
    return subtoken(pkg, idx + 1, pkg.length);
  }

  /**
   * Package dependency.
   * @author BaseX Team 2005-14, BSD License
   * @author Rositsa Shadura
   */
  static final class Dependency {
    /** Name of package a package depends on. */
    byte[] pkg;
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
    byte[] name(final byte[] version) {
      return new TokenBuilder(pkg).add('-').add(version).finish();
    }
  }

  /**
   * Package component.
   * @author BaseX Team 2005-14, BSD License
   * @author Rositsa Shadura
   */
  static final class Component {
    /** Namespace URI. */
    byte[] uri;
    /** Component file. */
    byte[] file;

    /**
     * Extracts component's file name from component's path.
     * @return component's name
     */
    String name() {
      final String path = string(file);
      final int i = path.lastIndexOf(File.separator);
      return i == -1 ? path : path.substring(i + 1, path.length());
    }
  }
}
