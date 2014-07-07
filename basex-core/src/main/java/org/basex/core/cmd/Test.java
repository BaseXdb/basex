package org.basex.core.cmd;

import static org.basex.core.Text.*;

import java.io.*;

import org.basex.core.*;
import org.basex.io.*;
import org.basex.io.serial.*;
import org.basex.query.util.unit.*;
import org.basex.util.*;

/**
 * Evaluates the 'test' command and processes an input file.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class Test extends Command {
  /**
   * Default constructor.
   * @param path input path
   */
  public Test(final String path) {
    super(Perm.ADMIN, false, path);
  }

  @Override
  protected boolean run() {
    final IOFile root = new IOFile(args[0]);
    if(!root.exists()) return error(RES_NOT_FOUND_X,
        context.user.has(Perm.CREATE) ? root : args[0]);

    try {
      final XMLSerializer ser = Serializer.get(out);
      final Suite suite = new Suite();
      ser.serialize(suite.test(root, context, this));
      out.print(NL);
      out.flush();

      final StringBuilder sb = new StringBuilder(RESULT).append(COLS);
      add(sb, "test", suite.tests);
      add(sb, "failure", suite.failures);
      add(sb, "error", suite.errors);
      sb.append(suite.skipped).append(' ').append("skipped.");
      return suite.errors + suite.failures == 0 ? info(sb.toString()) : error(sb.toString());
    } catch(final IOException ex) {
      cause = ex;
      return error(Util.message(ex));
    }
  }

  /**
   * Adds a string to the test summary.
   * @param sb string builder
   * @param string string
   * @param number number
   */
  private void add(final StringBuilder sb, final String string, final int number) {
    sb.append(number).append(' ').append(string);
    if(number != 1) sb.append('s');
    sb.append(", ");
  }

  @Override
  public boolean updating(final Context ctx) {
    return true;
  }

  @Override
  public void databases(final LockResult lr) {
    lr.writeAll = true;
  }
}
