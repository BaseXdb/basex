package org.basex.core.proc;

import static org.basex.Text.*;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.basex.BaseX;
import org.basex.core.Commands.COMMANDS;
import org.basex.core.Commands.INFO;
import org.basex.data.Data;
import org.basex.data.MetaData;
import org.basex.io.IO;
import org.basex.io.PrintOutput;
import org.basex.util.Performance;
import org.basex.util.TokenBuilder;

/**
 * Database info.
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
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
    final Data data = context.data();
    out.print(db(data.meta, data.size, true, true));
  }
  
  /**
   * Creates a database information string.
   * @param meta meta data
   * @param size database size
   * @param header add header flag
   * @param index add index information
   * @return info string
   */
  public static byte[] db(final MetaData meta, final int size,
      final boolean header, final boolean index) {
    
    final File dir = IO.dbpath(meta.dbname);
    long len = 0;
    if(dir.exists()) for(final File f : dir.listFiles()) len += f.length();

    final TokenBuilder tb = new TokenBuilder();
    final int l = maxLength(new String[] {
        INFODBNAME, INFODBSIZE, INFODOC, INFOTIME, INFONDOCS, INFODOCSIZE, 
        INFOENCODING, INFONODES, INFOHEIGHT
    }) + 1;

    if(header) {
      tb.add(INFODB + NL);
      format(tb, INFODBNAME, meta.dbname, header, l);
    }
    format(tb, INFODBSIZE, Performance.format(len), header, l);
    format(tb, INFOENCODING, meta.encoding, header, l);
    format(tb, INFONODES, Integer.toString(size), header, l);
    format(tb, INFOHEIGHT, Integer.toString(meta.height), header, l);
    
    tb.add(NL + INFOCREATE + NL);
    format(tb, INFODOC, meta.file.path(), header, l);
    format(tb, INFOTIME, DATE.format(new Date(meta.time)), header, l);
    format(tb, INFODOCSIZE, Performance.format(meta.filesize), header, l);
    format(tb, INFONDOCS, Integer.toString(meta.ndocs), header, l);
    //format(tb, INFOCHOP, BaseX.flag(meta.chop), true, 0);
    //format(tb, INFOENTITIES, BaseX.flag(meta.entity), true, 0);
    
    if(index) {
      tb.add(NL + INFOINDEX + NL);
      if(meta.newindex) {
        tb.add(" " + INDUPDATE + NL);
      } else {
        format(tb, INFOTEXTINDEX, BaseX.flag(meta.txtindex), true, 0);
        format(tb, INFOATTRINDEX, BaseX.flag(meta.atvindex), true, 0);
        format(tb, INFOFTINDEX, BaseX.flag(meta.ftxindex) + (meta.ftxindex &&
            meta.ftfz ? " (" + INFOFZINDEX + ")" : ""), true, 0);
      }
    }
    return tb.finish();
  }
  
  @Override
  public String toString() {
    return COMMANDS.INFO.name() + " " + INFO.DB;
  }
}
