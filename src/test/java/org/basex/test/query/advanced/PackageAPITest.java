package org.basex.test.query.advanced;

import static org.basex.util.Token.*;
import static org.junit.Assert.*;

import java.io.File;

import org.basex.core.BaseXException;
import org.basex.core.Context;
import org.basex.core.cmd.RepoDelete;
import org.basex.core.cmd.RepoInstall;
import org.basex.io.IO;
import org.basex.io.IOContent;
import org.basex.query.QueryException;
import org.basex.query.QueryProcessor;
import org.basex.query.util.Err;
import org.basex.query.util.pkg.PkgParser;
import org.basex.query.util.pkg.PkgValidator;
import org.basex.query.util.pkg.RepoManager;
import org.basex.util.TokenMap;
import org.basex.util.TokenObjMap;
import org.basex.util.TokenSet;
import org.basex.util.Util;
import org.junit.Before;
import org.junit.Test;

/**
 * This class tests the EXPath package API.
 * @author BaseX Team 2005-11, BSD License
 * @author Rositsa Shadura
 */
public final class PackageAPITest extends AdvancedQueryTest {
  /** Test repository. */
  protected static final String REPO = "etc/test/repo/";
  /** Context. */
  protected static Context ctx;

  /** Prepare test. */
  @Before
  public void setUpBeforeClass() {
    ctx = new Context();
    ctx.repo.init(REPO);
  }

  /** Tests repository initialization. */
  @Test
  public void testRepoInit() {
    // Check namespace dictionary
    final TokenObjMap<TokenSet> nsDict = ctx.repo.nsDict();
    final TokenMap pkgDict = ctx.repo.pkgDict();

    assertEquals(3, nsDict.keys().length);
    assertNotNull(nsDict.get(token("ns1")));
    assertNotNull(nsDict.get(token("ns2")));
    assertNotNull(nsDict.get(token("ns3")));
    TokenSet ts = new TokenSet();
    ts = nsDict.get(token("ns1"));
    assertEquals(ts.size(), 2);
    assertTrue(ts.id(token("http://www.pkg1.com-12.0")) != 0);
    assertTrue(ts.id(token("http://www.pkg2.com-10.0")) != 0);
    ts = nsDict.get(token("ns2"));
    assertEquals(ts.size(), 1);
    assertTrue(ts.id(token("http://www.pkg1.com-12.0")) != 0);
    ts = nsDict.get(token("ns3"));
    assertEquals(ts.size(), 1);
    assertTrue(ts.id(token("http://www.pkg2.com-10.0")) != 0);
    // Check package dictionary
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
    error(new IOContent(token("<package "
        + "xmlns:http='http://expath.org/ns/pkg' spec='1.0'/>")),
        Err.PKGDESCINV, "Missing mandatory attribute not detected.");
  }

