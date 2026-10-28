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
 * @author BaseX Team, BSD License
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
   * @param user user ({@code null} if operation is global)
   * @param perms permissions (can be {@code null})
   * @param patterns patterns
   * @param qc query context
   * @param info input info (can be {@code null})
   * @throws QueryException query exception
   */
  protected UserPermUpdate(final UpdateType type, final User user, final ArrayList<Perm> perms,
      final StringList patterns, final QueryContext qc, final InputInfo info)
      throws QueryException {

    super(type, user, qc, info);
    this.perms = perms;
    final StringList pats = new StringList(patterns.size()).add(patterns);

    // permissions without pattern are global
    final int ps = perms != null ? perms.size() : 0;
    while(pats.size() < ps) pats.add("");
    if(perms != null && pats.size() > ps) throw USER_PATTERN2_X.get(info, pats.get(ps));

    final StringList tmp = new StringList();
    for(final String pattern : pats) {
      if(tmp.contains(pattern)) throw pattern.isEmpty()
        ? USER_UPDATE3_X_X.get(info, user.name(), operation()) : USER_UPDATE2_X.get(info, pattern);
      tmp.add(pattern);
    }
    for(int p = 0; p < ps; p++) {
      if(perms.get(p) == Perm.ADMIN && !pats.get(p).isEmpty()) throw USER_LOCAL.get(info);
    }
    this.patterns = pats;
  }

  @Override
  public void merge(final Update update) throws QueryException {
    for(final String pattern : ((UserPermUpdate) update).patterns) {
      if(patterns.contains(pattern)) throw pattern.isEmpty()
        ? USER_UPDATE1_X_X.get(info, name(), operation()) : USER_UPDATE2_X.get(info, pattern);
    }
  }

  /**
   * Grants the specified permissions.
   */
  protected final void grant() {
    final int ps = perms.size();
    for(int p = 0; p < ps; p++) user.permission(perms.get(p), patterns.get(p));
  }
}
