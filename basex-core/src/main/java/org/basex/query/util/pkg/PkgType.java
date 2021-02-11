package org.basex.query.util.pkg;

/**
 * Package type.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public enum PkgType {
  /** XQuery.   */ XQUERY("XQuery"),
  /** Java.     */ JAVA("Java"),
  /** Combined. */ COMBINED("Combined"),
  /** EXPath.   */ EXPATH("EXPath");

  /** Name of type. */
  private final String name;

  /**
   * Constructor.
   * @param name name
   */
  PkgType(final String name) {
    this.name = name;
  }

  @Override
  public String toString() {
    return name;
  }
}
