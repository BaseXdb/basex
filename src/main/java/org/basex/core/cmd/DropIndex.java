package org.basex.core.cmd;

import static org.basex.core.Commands.*;
import static org.basex.core.Text.*;
import static org.basex.data.DataText.*;
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
    final Data data = context.data;
    if(data instanceof MemData) return error(PROCMM);

    final CmdIndex ci = getOption(CmdIndex.class);
    if(ci == null) return error(CMDUNKNOWN, this);
    switch(ci) {
      case TEXT:
        data.meta.textindex = false;
        return drop(IndexType.TEXT, DATATXT);
      case ATTRIBUTE:
        data.meta.attrindex = false;
        return drop(IndexType.ATTRIBUTE, DATAATV);
      case FULLTEXT:
        data.meta.ftindex = false;
        return drop(IndexType.FULLTEXT, DATAFTX);
      case PATH:
        data.meta.pathindex = false;
        data.pthindex.init();
        data.meta.dirty = true;
        data.flush();
        return info(INDDROP, perf);
      default:
        return false;
    }
  }

  /**
   * Drops the specified index.
   * @param index index type
   * @param pat pattern
   * @return success of operation
   */
  private boolean drop(final IndexType index, final String pat) {
    try {
      final Data data = context.data;
      data.closeIndex(index);
      data.meta.dirty = true;
      data.flush();
      return data.meta.drop(pat + '.') ? info(INDDROP, index, perf) :
        error(INDDROPERROR, index);
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
