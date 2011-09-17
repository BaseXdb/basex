package org.basex.core.cmd;

import static org.basex.core.Text.*;

import java.io.IOException;
import org.basex.build.Builder;
import org.basex.build.DiskBuilder;
import org.basex.build.MemBuilder;
import org.basex.build.Parser;
import org.basex.core.Command;
import org.basex.core.Context;
import org.basex.core.ProgressException;
import org.basex.core.Prop;
import org.basex.core.User;
import org.basex.data.Data;
import org.basex.data.MemData;
import org.basex.index.IndexBuilder;
import org.basex.index.IndexToken.IndexType;
import org.basex.index.ft.FTBuilder;
import org.basex.index.path.PathBuilder;
import org.basex.index.value.ValueBuilder;
import org.basex.util.Util;

/**
 * Abstract class for database creation commands.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public abstract class ACreate extends Command {
  /** Builder instance. */
  private Builder builder;
  /** Flag for creating new data instances. */
  private boolean closing;

  /**
   * Protected constructor, specifying command arguments.
   * @param arg arguments
   */
  protected ACreate(final String... arg) {
    this(User.CREATE, arg);
    closing = true;
  }

  /**
   * Protected constructor, specifying command flags and arguments.
   * @param flags command flags
   * @param arg arguments
   */
  protected ACreate(final int flags, final String... arg) {
    super(flags, arg);
  }

  @Override
  public boolean newData(final Context ctx) {
    if(closing) new Close().run(ctx);
    return closing;
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
   * Builds and creates a new database instance.
   * @param parser parser instance
   * @param db name of database
   * @return success of operation
   */
  protected final boolean build(final Parser parser, final String db) {
    if(!validName(db, false)) return error(NAMEINVALID, db);

    // close open database
    new Close().run(context);

    try {
      if(context.pinned(db)) return error(DBLOCKED, db);

      final boolean mem = prop.is(Prop.MAINMEM);
      builder = mem ? new MemBuilder(db, parser, prop) :
        new DiskBuilder(db, parser, context);

      Data data = progress(builder).build();
      if(mem) {
        context.openDB(data);
        context.pin(data);
      } else {
        data.close();
        final Open open = new Open(db);
        if(!open.run(context)) return error(open.info());

        data = context.data();
        if(prop.is(Prop.TEXTINDEX)) index(IndexType.TEXT,      data);
        if(prop.is(Prop.ATTRINDEX)) index(IndexType.ATTRIBUTE, data);
        if(prop.is(Prop.FTINDEX))   index(IndexType.FULLTEXT,  data);
        data.flush();
      }
      return info(parser.info() + DBCREATED, db, perf);
    } catch(final ProgressException ex) {
      throw ex;
    } catch(final IOException ex) {
      Util.debug(ex);
      abort();
      final String msg = ex.getMessage();
      return error(msg != null && msg.length() != 0 ? msg :
        Util.info(PARSEERR, parser.src));
    } catch(final Exception ex) {
      // known exceptions:
      // - IllegalArgumentException (UTF8, zip files)
      Util.debug(ex);
      abort();
      return error(Util.info(PARSEERR, parser.src));
    }
  }

  /**
   * Builds the specified index.
   * @param type index to be built
   * @param data data reference
   * @throws IOException I/O exception
   */
  protected final void index(final IndexType type, final Data data)
      throws IOException {
    index(type, data, this);
  }

  /**
   * Builds the specified index.
   * @param type index to be built
   * @param data data reference
   * @param cmd calling command
   * @throws IOException I/O exception
   */
  protected static void index(final IndexType type, final Data data,
      final ACreate cmd) throws IOException {

    if(data instanceof MemData) return;
    IndexBuilder ib = null;
    switch(type) {
      case TEXT:      ib = new ValueBuilder(data, true); break;
      case ATTRIBUTE: ib = new ValueBuilder(data, false); break;
      case FULLTEXT:  ib = FTBuilder.get(data); break;
      case PATH:      ib = new PathBuilder(data); break;
      default:        throw Util.notexpected();
    }
    data.closeIndex(type);
    data.setIndex(type, (cmd == null ? ib : cmd.progress(ib)).build());
  }
}
