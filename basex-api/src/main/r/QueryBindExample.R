# Basic example on how to use 'bind'.
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

query <- "declare variable $name external; for $i in 1 to 10 return element { $name } { $i }"
query <- Session$query(query)$query
query$bind("$name", "number", "")
print(query)
print(query$execute())
print(query$info())

query$close()

