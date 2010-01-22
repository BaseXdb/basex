package org.basex.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.basex.build.MemBuilder;
import org.basex.build.Parser;
import org.basex.core.BaseXException;
import org.basex.core.Context;
import org.basex.core.Main;
import org.basex.core.Prop;
import org.basex.core.proc.XQuery;
import org.basex.data.Data;
import org.basex.io.IO;
import org.basex.io.IOFile;
import org.basex.io.IOUrl;

/**
 * This class reads the version number from the maven property file.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Michael Seiferle
 * @author Bastian Lemke
 */
public final class ReadVersion {

  /** Hidden constructor. */
  private ReadVersion() { }

  /**
   * Returns the version number that is set in the maven property file.
   * @return the BaseX version.
   */
  public static String read() {
    Parser p;
    try {
      final File f = new File("pom.xml");
      final IO io;
      if(!f.exists()) { // jar file
        final URL url = ReadVersion.class.getResource(
            "/META-INF/maven/org.basex/basex/pom.xml");
        if(url != null) io = new IOUrl(url.toString());
        else return "unknown";

      } else io = new IOFile(f);
      p = Parser.xmlParser(io, new Prop(false));
      Data data = new MemBuilder(p).build();
      Context ctx = new Context();
      ctx.openDB(data);
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      new XQuery("declare default element namespace"
          + "\"http://maven.apache.org/POM/4.0.0\";"
          + "/project/version/text()").execute(ctx, baos);
      baos.flush();
      return baos.toString();
    } catch(IOException e) {
      Main.notexpected(e);
    } catch(BaseXException e) {
      Main.notexpected(e);
    }
    return "unknown";
  }
}
