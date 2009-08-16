package org.basex.core;

import java.io.IOException;
import org.basex.io.PrintOutput;

/**
 * This class executes commands locally.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public final class Launcher extends ALauncher {
  /** Database Context. */
  protected final Context context;
  /** Process reference. */
  protected Process proc;

  /**
   * Constructor.
   * @param ctx context
   */
  public Launcher(final Context ctx) {
    context = ctx;
  }

  @Override
  public boolean execute(final Process pr) {
    proc = pr;
    return pr.execute(context);
  }

  @Override
  public void output(final PrintOutput out) throws IOException {
    proc.output(out);
  }

  @Override
  public void info(final PrintOutput out) throws IOException {
    proc.info(out);
  }
}
