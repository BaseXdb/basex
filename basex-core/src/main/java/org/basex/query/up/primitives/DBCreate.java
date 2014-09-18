package org.basex.query.up.primitives;

import static org.basex.query.util.Err.*;

import java.io.*;
import java.util.*;
import java.util.List;

import org.basex.build.*;
import org.basex.core.*;
import org.basex.core.cmd.*;
import org.basex.data.*;
import org.basex.data.atomic.*;
import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.util.*;
import org.basex.util.options.*;

/**
 * Update primitive for the {@link Function#_DB_CREATE} function.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Lukas Kircher
 */
public final class DBCreate extends NameUpdate {
  /** Container for new database documents. */
  private final DBNew add;
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
    final ArrayList<Option<?>> supported = new ArrayList<>();
    Collections.addAll(supported, DBOptions.INDEXING);
    Collections.addAll(supported, DBOptions.PARSING);
    options = new DBOptions(opts.free(), supported, info);
    add = new DBNew(qc, input, info);
  }

  @Override
  public void prepare() throws QueryException {
    if(add.inputs == null || add.inputs.isEmpty()) return;

    final MainOptions opts = qc.context.options;
    options.assign(opts);
    try {
      add.addDocs(new MemData(opts), name);
    } finally {
      options.reset(opts);
    }
  }

  @Override
  public void apply() throws QueryException {
    close();

    final MainOptions opts = qc.context.options;
    options.assign(opts);
    try {
      final Data data = CreateDB.create(name, Parser.emptyParser(opts), qc.context);

      // add initial documents and optimize database
      if(add.data != null) {
        data.startUpdate();
        try {
          data.insert(data.meta.size, -1, new DataClip(add.data));
          Optimize.optimize(data, null);
        } finally {
          data.finishUpdate();
        }
      }
      Close.close(data, qc.context);

    } catch(final IOException ex) {
      throw UPDBOPTERR_X.get(info, ex);
    } finally {
      options.reset(opts);
    }
  }

  @Override
  public String toString() {
    return Util.className(this) + '[' + add.inputs + ']';
  }

  @Override
  public String operation() { return "created"; }
}
