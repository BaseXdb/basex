package org.basex.query.func;

import static org.basex.query.func.Function.*;
import static org.junit.jupiter.api.Assertions.*;

import org.basex.*;
import org.basex.core.*;
import org.basex.io.*;
import org.junit.jupiter.api.*;

/**
 * This class tests the functions of the Repository Module.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Rositsa Shadura
 */
public final class RepoModuleTest extends SandboxTest {
  /** Test repository. */
  private static final String REPO = "src/test/resources/repo/";
  /** Pkg3 name. */
  private static final String PKG3 = "http://www.pkg3.com";
  /** Pkg4 name. */
  private static final String PKG4 = "http://www.pkg4.com";
  /** Pkg3 directory. */
  private static final String PKG3ID = PKG3 + "-10.0";

  /** Prepares a test. */
  @BeforeEach public void setupTest() {
    context.soptions.set(StaticOptions.REPOPATH, REPO);
    new IOFile(REPO, normalize(PKG3ID)).delete();
  }

  /** Test method. */
  @Test public void delete() {
    final Function func = _REPO_DELETE;
    // install
    query(_REPO_INSTALL.args(REPO + "pkg3.xar"));
    // delete by package name
    final String dir = normalize(PKG3ID);
    assertTrue(dir(dir), "Directory not found: " + dir);
    query(func.args(PKG3));
    assertFalse(dir(dir), "Directory still exists: " + dir);

    // install again
    query(_REPO_INSTALL.args(REPO + "pkg3.xar"));
    // delete by name and version
    query(func.args(PKG3ID));
    assertFalse(dir(dir));
  }

  /** Test method. */
  @Test public void install() {
    final Function func = _REPO_INSTALL;
    // queries
    query(func.args(REPO + "pkg3.xar"));
    final String dir = normalize(PKG3ID);
    assertTrue(dir(dir));
    assertTrue(file(dir + "/expath-pkg.xml"));
    assertTrue(dir(dir + "/pkg3"));
    assertTrue(dir(dir + "/pkg3/mod"));
    assertTrue(file(dir + "/pkg3/mod/pkg3mod1.xql"));
    query(_REPO_DELETE.args(PKG3));
  }

  /** Test method. */
  @Test public void list() {
    final Function func = _REPO_LIST;
    // install pkg3
    query(_REPO_INSTALL.args(REPO + "pkg3.xar"));
    // install pkg4
    query(_REPO_INSTALL.args(REPO + "pkg4.xar"));
    contains(func.toString(), PKG3);
    contains(func.toString(), PKG4);
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
