package org.basex.query.up.primitives.name;

import static org.basex.core.Text.*;
import static org.basex.query.QueryError.*;

import java.io.*;

import org.basex.build.*;
import org.basex.core.*;
import org.basex.core.cmd.*;
import org.basex.core.users.*;
import org.basex.data.*;
import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.up.primitives.*;
import org.basex.query.value.map.*;
import org.basex.util.*;

/**
 * Update primitive for the {@link Function#_DB_CREATE} function.
 *
 * @author BaseX Team, BSD License
 * @author Lukas Kircher
 */
public final class DBCreate extends NameUpdate {
  /** Container for new documents. */
  private final DBNew newDocs;
  /** Main options. */
  private final MainOptions options;
  /** Data clip with input (can be {@code null}). */
  private DataClip clip;

  /**
   * Constructor.
   * @param name name for created database
   * @param inputs inputs (ANode and QueryInput references)
   * @param qopts query options
   * @param qc query context
   * @param info input info (can be {@code null})
   * @throws QueryException query exception
   */
  public DBCreate(final String name, final NewInput[] inputs, final XQMap qopts,
      final QueryContext qc, final InputInfo info) throws QueryException {

    super(UpdateType.DBCREATE, name, qc, info);
    final DBOptions dbopts = new DBOptions(qopts, MainOptions.CREATING, qc, info);
    options = dbopts.assignTo(new MainOptions(qc.context.options, false));
    newDocs = new DBNew(qc, options, info, inputs);
  }

  @Override
  public void prepare() throws QueryException {
    clip = newDocs.prepare(name, true);
  }

  @Override
  public void apply() throws QueryException {
    final Context ctx = qc.context;
    Data data = null;
    try {
      // close existing database instance; raise error if it is still pinned or locked
      close();

      final boolean move = clip != null && !clip.data.inMemory();
      if(move) {
        // temporary database on disk: rename it, and detach it from the clip
        if(!ctx.user().has(Perm.CREATE)) throw new BaseXException(PERM_REQUIRED_X, Perm.CREATE);
        final String tmpName = clip.data.meta.name;
        clip.data.close();
        clip.context(null);
        if(!AlterDB.alter(tmpName, name, ctx.soptions)) {
          DropDB.drop(tmpName, ctx.soptions);
          throw UPDBERROR_X_X.get(info, name, operation());
        }
        data = Open.open(name, ctx, options, true, true);
      } else {
        data = CreateDB.create(name, Parser.emptyParser(options), ctx, options);
      }

      // add initial documents and optimize database
      if(clip != null) {
        data.startUpdate(options);
        try {
          if(!move) newDocs.addTo(data, false);
          // release temporary data before index structures are built
          clip.finish();
          Optimize.optimize(data, null);
        } finally {
          data.finishUpdate(options);
        }
      }
    } catch(final IOException ex) {
      throw UPDBERROR_X.get(info, ex);
    } finally {
      // release the database instance, also if the creation was interrupted
      if(data != null) Close.close(data, ctx);
      if(clip != null) clip.finish();
    }
  }

  @Override
  public String operation() {
    return "created";
  }

  @Override
  public String toString() {
    return Util.className(this) + '[' + newDocs.inputs + ']';
  }
}
