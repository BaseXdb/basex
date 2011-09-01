package org.basex.api.jaxrx;

import static org.basex.core.Text.*;
import static org.basex.util.Token.*;
import static org.jaxrx.core.JaxRxConstants.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;

import javax.ws.rs.core.StreamingOutput;

import org.basex.core.BaseXException;
import org.basex.core.cmd.Delete;
import org.basex.core.cmd.DropDB;
import org.basex.core.cmd.List;
import org.basex.core.cmd.ListDB;
import org.basex.core.cmd.Open;
import org.basex.io.IO;
import org.basex.io.out.ArrayOutput;
import org.basex.io.serial.Serializer;
import org.basex.io.serial.SerializerProp;
import org.basex.server.Session;
import org.basex.util.Table;
import org.basex.util.Token;
import org.basex.util.Util;
import org.basex.util.list.TokenList;
import org.jaxrx.JaxRx;
import org.jaxrx.core.JaxRxException;
import org.jaxrx.core.QueryParameter;
import org.jaxrx.core.ResourcePath;

/**
 * This class offers an implementation of the JAX-RX interface.
 * It contains all methods which are necessary to answer GET, POST, PUT
 * and DELETE requests via JAX-RX.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 * @author Lukas Lewandowski
 */
public final class BXJaxRx implements JaxRx {
  @Override
  public HashSet<QueryParameter> getParameters() {
    final HashSet<QueryParameter> p = new HashSet<QueryParameter>();
    p.add(QueryParameter.COMMAND);
    p.add(QueryParameter.QUERY);
    p.add(QueryParameter.RUN);
    p.add(QueryParameter.COUNT);
    p.add(QueryParameter.OUTPUT);
    p.add(QueryParameter.START);
    p.add(QueryParameter.WRAP);
    p.add(QueryParameter.VAR);
    return p;
  }

  @Override
  public StreamingOutput get(final ResourcePath path) {
    return new BXOutput(path) {
      @Override
      String code() throws IOException {
        final Serializer xml = Serializer.get(out,
            new SerializerProp(serial(path)));

        if(path.getDepth() != 0) {
          final String all = path.getResourcePath();

          final Table table = new Table(exec(new ListDB(all), null));
          xml.openElement(token(JAXRX + ":" + "database"),
            token("name"), token(path.getResource(0)),
            token("documents"), token(table.contents.size()));

          for(final TokenList l : table.contents) {
            xml.emptyElement(token(JAXRX + ":" + "document"),
                token("path"), l.get(0), token("type"), l.get(1),
                token("size"), l.get(2));
          }
          xml.closeElement();
        } else {
          // retrieve list of databases
          final Table table = new Table(exec(new List(), null));
          for(final TokenList l : table.contents) {
            xml.emptyElement(token(JAXRX + ":" + "database"),
                token("name"), l.get(0), token("documents"), l.get(1),
                token("size"), l.get(2));
          }
        }
        xml.close();
        return null;
      }
    };
  }

  @Override
  public StreamingOutput query(final String query, final ResourcePath path) {
    return new BXOutput(path) {
      @Override
      String code() {
        return query(query);
      }
    };
  }

  @Override
  public StreamingOutput run(final String file, final ResourcePath path) {
    return new BXOutput(path) {
      @Override
      String code() {
        // get root directory for files
        final String root = System.getProperty(JaxRxServer.JAXRXPATH) + '/';
        final IO io = IO.get(root + file.trim());

        // file not found...
        if(!io.exists()) {
          throw new JaxRxException(404, Util.info(FILEWHICH, file));
        }

        try {
          // perform query
          return query(string(io.content()));
        } catch(final IOException ex) {
          // file could not be opened for some other reason...
          throw new JaxRxException(400, ex.getMessage());
        }
      }
    };
  }

  @Override
  public StreamingOutput command(final String cmd, final ResourcePath path) {
    return new BXOutput(path) {
      @Override
      String code() throws IOException {
        // perform command
        final ArrayOutput ao = new ArrayOutput();
        exec(cmd, ao);

        // serialize output and remove carriage returns
        final Serializer xml =
          Serializer.get(out, new SerializerProp(serial(path)));
        xml.text(Token.delete(ao.toArray(), '\r'));
        xml.close();
        return session.info();
      }
    };
  }

  @Override
  public String add(final InputStream input, final ResourcePath path) {
    return new BXCode(path) {
      @Override
      String code() {
        // open database
        try {
          session.execute(new Open(db(path)));
        } catch(final BaseXException ex) {
          throw new JaxRxException(404, ex.getMessage());
        }
        add(input, path, session);
        return session.info();
      }
    }.run();
  }

  /**
   * Adds a document to the database.
   * @param input input stream
   * @param path resource path
   * @param session session
   */
  void add(final InputStream input, final ResourcePath path,
      final Session session) {

    final int d = path.getDepth();
    final String name = path.getResource(d - 1);
    final StringBuilder target = new StringBuilder();
    for(int i = 1; i < d - 1; i++) {
      target.append('/').append(path.getResource(i));
    }
    try {
      session.add(name, target.toString(), input);
    } catch(final BaseXException ex) {
      throw new JaxRxException(400, ex.getMessage());
    }
  }

  @Override
  public String update(final InputStream input, final ResourcePath path) {
    return new BXCode(path) {
      @Override
      String code() {
        try {
          final int d = path.getDepth();
          // create new database
          if(d == 1) {
            session.create(db(path), input);
          } else {
            // add document to database
            session.execute(new Open(db(path)));
            final StringBuilder target = new StringBuilder();
            for(int i = 1; i < d; i++) {
              target.append('/').append(path.getResource(i));
            }
            session.execute(new Delete(target.toString()));
            add(input, path, session);
          }
          return session.info();
        } catch(final BaseXException ex) {
          // return exception if process failed
          throw new JaxRxException(400, ex.getMessage());
        }
      }
    }.run();
  }

  @Override
  public String delete(final ResourcePath path) {
    return new BXCode(path) {
      @Override
      String code() {
        final boolean root = path.getDepth() == 1;
        try {
          if(root) {
            session.execute(new DropDB(db(path)));
          } else {
            session.execute(new Open(db(path)));
            session.execute(new Delete(path(path)));
          }
          return session.info();
        } catch(final BaseXException ex) {
          // return exception if process failed
          if(root) throw new JaxRxException(ex);
          throw new JaxRxException(404, ex.getMessage());
        }
      }
    }.run();
  }
}
