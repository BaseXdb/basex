source("RbaseXClient.R")

BasexClient$undebug("void_send")

# Test 'command'
Session <- BasexClient$new("localhost", 1984, "admin", "admin")
test <- Session$command("OPEN test1")
if (!test$success) {
  test <- Session$create("test1", "<xml>Create test1</xml>")
  if (test$success) {cat( test$info, "\n")
  } else {
    cat("Could not create database\n")
  }
}
cat("Create database \"test2\" with empty resources\n")
Session$create("test2")
print(Session$command("list")$result)
print(Session$command("xquery 1 to 5")$result)
Session$command("CLOSE")
Session$command("DROP DB test2")
Session$command("DROP DB test1")

# Test 'query'
cat("Test Query1\n\n")
query1 <- "for $i in 1 to 2 return <xml>Text { $i }</xml>"
query1 <- Session$query(query1)

cat("query1$query$options():", query1$query$options(), sep = " ", "\n")
cat("query1$query$execute(): ")
print(query1$query$execute())
cat("query1$query$info():", query1$query$info(), sep = " ", "\n")
cat("query1$query$updating():", query1$query$updating(), sep = " ", "\n")
cat("query1$query$full(): ")
print(query1$query$full())
if (query1$query$close()) {
  cat("query1 closed\n")
} else {
  cat("query1 could not be closed")}
cat("\n")

cat("Test Query2\n\n")

query2 <- "for $i in 3 to 4 return <xml>Text { $i }</xml>"
query2 <- Session$query(query2)$query
print(query2)
while (query2$more()) {
  cat(query2$next_row(), "\n")
}
query2$close()

cat("Test Query3\n\n")
query3 <- "declare variable $name external; for $i in 5 to 6 return element { $name } { $i }"
query3 <- Session$query(query3)$query
query3$bind("$name", "number", "")
print(query3)
print(query3$execute())
print(query3$info())
