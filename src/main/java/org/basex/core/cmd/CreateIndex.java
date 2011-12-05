package org.basex.core.cmd;

import static org.basex.core.Text.*;
import java.io.IOException;
import org.basex.core.CommandBuilder;
import org.basex.core.Prop;
import org.basex.core.User;
import org.basex.core.Commands.Cmd;
import org.basex.core.Commands.CmdCreate;
import org.basex.core.Commands.CmdIndex;
import org.basex.data.Data;
import org.basex.data.MemData;
import org.basex.index.IndexToken.IndexType;
import org.basex.util.Util;
import org.basex.util.ft.Language;

/**
 * Evaluates the 'create db' command and creates a new index.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public final class CreateIndex extends ACreate {
  /**
   * Default constructor.
   * @param type index type, defined in {@link CmdIndex}
   */
  public CreateIndex(final Object type) {
    super(DATAREF | User.WRITE, type != null ? type.toString() : null);
  }

  @Override
  protected boolean run() {
    final Data data = context.data();
    if(data instanceof MemData) return error(PROCMM);

    try {
      IndexType index = null;
      final CmdIndex ci = getOption(CmdIndex.class);
      if(ci == null) return error(CMDUNKNOWN, this);
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
          data.meta.wildcards = prop.is(Prop.WILDCARDS);
          data.meta.stemming = prop.is(Prop.STEMMING);
          data.meta.casesens = prop.is(Prop.CASESENS);
          data.meta.diacritics = prop.is(Prop.DIACRITICS);
          data.meta.scoring = prop.num(Prop.SCORING);
          data.meta.language = Language.get(prop);
          index = IndexType.FULLTEXT;
          break;
        case PATH:
          data.meta.createpath = true;
          index = IndexType.PATH;
          break;
        default:
          throw Util.notexpected();
      }
      create(index, data, this);
      data.flush();

      return info(INDCREATED, index, perf);
    } catch(final IOException ex) {
      Util.debug(ex);
      return error(ex.getMessage());
    }
  }

  @Override
  public void build(final CommandBuilder cb) {
    cb.init(Cmd.CREATE + " " + CmdCreate.INDEX).args();
  }
}
