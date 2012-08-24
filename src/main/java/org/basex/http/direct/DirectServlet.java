package org.basex.http.direct;

import static org.basex.core.Text.*;

import java.io.*;
import java.util.Map.Entry;

import org.basex.core.*;
import org.basex.core.cmd.*;
import org.basex.core.cmd.Set;
import org.basex.http.*;
import org.basex.io.*;
import org.basex.io.in.*;
import org.basex.io.serial.*;
import org.basex.server.*;

/**
 * This servlet directly evaluates the specified files.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class DirectServlet extends BaseXServlet {
  @Override
  protected void run(final HTTPContext http) throws Exception {
    // get root directory for files
    final String path = http.context().mprop.get(MainProp.HTTPPATH);
    final String input = http.req.getRequestURI();

    // check if file is not found, is a folder or points to parent folder
    final IOFile root = new IOFile(path);
    final IOFile io = new IOFile(path, input);
    if(!io.exists() || io.isDir() || !io.path().startsWith(root.path()))
      HTTPErr.NOT_FOUND_X.thrw(RES_NOT_FOUND_X, input);

    final LocalSession session = http.session();
    final OutputStream os = http.res.getOutputStream();
    session.setOutputStream(os);

    if(io.hasSuffix(IO.BXSSUFFIX)) {
      // run script
      session.execute(new Run(io.path()));
    } else if(io.hasSuffix(IO.XQSUFFIXES)) {
      // evaluate query: set local query path
      session.execute(new Set(Prop.QUERYPATH, io.path()));
      // create query instance
      final Query qu = session.query(io.string());
      // bind variables
      for(final Entry<String, String[]> param : http.params().entrySet()) {
        final String[] val = param.getValue();
        qu.bind(param.getKey(), val[0], val.length == 1 ? "" : val[1]);
      }
      // initializes the response with query serialization options
      http.initResponse(new SerializerProp(qu.options()));
      // run query
      qu.execute();
    } else {
      // for all other file types: set content type, return raw bytes
      http.res.setContentType(MimeTypes.get(io.path()));
      final BufferInput bi = new BufferInput(io);
      try {
        for(int b; (b = bi.read()) != -1;) os.write(b);
      } finally {
        bi.close();
      }
    }
  }
}
