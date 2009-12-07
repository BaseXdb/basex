package org.deepfs.util;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import org.basex.core.Context;
import org.basex.core.Main;
import org.basex.core.Prop;
import org.basex.io.IO;
import org.basex.query.QueryException;
import org.basex.query.QueryProcessor;
import org.deepfs.fsml.plugin.SpotlightExtractor;
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

  // [BL] better exception handling
  // [BL] more consistent way to set file name atts (file name vs. full path)
  // [BL] more consistent way to unify the file system paths

  /** The buffer to use for parsing the file contents. */
  private final ByteBuffer buffer = ByteBuffer.allocateDirect(IO.BLOCKSIZE);
  /** The parser registry. */
  private final ParserRegistry parserRegistry = new ParserRegistry();

  /** Database context. */
  private final Context ctx;
  /** Document node. */
  public static final String DOC_NODE = "fsml";
  /** Insertion target. */
  private String targetNode = "/" + DOC_NODE;

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
   * @param text the text to check
   * @return the escaped text
   */
  public static String escape(final String text) {
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
   * @param absolutePath if true, the absolute path is added instead of the file
   *          name
   * @return escaped file name
   */
  private String insert(final File f, final boolean absolutePath) {
    currentFile = f.toString();
    String xmlFragment = null;
    if(f.isDirectory()) {
      try {
        xmlFragment = FSMLSerializer.serializeFile(f, absolutePath);
      } catch(final IOException e) {
        Main.debug("Failed to open dir (%)", e);
      }
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
          if(absolutePath) {
            final String par = escape(f.getParent().replace("\\", "/"));
            xmlFragment = xmlFragment.replace("<file name=\"",
                "<file name=\"" + par + "/");
          }
        } catch(final Exception ex) {
          Main.debug(
              "FSImporter: Failed to extract metadata/contents from the file " +
              "(% - %)", f.getAbsolutePath(), ex);
        } finally {
          try {
            bfc.close();
          } catch(final IOException e1) { /* */}
        }
      } catch(final IOException e) {
        Main.debug("FSImporter: Failed to open the file (% - %)",
            f.getAbsolutePath(), e);
      }
    }
    
    if(xmlFragment == null) {
      try {
        xmlFragment = FSMLSerializer.serializeFile(f, absolutePath);
      } catch(IOException e) {
        Main.debug("FSImporter: Failed to parse file attributes (% - %)",
            f.getAbsolutePath(), e);
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
            "FSImporter: Failed to insert a node for the file % into the " +
            "document (%)", f.getAbsolutePath(), ex);
      } finally {
        try {
          qp.close();
        } catch(final IOException e) { /* */ }
      }
    }

    return escape(absolutePath ? f.getAbsolutePath().replace("\\", "/")
        : f.getName());
  }

  @Override
  public void preTraversalVisit(final File d) {
    final String fname = insert(d, true);
    if(d.isDirectory())
      targetNode += "/dir[@name = \"" + escape(fname) + "\"]";
  }

  @Override
  public void postTraversalVisit(final File d) { postDirectoryVisit(d); }

  @Override
  public void levelUpdate(final int l) { /* NOT_USED */}

  @Override
  public void preDirectoryVisit(final File d) {
    final String fname = insert(d, false);
    targetNode += "/dir[@name = \"" + escape(fname) + "\"]";
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
