package org.basex.query.func.db;

import static org.basex.query.QueryError.*;
import static org.basex.util.Token.*;

import org.basex.data.*;
import org.basex.io.*;
import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.util.*;
import org.basex.util.http.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class DbContentType extends DbAccess {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Data data = checkData(qc);
    final String path = path(1, qc);
    final int pre = data.resources.doc(path);
    MediaType type = null;
    if(pre != -1) {
      // check media type; return application/xml if returned string is not of type xml
      type = MediaType.get(string(data.text(pre, true)));
      if(!type.isXML()) type = MediaType.APPLICATION_XML;
    } else if(!data.inMemory()) {
      final IOFile io = data.meta.binary(path);
      if(io.exists() && !io.isDir()) type = MediaType.get(path);
    }
    if(type == null) throw WHICHRES_X.get(info, path);
    return Str.get(type.toString());
  }
}
