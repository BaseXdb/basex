package org.basex.http;

import static jakarta.servlet.http.HttpServletResponse.*;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.*;

import org.basex.core.*;
import org.basex.io.*;
import org.basex.util.*;
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
    final Path source;
    final BasicFileAttributes attrs;
    try {
      source = file.file().toPath();
      attrs = Files.readAttributes(source, BasicFileAttributes.class);
    } catch(final InvalidPathException | NoSuchFileException ignore) {
      throw HTTPStatus.NOT_FOUND_X.get(path);
    }
    if(attrs.isDirectory() || !source.toRealPath().startsWith(base.file().toPath().toRealPath())) {
      throw HTTPStatus.NOT_FOUND_X.get(path);
    }

    // report unchanged resources; timestamps are sent with a granularity of seconds
    final long modified = attrs.lastModifiedTime().toMillis() / 1000 * 1000;
    long since;
    try {
      since = conn.request.getDateHeader(HTTPText.IF_MODIFIED_SINCE);
    } catch(final IllegalArgumentException ex) {
      // malformed header: ignored
      Util.debug(ex);
      since = -1;
    }
    if(since >= modified) {
      conn.response.setStatus(SC_NOT_MODIFIED);
      conn.log(SC_NOT_MODIFIED, "");
      return;
    }

    conn.contentType(MediaType.get(file.path()), Strings.UTF8);
    conn.response.setContentLengthLong(attrs.size());
    conn.response.setDateHeader(HTTPText.LAST_MODIFIED, modified);
    Files.copy(source, conn.response.getOutputStream());
    conn.log(SC_OK, "");
  }
}
