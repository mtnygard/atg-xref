# ATG XRef Solr Server Configuration

## Startup 

To start this server, just run:

    java -jar start.jar

## Shutdown

### Simple

The easiest way is just CTRL-C in the command window where you started
the server. This is OK to run if you're just using the server locally,
since it's unlikely that anyone else is indexing documents on your machine.

### Graceful

A more graceful approach is to specify a "stop port" and "stop key" on
the initial command line. If you start jetty with:

    java -DSTOP.PORT=9999 -DSTOP.KEY=secret -jar start.jar

Then you can stop it with:

    java -DSTOP.PORT=9999 -DSTOP.KEY=secret -jar start.jar --stop

## Admin Interface

Available on [localhost][1].

## License

Copyright (C) 2011 N6 Consulting LLC, All Rights Reserved

[1]: [http://localhost:8983/solr/admin]
