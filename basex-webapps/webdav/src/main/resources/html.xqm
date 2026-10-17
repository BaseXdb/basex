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
 : Returns a static file of the application.
 : @param  $file  file or unknown path
 : @return rest binary data
 :)
declare
  %rest:path('/webdav/.static/{$file=.+}')
  %output:method('basex')
function html:file(
  $file  as xs:string
) as item()+ {
  let $path := 'static/' || $file
  return if (contains($file, '..')) {
    web:error(400, 'Invalid path: ' || $file)
  } else {
    web:response-header(
      { 'media-type': web:content-type($path) },
      { 'Cache-Control': 'max-age=3600,public' }
    ),
    fetch:binary($path)
  }
};

(:~
 : Returns the content of a resource, or a listing of a collection.
 : @param  $path  path below the WebDAV root (empty for the root collection)
 : @return response
 :)
declare
  %rest:GET
  %rest:HEAD
  %rest:path('/webdav')
  %rest:path('/webdav/{$path=.*}')
function html:get(
  $path  as xs:string?
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
      <meta http-equiv='Content-Security-Policy'
            content="default-src 'self'; script-src 'self' 'unsafe-inline';
                     style-src 'self' 'unsafe-inline'; img-src 'self' data:;
                     object-src 'none'; base-uri 'self'"/>
      <meta name='viewport' content='width=device-width, initial-scale=1'/>
      <title>BaseX WebDAV</title>
      <meta name='description' content='WebDAV Interface'/>
      <meta name='author' content='BaseX Team, BSD License'/>
      <meta name='robots' content='noindex'/>
      <link rel='icon' href='{ $res:ROOT }/.static/basex.svg'/>
      <link rel='stylesheet' type='text/css' href='{ $res:ROOT }/.static/style.css'/>
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
          <div class='header-nav'>{ html:breadcrumb($rc) }</div>
        </div>
        <a href='/' class='header-logo'>
          <img src='{ $res:ROOT }/.static/basex.svg' alt='BaseX'/>
        </a>
      </header>
      <main>
        <div class='content' style='--columns: 1fr'>
          <div class='panel'>
            <table>
              <tr>
                <th>Name</th>
                <th>Content Type</th>
                <th class='num'>Size</th>
                <th>Modified</th>
              </tr>
              {
                for $child in res:children($rc)
                order by $child?kind, $child?name
                return html:entry($child)
              }
            </table>
          </div>
        </div>
      </main>
      <hr/>
      <footer><sup>BaseX Team, BSD License</sup></footer>
    </body>
  </html>
};

(:~
 : Generates the path to a collection: the ancestors are links, the collection itself
 : is the level that is shown.
 : @param  $rc  resource description
 : @return breadcrumb
 :)
declare %private function html:breadcrumb(
  $rc  as res:any
) as element(div) {
  let $steps := tokenize(res:path($rc), '/')[.]
  let $labels := ('WebDAV', $steps)
  return <div class='note ellipsis'>{
    for $label at $pos in $labels
    return (
      (: text nodes, as two adjacent strings would be separated by a space :)
      text { ' » ' }[$pos > 1],
      (: the last step leads to the level that is shown, and nowhere to go :)
      if ($pos = count($labels)) {
        text { $label }
      } else {
        element a {
          attribute href {
            resp:encode(string-join(subsequence($steps, 1, $pos - 1), '/')) || '/'
          },
          attribute class { 'root' }[$pos = 1],
          $label
        }
      }
    )
  }</div>
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
    <td class='num'>{
      (: for XML resources, the size is a node count, not a byte length :)
      if ($resource and $rc?type != 'xml') { $rc?size }
    }</td>
    <td>{ format-dateTime($rc?modified, '[Y0001]-[M01]-[D01] [H01]:[m01]:[s01]') }</td>
  </tr>
};
