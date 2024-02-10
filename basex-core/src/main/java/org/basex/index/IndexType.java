package org.basex.index;

import static org.basex.query.QueryError.*;

import org.basex.data.*;
import org.basex.query.*;
import org.basex.util.*;
import org.basex.util.options.*;

/**
 * This enumeration lists available index types.
 *
 * @author BaseX Team 2005-24, BSD License
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
    return EnumOption.string(name());
  }

  /**
   * Checks if the specified database has this index.
   * @param data data reference
   * @param info input info (can be {@code null})
   * @throws QueryException query exception
   */
  public void check(final Data data, final InputInfo info) throws QueryException {
    if(!data.meta.index(this)) throw DB_NOINDEX_X_X.get(info, data.meta.name, this);
  }
}
