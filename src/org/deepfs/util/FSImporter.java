package org.deepfs.util;

import java.io.File;
import org.basex.core.Context;
import org.basex.core.Prop;
import org.basex.core.proc.CreateDB;
import org.basex.data.Nodes;
import org.basex.data.XMLSerializer;
import org.basex.io.PrintOutput;
import org.basex.query.QueryException;
import org.basex.query.QueryProcessor;

/**
 * Build a FSML database while traversing a directory hierarchy.
 * 
 * @author Workgroup DBIS, University of Konstanz 2009, ISC License
 * @author Alexander Holupirek
 */
public final class FSImporter implements FSTraversal {

  /** Database context. */
  private Context ctx;
  /** Root node. */
  private String rootNode = "/fsml";
  /** Insertion target. */
  private String targetNode = rootNode;

  /**
   * Escapes characters not allowed in XML attributes.
   * @param s string to encode
   * @return escaped string
   */
  private String escape(final String s) {
      StringBuilder sb = new StringBuilder();
      for (int i = 0; i < s.length(); i++) {
          char c = s.charAt(i);
          if (c == '&') sb.append("&amp;");
          else if (c == '<') sb.append("&lt;");
          else if (c == '>') sb.append("&gt;");
          else if (c == '\"') sb.append("&quot;");
          else if (c == '\'') sb.append("&apos;");
          else sb.append(c);
     }
      return sb.toString();
  }

  /**
   * Builds an update query & inserts a file node.
   * 
   * @param f file to be inserted
   * @return escaped file name
   */
  private String insert(final File f) {
    final String fname = escape(f.getName());
    final String fragment = f.isDirectory() ? "<dir name=\"" + fname + "\"/>"
        : "<file name=\"" + fname + "\"/>";
    final String query = "insert nodes " + fragment + " into " + targetNode;
    try {
      new QueryProcessor(query, ctx).queryNodes();
    } catch(QueryException e) {
      e.printStackTrace();
    }
    return fname;
  }
  
  @Override
  public void preTraversalVisit(final File d) {
    ctx = new Context();
    new CreateDB("<fsml/>", "fsml").execute(ctx);
  }

  @Override
  public void postTraversalVisit(final File d) {
    try {
      final Nodes n = new QueryProcessor("/", ctx).queryNodes();
      final PrintOutput out = new PrintOutput(System.out);
      final XMLSerializer xml = new XMLSerializer(out);
      n.serialize(xml);
      out.flush();
      xml.close();
    } catch(Exception e) {
      e.printStackTrace();
    }
  }

  @Override
  public void levelUpdate(final int l) {
    /* NOT_USED */
  }
  
  @Override
  public void preDirectoryVisit(final File d) {
    final String fname = insert(d);
    targetNode += "/dir[@name = \"" + fname + "\"]";
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
  public void symLinkVisit(final File f) {
    // TODO Auto-generated method stub
  }

  /**
   * Temporary test method.
   * @param args cmdline arguments
   */
  public static void main(final String[] args) {
    new FSWalker(new FSImporter()).traverse(new File(Prop.WORK));
  }
}
