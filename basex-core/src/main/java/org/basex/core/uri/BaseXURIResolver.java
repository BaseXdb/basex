package org.basex.core.uri;


import org.basex.io.IO;

public interface BaseXURIResolver {
    IO resolve(String path, String uri);
}
