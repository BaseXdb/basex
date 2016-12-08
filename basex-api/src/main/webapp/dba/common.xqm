(:~
 : Common RESTXQ access points.
 :
 : @author Christian Grün, BaseX Team, 2014-16
 :)
module namespace dba = 'dba/common';

import module namespace Request = 'http://exquery.org/ns/request';
import module namespace cons = 'dba/cons' at 'modules/cons.xqm';
import module namespace tmpl = 'dba/tmpl' at 'modules/tmpl.xqm';

(:~
 : Redirects to the start page.
 :)
declare
  %rest:path("/dba")
function dba:redirect(
) as element(rest:response) {
  web:redirect("/dba/databases")
};

(:~
 : Returns a file.
 : @param  $file  file or unknown path
 : @return rest response and binary file
 :)
declare
  %rest:path("/dba/static/{$file=.+}")
function dba:file(
  $file as xs:string
) as item()+ {
  let $path := file:base-dir() || 'static/' || $file
  return (
    web:response-header(map { 'media-type': web:content-type($path) }),
    file:read-binary($path)
  )
};

(:~
 : Shows a page not found error.
 : @param  $unknown  unknown page
 : @return page
 :)
declare
  %rest:path("/dba/{$unknown}")
  %output:method("html")
function dba:any(
  $unknown  as xs:string
) as element(html) {
  cons:check(),
  tmpl:wrap(
    <tr>
      <td>
        <h2>Page not found:</h2>
        <ul>
          <li>Page: <code>dba/{ $unknown }</code></li>
          <li>Method: <code>{ Request:method() }</code></li>
        </ul>
      </td>
    </tr>
  )
};

(:~
 : Login error: redirects to the login page.
 : @param  $page page to redirect to
 :)
declare
  %rest:error("basex:login")
  %rest:error-param("value", "{$path}")
function dba:error-login(
  $path  as xs:string?
) as element(rest:response) {
  web:redirect("login", map { 'path': replace($path, '.*/', '') })
};
