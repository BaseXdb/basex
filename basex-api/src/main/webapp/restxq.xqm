(:~
 : This module contains some basic examples for RESTXQ annotations.
 : @author BaseX Team
 :)
module namespace page = 'http://basex.org/examples/web-page';

(:~
 : Generates a welcome page.
 : @return HTML page
 :)
declare
  %rest:GET
  %rest:path('')
  %output:method('html')
function page:start(
) as element(html) {
  <html lang='en'>
    <head>
      <meta charset='utf-8'/>
      <meta name='viewport' content='width=device-width, initial-scale=1'/>
      <title>BaseX HTTP Services</title>
      <link rel='icon' href='static/basex.svg'/>
      <link rel='stylesheet' type='text/css' href='static/style.css'/>
    </head>
    <body>
      <header>
        <div class='header-main'>
          <div class='header-top'>
            <h1>BaseX HTTP Services</h1>
          </div>
          <nav class='ellipsis'>{
            insert-separator(
              ('DBA', 'Chat', 'WebDAV', 'REST') ! <a href='{ lower-case(.) }'>{ . }</a>,
              ' · '
            )
          }</nav>
          <hr/>
        </div>
        <a href='/' class='header-logo'><img src='static/basex.svg' alt='BaseX'/></a>
      </header>
      <main>
        <p>Welcome to the BaseX HTTP Services: sample web applications written in
          XQuery, and a REST interface to your databases.</p>
        <p>For the full picture, see the
          <a href='https://docs.basex.org/main/Web_Application'>Web Application</a>
          documentation.</p>

        <h2><a href='dba'>DBA: Database Administration</a></h2>
        <p><b>A complete web front-end for your server</b></p>
        <p>Create, browse and edit databases and their resources, upload and download files,
          and manage users and their permissions. Write and run queries in a live editor,
          schedule them as background jobs, and revisit index and backup options at any time.
          Keep an eye on running jobs, open sessions, server settings and log files, all
          updating in real time.</p>
        <p>The interface is itself written in XQuery and RESTXQ, so it doubles as the largest
          worked example in this distribution – a good place to start reading if you want to
          see a full application built on top of BaseX.</p>

        <h2><a href='chat'>Chat Application</a></h2>
        <p><b>A live, multi-room chat</b></p>
        <p>Switch between channels, see who is online and which room they are in, and post to the
          whole room or privately to a single person. Messages are pushed instantly over
          WebSockets, nothing is polled, and every conversation is kept in a store and served
          again to everyone who joins later. Accounts are the ones you created in the DBA.</p>
        <p>For the full effect, create multiple users in the DBA, open the chat in two browsers,
          and log in as different users.</p>

        <h2><a href='webdav'>WebDAV</a></h2>
        <p><b>Your databases as a file system</b></p>
        <p>Mount <code>/webdav</code> in your file manager, and every database will show up as an
          ordinary folder, with its resources as files. Open documents in your favorite XML editor,
          save them straight back, and copy, rename or delete them the way you would anywhere else.
          Drag whole directories in to import them, and out to get them back. Behind the folders
          there are no files on disk: every listing and every save is answered straight from the
          database.</p>

        <h2><a href='rest'>REST</a></h2>
        <p><b>Work with your databases straight over HTTP</b></p>
        <p>List databases, and read, add, replace and delete their documents with plain
          <code>GET</code>, <code>PUT</code>, <code>POST</code> and <code>DELETE</code> requests.
          Send XQuery, XSLT or commands along, in the URL or in the request body, and let
          serialization parameters decide whether the answer comes back as XML, JSON, CSV, HTML
          or plain text. A browser or <code>curl</code> is all the tooling you need.</p>
        <p>And if the fixed set of REST operations is not the interface you want to expose,
          RESTXQ lets you define your own – this page is one.</p>
      </main>
      <hr/>
      <footer><sup>BaseX Team, BSD License</sup></footer>
    </body>
  </html>
};
