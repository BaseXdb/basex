package org.basex.query.up.primitives.name;

import static org.basex.query.QueryError.*;

import java.io.*;
import java.util.*;
import java.util.List;

import org.basex.build.*;
import org.basex.core.*;
import org.basex.core.cmd.*;
import org.basex.data.*;
import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.up.primitives.*;
import org.basex.util.*;
import org.basex.util.options.*;

/**
 * Update primitive for the {@link Function#_DB_CREATE} function.
 *
 * @author BaseX Team 2005-16, BSD License
 * @author Lukas Kircher
 */
public final class DBCreate extends NameUpdate {
  /** Container for new database documents. */
  private final DBNew newDocs;
  /** Database update options. */
  private final DBOptions options;

  /**
   * Constructor.
   * @param name name for created database
   * @param input input (ANode and QueryInput references)
   * @param opts database options
   * @param qc query context
   * @param info input info
   * @throws QueryException query exception
   */
  public DBCreate(final String name, final List<NewInput> input, final Options opts,
      final QueryContext qc, final InputInfo info) throws QueryException {

    super(UpdateType.DBCREATE, name, info, qc);
    final List<Option<?>> supported = new ArrayList<>();
    Collections.addAll(supported, DBOptions.INDEXING);
    Collections.addAll(supported, DBOptions.PARSING);
    options = new DBOptions(opts, supported, info);
    newDocs = new DBNew(qc, input, options, info);
  }

  @Override
  public void prepare() throws QueryException {
    newDocs.prepare(name);
  }

  @Override
  public void apply() throws QueryException {
    try {
      // close existing database instance; raise error if it is still pinned or locked
      close();

      // create new database
      final MainOptions mopts = options.assignTo(new MainOptions(qc.context.options, true));
      final Data data = CreateDB.create(name, Parser.emptyParser(mopts), qc.context, mopts);

      // add initial documents and optimize database
      final Data newData = newDocs.data;
      if(newData != null) {
        data.startUpdate(mopts);
        try {
          data.insert(data.meta.size, -1, new DataClip(newData));
          Optimize.optimize(data, null);
        } finally {
          data.finishUpdate(mopts);
        }
      }
      Close.close(data, qc.context);

    } catch(final IOException ex) {
      throw UPDBOPTERR_X.get(info, ex);
    } finally {
      // drop temporary database instance
      newDocs.finish();
    }
  }

  @Override
  public String toString() {
    return Util.className(this) + '[' + newDocs.inputs + ']';
  }

  @Override
  public String operation() { return "created"; }
}
