setwd("~/DataScience/RBaseX")

source("RbaseXClient.R")

BasexClient$undebug("void_send")

input_to_raw <- function(input) {
  if (dir.exists(input)) { 
    cat("Verwerk bestanden uit directory")
  } else if (file.exists(input)) {
    finfo <- file.info(input)
    toread <- file(input, "rb")
    raw_input <- readBin(toread, what = "raw", size = 1, n = finfo$size)
    close(toread)
  } else {
    raw_input <- charToRaw(input)
  }
  return(raw_input)
}

Session <- BasexClient$new("localhost", 1984, "admin", "admin")
test <- Session$command("Open Learn")
print(test$success)
if (!test$success) {
  test <- Session$create("Learn", "<xml>Create test1</xml>")
  if (!test$success) {cat("Could not create database\n")}
}

Path1 <- "Test1"
Path2 <- "test/Test2.xml"
Path3 <- "h-tk-20162017-60-7.xml"
Simple1 <- "<x>Hello World!</x>"
Replace1 <- "<x>Hallo Freunde!</x>"
Simple2 <- "/home/bengbers/DataScience/RBaseX/xml-files/Test2.xml"
Simple3 <- "/home/bengbers/DataScience/RBaseX/xml-files/h-tk-20162017-60-7.xml"

print(Session$command("xquery collection('Learn')")$result)
#print(q$result)

input1 <- input_to_raw(Simple1) 
Replace <- input_to_raw(Replace1) 
input2 <- input_to_raw(Simple2) 
input3 <- input_to_raw(Simple3) 
Session$add(path = Path1, input1)
print(Session$command("xquery collection('Learn')")$result)
Session$replace(path = Path1, Replace)
Session$add(path = Path2, input2)
#print(Session$command("xquery collection('Learn')")$result)
#Session$add(path = Path3, input3)
print(Session$command("xquery collection('Learn')")$result)

