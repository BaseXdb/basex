Rebol [ 
 Title: "Rebol client for BaseX"
 Author: "Sabu Francis"
 Date: "March 10, 2010"
 LastUpdated: "June 5, 2010"
 Version: "0.1" 
 Copyright: "Sabu Francis, Navi Mumbai, India"
 License: "Perl Artistic License 2.0"
 LicenseTextUrl: http://www.perlfoundation.org/artistic_license_2_0
 
 Purpose: {"BaseX Rebol client; works with BaseX 6.x (but not with BaseX 7.0 and later)"} 

 ;following data are for www.rebol.org library 
 ;you can find a lot of rebol script there 

 library: [ level: 'beginner 
 	     platform: 'all 
 	     type: [api library] 
 	     domain: [console] 
 	     tested-under: [windows linux] 
 	     support: none 
 	     license: [artistic_license_2_0] 
 	     see-also: http://code.google.com/p/reb-basex
 	     ]
 	     
 ChangeHistory: {
 
 	 June 5, 2010: Created a Google Project space for it and uploaded it there.
 		        	
 
 	}

 ToDo: {
 	  a) Error reporting.
 	  
 	}

]



getmd5: func [ 
{
An Md5 function, the way it is done in other languages. For some reason
Rebol gives the MD5 in uppercase. This takes in a string and gives out
its md5 hash
}
s [string!] ] [  

lowercase replace/all 
	  replace/all mold checksum/method copy s 'md5 "#{" ""  
	  "}" ""

]

gethashed: func[
	
{
	Two params: 1st is the password, 2nd the time stamp
 	It concantenates the md5 hash of the password with the password
 	and then make an md5 hash of the entire concatenation
 	That is how BaseX likes it
} 	
passwd [string!] 
ts [string!] 
/local p5 ]  [  

 	getmd5 rejoin [ getmd5 passwd  ts ] 
        
  ]
  

copyC: func [myport /local response data] [
wait myport ;;; waiting at the port is important!!!
data: copy myport
data
]


execBaseX: func [ 
{
1st param: username 2nd: Password. 3rd: BaseX command 
Example of 3rd param: "XQUERY basex:db('test')//author"
Refinement /p specifies port
Refinement /w specifies timeout (not used in this version)
This does complete execution of one BaseX command right from login to logout
It has a refinement /p using which you can specify the port to which to write to

}
user [string!] 
passwd [string!] 
Xqs [string!] /p port [any-type!] /w waittime [number!] /local c ts r1 rslt] [ 

system/schemes/default/timeout: 0.1
either p [ c: open/no-wait port]  ;;TCP Port should ONLY be opened using no-wait else it blocks!
         [ c: open/no-wait tcp://localhost:1984]

;print "Wokay, opened port"
ts: copyC c

;print ["Timestamp: " ts]

replace ts "^@" "" ;Remove the null character at end sent by BaseX

insert c user
insert c "^@"

insert c gethashed passwd ts
insert c "^@"

rslt: copyC c  ; At this point we should get an empty string if login successful

;print ["Result of login: " rslt  ]

insert c Xqs   ;;Now we send the command to be executed to BaseX on this session
;"XQUERY basex:db('test')//author"

insert c "^@"

rslt: replace copyC c "^@" ""

insert c "EXIT^@" ;This is the logout function in BaseX. It closes the session
close c 

rslt;;; returns back the string that was result of the command given to the BaseX engine in this session
]



BaseXLogin:  func [ 
{
1st param: username 2nd: Password. Refinement /p specifies port
Refinement /w specifies timeout (not used in this version)
This returns a connection port that can be used later
After you finish using the connection, make sure you do close it by using BaseXLogout
}
user [string!] 
passwd [string!] /p port [any-type!] /w waittime [number!] /local c ts r1 rslt] [ 

system/schemes/default/timeout: 0.1
either p [ c: open/no-wait port]  ;;TCP Port should ONLY be opened using no-wait else it blocks!
         [ c: open/no-wait tcp://localhost:1984]
;print "Wokay, opened port"
ts: copyC c

;print ["Timestamp: " ts]


replace ts "^@" "" ;Remove the null character at end sent by BaseX

insert c user
insert c "^@"

insert c gethashed passwd ts
insert c "^@"

rslt: copyC c  ; At this point we should get an empty string if login successful

;print ["Result of login: " rslt  ]

return c ;;; return the Port for this session
]


BaseXExecute: func [ 
{
This takes two arguments. 1st one is a connection port, 2nd is the BaseX command to be executed
Example: "XQUERY basex:db('test')//author"
}
c [any-type!] 
Xqs [string!] 
] [

insert c Xqs   ;;Now we send the command to be executed to BaseX on this session

insert c "^@"

rslt: replace copyC c "^@" ""
return rslt
]



BaseXLogout: func [ 
{This has to be called to close the BaseX connection}
c [any-type!] 
] [
insert c "EXIT^@" ;This is the logout function in BaseX. It closes the session
close c ;;; and also closes the port!

]