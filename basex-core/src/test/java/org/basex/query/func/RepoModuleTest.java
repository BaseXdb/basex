package org.basex.query.func;

import static org.basex.query.func.Function.*;
import static org.junit.Assert.*;

import org.basex.core.*;
import org.basex.io.*;
import org.basex.query.*;
import org.junit.*;

/**
 * This class tests the functions of the Repository Module.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Rositsa Shadura
 */
public class RepoModuleTest extends AdvancedQueryTest {
  /** Test repository. */
  private static final String REPO = "src/test/resources/repo/";
  /** Pkg3 name. */
  private static final String PKG3 = "http://www.pkg3.com";
  /** Pkg4 name. */
  private static final String PKG4 = "http://www.pkg4.com";
  /** Pkg3 directory. */
  private static final String PKG3ID = PKG3 + "-10.0";

  /** Prepares a test. */
  @Before
  public void setupTest() {
    context = new Context();
    context.globalopts.set(GlobalOptions.REPOPATH, REPO);
    new IOFile(REPO, PKG3ID).delete();
  }

  /** Test method. */
  @Test
  public void install() {
    query(_REPO_INSTALL.args(REPO + "pkg3.xar"));
    final String dir = normalize(PKG3ID);
    assertTrue(dir(dir));
    assertTrue(file(dir + "/expath-pkg.xml"));
    assertTrue(dir(dir + "/pkg3"));
    assertTrue(dir(dir + "/pkg3/mod"));
    assertTrue(file(dir + "/pkg3/mod/pkg3mod1.xql"));
    query(_REPO_DELETE.args(PKG3));
  }

  /** Test method. */
  @Test
  public void delete() {
    // install
    query(_REPO_INSTALL.args(REPO + "pkg3.xar"));
    // delete by package name
    final String dir = normalize(PKG3ID);
    assertTrue("Directory not found: " + dir, dir(dir));
    query(_REPO_DELETE.args(PKG3));
    assertFalse("Directory still exists: " + dir, dir(dir));

    // install again
    query(_REPO_INSTALL.args(REPO + "pkg3.xar"));
    // delete by name and version
    query(_REPO_DELETE.args(PKG3ID));
    assertFalse(dir(dir));
  }

  /** Test method. */
  @Test
  public void list() {
    // install pkg3
    query(_REPO_INSTALL.args(REPO + "pkg3.xar"));
    // install pkg4
    query(_REPO_INSTALL.args(REPO + "pkg4.xar"));
    contains(_REPO_LIST.toString(), PKG3);
    contains(_REPO_LIST.toString(), PKG4);
    query(_REPO_DELETE.args(PKG4));
    query(_REPO_DELETE.args(PKG3));
  }

  /**
   * Checks if the specified path points to a file.
   * @param path file path
   * @return result of check
   */
  private static boolean file(final String path) {
    final IOFile file = new IOFile(REPO, path);
    return file.exists() && !file.isDir();
  }

  /**
   * Checks if the specified path points to a directory.
   * @param path file path
   * @return result of check
   */
  private static boolean dir(final String path) {
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
