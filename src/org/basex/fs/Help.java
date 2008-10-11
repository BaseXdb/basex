package org.basex.fs;

import static org.basex.Text.*;
import static org.basex.util.Token.*;
import java.io.IOException;
import java.lang.reflect.Field;
import org.basex.BaseX;
import org.basex.core.Commands;
import org.basex.io.CachedOutput;
import org.basex.io.PrintOutput;
import org.basex.util.Levenshtein;

/**
 * Performs a help command.
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class Help extends FSCmd {
  /** De-/Activate verbose help. */
  private static final boolean VERBOSE = false;
  /** Help argument. */
  private String cmd;

  @Override
  public void args(final String args) {
    cmd = args;
  }

  @Override
  public void exec(final PrintOutput out) throws IOException {
    help(out, cmd);
    out.println();
  }

  /**
   * Returns the help as string.
   * @param cmd command
   * @return help string
   */
  public static String help(final String cmd) {
    final CachedOutput out = new CachedOutput();
    try {
      help(out, cmd);
    } catch(final IOException ex) {
      BaseX.notexpected(ex);
    }
    return out.toString();
  }

  /**
   * Prints the help message to the specified for the specified output stream.
   * @param out output stream
   * @param cmd command
   * @throws IOException exception
   */
  private static void help(final PrintOutput out, final String cmd)
      throws IOException {

    try {
      if(cmd.length() == 0) {
        out.println(FSText.HELP);
        final Field[] fields = FSText.class.getFields();
        int maxW = 0;
        for(final Field f : fields)
          maxW = Math.max(maxW, f.getName().length());
        for(final Field f : fields) {
          final Object o = f.get(null);
          if(o instanceof String[]) {
            final String[] cm = (String[]) o;
            out.print(LI);
            out.print(token(f.getName().toLowerCase()), maxW + 2);
            out.println(cm[0]);
          }
        }
      } else {
        final String cl = cmd.toLowerCase();
        final String cu = cmd.toUpperCase();
        final String[] help = (String[]) FSText.class.getField(cu).get(null);

        if(VERBOSE) {
          out.println(String.format("%-23s%-35s%18s", cu + "(1)",
              FSText.HEAD, "(1)" + cu) + NL);
          out.println(" " + FSText.NAME);
          out.println("   " + cl + " -- " + help[0] + NL);
          out.println(" " + FSText.SYNO);
          out.println("   " + cl + " " + help[1] + NL);
          out.println(" " + FSText.DESC);
          for(int i = 2; i < help.length; i++) out.println("   " + help[i]);
        } else {
          out.println(FSText.USAGE + cl + " " + help[1]);
          out.println(help[0] + NL);
          for(int i = 2; i < help.length; i++) out.println(help[i]);
        }
      }
    } catch(final Exception ex) {
      final byte[] n = lc(token(cmd));
      for(final Commands.FS c : Commands.FS.values()) {
        final byte[] s = lc(token(c.name()));
        if(Levenshtein.similar(n, s)) {
          out.println(BaseX.info(CMDSIMILAR, n, s));
          return;
        }
      }
      out.println(BaseX.info(CMDUNKNOWN, cmd));
    }
  }
}
