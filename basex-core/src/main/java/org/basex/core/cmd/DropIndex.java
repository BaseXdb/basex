package org.basex.core.cmd;

import static org.basex.core.Text.*;

import org.basex.core.*;
import org.basex.core.parse.*;
import org.basex.core.parse.Commands.*;
import org.basex.data.*;
import org.basex.index.*;

/**
 * Evaluates the 'drop index' command and deletes indexes in the currently
 * opened database.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class DropIndex extends ACreate {
  /**
   * Constructor.
   * @param type index type, defined in {@link CmdIndex}
   */
  public DropIndex(final Object type) {
    super(Perm.WRITE, true, type.toString());
  }

  @Override
  protected boolean run() {
    final Data data = context.data();
    if(data.inMemory()) return error(NO_MAINMEM);

    final CmdIndex ci = getOption(CmdIndex.class);
    if(ci == null) return error(UNKNOWN_CMD_X, this);
    final IndexType it;
    switch(ci) {
      case TEXT:
        data.meta.createtext = false;
        it = IndexType.TEXT;
        break;
      case ATTRIBUTE:
        data.meta.createattr = false;
        it = IndexType.ATTRIBUTE;
        break;
      case FULLTEXT:
        data.meta.createftxt = false;
        it = IndexType.FULLTEXT;
        break;
      default:
        return error(UNKNOWN_CMD_X, this);
    }

    if(!data.startUpdate()) return error(DB_PINNED_X, data.meta.name);
    try {
      return drop(it, context.data()) ? info(INDEX_DROPPED_X_X, it, perf) :
        error(INDEX_NOT_DROPPED_X, it);
    } finally {
      data.finishUpdate();
    }
  }

  @Override
  public void build(final CmdBuilder cb) {
    cb.init(Cmd.DROP + " " + CmdDrop.INDEX).args();
  }
}
