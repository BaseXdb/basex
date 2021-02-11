# Basic example. Add and replace resources in BaseX.
# Works with BaseX 8.0 and later
#
# Documentation: https://docs.basex.org/wiki/Clients
#
# (C) Ben Engbers

# This example requires a running database server instance.
# Documentation: https://docs.basex.org/wiki/Clients

# @author BaseX Team 2005-21, BSD License

source("RbaseXClient.R")

library(RCurl)

input_to_raw <- function(input) { # Utility function
  if (file.exists(input)) {
    finfo <- file.info(input)
    toread <- file(input, "rb")
    raw_input <- readBin(toread, what = "raw", size = 1, n = finfo$size)
    close(toread)
  } else if (url.exists(input)) {
    get <- getURL(input)
    raw_input <- charToRaw(get)
  } else {
    raw_input <- charToRaw(input)
  }
  return(raw_input)
}

Session <- BasexClient$new("localhost", 1984, "admin", "admin")
test <- Session$command("Open Learn")
if (!test$success) {
  test <- Session$create("Learn", "<xml>Create test1</xml>")
  if (!test$success) {cat("Could not create database\n")}
}

# Delete all previous resources
Session$command("delete /")

Path1   <- "world/world.xml"
Simple1 <- "<x>Hello World!</x>"
Path2   <- "universe.xml"
Simple2 <- input_to_raw("<x>Dag vrienden!</x>")
Replace <- "<x>Hallo Freunde!</x>"
Path3   <- "File.xml"
Simple3 <- input_to_raw("https://raw.githubusercontent.com/BaseXdb/basex/master/basex-api/src/test/resources/first.xml")

Session$add(path = Path1, Simple1)
Session$add(path = Path2, Simple2)
print(Session$command("xquery collection('Learn')")$result)

Session$replace(path = Path2, Replace)
print(Session$command("xquery collection('Learn')")$result)

Session$add(path = Path3, Simple3)
print(Session$command("xquery collection('Learn')")$result)
