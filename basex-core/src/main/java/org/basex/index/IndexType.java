package org.basex.index;

import java.util.*;

/**
 * This enumeration lists available index types.
 *
 * @author BaseX Team 2005-17, BSD License
 * @author Christian Gruen
 */
public enum IndexType {
  /** Element names. */
  ELEMNAME,
  /** Attribute names. */
  ATTRNAME,
  /** Path index. */
  PATH,
  /** Text index. */
  TEXT,
  /** Attribute index. */
  ATTRIBUTE,
  /** Token index. */
  TOKEN,
  /** Full-text index. */
  FULLTEXT;

  @Override
  public String toString() {
    return name().toLowerCase(Locale.ENGLISH);
  }
}
