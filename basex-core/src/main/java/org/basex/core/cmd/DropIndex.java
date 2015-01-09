package org.basex.core.cmd;

import static org.basex.core.Text.*;

import org.basex.core.parse.*;
import org.basex.core.parse.Commands.Cmd;
import org.basex.core.parse.Commands.CmdDrop;
import org.basex.core.parse.Commands.CmdIndex;
import org.basex.core.users.*;
import org.basex.data.*;
import org.basex.index.*;

/**
 * Evaluates the 'drop index' command and deletes indexes in the currently
 * opened database.
 *
 * @author BaseX Team 2005-15, BSD License
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
    final CmdIndex ci = getOption(CmdIndex.class);
    final IndexType type;
    if(ci == CmdIndex.TEXT) {
      data.meta.createtext = false;
      type = IndexType.TEXT;
    } else if(ci == CmdIndex.ATTRIBUTE) {
      data.meta.createattr = false;
      type = IndexType.ATTRIBUTE;
    } else if(ci == CmdIndex.FULLTEXT) {
      if(data.inMemory()) return error(NO_MAINMEM);
      data.meta.createftxt = false;
      type = IndexType.FULLTEXT;
    } else {
      return error(UNKNOWN_CMD_X, this);
    }

    if(!startUpdate()) return false;
    try {
      return drop(type, data) ? info(INDEX_DROPPED_X_X, type, perf) :
        error(INDEX_NOT_DROPPED_X, type);
    } finally {
      if(!finishUpdate()) return false;
    }
  }

  @Override
  public void build(final CmdBuilder cb) {
    cb.init(Cmd.DROP + " " + CmdDrop.INDEX).args();
  }
}
