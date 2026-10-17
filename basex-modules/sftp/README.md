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

The version matching your BaseX release is retrieved from the BaseX Maven repository:

```sh
basex -c "repo install sftp"
```

### Building from source

The module is built with BaseX: `mvn install` in the repository root (or `mvn install -pl basex-modules/sftp` for the module only) compiles the module, runs the tests, and leaves the module jar in the `target` folder. The jar contains all required dependencies and can be installed with `repo install <jar>`.

License
-------

The SFTP Module is provided under the BSD 3-Clause License. It depends on BaseX and JSch, the latter transitively adding further dependencies.

An overview of the licenses of these components is available in [LICENSE.txt].

[JSch]: http://www.jcraft.com/jsch/
[SFTP Functions]: https://docs.basex.org/13/SFTP_Functions
[LICENSE.txt]: src/main/resources/LICENSE.txt
