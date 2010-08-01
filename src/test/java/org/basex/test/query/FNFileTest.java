package org.basex.test.query;

import org.basex.core.Context;
import org.basex.core.Main;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.expr.Expr;
import org.basex.query.func.Fun;
import org.basex.query.func.FunDef;
import org.basex.query.item.Item;
import org.basex.query.item.Str;
import org.basex.query.iter.Iter;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * This class tests the functions of the file library.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Rositsa Shadura
 */
public final class FNFileTest {
  /** Database context. */
  protected static QueryContext qc;

  /** Prepares tests. */
  @BeforeClass
  public static void startTest() {
    qc = new QueryContext(new Context());
  }

  /** Test function file:mkdir. */
  @Test
  public void testMkDir() {
    atomic(FunDef.MKDIR, Str.get("test"));
  }

  /** Test function file:mkdirs. */
  @Test
  public void testMkDirs() {
    atomic(FunDef.MKDIRS, Str.get("test/test1/test2/test3"));
  }

  /** Test function file:is-directory. */
  @Test
  public void testIsDir() {
    atomic(FunDef.ISDIR, Str.get("test/test1/test2"));
  }

  /** Test function file:is-file. */
  @Test
  public void testIsFile() {
    atomic(FunDef.ISFILE, Str.get("test2"));
  }

  /** Test function file:is-read. */
  @Test
  public void testIsRead() {
    atomic(FunDef.ISREAD, Str.get("test"));
  }

  /** Test function file:is-write. */
  @Test
  public void testIsWrite() {
    atomic(FunDef.ISWRITE, Str.get("test"));
  }

  /** Test function file:path-separator. */
  @Test
  public void testPathSeparator() {
    atomic(FunDef.PATHSEP);
  }

  /** Test function file:delete. */
  @Test
  public void testDelete() {
    atomic(FunDef.DELETE, Str.get("test/test1/test2/test3"));
    atomic(FunDef.DELETE, Str.get("test/test1/test2"));
    atomic(FunDef.DELETE, Str.get("test/test1"));
    atomic(FunDef.DELETE, Str.get("test"));
  }

  /** Test function file:path-to-full-path. */
  @Test
  public void testPathToFull() {
    atomic(FunDef.PATHTOFULL, Str.get("test"));
  }

  /** Tests the function file:files. */
  @Test
  public void testFiles() {
    Expr[] args = new Expr[2];
    args[0] = Str.get("etc");
    args[1] = Str.get("[^z]*e");
    iter(FunDef.FILES, args);
  }

  /**
   * Runs an atomic function call.
   * @param def function definition
   * @param args function arguments
   * @return item
   */
  private Item atomic(final FunDef def, final Expr... args) {
    try {
      // [CG] XQuery/Query Info
      return Fun.create(null, def, args).atomic(qc);
    } catch(final QueryException ex) {
      Main.notexpected(ex);
      return null;
    }
  }

  /**
   * Runs a function.
   * @param def function definition
   * @param args function arguments
   * @return iterator
   */
  private Iter iter(final FunDef def, final Expr... args) {
    try {
      // [CG] XQuery/Query Info
      return Fun.create(null, def, args).iter(qc);
    } catch(final QueryException ex) {
      Main.notexpected(ex);
      return null;
    }
  }
}
