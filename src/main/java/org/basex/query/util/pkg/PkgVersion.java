package org.basex.query.util.pkg;

import static org.basex.util.Token.*;

import org.basex.util.Token;

/**
 * Version according to the SemVer syntax.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Rositsa Shadura
 */
public final class PkgVersion implements Comparable<PkgVersion> {
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
  public PkgVersion(final byte[] version) {
    final byte[][] versions = split(version, '.');
    major = Token.toInt(versions[0]);
    minor = versions.length > 1 ? Token.toInt(versions[1]) : -1;
    patch = versions.length > 2 ? Token.toInt(versions[2]) : -1;
  }

  /**
   * Checks if this version is compatible with the given version template.
   * @param ver version template
   * @return result
   */
  public boolean isCompatible(final PkgVersion ver) {
    if(major != ver.major) return false;
    if(ver.minor == -1) return true;
    if(ver.patch == -1) return minor == ver.minor;
    return minor == ver.minor && patch == ver.patch;
  }

  @Override
  public int compareTo(final PkgVersion ver) {
    return major != ver.major ? major - ver.major :
           minor != ver.minor ? minor - ver.minor : patch - ver.patch;
  }
}
