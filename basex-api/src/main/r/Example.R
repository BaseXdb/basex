# Basic example. Show info and execute simple commands.
# Works with BaseX 8.0 and later
#
# Documentation: https://docs.basex.org/wiki/Clients
#
# (C) Ben Engbers

source("RbaseXClient.R")

Session <- BasexClient$new("localhost", 1984, "admin", "admin")
print(Session$command("info")$result)

system.time(print(Session$command("xquery 1 to 5")$result))