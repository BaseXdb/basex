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
 * @author BaseX Team, BSD License
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
    final IndexType type = switch(ci) {
      case TEXT      -> IndexType.TEXT;
      case ATTRIBUTE -> IndexType.ATTRIBUTE;
      case TOKEN     -> IndexType.TOKEN;
      case FULLTEXT  -> IndexType.FULLTEXT;
      default        -> null;
    };
    if(type == null) return error(UNKNOWN_CMD_X, this);
    data.meta.create(type, false);
    data.meta.names(type, options);

    return update(data, () -> {
      drop(type, data);
      return info(INDEX_DROPPED_X_X, type, jc().performance);
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
