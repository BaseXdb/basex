package org.basex.query.util;

import java.util.ArrayList;
import java.util.List;

import org.basex.util.TokenBuilder;

/**
 * Package.
 * @author BaseX Team 2005-11, BSD License
 * @author Rositsa Shadura
 */
public final class Package {

  /** Package name - unique URI. */
  public byte[] name;
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
    return new TokenBuilder().add(name).add(version).finish();
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
  }

}
