Startup:

* Execute ```startserver``` or ```startserver.bat``` (Maven is required)

```
.
├── README.md
├── data   ...............................................  Database store
├── public
│   ├── pom.xml
│   ├── index.html
│   ├── WEB-INF
│   │   ├── jetty.xml
│   │   └── web.xml
│   └─── static
│       └── dist
│           ├── css
│           │   ├── bootstrap-theme.css
│           │   ├── bootstrap-theme.css.map
│           │   ├── bootstrap-theme.min.css
│           │   ├── bootstrap.css
│           │   ├── bootstrap.css.map
│           │   └── bootstrap.min.css
│           ├── fonts
│           │   ├── glyphicons-halflings-regular.eot
│           │   ├── glyphicons-halflings-regular.svg
│           │   ├── glyphicons-halflings-regular.ttf
│           │   └── glyphicons-halflings-regular.woff
│           └── js
│               ├── angular.min.js
│               ├── bootstrap.js
│               └── bootstrap.min.js
├── repo   ................................................  XQuery modules
├── restxq
│   └── hello.xqm   .......................................  RESTXQ service
├── startserver   .........................................  start on UNIX
└── startserver.bat   .....................................  start on WIN

13 directories, 21 files
```

Have fun.
  BaseX Team
