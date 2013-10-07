package org.basex.test.query.expr;

import static org.basex.util.Token.*;
import static org.junit.Assert.*;

import java.io.*;

import org.basex.core.*;
import org.basex.core.cmd.*;
import org.basex.io.*;
import org.basex.query.*;
import org.basex.query.util.*;
import org.basex.query.util.pkg.*;
import org.basex.test.query.*;
import org.basex.util.*;
import org.basex.util.hash.*;
import org.junit.*;

/**
 * This class tests the EXPath package API.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Rositsa Shadura
 */
public final class PackageAPITest extends AdvancedQueryTest {
  /** Test repository. */
  private static final String REPO = "src/test/resources/repo/";

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
  /** Pkg1 identifier. */
  private static final String PKG4ID = PKG4 + "-2.0";

  /** Prepare test. */
  @Before
  public void setUpBeforeClass() {
    for(final IOFile f : new IOFile(REPO).children()) {
      if(f.isDir() && f.name().contains(".")) f.delete();
    }
    context = new Context();
    context.globalopts.string(GlobalOptions.REPOPATH, REPO);
  }

  /** Tests repository initialization. */
  @Test
  public void repoInit() {
    // check namespace dictionary
    final TokenObjMap<TokenSet> nsDict = context.repo.nsDict();
    final TokenMap pkgDict = context.repo.pkgDict();

    assertEquals(3, nsDict.size());
    assertTrue(nsDict.contains(token("ns1")));
    assertTrue(nsDict.contains(token("ns2")));
    assertTrue(nsDict.contains(token("ns3")));
    TokenSet ts = nsDict.get(token("ns1"));
    assertEquals(ts.size(), 2);
    assertTrue(ts.contains(token(PKG1ID)));
    assertTrue(ts.contains(token(PKG2ID)));
    ts = nsDict.get(token("ns2"));
    assertEquals(ts.size(), 1);
    assertTrue(ts.contains(token(PKG1ID)));
    ts = nsDict.get(token("ns3"));
    assertEquals(ts.size(), 1);
    assertTrue(ts.contains(token(PKG2ID)));
    // check package dictionary
    assertEquals(pkgDict.size(), 2);
    assertTrue(pkgDict.contains(token(PKG1ID)));
    assertTrue(pkgDict.contains(token(PKG2ID)));
    assertEquals("pkg1", string(pkgDict.get(token(PKG1ID))));
    assertEquals("pkg2", string(pkgDict.get(token(PKG2ID))));
  }

  /** Test for missing mandatory attributes. */
  @Test
  public void mandatoryAttr() {
    error(new IOContent("<package xmlns:http='http://expath.org/ns/pkg' spec='1.0'/>"),
        Err.BXRE_DESC, "Missing mandatory attribute not detected.");
  }

  /**
   * Tests package with not installed dependencies - dependency is defined with
   * no specific versions.
   */
  @Test
  public void notInstalledDeps() {
    error(
        desc(PKG5, "pkg5", "12.0",
            "<dependency package='" + PKG4 + "'/>"),
        Err.BXRE_NOTINST, "Missing dependency not detected.");
  }

  /**
   * Tests package with not installed dependencies - dependency is defined with
   * version set.
   */
  @Test
  public void notInstalledDepVersion() {
    error(
        desc(PKG5, "pkg5", "12.0",
            "<dependency package='" + PKG1 + "' versions='1.0 7.0'/>"),
        Err.BXRE_NOTINST, "Missing dependency not detected.");
  }

  /**
   * Tests package with not installed dependencies - dependency is defined with
   * version template.
   */
  @Test
  public void notInstalledDepTemp() {
    error(
        desc(PKG5, "pkg5", "12.0",
            "<dependency package='" + PKG1 + "' versions='12.7'/>"),
        Err.BXRE_NOTINST, "Missing dependency not detected.");
  }

  /**
   * Tests package with not installed dependencies - dependency is defined with
   * version template for minimal acceptable version.
   */
  @Test
  public void notInstalledMin() {
    error(
        desc(PKG5, "pkg5", "12.0",
            "<dependency package='" + PKG1 + "' versions='12.7'/>"),
        Err.BXRE_NOTINST, "Missing dependency not detected.");
  }

  /**
   * Tests package with not installed dependencies - dependency is defined with
   * version template for maximal acceptable version.
   */
  @Test
  public void notInstalledMax() {
    error(
        desc(PKG5, "pkg5", "12.0",
            "<dependency package='" + PKG1 + "' semver-max='11'/>"),
        Err.BXRE_NOTINST, "Missing dependency not detected.");
  }

