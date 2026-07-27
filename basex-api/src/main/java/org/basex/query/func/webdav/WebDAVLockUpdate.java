package org.basex.query.func.webdav;

import static org.basex.query.QueryError.*;

import org.basex.core.users.*;
import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.map.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class WebDAVLockUpdate extends StandardFunc {
  @Override
  public Bln item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final FItem update = toFunction(arg(0), 1, qc);
    final User user = qc.context.user();

    return Bln.get(qc.context.stores.locks(entries -> {
      final XQMap result = toMap(invoke(update, new HofArgs(1).set(0, entries), qc).item(qc, info));
      if(result == entries) return result;

      XQMap map = result;
      for(final XQMap.Entry entry : result.entries()) {
        final Item key = entry.key();
        final Value value = entry.value();
        if(key.type != BasicType.STRING || !value.equals(entries.getOrNull(key))) {
          if(!WebDAVLocks.LOCK.instance(value)) throw typeError(value, WebDAVLocks.LOCK, info);
          final Item ky = key.type == BasicType.STRING ? key : Str.get(toToken(key));
          if(ky != key) map = map.remove(key);
          map = map.put(ky, value.materialize(n -> false, info, qc).shrink(qc));
        }
      }

      // locks of databases that may not be written are restored
      for(final XQMap.Entry entry : map.entries()) {
        if(!writable(user, entry.value(), info)) map = map.remove(entry.key());
      }
      for(final XQMap.Entry entry : entries.entries()) {
        if(!writable(user, entry.value(), info)) map = map.put(entry.key(), entry.value());
      }

      long same = 0;
      for(final XQMap.Entry entry : map.entries()) {
        if(entry.value().equals(entries.getOrNull(entry.key()))) same++;
      }
      return same == map.structSize() && same == entries.structSize() ? entries : map;
    }));
  }

  /**
   * Checks if a lock refers to a database that may be written.
   * @param user user
   * @param value lock
   * @param info input info (can be {@code null})
   * @return result of check
   * @throws QueryException query exception
   */
  private static boolean writable(final User user, final Value value, final InputInfo info)
      throws QueryException {
    return !(value instanceof final XQMap lock) ||
      user.has(Perm.WRITE, WebDAVLocks.database(lock, info));
  }
}
