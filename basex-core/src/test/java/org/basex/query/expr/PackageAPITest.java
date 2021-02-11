package org.basex.query.expr;

import static org.basex.query.QueryError.*;
import static org.basex.util.Token.*;
import static org.junit.jupiter.api.Assertions.*;

import java.io.*;
import java.nio.file.*;
import java.util.*;

import org.basex.*;
import org.basex.core.*;
import org.basex.core.cmd.*;
import org.basex.io.*;
import org.basex.query.*;
import org.basex.query.util.pkg.*;
import org.basex.util.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.Test;

/**
 * This class tests the EXPath package API.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Rositsa Shadura
 */
public final class PackageAPITest extends SandboxTest {
  /** Test repository. */
  private static final String SOURCE = "src/test/resources/repo/";
  /** Test repository. */
  private static final String REPO = sandbox() + "repo/";

  /** Pkg1 URI. */
  private static final String PKG1 = "http://www.pkg1.com";
  /** Pkg1 URI. */
  private static final String PKG2 = "http://www.pkg2.com";
  /** Pkg1 URI. */
  private static final String PKG3 = "http://www.pkg3.com";
  /** Pkg4 URI. */
  private static final String PKG4 = "http://www.pkg4.com";
  /** Pkg5 URI. */
  private static final String PKG5 = "http://www.pkg5.com";
  /** Pkg1 identifier. */
  private static final String PKG1ID = PKG1 + "-12.0";
  /** Pkg2 identifier. */
  private static final String PKG2ID = PKG2 + "-10.0";
  /** Pkg3 identifier. */
  private static final String PKG3ID = PKG3 + "-10.0";
  /** Pkg4 identifier. */
  private static final String PKG4ID = PKG4 + "-2.0";

  /**
   * Prepares a test.
   * @throws IOException I/O exception
   */
  @BeforeEach public void init() throws IOException {
    new IOFile(REPO).delete();
    copy(Paths.get(SOURCE), Paths.get(REPO));
  }

  /**
   * Finalizes a test.
   */
  @AfterEach public void finish() {
    new IOFile(REPO).delete();
  }

  /**
   * Recursively copies files.
   * @param source source
   * @param target target
   * @throws IOException I/O exception
   */
  private static void copy(final Path source, final Path target) throws IOException {
    if(Files.isDirectory(source)) {
      Files.createDirectory(target);
      try(DirectoryStream<Path> paths = Files.newDirectoryStream(source)) {
        for(final Path path : paths) copy(path, target.resolve(path.getFileName()));
      }
    } else {
      Files.copy(source, target);
    }
  }

  /** Tests repository initialization. */
  @Test public void repoInit() {
    // check namespace dictionary
    final HashMap<String, HashSet<String>> nsDict = context.repo.nsDict();
    final HashMap<String, Pkg> pkgDict = context.repo.pkgDict();

    assertEquals(3, nsDict.size());
    assertTrue(nsDict.containsKey("ns1"));
    assertTrue(nsDict.containsKey("ns2"));
    assertTrue(nsDict.containsKey("ns3"));
    HashSet<String> ts = nsDict.get("ns1");
    assertEquals(ts.size(), 2);
    assertTrue(ts.contains(PKG1ID));
    assertTrue(ts.contains(PKG2ID));
    ts = nsDict.get("ns2");
    assertEquals(ts.size(), 1);
    assertTrue(ts.contains(PKG1ID));
    ts = nsDict.get("ns3");
    assertEquals(ts.size(), 1);
    assertTrue(ts.contains(PKG2ID));
    // check package dictionary
    assertEquals(pkgDict.size(), 2);
    assertTrue(pkgDict.containsKey(PKG1ID));
    assertTrue(pkgDict.containsKey(PKG2ID));
    assertEquals("pkg1", pkgDict.get(PKG1ID).path());
    assertEquals("pkg2", pkgDict.get(PKG2ID).path());
  }

  /** Test for missing mandatory attributes. */
  @Test public void mandatoryAttr() {
    error(new IOContent("<package xmlns:http='http://expath.org/ns/pkg' spec='1.0'/>"),
        REPO_DESCRIPTOR_X, "Missing mandatory attribute not detected.");
  }

  /**
   * Tests package with not installed dependencies - dependency is defined with
   * no specific versions.
   */
  @Test public void notInstalledDeps() {
    error(
        desc(PKG5, "pkg5", "12.0",
            "<dependency package='" + PKG4 + "'/>"),
        REPO_NOTFOUND_X, "Missing dependency not detected.");
  }

