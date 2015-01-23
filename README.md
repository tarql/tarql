# Tarql: SPARQL for Tables

Tarql is a command-line tool for converting CSV files to RDF using SPARQL 1.1 syntax. It's written in Java and based on Apache ARQ.

**See http://tarql.github.io/ for documentation.**

## Building

Get the code from GitHub: http://github.com/tarql/tarql

Tarql uses Maven. To create executable scripts for Windows and Unix in `/target/appassembler/bin/tarql`:

    mvn package appassembler:assemble

Otherwise it's standard Maven.
