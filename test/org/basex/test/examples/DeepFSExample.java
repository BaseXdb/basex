package org.basex.test.examples;

import java.io.File;
import org.basex.core.Context;
import org.basex.core.Prop;
import org.basex.core.proc.Close;
import org.basex.core.proc.CreateFS;
import org.basex.core.proc.Open;
import org.basex.core.proc.Optimize;
import org.basex.core.proc.XQuery;
import org.deepfs.fsml.DeepFile;
import org.deepfs.fsml.ParserRegistry;
import org.deepfs.fsml.ser.FSMLSerializer;
import org.deepfs.util.FSImporter;
import org.deepfs.util.FSTraversal;
import org.deepfs.util.FSWalker;

/**
 * This class presents the usage of the DeepFS package.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author BaseX Team
 */
public final class DeepFSExample {
  /** The current database Context. */
  static final Context CONTEXT = new Context();
  /** Names of the test databases. */
  private static final String[] DB = {"testdb", "testdb-mp3", "testdb-jpg"};
  /** Test file. */
  private static final File FILE = new File(".").listFiles()[0];
  /** Test directories. */
  private static final File[] DIR;

  static {
    final File[] f = findFiles();
    DIR = new File[] { new File(CONTEXT.prop.get(Prop.DBPATH)), f[0], f[1] };
  }

  /** Example queries. */
  private static final String[][] QUERIES = new String[][] {
    {
      "All directories that contain more than 10 files of type 'audio'",
      "basex:fspath(//dir[count(file[type=\"audio\"]) > 10])"
    },
    {
      "All files whose file name is equal to its title",
      "basex:fspath(//file[@name contains text {title}])"
    },
    {
      "All files that match the pattern '^[0-9]{2} - .*\\.mp3$'",
      "basex:fspath(//file[matches(@name, '^[0-9]{2} - .*\\.mp3$')])"
    },
    {
      "All pictures in landscape mode",
      "basex:fspath(//file[type=\"picture\"][pixelHeight < pixelWidth])"
    },
    {
      "All pictures that are more than twice as large as the average",
      "basex:fspath(" +
      " let $pictures   := //file[type=\"picture\"]" +
      " let $avgHeight  := avg($pictures/pixelHeight)" +
      " let $avgWidth   := avg($pictures/pixelWidth)" +
      " for $pic in //file[type=\"picture\"]" +
      " where $pic/pixelHeight > 2 * $avgHeight and" +
      "   $pic/pixelWidth  > 2 * $avgWidth" +
      " return $pic)"
    },
    {
      "Artist, album and title of all audio files",
      "string-join(" +
      " for $file in //file[type=\"audio\" and artist and album and title]" +
      " let $artist := $file/artist[1]" +
      " let $album  := $file/album[1]" +
      " let $title  := $file/title[1]" +
      " order by $artist, $album, $title" +
      " return concat($artist,\" (\",$album,\")\",\": \",$title), \"\n" +
      "\")"
    }
  };
  /** Query - DB mapping. */
  private static final int[] INDEX = new int[] { 1, 1, 1, 2, 2, 1 };

  /** Private constructor. */
  private DeepFSExample() { }