  /**
   * Tests package with not installed dependencies - dependency is defined with
   * version set.
   */
  @Test public void notInstalledDepVersion() {
    error(
        desc(PKG5, "pkg5", "12.0",
            "<dependency package='" + PKG1 + "' versions='1.0 7.0'/>"),
        REPO_NOTFOUND_X, "Missing dependency not detected.");
  }

  /**
   * Tests package with not installed dependencies - dependency is defined with
   * version template.
   */
  @Test public void notInstalledDepTemp() {
    error(
        desc(PKG5, "pkg5", "12.0",
            "<dependency package='" + PKG1 + "' versions='12.7'/>"),
        REPO_NOTFOUND_X, "Missing dependency not detected.");
  }

  /**
   * Tests package with not installed dependencies - dependency is defined with
   * version template for minimal acceptable version.
   */
  @Test public void notInstalledMin() {
    error(
        desc(PKG5, "pkg5", "12.0",
            "<dependency package='" + PKG1 + "' versions='12.7'/>"),
        REPO_NOTFOUND_X, "Missing dependency not detected.");
  }

  /**
   * Tests package with not installed dependencies - dependency is defined with
   * version template for maximal acceptable version.
   */
  @Test public void notInstalledMax() {
    error(
        desc(PKG5, "pkg5", "12.0",
            "<dependency package='" + PKG1 + "' semver-max='11'/>"),
        REPO_NOTFOUND_X, "Missing dependency not detected.");
  }

  /**
   * Tests package with not installed dependencies - dependency is defined with
   * version templates for minimal and maximal acceptable version.
   */
  @Test public void notInstalledMinMax() {
    error(desc(PKG5, "pkg5", "12.0",
        "<dependency package='" + PKG1 + "' semver-min='5.7' "
            + "semver-max='11'/>"), REPO_NOTFOUND_X,
        "Missing dependency not detected.");
  }

  /**
   * Tests package with component which is already installed as part of another
   * package.
   */
  @Test public void alreadyAnotherInstalled() {
    error(desc(PKG5, "pkg5", "12.0",
        "<xquery><namespace>ns1</namespace><file>pkg1mod1.xql</file></xquery>"),
        REPO_INSTALLED_X, "Already installed component not detected.");
  }

  /**
   * Tests package with dependency on an older version of BaseX.
   */
  @Test public void notSupported() {
    error(desc(PKG5, "pkg5", "12.0",
        "<dependency processor='basex' semver='5.0'/>"), REPO_VERSION,
        "Unsupported package not detected.");
  }

  /**
   * Tests package with component which is already installed as part of another
   * version of the same package.
   */
  @Test public void alreadyAnotherSame() {
    ok(desc(PKG1, "pkg1", "10.0",
        "<xquery><namespace>ns1</namespace><file>pkg1mod1.xql</file></xquery>"));
  }

  /**
   * Tests valid package.
   */
  @Test public void valid() {
    ok(desc(PKG1, "pkg1", "10.0", "<dependency package='" + PKG1 + "' semver-min='11'/>"
        + "<xquery><namespace>ns3</namespace>"
        + "<file>pkg5mod1.xql</file></xquery>"));
  }

  /**
   * Tests ability to import two modules from the same package.
   * @throws Exception exception
   */
  @Test public void importTwoModulesFromPkg() throws Exception {
    try(QueryProcessor qp = new QueryProcessor(
      "import module namespace ns1='ns1';" +
      "import module namespace ns3='ns3';" +
      "(ns1:test2() eq 'pkg2mod1') and (ns3:test() eq 'pkg2mod2')",
      context)) {
      assertEquals(qp.value().serialize().toString(), "true");
    }
  }

  /**
   * Tests package installation.
   */
  @Test public void repoInstall() {
    // try to install non-existing package
    try {
      new RepoManager(context).install("src/test/resources/pkg");
      fail("Not existing package not detected.");
    } catch(final QueryException ex) {
      error(null, ex, REPO_NOTFOUND_X);
    }

    // try to install a XAR package
    execute(new RepoInstall(REPO + "pkg3.xar", null));
    final String dir = normalize(PKG3ID);
    assertTrue(isDir(dir));
    assertTrue(isFile(dir + "/expath-pkg.xml"));
    assertTrue(isDir(dir + "/pkg3"));
    assertTrue(isDir(dir + "/pkg3/mod"));
    assertTrue(isFile(dir + "/pkg3/mod/pkg3mod1.xql"));
    assertTrue(new IOFile(REPO, dir).delete());

    // try to install a URN package
    execute(new RepoInstall(REPO + "12345.xqm", null));
    assertTrue(isFile("urn/isbn/12345.xqm"));
  }

