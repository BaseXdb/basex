package org.basex.core.cmd;

import static org.basex.core.Text.*;

import java.io.*;

import org.basex.core.*;
import org.basex.core.Commands.Cmd;
import org.basex.core.Commands.CmdCreate;
import org.basex.core.Commands.CmdIndex;
import org.basex.data.*;
import org.basex.index.IndexToken.IndexType;
import org.basex.util.*;
import org.basex.util.ft.*;

/**
 * Evaluates the 'create db' command and creates a new index.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class CreateIndex extends ACreate {
  /**
   * Default constructor.
   * @param type index type, defined in {@link CmdIndex}
   */
  public CreateIndex(final Object type) {
    super(Perm.WRITE, true, type != null ? type.toString() : null);
  }

  @Override
  protected boolean run() {
    final Data data = context.data();
    if(data.inMemory()) return error(NO_MAINMEM);

    final IndexType index;
    final CmdIndex ci = getOption(CmdIndex.class);
    switch(ci) {
      case TEXT:
        data.meta.createtext = true;
        index = IndexType.TEXT;
        break;
      case ATTRIBUTE:
        data.meta.createattr = true;
        index = IndexType.ATTRIBUTE;
        break;
      case FULLTEXT:
        data.meta.createftxt = true;
        data.meta.stemming = prop.is(Prop.STEMMING);
        data.meta.casesens = prop.is(Prop.CASESENS);
        data.meta.diacritics = prop.is(Prop.DIACRITICS);
        data.meta.language = Language.get(prop);
        index = IndexType.FULLTEXT;
        break;
      default:
        return error(UNKNOWN_CMD_X, this);
    }

    if(!data.startUpdate()) return error(DB_PINNED_X, data.meta.name);
    try {
      create(index, data, this);
      return info(INDEX_CREATED_X_X, index, perf);
    } catch(final IOException ex) {
      Util.debug(ex);
      return error(ex.getMessage());
    } finally {
      data.finishUpdate();
    }
  }

  @Override
  public void build(final CommandBuilder cb) {
    cb.init(Cmd.CREATE + " " + CmdCreate.INDEX).args();
  }
}
