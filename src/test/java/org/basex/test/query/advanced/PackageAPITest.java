package org.basex.test.query.advanced;

import static org.basex.util.Token.*;
import static org.junit.Assert.*;

import java.io.File;

import org.basex.core.BaseXException;
import org.basex.core.Context;
import org.basex.core.Prop;
import org.basex.core.cmd.InstallPkg;
import org.basex.io.IO;
import org.basex.io.IOContent;
import org.basex.query.QueryException;
import org.basex.query.util.PackageParser;
import org.basex.query.util.PkgValidator;
import org.basex.query.util.Repo;
import org.basex.query.util.RepoManager;
import org.basex.util.TokenList;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * This class tests EXPath package API.
 * @author BaseX Team 2005-11, BSD License
 * @author Rositsa Shadura
 */
public class PackageAPITest {
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

    IO io = null;
    // Missing mandatory attributes
    final byte[] pkg1 = token("<package "
        + "xmlns:http=\"http://expath.org/ns/pkg\" " + "spec='1.0'/>");
    io = new IOContent(pkg1);
    try {
      PkgValidator.check(PackageParser.parse(io, ctx, null), ctx, null);
      fail("Missing mandatory attribute not detected.");
    } catch(QueryException ex) {
      assertTrue(indexOf(token(ex.getMessage()), token("PACK0005")) != -1);
    }

    // Already installed package
    final byte[] pkg2 = token("<package "
        + "xmlns:http=\"http://expath.org/ns/pkg\" "
        + "spec='1.0' name='http://www.pkg1.com' "
        + "abbrev='pkg1' version='12.0'/>");
    io = new IOContent(pkg2);
    try {
      PkgValidator.check(PackageParser.parse(io, ctx, null), ctx, null);
      fail("Installed package not detected.");
    } catch(QueryException ex) {
      assertTrue(indexOf(token(ex.getMessage()), token("PACK0003")) != -1);
    }

    // Package with not installed dependencies - dependency is defined with no
    // specific versions
    final byte[] pkg3 = token("<package "
        + "xmlns:http=\"http://expath.org/ns/pkg\" "
        + "spec='1.0' name='http://www.pkg5.com' "
        + "abbrev='pkg5' version='12.0'>"
        + "<dependency package='http://www.pkg4.com'/></package>");
    io = new IOContent(pkg3);
    try {
      PkgValidator.check(PackageParser.parse(io, ctx, null), ctx, null);
      fail("Missing dependency not detected.");
    } catch(QueryException ex) {
      assertTrue(indexOf(token(ex.getMessage()), token("PACK0004")) != -1);
    }

    // Package with not installed dependencies - dependency is defined with
    // version set
    final byte[] pkg4 = token("<package "
        + "xmlns:http=\"http://expath.org/ns/pkg\" "
        + "spec='1.0' name='http://www.pkg5.com' "
        + "abbrev='pkg5' version='12.0'>"
        + "<dependency package='http://www.pkg1.com' versions='1.0 7.0'/>"
        + "</package>");
    io = new IOContent(pkg4);
    try {
      PkgValidator.check(PackageParser.parse(io, ctx, null), ctx, null);
      fail("Missing dependency not detected.");
    } catch(QueryException ex) {
      assertTrue(indexOf(token(ex.getMessage()), token("PACK0004")) != -1);
    }

    // Package with not installed dependencies - dependency is defined with
    // version template
    final byte[] pkg5 = token("<package "
        + "xmlns:http=\"http://expath.org/ns/pkg\" "
        + "spec='1.0' name='http://www.pkg5.com' "
        + "abbrev='pkg5' version='12.0'>"
        + "<dependency package='http://www.pkg1.com' semver='12.7'/>"
        + "</package>");
    io = new IOContent(pkg5);
    try {
      PkgValidator.check(PackageParser.parse(io, ctx, null), ctx, null);
      fail("Missing dependency not detected.");
    } catch(QueryException ex) {
      assertTrue(indexOf(token(ex.getMessage()), token("PACK0004")) != -1);
    }

    // Package with not installed dependencies - dependency is defined with
    // version template for minimal acceptable version
    final byte[] pkg6 = token("<package "
        + "xmlns:http=\"http://expath.org/ns/pkg\" "
        + "spec='1.0' name='http://www.pkg5.com' "
        + "abbrev='pkg5' version='12.0'>"
        + "<dependency package='http://www.pkg1.com' semver-min='12.7'/>"
        + "</package>");
    io = new IOContent(pkg6);
    try {
      PkgValidator.check(PackageParser.parse(io, ctx, null), ctx, null);
      fail("Missing dependency not detected.");
    } catch(QueryException ex) {
      assertTrue(indexOf(token(ex.getMessage()), token("PACK0004")) != -1);
    }

