package org.basex.core.cmd;

import static org.basex.core.Text.*;

import java.io.*;

import org.basex.core.*;
import org.basex.core.parse.*;
import org.basex.core.parse.Commands.Cmd;
import org.basex.core.parse.Commands.CmdDrop;
import org.basex.core.parse.Commands.CmdIndex;
import org.basex.core.users.*;
import org.basex.data.*;
import org.basex.index.*;
import org.basex.util.*;

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
      data.meta.textinclude = options.get(MainOptions.TEXTINCLUDE);
      type = IndexType.TEXT;
    } else if(ci == CmdIndex.ATTRIBUTE) {
      data.meta.createattr = false;
      data.meta.attrinclude = options.get(MainOptions.ATTRINCLUDE);
      type = IndexType.ATTRIBUTE;
    } else if(ci == CmdIndex.FULLTEXT) {
      data.meta.createftxt = false;
      data.meta.ftinclude = options.get(MainOptions.FTINCLUDE);
      type = IndexType.FULLTEXT;
    } else {
      return error(UNKNOWN_CMD_X, this);
    }

    if(!startUpdate()) return false;
    boolean ok = true;
    try {
      drop(type, data);
      ok = info(INDEX_DROPPED_X_X, type, perf);
    } catch(final IOException ex) {
      ok = error(Util.message(ex));
    } finally {
      ok &= finishUpdate();
    }
    return ok;
  }

  @Override
  public void build(final CmdBuilder cb) {
    cb.init(Cmd.DROP + " " + CmdDrop.INDEX).args();
  }

  /**
   * Drops the specified index.
   * @param type index type
   * @param data data reference
   * @throws IOException I/O exception
   */
  static void drop(final IndexType type, final Data data) throws IOException {
    data.meta.dirty = true;
    if(type == IndexType.TEXT) {
      data.meta.textindex = false;
    } else if(type == IndexType.ATTRIBUTE) {
      data.meta.attrindex = false;
    } else if(type == IndexType.FULLTEXT) {
      data.meta.ftindex = false;
    } else {
      throw Util.notExpected();
    }
    data.dropIndex(type);
  }
}