  /**
   * Tests package with not installed dependencies - dependency is defined with
   * version templates for minimal and maximal acceptable version.
   */
  @Test
  public void notInstalledMinMax() {
    error(desc(PKG5, "pkg5", "12.0",
        "<dependency package='" + PKG1 + "' semver-min='5.7' "
            + "semver-max='11'/>"), Err.BXRE_NOTINST,
        "Missing dependency not detected.");
  }

  /**
   * Tests package with component which is already installed as part of another
   * package.
   */
  @Test
  public void alreadyAnotherInstalled() {
    error(desc(PKG5, "pkg5", "12.0",
        "<xquery><namespace>ns1</namespace><file>pkg1mod1.xql</file></xquery>"),
        Err.BXRE_INST, "Already installed component not detected.");
  }

  /**
   * Tests package with dependency on an older version of BaseX.
   */
  @Test
  public void notSupported() {
    error(desc(PKG5, "pkg5", "12.0",
        "<dependency processor='basex' semver='5.0'/>"), Err.BXRE_VERSION,
        "Unsupported package not detected.");
  }

  /**
   * Tests package with component which is already installed as part of another
   * version of the same package.
   */
  @Test
  public void alreadyAnotherSame() {
    ok(desc(PKG1, "pkg1", "10.0",
        "<xquery><namespace>ns1</namespace><file>pkg1mod1.xql</file></xquery>"));
  }

  /**
   * Tests valid package.
   */
  @Test
  public void valid() {
    ok(desc(PKG1, "pkg1", "10.0", "<dependency package='" + PKG1 + "' semver-min='11'/>"
        + "<xquery><namespace>ns3</namespace>"
        + "<file>pkg5mod1.xql</file></xquery>"));
  }

  /**
   * Tests ability to import two modules from the same package.
   * @throws QueryException query exception
   */
  @Test
  public void importTwoModulesFromPkg() throws QueryException {
    final QueryProcessor qp = new QueryProcessor(
      "import module namespace ns1='ns1';" +
      "import module namespace ns3='ns3';" +
      "(ns1:test2() eq 'pkg2mod1') and (ns3:test() eq 'pkg2mod2')",
      context);
    assertEquals(qp.execute().toString(), "true");
    qp.execute();
  }

  /**
   * Tests package installation.
   * @throws BaseXException database exception
   */
  @Test
  public void repoInstall() throws BaseXException {
    // try to install non-existing package
    try {
      new RepoManager(context).install("src/test/resources/pkg");
      fail("Not existing package not detected.");
    } catch(final QueryException ex) {
      check(null, ex, Err.BXRE_WHICH);
    }
    // try to install a package
    new RepoInstall(REPO + "pkg3.xar", null).execute(context);
    final String dir = normalize(PKG3ID);
    assertTrue(dir(dir));
    assertTrue(file(dir + "/expath-pkg.xml"));
    assertTrue(dir(dir + "/pkg3"));
    assertTrue(dir(dir + "/pkg3/mod"));
    assertTrue(file(dir + "/pkg3/mod/pkg3mod1.xql"));
    assertTrue(new IOFile(REPO, dir).delete());
  }

  /**
   * Tests installing of a package containing a jar file.
   * @throws BaseXException database exception
   * @throws QueryException query exception
   */
  @Test
  public void installJar() throws BaseXException, QueryException {
    // install package
    new RepoInstall(REPO + "testJar.xar", null).execute(context);

    // ensure package was properly installed
    final String dir = normalize("jarPkg-1.0.0");
    assertTrue(dir(dir));
    assertTrue(file(dir + "/expath-pkg.xml"));
    assertTrue(file(dir + "/basex.xml"));
    assertTrue(dir(dir + "/jar"));
    assertTrue(file(dir + "/jar/test.jar"));
    assertTrue(file(dir + "/jar/wrapper.xq"));

    // use package
    final QueryProcessor qp = new QueryProcessor(
        "import module namespace j='jar'; j:print('test')", context);
    assertEquals(qp.execute().toString(), "test");
    qp.close();

    // delete package
    assertTrue("Repo directory could not be deleted.", new IOFile(REPO, dir).delete());
  }

