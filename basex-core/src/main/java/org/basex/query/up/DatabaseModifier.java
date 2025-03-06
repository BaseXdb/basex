package org.basex.query.up;

import org.basex.data.*;

/**
 * The database modifier holds all database updates during a snapshot.
 *
 * @author BaseX Team, BSD License
 * @author Lukas Kircher
 */
final class DatabaseModifier extends ContextModifier {
  @Override
  public void addData(final Data data) {
    // ignore data
  }
}
