(:~
 : GET and HEAD requests: resource contents and browsable collection listings.
 :
 : Retrieving a resource is plain HTTP. The listing of a collection is a
 : convenience and not part of the protocol: RFC 4918 leaves the response to a
 : GET on a collection to the implementation, and WebDAV clients enumerate
 : members with PROPFIND. The pages produced here can be changed or dropped
 : without affecting any client.
 :
 : @author BaseX Team, BSD License
 :)
module namespace html = 'webdav/html';

import module namespace res = 'webdav/resources' at 'resources.xqm';
import module namespace resp = 'webdav/responses' at 'responses.xqm';

(: GET, HEAD ====================================================================================:)

(:~
 : Returns a listing of the root collection.
 : @return response
 :)
declare
  %rest:GET
  %rest:HEAD
  %rest:path('/webdav')
function html:get-root() as item()+ {
  html:get('')
};

(:~
 : Returns the content of a resource, or a listing of a collection.
 : @param  $path  path below the WebDAV root
 : @return response
 :)
declare
  %rest:GET
  %rest:HEAD
  %rest:path('/webdav/{$path=.*}')
function html:get(
  $path  as xs:string
) as item()+ {
  let $rc := res:lookup($path)
  return if (empty($rc)) {
    web:error(404, 'Not Found')
  } else if ($rc?kind = 'resource') {
    web:response-header(
      { 'method': if ($rc?type = 'xml') { 'xml' } else { 'basex' } },
      {
        'Content-Type': $rc?content-type,
        'Last-Modified': resp:http-date($rc?modified),
        'ETag': resp:etag($rc)
      },
      { 'status': 200, 'message': 'OK' }),
    res:content($rc)
  } else {
    web:response-header({ 'method': 'html' }, (), { 'status': 200, 'message': 'OK' }),
    html:listing($rc)
  }
};

(: PAGES ========================================================================================:)

(:~
 : Generates a listing of a collection.
 : @param  $rc  collection description
 : @return HTML page
 :)
declare %private function html:listing(
  $rc  as res:any
) as element(html) {
  <html lang='en'>
    <head>
      <meta charset='utf-8'/>
      <meta name='viewport' content='width=device-width, initial-scale=1'/>
      <title>BaseX WebDAV</title>
      <meta name='author' content='BaseX Team, BSD License'/>
      <link rel='icon' href='/static/basex.svg'/>
      <link rel='stylesheet' type='text/css' href='/static/style.css'/>
    </head>
    <body>
      <header>
        <div class='header-main'>
          <div class='header-top'>
            <h1>
              <span class='title-full'>BaseX WebDAV</span>
              <span class='title-short'>WebDAV</span>
            </h1>
          </div>
          <nav class='ellipsis'>{ html:breadcrumb($rc) }</nav>
          <hr/>
        </div>
        <a href='/' class='header-logo'><img src='/static/basex.svg' alt='BaseX'/></a>
      </header>
      <main>
        <table class='content'>
          <tr>
            <th>Name</th>
            <th>Content Type</th>
            <th class='right'>Size</th>
            <th>Modified</th>
          </tr>
          {
            for $child in res:children($rc)
            order by $child?kind, $child?name
            return html:entry($child)
          }
        </table>
      </main>
      <hr/>
      <footer class='right'><sup>BaseX Team, BSD License</sup></footer>
    </body>
  </html>
};

(:~
 : Generates the links to the ancestors of a resource.
 : @param  $rc  resource description
 : @return links
 :)
declare %private function html:breadcrumb(
  $rc  as res:any
) as item()* {
  let $steps := tokenize(res:path($rc), '/')[.]
  let $links := (
    <a href='{ $res:ROOT }/'>WebDAV</a>,
    for $step at $pos in $steps
    return <a href='{ resp:encode(string-join(subsequence($steps, 1, $pos), '/')) }/'>{
      $step
    }</a>
  )
  return (head($links), tail($links) ! (' / ', .))
};

(:~
 : Generates a table row for a collection member.
 : @param  $rc  resource description
 : @return table row
 :)
declare %private function html:entry(
  $rc  as res:any
) as element(tr) {
  let $resource := $rc?kind = 'resource'
  return <tr>
    <td>
      <a href='{ resp:href($rc) }'>{ $rc?name }{ if (not($resource)) { '/' } }</a>
    </td>
    <td>{ if ($resource) { $rc?content-type } }</td>
    <td class='right'>{
      (: for XML resources, the size is a node count, not a byte length :)
      if ($resource and $rc?type != 'xml') { $rc?size }
    }</td>
    <td>{ format-dateTime($rc?modified, '[Y0001]-[M01]-[D01] [H01]:[m01]:[s01]') }</td>
  </tr>
};
