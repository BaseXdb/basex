; -----------------------------------------------------------------------------
;
; This example shows how BaseX commands can be performed via the Lisp API.
; The execution time will be printed along with the result of the command.
;
; -----------------------------------------------------------------------------
; (C) Andy Chambers, Formedix Ltd 2010, ISC License
; -----------------------------------------------------------------------------

(defpackage :basex-user
 (:use :cl :basex))

(in-package :basex-user)

(time
 (let ((session (make-instance 'session)))
  (if (execute session "xquery 1 to 10")
      (print (result session))
      (print (info session)))

  (close-session session)))
