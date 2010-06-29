; -----------------------------------------------------------------------------
;
; This module provides methods to connect to and communicate with the
; BaseX Server.
;
; The Constructor of the class expects a hostname, port, username and password
; for the connection. The socket connection will then be established via the
; hostname and the port.
;
; For the execution of commands you need to call the execute() method with the
; database command as argument. The method returns a boolean, indicating if
; the command was successful. The result can be requested with the result()
; method, and the info() method returns additional processing information
; or error output.
;
; -----------------------------------------------------------------------------
;
; Example:
;
; (defpackage :basex-user
; (:use :cl :basex))
;
; (time
;   (let ((session (make-instance 'session)))
;    (if (execute session "xquery 1 to 10")
;        (print (result session))
;        (print (info session)))
;    (close-session session)))
;
; -----------------------------------------------------------------------------
; (C) Andy Chambers, Formedix Ltd 2010, ISC License
; -----------------------------------------------------------------------------

(defpackage :basex
 (:use :cl :usocket)
 (:export :session
          :execute
          :close-session
          :info
          :result))

(in-package :basex)

(defconstant +null+ (code-char 0))

(defclass session ()
 ((host :initarg :host :initform "localhost")
  (port :initarg :port :initform 1984)
  (user :initarg :user :initform "admin")
  (pw :initarg :pw :initform "admin")
  (sock :initform nil)
  (result :initform nil :accessor result)
  (info :initform nil :accessor info)))

(defmethod initialize-instance :after ((self session) &key)
 (with-slots (host port user pw sock) self
   (setf sock (socket-connect host port :element-type '(unsigned-byte 8)))
   (unless (hand-shake self)
     (error "Could not initiate connection"))))

(defun hand-shake (session)
 (declare (optimize debug))
 (labels ((md5 (str)
            (string-downcase (with-output-to-string (s)
                               (loop for hex across
                                    (sb-md5:md5sum-string str)
                                  do (format s "~2,'0x" hex)))))
          (auth-token (pw timestamp)
            (md5 (format nil "~a~a"
                         (md5 pw) timestamp))))

   (with-slots (user pw sock) session
     (let* ((ts (read-null-terminated (socket-stream sock)))
            (auth (auth-token pw ts)))
       (write-null-terminated user (socket-stream sock))
       (write-null-terminated auth (socket-stream sock))
       (force-output (socket-stream sock))
       (eq 0 (read-byte (socket-stream sock)))))))

(defun read-null-terminated (in)
 (with-output-to-string (s)
   (loop for char = (code-char (read-byte in))
        until (char= char +null+)
        do (write-char char s))))

(defun write-null-terminated (string out)
 (loop for char across string
      do (write-byte (char-code char) out))
 (write-byte (char-code +null+) out))

(defmethod execute ((self session) query)
 (with-slots (sock) self
   (let ((stream (socket-stream sock)))
     (write-null-terminated query stream)
     (force-output stream)
     (setf (result self) (read-null-terminated stream)
           (info self) (read-null-terminated stream))
     (eq 0 (read-byte (socket-stream sock))))))

(defmethod open-session ((self session))
 (unwind-protect
      (unless (hand-shake self)
        (error "Could not open session"))
   (close-session self)))

(defmethod close-session ((self session))
 (with-slots (sock) self
   (write-null-terminated "exit" (socket-stream sock))
   (socket-close sock)))
