package org.basex.http.web;

import static org.junit.jupiter.api.Assertions.*;

import java.io.*;
import java.net.*;
import java.net.http.*;
import java.util.Base64;
import java.util.Map;
import java.util.zip.*;

import org.basex.core.*;
import org.basex.http.*;
import org.basex.io.*;
import org.basex.util.*;
import org.basex.util.http.*;
import org.junit.jupiter.api.*;

/**
 * This class tests the shipped applications, deployed as web archives.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class WebappArchiveTest extends HTTPTest {
  /** Source directory of the shipped applications. */
  private static final IOFile SOURCE = new IOFile("src/main/webapp");
  /** Shipped applications. */
  private static final String[] APPS = { "chat", "webdav", "dba" };

  /**
   * Starts the server and deploys all applications as archives.
   * @throws Exception exception
   */
  @BeforeAll public static void start() throws Exception {
    init(HTTP_ROOT, true);
    for(final String app : APPS) archive(app).write(zip(new IOFile(SOURCE, app)));
    WebModules.get(HTTPContext.get().context()).init(false);
  }

  /**
   * Removes the archives.
   */
  @AfterAll public static void undeploy() {
    for(final String app : APPS) archive(app).delete();
    archive("probe").delete();
  }

  /**
   * Resolves a module import inside a job of an archived application.
   * @throws Exception exception
   */
  @Test public void jobModuleImport() throws Exception {
    archive("probe").write(zip(Map.of(
      "basex-web.xml", "<webapp name='probe' version='13.0'/>",
      "lib.xqm", "module namespace lib = 'probe/lib';" +
        "declare function lib:hello() { 'hello' };",
      "job.xq", "import module namespace lib = 'probe/lib' at 'lib.xqm'; lib:hello()",
      "probe.xqm", "module namespace probe = 'probe';" +
        "declare %rest:path('/probe') %rest:GET %output:method('text')" +
        "function probe:go() { job:execute(xs:anyURI('job.xq')) };")));
    WebModules.get(HTTPContext.get().context()).init(false);

    assertEquals("hello", get(200, "probe"));
  }

  /**
   * Returns a ZIP archive with the specified entries.
   * @param entries entry names and contents
   * @return archive
   * @throws IOException I/O exception
   */
  private static byte[] zip(final Map<String, String> entries) throws IOException {
    final ByteArrayOutputStream bos = new ByteArrayOutputStream();
    try(ZipOutputStream zos = new ZipOutputStream(bos)) {
      for(final Map.Entry<String, String> entry : entries.entrySet()) {
        zos.putNextEntry(new ZipEntry(entry.getKey()));
        zos.write(Token.token(entry.getValue()));
        zos.closeEntry();
      }
    }
    return bos.toByteArray();
  }

  /**
   * Serves the login page of the chat application.
   * @throws IOException I/O exception
   */
  @Test public void chatLogin() throws IOException {
    final String page = get(200, "chat");
    assertTrue(page.contains("/chat/login-check"), page);
  }

  /**
   * Resolves the module imports of the chat application (redirects to the login page).
   * @throws IOException I/O exception
   */
  @Test public void chatLogout() throws IOException {
    final String page = get(200, "chat/logout");
    assertTrue(page.contains("/chat/login-check"), page);
  }

  /**
   * Reads a static chat resource from within the archive.
   * @throws IOException I/O exception
   */
  @Test public void chatResource() throws IOException {
    assertTrue(get(200, "chat/.static/style.css").contains("{"));
  }

  /**
   * Lists the databases via the WebDAV application (which enforces authentication).
   * @throws Exception exception
   */
  @Test public void webdavRoot() throws Exception {
    assertFalse(authorized("webdav").body().isEmpty());
  }

  /**
   * Executes an authenticated GET request (the WebDAV service enforces authentication).
   * @param path path of request
   * @return response
   * @throws Exception exception
   */
  private static HttpResponse<String> authorized(final String path) throws Exception {
    final String credentials = Base64.getEncoder().encodeToString(Token.token("admin:" + NAME));
    final HttpRequest request = HttpRequest.newBuilder(URI.create(HTTP_ROOT + path)).
        header(HTTPText.AUTHORIZATION, "Basic " + credentials).build();
    final HttpResponse<String> response = IOUrl.client(true).send(request,
        HttpResponse.BodyHandlers.ofString());
    assertEquals(200, response.statusCode(), response.body());
    return response;
  }

  /**
   * Serves the start page of the DBA.
   * @throws IOException I/O exception
   */
  @Test public void dbaStart() throws IOException {
    assertFalse(get(200, "dba").isEmpty());
  }

  /**
   * Reads a static DBA resource from within the archive.
   * @throws IOException I/O exception
   */
  @Test public void dbaResource() throws IOException {
    assertTrue(get(200, "dba/.static/style.css").contains("{"));
  }

  /**
   * Reads a static WebDAV resource (which is not shadowed by the resource paths).
   * @throws Exception exception
   */
  @Test public void webdavResource() throws Exception {
    assertTrue(authorized("webdav/.static/style.css").body().contains("{"));
  }

  /**
   * Rejects unknown paths.
   * @throws IOException I/O exception
   */
  @Test public void unknown() throws IOException {
    get(404, "chat/missing");
  }

  /**
   * Returns the deployed archive file in the RESTXQ directory.
   * @param app application name
   * @return file
   */
  private static IOFile archive(final String app) {
    final StaticOptions sopts = HTTPContext.get().context().soptions;
    return new IOFile(sopts.get(StaticOptions.WEBPATH), app + IO.ZIPSUFFIX);
  }

  /**
   * Returns a ZIP archive with the contents of the specified directory.
   * @param dir source directory
   * @return archive
   * @throws IOException I/O exception
   */
  private static byte[] zip(final IOFile dir) throws IOException {
    final ByteArrayOutputStream bos = new ByteArrayOutputStream();
    try(ZipOutputStream zos = new ZipOutputStream(bos)) {
      for(final String path : dir.descendants()) {
        zos.putNextEntry(new ZipEntry(path));
        zos.write(new IOFile(dir, path).read());
        zos.closeEntry();
      }
    }
    return bos.toByteArray();
  }
}
