package org.deepfs.util;

import static org.deepfs.fs.DeepFS.*;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import org.basex.core.Context;
import org.basex.core.Prop;
import org.basex.core.cmd.CreateDB;
import org.basex.io.IO;
import org.basex.query.QueryException;
import org.basex.query.QueryProcessor;
import org.basex.util.Util;
import org.deepfs.fs.DeepFS;
import org.deepfs.fsml.BufferedFileChannel;
import org.deepfs.fsml.DeepFile;
import org.deepfs.fsml.ParserException;
import org.deepfs.fsml.ParserRegistry;
import org.deepfs.fsml.plugin.SpotlightExtractor;
import org.deepfs.fsml.ser.FSMLSerializer;

/**
 * Build a FSML database while traversing a directory hierarchy.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Alexander Holupirek
 * @author Bastian Lemke
 */
public final class FSImporter implements FSTraversal {

  /** The buffer to use for parsing the file contents. */
  private final ByteBuffer buffer = ByteBuffer.allocateDirect(IO.BLOCKSIZE);
  /** The parser registry. */
  private final ParserRegistry parserRegistry = new ParserRegistry();

  /** Database context. */
  private final Context ctx;
  /** Insertion target. */
  private String targetNode = "/" + S_FSML;

  /** The name of the current file. */
  private String currentFile;
  /** The spotlight extractor. */
  private final SpotlightExtractor spotlight;

  /**
   * Constructor.
   * @param context the database context
   */
  public FSImporter(final Context context) {
    ctx = context;
    final Prop prop = ctx.prop;
    prop.set(Prop.INTPARSE, true);
    prop.set(Prop.ENTITY, false);
    prop.set(Prop.DTD, false);

    if(prop.is(Prop.FSMETA) && prop.is(Prop.SPOTLIGHT) && Prop.MAC) {
      SpotlightExtractor spot;
      try {
        spot = new SpotlightExtractor();
      } catch(final ParserException ex) {
        Util.debug("Failed to load spotex library (%).", ex);
        spot = null;
      }
      spotlight = spot;
      return;
    }
    spotlight = null;
  }

  /**
   * Creates a fsml database.
   * @param name name of the database
   */
  public void createDB(final String name) {
    final CreateDB c = new CreateDB(name, "<" + DeepFS.S_FSML + "/>");
    if(!c.run(ctx)) Util.notexpected(
        "Failed to create file system database (%).", c.info());
    ctx.data.meta.deepfs = true;
  }

  /**
   * Escapes a string.
   * @param text the text to check
   * @return the escaped text
   */
  public static String escape(final String text) {
    final StringBuilder sb = new StringBuilder(text.length());
    for(final char c : text.toCharArray()) {
      switch(c) {
        case '&':  sb.append("&amp;");  break;
        case '>':  sb.append("&gt;");   break;
        case '<':  sb.append("&lt;");   break;
        case '\"': sb.append("&quot;"); break;
        case '\'': sb.append("&apos;"); break;
        case '{':  sb.append("{{");     break;
        case '}':  sb.append("}}");     break;
        default:   sb.append(c);
      }
    }
    return sb.toString();
  }

  /**
   * Builds an update query & inserts a file node.
   * @param f file to be inserted
   * @param root adds a filesystem root node instead of a simple directory node
   *          (flag is ignored if the file is not a directory)
   * @return escaped file name
   */
  private String insert(final File f, final boolean root) {
    currentFile = f.toString();
    String xmlFragment = null;
    if(f.isDirectory()) {
      xmlFragment = FSMLSerializer.serialize(f, root);
    } else {
      try {
        final BufferedFileChannel bfc = new BufferedFileChannel(f, buffer);
        try {
          final DeepFile deepFile = new DeepFile(parserRegistry, bfc, ctx);
          if(spotlight != null) {
            spotlight.extract(deepFile);
            deepFile.finishMetaExtraction();
          }
          deepFile.extract();
          xmlFragment = FSMLSerializer.serialize(deepFile);
        } catch(final Exception ex) {
          Util.debug(
              "FSImporter: Failed to extract metadata/contents from the file " +
              "(% - %)", f.getAbsolutePath(), ex);
        } finally {
          try {
            bfc.close();
          } catch(final IOException e1) {
            Util.err("FSImporter: Failed to close the file (% - %)",
                bfc.getFileName(), e1);
          }
        }
      } catch(final IOException ex) {
        Util.debug("FSImporter: Failed to open the file (% - %)",
            f.getAbsolutePath(), ex);
      }
      if(xmlFragment == null)
        xmlFragment = FSMLSerializer.serialize(f, false);
    }

    if(xmlFragment != null) {
      final String query = "insert nodes " + xmlFragment + " into "
          + targetNode;
      final QueryProcessor qp = new QueryProcessor(query, ctx);
      try {
        qp.execute();
      } catch(final QueryException ex) { // insertion failed
        Util.debug(
            "FSImporter: Failed to insert a node for the file % into the " +
            "document (%)", f.getAbsolutePath(), ex);
      } finally {
        try { qp.close(); } catch(final IOException ex) { }
      }
    }

    return escape(root ? f.getAbsolutePath().replace("\\", "/") : f.getName());
  }

  @Override
  public void preTraversalVisit(final File d) {
    if(d.isDirectory()) targetNode += "/" + S_DEEPFS + "[@"
    + S_BACKINGSTORE + " = \"" + insert(d, true) + "\"]";
    else {
      preTraversalVisit(d.getParentFile());
      insert(d, false);
    }
  }

  @Override
  public void postTraversalVisit(final File d) { postDirectoryVisit(d); }

  @Override
  public void levelUpdate(final int l) { /* NOT_USED */}

  @Override
  public void preDirectoryVisit(final File d) {
    targetNode += "/" + S_DIR + "[@" + S_NAME + " = \""
      + insert(d, false) + "\"]";
  }

  @Override
  public void postDirectoryVisit(final File d) {
    targetNode = targetNode.substring(0, targetNode.lastIndexOf('/'));
  }

  @Override
  public void regularFileVisit(final File f) {
    insert(f, false);
  }

  @Override
  public void symLinkVisit(final File f) { /* NOT_USED */}

  /**
   * Returns the current file name.
   * @return the current file name
   */
  public String getCurrentFileName() {
    return currentFile;
  }
}
