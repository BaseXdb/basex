BaseX Tests
-----------

This project contains tests that are used in BaseX development, but are not part of the main build because they run for a long time, are still in the making, or are broken.

Running the Tests
-----------------

The project is not a module of the root POM. Install the main modules first, and then run the tests from the root directory:

    mvn install -DskipTests
    mvn -f basex-tests/pom.xml test

Feedback
--------

For questions or feedback, please use our [issue tracker](https://github.com/BaseXdb/basex/issues) or our [mailing list](https://basex.org/open-source/).
