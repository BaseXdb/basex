package org.basex.core.cmd;

import static org.basex.data.DataText.*;

import java.io.*;

import org.basex.core.*;
import org.basex.data.*;
import org.basex.index.*;
import org.basex.index.IndexToken.IndexType;
import org.basex.index.ft.*;
import org.basex.index.value.*;
import org.basex.io.*;
import org.basex.util.*;

/**
 * Abstract class for database creation commands.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public abstract class ACreate extends Command {
  /** Flag for closing a data instances before executing the command. */
  private boolean newData;

  /**
   * Protected constructor, specifying command arguments.
   * @param arg arguments
   */
  ACreate(final String... arg) {
    this(Perm.CREATE, false, arg);
    newData = true;
  }

  /**
   * Protected constructor, specifying command flags and arguments.
   * @param p required permission
   * @param d requires opened database
   * @param arg arguments
   */
  ACreate(final Perm p, final boolean d, final String... arg) {
    super(p, d, arg);
  }

  @Override
  public boolean newData(final Context ctx) {
    if(newData) new Close().run(ctx);
    return newData;
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
   * Converts the input source to an {@link IO} reference.
   * @param name name of source
   * @return IO reference
   * @throws IOException I/O exception
   */
  protected IO sourceToIO(final String name) throws IOException {
    IO io = null;
    if(args[1] != null) {
      io = IO.get(args[1]);
    } else if(in != null) {
      if(in.getCharacterStream() != null) {
        final Reader r = in.getCharacterStream();
        final TokenBuilder tb = new TokenBuilder();
        try {
          for(int c; (c = r.read()) != -1;) tb.add(c);
        } finally {
          r.close();
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
      io.name(name.isEmpty() ? "" : name + '.' + prop.get(Prop.PARSER));
    }
    return io;
  }

  /**
   * Builds the specified index.
   * @param index index to be built
   * @param data data reference
   * @param cmd calling command
   * @throws IOException I/O exception
   */
  protected static void create(final IndexType index, final Data data, final ACreate cmd)
      throws IOException {

    if(data.inMemory()) return;

    final IndexBuilder ib;
    switch(index) {
      case TEXT:      ib = new ValueBuilder(data, true); break;
      case ATTRIBUTE: ib = new ValueBuilder(data, false); break;
      case FULLTEXT:  ib = new FTBuilder(data); break;
      default:        throw Util.notexpected();
    }
    data.closeIndex(index);
    data.setIndex(index, (cmd == null ? ib : cmd.progress(ib)).build());
  }

  /**
   * Drops the specified index.
   * @param index index type
   * @param data data reference
   * @return success of operation
   */
  protected static boolean drop(final IndexType index, final Data data) {
    String pat = null;
    switch(index) {
      case TEXT:
        data.meta.textindex = false;
        pat = DATATXT;
        break;
      case ATTRIBUTE:
        data.meta.attrindex = false;
        pat = DATAATV;
        break;
      case FULLTEXT:
        data.meta.ftxtindex = false;
        pat = DATAFTX;
        break;
      default:
    }
    data.closeIndex(index);
    data.meta.dirty = true;
    return pat == null || data.meta.drop(pat + '.');
  }
}
