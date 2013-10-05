package org.basex.core.cmd;

import static org.basex.core.Text.*;

import java.io.*;

import org.basex.core.*;
import org.basex.core.parse.*;
import org.basex.core.parse.Commands.*;
import org.basex.data.*;
import org.basex.index.*;
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
    if(ci == null) return error(UNKNOWN_CMD_X, this);
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
        data.meta.stemming = options.is(Options.STEMMING);
        data.meta.casesens = options.is(Options.CASESENS);
        data.meta.diacritics = options.is(Options.DIACRITICS);
        data.meta.language = Language.get(options);
        data.meta.stopwords = options.get(Options.STOPWORDS);
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
      return error(Util.message(ex));
    } finally {
      data.finishUpdate();
    }
  }

  @Override
  public void build(final CmdBuilder cb) {
    cb.init(Cmd.CREATE + " " + CmdCreate.INDEX).args();
  }
}
