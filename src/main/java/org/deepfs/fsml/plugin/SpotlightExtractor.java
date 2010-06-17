package org.deepfs.fsml.plugin;

import java.util.Date;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Map.Entry;
import org.basex.core.Main;
import org.basex.util.Token;
import org.deepfs.fsml.DeepFile;
import org.deepfs.fsml.FileType;
import org.deepfs.fsml.MetaElem;
import org.deepfs.fsml.MimeType;
import org.deepfs.fsml.ParserException;
import org.deepfs.fsml.parsers.EMLParser;
import org.deepfs.fsml.parsers.IFileParser;
import org.deepfs.fsml.parsers.MP3Parser;
import org.deepfs.fsml.util.ParserUtil;
import org.deepfs.util.LibraryLoader;

/**
 * Extracts metadata from Apple's Spotlight.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Bastian Lemke
 */
public final class SpotlightExtractor implements IFileParser {

  /** Flag, if the spotex library is available or not. */
  private static final boolean LIBAVAILABLE;

  static {
    LIBAVAILABLE = LibraryLoader.load(LibraryLoader.SPOTEXLIBNAME);
  }

  /**
   * Mapping of spotlight content types to DeepFS MIME types.
   * @author Bastian Lemke
   */
  enum SpotlightContentType {
    /** MP3 file. */
    PUBLIC_MP3(FileType.AUDIO, MimeType.MP3),
    /** JPEG file. */
    PUBLIC_JPEG(FileType.PICTURE, MimeType.JPG),
    /** PNG file. */
    PUBLIC_PNG(FileType.PICTURE, MimeType.PNG),
    /** GIF file. */
    COM_COMPUSERVE_GIF(FileType.PICTURE, MimeType.GIF),
    /** BMP file. */
    COM_MICROSOFT_BMP(FileType.PICTURE, MimeType.BMP),
    /** TIFF file. */
    PUBLIC_TIFF(FileType.PICTURE, MimeType.TIFF),
    /** HTML file. */
    PUBLIC_HTML(FileType.WEBSITE, MimeType.HTML),
    /** Plain text file. */
    PUBLIC_PLAIN_TEXT(FileType.TEXT, MimeType.TXT),
    /** CSS file. */
    COM_APPLE_DASHCODE_CSS(FileType.TEXT, MimeType.CSS),
    /** Email file. */
    COM_APPLE_MAIL_EMAIL(FileType.MESSAGE),
    /** Email file. */
    COM_APPLE_MAIL_EMLX(FileType.MESSAGE),
    /** Word file. */
    COM_MICROSOFT_WORD_DOC(FileType.DOCUMENT, MimeType.DOC),
    /** PDF file. */
    COM_ADOBE_PDF(FileType.DOCUMENT, MimeType.PDF),
    /** Image. */
    PUBLIC_IMAGE(FileType.PICTURE),
    /** Audio. */
    PUBLIC_AUDIO(FileType.AUDIO),
    /** Audio-visual content. */
    PUBLIC_AUDIOVISUAL_CONTENT(FileType.VIDEO),
    /** Data. */
    PUBLIC_DATA,
    /** Item. */
    PUBLIC_ITEM,
    /** Content. */
    PUBLIC_CONTENT,
    /** E-mail message. */
    PUBLIC_EMAIL_MESSAGE(FileType.MESSAGE),
    /** Message. */
    PUBLIC_MESSAGE(FileType.MESSAGE),
    /** Script. */
    PUBLIC_SCRIPT(FileType.SCRIPT),
    /** Shell script. */
    PUBLIC_SHELL_SCRIPT(FileType.SCRIPT),
    /** Source code. */
    PUBLIC_SOURCE_CODE,
    /** Text. */
    PUBLIC_TEXT(FileType.TEXT),
    /** XML. */
    PUBLIC_XML(FileType.XML, MimeType.XML);

    /** The {@link FileType}. */
    private final FileType fileType;
    /** The {@link MimeType}. */
    private final MimeType mimeType;

