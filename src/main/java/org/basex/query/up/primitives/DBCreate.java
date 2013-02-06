package org.basex.query.up.primitives;

import static org.basex.query.util.Err.*;

import java.io.*;
import java.util.List;

import org.basex.build.*;
import org.basex.core.cmd.*;
import org.basex.data.*;
import org.basex.data.atomic.*;
import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.value.node.*;
import org.basex.util.*;

/**
 * Update primitive for the {@link Function#_DB_CREATE} function.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Lukas Kircher
 */
public final class DBCreate extends DBNew {
  /** Name for created database. */
  public final String name;

  /**
   * Constructor.
   * @param ii input info
   * @param nm name for created database
   * @param in input (ANode and QueryInput references)
   * @param c query context
   */
  public DBCreate(final InputInfo ii, final String nm, final List<NewInput> in,
      final QueryContext c) {

    super(TYPE.DBCREATE, null, in, c, ii);
    name = nm;
  }

  @Override
  public DBNode getTargetNode() {
    return null;
  }

  @Override
  public void merge(final BasicOperation o) throws QueryException {
    BXDB_CREATE.thrw(info, ((DBCreate) o).name);
  }

  @Override
  public void prepare(final MemData tmp) throws QueryException {
    if(inputs != null) addDocs(new MemData(qc.context.prop), name);
  }

  @Override
  public void apply() throws QueryException {
    // close data instance in query processor
    qc.resource.removeData(name);
    // check if addressed databases are still pinned
    if(qc.context.pinned(name)) BXDB_OPENED.thrw(info, name);

    try {
      data = CreateDB.create(name, Parser.emptyParser(qc.context.prop), qc.context);
    } catch(final IOException ex) {
      UPDBOPTERR.thrw(info, ex);
    }
    qc.resource.addData(data);

    // add initial documents
    if(md != null) {
      if(!data.startUpdate()) BXDB_OPENED.thrw(null, data.meta.name);
      data.insert(data.meta.size, -1, new DataClip(md));
      try {
        Optimize.optimize(data, null);
      } catch(final IOException ex) {
        data.finishUpdate();
        UPDBOPTERR.thrw(info, ex);
      }
    }
  }

  @Override
  public int size() {
    return 1;
  }

  @Override
  public String toString() {
    return Util.name(this) + '[' + inputs + ']';
  }
}
