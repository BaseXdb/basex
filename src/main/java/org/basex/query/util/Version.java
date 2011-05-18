package org.basex.query.util;

import static org.basex.util.Token.*;

import org.basex.util.Token;

/**
 * Version according the the SemVer syntax.
 * @author BaseX Team 2005-11, BSD License
 * @author Rositsa Shadura
 */
public final class Version implements Comparable<Version> {

  /** Major version. */
  public int major;
  /** Minor version. */
  public int minor;
  /** Patch version. */
  public int patch;

  /**
   * Constructor.
   * @param version according to semantic versioning
   */
  public Version(final byte[] version) {
    final byte[][] versions = split(version, '.');
    major = Token.toInt(versions[0]);
    minor = versions.length > 1 ? Token.toInt(versions[1]) : -1;
    patch = versions.length > 2 ? Token.toInt(versions[2]) : -1;
  }

  /**
   * Checks if this version is compatible with the given version template.
   * @param semVer version template
   * @return result
   */
  public boolean isCompatible(final Version semVer) {
    if(major == semVer.major) {
      if(semVer.minor != -1) {
        if(semVer.patch == -1) return minor == semVer.minor;
        return minor == semVer.minor && patch == semVer.patch;
      }
      return true;
    }
    return false;
  }

  @Override
  public int compareTo(final Version v) {
    if(major == v.major) {
      if(v.minor != -1) {
        if(v.patch != -1) {
          if(minor == v.minor) return patch > v.patch ? 1
              : patch < v.patch ? -1 : 0;
          return minor > v.minor ? 1 : -1;
        }
        return minor > v.minor ? 1 : minor < v.minor ? -1 : 0;
      }
      return minor == -1 ? 0 : 1;
    }
    return major > v.major ? 1 : -1;
  }
}
