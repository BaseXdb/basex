package org.basex.query.up;

import java.util.*;

import org.basex.query.*;
import org.basex.query.up.primitives.*;
import org.basex.util.list.*;

/**
 * This class 'caches' all update operations that use a database name as reference.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
final class NameUpdates {
  /** List of update operations. */
  private final List<NameUpdate> nameUpdates = new LinkedList<NameUpdate>();

  /**
   * Adds an update to the list.
   * @param up update primitive
   * @throws QueryException query exception
   */
  void add(final NameUpdate up) throws QueryException {
    for(final NameUpdate o : nameUpdates) {
      if(o.type == up.type) {
        o.merge(up);
        return;
      }
    }
    nameUpdates.add(up);
  }

  /**
   * Prepares updates for execution.
   * @throws QueryException query exception
   */
  void prepare() throws QueryException {
    for(final NameUpdate o : nameUpdates) o.prepare();
    // sort all operations
    Collections.sort(nameUpdates);
  }

  /**
   * Applies all updates.
   * @param before run updates specified before or after node updates
   * @throws QueryException query exception
   */
  void apply(final boolean before) throws QueryException {
    final int pos = UpdateType._NODE_UPDATES_.ordinal();
    for(final NameUpdate up : nameUpdates) {
      final int ord = up.type.ordinal();
      if(before ? ord < pos : ord > pos) up.apply();
    }
  }

  /**
   * Returns the number of performed updates.
   * @return number of updates
   */
  int size() {
    int size = 0;
    for(final NameUpdate up : nameUpdates) size += up.size();
    return size;
  }

  /**
   * Adds databases to be updated to the specified list.
   * @param db databases
   */
  void databases(final StringList db) {
    for(final NameUpdate up : nameUpdates) up.databases(db);
  }
}
