package org.basex.test.query.advanced;

import static org.basex.util.Token.*;
import static org.junit.Assert.*;

import java.io.File;

import org.basex.core.BaseXException;
import org.basex.core.Context;
import org.basex.core.cmd.RepoInstall;
import org.basex.io.IO;
import org.basex.io.IOContent;
import org.basex.query.QueryException;
import org.basex.query.util.Err;
import org.basex.query.util.repo.PkgParser;
import org.basex.query.util.repo.PkgValidator;
import org.basex.query.util.repo.RepoManager;
import org.basex.util.TokenList;
import org.basex.util.TokenMap;
import org.basex.util.TokenObjMap;
import org.basex.util.Util;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * This class tests the EXPath package API.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Rositsa Shadura
 */
public class PackageAPITest extends AdvancedQueryTest {
  /** Context. */
  protected static Context ctx;

  /** Prepare test. */
  @BeforeClass
  public static void setUpBeforeClass() {
    ctx = new Context();
    ctx.repo.init("etc/repo");
  }

  /** Tests repository initialization. */
  @Test
  public void testRepoInit() {
    // Check namespace dictionary
    final TokenObjMap<TokenList> nsDict = ctx.repo.nsDict();
    final TokenMap pkgDict = ctx.repo.pkgDict();

    assertEquals(3, nsDict.keys().length);
    assertNotNull(nsDict.get(token("ns1")));
    assertNotNull(nsDict.get(token("ns2")));
    assertNotNull(nsDict.get(token("ns3")));
    TokenList tl = new TokenList();
    tl = nsDict.get(token("ns1"));
    assertEquals(tl.size(), 2);
    assertTrue(tl.contains(token("http://www.pkg1.com-12.0")));
    assertTrue(tl.contains(token("http://www.pkg2.com-10.0")));
    tl = nsDict.get(token("ns2"));
    assertEquals(tl.size(), 1);
    assertTrue(tl.contains(token("http://www.pkg1.com-12.0")));
    tl = nsDict.get(token("ns3"));
    assertEquals(tl.size(), 1);
    assertTrue(tl.contains(token("http://www.pkg2.com-10.0")));
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
    error(new IOContent(token("<package " +
      "xmlns:http='http://expath.org/ns/pkg' spec='1.0'/>")),
      Err.PKGDESCINV, "Missing mandatory attribute not detected.");
  }

  /** Test for already installed package. */
  @Test
  public void alreadyInstalled() {
    error(desc("http://www.pkg1.com", "pkg1", "12.0", ""),
      Err.PKGINSTALLED, "Installed package not detected.");
  }

  /**
   * Tests package with not installed dependencies - dependency is defined with
   * no specific versions.
   */
  @Test
  public void notInstalledDeps() {
    error(desc("http://www.pkg5.com", "pkg5", "12.0",
      "<dependency package='http://www.pkg4.com'/>"),
      Err.PKGNOTINSTALLED, "Missing dependency not detected.");
  }

  /**
   * Tests package with not installed dependencies - dependency is defined with
   * version set.
   */
  @Test
  public void notInstalledDepVersion() {
    error(desc("http://www.pkg5.com", "pkg5", "12.0",
      "<dependency package='http://www.pkg1.com' versions='1.0 7.0'/>"),
      Err.PKGNOTINSTALLED, "Missing dependency not detected.");
  }

  /**
   * Tests package with not installed dependencies - dependency is defined with
   * version template.
   */
  @Test
  public void notInstalledDepTemp() {
    error(desc("http://www.pkg5.com", "pkg5", "12.0",
      "<dependency package='http://www.pkg1.com' versions='12.7'/>"),
      Err.PKGNOTINSTALLED, "Missing dependency not detected.");
  }

  /**
   * Tests package with not installed dependencies - dependency is defined with
   * version template for minimal acceptable version.
   */
  @Test
  public void notInstalledMin() {
    error(desc("http://www.pkg5.com", "pkg5", "12.0",
      "<dependency package='http://www.pkg1.com' versions='12.7'/>"),
      Err.PKGNOTINSTALLED, "Missing dependency not detected.");
  }

  /**
   * Tests package with not installed dependencies - dependency is defined with
   * version template for maximal acceptable version.
   */
  @Test
  public void notInstalledMax() {
    error(desc("http://www.pkg5.com", "pkg5", "12.0",
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
      "<dependency package='http://www.pkg1.com' semver-min='5.7' " +
      "semver-max='11'/>"),
      Err.PKGNOTINSTALLED, "Missing dependency not detected.");
  }

  /**
   * Tests package with component which is already installed as part of another
   * package.
   */
  @Test
  public void alreadyAnotherInstalled() {
    error(desc("http://www.pkg5.com", "pkg5", "12.0",
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
        "<xquery><namespace>ns1</namespace>" +
        "<file>pkg1mod1.xql</file></xquery>"));
  }

  /**
   * Tests valid package.
   */
  @Test
  public void valid() {
    ok(desc("http://www.pkg1.com", "pkg1", "10.0",
      "<dependency package='http://www.pkg1.com' semver-min='11'/>" +
      "<xquery><namespace>ns3</namespace>" +
      "<file>pkg5mod1.xql</file></xquery>"));
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
    new RepoInstall("etc/repo/pkg3.xar", null).execute(ctx);
    final File pkgDir = new File("etc/repo/pkg3");
    assertTrue(pkgDir.exists());
    assertTrue(pkgDir.isDirectory());
    final File pkgDesc = new File("etc/repo/pkg3/expath-pkg.xml");
    assertTrue(pkgDesc.exists());
    final File modDir = new File("etc/repo/pkg3/pkg3");
    assertTrue(modDir.exists());
    assertTrue(modDir.isDirectory());
    final File modFile = new File("etc/repo/pkg3/pkg3/pkg3mod1.xql");
    assertTrue(modFile.exists());

    modFile.delete();
    modDir.delete();
    pkgDesc.delete();
    pkgDir.delete();
  }
}
