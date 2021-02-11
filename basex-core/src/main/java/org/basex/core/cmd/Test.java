package org.basex.core.cmd;

import static org.basex.core.Text.*;

import java.io.*;

import org.basex.core.*;
import org.basex.core.users.*;
import org.basex.io.*;
import org.basex.io.serial.*;
import org.basex.query.func.unit.*;
import org.basex.util.*;

/**
 * Evaluates the 'test' command and processes an input file.
 *
 * @author BaseX Team 2005-21, BSD License
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
    if(!root.exists())
      return error(RES_NOT_FOUND_X, context.user().has(Perm.CREATE) ? root : args[0]);

    try {
      final Suite suite = new Suite();
      suite.test(root, context, this).serialize(Serializer.get(out));
      out.print(NL);

      final StringBuilder sb = new StringBuilder(RESULT).append(COLS);
      add(sb, "test", suite.tests);
      add(sb, "failure", suite.failures);
      add(sb, "error", suite.errors);
      sb.append(suite.skipped).append(' ').append("skipped.");
      return suite.errors + suite.failures == 0 ? info(sb.toString()) : error(sb.toString());
    } catch(final IOException ex) {
      exception = ex;
      return error(Util.message(ex));
    }
  }

  /**
   * Adds a string to the test summary.
   * @param sb string builder
   * @param string string
   * @param number number
   */
  private static void add(final StringBuilder sb, final String string, final int number) {
    sb.append(number).append(' ').append(string);
    if(number != 1) sb.append('s');
    sb.append(", ");
  }

  @Override
  public boolean updating(final Context ctx) {
    return true;
  }

  @Override
  public void addLocks() {
    jc().locks.writes.addGlobal();
  }
}
