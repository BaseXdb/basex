package org.basex.query.util;

import java.util.*;

import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Shared data references.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class SharedData {
  /** Cached QNames. */
  private final TokenObjectMap<QNm> qnames = new TokenObjectMap<>();
  /** Cached tokens. */
  private final WeakTokenSet tokens = new WeakTokenSet();
  /** Cached record types. */
  private final IntObjectMap<ArrayList<RecordType>> recordTypes = new IntObjectMap<>();

  /**
   * Constructor.
   */
  public SharedData() {
    record(Types.RECORD);
  }

  /**
   * Parses and returns a shared QName.
   * @param token QName token
   * @param elem always resolve URI
   * @param sc static context
   * @return QName, or {@code null} if QName cannot be parsed
   * @see QNm#parse(byte[], byte[], StaticContext, InputInfo)
   */
  public QNm parseQName(final byte[] token, final boolean elem, final StaticContext sc) {
    final byte[] name = Token.trim(token);
    if(XMLToken.isQName(name)) {
      final byte[] prefix = Token.prefix(name);
      final byte[] uri = prefix.length != 0 || elem ? sc.ns.uri(prefix) : null;
      return qName(name, uri);
    }
    final byte[][] parsed = QNm.parseExpanded(name, false);
    return parsed != null ? qName(parsed[0], parsed[1]) : null;
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
      uri != null ? Token.concat(name, Token.cpToken(' '), uri) : name,
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

  /**
   * Creates a non-extensible record or returns an existing instance.
   * @param key key
   * @param type type
   * @return new or already registered record type
   */
  public RecordType record(final Str key, final SeqType type) {
    final TokenObjectMap<RecordField> map = new TokenObjectMap<>(1);
    map.put(key.string(), new RecordField(type));
    return record(new RecordType(map));
  }

  /**
   * Creates a new record or returns an existing instance.
   * @param rt record type
   * @return new or already registered record type
   */
  public RecordType record(final RecordType rt) {
    final ArrayList<RecordType> types = recordTypes.computeIfAbsent(rt.fields().size(),
        ArrayList::new);
    for(final RecordType type : types) {
      if(type.equals(rt)) return type;
    }
    types.add(rt);
    return rt;
  }
}