    /** Constructor for items that should be ignored. */
    private SpotlightContentType() {
      mimeType = null;
      fileType = null;
    }

    /**
     * Initializes the content type instance with a {@link FileType} and a
     * {@link MimeType}.
     * @param f the corresponding file type
     * @param m the corresponding mime type
     */
    private SpotlightContentType(final FileType f, final MimeType m) {
      fileType = f;
      mimeType = m;
    }

    /**
     * Initializes the content type instance with a {@link FileType}.
     * @param f the corresponding {@link FileType}
     */
    private SpotlightContentType(final FileType f) {
      fileType = f;
      mimeType = null;
    }

    /**
     * Returns the deepFile type for this spotlight media type.
     * @return the {@link MimeType}
     */
    MimeType getFormat() {
      return mimeType;
    }

    /**
     * Returns the deepFile type for this spotlight content type.
     * @return the {@link FileType}
     */
    FileType getType() {
      return fileType;
    }
  }

  /**
   * Registered metadata items and corresponding actions for metadata events.
   */
  enum Item {
    /** Date and time of the last change made to a metadata attribute. */
    AttributeChangeDate {
      @Override
      void parse(final DeepFile deepFile, final Object o) {
        if(check(o, Date.class)) deepFile.addMeta(
            MetaElem.DATETIME_ATTRIBUTE_MODIFIED,
            ParserUtil.convertDateTime((Date) o));
      }
    },

    /**
     * Title for the collection containing this item. This is analogous to a
     * record label or photo album.
     */
    Album {
      @Override
      public void parse(final DeepFile deepFile, final Object o) {
        if(check(o, String.class)) deepFile.addMeta(MetaElem.ALBUM, (String) o);
      }
    },

    /** Track number of a song or composition when it's part of an album. */
    AudioTrackNumber {
      @Override
      public void parse(final DeepFile deepFile, final Object o) {
        parseInt(deepFile, MetaElem.TRACK, o);
      }
    },

    /** The author of the contents of the file. */
    Authors {
      @Override
      void parse(final DeepFile deepFile, final Object o) {
        if(!check(o, String.class)) return;
        final String str = (String) o;
        if(EMLParser.isEmailAddress(str)) deepFile.addMeta(
            MetaElem.CREATOR_EMAIL, str);
        else deepFile.addMeta(MetaElem.CREATOR_NAME, str);
      }
    },

    /**
     * Identifies city of origin according to guidelines established by the
     * provider. For example, "New York", "Cupertino", or "Toronto".
     */
    City {
      @Override
      void parse(final DeepFile deepFile, final Object o) {
        if(check(o, String.class)) deepFile.addMeta(MetaElem.CITY, (String) o);
      }
    },

    /**
     * A comment related to the file. This comment is not displayed by the
     * Finder.
     */
    Comment {
      @Override
      public void parse(final DeepFile deepFile, final Object o) {
        if(check(o, String.class)) deepFile.addMeta(MetaElem.DESCRIPTION,
            (String) o);
      }
    },

    /** Composer of the song in the audio file. */
    Composer {
      @Override
      public void parse(final DeepFile deepFile, final Object o) {
        if(check(o, String.class)) deepFile.addMeta(MetaElem.COMPOSER,
            (String) o);
      }
    },

    /** The date and time that the content was created. */
    ContentCreationDate {
      @Override
      public void parse(final DeepFile deepFile, final Object o) {
        if(check(o, Date.class)) deepFile.addMeta(MetaElem.DATETIME_CREATED,
            ParserUtil.convertDateTime((Date) o));
      }
    },

