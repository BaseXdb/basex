package org.basex.test.query.func;

import static org.basex.query.func.Function.*;
import static org.junit.Assert.*;

import java.io.File;

import org.basex.test.query.AdvancedQueryTest;
import org.junit.Before;
import org.junit.Test;

/**
 * This class tests the functions for EXPath package management.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Rositsa Shadura
 */
public class FNPkgTest extends AdvancedQueryTest {
  /** Test repository. */
  private static final String REPO = "src/test/resources/repo/";
  /** Pkg3 directory. */
  private static final String PKGDIR = normalize("http://www.pkg3.com-10.0");
  /** Pkg3 name. */
  private static final String PKG3NAME = "http://www.pkg3.com";
  /** Pkg4 name. */
  private static final String PKG4NAME = "http://www.pkg4.com";

  /**
   * Prepare test.
   */
  @Before
  public void setupBeforeClass() {
    CONTEXT.repo.init(REPO);
  }

  /**
   * Test method for pkg:install().
   */
  @Test
  public void install() {
    check(_PKG_INSTALL);
    query(_PKG_INSTALL.args(REPO + "pkg3.xar"));
    assertTrue(dir(PKGDIR));
    assertTrue(file(PKGDIR + "/expath-pkg.xml"));
    assertTrue(dir(PKGDIR + "/pkg3"));
    assertTrue(dir(PKGDIR + "/pkg3/mod"));
    assertTrue(file(PKGDIR + "/pkg3/mod/pkg3mod1.xql"));
    query(_PKG_DELETE.args(PKGDIR));
  }

  /**
   * Test method for pkg:delete().
   */
  @Test
  public void delete() {
    check(_PKG_DELETE);
    // Install
    query(_PKG_INSTALL.args(REPO + "pkg3.xar"));
    // Delete by directory name
    query(_PKG_DELETE.args(PKGDIR));
    assertTrue(!dir(PKGDIR));
    // Install again
    query(_PKG_INSTALL.args(REPO + "pkg3.xar"));
    // Delete by package name
    query(_PKG_DELETE.args("http://www.pkg3.com"));
    assertTrue(!dir(PKGDIR));
  }

  /**
   * Test method for pkg:list().
   */
  @Test
  public void list() {
    check(_PKG_LIST);
    // Install pkg3
    query(_PKG_INSTALL.args(REPO + "pkg3.xar"));
    // Install pkg4
    query(_PKG_INSTALL.args(REPO + "pkg4.xar"));
    contains(_PKG_LIST.toString(), PKG3NAME);
    contains(_PKG_LIST.toString(), PKG4NAME);
    query(_PKG_DELETE.args(PKG4NAME));
    query(_PKG_DELETE.args(PKG3NAME));
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
