package org.basex.query.util;

import org.basex.query.value.item.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * QName pool.
 *
 * @author BaseX Team 2005-23, BSD License
 * @author Christian Gruen
 */
public final class QNmPool {
  /** Cached QNames. */
  private final TokenObjMap<QNm> cache = new TokenObjMap<>();

  /**
   * Returns a QName.
   * @param name name
   * @param uri URI (can be {@code null})
   * @return QName
   */
  public QNm get(final byte[] name, final byte[] uri) {
    return cache.computeIfAbsent(
      uri != null ? Token.concat(name, Token.SPACE, uri) : name,
      () -> new QNm(name, uri)
    );
  }
}
