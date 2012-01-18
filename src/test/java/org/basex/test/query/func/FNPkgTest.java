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
    final String dirName = normalize("http://www.pkg3.com-10.0");
    assertTrue(dir(dirName));
    assertTrue(file(dirName + "/expath-pkg.xml"));
    assertTrue(dir(dirName + "/pkg3"));
    assertTrue(dir(dirName + "/pkg3/mod"));
    assertTrue(file(dirName + "/pkg3/mod/pkg3mod1.xql"));
    query(_PKG_DELETE.args(dirName));
  }
  
  /**
   * Test method for pkg:delete().
   */
  @Test
  public void delete() {
    check(_PKG_DELETE);
  }
  
  /**
   * Test method for pkg:list().
   */
  @Test
  public void list() {
    check(_PKG_LIST);
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