  /**
   * Main method of the example class.
   * @param args (ignored) command-line arguments
   * @throws Exception exception
   */
  public static void main(final String[] args) throws Exception {
    String xml;

    // -------------------------------------------------------------------------

    System.out.println(
        "=== Serialize a single file (without metadata and contents)");
    xml = FSMLSerializer.serialize(FILE, false);
    System.out.println(xml);

    // -------------------------------------------------------------------------

    System.out.println(
        "\n=== Serialize a single file (with metadata and text/xml contents)");
    final DeepFile deepFile = new DeepFile(FILE);
    // Extracts metadata and text/xml contents
    deepFile.extract();
    // Serializes the deep file
    xml = FSMLSerializer.serialize(deepFile);
    System.out.println(xml);

    // -------------------------------------------------------------------------

    System.out.println("\n\n=== Traverse a file system hierarchy (method 1)");
    // Creates the file system database
    new CreateFS(DIR[0].getAbsolutePath(), DB[0]).execute(CONTEXT, System.out);
    // Serializes the database
    new XQuery("/").execute(CONTEXT, System.out);
    // Closes the database
    new Close().execute(CONTEXT, System.out);

    // -------------------------------------------------------------------------

    System.out.println("\n\n=== Traverse a file system hierarchy (method 2)");
    // Initializes the file system importer
    final FSImporter importer = new FSImporter(CONTEXT);
    // Creates the database
    importer.createDB(DB[0]);
    // Traverses the directory, extracts the metadata and text/xml contents and
    // inserts everything into the database
    new FSWalker(importer).traverse(DIR[0]);
    // Creates indexes
    new Optimize().execute(CONTEXT, System.out);
    // Serializes the database
    new XQuery("/").execute(CONTEXT, System.out);
    // Closes the database
    new Close().execute(CONTEXT, System.out);

    // -------------------------------------------------------------------------

    System.out.println("\n\n=== List available parsers");
    System.out.println("file suffix\t| \t java class");
    System.out.println("-----------------------------------------------------");
    final ParserRegistry registry = new ParserRegistry();
    for(final String[] parser : registry.availableParsers()) {
      System.out.println(parser[0] + (parser[0].length() > 7 ? "" : "\t") +
          "\t  " + parser[1]);
    }

    // -------------------------------------------------------------------------

    System.out.println("\n=== Example queries ====================");
    if(DIR[1] != null) {
      System.out.println("\tcreating mp3 database...");
      new CreateFS(DIR[1].getAbsolutePath(), DB[1]).
        execute(CONTEXT, System.out);
      new Close().execute(CONTEXT, System.out);
    }
    if(DIR[2] != null) {
      System.out.println("\tcreating jpg database...");
      new CreateFS(DIR[2].getAbsolutePath(), DB[2]).
        execute(CONTEXT, System.out);
      new Close().execute(CONTEXT, System.out);
    }
    for(int i = 0; i < QUERIES.length; i++) {
      if(DIR[INDEX[i]] == null) continue;
      System.out.println("\n\n=== " + QUERIES[i][0]);
      new Open(DB[INDEX[i]]).execute(CONTEXT, System.out);
      new XQuery(QUERIES[i][1]).execute(CONTEXT, System.out);
      new Close().execute(CONTEXT, System.out);
    }
  }

  // ---------------------------------------------------------------------------

  /**
   * Searches for mp3 and jpg files on disk that can be used as sample data.
   * @return mp3 and jpg file that were found
   */
  static File[] findFiles() {
    System.out.println(
        "\nsearching for mp3 and jpg files (needed for example queries) ");
    final FileFinder ff = new FileFinder();
    final FSWalker walker = new FSWalker(ff);
    File mp3Directory = null;
    File jpgDirectory = null;
    try {
      for(final File f : File.listRoots())
        walker.traverse(f);
    } catch(final RuntimeException e) {
      if(ff.mp3Dir != null)
        mp3Directory = new File(ff.mp3Dir).getParentFile().getParentFile();
      if(ff.jpgDir != null)
        jpgDirectory = new File(ff.jpgDir).getParentFile().getParentFile();
    }
    if(mp3Directory == null || !mp3Directory.exists()) {
      System.out.println("... no mp3 files found. skipping example queries");
    }
    if(jpgDirectory == null || !mp3Directory.exists()) {
      System.out.println("... no mp3 files found. skipping example queries");
    }
    return new File[] { mp3Directory, jpgDirectory};
  }

  /** Class to find a folder that contains mp3 files. */
  static class FileFinder implements FSTraversal {
    /** Directory, containing mp3 files. */
    String mp3Dir;
    /** Directory, containing jpg files. */
    String jpgDir;
    @Override
    public void levelUpdate(final int l) { /* NOT_USED */ }
    @Override
    public void postDirectoryVisit(final File d) { /* NOT_USED */ }
    @Override
    public void postTraversalVisit(final File d) { /* NOT_USED */ }
    @Override
    public void preDirectoryVisit(final File d) { /* NOT_USED */ }
    @Override
    public void preTraversalVisit(final File d) { /* NOT_USED */ }
    @Override
    public void regularFileVisit(final File f) {
      final String name = f.getName();
      if(name.endsWith(".mp3")) {
        mp3Dir = f.getAbsolutePath();
        if(jpgDir != null) throw new RuntimeException();
      } else if(name.endsWith(".jpg")) {
        jpgDir = f.getAbsolutePath();
        if(mp3Dir != null) throw new RuntimeException();
      }
    }
    @Override
    public void symLinkVisit(final File f) { /* NOT_USED */ }
  }
}
