(:~
 : Common RESTXQ access points.
 :
 : @author Christian Grün, BaseX Team, BSD License
 :)
module namespace dba = 'dba/common';

import module namespace config = 'dba/lib/config' at 'lib/config.xqm';
import module namespace html = 'dba/lib/html' at 'lib/html.xqm';

(:~
 : Redirects to the start page.
 : @return redirection
 :)
declare
  %rest:path('/dba')
function dba:redirect(
) as element(rest:response) {
  (: the start page is the first view the navigation offers :)
  web:redirect('/dba/' || head($config:VIEWS))
};

(:~
 : Returns a static file of the application.
 : @param  $file  file or unknown path
 : @param  $v     version of the file (see html:static)
 : @return rest binary data
 :)
declare
  %rest:path('/dba/.static/{$file=.+}')
  %rest:query-param('v', '{$v}')
  %perm:allow('public')
function dba:file(
  $file  as xs:string,
  $v     as xs:string?
) as item()+ {
  let $path := 'static/' || $file
  return if (contains($file, '..')) then (
    web:error(400, 'Invalid path: ' || $file)
  ) else (
    try {
      (: the lazy binary is read here: an error must not surface during serialization, and its
         message would name the directory of the application :)
      let $data := fetch:binary($path)
      let $size := bin:length($data)
      return (
        web:response-header(
          { 'media-type': web:content-type($path) },
          {
            (: a versioned address changes with the file :)
            'Cache-Control': if ($v) then 'max-age=31536000,public,immutable'
                             else 'max-age=3600,public',
            'Content-Length': string($size)
          }
        ),
        $data
      )
    } catch * {
      web:error(404, 'Unknown file: ' || $file)
    }
  )
};

(:~ Namespace of the File Module: its errors are caused by the path that was requested. :)
declare %private variable $dba:FILE-NS := 'http://expath.org/ns/file';

(:~
 : Reports an error that no endpoint has handled: the client is sent the error code and its
 : description, never the module and line of the code that raised it.
 : @param  $path         path of the request that failed
 : @param  $code         error code
 : @param  $description  error description
 : @return error message
 :)
declare
  %rest:error('*')
  %rest:path('/dba/{$path=.*}')
  %rest:error-param('code', '{$code}')
  %rest:error-param('description', '{$description}')
function dba:error(
  $path         as xs:string,
  $code         as xs:QName?,
  $description  as xs:string?
) as item()+ {
  (: the path is not evaluated; it limits the handler to the DBA, as other RESTXQ applications
     report their own errors :)
  let $local := $code ! local-name-from-QName(.)
  (: web:error already states the status, and its message needs no code :)
  let $stated := $local[matches(., '^status\d+$')] ! xs:integer(substring(., 7))
  let $status := $stated otherwise (
    switch () {
      case $local = 'not-found' return 404
      case $code ! namespace-uri-from-QName(.) = $dba:FILE-NS return 400
      case matches($local, '^X[PQ]ST') return 400
      default return 500
    }
  )
  (: XQuery errors are known by their code alone, the errors of a module by their prefix :)
  let $name := if (not($stated)) {
    string-join(($code ! prefix-from-QName(.)[. != 'err'], $local), ':')
  }
  return (
    web:response-header((), (), { 'status': $status }),
    string-join((`[{ $name }] `[$name], $description otherwise 'Unexpected error.'))
  )
};

(:~
 : Shows a 'page not found' error.
 : @param  $path  path to unknown page
 : @return page
 :)
declare
  %rest:path('/dba/{$path}')
  %output:method('html')
function dba:unknown(
  $path  as xs:string
) as element()+ {
  web:response-header((), (), { 'status': 404 }),
  html:panel((
    <h2>Page not found:</h2>,
    <ul>
      <li>Page: dba/{ $path }</li>
      <li>Method: { request:method() }</li>
    </ul>
  ), { 'pane': false() })
  => html:wrap()
};
