package org.basex.query.up.primitives.db;

import static org.basex.query.QueryError.*;

import java.util.*;

import org.basex.core.*;
import org.basex.data.*;
import org.basex.query.*;
import org.basex.query.up.primitives.*;
import org.basex.util.*;

/**
 * Add primitive.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Dimitar Popov
 */
public final class DBAdd extends DBUpdate {
  /** Container for new documents. */
  private final DBNew newDocs;
  /** Replace flag. */
  private final boolean replace;
  /** Data clip with generated input. */
  private DataClip clip;
  /** Size. */
  private int size;

  /**
   * Constructor.
   * @param data target database
   * @param input document to add (IO or ANode instance)
   * @param qopts query options
   * @param replace replace flag
   * @param qc query context
   * @param info input info (can be {@code null})
   * @throws QueryException query exception
   */
  public DBAdd(final Data data, final NewInput input, final HashMap<String, String> qopts,
      final boolean replace, final QueryContext qc, final InputInfo info) throws QueryException {

    super(UpdateType.DBADD, data, info);
    this.replace = replace;

    final DBOptions dbopts = new DBOptions(qopts, MainOptions.PARSING, info);
    final MainOptions mopts = dbopts.assignTo(new MainOptions(qc.context.options, false));
    newDocs = new DBNew(qc, mopts, info, input);
  }

  @Override
  public void prepare() throws QueryException {
    size = newDocs.inputs.size();
    clip = newDocs.prepare(data.meta.name, false);
  }

  @Override
  public void apply() throws QueryException {
    try {
      newDocs.addTo(data);
    } finally {
      clip.finish();
    }
  }

  @Override
  public void merge(final Update update) throws QueryException {
    final DBAdd add = (DBAdd) update;
    if(replace || add.replace) {
      final NewInput input = newDocs.inputs.get(0), addInput = add.newDocs.inputs.get(0);
      String path = input.path, addPath = addInput.path;
      if(input.io != null) path += '/' + input.io.name();
      if(addInput.io != null) addPath += '/' + addInput.io.name();
      if(path.equals(addPath)) throw UPMULTDOC_X_X.get(info, data.meta.name, addPath);
    }
    newDocs.merge(add.newDocs);
  }

  @Override
  public int size() {
    return size;
  }

  @Override
  public String toString() {
    return Util.className(this) + '[' + newDocs.inputs + ']';
  }
}