  /** Test for already installed package. */
  @Test
  public void alreadyInstalled() {
    error(desc("http://www.pkg1.com", "pkg1", "12.0", ""), Err.PKGINSTALLED,
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
        Err.PKGNOTINSTALLED, "Missing dependency not detected.");
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
        Err.PKGNOTINSTALLED, "Missing dependency not detected.");
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
        Err.PKGNOTINSTALLED, "Missing dependency not detected.");
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
        Err.PKGNOTINSTALLED, "Missing dependency not detected.");
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
        Err.PKGNOTINSTALLED, "Missing dependency not detected.");
  }

  /**
   * Tests package with not installed dependencies - dependency is defined with
   * version templates for minimal and maximal acceptable version.
   */
  @Test
  public void notInstalledMinMax() {
    error(desc("http://www.pkg5.com", "pkg5", "12.0",
        "<dependency package='http://www.pkg1.com' semver-min='5.7' "
            + "semver-max='11'/>"), Err.PKGNOTINSTALLED,
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
   * Checks if the specified package descriptor results in an error.
   * @param desc descriptor
   */
  private static void ok(final IO desc) {
    try {
      new PkgValidator(ctx, null).check(new PkgParser(ctx, null).parse(desc));
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
  private static void error(final IO desc, final Err err, final String exp) {
    try {
      new PkgValidator(ctx, null).check(new PkgParser(ctx, null).parse(desc));
      fail(exp);
    } catch(final QueryException ex) {
      check(ex, err);
    }
  }

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
   * Tests package installation.
   * @throws BaseXException database exception
   */
  @Test
  public void testRepoInstall() throws BaseXException {
    // Try to install non-existing package
    try {
      new RepoManager(ctx).install("etc/pkg", null);
      fail("Not existing package not detected.");
    } catch(final QueryException ex) {
      check(ex, Err.PKGNOTEXIST);
    }
    // Try to install a package
    new RepoInstall(REPO + "pkg3.xar", null).execute(ctx);
    final File pkgDir = new File(REPO + "pkg3");
    assertTrue(pkgDir.exists());
    assertTrue(pkgDir.isDirectory());
    final File pkgDesc = new File(REPO + "pkg3/expath-pkg.xml");
    assertTrue(pkgDesc.exists());
    final File modDir1 = new File(REPO + "pkg3/pkg3");
    assertTrue(modDir1.exists());
    assertTrue(modDir1.isDirectory());
    final File modDir2 = new File(REPO + "pkg3/pkg3/mod");
    assertTrue(modDir2.exists());
    assertTrue(modDir2.isDirectory());
    final File modFile = new File(REPO + "pkg3/pkg3/mod/pkg3mod1.xql");
    assertTrue(modFile.exists());

    // Delete package
    modFile.delete();
    modDir2.delete();
    modDir1.delete();
    pkgDesc.delete();
    pkgDir.delete();

  }

  /**
   * Tests usage of installed packages.
   * @throws QueryException query exception
   */
  @Test
  public void testImport() throws QueryException {
    // Try with a package without dependencies
    final QueryProcessor qp1 = new QueryProcessor(
        "import module namespace ns3='ns3';\nns3:test()", ctx);
    assertEquals(qp1.execute().toString(), "pkg2mod2");
    qp1.execute();

    // Try with a package with dependencies
    final QueryProcessor qp2 = new QueryProcessor(
        "import module namespace ns2='ns2';\nns2:test()", ctx);
    assertEquals(qp2.execute().toString(), "pkg2mod2");
    qp2.execute();
  }

  /**
   * Tests package delete.
   * @throws BaseXException database exception
   */
  @Test
  public void testDelete() throws BaseXException {
    // Install a package without dependencies (pkg3)
    new RepoInstall(REPO + "pkg3.xar", null).execute(ctx);
    // Check if pkg3 is registered in the repo
    assertNotNull(ctx.repo.pkgDict().id(token("pkg3-10.0")) != 0);
    // Check if pkg3 was correctly unzipped
    final File pkgDir1 = new File(REPO + "pkg3");
    assertTrue(pkgDir1.exists());
    assertTrue(pkgDir1.isDirectory());
    final File pkgDesc1 = new File(REPO + "pkg3/expath-pkg.xml");
    assertTrue(pkgDesc1.exists());
    final File modDir1 = new File(REPO + "pkg3/pkg3/mod");
    assertTrue(modDir1.exists());
    assertTrue(modDir1.isDirectory());
    final File modFile1 = new File(REPO + "pkg3/pkg3/mod/pkg3mod1.xql");
    assertTrue(modFile1.exists());
    // Install another package (pkg4) with a dependency to pkg3
    new RepoInstall(REPO + "pkg4.xar", null).execute(ctx);
    // Check if pkg4 is registered in the repo
    assertNotNull(ctx.repo.pkgDict().id(token("pkg4-2.0")) != 0);
    // Check if pkg3 was correctly unzipped
    final File pkgDir2 = new File(REPO + "pkg4");
    assertTrue(pkgDir2.exists());
    assertTrue(pkgDir2.isDirectory());
    final File pkgDesc2 = new File(REPO + "pkg4/expath-pkg.xml");
    assertTrue(pkgDesc2.exists());
    final File modDir2 = new File(REPO + "pkg4/pkg4/mod");
    assertTrue(modDir2.exists());
    assertTrue(modDir2.isDirectory());
    final File modFile2 = new File(REPO + "pkg4/pkg4/mod/pkg4mod1.xql");
    assertTrue(modFile2.exists());
    // Try to delete pkg3
    try {
      new RepoManager(ctx).delete("pkg3", null);
      fail("Package involved in a dependency was deleted.");
    } catch(QueryException ex) {
      check(ex, Err.PKGDEP);
    }
    // Try to delete pkg4 (use package name)
    new RepoDelete("http://www.pkg4.com", null).execute(ctx);
    // Check if pkg4 is unregistered from the repo
    assertTrue(ctx.repo.pkgDict().id(token("pkg4-2.0")) == 0);
    // Check if pkg4 directory was deleted
    assertTrue(!pkgDir2.exists());
    // Try to delete pkg3 (use package dir)
    new RepoDelete("pkg3", null).execute(ctx);
    // Check if pkg3 is unregistered from the repo
    assertTrue(ctx.repo.pkgDict().id(token("pkg3-10.0")) == 0);
    // Check if pkg4 directory was deleted
    assertTrue(!pkgDir1.exists());
  }
}
