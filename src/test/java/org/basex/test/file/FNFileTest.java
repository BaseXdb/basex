package org.basex.test.file;

import org.basex.query.QueryException;
import org.basex.query.expr.Expr;
import org.basex.query.func.FNFile;
import org.basex.query.func.FunDef;
import org.basex.query.item.Str;
import org.junit.Test;

/**
 * This class tests the functions of the file library
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * 
 */
public class FNFileTest {

  /** Test function file:mkdir. */
  @Test
  public void testMkDir() {

    FNFile fnFile = new FNFile();
    Expr[] expr = new Expr[1];
    expr[0] = Str.get("test");
    fnFile.init(FunDef.MKDIR, expr);

    try {
      fnFile.atomic(null);
    } catch(QueryException e) {
      throw new RuntimeException(e);
    }
  }

  /** Test function file:mkdirs. */
  @Test
  public void testMkDirs() {

    FNFile fnFile = new FNFile();
    Expr[] expr = new Expr[1];
    expr[0] = Str.get("test/test1/test2/test3");
    fnFile.init(FunDef.MKDIRS, expr);
    try {
      fnFile.atomic(null);
    } catch(QueryException e) {
      throw new RuntimeException(e);
    }
  }

  /** Test function file:is-directory. */
  @Test
  public void testIsDir() {

    FNFile fnFile = new FNFile();
    Expr[] expr = new Expr[1];
    expr[0] = Str.get("test/test1/test2");
    fnFile.init(FunDef.ISDIR, expr);
    try {
      fnFile.atomic(null);
    } catch(QueryException e) {
      throw new RuntimeException(e);
    }

  }

  /** Test function file:is-file. */
  @Test
  public void testIsFile() {

    FNFile fnFile = new FNFile();
    Expr[] expr = new Expr[1];
    expr[0] = Str.get("test2");
    fnFile.init(FunDef.ISFILE, expr);
    try {
      fnFile.atomic(null);
    } catch(QueryException e) {
      throw new RuntimeException(e);
    }

  }

  /** Test function file:is-read. */
  @Test
  public void testIsRead() {

    FNFile fnFile = new FNFile();
    Expr[] expr = new Expr[1];
    expr[0] = Str.get("test");
    fnFile.init(FunDef.ISREAD, expr);
    try {
      fnFile.atomic(null);
    } catch(QueryException e) {
      throw new RuntimeException(e);
    }

  }

  /** Test function file:is-write. */
  @Test
  public void testIsWrite() {

    FNFile fnFile = new FNFile();
    Expr[] expr = new Expr[1];
    expr[0] = Str.get("test");
    fnFile.init(FunDef.ISWRITE, expr);
    try {
      fnFile.atomic(null);
    } catch(QueryException e) {
      throw new RuntimeException(e);
    }

  }

  /** Test function file:path-separator. */
  @Test
  public void testPathSeparator() {

    FNFile fnFile = new FNFile();
    Expr[] expr = new Expr[0];
    fnFile.init(FunDef.PATHSEP, expr);
    try {
      fnFile.atomic(null);
    } catch(QueryException e) {
      throw new RuntimeException(e);
    }

  }

  /** Test function file:delete. */
  @Test
  public void testDelete() {

    FNFile fnFile = new FNFile();
    Expr[] expr = new Expr[1];
    expr[0] = Str.get("test2");
    fnFile.init(FunDef.DELETE, expr);
    try {
      fnFile.atomic(null);
    } catch(QueryException e) {
      throw new RuntimeException(e);
    }

  }

  /** Test function file:path-to-full-path. */
  @Test
  public void testPathToFull() {

    FNFile fnFile = new FNFile();
    Expr[] expr = new Expr[1];
    expr[0] = Str.get("test");
    fnFile.init(FunDef.PATHTOFULL, expr);
    try {
      fnFile.atomic(null);
    } catch(QueryException e) {
      throw new RuntimeException(e);
    }

  }

  /** Test function file:files */
  @Test
  public void testFiles() {

    FNFile fnFile = new FNFile();
    Expr[] expr = new Expr[1];
    expr[0] = Str.get("/home");
    fnFile.init(FunDef.FILES, expr);
    try {
      fnFile.iter(null);
    } catch(QueryException e) {
      throw new RuntimeException(e);
    }

  }
}
