[Documentation Page](http://emunoz.org/tarql)

Supports Java 1.7

[![Build status](https://travis-ci.org/emir-munoz/tarql.svg?branch=master)](https://travis-ci.org/emir-munoz/tarql)


# Tarql: SPARQL for Tables

Tarql is a command-line tool for converting CSV files to RDF using SPARQL 1.1 syntax. It's written in Java and based on Apache ARQ.

**See http://tarql.github.io/ for documentation.**

## Building

Get the code from GitHub: http://github.com/tarql/tarql

Tarql uses Maven. To create executable scripts for Windows and Unix in `/target/appassembler/bin/tarql`:

    mvn clean package appassembler:assemble

Otherwise it's standard Maven.
