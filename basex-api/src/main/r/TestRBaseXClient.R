source("RbaseXClient.R")

BasexClient$undebug("execute")

sink("RBaseX.txt")
# Test 'execute'
Session <- BasexClient$new("localhost", 1984, "admin", "admin")
print(Session$execute("info"))
print(Session$execute("xquery 1 to 5"))
print(Session$execute("list"))
Session$create("test1")
Session$execute("DROP DB test1")
print(Session$execute("list"))
Session$create("test1", "<xml>Create test1</xml>")
print(Session$execute("list"))
Session$execute("DROP DB test1")
print(Session$execute("list"))

# Test 'query'
query1 <- "for $i in 1 to 2 return <xml>Text { $i }</xml>"
query1 <- Session$query(query1)
print(query1)
print(query1$options())
print(query1$execute())
print(query1$info())
print(query1$updating())
cat("Query1, output from full()", "\n")
print(query1$full())
query1$close()

query2 <- "for $i in 3 to 4 return <xml>Text { $i }</xml>"
query2 <- Session$query(query2)
print(query2)
while (query2$more()) {
  cat(query2$next_row(), "\n")
}
query2$close()

query3 <- "declare variable $name external; for $i in 5 to 6 return element { $name } { $i }"
query3 <- Session$query(query3)
query3$bind("$name", "number", "")
print(query3)
print(query3$execute())
print(query3$info())

sink()
