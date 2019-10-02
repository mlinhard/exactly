# Intro

**exactly** is an exact substring search tool. It is able to find positions of arbitrary substrings, not just the whole words or similar words
as it is the case with the full-text search tools such as [Apache Lucene](http://lucene.apache.org/). **exactly** builds an index on
a set of files (binary or text, doesn't matter) and once the index is computed, it can be queried for occurence of a pattern, i.e.

We perform searches (pattern queries) on a set of files or *documents*, that we interpret as strings over byte alphabet.
If we have a set of documents *D1, D2, ..., Dn*. Let *sep* be a separator that is not substring of any of *Di (i in 1..n)*
Let *T* be concatenation of the documents, *T = D1 + sep + D2 + sep + ... + Dn* and let *N* be the length of *T*.

a query for pattern *P* will find all of the tuples *(p, j)* where it holds that *P* is substring of *Dj* starting at position *p*.

The query can be answered pretty fast, in *O(len(P)+Q)* time (len(P) is length of *P* and *Q* is the number of the returned *(p,j)* tuples)

Since **exactly** can find position of any pattern anywhere in the set of documents, this comes at the cost of memory.

**exactly** uses Enhanced suffix arrays as described in *Replacing suffix trees with enhanced suffix arrays* by *Abouelhoda, Kurtz, Ohlebusch* [article](https://www.sciencedirect.com/science/article/pii/S1570866703000650)
So far this is a basic straightforward implementation without any optimizations. The complete data structure takes *~ 25N* bytes of memory (*N* is length of total text *T*)
in the peak and *21N* afterwards. Currently only total text length up to 2 GB is supported.

To compute suffix array we use [SA-IS algorithm implementation by Yuta Mori](https://sites.google.com/site/yuta256/sais).

## Installation

Currently, the installer is only available for Fedora 64-bit system.

Download RPM from [latest release](https://github.com/mlinhard/exactly/releases)

## Usage

`exactly index <root>`

Start the indexing server on given root folder.

`exactly search`

Will start exactly console client where you'll be able to enter search queries.

## Build

The following snippets assume you checked out this git repository

### Installer

See [Exactly installers](installer) section

### Server

See [Exactly indexing server](server) section

### Client

Go into the `client` directory and run `setup.sh` script. This will locally build `exactly-index` golang binary and then include it in virtualenv folder `.venv`. You can then use exactly in a familiar fashion:

```
source .venv/bin/activate
exactly
```

# TODO

- [ ] Improve console - display document paths and more context and more hits on demand
- [ ] Improve console - display server connection / indexing status in status-bar
- [ ] Enhanced suffix array memory optimization
- [ ] Hexadecimal console mode (allow search for binary strings)
- [ ] Add memory performance test (comparison with Lucene)
- [ ] Allow UTF-8 String searches and UTF-8 context display
- [ ] Better test coverage
 