  /**
   * Tests installation of a XAR file.
   * @throws Exception exception
   */
  @Test public void installXar() throws Exception {
    // install package
    execute(new RepoInstall(REPO + "testJar.xar", null));

    // ensure package was properly installed
    final String dir = normalize("jarPkg-1.0.0");
    assertTrue(isDir(dir));
    assertTrue(isFile(dir + "/expath-pkg.xml"));
    assertTrue(isFile(dir + "/basex.xml"));
    assertTrue(isDir(dir + "/jar"));
    assertTrue(isFile(dir + "/jar/test.jar"));
    assertTrue(isFile(dir + "/jar/wrapper.xq"));

    // use package
    try(QueryProcessor qp = new QueryProcessor(
        "import module namespace j='jar'; j:print('test')", context)) {
      assertEquals(qp.value().serialize().toString(), "test");
    }

    // delete package
    assertTrue(new IOFile(REPO, dir).delete(), "Repo directory could not be deleted.");
    assertFalse(new IOFile(REPO, dir).exists());
  }

  /**
   * Tests installation of a JAR file.
   */
  @Test public void installJar() {
    // ensure that all files are installed
    execute(new RepoInstall(REPO + "Hello.jar", null));

    final IOFile jar = new IOFile(REPO, "org/basex/modules/Hello.jar");
    final IOFile xqm = new IOFile(REPO, "org/basex/modules/Hello.xqm");
    assertTrue(jar.exists(), "File not found: " + jar);
    assertTrue(xqm.exists(), "File not found: " + xqm);

    // run query
    String query = "import module namespace h='http://basex.org/modules/Hello';h:hello('Universe')";
    assertEquals("Hello Universe", execute(new XQuery(query)));

    // run query, ensure that wrong types will be rejected
    query = "import module namespace h='http://basex.org/modules/Hello';h:hello(123)";
    try(QueryProcessor qp = new QueryProcessor(query, context)) {
      qp.value();
    } catch(final QueryException ex) {
      assertEquals(INVPROMOTE_X_X_X, ex.error());
    }

    // ensure that all files were deleted
    execute(new RepoDelete("org.basex.modules.Hello", null));
    assertFalse(jar.exists(), "File was not deleted:" + jar);
    assertFalse(xqm.exists(), "File was not deleted:" + xqm);

    // ensure that package can only be deleted once
    try {
      new RepoDelete("org.basex.modules.Hello", null).execute(context);
    } catch(final BaseXException ex) {
      assertTrue(ex.toString().contains(REPO_NOTFOUND_X.toString()));
    }
  }

  /**
   * Tests installation of a package containing a jar file with packaged dependencies.
   * @throws Exception exception
   */
  @Test public void installJarWithDependencies() throws Exception {
    // install package
    execute(new RepoInstall(REPO + "testJarInJar.jar", null));

    // ensure package was properly installed
    final String dir = normalize("test");
    final String jar = "GreeterCaller";
    assertTrue(isDir(dir));
    assertTrue(isFile(dir + '/' + jar + IO.JARSUFFIX));

    // use package
    try(QueryProcessor qp = new QueryProcessor(
            "import module namespace j='test." + jar + "'; j:call('Unit Test')", context)) {
      assertEquals(qp.value().serialize().toString(), "Hello Unit Test");
    }
    // dependency installed
    assertTrue(isFile(dir + "/." + jar + "/Greeter.jar"));

    new RepoManager(context).delete("test." + jar);
    final boolean existAfterDelete = isFile(dir + "/." +
        jar + "/Greeter.jar") || isFile(dir + '/' + jar + IO.JARSUFFIX);
    assertTrue(new IOFile(REPO, dir).delete(), "Repo directory could not be deleted.");
    assertFalse(existAfterDelete, "Package could not be deleted.");
  }

  /**
   * Tests usage of installed packages.
   * @throws Exception exception
   */
  @Test public void importPkg() throws Exception {
    // try with a package without dependencies
    try(QueryProcessor qp = new QueryProcessor(
        "import module namespace ns3='ns3'; ns3:test()", context)) {
      assertEquals(qp.value().serialize().toString(), "pkg2mod2");
    }
    // try with a package with dependencies
    try(QueryProcessor qp = new QueryProcessor(
        "import module namespace ns2='ns2'; ns2:test()", context)) {
      assertEquals(qp.value().serialize().toString(), "pkg2mod2");
    }
  }