    /**
     * Uniform Type Identifier of the file. For example, a jpeg image file will
     * have a value of public.jpeg.
     */
    ContentTypeTree {
      @Override
      public void parse(final DeepFile deepFile, final Object o) {
        try {
          String key = ((String) o).toUpperCase();
          key = key.replace('.', '_').replace('-', '_');
          final SpotlightContentType ct = SpotlightContentType.valueOf(key);
          final FileType ft = ct.getType();
          if(ft != null && !deepFile.isFileTypeSet()) deepFile.setFileType(ft);
          else {
            final MimeType mi = ct.getFormat();
            if(mi != null) deepFile.setFileFormat(mi);
          }
        } catch(final IllegalArgumentException ex) {
          Main.debug("SpotlightExtractor: unsupported ContentType found (%)",
              (String) o);
        }
      }
    },

    /**
     * Entity responsible for making contributions to the content of the
     * resource. Examples of a contributor include a person, an organization or
     * a service.
     */
    Contributors {
      @Override
      public void parse(final DeepFile deepFile, final Object o) {
        if(check(o, String.class)) deepFile.addMeta(MetaElem.CONTRIBUTOR,
            (String) o);
      }
    },

    /**
     * The full, publishable name of the country or primary location where the
     * intellectual property of the item was created, according to guidelines of
     * the provider.
     */
    Country {
      @Override
      public void parse(final DeepFile deepFile, final Object o) {
        if(check(o, String.class)) deepFile.addMeta(MetaElem.COUNTRY,
            (String) o);
      }
    },

    /** Description of the kind of item this file represents. */
    Description {
      @Override
      public void parse(final DeepFile deepFile, final Object o) {
        if(check(o, String.class)) deepFile.addMeta(MetaElem.DESCRIPTION,
            (String) o);
      }
    },

    /**
     * The duration, in seconds, of the content of the item. A value of 10.5
     * represents media that is 10 and 1/2 seconds long.
     */
    DurationSeconds {
      @Override
      public void parse(final DeepFile deepFile, final Object o) {
        parseDuration(deepFile, MetaElem.DURATION, o);
      }
    },

    /** Mac OS X Finder comments for this item. */
    FinderComment {
      @Override
      public void parse(final DeepFile deepFile, final Object o) {
        if(check(o, String.class)) deepFile.addMeta(MetaElem.COMMENT,
            (String) o);
      }
    },

    /** Group ID of the owner of the file. */
    FSOwnerGroupID {
      @Override
      void parse(final DeepFile deepFile, final Object o) {
        parseInt(deepFile, MetaElem.FS_OWNER_GROUP_ID, o);
      }
    },

    /** User ID of the owner of the file. */
    FSOwnerUserID {
      @Override
      public void parse(final DeepFile deepFile, final Object o) {
        parseInt(deepFile, MetaElem.FS_OWNER_USER_ID, o);
      }
    },

    /**
     * Publishable entry providing a synopsis of the contents of the item. For
     * example, "Apple Introduces the iPod Photo".
     */
    Headline {
      @Override
      public void parse(final DeepFile deepFile, final Object o) {
        if(check(o, String.class)) deepFile.addMeta(MetaElem.HEADLINE,
            (String) o);
      }
    },

    /**
     * Formal identifier used to reference the resource within a given context.
     * For example, the Message-ID of a mail message.
     */
    Identifier {
      @Override
      public void parse(final DeepFile deepFile, final Object o) {
        if(check(o, String.class)) deepFile.addMeta(MetaElem.IDENTIFIER,
            (String) o);
      }
    },

    /**
     * Keywords associated with this file. For example, "Birthday", "Important",
     * etc.
     */
    Keywords {
      @Override
      public void parse(final DeepFile deepFile, final Object o) {
        if(check(o, String.class)) deepFile.addMeta(MetaElem.KEYWORD,
            (String) o);
      }
    },

    /**
     * Indicates the languages used by the item. The recommended best practice
     * for the values of this attribute are defined by RFC 3066.
     */
    Languages {
      @Override
      public void parse(final DeepFile deepFile, final Object o) {
        if(check(o, String.class)) deepFile.addMeta(MetaElem.LANGUAGE,
            (String) o);
      }
    },

