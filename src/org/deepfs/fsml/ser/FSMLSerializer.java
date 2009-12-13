package org.deepfs.fsml.ser;

import static org.basex.data.DataText.*;
import static org.basex.util.Token.*;
import static org.deepfs.fsml.DeepFile.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.TreeMap;
import java.util.Map.Entry;
import org.basex.util.Atts;
import org.deepfs.fs.DeepFS;
import org.deepfs.fsml.DeepFile;
import org.deepfs.fsml.MetaElem;
import org.deepfs.fsml.DeepFile.Content;
import org.deepfs.util.FSImporter;

/**
 * DeepFile Serializer. Serializes DeepFiles in XML.
 * @author Workgroup DBIS, University of Konstanz 2009, ISC License
 * @author Bastian Lemke
 */
public class FSMLSerializer {
  /** The DeepFile to serialize. */
  private final DeepFile deepFile;
  /** The xml fragment. */
  private final StringBuilder xml;

  /**
   * Constructor.
   * @param df the DeepFile
   */
  public FSMLSerializer(final DeepFile df) {
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
    final FSMLSerializer ser = new FSMLSerializer(deepFile);
    ser.serialize();
    return ser.toString();
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
    for(int i = 0; i < size; i++)
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
  public static String serializeFile(final File file,
      final boolean root) {
    final FSMLSerializer ser = new FSMLSerializer();
    final Atts atts = DeepFS.atts(file, root);
    final String name;
    if(file.isDirectory()) name = root ? FSImporter.ROOT_NODE : DIR_NS;
    else name = FILE_NS;
    ser.startElem(name, convAtts(atts), true);
    return ser.toString();
  }

  /**
   * Serializes the DeepFile.
   * @return the serialized deep file
   * @throws IOException if any error occurs
   */
  public String serialize() throws IOException {
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
   * @param close flag if the attribute should be closed (
   *          <code>&lt;element/&gt;</code>)
   */
  private void startElem(final String e, final String[][] atts,
      final boolean close) {
    xml.append('<').append(e);
    if(atts != null) {
      for(int i = 0; i < atts.length; i++) {
        xml.append(' ').append(atts[i][0]);
        xml.append('=').append('"');
        text(atts[i][1]);
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
        startElem(CONTENT_NS, convAtts(atts));
      } else { // regular file
        final Atts a = d.getFSAtts();
        startElem(FILE_NS, convAtts(a));
      }

      // serialize metadata
      final TreeMap<MetaElem, ArrayList<String>> meta = d.getMeta();
      if(meta != null) {
        for(final Entry<MetaElem, ArrayList<String>> e : meta.entrySet())
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
          startElem(TEXT_CONTENT_NS, convAtts(atts));
          text(t.getContent());
          endElem(TEXT_CONTENT_NS);
        }
      }

      // serialize xml contents
      final Content[] xmlContents = d.getXMLContents();
      if(xmlContents != null) {
        for(final Content x : xmlContents) {
          atts.reset();
          atts.add(OFFSET, token(x.getOffset()));
          atts.add(SIZE, token(x.getSize()));
          startElem(XML_CONTENT_NS, convAtts(atts));
          xml(x.getContent());
          endElem(XML_CONTENT_NS);
        }
      }

      // process all content sections
      serializeDeepFiles(true, d.getContent());
      endElem(subfile ? CONTENT_NS : FILE_NS);
    }
  }

  @Override
  public String toString() {
    return xml.toString();
  }
}
