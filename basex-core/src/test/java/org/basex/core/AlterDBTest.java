package org.basex.core;

import static org.basex.query.QueryError.*;
import static org.basex.query.func.Function.*;
import static org.junit.jupiter.api.Assertions.*;

import java.io.*;

import org.basex.*;
import org.basex.api.client.*;
import org.basex.core.cmd.*;
import org.basex.data.*;
import org.basex.io.*;
import org.basex.util.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.Test;

/**
 * Tests for renaming a database to the name of an existing database.
 *
 * @author BaseX Team, BSD License
 * @author WingedGuardian
 */
public final class AlterDBTest extends SandboxTest {
  /** Name of the target database. */
  private static final String NAME2 = NAME + '2';

  /** Creates the source and the target database. */
  @BeforeEach public void init() {
    execute(new CreateDB(NAME, "<a/>"));
    execute(new CreateDB(NAME2, "<b/>"));
    execute(new Close());
  }

  /** Drops the test databases. */
  @AfterEach public void finish() {
    execute(new DropDB(NAME));
    execute(new DropDB(NAME2));
  }

  /** An existing target database is replaced, and no temporary files are left behind. */
  @Test public void replace() {
    execute(new AlterDB(NAME, NAME2));
    assertFalse(context.soptions.dbExists(NAME));
    query(_DB_GET.args(NAME2) + "/*/name()", "a");
    for(final IOFile file : context.soptions.dbPath().children()) {
      assertFalse(file.name().startsWith('.' + NAME2), file.name());
    }
  }

  /**
   * A target database that is opened by another client is not replaced.
   * @throws IOException I/O exception
   */
  @Test public void targetOpened() throws IOException {
    try(LocalSession session = new LocalSession(context)) {
      session.execute(new Open(NAME2));
      final BaseXException ex = assertThrows(BaseXException.class,
          () -> new AlterDB(NAME, NAME2).execute(context));
      assertTrue(ex.getMessage().contains(NAME2), ex.getMessage());
      error(_DB_ALTER.args(NAME, NAME2), DB_LOCK1_X);
    }
    query(_DB_GET.args(NAME) + "/*/name()", "a");
    query(_DB_GET.args(NAME2) + "/*/name()", "b");
  }

  /**
   * A target database with a file that is opened by another process is left intact.
   * Windows only: on other systems, open files do not prevent renaming or deleting a directory.
   * @throws IOException I/O exception
   */
  @Test public void targetFileOpened() throws IOException {
    Assumptions.assumeTrue(Prop.WIN, "Windows only");
    final IOFile file = new IOFile(context.soptions.dbPath(NAME2),
        DataText.DATAINF + IO.BASEXSUFFIX);
    try(RandomAccessFile raf = new RandomAccessFile(file.file(), "r")) {
      assertThrows(BaseXException.class, () -> new AlterDB(NAME, NAME2).execute(context));
      error(_DB_ALTER.args(NAME, NAME2), UPDBERROR_X_X);
    }
    query(_DB_GET.args(NAME) + "/*/name()", "a");
    query(_DB_GET.args(NAME2) + "/*/name()", "b");
    // the operation succeeds after the file has been closed
    execute(new AlterDB(NAME, NAME2));
    query(_DB_GET.args(NAME2) + "/*/name()", "a");
  }

  /**
   * A target database is restored if the source database cannot be renamed.
   * Windows only: on other systems, open files do not prevent renaming a directory.
   * @throws IOException I/O exception
   */
  @Test public void sourceFileOpened() throws IOException {
    Assumptions.assumeTrue(Prop.WIN, "Windows only");
    final IOFile file = new IOFile(context.soptions.dbPath(NAME),
        DataText.DATAINF + IO.BASEXSUFFIX);
    try(RandomAccessFile raf = new RandomAccessFile(file.file(), "r")) {
      assertThrows(BaseXException.class, () -> new AlterDB(NAME, NAME2).execute(context));
    }
    query(_DB_GET.args(NAME) + "/*/name()", "a");
    query(_DB_GET.args(NAME2) + "/*/name()", "b");
  }
}
