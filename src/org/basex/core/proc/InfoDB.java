package org.basex.core.proc;

import static org.basex.Text.*;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.basex.BaseX;
import org.basex.core.Commands.Cmd;
import org.basex.core.Commands.CmdInfo;
import org.basex.data.MetaData;
import org.basex.io.PrintOutput;
import org.basex.util.Performance;
import org.basex.util.TokenBuilder;

/**
 * Evaluates the 'info database' command.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public final class InfoDB extends AInfo {
  /** Date Format. */
  private static final SimpleDateFormat DATE =
    new SimpleDateFormat("dd.MM.yyyy hh:mm:ss");

  /**
   * Constructor.
   */
  public InfoDB() {
    super(DATAREF | PRINTING);
  }

  @Override
  protected void out(final PrintOutput out) throws IOException {
    out.print(db(context.data().meta, false, true).finish());
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
    final int l = maxLength(new String[] {
        INFODBNAME, INFODBSIZE, INFODOC, INFOTIME, INFONDOCS, INFODOCSIZE,
        INFOENCODING, INFONODES, INFOHEIGHT
    }) + 1;

    final String header = (bold ?
        new TokenBuilder().high().add("%").norm().toString() : "%") + NL;
    tb.add(header, INFODB);
    format(tb, INFODBNAME, meta.name, l);
    format(tb, INFODBSIZE, Performance.format(len), l);
    format(tb, INFOENCODING, meta.encoding, l);
    format(tb, INFONODES, Integer.toString(meta.size), l);
    format(tb, INFOHEIGHT, Integer.toString(meta.height), l);

    tb.add(NL);
    tb.add(header, INFOCREATE);
    format(tb, INFODOC, meta.file.path(), l);
    format(tb, INFOTIME, DATE.format(new Date(meta.time)), l);
    format(tb, INFODOCSIZE, Performance.format(meta.filesize), l);
    format(tb, INFONDOCS, Integer.toString(meta.ndocs), l);
    format(tb, INFOCHOP, BaseX.flag(meta.chop), 0);
    format(tb, INFOENTITY, BaseX.flag(meta.entity), 0);

    if(index) {
      tb.add(NL);
      tb.add(header, INFOINDEX);
      if(meta.oldindex) {
        tb.add(" " + INDUPDATE + NL);
      } else {
        format(tb, INFOTEXTINDEX, BaseX.flag(meta.txtindex), 0);
        format(tb, INFOATTRINDEX, BaseX.flag(meta.atvindex), 0);
        format(tb, INFOFTINDEX, BaseX.flag(meta.ftxindex) + (meta.ftxindex &&
            meta.ftfz ? " (" + INFOFZINDEX + ")" : ""), 0);
      }
    }
    return tb;
  }

  @Override
  public String toString() {
    return Cmd.INFO.name() + " " + CmdInfo.DB;
  }
}
