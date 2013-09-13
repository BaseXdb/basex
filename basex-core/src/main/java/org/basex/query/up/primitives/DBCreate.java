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
import org.basex.util.hash.*;

/**
 * Update primitive for the {@link Function#_DB_CREATE} function.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Lukas Kircher
 */
public final class DBCreate extends DBNew {
  /** Name of new database. */
  public final String name;

  /**
   * Constructor.
   * @param ii input info
   * @param nm name for created database
   * @param in input (ANode and QueryInput references)
   * @param map index options
   * @param c query context
   * @throws QueryException query exception
   */
  public DBCreate(final InputInfo ii, final String nm, final List<NewInput> in,
      final TokenMap map, final QueryContext c) throws QueryException {

    super(TYPE.DBCREATE, null, c, ii);
    inputs = in;
    name = nm;
    options = map;
    check(true);
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
    if(inputs != null && !inputs.isEmpty()) addDocs(new MemData(qc.context.prop), name);
  }

  @Override
  public void apply() throws QueryException {
    // remove data instance from list of opened resources
    qc.resource.removeData(name);
    // check if addressed database is still pinned by any other process
    if(qc.context.pinned(name)) BXDB_OPENED.thrw(info, name);

    initOptions();
    assignOptions();
    try {
      data = CreateDB.create(name, Parser.emptyParser(qc.context.prop), qc.context);
    } catch(final IOException ex) {
      UPDBOPTERR.thrw(info, ex);
    } finally {
      resetOptions();
    }
    qc.resource.addData(data);

    // add initial documents
    if(md != null) {
      if(!data.startUpdate()) BXDB_OPENED.thrw(null, data.meta.name);
      data.insert(data.meta.size, -1, new DataClip(md));
      try {
        Optimize.optimize(data, false, null);
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
