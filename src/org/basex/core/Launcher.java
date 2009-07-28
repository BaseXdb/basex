package org.basex.core;

import java.io.IOException;

import org.basex.io.PrintOutput;

/**
 * This class executes commands locally.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public class Launcher extends ALauncher {
  /** Database Context. */
  protected Context context;

  /**
   * Constructor.
   * @param pr process instance
   * @param ctx context
   */
  public Launcher(final Process pr, final Context ctx) {
    super(pr);
    context = ctx;
  }

  @Override
  public boolean execute() {
    return proc.execute(context);
  }

  @Override
  public void out(final PrintOutput out) throws IOException {
    proc.output(out);
  }

  @Override
  public void info(final PrintOutput out) throws IOException {
    proc.info(out);
  }
}