    // Package with not installed dependencies - dependency is defined with
    // version template for maximal acceptable version
    final byte[] pkg7 = token("<package "
        + "xmlns:http=\"http://expath.org/ns/pkg\" "
        + "spec='1.0' name='http://www.pkg5.com' "
        + "abbrev='pkg5' version='12.0'>"
        + "<dependency package='http://www.pkg1.com' semver-max='11'/>"
        + "</package>");
    io = new IOContent(pkg7);
    try {
      PkgValidator.check(PackageParser.parse(io, ctx, null), ctx, null);
      fail("Missing dependency not detected.");
    } catch(QueryException ex) {
      assertTrue(indexOf(token(ex.getMessage()), token("PACK0004")) != -1);
    }

    // Package with not installed dependencies - dependency is defined with
    // version templates for minimal and maximal acceptable version
    final byte[] pkg8 = token("<package "
        + "xmlns:http=\"http://expath.org/ns/pkg\" "
        + "spec='1.0' name='http://www.pkg5.com' "
        + "abbrev='pkg5' version='12.0'>"
        + "<dependency package='http://www.pkg1.com' semver-min='5.7' "
        + "semver-max='11'/>" + "</package>");
    io = new IOContent(pkg8);
    try {
      PkgValidator.check(PackageParser.parse(io, ctx, null), ctx, null);
      fail("Missing dependency not detected.");
    } catch(QueryException ex) {
      assertTrue(indexOf(token(ex.getMessage()), token("PACK0004")) != -1);
    }

    // Package with component which is already installed as part of another
    // package
    final byte[] pkg9 = token("<package "
        + "xmlns:http=\"http://expath.org/ns/pkg\" "
        + "spec='1.0' name='http://www.pkg5.com' "
        + "abbrev='pkg5' version='12.0'>"
        + "<xquery><namespace>ns1</namespace><file>pkg1mod1.xql</file>"
        + "</xquery></package>");
    io = new IOContent(pkg9);
    try {
      PkgValidator.check(PackageParser.parse(io, ctx, null), ctx, null);
      fail("Already installed component not detected.");
    } catch(QueryException ex) {
      assertTrue(indexOf(token(ex.getMessage()), token("PACK0007")) != -1);
    }

    // Package with component which is already installed as part of another
    // version of the same package
    final byte[] pkg10 = token("<package "
        + "xmlns:http=\"http://expath.org/ns/pkg\" "
        + "spec='1.0' name='http://www.pkg1.com' "
        + "abbrev='pkg1' version='10.0'>"
        + "<xquery><namespace>ns1</namespace><file>pkg1mod1.xql</file>"
        + "</xquery></package>");
    io = new IOContent(pkg10);
    try {
      PkgValidator.check(PackageParser.parse(io, ctx, null), ctx, null);
    } catch(QueryException ex) {
      fail("Unexpected exception thrown");
    }

    // Valid package
    final byte[] pkg11 = token("<package "
        + "xmlns:http=\"http://expath.org/ns/pkg\" "
        + "spec='1.0' name='http://www.pkg1.com' "
        + "abbrev='pkg1' version='10.0'>"
        + "<dependency package='http://www.pkg1.com' semver-min='11'/>"
        + "<xquery><namespace>ns3</namespace><file>pkg5mod1.xql</file>"
        + "</xquery></package>");
    io = new IOContent(pkg11);
    try {
      PkgValidator.check(PackageParser.parse(io, ctx, null), ctx, null);
    } catch(QueryException ex) {
      fail("Unexpected exception thrown");
    }
  }

  /**
   * Tests package installation.
   * @throws BaseXException basex exception
   */
  @Test
  public void testRepoInstall() throws BaseXException {
    // Try to install non-existing package
    try {
      RepoManager.installPackage("etc/pkg", ctx, null);
      fail("Not existing package not detected.");
    } catch(QueryException ex) {
      assertTrue(indexOf(token(ex.getMessage()), token("PACK0001")) != -1);
    }
    // Try to install a package
    new InstallPkg("etc/repo/pkg3.xar", ctx, null).execute(ctx);
    File pkgDir = new File("etc/repo/pkg3");
    assertTrue(pkgDir.exists());
    assertTrue(pkgDir.isDirectory());
    File pkgDesc = new File("etc/repo/pkg3/expath-pkg.xml");
    assertTrue(pkgDesc.exists());
    File modDir = new File("etc/repo/pkg3/pkg3");
    assertTrue(modDir.exists());
    assertTrue(modDir.isDirectory());
    File modFile = new File("etc/repo/pkg3/pkg3/pkg3mod1.xql");
    assertTrue(modFile.exists());

    modFile.delete();
    modDir.delete();
    pkgDesc.delete();
    pkgDir.delete();
  }
}
