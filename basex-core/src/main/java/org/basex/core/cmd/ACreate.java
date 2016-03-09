package org.basex.core.cmd;

import static org.basex.core.Text.*;

import java.io.*;

import org.basex.core.*;
import org.basex.core.locks.*;
import org.basex.core.users.*;
import org.basex.io.*;

/**
 * Abstract class for database creation commands.
 *
 * @author BaseX Team 2005-16, BSD License
 * @author Christian Gruen
 */
public abstract class ACreate extends Command {
  /** Flag for closing a data instances before executing the command. */
  private boolean newData;

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
        try(final Reader r = in.getCharacterStream()) {
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
      if(name.endsWith("/")) throw new BaseXException(NAME_INVALID_X, name);
      io.name(name.isEmpty() ? "" : name + '.' + options.get(MainOptions.PARSER));
    }
    return io;
  }

  /**
   * Starts an update operation.
   * @return success flag
   */
  final boolean startUpdate() {
    return startUpdate(context.data());
  }

  /**
   * Finalizes an update operation.
   * @return success flag
   */
  final boolean finishUpdate() {
    return finishUpdate(context.data());
  }

  @Override
  public boolean newData(final Context ctx) {
    if(newData) new Close().run(ctx);
    return newData;
  }

  @Override
  public void databases(final LockResult lr) {
    // default implementation for commands accessing (exclusively) the opened database
    lr.write.add(DBLocking.CONTEXT);
  }

  @Override
  public final boolean supportsProg() {
    return true;
  }

  @Override
  public boolean stoppable() {
    return true;
  }
}
