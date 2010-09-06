package org.deepfs.fsml.parsers;

import java.io.IOException;
import java.util.TreeMap;
import org.basex.util.Token;
import org.basex.util.Util;
import org.deepfs.fsml.BufferedFileChannel;
import org.deepfs.fsml.DeepFile;
import org.deepfs.fsml.FileType;
import org.deepfs.fsml.MimeType;
import org.deepfs.fsml.ParserRegistry;

/**
 * Text parser that tries to extract textual content from files.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Bastian Lemke
 */
public final class TXTParser implements IFileParser {

  /** Suffixes of all file formats, this parser is able to parse. */
  private static final TreeMap<String, MimeType> SUFFIXES =
      new TreeMap<String, MimeType>();

  static {
    SUFFIXES.put("txt", MimeType.TXT);
    SUFFIXES.put("html", MimeType.HTML);
    SUFFIXES.put("htm", MimeType.HTML);
    SUFFIXES.put("css", MimeType.CSS);
    SUFFIXES.put("java", MimeType.JAVA);
    SUFFIXES.put("xq", MimeType.TXT);
    SUFFIXES.put("sh", MimeType.SH);
    for(final String suf : SUFFIXES.keySet())
      ParserRegistry.register(suf, TXTParser.class);
    ParserRegistry.registerFallback(TXTParser.class);
  }

  @Override
  public boolean check(final DeepFile deepFile) {
    try {
      final BufferedFileChannel bfc = deepFile.getBufferedFileChannel();
      final int len = (int) Math.min(bfc.size(), deepFile.maxTextSize());
      return Token.valid(bfc.get(new byte[len]));
    } catch(final Exception ex) {
      return false;
    }
  }

  @Override
  public void extract(final DeepFile deepFile) throws IOException {
    final BufferedFileChannel bfc = deepFile.getBufferedFileChannel();

    final int len = (int) Math.min(bfc.size(), deepFile.maxTextSize());
    final byte[] text = bfc.get(new byte[len]);
    final boolean valid = Token.valid(text);

    if(deepFile.extractText() && valid)
      deepFile.addText(0, len, Token.string(text));

    if(deepFile.extractMeta()) {
      final String name = bfc.getFileName();
      final String s = name.substring(name.lastIndexOf('.') + 1).toLowerCase();
      final MimeType mime = SUFFIXES.get(s);
      if(mime == null) {
        deepFile.setFileType(valid ? FileType.TEXT : FileType.UNKNOWN_TYPE);
        deepFile.setFileFormat(MimeType.UNKNOWN);
      } else {
        for(final FileType ft : mime.getMetaTypes())
          deepFile.setFileType(ft);
        deepFile.setFileFormat(mime);
      }
    }
  }

  @Override
  public void propagate(final DeepFile deepFile) {
    Util.notimplemented();
  }
}