package org.basex.uri;


import org.basex.api.client.LocalQuery;
import org.basex.api.client.LocalSession;
import org.basex.core.Context;
import org.basex.core.uri.BaseXURIResolver;
import org.basex.io.IO;
import org.basex.io.IOStream;
import org.junit.Test;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class BaseXURIResolverTest {


    @Test
    public void testImportFromClasspath() throws IOException {
        Context context = new Context();
        context.setBaseXURIResolver(new BaseXURIResolver() {

            @Override
            public IO resolve(String path, String uri) {
                InputStream in = getClass().getResourceAsStream("/" + path);
                return new IOStream(in);
            }
        });
        assertEquals("hello world", runXQuery(context, "/hello.xq"));
    }

    private String runXQuery(Context context, String xqueryClassPath) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        LocalSession session = new LocalSession(context, out);
        String xqueryCode = inputStreamToString(getClass().getResourceAsStream(xqueryClassPath), StandardCharsets.UTF_8);
        LocalQuery query = session.query(xqueryCode);
        assertNull(query.next());
        return new String(out.toByteArray());
    }

    private static String inputStreamToString(final InputStream is, Charset charset) throws IOException {
        int bufferSize = 512;
        final char[] buffer = new char[bufferSize];
        final StringBuilder out = new StringBuilder();
        Reader in = new InputStreamReader(is, charset);
            for (;;) {
                int rsz = in.read(buffer, 0, buffer.length);
                if (rsz < 0)
                    break;
                out.append(buffer, 0, rsz);
            }
        return out.toString();
    }
}
