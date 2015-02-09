package org.basex.query.util.hash;

import org.basex.query.value.item.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * This class caches frequently used QNames.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
public final class QNmMap extends TokenSet {
  /** QNames. */
  private QNm[] values = new QNm[Array.CAPACITY];

  /**
   * Creates a QName for the specified key, or returns an existing one.
   * @param prefix prefix (can be {@code null)}
   * @param local local name
   * @param uri namespace uri (can be {@code null)}
   * @return name
   */
  public QNm index(final byte[] prefix, final byte[] local, final byte[] uri) {
    final int i = put(QNm.internal(prefix, local, uri));
    QNm value = values[i];
    if(value == null) {
      final byte[] nm = QNm.internal(prefix, local, null);
      value = uri == null ? new QNm(nm) : new QNm(nm, uri);
      values[i] = value;
    }
    return value;
  }

  @Override
  protected void rehash(final int s) {
    super.rehash(s);
    values = Array.copy(values, new QNm[s]);
  }
}
