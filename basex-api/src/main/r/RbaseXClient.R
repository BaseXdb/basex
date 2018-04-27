# R client for BaseX.
# Works with BaseX 8.0 and later
#
# Documentation: http://docs.basex.org/wiki/Clients
#
# (C) Ben Engbers

library(utils)
library(R6)
library(openssl)
library(dplyr)
library(purrr)
library(stringr)
library(magrittr)

BasexClient <- R6Class("BasexClient",
  public = list(
    initialize = function(host, port, username, password) {
      private$sock <- socketConnection(host = "localhost", port = 1984L, 
                                    open = "w+b", server = FALSE, blocking = TRUE, 
                                    encoding = "utf-8")
      private$response <- self$str_receive()
      splitted <-strsplit(private$response, "\\:")
      ifelse(length(splitted[[1]]) > 1,
             { code <- paste(username, splitted[[1]][1],password, sep=":")
             nonce <- splitted[[1]][2]
             },
             { code <- password
             nonce <- splitted[[1]][1]
             }
      )
      code <- md5(paste(md5(code), nonce, sep = ""))
      class(code) <- "character"
      private$void_send(username)
      private$void_send(code)
      if (!self$bool_test_sock()) stop("Access denied")},
    command = function(command = command) {
      private$void_send(command)
      private$result <- self$str_receive()
      private$info <-   self$str_receive()
      if (length(private$info) > 0) cat(private$info, "\n")
      return(list(result = private$result %>% strsplit("\n", fixed = TRUE), 
                  info = private$info, 
                  success = self$bool_test_sock()))
    },
    query = function(query = query) {
      return(list(query = Query$new(query, private$get_sock()), success = self$bool_test_sock()))
      },
    create = function(name = name, input = input) {
      if (missing(input)) input <- ""
      writeBin(as.raw(0x08), private$sock)
      private$void_send(name)
      private$void_send(input)
#      writeBin(private$raw_terminated_string(name), private$sock)
#      writeBin(private$raw_terminated_string(input), private$sock)
      private$info <- self$str_receive()
      return(list(info = private$info, success = self$bool_test_sock()))
    },
    add = function(path = path, input = input) {
      writeBin(as.raw(0x09), private$sock)
      private$void_send(path)
      private$void_send(input)
      private$info <- self$str_receive()
      return(list(info = private$info, success = self$bool_test_sock()))
    },
    replace = function(path = path, input = input) {
      writeBin(as.raw(0x0C), private$sock)
      private$void_send(path)
      private$void_send(input)
      private$info <- self$str_receive()
      return(list(info = private$info, success = self$bool_test_sock()))
    },
    store = function(path = path, input = input) {
      writeBin(as.raw(0x0D), private$sock)
      private$void_send(path)
      private$void_send(input)
      private$info <- self$str_receive()
    },
    
    bool_test_sock = function(socket) {
      if (missing(socket)) socket <- private$get_sock()
      test <- readBin(socket, what = "raw", n =1)
      return(test == 0x00)
    },
    finalize = function() {
      private$close_sock()},
    print = function(...) {
      cat("Socket: ", private$get_sock(), "\n", sep = "")
      invisible(self)},
    str_receive = function(input, output) {
      if (missing(input)) input   <- private$get_sock()
      if (missing(output)) output <- raw(0)
      while ((rd <- readBin(input, what = "raw", n =1)) > 0) {
        if (rd == 0xff) next
        output <- c(output, rd)
      }
      ret <- rawToChar(output)
      return(ret)},
    term_string = function(string) {
      return(charToRaw(string) %>% append(0) %>% as.raw())}
  ),
  
  private = list(
    result = NULL,
    info = NULL,
    sock = NULL,
    response = NULL,
    get_sock = function() { private$sock },
    close_sock = function() { close(private$sock)},
    void_send = function(input) {
      if (class(input) == "character") {
        streamOut <- charToRaw(input)
      } else {
        rd_id <- 1
        end <- length(input)
        streamOut <- raw()
        while (rd_id <= end) { 
          rd <- c(input[rd_id])
          if (rd == 255 || rd == 0) streamOut <- c(streamOut, c(0x00))
          rd_id <- rd_id + 1
          streamOut <- c(streamOut, rd)
        }
      }
      streamOut <- c(streamOut, c(0x00)) %>% as.raw()
      writeBin(streamOut, private$get_sock())
      
    }
  )
)

