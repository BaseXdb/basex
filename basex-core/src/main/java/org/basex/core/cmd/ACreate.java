package org.basex.core.cmd;

import static org.basex.core.Text.*;

import java.io.*;

import org.basex.core.*;
import org.basex.data.*;
import org.basex.index.*;
import org.basex.io.*;
import org.basex.util.*;

/**
 * Abstract class for database creation commands.
 *
 * @author BaseX Team 2005-14, BSD License
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
   * @return IO reference
   * @throws IOException I/O exception
   */
  final IO sourceToIO(final String name) throws IOException {
    IO io = null;
    if(args[1] != null && !args[1].isEmpty()) {
      io = IO.get(args[1]);
    } else if(in != null) {
      if(in.getCharacterStream() != null) {
        final TokenBuilder tb = new TokenBuilder();
        try(final Reader r = in.getCharacterStream()) {
          for(int c; (c = r.read()) != -1;) tb.add(c);
        }
        io = new IOContent(tb.finish());
      } else if(in.getByteStream() != null) {
        io = new IOStream(in.getByteStream());
      } else if(in.getSystemId() != null) {
        io = IO.get(in.getSystemId());
      }
    }
    // update name if not given by the IO reference anyway
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

  /**
   * Builds the specified index.
   * @param type index to be built
   * @param data data reference
   * @param options main options
   * @param cmd calling command
   * @throws IOException I/O exception
   */
  static void create(final IndexType type, final Data data, final MainOptions options,
      final ACreate cmd) throws IOException {

    data.meta.dirty = true;
    final boolean ok = data.dropIndex(type);
    if(ok) {
      if(type == IndexType.TEXT) {
        data.meta.textindex = true;
      } else if(type == IndexType.ATTRIBUTE) {
        data.meta.attrindex = true;
      } else if(type == IndexType.FULLTEXT) {
        data.meta.ftxtindex = true;
      } else {
        throw Util.notExpected();
      }
    }
    data.createIndex(type, options, cmd);
  }

  /**
   * Drops the specified index.
   * @param type index type
   * @param data data reference
   * @return success flag
   */
  static boolean drop(final IndexType type, final Data data) {
    data.meta.dirty = true;
    final boolean ok = data.dropIndex(type);
    if(ok) {
      if(type == IndexType.TEXT) {
        data.meta.textindex = false;
      } else if(type == IndexType.ATTRIBUTE) {
        data.meta.attrindex = false;
      } else if(type == IndexType.FULLTEXT) {
        data.meta.ftxtindex = false;
      } else {
        throw Util.notExpected();
      }
    }
    return ok;
  }

  @Override
  public boolean newData(final Context ctx) {
    if(newData) new Close().run(ctx);
    return newData;
  }

  @Override
  public void databases(final LockResult lr) {
    // default implementation for commands accessing (exclusively) the opened database
    lr.write.add(DBLocking.CTX);
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
