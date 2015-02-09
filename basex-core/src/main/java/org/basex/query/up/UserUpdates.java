package org.basex.query.up;

import static org.basex.query.QueryError.*;

import java.util.*;

import org.basex.query.*;
import org.basex.query.up.primitives.*;

/**
 * This class 'caches' all update operations that use a user name as reference.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
final class UserUpdates {
  /** List of update operations. */
  private final List<UserUpdate> userUpdates = new LinkedList<>();

  /**
   * Adds an update to the list.
   * @param up update primitive
   * @throws QueryException query exception
   */
  void add(final UserUpdate up) throws QueryException {
    final boolean alter = up.type == UpdateType.USERALTER;
    final boolean drop = up.type == UpdateType.USERDROP;
    for(final UserUpdate o : userUpdates) {
      if(o.type == up.type) o.merge(up);
      if(drop && o.type == UpdateType.USERALTER || alter && o.type == UpdateType.USERDROP) {
        throw USER_CONFLICT_X.get(o.info(), o.name());
      }
    }
    userUpdates.add(up);
  }

  /**
   * Applies all updates.
   */
  void apply() {
    for(final UserUpdate up : userUpdates) up.apply();
  }

  /**
   * Returns the number of performed updates.
   * @return number of updates
   */
  int size() {
    int size = 0;
    for(final UserUpdate up : userUpdates) size += up.size();
    return size;
  }
}
