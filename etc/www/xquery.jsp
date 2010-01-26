<%@ page import="java.io.*" %>
<%@ page import="org.basex.core.*" %>
<%@ page import="org.basex.data.*" %>
<%@ page import="org.basex.util.*" %>
<%@ page import="org.basex.io.*" %>
<%@ page import="org.basex.query.*" %>
<%@ page import="org.basex.query.xquery.*" %>
<%

/** Root directory (dynamic?...). */
String ROOT = "webapps/ROOT/";

String file = request.getParameter("page");
if(file == null || file.length() == 0) file = "index";
final String fn = ROOT + file + ".xq";
final String query = Token.string(IOConstants.read(fn));

try {
  // create query instance
  final XQueryProcessor xq = new XQueryProcessor(query, new File(fn));
  final Result res = xq.query(null);
  final CachedOutput cache = new CachedOutput();
  res.serialize(new PrintSerializer(cache));
  out.print(cache.toString());
} catch(final Exception e) {
  out.print(e.toString());
}

%>
