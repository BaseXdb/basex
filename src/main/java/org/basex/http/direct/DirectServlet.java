package org.basex.http.direct;

import static org.basex.core.Text.*;

import java.io.*;
import java.util.*;
import java.util.Map.Entry;

import javax.servlet.http.*;

import org.basex.core.*;
import org.basex.core.cmd.*;
import org.basex.core.cmd.Set;
import org.basex.core.parse.*;
import org.basex.http.*;
import org.basex.io.*;
import org.basex.io.in.*;
import org.basex.io.out.*;
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
    final Context ctx = http.context();
    final IOFile root = new IOFile(ctx.mprop.get(MainProp.WEBPATH));

    // check if file is not found, is a folder or points to parent folder
    final String input = http.req.getRequestURI();
    final IOFile io = new IOFile(root, input);
    if(!io.exists() || io.isDir() || !io.path().startsWith(root.path()))
      HTTPErr.NOT_FOUND_X.thrw(Util.info(RES_NOT_FOUND_X, input));

    // check if request contains redirect parameter
    final boolean redirect = http.req.getParameter("@redirect") != null;

    // set output stream and local query path
    final LocalSession session = http.session();
    final OutputStream os = redirect ? new ArrayOutput() : http.res.getOutputStream();
    session.setOutputStream(os);
    session.execute(new Set(Prop.QUERYPATH, io.path()));

    // bind variables
    final TokenBuilder vars = new TokenBuilder();
    for(final Entry<String, String[]> param : http.params().entrySet()) {
      final String key = param.getKey().trim();
      final String[] val = param.getValue();
      if(!key.startsWith("@")) {
        vars.add(key).add('=').add(val[0].replace(",", ",,")).add(',');
      }
    }
    session.execute(new Set(Prop.BINDINGS, vars.toString()));
    // required for local command parsing
    ctx.prop.set(Prop.BINDINGS, vars.toString());

    // create commands to be executed
    final ArrayList<Command> list = new ArrayList<Command>();
    if(io.hasSuffix(IO.BXSSUFFIX)) {
      // interpret at commands if input starts with < or ends with command script suffix
      for(final Command c : new CommandParser(io.string(), ctx).parse()) list.add(c);
    } else if(io.hasSuffix(IO.XQSUFFIXES)) {
      list.add(new XQuery(io.string()));
    } else {
      // for all other file types: set content type, write raw stream
      http.res.setContentType(MimeTypes.get(io.path()));
      final BufferInput bi = new BufferInput(io);
      try {
        for(int b; (b = bi.read()) != -1;) os.write(b);
      } finally {
        bi.close();
      }
    }

    // process list of commands
    final TokenBuilder info = new TokenBuilder();
    for(final Command c : list) {
      String inf;
      if(c instanceof XQuery) {
        // create query instance
        final Query qu = session.query(c.args[0]);
        // initializes the response with query serialization options
        http.initResponse(new SerializerProp(qu.options()));
        // run query
        qu.execute();
        inf = qu.info();
      } else {
        session.execute(c);
        inf = session.info();
      }
      info.add(inf.trim().replaceAll("\r\n?", "\n")).add('\n');
    }

    if(redirect) {
      final String ovar = http.req.getParameter("@output");
      final String ivar = http.req.getParameter("@info");
      String uri = http.req.getParameter("@redirect");

      final TokenBuilder tb = new TokenBuilder();
      if(http.method == HTTPMethod.POST) {
        // post request: return html form with javascript to trigger client-side reload
        tb.add("<html><body onload='document.forms[\"form\"].submit()'>");
        tb.add("<form name='form' action='" + uri + "' method='post'>");
        if(ovar != null) {
          tb.add("<input type='hidden' name='" + ovar + "' value='");
          tb.add(os.toString().replace("'", "&apos;")).add("'/>");
        }
        if(ivar != null) {
          tb.add("<input type='hidden' name='" + ivar + "' value='");
          tb.add(info.toString().replace("'", "&apos;")).add("'/>");
        }
        tb.add("</form></body></html>");
        http.res.getOutputStream().write(tb.finish());
      } else {
        // bind query output to specified variable
        if(ovar != null) uri += (uri.indexOf('?') != -1 ? '&' : '?') + ovar + '=' +
            Token.string(Token.uri(Token.token(os.toString()), false));
        // bind query info to specified variable
        if(ivar != null) uri += (uri.indexOf('?') != -1 ? '&' : '?') + ivar + '=' +
            Token.string(Token.uri(info.finish(), false));

        // set status and location
        http.status(HttpServletResponse.SC_MOVED_TEMPORARILY, null);
        http.res.setHeader("location", uri);
      }
    }
  }
}
