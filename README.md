# ATG Xref

Indexing, search, and cross-reference for the BestBuy.com ATG
codebase.

## Getting Started

First, run the Solr server that comes with this install:

    cd server
    java -jar start.jar

Now, index the ATG codebase from another terminal (Solr will take over
your first one.)

    cd indexer
    java -jar atg-xref-1.0.0-SNAPSHOT-standalone.jar ../bestbuy/dotcom/dgt

## Querying

So far, I haven't put a UI in front of the query engine. You can use
the Solr [admin GUI][http://localhost:8983/solr/admin] to run some
queries, if you don't mind reading XML for the results. Try out a
couple of queries like these examples.

* [Modules which require dgt.common][http://localhost:8983/solr/select/?q=required%3Adgt.common]
* [Values from LIB-COMMON itself][http://localhost:8983/solr/select/?q=LIB-COMMON]

## Deployment Topology

For the time being, all components should be deployed to the same
host. I haven't yet added a way to configure the Solr URL used by the
indexer and webui.

This also means that the codebase itself should be available on that
host.

I sort of expect that the "server" will usually just be your own
laptop or workstation.

## TODO

* Flesh out the module and component pages.
* Paginate module list and component list.
* Make search box work.
* Index JSP files, too. Look for bean references.
* Create high-level "what uses?" page.

## License

Copyright (C) 2011 N6 Consulting LLC, All Rights Reserved
