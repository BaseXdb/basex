package org.basex.core.proc;

import static org.basex.core.Text.*;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.basex.core.User;
import org.basex.core.Commands.Cmd;
import org.basex.core.Commands.CmdInfo;
import org.basex.data.MetaData;
import org.basex.io.PrintOutput;
import org.basex.util.Performance;
import org.basex.util.TokenBuilder;

/**
 * Evaluates the 'info database' command and returns information on the
 * currently opened database.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public final class InfoDB extends AInfo {
  /** Date Format. */
  private static final SimpleDateFormat DATE =
    new SimpleDateFormat("dd.MM.yyyy hh:mm:ss");

  /**
   * Default constructor.
   */
  public InfoDB() {
    super(DATAREF | User.READ);
  }

  @Override
  protected boolean exec(final PrintOutput out) throws IOException {
    out.print(db(context.data.meta, false, true).finish());
    return true;
  }

  /**
   * Creates a database information string.
   * @param meta meta data
   * @param bold header bold header flag
   * @param index add index information
   * @return info string
   */
  public static TokenBuilder db(final MetaData meta, final boolean bold,
      final boolean index) {

    final File dir = meta.prop.dbpath(meta.name);
    long len = 0;
    if(dir.exists()) for(final File f : dir.listFiles()) len += f.length();

    final TokenBuilder tb = new TokenBuilder();
    final String header = (bold ?
        new TokenBuilder().high().add("%").norm().toString() : "%") + NL;
    tb.add(header, INFODB);
    format(tb, INFODBNAME, meta.name);
    format(tb, INFODBSIZE, Performance.format(len));
    format(tb, INFONODES, Integer.toString(meta.size));
    format(tb, INFOHEIGHT, Integer.toString(meta.height));

    tb.add(NL);
    tb.add(header, INFOCREATE);
    format(tb, INFODOC, meta.file.path());
    format(tb, INFOTIME, DATE.format(new Date(meta.time)));
    format(tb, INFODOCSIZE, Performance.format(meta.filesize));
    format(tb, INFOENCODING, meta.encoding);
    format(tb, INFONDOCS, Integer.toString(meta.ndocs));
    format(tb, INFOCHOP, flag(meta.chop));
    format(tb, INFOENTITY, flag(meta.entity));

    if(index) {
      tb.add(NL);
      tb.add(header, INFOINDEX);
      if(meta.oldindex) {
        tb.add(" " + INDUPDATE + NL);
      } else {
        format(tb, INFOPATHINDEX, flag(meta.pathindex));
        format(tb, INFOTEXTINDEX, flag(meta.txtindex));
        format(tb, INFOATTRINDEX, flag(meta.atvindex));
        format(tb, INFOFTINDEX, flag(meta.ftxindex) + (meta.ftxindex &&
            meta.ftfz ? " (" + INFOFZINDEX + ")" : ""));
      }
    }
    return tb;
  }

  @Override
  public String toString() {
    return Cmd.INFO + " " + CmdInfo.DB;
  }
}
