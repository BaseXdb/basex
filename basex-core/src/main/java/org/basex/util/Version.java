package org.basex.util;

import static org.basex.util.Token.*;

/**
 * Version according to the SemVer syntax.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Rositsa Shadura
 */
public final class Version implements Comparable<Version> {
  /** Major version. */
  private final int major;
  /** Minor version. */
  private final int minor;
  /** Patch version. */
  private final int patch;

  /**
   * Constructor.
   * @param version according to semantic versioning
   */
  public Version(final String version) {
    this(token(version));
  }

  /**
   * Constructor.
   * @param version according to semantic versioning
   */
  public Version(final byte[] version) {
    final byte[][] versions = split(version, '.');
    major = toInt(versions[0]);
    minor = versions.length > 1 ? toInt(versions[1]) : -1;
    patch = versions.length > 2 ? toInt(versions[2]) : -1;
  }

  /**
   * Checks if this version is compatible with the given version template.
   * @param ver version template
   * @return result
   */
  public boolean isCompatible(final Version ver) {
    if(major != ver.major) return false;
    if(ver.minor == -1) return true;
    if(ver.patch == -1) return minor == ver.minor;
    return minor == ver.minor && patch == ver.patch;
  }

  @Override
  public int compareTo(final Version ver) {
    return major == ver.major ? minor == ver.minor ? patch - ver.patch : minor - ver.minor :
      major - ver.major;
  }

  @Override
  public boolean equals(final Object o) {
    return o instanceof Version && compareTo((Version) o) == 0;
  }

  @Override
  public int hashCode() {
    return major + (minor << 8) + (patch << 16);
  }

  @Override
  public String toString() {
    return major + (minor == -1 ? "" : "." + minor +
        (patch == -1 ? "" : "." + patch));
  }
}
