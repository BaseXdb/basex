package org.basex.http.web;

import static org.junit.jupiter.api.Assertions.*;

import java.io.*;
import java.util.zip.*;

import org.basex.core.*;
import org.basex.http.*;
import org.basex.io.*;
import org.basex.query.util.pkg.*;
import org.basex.util.*;
import org.junit.jupiter.api.*;

/**
 * This class tests web applications that are deployed as archives.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class WebArchiveTest extends HTTPTest {
  /** Contents of the test archive: entry names and contents. */
  private static final String[] ENTRIES = {
    "basex-web.xml", "<webapp name='demo' version='1.2'/>",
    "modules/main.xqm",
      "module namespace m = 'http://basex.org/demo';" +
      "import module namespace u = 'http://basex.org/demo/util' at '../lib/util.xqm';" +
      "declare %rest:path('/hello') %rest:GET function m:hello() {" +
      "  u:name() || ' ' || unparsed-text('../data/text.txt') };",
    "lib/util.xqm",
      "module namespace u = 'http://basex.org/demo/util';" +
      "declare function u:name() { 'hello' };",
    "data/text.txt", "from-the-archive"
  };

  /**
   * Starts the server and deploys the test application.
   * @throws Exception exception
   */
  @BeforeAll public static void start() throws Exception {
    init(HTTP_ROOT, true);
    archive().write(archive(ENTRIES));
    WebModules.get(HTTPContext.get().context()).init(false);
  }

  /**
   * Removes the test application.
   */
  @AfterAll public static void undeploy() {
    archive().delete();
  }

  /**
   * Resolves relative module imports and URIs inside the archive.
   * @throws IOException I/O exception
   */
  @Test public void resolve() throws IOException {
    get("hello from-the-archive", "hello");
  }

  /**
   * Never exposes archive contents over HTTP.
   * @throws IOException I/O exception
   */
  @Test public void hidden() throws IOException {
    get(404, "data/text.txt");
    get(404, "modules/main.xqm");
  }

  /**
   * Lists and deletes the application via the repository.
   * @throws Exception exception
   */
  @Test public void repository() throws Exception {
    // identity is adopted from the descriptor, not from the versioned file name
    final RepoManager repo = new RepoManager(HTTPContext.get().context());
    final Pkg pkg = repo.packages().stream().filter(p -> p.type() == PkgType.WEB).
        findFirst().orElseThrow();
    assertEquals("demo", pkg.name());
    assertEquals("1.2", pkg.version());

    // the absolute path reported by repo:list() can be fed back to repo:delete()
    repo.delete(repo.path(pkg).path());
    assertFalse(repo.ids().contains("demo"));

    // reinstall under a different file name; the old archive must not survive
    final IOFile source = new IOFile(Prop.TEMPDIR, "demo-1.3" + IO.ZIPSUFFIX);
    source.write(archive(ENTRIES));
    try {
      repo.install(source.path());
      repo.install(source.path());
    } finally {
      source.delete();
    }
    assertEquals(1, repo.packages().stream().filter(p -> p.type() == PkgType.WEB).count());

    // restore the original file name for the remaining tests
    repo.delete("demo");
    archive().write(archive(ENTRIES));
  }

  /**
   * Returns the archive file in the RESTXQ directory.
   * @return file
   */
  private static IOFile archive() {
    final StaticOptions sopts = HTTPContext.get().context().soptions;
    return new IOFile(sopts.get(StaticOptions.WEBPATH), "demo-1.2" + IO.ZIPSUFFIX);
  }

  /**
   * Returns a ZIP archive with the specified entry names and contents.
   * @param entries entry names and contents
   * @return archive
   * @throws IOException I/O exception
   */
  static byte[] archive(final String... entries) throws IOException {
    final ByteArrayOutputStream bos = new ByteArrayOutputStream();
    try(ZipOutputStream zos = new ZipOutputStream(bos)) {
      final int el = entries.length;
      for(int e = 0; e < el; e += 2) {
        zos.putNextEntry(new ZipEntry(entries[e]));
        zos.write(Token.token(entries[e + 1]));
        zos.closeEntry();
      }
    }
    return bos.toByteArray();
  }
}
