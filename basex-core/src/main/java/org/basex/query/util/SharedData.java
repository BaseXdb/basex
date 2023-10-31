package org.basex.query.util;

import java.util.regex.*;

import org.basex.query.*;
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
  /** Cached QNames. */
  private final TokenObjMap<QNm> qnames = new TokenObjMap<>();
  /** Cached tokens. */
  private final WeakTokenSet tokens = new WeakTokenSet();

  /**
   * Parses and returns a shared QName.
   * @param token QName token
   * @param elem always resolve URI
   * @param sc static context
   * @return QName, or {@code null} if QName cannot be parsed.
   * @see QNm#parse(byte[], byte[], StaticContext, InputInfo)
   */
  public QNm parseQName(final byte[] token, final boolean elem, final StaticContext sc) {
    final byte[] name = Token.trim(token);
    if(XMLToken.isQName(name)) {
      final byte[] prefix = Token.prefix(name);
      final byte[] uri = prefix.length != 0 || elem ? sc.ns.uri(prefix) : null;
      return qName(name, uri);
    }
    final Matcher matcher = QNm.EQNAME.matcher(Token.string(name));
    if(matcher.matches()) {
      final byte[] nm = Token.token(matcher.group(2)), uri = Token.token(matcher.group(1));
      if(XMLToken.isNCName(nm)) return qName(nm, uri);
    }
    return null;
  }

  /**
   * Returns a shared QName.
   * @param name local name with optional prefix
   * @return QName
   */
  public QNm qName(final byte[] name) {
    return qName(name, null);
  }

  /**
   * Returns a shared QName.
   * @param name local name with optional prefix
   * @param uri URI (can be {@code null})
   * @return QName
   */
  public QNm qName(final byte[] name, final byte[] uri) {
    return qnames.computeIfAbsent(
      uri != null ? Token.concat(name, Token.SPACE, uri) : name,
      () -> new QNm(name, uri)
    );
  }

  /**
   * Returns a shared token.
   * @param token token to be cached
   * @return shared token
   */
  public byte[] token(final byte[] token) {
    return token.length == 0 ? Token.EMPTY : tokens.put(token);
  }
}