    /**
     * Date and time that the file was last used. This value is updated
     * automatically by LaunchServices everytime a file is opened by double
     * clicking, or by asking LaunchServices to open a file.
     */
    LastUsedDate {
      @Override
      public void parse(final DeepFile deepFile, final Object o) {
        if(check(o, Date.class)) deepFile.addMeta(MetaElem.DATETIME_LAST_USED,
            ParserUtil.convertDateTime((Date) o));
      }
    },

    /** Lyricist of the song in the audio file. */
    Lyricist {
      @Override
      public void parse(final DeepFile deepFile, final Object o) {
        if(check(o, String.class)) deepFile.addMeta(MetaElem.LYRICIST,
            (String) o);
      }
    },

    /**
     * Musical genre of the song or composition contained in the audio file. For
     * example: "Jazz", "Pop", "Rock", "Classical".
     */
    MusicalGenre {
      @Override
      public void parse(final DeepFile deepFile, final Object o) {
        String g = (String) o;
        try {
          if(g.charAt(0) == '(') g = g.substring(1, g.length() - 2);
          final int genreId = Integer.parseInt(g);
          deepFile.addMeta(MetaElem.GENRE, MP3Parser.getGenre(genreId));
        } catch(final NumberFormatException ex) {
          if(g.contains(",")) {
            final StringTokenizer tok = new StringTokenizer(g, ", ");
            while(tok.hasMoreTokens())
              deepFile.addMeta(MetaElem.GENRE, tok.nextToken());
          } else {
            deepFile.addMeta(MetaElem.GENRE, g);
          }
        }
      }
    },

    /** Number of pages in the document. */
    NumberOfPages {
      @Override
      public void parse(final DeepFile deepFile, final Object o) {
        parseInt(deepFile, MetaElem.NUMBER_OF_PAGES, o);
      }
    },

    /**
     * Height, in pixels, of the contents. For example, the image height or the
     * video frame height.
     */
    PixelHeight {
      @Override
      public void parse(final DeepFile deepFile, final Object o) {
        parseInt(deepFile, MetaElem.PIXEL_HEIGHT, o);
      }
    },

    /**
     * Width, in pixels, of the contents. For example, the image width or the
     * video frame width.
     */
    PixelWidth {
      @Override
      public void parse(final DeepFile deepFile, final Object o) {
        parseInt(deepFile, MetaElem.PIXEL_WIDTH, o);
      }
    },

    /**
     * Publishers of the item. For example, a person, an organization, or a
     * service.
     */
    Publisher {
      @Override
      public void parse(final DeepFile deepFile, final Object o) {
        if(check(o, String.class)) deepFile.addMeta(MetaElem.PUBLISHER,
            (String) o);
      }
    },

    /** Recipients of this item. */
    Recipients {
      @Override
      public void parse(final DeepFile deepFile, final Object o) {
        if(!check(o, String.class)) return;
        final String str = (String) o;
        if(EMLParser.isEmailAddress(str)) deepFile.addMeta(
            MetaElem.RECEIVER_EMAIL, str);
        else deepFile.addMeta(MetaElem.RECEIVER_NAME, str);
      }
    },

    /**
     * Recording date of the song or composition. This is in contrast to
     * kMDItemContentCreationDate which, could indicate the creation date of an
     * edited or "mastered" version of the original art.
     */
    RecordingDate {
      @Override
      public void parse(final DeepFile deepFile, final Object o) {
        if(check(o, Date.class)) deepFile.addMeta(MetaElem.DATETIME_CREATED,
            (String) o);
      }
    },

    /**
     * Title of the item. For example, this could be the title of a document,
     * the name of an song, or the subject of an email message.
     */
    Title {
      @Override
      public void parse(final DeepFile deepFile, final Object o) {
        if(check(o, String.class)) deepFile.addMeta(MetaElem.TITLE, (String) o);
      }
    };

    /**
     * Parses the data and fires parser events.
     * @param deepFile {@link DeepFile} to store the metadata information to
     * @param o the data to parse
     */
    abstract void parse(final DeepFile deepFile, final Object o);

    @Override
    public String toString() {
      return "kMDItem" + name();
    }

