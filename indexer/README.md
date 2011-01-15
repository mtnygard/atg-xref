# atg-xref/indexer

This is the indexing utility for ATG-Xref.

At present, this only knows how to index ATG modules.

## Usage

The whole program is packaged as an uberjar, so you can run it
straight from the command line:

    java -jar atg-xref-indexer-1.0.0-SNAPSHOT-standalone.jar <path_to_codebase>

This indexer knows some specifics about a certain large retailer's codebase. Therefore, it expects to find "apps", "zones", and "modules" underneath the root of the codebase.

## Building

This uses [Leiningen][1]. You should install version 1.3.1 or higher. To create the uberjar, just use:

    lein uberjar

## License

Copyright (C) 2011 N6 Consulting LLC, All Rights Reserved


[1]: https://github.com/technomancy/leiningen  "github.com/technomancy/leiningen"
