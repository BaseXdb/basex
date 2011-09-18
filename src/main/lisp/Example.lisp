; This example shows how database commands can be executed.
;
; Documentation: http://docs.basex.org/wiki/Clients
;
; (C) Andy Chambers, Formedix Ltd 2010, BSD License

(defpackage :basex-user
 (:use :cl :basex))

(in-package :basex-user)

(time
 (let ((session (make-instance 'session)))
  (if (execute session "xquery 1 to 10")
      (print (result session))
      (print (info session)))

  (close-session session)))
