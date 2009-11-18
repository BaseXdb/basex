package org.deepfs.util;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

import org.basex.core.Context;
import org.basex.core.Main;
import org.basex.core.Prop;
import org.basex.core.proc.CreateDB;
import org.basex.io.IO;
import org.basex.query.QueryException;
import org.basex.query.QueryProcessor;
import org.deepfs.fsml.extractors.SpotlightExtractor;
import org.deepfs.fsml.util.BufferedFileChannel;
import org.deepfs.fsml.util.DeepFile;
import org.deepfs.fsml.util.FSMLSerializer;
import org.deepfs.fsml.util.ParserException;
import org.deepfs.fsml.util.ParserRegistry;

/**
 * Build a FSML database while traversing a directory hierarchy.
 * @author Workgroup DBIS, University of Konstanz 2009, ISC License
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
  /** Root node. */
  private final String rootNode = "/deepfs";
  /** Insertion target. */
  private String targetNode = rootNode;

  /** The name of the database. */
  private final String db;
  /** The name of the current file. */
  private String currentFile;
  /** The spotlight extractor. */
  private final SpotlightExtractor spotlight;

  /**
   * Constructor.
   * @param context the database context.
   * @param dbName name of the database.
   */
  public FSImporter(final Context context, final String dbName) {
    ctx = context;
    final Prop prop = ctx.prop;
    prop.set(Prop.INTPARSE, true);
    prop.set(Prop.ENTITY, false);
    prop.set(Prop.DTD, false);
    db = dbName;

    if(prop.is(Prop.FSMETA) && prop.is(Prop.SPOTLIGHT) && Prop.MAC) {
      SpotlightExtractor spot;
      try {
        spot = new SpotlightExtractor();
      } catch(final ParserException ex) {
        Main.debug("Failed to load spotex library (%).", ex);
        spot = null;
      }
      spotlight = spot;
      return;
    }
    spotlight = null;
  }

  /**
   * Escapes a string.
   * @param text the text to check.
   * @return the escaped text.
   */
  private String escape(final String text) {
    final StringBuilder sb = new StringBuilder(text.length());
    for(final char c : text.toCharArray()) {
      switch(c) {
        case '&':
          sb.append("&amp;");
          break;
        case '>':
          sb.append("&gt;");
          break;
        case '<':
          sb.append("&lt;");
          break;
        case '\"':
          sb.append("&quot;");
          break;
        case '\'':
          sb.append("&apos;");
          break;
        case '{':
          sb.append("{{");
          break;
        case '}':
          sb.append("}}");
          break;
        default:
          sb.append(c);
      }
    }
    return sb.toString();
  }

  /**
   * Builds an update query & inserts a file node.
   * @param f file to be inserted
   * @return escaped file name
   */
  private String insert(final File f) {
    currentFile = f.toString();
    String xmlFragment = null;
    if(f.isDirectory()) {
      try {
        xmlFragment = FSMLSerializer.serializeFile(f);
      } catch(final IOException e) {
        Main.debug("Failed to open dir (%)", e);
      }
    } else {
      try {
        final BufferedFileChannel bfc = new BufferedFileChannel(f, buffer);
        try {
          final DeepFile deepFile = new DeepFile(parserRegistry, bfc,
              ctx.prop.is(Prop.FSMETA), ctx.prop.is(Prop.FSXML),
              ctx.prop.is(Prop.FSCONT), ctx.prop.num(Prop.FSTEXTMAX));
          if(spotlight != null) {
            spotlight.extract(deepFile);
            deepFile.finishMetaExtraction();
          }
          deepFile.extract();
          xmlFragment = FSMLSerializer.serialize(deepFile);
        } catch(final IOException ex) {
          Main.debug(
              "Failed to extract metadata/contents from the file (% - %)",
              f.getAbsolutePath(), ex);
        } finally {
          try {
            bfc.close();
          } catch(final IOException e1) {}
        }
      } catch(final IOException e) { // opening failed
        Main.debug("Failed to open the file (% - %)", f.getAbsolutePath(), e);
      }
    }

    if(xmlFragment != null) {
      final String query = "insert nodes " + xmlFragment + " into "
          + targetNode;
      final QueryProcessor qp = new QueryProcessor(query, ctx);
      try {
        qp.query();
      } catch(final QueryException ex) { // insertion failed
        Main.debug(
            "Failed to insert a node for the file % into the document (%)",
            f.getAbsolutePath(), ex);
      } finally {
        try {
          qp.close();
        } catch(final IOException e) {
          Main.debug("Failed to close query processor (%).", e);
        }
      }
    }

    return f.getName();
  }

  @Override
  public void preTraversalVisit(final File d) {
    final CreateDB c = new CreateDB("<deepfs backingstore=\""
        + escape(d.getAbsolutePath()) + "\"/>", db);
    if(!c.execute(ctx)) throw new RuntimeException(
        "Failed to create file system database (" + c.info() + ").");
    ctx.data.meta.deepfs = true;
  }

  @Override
  public void postTraversalVisit(final File d) { /* NOT_USED */}

  @Override
  public void levelUpdate(final int l) { /* NOT_USED */}

  @Override
  public void preDirectoryVisit(final File d) {
    final String fname = insert(d);
    targetNode += "/dir[@name = \"" + escape(fname) + "\"]";
  }

  @Override
  public void postDirectoryVisit(final File d) {
    targetNode = targetNode.substring(0, targetNode.lastIndexOf('/'));
  }

  @Override
  public void regularFileVisit(final File f) {
    insert(f);
  }

  @Override
  public void symLinkVisit(final File f) { /* NOT_USED */}

  /**
   * Returns the current file name.
   * @return the current file name.
   */
  public String getCurrentFileName() {
    return currentFile;
  }
}
