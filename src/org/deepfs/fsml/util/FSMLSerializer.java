package org.deepfs.fsml.util;

import static org.basex.data.DataText.*;
import static org.basex.util.Token.*;
import static org.deepfs.fsml.util.DeepFile.*;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.TreeMap;
import java.util.Map.Entry;

import org.basex.io.PrintOutput;
import org.basex.util.Atts;
import org.deepfs.fs.DeepFS;
import org.deepfs.fsml.util.DeepFile.TextContent;
import org.deepfs.fsml.util.DeepFile.XMLContent;

/**
 * DeepFile Serializer. Serializes DeepFiles in XML.
 * @author Bastian Lemke
 */
public class FSMLSerializer {

  /** The DeepFile to serialize. */
  private final DeepFile deepFile;
  /** The output to write the xml data to. */
  private final PrintOutput o;

  /**
   * Constructor.
   * @param df the DeepFile.
   * @param po the output.
   */
  public FSMLSerializer(final DeepFile df, final PrintOutput po) {
    deepFile = df;
    o = po;
  }

  /**
   * Constructor.
   * @param po the output.
   */
  private FSMLSerializer(final PrintOutput po) {
    deepFile = null;
    o = po;
  }

  /**
   * Serializes a DeepFile.
   * @param deepFile the DeepFile to serialize.
   * @return the xml data as string.
   * @throws IOException if any error occurs.
   */
  public static String serialize(final DeepFile deepFile) throws IOException {
    final ByteArrayOutputStream baos = new ByteArrayOutputStream();
    final PrintOutput po = new PrintOutput(baos);
    final FSMLSerializer ser = new FSMLSerializer(deepFile, po);
    ser.serialize();
    final String s = baos.toString();
    po.close();
    return s;
  }

  /**
   * Serializes a file without metadata and content.
   * @param file the file to serialize.
   * @return the xml data as string.
   * @throws IOException if any error occurs.
   */
  public static String serializeFile(final File file) throws IOException {
    final ByteArrayOutputStream baos = new ByteArrayOutputStream();
    final PrintOutput po = new PrintOutput(baos);
    final FSMLSerializer ser = new FSMLSerializer(po);
    final Atts atts = DeepFS.atts(file);
    ser.startElem(file.isDirectory() ? DIR_NS : FILE_NS, atts, true);
    final String s = baos.toString();
    po.close();
    return s;
  }

  /**
   * Serializes the DeepFile.
   * @throws IOException if any error occurs.
   */
  public void serialize() throws IOException {
    serializeDeepFiles(false, deepFile);
  }

  /**
   * Starts an xml element.
   * @param e the name of the element.
   * @param atts the xml attributes.
   * @throws IOException if any error occurs.
   */
  private void startElem(final byte[] e, final Atts atts) throws IOException {
    startElem(e, atts, false);
  }

  /**
   * Starts an xml element.
   * @param e the name of the element.
   * @param atts the xml attributes.
   * @param close flag if the attribute should be closed (
   *          <code>&lt;element/&gt;</code>).
   * @throws IOException if any error occurs.
   */
  private void startElem(final byte[] e, final Atts atts, final boolean close)
      throws IOException {
    o.write(ELEM1);
    o.write(e);
    for(int i = 0; i < atts.size; i++) {
      o.write(' ');
      o.write(atts.key[i]);
      o.write('=');
      o.write('"');
      text(atts.val[i]);
      o.write('"');
    }
    if(close) o.write(ELEM4);
    else o.write(ELEM2);
  }

  /**
   * Writes an xml element that contains a text node.
   * @param n name of the xml element.
   * @param a xml attributes of the element.
   * @param t text content of the element (the text node).
   * @throws IOException if any error occurs.
   */
  private void nodeAndText(final byte[] n, final Atts a, final byte[] t)
      throws IOException {
    startElem(n, a, false);
    text(t);
    endElem(n);
  }

  /**
   * Checks if a char has to be escaped and writes it to the output.
   * @param b the char.
   * @throws IOException if any error occurs.
   */
  private void escape(final byte b) throws IOException {
    switch(b) {
      case '&':
        o.print(E_AMP);
        break;
      case '>':
        o.print(E_GT);
        break;
      case '<':
        o.print(E_LT);
        break;
      case '\"':
        o.print(E_QU);
        break;
      case '\'':
        o.print(token("&apos;"));
        break;
      case '{':
        o.print("{{");
        break;
      case '}':
        o.print("}})");
        break;
      default:
        o.write(b);
    }
  }

  /**
   * Writes an xml text node.
   * @param t the text.
   * @throws IOException if any error occurs.
   */
  private void text(final byte[] t) throws IOException {
    for(final byte b : t) {
      escape(b);
    }
  }

  /**
   * Ends an xml element.
   * @param e the name of the xml element.
   * @throws IOException if any error occurs.
   */
  private void endElem(final byte[] e) throws IOException {
    o.write(ELEM3);
    o.write(e);
    o.write(ELEM2);
  }

  /**
   * Serializes one or more DeepFiles.
   * @param subfile flag if the DeepFile(s) are inside a real file (content
   *          sections) or if the DeepFile(s) represent regular files in the
   *          file system.
   * @param c the DeepFile(s)
   * @throws IOException if any error occurs.
   */
  private void serializeDeepFiles(final boolean subfile, final DeepFile... c)
      throws IOException {
    final Atts atts = new Atts();
    for(final DeepFile d : c) { // iterate over all deep files
      if(subfile) { // content section
        atts.add(OFFSET, token(d.getOffset()));
        atts.add(SIZE, token(d.getSize()));
        startElem(CONTENT_NS, atts);
      } else { // regular file
        final Atts a = d.getFSAtts();
        startElem(FILE_NS, a == null ? new Atts() : a);
      }

      // serialize metadata
      final TreeMap<MetaElem, ArrayList<byte[]>> meta = d.getMeta();
      if(meta != null) {
        for(final Entry<MetaElem, ArrayList<byte[]>> e : meta.entrySet())
          for(final byte[] val : e.getValue())
            nodeAndText(e.getKey().get(), atts.reset(), val);
      }

      // serialize text contents
      final TextContent[] textContents = d.getTextContents();
      if(textContents != null) {
        for(final TextContent t : textContents) {
          atts.reset();
          atts.add(OFFSET, token(t.getOffset()));
          atts.add(SIZE, token(t.getSize()));
          startElem(TEXT_CONTENT_NS, atts);
          text(t.getText());
          endElem(TEXT_CONTENT_NS);
        }
      }

      // serialize xml contents
      final XMLContent[] xmlContents = d.getXMLContents();
      if(xmlContents != null) {
        for(final XMLContent x : xmlContents) {
          atts.reset();
          atts.add(OFFSET, token(x.getOffset()));
          atts.add(SIZE, token(x.getSize()));
          startElem(XML_CONTENT_NS, atts);
          o.write(token(x.asString()));
          endElem(XML_CONTENT_NS);
        }
      }

      // process all content sections
      serializeDeepFiles(true, d.getContent());
      endElem(subfile ? CONTENT_NS : FILE_NS);
    }
  }
}
