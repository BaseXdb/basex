# Basic example. Show 3 different ways to execute a query.
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

query <- "for $i in 1 to 2 return <xml>Text { $i }</xml>"
query <- Session$query(query)

# Execute query in one execute()-command
cat("query$query$execute(): ")
print(query$query$execute())

# Execute query in one full()-command
cat("query$query$full(): ")
print(query$query$full())

if (query$query$close()) {
  cat("query closed\n")
} else {
  cat("query could not be closed")}
cat("\n")

# Iterate over query
query2 <- "for $i in 3 to 4 return <xml>Text { $i }</xml>"
query2_as_attribute <- Session$query(query2)$query   # <== Alternative call to query-object  
while (query2_as_attribute$more()) {
  cat(query2_as_attribute$next_row(), "\n")
}

query2_as_attribute$close()
