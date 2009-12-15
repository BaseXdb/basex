package org.basex.core.proc;

import static org.basex.core.Text.*;
import java.io.IOException;
import org.basex.core.Main;
import org.basex.core.Prop;
import org.basex.core.User;
import org.basex.core.Commands.Cmd;
import org.basex.core.Commands.CmdCreate;
import org.basex.core.Commands.CmdIndex;
import org.basex.data.Data;
import org.basex.data.MemData;
import org.basex.data.Data.Type;
import org.basex.io.PrintOutput;

/**
 * Evaluates the 'create db' command and creates a new index.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public final class CreateIndex extends ACreate {
  /**
   * Default constructor.
   * @param type index type, defined in {@link CmdIndex}
   */
  public CreateIndex(final Object type) {
    super(DATAREF | User.WRITE, type.toString());
  }

  @Override
  protected boolean exec(final PrintOutput out) {
    final Data data = context.data;

    try {
      Type index = null;
      switch(getOption(CmdIndex.class)) {
        case TEXT:
          data.meta.txtindex = true;
          index = Type.TXT;
          break;
        case ATTRIBUTE:
          data.meta.atvindex = true;
          index = Type.ATV;
          break;
        case FULLTEXT:
          data.meta.ftxindex = true;
          data.meta.wildcards = prop.is(Prop.WILDCARDS);
          data.meta.stemming = prop.is(Prop.STEMMING);
          data.meta.casesens = prop.is(Prop.CASESENS);
          data.meta.diacritics = prop.is(Prop.DIACRITICS);
          data.meta.scoring = prop.num(Prop.SCORING);
          index = Type.FTX;
          break;
        case PATH:
          data.path.build(data);
          return true;
        default:
          return false;
      }
      if(data instanceof MemData) return error(PROCMM);

      index(index, data);
      data.flush();

      return info(DBINDEXED, perf);
    } catch(final IOException ex) {
      Main.debug(ex);
      return error(ex.getMessage());
    }
  }

  @Override
  public String toString() {
    return Cmd.CREATE + " " + CmdCreate.INDEX + args();
  }
}
