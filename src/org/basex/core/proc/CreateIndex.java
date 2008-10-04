package org.basex.core.proc;

import static org.basex.Text.*;
import java.io.IOException;
import org.basex.BaseX;
import org.basex.core.Commands.COMMANDS;
import org.basex.core.Commands.CREATE;
import org.basex.core.Commands.INDEX;
import org.basex.data.Data;
import org.basex.index.IndexToken;

/**
 * Creates a new index.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class CreateIndex extends ACreate {
  /** Index type. */
  private final INDEX type;

  /**
   * Constructor.
   * @param t index type
   */
  public CreateIndex(final INDEX t) {
    super(DATAREF);
    type = t;
  }
  
  @Override
  protected boolean exec() {
    try {
      final Data data = context.data();
      IndexToken.TYPE index = null;
      switch(type) {
        case TEXT:
          data.meta.txtindex = true;
          index = IndexToken.TYPE.TXT;
          break;
        case ATTRIBUTE:
          data.meta.atvindex = true;
          index = IndexToken.TYPE.ATV;
          break;
        case FULLTEXT:
          data.meta.ftxindex = true;
          index = IndexToken.TYPE.FTX;
          break;
      }
      data.flush();
      buildIndex(index, data);
      return info(DBINDEXED, perf.getTimer());
    } catch(final IOException ex) {
      BaseX.debug(ex);
      return error(ex.getMessage());
    }
  }
  
  @Override
  public String toString() {
    return COMMANDS.CREATE.name() + " " +  CREATE.INDEX + " " + type;
  }
}
