(:~
 : Page template and shared helpers.
 :
 : @author Christian Grün, BaseX Team, BSD License
 :)
module namespace html = 'dba/lib/html';

import module namespace config = 'dba/lib/config' at 'config.xqm';
import module namespace utils = 'dba/lib/utils' at 'utils.xqm';

(:~
 : Extends the specified content panels with the page template.
 : Panels are laid out side by side, one grid column each.
 : The following options can be specified:
 : * header: name of the page, which is also the entry marked in the navigation
 : * error: error string
 : * info: info string
 : * columns: grid track widths, one per panel; defaults to equal widths
 : * rows: grid track heights; the panels then fill the viewport instead of growing with
 :   their content, and each panel scrolls on its own
 : * panels: 'auto' if the page assigns the collapsed state of its panels itself; it is then
 :   not remembered, as it follows from what the page shows
 : * scripts: names of the scripts the page needs, besides the shared one
 : * init: call that prepares the page, evaluated after the shared setup
 :
 : @param  $panels   content panels
 : @param  $options  options
 : @return page
 :)
declare function html:wrap(
  $panels   as element()*,
  $options  as map(*) := {}
) as element(html) {
  (: the view is named by its page; the label of its entry is the capitalized name :)
  let $view := $options?header
  let $header := $view ! utils:capitalize(.)
  let $user := session:get($config:SESSION-KEY)
  (: only panels get a grid track; a page may supply scripts as well :)
  let $tracks := $panels[tokenize(@class) = 'panel']
  let $columns := string-join($options?columns otherwise ($tracks ! '1fr'), ' ')
  let $rows := string-join($options?rows, ' ')
  (: the client cannot tell a context path from a subdirectory, so the server states it :)
  return <html lang='en' data-context='{ request:context-path() }'
               data-interval='{ config:get($config:INTERVAL) }'>
    <head>
      <meta charset='utf-8'/>
      <meta http-equiv='Content-Security-Policy'
            content="default-src 'self'; script-src 'self' 'unsafe-inline';
                     style-src 'self' 'unsafe-inline'; img-src 'self' data:;
                     object-src 'none'; base-uri 'self'"/>
      <meta name='viewport' content='width=device-width, initial-scale=1'/>
      <title>DBA{ $header ! (' » ' || .) }</title>
      <meta name='description' content='Database Administration'/>
      <meta name='author' content='BaseX Team, BSD License'/>
      <meta name='robots' content='noindex'/>
      <link rel='icon' href='.static/basex.svg'/>
      <link rel='stylesheet' href='.static/style.css'/>
      <script src='.static/js.js'/>
      { $options?scripts ! <script src='.static/{ . }.js'/> }
    </head>
    <body>
      <header>
        <div class='header-main'>
          <div class='header-top'>
            <h1>
              <span class='title-full'>BaseX Database Administration</span>
              <span class='title-short'>BaseX DBA</span>
            </h1>
            {
              if ($user) {
                (: the logout ends the session: it is submitted, not a link that a prefetch
                   or a link scanner could follow :)
                <div>
                  <b>{ $user }</b> · <form method='post' action='logout'>
                    <button type='submit' class='link'>logout</button>
                  </form>
                </div>
              }
            }
          </div>
          <div class='header-nav'>{
            if ($user) {
              <nav>
                <ul>{
                  for $entry in $config:VIEWS
                  (: the current view is marked by an attribute, which is what a reader is
                     told and what the style picks out; the entry stays a link, and a click
                     on it reloads the starting page of the view :)
                  return <li>{
                    element a {
                      attribute href { $entry },
                      attribute aria-current { 'page' }[$entry = $view],
                      utils:capitalize($entry)
                    }
                  }</li>
                }</ul>
              </nav>
            } else {
              <div class='note'>
                Please enter your admin credentials:
              </div>
            },
            (: a status message, not a navigation entry: the client replaces its text, and
               the role is what has the replacement announced. It is spelled out, as not
               every browser maps the implicit role of the element :)
            element output {
              attribute id { 'info' },
              attribute role { 'status' },
              let $error := $options?error[.], $info := $options?info[.]
              return if ($error) {
                attribute class { 'error' }, $error
              } else if ($info) {
                attribute class { 'info' }, $info
              }
            }
          }</div>
        </div>
        <a href='{ request:context-path() }/dba' class='header-logo'>
          <img src='.static/basex.svg' alt='BaseX'/>
        </a>
      </header>
      <main>
        <div class='content{ ' fill'[$rows] }'
             style='--columns: { $columns }{ $rows[.] ! ('; --rows: ' || .) }'>{
          $options?panels ! attribute data-panels { . },
          $panels
        }</div>
      </main>
      <hr/>
      <footer><sup>BaseX Team, BSD License</sup></footer>
      {
        (: the dialogs that replace the browser's confirm and prompt: every page has them,
           and 'method=dialog' hands the clicked button's value back as the answer :)
        (: the answers are supplied by the question, so the row is filled by askQuestion :)
        <dialog id='confirm-dialog'>
          <form method='dialog'>
            <p id='confirm-text'/>
            <div class='buttons'/>
          </form>
        </dialog>,
        <dialog id='prompt-dialog'>
          <form method='dialog'>
            <p id='prompt-text'/>
            <input type='text' id='prompt-input' class='wide'/>
            <div class='buttons'>
              <button value='ok'>OK</button>
              <button value=''>Cancel</button>
            </div>
          </form>
        </dialog>,
        html:js('buttons(); ready();'),
        (: the page is prepared once the shared setup is done :)
        $options?init ! html:js(.),
        html:js('hideParams("info", "error");')[exists(($options?info, $options?error)[.])]
      }
    </body>
  </html>
};

(:~
 : Creates a link to the specified target.
 : @param  $text    link text
 : @param  $href    link reference
 : @param  $params  query parameters
 : @return link
 :)
declare function html:link(
  $text    as xs:string,
  $href    as xs:string,
  $params  as map(*)* := {}
) as element(a) {
  <a href='{ web:create-url($href, map:merge($params)) }'>{ $text }</a>
};

(:~
 : Returns a formatted representation of a dateTime value. Seconds are dropped: the value tells
 : when a file or database was last touched, which no one counts in seconds.
 : @param  $date  date
 : @return string
 :)
declare function html:date(
  $date  as xs:dateTime
) as xs:string {
  format-dateTime(html:adjust($date), '[Y0000]-[M00]-[D00] [H00]:[m00]')
};

(:~
 : Returns a formatted time representation of a dateTime value with tooltip.
 : @param  $date  date
 : @return element with tooltip
 :)
declare function html:time(
  $date  as xs:dateTime
) as element(span) {
  let $adjusted := html:adjust($date)
  let $formatted := format-dateTime($adjusted, '[H00]:[m00]:[s00]')
  return <span title='{ $adjusted }'>{ $formatted }</span>
};

(:~
 : Returns a dateTime value adjusted to the current time zone.
 : @param  $date  date
 : @return adjusted value
 :)
declare function html:adjust(
  $date  as xs:dateTime
) as xs:dateTime {
  let $zone := timezone-from-dateTime(current-dateTime())
  return adjust-dateTime-to-timezone(xs:dateTime($date), $zone)
};

(:~
 : Formats a duration.
 : @param  $seconds  seconds
 : @return string
 :)
declare function html:duration(
  $seconds  as xs:decimal
) as xs:string {
  let $min := $seconds idiv 60
  let $sec := $seconds - $min * 60
  return (format-number($min, '00') || ':' || format-number($sec, '00'))
};

(:~
 : Creates an embedded JavaScript snippet.
 : @param  $js  JavaScript string
 : @return script element
 :)
declare function html:js(
  $js  as xs:string
) as element(script) {
  <script>{ '(function() { ' || $js || ' })();' }</script>
};

(:~
 : Creates a new map with the current query parameters.
 : @return map with query parameters
 :)
declare function html:parameters() as map(*) {
  map:merge(
    for $param in request:parameter-names()[not(starts-with(., '_'))]
    return { $param: request:parameter($param) }
  )
};

(:~
 : Creates a new map with query parameters. The returned map contains all
 : current query parameters, and the given ones, prefixed with an underscore.
 : @param  $map  predefined parameters
 : @return map with query parameters
 :)
declare function html:parameters(
  $map  as map(*)?
) as map(*) {
  map:merge((
    html:parameters(),
    map:for-each($map, fn($name, $value) {
      map:entry('_' || $name, $value)
    })
  ))
};
