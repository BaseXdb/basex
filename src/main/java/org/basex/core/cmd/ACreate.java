package org.basex.core.cmd;

import static org.basex.core.Text.*;
import static org.basex.data.DataText.*;

import java.io.IOException;

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
import org.basex.data.MetaData;
import org.basex.index.IndexBuilder;
import org.basex.index.IndexToken.IndexType;
import org.basex.index.ft.FTBuilder;
import org.basex.index.path.PathBuilder;
import org.basex.index.value.ValueBuilder;
import org.basex.util.Util;

/**
 * Abstract class for database creation commands.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public abstract class ACreate extends Command {
  /** Flag for creating new data instances. */
  private boolean closing;

  /**
   * Protected constructor, specifying command arguments.
   * @param arg arguments
   */
  ACreate(final String... arg) {
    this(User.CREATE, arg);
    closing = true;
  }

  /**
   * Protected constructor, specifying command flags and arguments.
   * @param flags command flags
   * @param arg arguments
   */
  ACreate(final int flags, final String... arg) {
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
  final boolean build(final Parser parser, final String db) {
    if(!MetaData.validName(db, false)) return error(NAME_INVALID_X, db);

    // close open database
    new Close().run(context);

    try {
      if(context.pinned(db)) return error(DB_PINNED_X, db);

      // database builder instance.
      if(prop.is(Prop.MAINMEM)) {
        final Data data = progress(new MemBuilder(db, parser, prop)).build();
        context.openDB(data);
        context.pin(data);
      } else {
        Data data = progress(new DiskBuilder(db, parser, context)).build();
        data.close();
        final Open open = new Open(db);
        if(!open.run(context)) return error(open.info());

        data = context.data();
        data.meta.pathindex = data.meta.createpath;
        if(data.meta.createtext) create(IndexType.TEXT,      data, this);
        if(data.meta.createattr) create(IndexType.ATTRIBUTE, data, this);
        if(data.meta.createftxt) create(IndexType.FULLTEXT,  data, this);
        data.flush();
      }
      return info(parser.info() + DB_CREATED_X_X, db, perf);
    } catch(final ProgressException ex) {
      throw ex;
    } catch(final IOException ex) {
      Util.debug(ex);
      abort();
      final String msg = ex.getMessage();
      return error(msg != null && !msg.isEmpty() ? msg :
        Util.info(NOT_PARSED_X, parser.src));
    } catch(final Exception ex) {
      // known exceptions:
      // - IllegalArgumentException (UTF8, zip files)
      Util.debug(ex);
      abort();
      return error(Util.info(NOT_PARSED_X, parser.src));
    }
  }

  /**
   * Builds the specified index.
   * @param index index to be built
   * @param data data reference
   * @param cmd calling command
   * @throws IOException I/O exception
   */
  static void create(final IndexType index, final Data data,
      final ACreate cmd) throws IOException {

    if(data instanceof MemData) return;
    final IndexBuilder ib;
    switch(index) {
      case TEXT:      ib = new ValueBuilder(data, true); break;
      case ATTRIBUTE: ib = new ValueBuilder(data, false); break;
      case FULLTEXT:  ib = FTBuilder.get(data); break;
      case PATH:      ib = new PathBuilder(data); break;
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
   * @throws IOException I/O exception
   */
  static boolean drop(final IndexType index, final Data data)
      throws IOException {

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
      case PATH:
        data.meta.pathindex = false;
        break;
      default:
    }
    data.closeIndex(index);
    data.meta.dirty = true;
    data.flush();
    return pat == null || data.meta.drop(pat + '.');
  }
}
