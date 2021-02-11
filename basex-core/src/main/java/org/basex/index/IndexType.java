package org.basex.index;

import static org.basex.query.QueryError.*;

import java.util.*;

import org.basex.data.*;
import org.basex.query.*;
import org.basex.util.*;

/**
 * This enumeration lists available index types.
 *
 * @author BaseX Team 2005-21, BSD License
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

  /**
   * Checks if the specified database has this index.
   * @param data data reference
   * @param ii input info
   * @throws QueryException query exception
   */
  public void check(final Data data, final InputInfo ii) throws QueryException {
    if(!data.meta.index(this)) throw DB_NOINDEX_X_X.get(ii, data.meta.name, this);
  }
}
