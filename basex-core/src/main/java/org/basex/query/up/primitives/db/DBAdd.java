package org.basex.query.up.primitives.db;

import static org.basex.query.QueryError.*;

import java.util.*;

import org.basex.core.*;
import org.basex.data.*;
import org.basex.index.resource.*;
import org.basex.query.*;
import org.basex.query.up.primitives.*;
import org.basex.util.*;

/**
 * Add primitive.
 *
 * @author BaseX Team, BSD License
 * @author Dimitar Popov
 */
public final class DBAdd extends DBUpdate {
  /** Container for new documents. */
  private final DBNew newDocs;
  /** Replace flag. */
  private final boolean replace;
  /** Recorded XML target paths (path + '/' + io-name when applicable). */
  private final HashSet<String> xmlPaths = new HashSet<>();
  /** Recorded binary target paths. */
  private final HashSet<String> binaryPaths = new HashSet<>();
  /** Recorded value target paths. */
  private final HashSet<String> valuePaths = new HashSet<>();
  /** Data clip with generated input. */
  private DataClip clip;
  /** Size. */
  private int size;

  /**
   * Constructor.
   * @param data target database
   * @param qopts query options
   * @param replace replace flag
   * @param qc query context
   * @param info input info (can be {@code null})
   * @param inputs documents to add (IO or ANode instances)
   * @throws QueryException query exception
   */
  public DBAdd(final Data data, final HashMap<String, String> qopts, final boolean replace,
      final QueryContext qc, final InputInfo info, final NewInput... inputs)
      throws QueryException {

    super(UpdateType.DBADD, data, info);
    this.replace = replace;

    final DBOptions dbopts = new DBOptions(qopts, MainOptions.PARSING, info);
    final MainOptions mopts = dbopts.assignTo(new MainOptions(qc.context.options, false));
    newDocs = new DBNew(qc, mopts, info, inputs);
    for(final NewInput input : inputs) {
      paths(input.type).add(pathKey(input));
    }
  }

  /**
   * Returns the path set for the specified resource type.
   * @param rt resource type
   * @return path set
   */
  private HashSet<String> paths(final ResourceType rt) {
    return switch(rt) {
      case XML    -> xmlPaths;
      case BINARY -> binaryPaths;
      case VALUE  -> valuePaths;
    };
  }

  /**
   * Returns the path key recorded in the path set for the specified input.
   * For XML inputs originating from an IO reference, the file name is appended,
   * matching the behavior of the legacy nested-loop conflict check.
   * @param input new input
   * @return path key
   */
  private static String pathKey(final NewInput input) {
    return input.type == ResourceType.XML && input.io != null
        ? input.path + '/' + input.io.name() : input.path;
  }

  @Override
  public void prepare() throws QueryException {
    size = newDocs.inputs.size();
    clip = newDocs.prepare(data.meta.name, false);
    checkLimit(clip.size());
  }

  @Override
  public void apply() throws QueryException {
    try {
      newDocs.addTo(data, replace);
    } finally {
      clip.finish();
    }
  }

  @Override
  public void merge(final Update update) throws QueryException {
    final DBAdd add = (DBAdd) update;
    for(final NewInput input : add.newDocs.inputs) {
      final String key = pathKey(input);
      final HashSet<String> set = paths(input.type);
      if(input.type == ResourceType.XML) {
        if((replace || add.replace) && set.contains(key)) {
          throw UPMULTDOC_X_X.get(info, data.meta.name, key);
        }
      } else if(set.contains(key)) {
        throw DB_CONFLICT5_X.get(info, key);
      }
      set.add(key);
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
