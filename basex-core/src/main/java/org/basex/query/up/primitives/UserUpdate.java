package org.basex.query.up.primitives;

import static org.basex.query.QueryError.*;

import org.basex.core.users.*;
import org.basex.query.*;
import org.basex.util.*;

/**
 * Update that operates on a global user.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public abstract class UserUpdate extends Update {
  /** Users. */
  protected final Users users;
  /** User ({@code null} if operation is global). */
  protected final User user;

  /**
   * Constructor.
   * @param type type of this operation
   * @param user user ({@code null} if operation is global)
   * @param qc query context
   * @param info input info
   */
  protected UserUpdate(final UpdateType type, final User user, final QueryContext qc,
      final InputInfo info) {
    super(type, info);
    this.user = user;
    users = qc.context.users;
  }

  /**
   * Returns the name of the user.
   * @return name (can be {@code null})
   */
  public final String name() {
    return user != null ? user.name() : null;
  }

  /**
   * Applies this operation.
   */
  public abstract void apply();

  /**
   * Returns an info string.
   * @return info string
   */
  protected abstract String operation();

  @Override
  public final int size() {
    return 1;
  }

  @Override
  public void merge(final Update update) throws QueryException {
    final UserUpdate up = (UserUpdate) update;
    if(user.equals(up.user)) throw USER_UPDATE1_X_X.get(info, name(), operation());
  }
}
