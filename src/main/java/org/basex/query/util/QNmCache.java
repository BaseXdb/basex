package org.basex.query.util;

import java.util.*;

import org.basex.query.value.item.*;
import org.basex.util.hash.*;

/**
 * This class caches frequently used QNames.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Christian Gruen
 */
public final class QNmCache extends TokenSet {
  /** QNames. */
  private QNm[] values = new QNm[CAP];

  /**
   * Creates a QName for the specified key, or returns an existing one.
   * @param prefix prefix (may be {@code null)}
   * @param local local name
   * @param uri namespace uri (may be {@code null)}
   * @return name
   */
  public QNm get(final byte[] prefix, final byte[] local, final byte[] uri) {
    final int i = add(QNm.internal(prefix, local, uri));
    if(i < 0) return values[-i];

    final byte[] nm = QNm.internal(prefix, local, null);
    final QNm qn = uri == null ? new QNm(nm) : new QNm(nm, uri);
    values[i] = qn;
    return qn;
  }

  @Override
  protected void rehash() {
    super.rehash();
    values = Arrays.copyOf(values, size << 1);
  }
}
