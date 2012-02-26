package org.basex.test.query.expr;

import static org.basex.util.Token.*;
import static org.junit.Assert.*;

import java.io.File;

import org.basex.core.BaseXException;
import org.basex.core.Context;
import org.basex.core.cmd.RepoDelete;
import org.basex.core.cmd.RepoInstall;
import org.basex.io.IO;
import org.basex.io.IOContent;
import org.basex.io.IOFile;
import org.basex.query.QueryException;
import org.basex.query.QueryProcessor;
import org.basex.query.util.Err;
import org.basex.query.util.pkg.PkgParser;
import org.basex.query.util.pkg.PkgValidator;
import org.basex.query.util.pkg.RepoManager;
import org.basex.test.query.AdvancedQueryTest;
import org.basex.util.Util;
import org.basex.util.hash.TokenMap;
import org.basex.util.hash.TokenObjMap;
import org.basex.util.hash.TokenSet;
import org.junit.Before;
import org.junit.Test;

/**
 * This class tests the EXPath package API.
 * @author BaseX Team 2005-12, BSD License
 * @author Rositsa Shadura
 */
public final class PackageAPITest extends AdvancedQueryTest {
  /** Test repository. */
  private static final String REPO = "src/test/resources/repo/";
  /** Context. */
  private Context ctx;

  /** Prepare test. */
  @Before
  public void setUpBeforeClass() {
    for(final IOFile f : new IOFile(REPO).children()) {
      if(f.isDir() && f.name().contains(".")) f.delete();
    }
    ctx = new Context();
    ctx.repo.init(REPO);
  }

  /** Tests repository initialization. */
  @Test
  public void repoInit() {
    // check namespace dictionary
    final TokenObjMap<TokenSet> nsDict = ctx.repo.nsDict();
    final TokenMap pkgDict = ctx.repo.pkgDict();

    assertEquals(3, nsDict.keys().length);
    assertNotNull(nsDict.get(token("ns1")));
    assertNotNull(nsDict.get(token("ns2")));
    assertNotNull(nsDict.get(token("ns3")));
    TokenSet ts = nsDict.get(token("ns1"));
    assertEquals(ts.size(), 2);
    assertTrue(ts.id(token("http://www.pkg1.com-12.0")) != 0);
    assertTrue(ts.id(token("http://www.pkg2.com-10.0")) != 0);
    ts = nsDict.get(token("ns2"));
    assertEquals(ts.size(), 1);
    assertTrue(ts.id(token("http://www.pkg1.com-12.0")) != 0);
    ts = nsDict.get(token("ns3"));
    assertEquals(ts.size(), 1);
    assertTrue(ts.id(token("http://www.pkg2.com-10.0")) != 0);
    // check package dictionary
    assertEquals(pkgDict.keys().length, 2);
    assertNotNull(pkgDict.get(token("http://www.pkg1.com-12.0")));
    assertNotNull(pkgDict.get(token("http://www.pkg2.com-10.0")));
    assertEquals("pkg1",
        string(pkgDict.get(token("http://www.pkg1.com-12.0"))));
    assertEquals("pkg2",
        string(pkgDict.get(token("http://www.pkg2.com-10.0"))));
  }

  /** Test for missing mandatory attributes. */
  @Test
  public void mandatoryAttr() {
    error(new IOContent("<package xmlns:http='http://expath.org/ns/pkg' spec='1.0'/>"),
        Err.PKGDESCINV, "Missing mandatory attribute not detected.");
  }

  /** Test for already installed package. */
  @Test
  public void alreadyInstalled() {
    error(desc("http://www.pkg1.com", "pkg1", "12.0", ""), Err.PKGINST,
        "Installed package not detected.");
  }

  /**
   * Tests package with not installed dependencies - dependency is defined with
   * no specific versions.
   */
  @Test
  public void notInstalledDeps() {
    error(
        desc("http://www.pkg5.com", "pkg5", "12.0",
            "<dependency package='http://www.pkg4.com'/>"),
        Err.NECPKGNOTINST, "Missing dependency not detected.");
  }

