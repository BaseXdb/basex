package org.basex.query.util;

import static org.basex.util.Token.*;

import java.util.regex.*;

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
  /** EQName syntax. */
  private static final Pattern EQNAME = Pattern.compile("Q\\{([^{}]*)\\}(.*)$");
  /** Cached QNames. */
  private final TokenObjMap<QNm> cache = new TokenObjMap<>();

  /**
   * Returns a QName.
   * @param name local name with optional prefix
   * @param uri URI (can be {@code null})
   * @return QName
   */
  public QNm get(final byte[] name, final byte[] uri) {
    return cache.computeIfAbsent(
      uri != null ? Token.concat(name, Token.SPACE, uri) : name,
      () -> new QNm(name, uri)
    );
  }

  /**
   * Returns a QName.
   * @param eqname EQname string
   * @return QName or {@code null}
   */
  public QNm get(final byte[] eqname) {
    final Matcher m = EQNAME.matcher(string(eqname));
    if(m.matches()) {
      final byte[] ncname = token(m.group(2));
      if(XMLToken.isNCName(ncname)) return get(ncname, token(m.group(1)));
    }
    return null;
  }
}