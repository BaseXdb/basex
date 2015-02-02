package org.basex.core.cmd;

import static org.basex.core.Text.*;

import java.io.*;

import org.basex.core.*;
import org.basex.core.parse.*;
import org.basex.core.parse.Commands.Cmd;
import org.basex.core.parse.Commands.CmdCreate;
import org.basex.core.parse.Commands.CmdIndex;
import org.basex.core.users.*;
import org.basex.data.*;
import org.basex.index.*;
import org.basex.util.*;
import org.basex.util.ft.*;

/**
 * Evaluates the 'create db' command and creates a new index.
 *
 * @author BaseX Team 2005-15, BSD License
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

    final CmdIndex ci = getOption(CmdIndex.class);
    if(ci == null) return error(UNKNOWN_CMD_X, this);
    final IndexType type;
    if(ci == CmdIndex.TEXT) {
      data.meta.createtext = true;
      type = IndexType.TEXT;
    } else if(ci == CmdIndex.ATTRIBUTE) {
      data.meta.createattr = true;
      type = IndexType.ATTRIBUTE;
    } else if(ci == CmdIndex.FULLTEXT) {
      if(data.inMemory()) return error(NO_MAINMEM);
      data.meta.createftxt = true;
      data.meta.stemming = options.get(MainOptions.STEMMING);
      data.meta.casesens = options.get(MainOptions.CASESENS);
      data.meta.diacritics = options.get(MainOptions.DIACRITICS);
      data.meta.language = Language.get(options);
      data.meta.stopwords = options.get(MainOptions.STOPWORDS);
      type = IndexType.FULLTEXT;
    } else {
      return error(UNKNOWN_CMD_X, this);
    }

    if(!startUpdate()) return false;
    boolean ok = true;
    try {
      create(type, data, options, this);
      ok = info(INDEX_CREATED_X_X, type, perf);
    } catch(final IOException ex) {
      ok = error(Util.message(ex));
    } finally {
      ok &= finishUpdate();
    }
    return ok;
  }

  @Override
  public void build(final CmdBuilder cb) {
    cb.init(Cmd.CREATE + " " + CmdCreate.INDEX).args();
  }
}
