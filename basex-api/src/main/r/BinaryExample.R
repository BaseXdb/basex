# Basic example. Store and retrieve binary data.
# Works with BaseX 8.0 and later
#
# Documentation: https://docs.basex.org/main/Clients
#
# (C) Ben Engbers

# This example requires a running database server instance.
# Documentation: https://docs.basex.org/main/Clients

# @author BaseX Team, BSD License

source("RbaseXClient.R")

library(dplyr)

Session <- BasexClient$new("localhost", 1984, "admin", "admin")

Session$command("DROP DB binBase")

testBin <- Session$command("OPEN binBase")
if (!testBin$success) {
  testBin <- Session$create("binBase", "<xml>Create binBase</xml>")
  if (testBin$success) {cat( testBin$info, "\n")
  } else {
    cat("Could not create binBase\n")
  }
}
print(Session$command("LIST")$result)

Session$command("delete /")

bais <- raw()
for (b in 252:256) bais <- c(bais, c(b)) %>% as.raw()

test <- Session$put_binary("test.bin", bais)
print(test$success)

baos <- Session$command("binary get test.bin")
print(bais)
print(baos$result)

Session$command("CLOSE")
