package org.deepfs.fsml.parsers;

import static org.basex.util.Token.*;

import java.io.IOException;
import java.util.TreeMap;

import org.basex.core.Main;
import org.basex.util.TokenBuilder;
import org.deepfs.fsml.util.BufferedFileChannel;
import org.deepfs.fsml.util.DeepFile;
import org.deepfs.fsml.util.FileType;
import org.deepfs.fsml.util.MimeType;
import org.deepfs.fsml.util.ParserRegistry;

/**
 * Text parser that tries to extract textual content from files.
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
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
    for(final String suf : SUFFIXES.keySet())
      ParserRegistry.register(suf, TXTParser.class);
    ParserRegistry.registerFallback(TXTParser.class);
  }

  /** {@inheritDoc} */
  @Override
  public boolean check(final BufferedFileChannel bfc) {
    // final String name = bfc.getFileName();
    // final String suf = name.substring(name.lastIndexOf('.') +
    // 1).toLowerCase();
    // return SUFFIXES.keySet().contains(suf);
    // [BL] search for invalid characters
    return true;
  }

  @Override
  public void extract(final DeepFile deepFile) throws IOException {
    final BufferedFileChannel bfc = deepFile.getBufferedFileChannel();

    if(deepFile.fsmeta) {
      if(!check(bfc)) return;
      deepFile.setFileType(FileType.TEXT);
      final String name = bfc.getFileName();
      final String suf = name.substring(
          name.lastIndexOf('.') + 1).toLowerCase();
      MimeType mime = SUFFIXES.get(suf);
      if(mime == null) mime = MimeType.UNKNOWN;
      deepFile.setFileFormat(mime);
    }
    if(deepFile.fscont) {
      // ignore *.emlxpart files
      if(bfc.getFileName().endsWith(".emlxpart")) return;
      final int len = (int) Math.min(bfc.size(), deepFile.fstextmax);
      final TokenBuilder content = new TokenBuilder(len);
      final int bufSize = bfc.getBufferSize();
      int remaining = len;
      while(remaining > 0) {
        int bytesToRead = remaining > bufSize ? bufSize : remaining;
        remaining -= bytesToRead;
        final boolean res = bfc.buffer(bytesToRead);
        assert res;
        while(bytesToRead-- > 0) {
          final int b = bfc.get();
          if(b >= 0 && b < ' ' && !ws(b)) return; // perhaps a binary file?
          if(b <= 0x7F) { // ascii char
            content.add((byte) b);
          } else {
            final int followingBytes;
            if(b >= 0xC2 && b <= 0xDF) { // two byte UTF-8 char
              followingBytes = 1;
            } else if(b >= 0xE0 && b <= 0xEF) { // three byte UTF-8 char
              followingBytes = 2;
            } else if(b >= 0xF0 && b <= 0xF4) { // four byte UTF-8 char
              followingBytes = 3;
            } else {
              return; // not an UTF-8 character
            }
            if(bytesToRead < followingBytes) {
              if(remaining + bytesToRead < followingBytes) {
                content.chop();
                deepFile.addText(0, len - remaining - bytesToRead, content);
                return;
              }
              remaining += bytesToRead;
              bytesToRead = remaining > bufSize ? bufSize : remaining;
              remaining -= bytesToRead;
              bfc.buffer(bytesToRead);
            }
            content.add((byte) b);
            bytesToRead -= followingBytes;
            for(int i = 0; i < followingBytes; i++) {
              final int b2 = bfc.get();
              if(b2 < 0x80 || b2 > 0xBF) return;
              content.add((byte) b2);
            }
          }
        }
      }
      content.chop();
      deepFile.addText(0, len, content);
    }
  }

  @Override
  public void propagate(final DeepFile deepFile) {
    Main.notimplemented();
  }
}
