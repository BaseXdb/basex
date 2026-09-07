package org.basex.query.up.primitives.name;

import static org.basex.query.QueryError.*;

import java.io.*;
import java.util.*;

import org.basex.build.*;
import org.basex.core.*;
import org.basex.core.cmd.*;
import org.basex.data.*;
import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.up.primitives.*;
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
  public DBCreate(final String name, final NewInput[] inputs, final HashMap<String, String> qopts,
      final QueryContext qc, final InputInfo info) throws QueryException {

    super(UpdateType.DBCREATE, name, qc, info);
    final DBOptions dbopts = new DBOptions(qopts, MainOptions.CREATING, info);
    options = dbopts.assignTo(new MainOptions(qc.context.options, false));
    newDocs = new DBNew(qc, options, info, inputs);
  }

  @Override
  public void prepare() throws QueryException {
    clip = newDocs.prepare(name, true);
  }

  @Override
  public void apply() throws QueryException {
    Data data = null;
    try {
      // close existing database instance; raise error if it is still pinned or locked
      close();

      // create new database
      data = CreateDB.create(name, Parser.emptyParser(options), qc.context, options);

      // add initial documents and optimize database
      if(clip != null) {
        data.startUpdate(options);
        try {
          newDocs.addTo(data, false);
          Optimize.optimize(data, null);
        } finally {
          data.finishUpdate(options);
        }
      }
    } catch(final IOException ex) {
      throw UPDBERROR_X.get(info, ex);
    } finally {
      // release the database instance, also if the creation was interrupted
      if(data != null) Close.close(data, qc.context);
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
