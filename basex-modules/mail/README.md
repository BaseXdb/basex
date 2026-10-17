BaseX Mail Module
-----------------

The BaseX Mail Module provides functions for sending e-mail from XQuery, built on [Apache Commons Email]. Messages are described by a map that carries both the SMTP connection settings and the message.

Example
-------

Send a plain-text e-mail via an authenticated SMTP server:

```xquery
import module namespace mail = 'http://basex.org/modules/mail';

mail:send({
  'host': 'smtp.your-mail-provider.org',
  'ssl': true(),
  'user': 'from@your-mail-provider.org',
  'password': '...',
  'from': 'from@your-mail-provider.org',
  'to': 'to@your-mail-recipient.org',
  'subject': 'Subject',
  'message': 'Message'
})
```

Supported Functions
-------------------

For details of the functions supported by this module, see [Mail Functions] in the BaseX documentation.

Installation
------------

The version matching your BaseX release is retrieved from the BaseX Maven repository:

```sh
basex -c "repo install mail"
```

### Building from source

The module is built with BaseX: `mvn install` in the repository root (or `mvn install -pl basex-modules/mail` for the module only) compiles the module, runs the tests, and leaves the module jar in the `target` folder. The jar contains all required dependencies and can be installed with `repo install <jar>`.

License
-------

The Mail Module is provided under the BSD 3-Clause License. It depends on BaseX and Apache Commons Email, the latter transitively adding further dependencies.

An overview of the licenses of these components is available in [LICENSE.txt].

[Apache Commons Email]: https://commons.apache.org/proper/commons-email/
[Mail Functions]: https://docs.basex.org/13/Mail_Functions
[LICENSE.txt]: src/main/resources/LICENSE.txt
