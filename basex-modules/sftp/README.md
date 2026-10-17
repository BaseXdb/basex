BaseX SFTP Module
-----------------

The BaseX SFTP Module provides functions for interacting with remote SFTP servers from XQuery, built on [JSch]. A session is opened with `sftp:connect`, which returns an opaque session handle used by subsequent functions, and closed with `sftp:disconnect`.

Example
-------

Open a session, list a directory, read a text file, and close the session:

```xquery
import module namespace sftp = 'http://basex.org/modules/sftp';

let $session := sftp:connect('sftp.example.org', 'user', 'password')
let $entries := sftp:list($session, '/home/user')
let $text    := sftp:get-text($session, '/home/user/readme.txt')
return (
  sftp:disconnect($session),
  $text
)
```

Supported Functions
-------------------

For details of the functions supported by this module, see [SFTP Functions] in the BaseX documentation.

Installation
------------

### Using a pre-built release

Pre-built module jars are available at:
https://files.basex.org/modules/sftp/

Download the jar matching your BaseX release and install it with:

```sh
basex -c "repo install <jar>"
```

where `<jar>` must be replaced by the path to the module jar.

### Building from source

The prerequisites for building the module are a JDK 21 and Maven.

Use the SFTP Module release whose version matches the target BaseX version. For building the module, check out the source code and run

```sh
mvn clean install
```

This will compile the module and leave the module jar in the `target` folder, as well as in the local Maven repository at `~/.m2/repository/org/basex/modules/modules-sftp`.

The module jar contains all required dependencies.

To install the built jar, use the `repo install` command shown above, replacing `<jar>` with the path to the generated file.

License
-------

The SFTP Module is provided under the BSD 3-Clause License. It depends on BaseX and JSch, the latter transitively adding further dependencies.

An overview of the licenses of these components is available in [LICENSE.txt].

[JSch]: http://www.jcraft.com/jsch/
[SFTP Functions]: https://docs.basex.org/13/SFTP_Functions
[LICENSE.txt]: src/main/resources/LICENSE.txt
