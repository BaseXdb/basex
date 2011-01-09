package org.deepfs.fsml.ser;

import static org.deepfs.fs.DeepFS.*;
import static org.basex.util.Token.*;
import java.io.File;
import java.io.IOException;
import java.util.TreeMap;
import java.util.Map.Entry;
import org.basex.util.Atts;
import org.basex.util.StringList;
import org.deepfs.fs.DeepFS;
import org.deepfs.fsml.DeepFile;
import org.deepfs.fsml.MetaElem;
import org.deepfs.fsml.DeepFile.Content;

/**
 * DeepFile Serializer. Serializes DeepFiles in XML.
 *
 * @author BaseX Team 2005-11, ISC License
 * @author Bastian Lemke
 */
public final class FSMLSerializer {
  /** File reference to serialize. */
  private final DeepFile deepFile;
  /** The xml fragment. */
  private final StringBuilder xml;

  /**
   * Constructor.
   * @param df the DeepFile
   */
  private FSMLSerializer(final DeepFile df) {
    deepFile = df;
    xml = new StringBuilder();
  }

  /**
   * Constructor.
   */
  private FSMLSerializer() {
    this(null);
  }

  /**
   * Serializes a DeepFile.
   * @param deepFile the DeepFile to serialize
   * @return the xml data as string
   * @throws IOException if any error occurs
   */
  public static String serialize(final DeepFile deepFile) throws IOException {
    return new FSMLSerializer(deepFile).serialize();
  }

  /**
   * Converts attributes object into a String array.
   * @param atts the attributes to convert
   * @return the xml attributes as String array
   */
  private static String[][] convAtts(final Atts atts) {
    if(atts == null) return null;
    final int size = atts.size;
    final byte[][] keys = atts.key;
    final byte[][] vals = atts.val;
    final String[][] a = new String[size][];
    for(int i = 0; i < size; ++i)
      a[i] = new String[] { string(keys[i]), string(vals[i]) };
    return a;
  }

  /**
   * Serializes a file without metadata and content.
   * @param file the file to serialize
   * @param root if true, a filesystem root is are created instead of a simple
   *          directory
   * @return the xml data as string
   */
  public static String serialize(final File file, final boolean root) {
    final FSMLSerializer ser = new FSMLSerializer();
    final Atts atts = DeepFS.atts(file, root);
    final String name;
    if(file.isDirectory()) name = root ? S_DEEPFS : S_DIR;
    else name = S_FILE;
    ser.startElem(name, convAtts(atts), true);
    return ser.xml.toString();
  }

  /**
   * Serializes the DeepFile.
   * @return the serialized deep file
   * @throws IOException if any error occurs
   */
  private String serialize() throws IOException {
    serializeDeepFiles(false, deepFile);
    return xml.toString();
  }

  /**
   * Starts an xml element.
   * @param e the name of the element
   * @param atts the xml attributes
   */
  private void startElem(final String e, final String[][] atts) {
    startElem(e, atts, false);
  }

  /**
   * Starts an xml element.
   * @param e the name of the element
   * @param atts the xml attributes
   * @param close flag if the attribute should be closed {@code <element/>})
   */
  private void startElem(final String e, final String[][] atts,
      final boolean close) {
    xml.append('<').append(e);
    if(atts != null) {
      for(final String[] att : atts) {
        xml.append(' ').append(att[0]);
        xml.append('=').append('"');
        text(att[1]);
        xml.append('"');
      }
    }
    xml.append(close ? "/>" : '>');
  }

  /**
   * Writes an xml element that contains a text node.
   * @param n name of the xml element
   * @param a xml attributes of the element
   * @param t text content of the element (the text node)
   */
  private void nodeAndText(final String n, final String[][] a,
      final String t) {
    startElem(n, a, false);
    text(t);
    endElem(n);
  }

  /**
   * Checks if a char has to be escaped and adds it to the output.
   * @param ch the char
   */
  private void escape(final char ch) {
    switch(ch) {
      case '&':  xml.append("&amp;");  break;
      case '>':  xml.append("&gt;");   break;
      case '<':  xml.append("&lt;");   break;
      case '\"': xml.append("&quot;"); break;
      case '\'': xml.append("&apos;"); break;
      default:   escapeXQUP(ch);
    }
  }

  /**
   * Checks if a char has to be escaped for xquery update and writes it to the
   * output.
   * @param ch the char to check
   */
  private void escapeXQUP(final char ch) {
    switch(ch) {
      case '{': xml.append("{{"); break;
      case '}': xml.append("}}"); break;
      default:  xml.append(ch);
    }
  }

  /**
   * Writes an xml text node.
   * @param t the text
   */
  private void text(final String t) {
    for(final char ch : t.toCharArray()) escape(ch);
  }

  /**
   * Writes an xml fragment.
   * @param x the xml fragment
   */
  private void xml(final String x) {
    for(final char ch : x.toCharArray()) escapeXQUP(ch);
  }

  /**
   * Ends an xml element.
   * @param e the name of the xml element
   */
  private void endElem(final String e) {
    xml.append("</").append(e).append(">");
  }

  /**
   * Serializes one or more DeepFiles.
   * @param subfile flag if the DeepFile(s) are inside a real file (content
   *          sections) or if the DeepFile(s) represent regular files in the
   *          file system
   * @param c the DeepFile(s)
   * @throws IOException if any error occurs
   */
  private void serializeDeepFiles(final boolean subfile, final DeepFile... c)
      throws IOException {
    final Atts atts = new Atts();
    for(final DeepFile d : c) { // iterate over all deep files
      if(subfile) { // content section
        atts.add(OFFSET, token(d.getOffset()));
        atts.add(SIZE, token(d.getSize()));
        startElem(S_CONTENT, convAtts(atts));
      } else { // regular file
        final Atts a = d.getFSAtts();
        startElem(S_FILE, convAtts(a));
      }

      // serialize metadata
      final TreeMap<MetaElem, StringList> meta = d.getMeta();
      if(meta != null) {
        for(final Entry<MetaElem, StringList> e : meta.entrySet())
          for(final String val : e.getValue())
            nodeAndText(e.getKey().get(), null, val);
      }

      // serialize text contents
      final Content[] textContents = d.getTextContents();
      if(textContents != null) {
        for(final Content t : textContents) {
          atts.reset();
          atts.add(OFFSET, token(t.getOffset()));
          atts.add(SIZE, token(t.getSize()));
          startElem(S_TEXT_CONTENT, convAtts(atts));
          text(t.getContent());
          endElem(S_TEXT_CONTENT);
        }
      }

      // serialize xml contents
      final Content[] xmlContents = d.getXMLContents();
      if(xmlContents != null) {
        for(final Content x : xmlContents) {
          atts.reset();
          atts.add(OFFSET, token(x.getOffset()));
          atts.add(SIZE, token(x.getSize()));
          startElem(S_XML_CONTENT, convAtts(atts));
          xml(x.getContent());
          endElem(S_XML_CONTENT);
        }
      }

      // process all content sections
      serializeDeepFiles(true, d.getContent());
      endElem(subfile ? S_CONTENT : S_FILE);
    }
  }
}
