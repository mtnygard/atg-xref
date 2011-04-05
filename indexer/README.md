# atg-xref/indexer

This is the indexing utility for ATG-Xref.

At present, this only knows how to index ATG modules.

## Usage

The whole program is packaged as an executable uberjar, so you can run it straight from the command line:

    ./atg-xref-indexer <path_to_codebase>

This indexer knows some specifics about a certain large retailer's codebase. Therefore, it expects to find "apps", "zones", and "modules" underneath the root of the codebase.

## Building

This uses [Cake][1].

    cake deps
    cake bin

## License

Copyright (C) 2011 N6 Consulting LLC, All Rights Reserved


[1]: https://github.com/ninjudd/cake  "github.com/ninjudd/cake"
