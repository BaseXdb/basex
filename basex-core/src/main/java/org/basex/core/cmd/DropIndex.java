package org.basex.core.cmd;

import static org.basex.core.Text.*;

import java.io.*;

import org.basex.core.parse.*;
import org.basex.core.parse.Commands.*;
import org.basex.core.users.*;
import org.basex.data.*;
import org.basex.index.*;

/**
 * Evaluates the 'drop index' command and deletes indexes in the currently
 * opened database.
 *
 * @author BaseX Team 2005-21, BSD License
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
      type = IndexType.TEXT;
      data.meta.createtext = false;
    } else if(ci == CmdIndex.ATTRIBUTE) {
      type = IndexType.ATTRIBUTE;
      data.meta.createattr = false;
    } else if(ci == CmdIndex.TOKEN) {
      type = IndexType.TOKEN;
      data.meta.createtoken = false;
    } else if(ci == CmdIndex.FULLTEXT) {
      type = IndexType.FULLTEXT;
      data.meta.createft = false;
    } else {
      return error(UNKNOWN_CMD_X, this);
    }
    data.meta.names(type, options);

    return update(data, new Code() {
      @Override
      boolean run() throws IOException {
        drop(type, data);
        return info(INDEX_DROPPED_X_X, type, jc().performance);
      }
    });
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
    data.meta.index(type, false);
    data.dropIndex(type);
  }
}
