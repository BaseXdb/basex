BaseX Mail Module
-----------------

The BaseX Mail Module provides functions for sending e-mail from XQuery, built on [Apache Commons Email]. Messages are described by an XML element that carries both the SMTP connection settings and the message body.

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

### Using a pre-built release

Pre-built module jars are available at:
https://files.basex.org/modules/mail/

Download the jar matching your BaseX release and install it with:

```sh
basex -c "repo install <jar>"
```

where `<jar>` must be replaced by the path to the module jar.

### Building from source

The prerequisites for building the module are a JDK 21 and Maven.

Use the Mail Module release whose version matches the target BaseX version. For building the module, check out the source code and run

```sh
mvn clean install
```

This will compile the module and leave the module jar in the `target` folder, as well as in the local Maven repository at `~/.m2/repository/org/basex/modules/modules-mail`.

The module jar contains all required dependencies.

To install the built jar, use the `repo install` command shown above, replacing `<jar>` with the path to the generated file.

License
-------

The Mail Module is provided under the BSD 3-Clause License. It depends on BaseX and Apache Commons Email, the latter transitively adding further dependencies.

An overview of the licenses of these components is available in [LICENSE.txt].

[Apache Commons Email]: https://commons.apache.org/proper/commons-email/
[Mail Functions]: https://docs.basex.org/13/Mail_Functions
[LICENSE.txt]: src/main/resources/LICENSE.txt
