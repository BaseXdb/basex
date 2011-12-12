package org.basex.core.cmd;

import static org.basex.core.Commands.*;
import static org.basex.core.Text.*;
import java.io.IOException;
import org.basex.core.CommandBuilder;
import org.basex.core.User;
import org.basex.core.Commands.CmdIndex;
import org.basex.data.Data;
import org.basex.data.MemData;
import org.basex.index.IndexToken.IndexType;
import org.basex.util.Util;

/**
 * Evaluates the 'drop index' command and deletes indexes in the currently
 * opened database.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public final class DropIndex extends ACreate {
  /**
   * Constructor.
   * @param type index type, defined in {@link CmdIndex}
   */
  public DropIndex(final Object type) {
    super(DATAREF | User.WRITE, type.toString());
  }

  @Override
  protected boolean run() {
    final Data data = context.data();
    if(data instanceof MemData) return error(PROCMM);

    final CmdIndex ci = getOption(CmdIndex.class);
    switch(ci) {
      case TEXT:
        data.meta.createtext = false;
        return drop(IndexType.TEXT);
      case ATTRIBUTE:
        data.meta.createattr = false;
        return drop(IndexType.ATTRIBUTE);
      case FULLTEXT:
        data.meta.createftxt = false;
        return drop(IndexType.FULLTEXT);
      case PATH:
        data.meta.createpath = false;
        return drop(IndexType.PATH);
      default:
        return error(CMDUNKNOWN, this);
    }
  }

  /**
   * Drops the specified index.
   * @param index index type
   * @return success of operation
   */
  private boolean drop(final IndexType index) {
    try {
      return drop(index, context.data()) ?
          info(INDDROP, index, perf) : error(INDDROPERROR, index);
    } catch(final IOException ex) {
      Util.debug(ex);
      return error(ex.getMessage());
    }
  }

  @Override
  public void build(final CommandBuilder cb) {
    cb.init(Cmd.DROP + " " + CmdDrop.INDEX).args();
  }
}
