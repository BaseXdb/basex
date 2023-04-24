package org.basex.query.util;

import static org.basex.util.Token.*;

import java.util.regex.*;

import org.basex.query.value.item.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Shared data references.
 *
 * @author BaseX Team 2005-23, BSD License
 * @author Christian Gruen
 */
public final class SharedData {
  /** EQName syntax. */
  private static final Pattern EQNAME = Pattern.compile("Q\\{([^{}]*)\\}(.*)$");
  /** Cached QNames. */
  private final TokenObjMap<QNm> qnames = new TokenObjMap<>();
  /** Cached tokens. */
  private final TokenSet tokens = new TokenSet();

  /**
   * Returns a shared QName.
   * @param name local name with optional prefix
   * @return QName
   */
  public QNm qname(final byte[] name) {
    return qname(name, null);
  }

  /**
   * Returns a shared QName.
   * @param name local name with optional prefix
   * @param uri URI (can be {@code null})
   * @return QName
   */
  public QNm qname(final byte[] name, final byte[] uri) {
    return qnames.computeIfAbsent(
      uri != null ? Token.concat(name, Token.SPACE, uri) : name,
      () -> new QNm(name, uri)
    );
  }

  /**
   * Returns a shared QName.
   * @param eqname EQname string
   * @return QName or {@code null}
   */
  public QNm eqname(final byte[] eqname) {
    final Matcher m = EQNAME.matcher(string(eqname));
    if(m.matches()) {
      final byte[] ncname = Token.token(m.group(2)), uri = Token.token(m.group(1));
      if(XMLToken.isNCName(ncname)) return qname(ncname, uri);
    }
    return null;
  }

  /**
   * Returns a shared token.
   * @param token token to be cached
   * @return shared token
   */
  public byte[] token(final byte[] token) {
    return token.length == 0 ? Token.EMPTY : tokens.key(tokens.put(token));
  }
}
