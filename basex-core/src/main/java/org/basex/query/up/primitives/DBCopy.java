package org.basex.query.up.primitives;

import static org.basex.query.util.Err.*;

import java.io.*;

import org.basex.core.cmd.*;
import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.util.*;

/**
 * Update primitive for the {@link Function#_DB_COPY} function.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public class DBCopy extends NameUpdate {
  /** Name of the new database. */
  protected final String newName;

  /**
   * Constructor.
   * @param name database to be copied
   * @param newName name of new database
   * @param info input info
   * @param qc query context
   */
  public DBCopy(final String name, final String newName, final InputInfo info,
      final QueryContext qc) {

    super(UpdateType.DBCOPY, name, info, qc);
    this.newName = newName;
  }

  @Override
  public void apply() throws QueryException {
    close();
    close(newName, qc, info);
    try {
      Copy.copy(name, newName, qc.context, null);
    } catch(final IOException ex) {
      throw UPDBOPTERR.get(info, ex);
    }
  }

  @Override
  public void prepare() throws QueryException { }

  @Override
  public String operation() { return "copied"; }
}
