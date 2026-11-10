package org.basex.query.up;

import org.basex.data.*;
import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.util.*;

/**
 * The database modifier holds all database updates during a snapshot.
 *
 * @author BaseX Team, BSD License
 * @author Lukas Kircher
 */
final class DatabaseModifier extends ContextModifier {
  /**
   * Constructor.
   * @param outer outer context modifier (can be {@code null})
   */
  DatabaseModifier(final ContextModifier outer) {
    super(outer);
  }

  @Override
  public void addData(final Data data) {
    // ignore data
  }

  @Override
  ContextModifier nest() {
    return new DatabaseModifier(this);
  }

  @Override
  void addOutput(final Value value, final InputInfo info, final QueryContext qc) {
    cacheOutput(value, qc);
  }

  @Override
  void check() {
    // updates can always be applied
  }
}
