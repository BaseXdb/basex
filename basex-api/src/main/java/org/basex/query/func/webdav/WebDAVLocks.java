package org.basex.query.func.webdav;

import org.basex.core.users.*;
import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.map.*;
import org.basex.query.value.type.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class WebDAVLocks extends StandardFunc {
  /** Type of a single lock. */
  public static final SeqType LOCK;
  /** Type of the lock store: locks, keyed by lock token. */
  public static final SeqType LOCKS;

  /** Path field of a lock. */
  private static final Str PATH = Str.get("path");

  static {
    final TokenObjectMap<ShapeField> fields = new TokenObjectMap<>();
    for(final String name : new String[] { "token", "path", "depth", "scope" }) {
      fields.put(Token.token(name), new ShapeField(Types.STRING_O));
    }
    fields.put(Token.token("timeout"), new ShapeField(Types.INTEGER_O));
    fields.put(Token.token("expires"), new ShapeField(Types.DATE_TIME_O));
    fields.put(Token.token("owner"), new ShapeField(Types.ELEMENT_ZO));
    LOCK = new ShapeType(fields).seqType();
    LOCKS = MapType.get(BasicType.STRING, LOCK).seqType();
  }

  @Override
  public XQMap value(final QueryContext qc) throws QueryException {
    final User user = qc.context.user();

    // a local permission overrides the global one, so every lock is checked separately
    final MapBuilder mb = new MapBuilder();
    for(final XQMap.Entry entry : qc.context.stores.locks().entries()) {
      final Value value = entry.value();
      if(value instanceof final XQMap lock && user.has(Perm.READ, database(lock, info))) {
        mb.put(entry.key(), value);
      }
    }
    return mb.map();
  }

  /**
   * Returns the database a lock refers to.
   * @param lock lock
   * @param info input info (can be {@code null})
   * @return database name
   * @throws QueryException query exception
   */
  static String database(final XQMap lock, final InputInfo info) throws QueryException {
    final Value value = lock.getOrNull(PATH);
    return value instanceof final Item item ? Strings.split(Token.string(item.string(info)),
      '/', 2)[0] : "";
  }
}