Query <- R6Class("Query",
  inherit = BasexClient,
  
  public = list(
    str_id = NULL,
    raw_id = NULL,
    initialize = function(query, sock) {  
      private$sock <- sock
      out_stream <- super$get_sock()
      writeBin(as.raw(0x00), out_stream)
      super$void_send(query)
      self$str_id <- super$str_receive()
      self$raw_id <- super$term_string(self$str_id)},
    close = function() { 
      private$req_exe(0x02, self$raw_id)
      if (!private$req_success) cat("Query \'", self$str_id, "\' could not be closed.", "\n")
      return(private$req_success)
    },
    bind = function(name, value, type) {
      socket <- super$get_sock()
      if (missing(type)) type = ""
      private$write_code_ID(0x03, self$raw_id)
      name  %>% charToRaw() %>% append(0) %>% as.raw() %>% writeBin(socket)
      value %>% charToRaw() %>% append(0) %>% as.raw() %>% writeBin(socket)
      type  %>% charToRaw() %>% append(0) %>% as.raw() %>% writeBin(socket)
      private$req_result <- super$str_receive()
      private$req_success <- super$bool_test_sock()
      return(private$req_success)
    },
    execute = function() {
      private$req_exe(0x05, self$raw_id)
      result <- private$req_result %>% private$clean()
      return(result)
    },  
    more = function() {
      if (is.null(private$cache)) {
        in_stream <- out_stream <- super$get_sock()
        writeBin(as.raw(0x04), out_stream)
        writeBin(self$raw_id, out_stream)
        cache <- c()
        while ((rd <- readBin(in_stream, what = "raw", n =1)) > 0) {
          cache <- c(cache, as.character(rd))
          cache <- c(cache, super$str_receive())
        }
        private$req_success <- super$bool_test_sock()
        private$cache <- cache
        private$pos <- 0
      }
      if ( length(private$cache) > private$pos) return(TRUE)
      else { 
        private$cache <- NULL
        return(FALSE) 
      }},
    next_row = function() {      
      if (self$more()) {
        private$pos <- private$pos + 1
        result <- private$cache[private$pos]
      }
      return(result)},
    info = function() { 
      private$req_exe(0x06, self$raw_id)
      result <- private$req_result %>% private$clean()
      return(result)},  
    options = function() { 
      private$req_exe(0x07, self$raw_id)
      res <- private$req_result 
      res <- ifelse(length(private$req_result) > 1,
      private$req_result %>% private$clean(), "No options set")},  
    updating = function() { 
      private$req_exe(0x1E, self$raw_id)
      result <- private$req_result %>% as.logical()
      return(result)},  
    full = function() { 
      in_stream <- out_stream <- super$get_sock()
      writeBin(as.raw(0x1F), out_stream)
      writeBin(self$raw_id, out_stream)
      cache <- c()
      while ((rd <- readBin(in_stream, what = "raw", n =1)) > 0) {
        cache <- c(cache, as.character(rd))
        cache <- c(cache, super$str_receive())
      }
      private$req_success <- super$bool_test_sock()
      result <- cache
      return(result)},  
  
    print = function(...) {
      cat("Query-ID: ", self$str_id, "\n", sep = "")
      invisible(self)}
  ),
  private = list(
    cache = NULL,
    pos = NULL,
    req_result = NULL,
    req_success = NULL,
    write_code_ID = function(id_code, arg) {
      out_stream <- super$get_sock()
      writeBin(as.raw(id_code), out_stream)
      writeBin(arg, out_stream)},
    req_exe = function(id_code, arg) {
      private$write_code_ID(id_code, arg)
      private$req_result <- super$str_receive()
      private$req_success <- super$bool_test_sock()
    }, 
    receive_more = function(input, output) {
      if (missing(input)) input   <- private$get_sock()
      if (missing(output)) output <- raw(0)
      while ((rd <- readBin(input, what = "raw", n =1)) > 0) {
        if (rd == 0xff) next
        output <- c(output, rd)
      }
      ret <- rawToChar(output)
      return(ret)},
    clean = function(input) {
      result <- input %>% strsplit("\n", fixed = TRUE) 
      if ((result[[1]][1]  == "")) result <- result[[1]][2]
      return(result)
    }
  )
)