  /**
   * Tests package with not installed dependencies - dependency is defined with
   * version set.
   */
  @Test
  public void notInstalledDepVersion() {
    error(
        desc("http://www.pkg5.com", "pkg5", "12.0",
            "<dependency package='http://www.pkg1.com' versions='1.0 7.0'/>"),
        Err.NECPKGNOTINST, "Missing dependency not detected.");
  }

  /**
   * Tests package with not installed dependencies - dependency is defined with
   * version template.
   */
  @Test
  public void notInstalledDepTemp() {
    error(
        desc("http://www.pkg5.com", "pkg5", "12.0",
            "<dependency package='http://www.pkg1.com' versions='12.7'/>"),
        Err.NECPKGNOTINST, "Missing dependency not detected.");
  }

  /**
   * Tests package with not installed dependencies - dependency is defined with
   * version template for minimal acceptable version.
   */
  @Test
  public void notInstalledMin() {
    error(
        desc("http://www.pkg5.com", "pkg5", "12.0",
            "<dependency package='http://www.pkg1.com' versions='12.7'/>"),
        Err.NECPKGNOTINST, "Missing dependency not detected.");
  }

  /**
   * Tests package with not installed dependencies - dependency is defined with
   * version template for maximal acceptable version.
   */
  @Test
  public void notInstalledMax() {
    error(
        desc("http://www.pkg5.com", "pkg5", "12.0",
            "<dependency package='http://www.pkg1.com' semver-max='11'/>"),
        Err.NECPKGNOTINST, "Missing dependency not detected.");
  }

  /**
   * Tests package with not installed dependencies - dependency is defined with
   * version templates for minimal and maximal acceptable version.
   */
  @Test
  public void notInstalledMinMax() {
    error(desc("http://www.pkg5.com", "pkg5", "12.0",
        "<dependency package='http://www.pkg1.com' semver-min='5.7' "
            + "semver-max='11'/>"), Err.NECPKGNOTINST,
        "Missing dependency not detected.");
  }

  /**
   * Tests package with component which is already installed as part of another
   * package.
   */
  @Test
  public void alreadyAnotherInstalled() {
    error(
        desc("http://www.pkg5.com", "pkg5", "12.0",
        "<xquery><namespace>ns1</namespace><file>pkg1mod1.xql</file></xquery>"),
        Err.MODISTALLED, "Already installed component not detected.");
  }

  /**
   * Tests package with dependency on an older version of BaseX.
   */
  @Test
  public void notSupported() {
    error(desc("http://www.pkg5.com", "pkg5", "12.0",
        "<dependency processor='basex' "
            + "semver='5.0'/>"), Err.PKGNOTSUPP,
        "Unsupported package not detected.");
  }

  /**
   * Tests package with component which is already installed as part of another
   * version of the same package.
   */
  @Test
  public void alreadyAnotherSame() {
    ok(desc("http://www.pkg1.com", "pkg1", "10.0",
        "<xquery><namespace>ns1</namespace>"
            + "<file>pkg1mod1.xql</file></xquery>"));
  }

  /**
   * Tests valid package.
   */
  @Test
  public void valid() {
    ok(desc("http://www.pkg1.com", "pkg1", "10.0",
        "<dependency package='http://www.pkg1.com' semver-min='11'/>"
            + "<xquery><namespace>ns3</namespace>"
            + "<file>pkg5mod1.xql</file></xquery>"));
  }

  /**
   * Tests package installation.
   * @throws BaseXException database exception
   */
  @Test
  public void repoInstall() throws BaseXException {
    // try to install non-existing package
    try {
      new RepoManager(ctx.repo).install("src/test/resources/pkg", null);
      fail("Not existing package not detected.");
    } catch(final QueryException ex) {
      check(ex, Err.PKGNOTEXIST);
    }
    // try to install a package
    new RepoInstall(REPO + "pkg3.xar", null).execute(ctx);
    final String dirName = normalize("http://www.pkg3.com-10.0");
    assertTrue(dir(dirName));
    assertTrue(file(dirName + "/expath-pkg.xml"));
    assertTrue(dir(dirName + "/pkg3"));
    assertTrue(dir(dirName + "/pkg3/mod"));
    assertTrue(file(dirName + "/pkg3/mod/pkg3mod1.xql"));
    assertTrue(new IOFile(REPO, dirName).delete());
  }

