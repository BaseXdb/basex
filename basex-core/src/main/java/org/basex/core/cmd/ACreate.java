package org.basex.core.cmd;

import static org.basex.core.Text.*;

import java.io.*;

import org.basex.core.*;
import org.basex.core.locks.*;
import org.basex.core.users.*;
import org.basex.data.*;
import org.basex.io.*;
import org.basex.util.*;

/**
 * Abstract class for database updates.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public abstract class ACreate extends Command {
  /** Flag for closing a data instances before executing the command. */
  private boolean newData;
  /** Indicates if database should be locked. */
  protected boolean lock = true;

  /**
   * Protected constructor, specifying command arguments.
   * @param args arguments
   */
  ACreate(final String... args) {
    this(Perm.CREATE, false, args);
    newData = true;
  }

  /**
   * Protected constructor, specifying command flags and arguments.
   * @param perm required permission
   * @param openDB requires opened database
   * @param args arguments
   */
  ACreate(final Perm perm, final boolean openDB, final String... args) {
    super(perm, openDB, args);
  }

  /**
   * Converts the input (second argument of {@link #args}, or {@link #in} reference)
   * to an {@link IO} reference.
   * @param name name of source
   * @return IO reference (can be {@code null})
   * @throws IOException I/O exception
   */
  final IO sourceToIO(final String name) throws IOException {
    IO io = null;
    if(args[1] != null && !args[1].isEmpty()) {
      io = IO.get(args[1]);
    } else if(in != null) {
      if(in.getCharacterStream() != null) {
        final StringBuilder sb = new StringBuilder();
        try(Reader r = in.getCharacterStream()) {
          for(int c; (c = r.read()) != -1;) sb.append((char) c);
        }
        io = new IOContent(sb.toString());
      } else if(in.getByteStream() != null) {
        io = new IOStream(in.getByteStream());
      } else if(in.getSystemId() != null) {
        io = IO.get(in.getSystemId());
      }
    }

    // assign (intermediate) name to input reference
    if(io instanceof IOContent || io instanceof IOStream) {
      if(Strings.endsWith(name, '/')) throw new BaseXException(NAME_INVALID_X, name);
      io.name(name.isEmpty() ? "" : name + '.' + options.get(MainOptions.PARSER));
    }
    return io;
  }

  /**
   * Runs an update operation.
   * @param data data reference
   * @param update update operation
   * @return success flag
   */
  final boolean update(final Data data, final Code update) {
    IOException exc = null;
    try {
      // prepare update, set locks
      if(lock) data.startUpdate(options);
      // perform update, return success flag
      return update.run();
    } catch(final IOException ex) {
      exc = ex;
    } finally {
      try {
        // finish update, remove locks
        Optimize.finish(data);
        if(lock) data.finishUpdate(options);
      } catch(final IOException ex) {
        // do not overwrite existing error if something goes wrong
        if(exc == null) exc = ex;
        else Util.debug(ex);
      }
    }
    return error(Util.message(exc));
  }

  @Override
  public boolean newData(final Context ctx) {
    if(newData) Close.close(ctx);
    return newData;
  }

  @Override
  public void addLocks() {
    // default implementation for commands accessing (exclusively) the opened database
    jc().locks.writes.add(Locking.CONTEXT);
  }

  @Override
  public final boolean supportsProg() {
    return true;
  }

  @Override
  public boolean stoppable() {
    return true;
  }

  /**
   * Update code.
   *
   * @author BaseX Team 2005-21, BSD License
   * @author Christian Gruen
   */
  abstract static class Code {
    /**
     * Runs the update.
     * @return success flag
     * @throws IOException I/O exception
     */
    abstract boolean run() throws IOException;
  }
}
