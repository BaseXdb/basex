package org.basex.query.up.primitives;

import static org.basex.query.util.Err.*;
import static org.basex.core.Text.*;

import java.io.*;

import org.basex.data.*;
import org.basex.io.out.*;
import org.basex.io.serial.*;
import org.basex.query.*;
import org.basex.query.value.node.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * Fn:put operation primitive.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Lukas Kircher
 */
public final class Put extends BasicOperation {
  /** Target paths. The same node can be stored in multiple locations. */
  private final StringList paths = new StringList(1);
  /** Node id of the target node. Target nodes are identified via their ID, as structural
   *  changes (delete/insert) during the snapshot lead to PRE value shifts on disk.
   *  In addition, deleted/replaced nodes will not be serialized by fn:put as the
   *  identity of these nodes is gone - which is easier to track operating on IDs. */
  public final int nodeid;

  /**
   * Constructor.
   * @param i input info
   * @param id target node id
   * @param d target data reference
   * @param u location path URI
   */
  public Put(final InputInfo i, final int id, final Data d, final String u) {
    super(BasicOperation.TYPE.FNPUT, d, i);
    nodeid = id;
    paths.add(u);
  }

  @Override
  public void apply() throws QueryException {
    for(final String u : paths) {
      final int pre = data.pre(nodeid);
      if(pre == -1) return;
      final DBNode node = new DBNode(data, pre);
      try {
        final PrintOutput po = new PrintOutput(u);
        try {
          final SerializerOptions pr = new SerializerOptions();
          // try to reproduce non-chopped documents correctly
          pr.set(SerializerOptions.S_INDENT, node.data.meta.chop ? YES : NO);
          final Serializer ser = Serializer.get(po, pr);
          ser.serialize(node);
          ser.close();
        } finally {
          po.close();
        }
      } catch(final IOException ex) {
        UPPUTERR.thrw(info, u);
      }
    }
  }

  @Override
  public void merge(final BasicOperation o) throws QueryException {
    for(final String u : ((Put) o).paths) paths.add(u);
  }

  @Override
  public int size() {
    return paths.size();
  }

  @Override
  public String toString() {
    return Util.className(this) + '[' + getTargetNode() + ", " + paths.get(0) + ']';
  }

  @Override
  public void prepare(final MemData tmp) throws QueryException { }

  @Override
  public DBNode getTargetNode() {
    return new DBNode(data, data.pre(nodeid));
  }
}
