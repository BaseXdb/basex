package org.basex.core.proc;

import static org.basex.Text.*;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import org.basex.BaseX;
import org.basex.core.Prop;
import org.basex.data.MetaData;
import org.basex.io.PrintOutput;

/**
 * Evaluates the 'list' command. Shows all available databases.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class List extends Proc {
  @Override
  protected boolean exec() {
    return true;
  }
  

  @Override
  protected void out(final PrintOutput o) throws IOException {
    final String[] list = list();

    final StringBuilder sb = new StringBuilder();
    int c = 0;
    for(final String name : list) {
      sb.append(name);
      int l = name.length();
      while(l++ < 14) sb.append(" ");
      try {
        final MetaData md = new MetaData(name);
        md.read();
        sb.append(md.filename);
        c++;
      } catch(final IOException ex) {
        BaseX.debug(ex);
        sb.append(INFODBERR);
      }
      sb.append(NL);
    }
    if(c == 0) {
      o.print(INFONODB);
    } else {
      o.print("Database      XML Document" + NL);
      o.print("--------------------------------------------" + NL);
      o.print(sb + NL + c + INFODBLIST + NL);
    }
  }

  /**
   * Returns the list of available databases.
   * @return available databases.
   */
  public static String[] list() {
    // create database list
    int n = 0;
    final File file = new File(Prop.dbpath);
    // no database directory found...
    if(!file.exists()) return new String[] {};

    for(final File f : file.listFiles()) if(f.isDirectory()) n++;
    final String[] db = new String[n];
    n = 0;
    for(final File f : file.listFiles()) {
      if(f.isDirectory()) db[n++] = f.getName();
    }
    Arrays.sort(db, new Comparator<String>() {
      public int compare(final String s1, final String s2) {
        return s1.compareToIgnoreCase(s2);
      }
    });
    return db;
  }
}
