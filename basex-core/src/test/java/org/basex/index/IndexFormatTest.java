package org.basex.index;

import static org.junit.jupiter.api.Assertions.*;

import java.security.*;

import org.basex.*;
import org.basex.core.*;
import org.basex.core.cmd.*;
import org.basex.io.*;
import org.basex.util.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.Test;

/**
 * Guards the on-disk format of the index structures by checksums of the index files.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class IndexFormatTest extends SandboxTest {
  /** Index files and the checksums of their contents for the test document. */
  private static final String[][] CHECKSUMS = {
    { "txtl", "225d74c7c73a295ce8d0f93eeb7b0fbb" },
    { "txtr", "4d2f63eba254941a5a7c8c1ec24bedd7" },
    { "atvl", "c0d07ecbe7a16a4742b2020430e07fb1" },
    { "atvr", "73ef5ab8d9235efb5ce2ba6ea63a5081" },
    { "tokl", "b85c6290856dc961ed5ec743ff667acb" },
    { "tokr", "df991f03cd2e2f4e486a49abd6a08666" },
    { "ftxx", "65eac60d76e951a7b7f68b718bd0272a" },
    { "ftxy", "7973f4993b8dce92d951de67192c7d36" },
    { "ftxz", "4c3356c9b6ed09d9ee43de462251c04a" },
  };

  /** Creates the database with all value indexes. */
  @BeforeAll public static void start() {
    set(MainOptions.TOKENINDEX, true);
    set(MainOptions.FTINDEX, true);
    execute(new CreateDB(NAME, "src/test/resources/input.xml"));
    execute(new Close());
  }

  /** Drops the database. */
  @AfterAll public static void stop() {
    execute(new DropDB(NAME));
    set(MainOptions.TOKENINDEX, false);
    set(MainOptions.FTINDEX, false);
  }

  /**
   * Compares the index files with the expected checksums.
   * @throws Exception exception
   */
  @Test public void checksums() throws Exception {
    final IOFile path = context.soptions.dbPath(NAME);
    final StringBuilder actual = new StringBuilder();
    for(final String[] checksum : CHECKSUMS) {
      final String file = checksum[0];
      final byte[] digest = MessageDigest.getInstance("MD5").digest(
        new IOFile(path, file + IO.BASEXSUFFIX).read());
      actual.append(file).append(' ').append(Token.string(Token.hex(digest, false))).append('\n');
    }
    final StringBuilder expected = new StringBuilder();
    for(final String[] checksum : CHECKSUMS) {
      expected.append(checksum[0]).append(' ').append(checksum[1]).append('\n');
    }
    assertEquals(expected.toString(), actual.toString());
  }
}
