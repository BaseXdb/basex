package org.basex.core.cmd;

import static org.basex.core.Text.*;

import java.io.*;

import org.basex.core.*;
import org.basex.core.locks.*;
import org.basex.core.users.*;
import org.basex.data.*;

/**
 * Evaluates the 'inspect' command: checks if a database has inconsistent data structures.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class Inspect extends Command {
  /**
   * Default constructor.
   */
  public Inspect() {
    this(null);
  }

  /**
   * Constructor.
   * @param name name of database (if {@code null}, the opened database is inspected)
   */
  public Inspect(final String name) {
    super(name == null ? Perm.READ : Perm.NONE, name == null, name == null ? "" : name);
  }

  @Override
  protected boolean run() throws IOException {
    final String name = args[0];
    if(name.isEmpty()) return inspect(context.data());

    if(!Databases.validName(name)) return error(NAME_INVALID_X, name);
    final Data data;
    try {
      data = Open.open(name, context, options, true, true);
    } catch(final IOException ex) {
      return error(ex);
    }
    try {
      return inspect(data);
    } finally {
      Close.close(data, context);
    }
  }

  @Override
  public void addLocks() {
    jc().locks.reads.add(args[0].isEmpty() ? Locking.CONTEXT : args[0]);
  }

  /**
   * Inspects the specified database.
   * @param data data reference
   * @return success flag
   * @throws IOException I/O exception
   */
  private boolean inspect(final Data data) throws IOException {
    out.print(new Inspection(data, this).info());
    return info("'%' inspected in %.", data.meta.name, jc().performance);
  }
}
