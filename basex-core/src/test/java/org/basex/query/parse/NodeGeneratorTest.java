package org.basex.query.parse;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.charset.*;
import java.nio.file.*;
import java.util.*;

import org.junit.jupiter.api.*;

/**
 * Tests for the generated records of the syntax tree.
 *
 * @author BaseX Team, BSD License
 * @author Gunther Rademacher
 */
public final class NodeGeneratorTest {
  /**
   * Checks that the records and the factory were generated from the current grammar.
   * @throws Exception exception
   */
  @Test public void upToDate() throws Exception {
    for(final Map.Entry<Path, String> entry : NodeGenerator.generate().entrySet()) {
      final String checkedIn = Files.readString(entry.getKey(), StandardCharsets.UTF_8).
          replace("\r", "");
      assertEquals(entry.getValue(), checkedIn, entry.getKey().getFileName() + " is outdated. " +
          "After changing the grammar or the fold policy, follow the steps of GrammarUpdate.");
    }
  }
}
