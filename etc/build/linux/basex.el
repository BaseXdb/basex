
;;; Emacs runs BaseX
;;; 
;;; Version: 0.1
;;; Athor: Stefan Klinger
;;; Date: 2006-May-31 11:32:49 CEST


(require 'comint)


(defun switch-to-basex ()

  "Calling 'switch-to-basex' switches to a buffer '*BaseX*' and runs the stand alone version of the BaseX DB in comint-mode. If a buffer with that name already exists, it is reused. If a process is running in that buffer already, no new process is started."
  (interactive)
   
  ;;; switch to (and maybe create) the *BaseX* buffer.
  (switch-to-buffer "*BaseX*")

  ;;; check whether there's a running proces
  (if (comint-check-proc (current-buffer))
  
    ;;; if so, do nothing
    (message "running process found")
    
    ;;; otherwise set comint mode
    (comint-mode)
    
    ;;; and point java to BaseX
    (comint-exec
      (current-buffer)
      (buffer-name)
      "basexc"
      nil ;STARTFILE
      nil ;ARGUMENTS
))) 
