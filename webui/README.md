# ATG-XRef Web UI

Human interface to the ATG Xref index.

## Usage

TEMPORARY INSTRUCTIONS:

Run this from the repl as follows:

$ lein repl

"REPL started; server listening on localhost:11815."
user=> (use 'view)
nil
user=> (use 'webui.core)
nil
user=> (def server (start-server))



The UI will be packaged as an uberjar. That means you just run it from the
command line:

    java -jar atg-xref-webui-1.0.0-SNAPSHOT-standalone.jar

## License

Copyright (C) 2011 N6 Consulting LLC.

All rights reserved.

