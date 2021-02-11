# Basic example. Show 2 different ways to create a database.
# Works with BaseX 8.0 and later
#
# Documentation: https://docs.basex.org/wiki/Clients
#
# (C) Ben Engbers

# This example requires a running database server instance.
# Documentation: https://docs.basex.org/wiki/Clients

# @author BaseX Team 2005-21, BSD License

source("RbaseXClient.R")

Session <- BasexClient$new("localhost", 1984, "admin", "admin")

test <- Session$command("OPEN test1")
if (!test$success) {
  test <- Session$create("test1", "<xml>Create test1</xml>")
  if (test$success) {cat( test$info, "\n")
  } else {
    cat("Could not create database\n")
  }
}

Session$create("test2")

print(Session$command("list")$result)

Session$command("DROP DB test2")
Session$command("DROP DB test1")
