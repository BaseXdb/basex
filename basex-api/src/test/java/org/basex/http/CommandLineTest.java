package org.basex.http;

import static org.junit.jupiter.api.Assertions.*;

import java.net.*;

import org.basex.*;
import org.basex.core.*;
import org.basex.io.*;
import org.basex.util.*;
import org.junit.jupiter.api.*;

/**
 * Precedence of command-line options over configuration files of the web application.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class CommandLineTest extends SandboxTest {
  /** Web application configuration. */
  private static final IOFile WEBXML = new IOFile(sandbox(), "webapp/WEB-INF/web.xml");
  /** Jetty configuration. */
  private static final IOFile JETTYXML = new IOFile(sandbox(), "webapp/WEB-INF/jetty.xml");

  /** Removes the configuration files. */
  @AfterEach public void clean() {
    WEBXML.delete();
    JETTYXML.delete();
  }

  /**
   * Command-line options override the context parameters of web.xml and the port of jetty.xml.
   * @throws Exception exception
   */
  @Test public void precedence() throws Exception {
    WEBXML.parent().md();
    final String web = Token.string(new IOFile("src/main/webapp/WEB-INF/web.xml").read());
    WEBXML.write(web.replace("<description>",
        "<context-param><param-name>org.basex.serverport</param-name>"
      + "<param-value>1</param-value></context-param><description>"));
    JETTYXML.write(
        "<!DOCTYPE Configure PUBLIC '-//Jetty//Configure//EN' "
      + "'http://www.eclipse.org/jetty/configure.dtd'>"
      + "<Configure id='Server' class='org.eclipse.jetty.server.Server'>"
      + "<Call name='addConnector'><Arg>"
      + "<New class='org.eclipse.jetty.server.ServerConnector'>"
      + "<Arg name='server'><Ref refid='Server'/></Arg>"
      + "<Set name='port'>" + (HTTP_PORT + 1) + "</Set>"
      + "</New></Arg></Call></Configure>");

    final BaseXHTTP http = new BaseXHTTP("-p" + DB_PORT, "-h" + HTTP_PORT, "-s" + STOP_PORT,
        "-z", "-q");
    try {
      final StaticOptions sopts = HTTPContext.get().context().soptions;
      assertEquals(DB_PORT, sopts.get(StaticOptions.SERVERPORT));
      // fails if the port of jetty.xml is used
      new Socket("localhost", HTTP_PORT).close();
    } finally {
      http.stop();
    }
  }
}