  /**
   * Tests package delete.
   */
  @Test public void delete() {
    // try to delete a package which is not installed
    try {
      new RepoManager(context).delete("xyz");
      fail("Not installed package not detected.");
    } catch(final QueryException ex) {
      error(null, ex, REPO_NOTFOUND_X);
    }
    // install a package without dependencies (pkg3)
    execute(new RepoInstall(REPO + "pkg3.xar", null));

    // check if pkg3 is registered in the repo
    assertTrue(context.repo.pkgDict().containsKey(PKG3ID));

    // check if pkg3 was correctly unzipped
    final String pkg3Dir = normalize(PKG3ID);
    assertTrue(isDir(pkg3Dir));
    assertTrue(isFile(pkg3Dir + "/expath-pkg.xml"));
    assertTrue(isDir(pkg3Dir + "/pkg3"));
    assertTrue(isDir(pkg3Dir + "/pkg3/mod"));
    assertTrue(isFile(pkg3Dir + "/pkg3/mod/pkg3mod1.xql"));

    // install another package (pkg4) with a dependency to pkg3
    execute(new RepoInstall(REPO + "pkg4.xar", null));
    // check if pkg4 is registered in the repo
    assertTrue(context.repo.pkgDict().containsKey(PKG4ID));
    // check if pkg4 was correctly unzipped
    final String pkg4Dir = normalize(PKG4ID);
    assertTrue(isDir(pkg4Dir));
    assertTrue(isFile(pkg4Dir + "/expath-pkg.xml"));
    assertTrue(isDir(pkg4Dir + "/pkg4"));
    assertTrue(isDir(pkg4Dir + "/pkg4/mod"));
    assertTrue(isFile(pkg4Dir + "/pkg4/mod/pkg4mod1.xql"));

    // try to delete pkg3
    try {
      new RepoManager(context).delete(PKG3ID);
      fail("Package involved in a dependency was deleted.");
    } catch(final QueryException ex) {
      error(null, ex, REPO_DELETE_X_X);
    }
    // try to delete pkg4 (use package name)
    execute(new RepoDelete(PKG4, null));
    // check if pkg4 is unregistered from the repo
    assertFalse(context.repo.pkgDict().containsKey(PKG4ID));

    // check if pkg4 directory was deleted
    assertFalse(isDir(pkg4Dir));
    // try to delete pkg3 (use package dir)
    execute(new RepoDelete(PKG3ID, null));
    // check if pkg3 is unregistered from the repo
    assertFalse(context.repo.pkgDict().containsKey(PKG3ID));
    // check if pkg3 directory was deleted
    assertFalse(isDir(pkg3Dir));
  }

  // PRIVATE METHODS ==============================================================================

  /** Header string. */
  private static final byte[] HEADER = token("<package "
      + "xmlns='http://expath.org/ns/pkg' "
      + "spec='1.0' name='%' abbrev='%' version='%'>");
  /** Footer string. */
  private static final byte[] FOOTER = token("</package>");

  /**
   * Returns a package descriptor.
   * @param name package name
   * @param abbrev package abbreviation
   * @param version package version
   * @param cont package content
   * @return descriptor
   */
  private static IOContent desc(final String name, final String abbrev, final String version,
      final String cont) {
    return new IOContent(concat(Util.inf(HEADER, name, abbrev, version), token(cont), FOOTER));
  }

  /**
   * Checks if the specified package descriptor results in an error.
   * @param desc descriptor
   */
  private static void ok(final IO desc) {
    try {
      new PkgValidator(context.repo, null).check(new PkgParser(null).parse(desc));
    } catch(final QueryException ex) {
      fail("Unexpected exception thrown: " + ex);
    }
  }

  /**
   * Checks if the specified package descriptor results in an error.
   * @param desc descriptor
   * @param err expected error
   * @param exp information on expected error
   */
  private static void error(final IO desc, final QueryError err, final String exp) {
    try {
      new PkgValidator(context.repo, null).check(new PkgParser(null).parse(desc));
      fail(exp);
    } catch(final QueryException ex) {
      error(null, ex, err);
    }
  }

  /**
   * Checks if the specified path points to a file.
   * @param path file path
   * @return result of check
   */
  private static boolean isFile(final String path) {
    final IOFile file = new IOFile(REPO, path);
    return file.exists() && !file.isDir();
  }

  /**
   * Checks if the specified path points to a directory.
   * @param path file path
   * @return result of check
   */
  private static boolean isDir(final String path) {
    return new IOFile(REPO, path).isDir();
  }

  /**
   * Normalizes the given path.
   * @param path path
   * @return normalized path
   */
  private static String normalize(final String path) {
    return path.replaceAll("[^\\w.-]+", "-");
  }
}
