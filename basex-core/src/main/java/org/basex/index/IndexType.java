package org.basex.index;

import java.util.*;

/**
 * This enumeration lists available index types.
 *
 * @author BaseX Team 2005-16, BSD License
 * @author Christian Gruen
 */
public enum IndexType {
  /** Attribute names. */
  ATTRNAME,
  /** Element names. */
  ELEMNAME,
  /** Text index. */
  TEXT,
  /** Attribute index. */
  ATTRIBUTE,
  /** Token index. */
  TOKEN,
  /** Full-text index. */
  FULLTEXT,
  /** Path index. */
  PATH;

  @Override
  public String toString() {
    return name().toLowerCase(Locale.ENGLISH);
  }
}
