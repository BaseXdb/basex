package org.deepfs.fsml.parsers;

import java.io.IOException;
import java.util.TreeMap;
import org.basex.build.MemBuilder;
import org.basex.build.Parser;
import org.basex.core.Main;
import org.basex.core.Prop;
import org.basex.data.Data;
import org.basex.io.IOContent;
import org.deepfs.fsml.BufferedFileChannel;
import org.deepfs.fsml.DeepFile;
import org.deepfs.fsml.FileType;
import org.deepfs.fsml.MimeType;
import org.deepfs.fsml.ParserException;
import org.deepfs.fsml.ParserRegistry;

/**
 * Parser for XML files.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Bastian Lemke
 */
public final class XMLParser implements IFileParser {

  /** Suffixes of all file formats, this parser is able to parse. */
  private static final TreeMap<String, MimeType> SUFFIXES =
      new TreeMap<String, MimeType>();

  static {
    SUFFIXES.put("xml", MimeType.XML);
    SUFFIXES.put("kml", MimeType.KML);
    SUFFIXES.put("rng", MimeType.XML);
    SUFFIXES.put("webloc", MimeType.XML);
    SUFFIXES.put("mailtoloc", MimeType.XML);
    for(final String suf : SUFFIXES.keySet())
      ParserRegistry.register(suf, XMLParser.class);
  }

  @Override
  public boolean check(final DeepFile df) throws IOException {
    final BufferedFileChannel f = df.getBufferedFileChannel();
    return parse(f, new Prop(false)) != null ? true : false;
  }

  /**
   * Checks if the document is well-formed and returns the corresponding main
   * memory database.
   * @param f the {@link BufferedFileChannel} to read the xml document from
   * @param prop the database properties to use
   * @return the main memory database or {@code null} if the document is
   *         not well-formed
   * @throws IOException if any error occurs
   */
  public Data parse(final BufferedFileChannel f, final Prop prop)
      throws IOException {

    if(f.size() > Integer.MAX_VALUE)
     throw new IOException("Input file too big.");

    try {
      final byte[] data = f.get(new byte[(int) f.size()]);
      final Parser p = Parser.xmlParser(new IOContent(data), prop, "");
      return new MemBuilder(p, prop).build();
    } catch(final IOException ex) {
      // XML parsing exception...
      return null;
    }
  }

  @Override
  public void extract(final DeepFile deepFile) throws IOException {
    final BufferedFileChannel bfc = deepFile.getBufferedFileChannel();
    final int maxSize = deepFile.maxTextSize();
    if(bfc.size() <= maxSize) {
      final Data data = parse(bfc, deepFile.getContext().prop);
      if(data != null) {
        if(deepFile.extractMeta()) {
          deepFile.setFileType(FileType.XML);
          final String name = bfc.getFileName();
          final String suf = name.substring(
              name.lastIndexOf('.') + 1).toLowerCase();
          final MimeType mime = SUFFIXES.get(suf);
          if(mime == null) Main.notexpected();
          deepFile.setFileFormat(mime);
        }
        if(deepFile.extractXML()) {
          deepFile.addXML(bfc.getOffset(), (int) bfc.size(), data);
        }
        return; // successfully parsed xml content
      }
    }
    if(deepFile.extractText()) { // if file too big or not well-formed
      try {
        deepFile.fallback();
      } catch(final ParserException e) {
        deepFile.debug(
            "XMLParser: Failed to read text content from file with " +
                "fallback parser (%).", e);
      }
    }
  }

  @Override
  public void propagate(final DeepFile deepFile) {
    Main.notimplemented();
  }
}
