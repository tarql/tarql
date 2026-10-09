# Tarql: SPARQL for Tables

Tarql is a command-line tool for converting CSV files to RDF using SPARQL 1.1 syntax. It is written in Java and based on Apache Jena ARQ.

**See http://tarql.github.io/ for documentation.**

## Requirements

- Java 21 or later
- Apache Maven

Tarql currently uses Apache Jena 6.1.0.

## Building

Get the code from GitHub:

https://github.com/Ontotext-AD/tarql

Tarql uses Maven. To build the project and run the test suite:

    mvn clean test

To create the distribution packages:

    mvn clean package

To create executable scripts for Windows and Unix in `/target/appassembler/bin/tarql`:

    mvn package appassembler:assemble

Otherwise it's standard Maven.

## Version

The current release is Tarql 2.0.0.

This release updates Tarql to Java 21 and Apache Jena 6.1.0.