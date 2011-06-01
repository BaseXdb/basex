package org.basex.test.query.advanced;

import static org.basex.util.Token.*;
import static org.junit.Assert.*;

import java.io.File;

import org.basex.core.BaseXException;
import org.basex.core.Context;
import org.basex.core.Prop;
import org.basex.core.cmd.RepoInstall;
import org.basex.io.IO;
import org.basex.io.IOContent;
import org.basex.query.QueryException;
import org.basex.query.util.Err;
import org.basex.query.util.repo.PkgParser;
import org.basex.query.util.repo.PkgValidator;
import org.basex.query.util.repo.Repo;
import org.basex.query.util.repo.RepoManager;
import org.basex.util.TokenList;
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
    ctx.prop.set(Prop.REPOPATH, "etc/repo");
    ctx.repo = new Repo(ctx);
  }

  /** Tests repository initialization. */
  @Test
  public void testRepoInit() {
    // Check namespace dictionary
    assertEquals(3, ctx.repo.nsDict.keys().length);
    assertNotNull(ctx.repo.nsDict.get(token("ns1")));
    assertNotNull(ctx.repo.nsDict.get(token("ns2")));
    assertNotNull(ctx.repo.nsDict.get(token("ns3")));
    TokenList tl = new TokenList();
    tl = ctx.repo.nsDict.get(token("ns1"));
    assertEquals(tl.size(), 2);
    assertTrue(tl.contains(token("http://www.pkg1.com-12.0")));
    assertTrue(tl.contains(token("http://www.pkg2.com-10.0")));
    tl = ctx.repo.nsDict.get(token("ns2"));
    assertEquals(tl.size(), 1);
    assertTrue(tl.contains(token("http://www.pkg1.com-12.0")));
    tl = ctx.repo.nsDict.get(token("ns3"));
    assertEquals(tl.size(), 1);
    assertTrue(tl.contains(token("http://www.pkg2.com-10.0")));
    // Check package dictionary
    assertEquals(ctx.repo.pkgDict.keys().length, 2);
    assertNotNull(ctx.repo.pkgDict.get(token("http://www.pkg1.com-12.0")));
    assertNotNull(ctx.repo.pkgDict.get(token("http://www.pkg2.com-10.0")));
    assertEquals("pkg1",
        string(ctx.repo.pkgDict.get(token("http://www.pkg1.com-12.0"))));
    assertEquals("pkg2",
        string(ctx.repo.pkgDict.get(token("http://www.pkg2.com-10.0"))));

  }

  /**
   * Tests validation of packages.
   */
  @Test
  public void testPkgCheck() {
    // Missing mandatory attributes
    error(new IOContent(token("<package " +
        "xmlns:http='http://expath.org/ns/pkg' spec='1.0'/>")),
        Err.PKGDESCINV, "Missing mandatory attribute not detected.");

    // Already installed package
    error(desc("http://www.pkg1.com", "pkg1", "12.0", ""),
        Err.PKGINSTALLED, "Installed package not detected.");

    // Package with not installed dependencies - dependency is defined with no
    // specific versions
    error(desc("http://www.pkg5.com", "pkg5", "12.0",
        "<dependency package='http://www.pkg4.com'/>"),
        Err.PKGNOTINSTALLED, "Missing dependency not detected.");

    // Package with not installed dependencies - dependency is defined with
    // version set
    error(desc("http://www.pkg5.com", "pkg5", "12.0",
        "<dependency package='http://www.pkg1.com' versions='1.0 7.0'/>"),
        Err.PKGNOTINSTALLED, "Missing dependency not detected.");

    // Package with not installed dependencies - dependency is defined with
    // version template
    error(desc("http://www.pkg5.com", "pkg5", "12.0",
        "<dependency package='http://www.pkg1.com' versions='12.7'/>"),
        Err.PKGNOTINSTALLED, "Missing dependency not detected.");

    // Package with not installed dependencies - dependency is defined with
    // version template for minimal acceptable version
    error(desc("http://www.pkg5.com", "pkg5", "12.0",
        "<dependency package='http://www.pkg1.com' versions='12.7'/>"),
        Err.PKGNOTINSTALLED, "Missing dependency not detected.");

    // Package with not installed dependencies - dependency is defined with
    // version template for maximal acceptable version
    error(desc("http://www.pkg5.com", "pkg5", "12.0",
        "<dependency package='http://www.pkg1.com' semver-max='11'/>"),
        Err.PKGNOTINSTALLED, "Missing dependency not detected.");

    // Package with not installed dependencies - dependency is defined with
    // version templates for minimal and maximal acceptable version
    error(desc("http://www.pkg5.com", "pkg5", "12.0",
        "<dependency package='http://www.pkg1.com' semver-min='5.7' " +
        "semver-max='11'/>"),
        Err.PKGNOTINSTALLED, "Missing dependency not detected.");

    // Package with component which is already installed as part of another
    // package
    error(desc("http://www.pkg5.com", "pkg5", "12.0",
        "<xquery><namespace>ns1</namespace><file>pkg1mod1.xql</file></xquery>"),
        Err.MODISTALLED, "Already installed component not detected.");

    // Package with component which is already installed as part of another
    // version of the same package
    ok(desc("http://www.pkg1.com", "pkg1", "10.0",
        "<xquery><namespace>ns1</namespace>" +
        "<file>pkg1mod1.xql</file></xquery>"));

    // Valid package
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
      new PkgValidator(ctx).check(new PkgParser(ctx).parse(desc, null), null);
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
      new PkgValidator(ctx).check(new PkgParser(ctx).parse(desc, null), null);
      fail(exp);
    } catch(final QueryException ex) {
      check(ex, err);
    }
  }

  /** Header string. */
  private static final byte[] HEADER = token("<package "
      + "xmlns:http='http://expath.org/ns/pkg' "
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
