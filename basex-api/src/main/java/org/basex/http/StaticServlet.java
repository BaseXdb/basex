package org.basex.http;

import static jakarta.servlet.http.HttpServletResponse.*;

import java.io.*;

import org.basex.core.*;
import org.basex.io.*;
import org.basex.util.http.*;

import jakarta.servlet.*;

/**
 * This servlet returns static resources of the web application.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class StaticServlet extends BaseXServlet {
  /** Default directory, relative to the web application. */
  private static final String DEFAULT_PATH = "static";

  /** Base directory. */
  private IOFile base;

  @Override
  public void init(final ServletConfig config) throws ServletException {
    super.init(config);
    final String path = config.getInitParameter("path");
    final String dir = path != null ? path : DEFAULT_PATH;
    base = new File(dir).isAbsolute() ? new IOFile(dir) :
      new IOFile(HTTPContext.get().context().soptions.get(StaticOptions.WEBPATH), dir);
  }

  @Override
  protected void run(final HTTPConnection conn) throws Exception {
    final String path = conn.path();
    final IOFile file = new IOFile(base, path);
    if(file.isDir() || !file.exists() || !inside(file)) throw HTTPStatus.NOT_FOUND_X.get(path);

    // report unchanged resources; timestamps are sent with a granularity of seconds
    final long modified = file.timeStamp() / 1000 * 1000;
    if(conn.request.getDateHeader(HTTPText.IF_MODIFIED_SINCE) >= modified) {
      conn.response.setStatus(SC_NOT_MODIFIED);
      conn.log(SC_NOT_MODIFIED, "");
      return;
    }

    conn.response.setContentType(MediaType.get(file.path()).toString());
    conn.response.setContentLengthLong(file.length());
    conn.response.setDateHeader(HTTPText.LAST_MODIFIED, modified);
    conn.response.getOutputStream().write(file.read());
    conn.log(SC_OK, "");
  }

  /**
   * Checks if a file is located inside the base directory.
   * @param file file to be checked
   * @return result of check
   * @throws IOException I/O exception
   */
  private boolean inside(final IOFile file) throws IOException {
    return file.file().getCanonicalPath().startsWith(
      base.file().getCanonicalPath() + File.separator);
  }
}