  /**
   * Tests usage of installed packages.
   * @throws QueryException query exception
   */
  @Test
  public void importPkg() throws QueryException {
    // try with a package without dependencies
    final QueryProcessor qp1 = new QueryProcessor(
        "import module namespace ns3='ns3'; ns3:test()", context);
    assertEquals(qp1.execute().toString(), "pkg2mod2");
    qp1.execute();

    // try with a package with dependencies
    final QueryProcessor qp2 = new QueryProcessor(
        "import module namespace ns2='ns2'; ns2:test()", context);
    assertEquals(qp2.execute().toString(), "pkg2mod2");
    qp2.execute();
  }

  /**
   * Tests package delete.
   * @throws BaseXException database exception
   */
  @Test
  public void delete() throws BaseXException {
    // try to delete a package which is not installed
    try {
      new RepoManager(context).delete("xyz");
      fail("Not installed package not detected.");
    } catch(final QueryException ex) {
      check(null, ex, Err.BXRE_WHICH);
    }
    // install a package without dependencies (pkg3)
    new RepoInstall(REPO + "pkg3.xar", null).execute(context);

    // check if pkg3 is registered in the repo
    assertTrue(context.repo.pkgDict().contains(token(PKG3ID)));

    // check if pkg3 was correctly unzipped
    final String pkg3Dir = normalize(PKG3ID);
    assertTrue(dir(pkg3Dir));
    assertTrue(file(pkg3Dir + "/expath-pkg.xml"));
    assertTrue(dir(pkg3Dir + "/pkg3"));
    assertTrue(dir(pkg3Dir + "/pkg3/mod"));
    assertTrue(file(pkg3Dir + "/pkg3/mod/pkg3mod1.xql"));

    // install another package (pkg4) with a dependency to pkg3
    new RepoInstall(REPO + "pkg4.xar", null).execute(context);
    // check if pkg4 is registered in the repo
    assertTrue(context.repo.pkgDict().contains(token(PKG4ID)));
    // check if pkg4 was correctly unzipped
    final String pkg4Dir = normalize(PKG4ID);
    assertTrue(dir(pkg4Dir));
    assertTrue(file(pkg4Dir + "/expath-pkg.xml"));
    assertTrue(dir(pkg4Dir + "/pkg4"));
    assertTrue(dir(pkg4Dir + "/pkg4/mod"));
    assertTrue(file(pkg4Dir + "/pkg4/mod/pkg4mod1.xql"));

    // try to delete pkg3
    try {
      new RepoManager(context).delete(PKG3ID);
      fail("Package involved in a dependency was deleted.");
    } catch(final QueryException ex) {
      check(null, ex, Err.BXRE_DEP);
    }
    // try to delete pkg4 (use package name)
    new RepoDelete(PKG4, null).execute(context);
    // check if pkg4 is unregistered from the repo
    assertFalse(context.repo.pkgDict().contains(token(PKG4ID)));

    // check if pkg4 directory was deleted
    assertFalse(dir(pkg4Dir));
    // try to delete pkg3 (use package dir)
    new RepoDelete(PKG3ID, null).execute(context);
    // check if pkg3 is unregistered from the repo
    assertFalse(context.repo.pkgDict().contains(token(PKG3ID)));
    // check if pkg3 directory was deleted
    assertFalse(dir(pkg3Dir));
  }

  // PRIVATE METHODS ==========================================================

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
  private static IOContent desc(final String name, final String abbrev,
      final String version, final String cont) {

    return new IOContent(concat(Util.inf(HEADER, name, abbrev, version),
        token(cont), FOOTER));
  }

  /**
   * Checks if the specified package descriptor results in an error.
   * @param desc descriptor
   */
  private void ok(final IO desc) {
    try {
      new PkgValidator(context.repo, null).check(
         new PkgParser(context.repo, null).parse(desc));
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
  private void error(final IO desc, final Err err, final String exp) {
    try {
      new PkgValidator(context.repo, null).check(
         new PkgParser(context.repo, null).parse(desc));
      fail(exp);
    } catch(final QueryException ex) {
      check(null, ex, err);
    }
  }

  /**
   * Checks if the specified path points to a file.
   * @param path file path
   * @return result of check
   */
  private static boolean file(final String path) {
    final File file = new File(REPO + path);
    return file.exists() && !file.isDirectory();
  }

  /**
   * Checks if the specified path points to a directory.
   * @param path file path
   * @return result of check
   */
  private static boolean dir(final String path) {
    return new File(REPO + path).isDirectory();
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
