package org.basex.http.direct;

import static org.basex.core.Text.*;

import java.io.*;
import java.util.Map.Entry;

import org.basex.core.*;
import org.basex.core.cmd.*;
import org.basex.http.*;
import org.basex.io.*;
import org.basex.io.in.*;
import org.basex.io.serial.*;
import org.basex.server.*;
import org.basex.util.*;

/**
 * This servlet directly evaluates the specified files.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class DirectServlet extends BaseXServlet {
  @Override
  protected void run(final HTTPContext http) throws Exception {
    // get HTTP root directory
    final IOFile root = new IOFile(http.context().mprop.get(MainProp.WEBPATH));

    // check if file is not found, is a folder or points to parent folder
    final String input = http.req.getRequestURI();
    final IOFile io = new IOFile(root, input);
    if(!io.exists() || io.isDir() || !io.path().startsWith(root.path()))
      HTTPErr.NOT_FOUND_X.thrw(Util.info(RES_NOT_FOUND_X, input));

    final LocalSession session = http.session();
    final OutputStream os = http.res.getOutputStream();
    session.setOutputStream(os);

    if(io.hasSuffix(IO.BXSSUFFIX)) {
      // run script
      session.execute(new Run(io.path()));

      /* redirect parameter: redirect to another page
      final String redirect = http.req.getParameter("redirect");
      if(redirect != null) {
        http.res.setStatus(HttpServletResponse.SC_MOVED_TEMPORARILY);
        http.res.setHeader("location", redirect);
      }
      */
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
