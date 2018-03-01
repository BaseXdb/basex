(:~
 : Common RESTXQ access points.
 :
 : @author Christian Grün, BaseX Team, 2014-18
 :)
module namespace dba = 'dba/common';

import module namespace Request = 'http://exquery.org/ns/request';
import module namespace html = 'dba/html' at 'modules/html.xqm';

(:~
 : Redirects to the start page.
 : @return redirection
 :)
declare
  %rest:path("/dba")
function dba:redirect(
) as element(rest:response) {
  web:redirect("/dba/logs")
};

(:~
 : Returns a file.
 : @param  $file  file or unknown path
 : @return rest binary data
 :)
declare
  %rest:path("/dba/static/{$file=.+}")
  %perm:allow("all")
function dba:file(
  $file as xs:string
) as item()+ {
  let $path := file:base-dir() || 'static/' || $file
  return (
    web:response-header(
      map { 'media-type': web:content-type($path) },
      map { 'Cache-Control': 'max-age=3600,public' }
    ),
    file:read-binary($path)
  )
};

(:~
 : Shows a "page not found" error.
 : @param  $path  path to unknown page
 : @return page
 :)
declare
  %rest:path("/dba/{$path}")
  %output:method("html")
function dba:unknown(
  $path  as xs:string
) as element(html) {
  html:wrap(
    <tr>
      <td>
        <h2>Page not found:</h2>
        <ul>
          <li>Page: dba/{ $path }</li>
          <li>Method: { Request:method() }</li>
        </ul>
      </td>
    </tr>
  )
};
