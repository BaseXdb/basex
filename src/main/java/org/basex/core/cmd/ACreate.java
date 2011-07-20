package org.basex.core.cmd;

import static org.basex.core.Text.*;
import static org.basex.util.Token.*;

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
   * @param a arguments
   */
  protected ACreate(final String... a) {
    this(User.CREATE, a);
    closing = true;
  }

  /**
   * Protected constructor, specifying command flags and arguments.
   * @param p command properties
   * @param a arguments
   */
  protected ACreate(final int p, final String... a) {
    super(p, a);
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
   * @param p parser instance
   * @param db name of database
   * @return success of operation
   */
  protected final boolean build(final Parser p, final String db) {
    if(!validName(db, false)) return error(NAMEINVALID, db);

    // close open database
    new Close().run(context);

    try {
      if(context.pinned(db)) return error(DBLOCKED, db);

      final boolean mem = prop.is(Prop.MAINMEM);
      builder = mem ? new MemBuilder(p, prop) : new DiskBuilder(p, context);

      Data data = progress(builder).build(db);
      if(mem) {
        context.openDB(data);
        context.pin(data);
      } else {
        data.close();
        final Open open = new Open(db);
        if(!open.run(context)) return error(open.info());

        data = context.data;
        if(prop.is(Prop.TEXTINDEX)) index(IndexType.TEXT,      data);
        if(prop.is(Prop.ATTRINDEX)) index(IndexType.ATTRIBUTE, data);
        if(prop.is(Prop.FTINDEX))   index(IndexType.FULLTEXT,  data);
        data.flush();
      }
      return info(DBCREATED, db, perf);
    } catch(final ProgressException ex) {
      throw ex;
    } catch(final IOException ex) {
      Util.debug(ex);
      abort();
      final String msg = ex.getMessage();
      return error(msg != null && msg.length() != 0 ? msg :
        Util.info(PARSEERR, p.src));
    } catch(final Exception ex) {
      // known exceptions:
      // - IllegalArgumentException (UTF8, zip files)
      Util.debug(ex);
      abort();
      return error(Util.info(PARSEERR, p.src));
    }
  }

  /**
   * Runs the specified command and adopts its info message.
   * @param cmd command
   * @return success flag
   */
  protected boolean run(final Command cmd) {
    final boolean ok = cmd.run(context);
    error(cmd.info());
    return ok;
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

  /**
   * Normalizes the database path.
   * Removes duplicate, leading and trailing slashes
   * @param path input path
   * @return normalized path
   */
  public static final String path(final String path) {
    return path.replaceAll("[\\\\/]+", "/").replaceAll("^/|/$", "");
  }

  /**
   * Generate a new name for a document.
   * @param d data
   * @param pre pre value of the document
   * @param src source path
   * @param trg target path
   * @return new name
   */
  public static byte[] newName(final Data d, final int pre, final byte[] src,
      final byte[] trg) {

    final byte[] path = d.text(pre, true);
    byte[] target = trg;
    byte[] name = substring(path, src.length);
    if(name.length != 0) {
      // change file path: replace all paths with the target path
      if(startsWith(name, '/')) name = substring(name, 1);
      target = trg.length != 0 ? concat(trg, SLASH, name) : name;
    }
    return target;
  }
}
