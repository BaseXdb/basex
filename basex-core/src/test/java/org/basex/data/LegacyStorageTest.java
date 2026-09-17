package org.basex.data;

import static org.basex.query.func.Function.*;
import static org.junit.jupiter.api.Assertions.*;

import java.io.*;
import java.util.*;
import java.util.regex.*;
import java.util.stream.*;

import org.basex.*;
import org.basex.core.*;
import org.basex.core.cmd.*;
import org.basex.core.parse.Commands.*;
import org.basex.io.*;
import org.basex.io.in.DataInput;
import org.basex.util.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.*;
import org.junit.jupiter.params.provider.*;

/**
 * Tests the compatibility of the storage format with version 12.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class LegacyStorageTest extends SandboxTest {
  /** Database files that version 12 knows. */
  private static final Pattern FILES =
      Pattern.compile("(inf|tbli?|txt[lr]?|atv[lr]?|tok[lr]|ftx[xyz]|swl|pth|idp)\\.basex");
  /** Meta keys of segmented indexes. */
  private static final String[] SEGMENT_KEYS = { DataText.DBFTXSEGS, DataText.DBFTXBUF };
  /** Test document. */
  private static final String DOC =
      "<x><a>first entry</a><b id='b'>second entry</b><c>third one</c></x>";
  /** Query for the node whose ID is checked. */
  private static final String C = " " + _DB_GET.args(NAME) + "//c";

  /** Enabled indexes. */
  enum Indexes {
    /** Text, attribute, token and full-text index. */
    ALL,
    /** Text, attribute and token index. */
    VALUES,
    /** No index. */
    NONE
  }

  /** Finalizes a test. */
  @AfterEach public void after() {
    execute(new DropDB(NAME));
    set(MainOptions.UPDINDEX, false);
    set(MainOptions.TEXTINDEX, true);
    set(MainOptions.ATTRINDEX, true);
    set(MainOptions.TOKENINDEX, false);
    set(MainOptions.FTINDEX, false);
    set(MainOptions.FTMIXED, false);
    set(MainOptions.FTINCLUDE, "");
  }

  /**
   * Returns all combinations of the incremental index update flag and the enabled indexes.
   * @return arguments
   */
  static Stream<Arguments> cases() {
    return Stream.of(false, true).flatMap(updindex ->
      Arrays.stream(Indexes.values()).map(indexes -> Arguments.of(updindex, indexes)));
  }

  /**
   * Creates a database with input.
   * @param updindex incremental index update flag
   * @param indexes enabled indexes
   * @throws IOException I/O exception
   */
  @ParameterizedTest
  @MethodSource("cases")
  public void create(final boolean updindex, final Indexes indexes) throws IOException {
    create(updindex, indexes, DOC);
    storage(true);
  }

  /**
   * Creates a database without input.
   * @param updindex incremental index update flag
   * @param indexes enabled indexes
   * @throws IOException I/O exception
   */
  @ParameterizedTest
  @MethodSource("cases")
  public void createEmpty(final boolean updindex, final Indexes indexes) throws IOException {
    create(updindex, indexes, null);
    storage(true);
  }

  /**
   * Replaces a text without shifting PRE values.
   * @param updindex incremental index update flag
   * @param indexes enabled indexes
   * @throws IOException I/O exception
   */
  @ParameterizedTest
  @MethodSource("cases")
  public void replace(final boolean updindex, final Indexes indexes) throws IOException {
    create(updindex, indexes, DOC);
    query("replace value of node " + _DB_GET.args(NAME) + "//a with 'new entry'");
    storage(true);
  }

  /**
   * Adds a document to a database below the segment threshold of the full-text index.
   * @param updindex incremental index update flag
   * @param indexes enabled indexes
   * @throws IOException I/O exception
   */
  @ParameterizedTest
  @MethodSource("cases")
  public void add(final boolean updindex, final Indexes indexes) throws IOException {
    create(updindex, indexes, DOC);
    query(_DB_ADD.args(NAME, " <y>fourth entry</y>", "y.xml"));
    storage(true);
  }

  /**
   * Optimizes a database after updates: node IDs are kept.
   * @param updindex incremental index update flag
   * @param indexes enabled indexes
   * @throws IOException I/O exception
   */
  @ParameterizedTest
  @MethodSource("cases")
  public void optimize(final boolean updindex, final Indexes indexes) throws IOException {
    final String id = update(updindex, indexes);
    query(_DB_OPTIMIZE.args(NAME));
    storage(!updindex || indexes != Indexes.ALL);
    query(_DB_NODE_ID.args(C), id);
    query(_DB_GET_ID.args(NAME, " " + id) + " ! string()", "third one");
  }

  /**
   * Optimizes all structures of a database after updates.
   * @param updindex incremental index update flag
   * @param indexes enabled indexes
   * @throws IOException I/O exception
   */
  @ParameterizedTest
  @MethodSource("cases")
  public void optimizeAll(final boolean updindex, final Indexes indexes) throws IOException {
    update(updindex, indexes);
    query(_DB_OPTIMIZE.args(NAME, true));
    storage(true);
    query(C + " ! string()", "third one");
  }

  /**
   * Drops the full-text index and then all other indexes after updates: the format is kept.
   * @param updindex incremental index update flag
   * @param indexes enabled indexes
   * @throws IOException I/O exception
   */
  @ParameterizedTest
  @MethodSource("cases")
  public void dropIndexes(final boolean updindex, final Indexes indexes) throws IOException {
    update(updindex, indexes);
    final boolean legacy = !updindex || indexes != Indexes.ALL;
    storage(legacy);
    drop(CmdIndex.FULLTEXT);
    storage(legacy);
    drop(CmdIndex.TEXT, CmdIndex.ATTRIBUTE, CmdIndex.TOKEN);
    storage(legacy);
  }

  /**
   * Accepted exception: a mixed-content full-text index.
   * @param updindex incremental index update flag
   * @throws IOException I/O exception
   */
  @ParameterizedTest
  @ValueSource(booleans = { false, true })
  public void mixed(final boolean updindex) throws IOException {
    set(MainOptions.FTMIXED, true);
    set(MainOptions.FTINCLUDE, "a");
    create(updindex, Indexes.ALL, DOC);
    storage(false);
  }

  /**
   * Accepted exception: a namespace structure that is too large for the old format.
   * @param updindex incremental index update flag
   * @param indexes enabled indexes
   * @throws IOException I/O exception
   */
  @ParameterizedTest
  @MethodSource("cases")
  public void namespaces(final boolean updindex, final Indexes indexes) throws IOException {
    create(updindex, indexes, "<a>" + "<b xmlns:p='U'>x</b>".repeat(4200) + "</a>");
    storage(false);
    query("delete node " + _DB_GET.args(NAME) + "/a/b[position() > 10]");
    storage(!updindex || indexes != Indexes.ALL);
  }

  /**
   * Opens a database with a full-text index that was created with version 12 and updates it.
   * @param updindex incremental index update flag
   * @throws IOException I/O exception
   */
  @ParameterizedTest
  @ValueSource(booleans = { false, true })
  public void oldVersionFullText(final boolean updindex) throws IOException {
    copy("ftv12" + (updindex ? "upd" : ""));
    final String ft = _FT_SEARCH.args(NAME, "entry") + " ! string()";
    query(_DB_INFO.args(NAME) + "//updindex/text()", updindex);
    query(_DB_INFO.args(NAME) + "//ftindex/text()", true);
    query(ft, "first entry\nsecond entry");
    query(_DB_TEXT.args(NAME, "third one"), "third one");

    // the update invalidates the indexes, unless they are updatable: the full-text index of the
    // small database is then rebuilt in the layout of the old version
    query("replace value of node " + _DB_GET.args(NAME) + "//b with 'new entry'");
    query(_DB_INFO.args(NAME) + "//ftindex/text()", updindex);
    query(_DB_INFO.args(NAME) + "//textindex/text()", updindex);
    if(updindex) {
      query(ft, "first entry\nnew entry");
      query(_DB_TEXT.args(NAME, "new entry"), "new entry");
      final IOFile db = context.soptions.dbPath(NAME);
      assertFalse(new IOFile(db, "ftx0x.basex").exists());
      assertTrue(new IOFile(db, "ftxx.basex").exists());
    }
    query(_DB_OPTIMIZE.args(NAME));
    query(_DB_INFO.args(NAME) + "//ftindex/text()", true);
    query(ft, "first entry\nnew entry");
    query(_DB_TEXT.args(NAME, "new entry"), "new entry");
  }

  /**
   * Opens a database with namespaces that was created with version 12 and updates it.
   * @throws IOException I/O exception
   */
  @Test public void oldVersionNamespaces() throws IOException {
    copy("nsv12");
    final String doc = "<a:root xmlns:a=\"urn:a\" xmlns:b=\"urn:b\">" +
      "<a:x><c:one xmlns:c=\"urn:c\">1</c:one></a:x>" +
      "<a:y><c:two xmlns:c=\"urn:c\">2</c:two></a:y>" +
      "<d:deep xmlns:d=\"urn:d\"><e:in xmlns:e=\"urn:e\"><f:low xmlns:f=\"urn:f\">low</f:low>" +
      "</e:in></d:deep><g:def xmlns:g=\"urn:g\" xmlns=\"urn:default\"><plain>text</plain></g:def>" +
      "</a:root>";
    query(_DB_GET.args(NAME) + " => serialize()", doc);
    query("namespace-uri-for-prefix('f', " + _DB_GET.args(NAME) + "//*:low)", "urn:f");
    query(_DB_GET.args(NAME) + "//*:plain/namespace-uri()", "urn:default");

    // update the database: the structure is rewritten in the current format
    query("insert node <h:new xmlns:h='urn:h'/> into " + _DB_GET.args(NAME) + "/*");
    execute(new Close());
    query("namespace-uri-for-prefix('h', " + _DB_GET.args(NAME) + "//*:new)", "urn:h");
    query("namespace-uri-for-prefix('f', " + _DB_GET.args(NAME) + "//*:low)", "urn:f");
  }

  /**
   * Copies the files of a database created with version 12 to the sandbox.
   * @param name name of the directory in the test resources
   * @throws IOException I/O exception
   */
  private static void copy(final String name) throws IOException {
    final IOFile trg = context.soptions.dbPath(NAME);
    for(final IOFile file : new IOFile("src/test/resources/" + name).children()) {
      file.copyTo(new IOFile(trg, file.name()));
    }
  }

  /**
   * Creates the test database and closes it.
   * @param updindex incremental index update flag
   * @param indexes enabled indexes
   * @param input input (can be {@code null})
   */
  private static void create(final boolean updindex, final Indexes indexes, final String input) {
    set(MainOptions.UPDINDEX, updindex);
    set(MainOptions.TEXTINDEX, indexes != Indexes.NONE);
    set(MainOptions.ATTRINDEX, indexes != Indexes.NONE);
    set(MainOptions.TOKENINDEX, indexes != Indexes.NONE);
    set(MainOptions.FTINDEX, indexes == Indexes.ALL);
    execute(input == null ? new CreateDB(NAME) : new CreateDB(NAME, input));
    execute(new Close());
  }

  /**
   * Creates the test database, adds a document and deletes a node.
   * @param updindex incremental index update flag
   * @param indexes enabled indexes
   * @return node ID of the checked node
   */
  private static String update(final boolean updindex, final Indexes indexes) {
    create(updindex, indexes, DOC);
    query(_DB_ADD.args(NAME, " <y>fourth entry</y>", "y.xml"));
    query("delete node " + _DB_GET.args(NAME) + "//a");
    return query(_DB_NODE_ID.args(C));
  }

  /**
   * Drops indexes of the test database.
   * @param types index types
   */
  private static void drop(final CmdIndex... types) {
    execute(new Open(NAME));
    for(final CmdIndex type : types) execute(new DropIndex(type));
    execute(new Close());
  }

  /**
   * Checks the storage format of the test database.
   * @param legacy format of version 12 expected
   * @throws IOException I/O exception
   */
  private static void storage(final boolean legacy) throws IOException {
    final Map<String, String> meta = meta();
    assertEquals(legacy ? DataText.OLDSTORAGE : DataText.STORAGE, meta.get(DataText.DBSTR));
    if(!legacy) return;

    for(final String key : SEGMENT_KEYS) assertFalse(meta.containsKey(key), key);
    for(final IOFile file : context.soptions.dbPath(NAME).children()) {
      if(file.isDir()) continue;
      assertTrue(FILES.matcher(file.name()).matches(), "unknown file: " + file.name());
    }
  }

  /**
   * Returns the meta data entries of the test database.
   * @return entries
   * @throws IOException I/O exception
   */
  private static Map<String, String> meta() throws IOException {
    final Map<String, String> map = new HashMap<>();
    final MetaData md = new MetaData(NAME, context.options, context.soptions);
    try(DataInput in = new DataInput(md.dbFile(DataText.DATAINF))) {
      while(true) {
        final String key = Token.string(in.readToken());
        if(key.isEmpty()) return map;
        map.put(key, Token.string(in.readToken()));
      }
    }
  }
}