  /**
   * Tests installing of a package containing a jar file.
   * @throws BaseXException database exception
   * @throws QueryException query exception
   */
  @Test
  public void installJar() throws BaseXException, QueryException {
    // install package
    new RepoInstall(REPO + "testJar.xar", null).execute(ctx);

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
        "import module namespace j='jar'; j:print('test')", ctx);
    assertEquals(qp.execute().toString(), "test");
    qp.close();

    // delete package
    assertTrue(new IOFile(REPO, dir).delete());
  }

  /**
   * Tests usage of installed packages.
   * @throws QueryException query exception
   */
  @Test
  public void importPkg() throws QueryException {
    // try with a package without dependencies
    final QueryProcessor qp1 = new QueryProcessor(
        "import module namespace ns3='ns3'; ns3:test()", ctx);
    assertEquals(qp1.execute().toString(), "pkg2mod2");
    qp1.execute();

    // try with a package with dependencies
    final QueryProcessor qp2 = new QueryProcessor(
        "import module namespace ns2='ns2'; ns2:test()", ctx);
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
      new RepoManager(ctx.repo).delete("xyz", null);
      fail("Not installed package not detected.");
    } catch(final QueryException ex) {
      check(ex, Err.PKGNOTEXIST);
    }
    // install a package without dependencies (pkg3)
    new RepoInstall(REPO + "pkg3.xar", null).execute(ctx);

    // check if pkg3 is registered in the repo
    assertNotNull(ctx.repo.pkgDict().id(token("pkg3-10.0")) != 0);

    // check if pkg3 was correctly unzipped
    final String pkg3Dir = normalize("http://www.pkg3.com-10.0");
    assertTrue(dir(pkg3Dir));
    assertTrue(file(pkg3Dir + "/expath-pkg.xml"));
    assertTrue(dir(pkg3Dir + "/pkg3"));
    assertTrue(dir(pkg3Dir + "/pkg3/mod"));
    assertTrue(file(pkg3Dir + "/pkg3/mod/pkg3mod1.xql"));

    // install another package (pkg4) with a dependency to pkg3
    new RepoInstall(REPO + "pkg4.xar", null).execute(ctx);
    // check if pkg4 is registered in the repo
    assertNotNull(ctx.repo.pkgDict().id(token("pkg4-2.0")) != 0);
    // check if pkg4 was correctly unzipped
    final String pkg4Dir = normalize("http://www.pkg4.com-2.0");
    assertTrue(dir(pkg4Dir));
    assertTrue(file(pkg4Dir + "/expath-pkg.xml"));
    assertTrue(dir(pkg4Dir + "/pkg4"));
    assertTrue(dir(pkg4Dir + "/pkg4/mod"));
    assertTrue(file(pkg4Dir + "/pkg4/mod/pkg4mod1.xql"));

    // try to delete pkg3
    try {
      new RepoManager(ctx.repo).delete(pkg3Dir, null);
      fail("Package involved in a dependency was deleted.");
    } catch(final QueryException ex) {
      check(ex, Err.PKGDEP);
    }
    // try to delete pkg4 (use package name)
    new RepoDelete("http://www.pkg4.com", null).execute(ctx);
    // check if pkg4 is unregistered from the repo
    assertEquals(0, ctx.repo.pkgDict().id(token("http://www.pkg4.com-2.0")));

    // check if pkg4 directory was deleted
    assertTrue(!dir(pkg4Dir));
    // try to delete pkg3 (use package dir)
    new RepoDelete(pkg3Dir, null).execute(ctx);
    // check if pkg3 is unregistered from the repo
    assertEquals(0, ctx.repo.pkgDict().id(token("http://www.pkg3.com-10.0")));
    // check if pkg3 directory was deleted
    assertTrue(!dir(pkg3Dir));
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
      new PkgValidator(ctx.repo, null).check(
         new PkgParser(ctx.repo, null).parse(desc));
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
      new PkgValidator(ctx.repo, null).check(
         new PkgParser(ctx.repo, null).parse(desc));
      fail(exp);
    } catch(final QueryException ex) {
      check(ex, err);
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
