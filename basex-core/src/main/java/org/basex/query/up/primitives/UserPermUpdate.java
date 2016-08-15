package org.basex.query.up.primitives;

import static org.basex.query.QueryError.*;

import java.util.*;

import org.basex.core.users.*;
import org.basex.query.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * Update that operates on a global user.
 *
 * @author BaseX Team 2005-16, BSD License
 * @author Christian Gruen
 */
public abstract class UserPermUpdate extends UserUpdate {
  /** Permission (can be {@code null}). */
  private final ArrayList<Perm> perms;
  /** Database patterns. */
  protected final StringList patterns;

  /**
   * Constructor.
   * @param type type of this operation
   * @param user user
   * @param perms permissions (can be {@code null})
   * @param patterns patterns
   * @param qc query context
   * @param info input info
   * @throws QueryException query exception
   */
  public UserPermUpdate(final UpdateType type, final User user, final ArrayList<Perm> perms,
      final StringList patterns, final QueryContext qc, final InputInfo info)
          throws QueryException {

    super(type, user, qc, info);
    this.perms = perms;
    this.patterns = patterns;

    final StringList tmp = new StringList();
    for(final String pattern : patterns) {
      if(tmp.contains(pattern)) throw pattern.isEmpty()
        ? USER_SAMEPERM_X_X.get(info, user.name(), operation()) : USER_SAMEPAT_X.get(info, pattern);
      tmp.add(pattern);
    }
  }

  @Override
  public void merge(final Update update) throws QueryException {
    final UserPermUpdate up = (UserPermUpdate) update;
    if(!name().equals(up.name())) return;
    for(final String pattern : up.patterns) {
      if(patterns.contains(pattern)) throw pattern.isEmpty()
        ? USER_UPDATE_X_X.get(info, name(), operation()) : USER_SAMEPAT_X.get(info, pattern);
    }
  }

  /**
   * Grants the specified permissions.
   */
  protected final void grant() {
    final int ps = perms.size();
    for(int p = 0; p < ps; p++) user.perm(perms.get(p), patterns.get(p));
  }
}
