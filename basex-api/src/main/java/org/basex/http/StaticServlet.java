package org.basex.http;

import static jakarta.servlet.http.HttpServletResponse.*;
import static org.basex.util.http.HTTPText.*;

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
  /** Precompressed variants in the order of preference (content coding, file suffix). */
  private static final String[][] VARIANTS = { { BR, IO.BRSUFFIX }, { GZIP, IO.GZSUFFIX } };

  /** Base directory. */
  private IOFile base;

  @Override
  public void init(final ServletConfig config) throws ServletException {
    super.init(config);
    final String path = config.getInitParameter("path");
    final String dir = path != null ? path : STATIC_PATH;
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
    final Path root = base.file().toPath().toRealPath();
    if(attrs.isDirectory() || !source.toRealPath().startsWith(root)) {
      throw HTTPStatus.NOT_FOUND_X.get(path);
    }

    // choose the first precompressed variant accepted by the client
    final String accept = conn.request.getHeader(ACCEPT_ENCODING);
    Path send = source;
    long size = attrs.size();
    String encoding = null;
    for(final String[] variant : VARIANTS) {
      final Path sibling = source.resolveSibling(source.getFileName() + variant[1]);
      final BasicFileAttributes sa;
      try {
        sa = Files.readAttributes(sibling, BasicFileAttributes.class);
      } catch(final NoSuchFileException ignore) {
        continue;
      }
      if(!sa.isRegularFile() || !sibling.toRealPath().startsWith(root)) continue;
      conn.response.setHeader(VARY, ACCEPT_ENCODING);
      if(QualityFactor.of(accept, variant[0]) > 0) {
        send = sibling;
        size = sa.size();
        encoding = variant[0];
        break;
      }
    }

    // report unchanged resources; timestamps are sent with a granularity of seconds
    final long modified = attrs.lastModifiedTime().toMillis() / 1000 * 1000;
    long since;
    try {
      since = conn.request.getDateHeader(IF_MODIFIED_SINCE);
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
    if(encoding != null) conn.response.setHeader(CONTENT_ENCODING, encoding);
    conn.response.setContentLengthLong(size);
    conn.response.setDateHeader(LAST_MODIFIED, modified);
    Files.copy(send, conn.response.getOutputStream());
    conn.log(SC_OK, "");
  }
}