    /**
     * Checks the type of the given object.
     * @param o the object to check
     * @param c the data type
     * @return true if the object is an instance of the given class, false
     *         otherwise
     */
    boolean check(final Object o, final Class<?> c) {
      if(c.isInstance(o)) return true;
      Main.debug("SpotlightExtractor: wrong data type for attribute " +
          this.toString());
      return false;
    }

    /**
     * Converts the object to a Byte/Short/Integer/Long/Float or Double and adds
     * it to the metadata store.
     * @param deepFile the metadata store for the current file
     * @param elem the corresponding metadata element for this object
     * @param o the object to convert
     */
    void parseInt(final DeepFile deepFile, final MetaElem elem,
        final Object o) {
      final Long value = long0(o);
      if(value != null) deepFile.addMeta(elem, value);
    }

    /**
     * Converts the object to a Duration and adds it to the metadata store.
     * @param ms the metadata store for the current file
     * @param e the corresponding metadata element for this object
     * @param o the object to convert
     */
    void parseDuration(final DeepFile ms, final MetaElem e,
        final Object o) {

      final Long value = long0(o);
      if(value != null) ms.addMeta(e,
          ParserUtil.convertMsDuration((int) (value * 1000)));
    }

    /**
     * Returns the long value for an object.
     * @param o the object to parse
     * @return the long value or {@code null} if the object can't be
     *         parsed
     */
    private Long long0(final Object o) {
      long value;
      // most objects will be Integer, Long or Double
      if(o instanceof Integer) value = (Integer) o;
      else if(o instanceof Double) value = ((Double) o).longValue();
      else if(o instanceof Long) value = (Long) o;
      else if(o instanceof Short) value = (Short) o;
      else if(o instanceof Byte) value = (Byte) o;
      else if(o instanceof Float) value = ((Float) o).longValue();
      else if(o instanceof String) {
        final byte[] a = Token.token((String) o);
        int i = 0;
        final int len = a.length;
        while(i < len && a[i] >= '0' && a[i] <= '9')
          i++;
        value = Token.toLong(a, 0, i);
        if(value == Long.MIN_VALUE) {
          Main.debug("SpotlightExtractor: invalid value for int field: %",
              (String) o);
          return null;
        }
      } else {
        Main.debug("SpotlightExtractor: unsupported data type: %",
            o.getClass().getName());
        return null;
      }
      return value;
    }

    /**
     * Returns the enum constant for the string. Use this method instead of
     * {@link #valueOf(String)}!
     * @param n the name of the constant
     * @return the enum instance
     */
    public static Item getValue(final String n) {
      return valueOf(n.substring(7));
    }
  }

  /**
   * Constructor.
   * @throws ParserException if the spotex library is not available
   */
  public SpotlightExtractor() throws ParserException {
    if(!LIBAVAILABLE)
      throw new ParserException("Spotex library not available.");
  }

  @Override
  public boolean check(final DeepFile deepFile) {
    return true;
  }

  @Override
  public void extract(final DeepFile deepFile) {
    final String fileName = deepFile.getBufferedFileChannel().getFileName();
    final Map<String, Object> metadata = getMetadata(fileName);
    if(metadata == null) return;
    for(final Entry<String, Object> e : metadata.entrySet()) {
      try {
        final Item item = Item.getValue(e.getKey());
        final Object val = e.getValue();
        if(val instanceof Object[]) {
          for(final Object o : (Object[]) val)
            item.parse(deepFile, o);
        } else item.parse(deepFile, val);
      } catch(final IllegalArgumentException ex) {
        // item is not in enum ...do nothing
      }
    }
  }

  @Override
  public void propagate(final DeepFile deepFile) {
    Main.notimplemented();
  }

  /**
   * Native method for retrieving all available metadata for a file.
   * @param filename the path to the file
   * @return map containing the queried metadata attributes or {@code null}
   *         if any error occurs
   */
  private native Map<String, Object> getMetadata(final String filename);
}
