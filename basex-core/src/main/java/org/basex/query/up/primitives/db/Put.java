package org.basex.query.up.primitives.db;

import static org.basex.query.QueryError.*;

import java.io.*;
import java.util.*;

import org.basex.data.*;
import org.basex.io.*;
import org.basex.io.out.*;
import org.basex.io.serial.*;
import org.basex.query.*;
import org.basex.query.up.primitives.*;
import org.basex.query.value.node.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * Fn:put operation primitive.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Lukas Kircher
 */
public final class Put extends DBUpdate {
  /** Target paths. The same node can be stored in multiple locations. */
  private final StringList paths = new StringList(1);
  /** Serializer options. */
  private final ArrayList<SerializerOptions> options = new ArrayList<>();
  /** Node id of the target node. Target nodes are identified via their ID, as structural
   *  changes (delete/insert) during the snapshot lead to PRE value shifts on disk.
   *  In addition, deleted/replaced nodes will not be serialized by fn:put as the
   *  identity of these nodes is gone - which is easier to track operating on IDs. */
  public final int id;

  /**
   * Constructor.
   * @param id target node id
   * @param data target data reference
   * @param path target path
   * @param sopts serializer options
   * @param info input info
   */
  public Put(final int id, final Data data, final String path, final SerializerOptions sopts,
      final InputInfo info) {
    super(UpdateType.FNPUT, data, info);
    this.id = id;
    paths.add(path);
    options.add(sopts);
  }

  @Override
  public void prepare() {
  }

  @Override
  public void apply() throws QueryException {
    final int pre = data.pre(id);
    if(pre == -1) return;

    final int pl = paths.size();
    for(int p = 0; p < pl; p++) {
      final IOFile path = new IOFile(paths.get(p));
      final DBNode node = new DBNode(data, pre);
      try(PrintOutput po = new PrintOutput(path)) {
        try(Serializer ser = Serializer.get(po, options.get(p))) {
          ser.serialize(node);
        }
      } catch(final IOException ex) {
        Util.debug(ex);
        throw UPPUTERR_X.get(info, path);
      }
    }
  }

  @Override
  public void merge(final Update update) {
    for(final String path : ((Put) update).paths) paths.add(path);
  }

  @Override
  public int size() {
    return paths.size();
  }

  @Override
  public String toString() {
    return Util.className(this) + '[' + id + ", " + paths.get(0) + ']';
  }
}
