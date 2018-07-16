# Intro

**exactly** is an exact substring search tool. It is ablet to find positions of arbitrary substrings, not just the whole words or similar words
as it is the case with the full-text search tools such as [Apache Lucene](http://lucene.apache.org/). **exactly** builds an index on
a set of files (binary or text, doesn't matter) and once the index is computed, it can be queried for occurence of a pattern, i.e.

We perform searches (pattern queries) on a set of files or *documents*, that we interpret as strings over byte alphabet.
If we have a set of documents *D1, D2, ..., Dn*. Let *sep* be a separator that is not substring of any of *Di (i in 1..n)*
Let *T* be concatenation of the documents, *T = D1 + sep + D2 + sep + ... + Dn* and let *N* be the length of *T*.

a query for pattern *P* will find all of the tuples *(p, j)* where it holds that *P* is substring of *Dj* starting at position *p*.

The query can be answered pretty fast, in *O(P+Q)* time (*Q* is the number of the returned *(p,j)* tuples)

Since **exactly** can find position of any pattern anywhere in the set of documents, this comes at the cost of memory.

**exactly** uses Enhanced suffix arrays as described in *Replacing suffix trees with enhanced suffix arrays* by *Abouelhoda, Kurtz, Ohlebusch* [article](https://www.sciencedirect.com/science/article/pii/S1570866703000650)
So far this is a basic straightforward implementation without any optimizations. The complete data structure takes *~ 25N* bytes of memory (*N* is length of total text *T*)
in the peak and *21N* afterwards. Currently only total text up to 2 GB is supported due to java array indexing limitation.

To compute suffix array we use [SA-IS algorithm implementation by Yuta Mori](https://sites.google.com/site/yuta256/sais).

# Building and installation
## Requirements

So far I've only created installer for Fedora/CentOS7/RHEL7 64-bit. I haven't figured out a way to build and securely distribute RPMs yet, 
so it needs to be built from sources, which requires git and docker. Everything else will be installed only inside of the docker container.

## Build

```
git clone https://github.com/mlinhard/exactly
pushd exactly/installer/rpm-builder
./build.sh
popd
```

After this you should find your installer in exactly/installer/rpm-builder/rpm/x86_64

## Install

Just install the RPM with yum/dnf tool. After this you should be able to use the **exactly** tool from command-line. The main dependencies are

- Java 1.8 JRE - the REST server is currently written as Spring boot REST service
- Python - the exactly command-line console tool is written in Python

# Usage

## Command line

`exactly index <root>`

Will start the REST server at http://localhost:9201 and index for all of the files (recursively) under given `<root>` directory.

`exactly search`

Will start exactly console client where you'll be able to enter search queries.


## API

**WARNING: The API is not yet fixed and is subject to change even with bugfix releases.**

### Server statistics
*GET http://localhost:9201/version*

will return simple one-line version string (no JSON)

*GET http://localhost:9201/stats*

will return server stats, that look like this:

```json
{
  "indexed_bytes": 110505,
  "indexed_files": 39,
  "done_crawling": true,
  "done_loading": true,
  "done_indexing": true
}
```

### Search query

*POST http://localhost:9201/search*  (Content-Type: application/json)

With request data:
```json
{
  "pattern": "cGF0dGVybg==",
  "max_candidates": 3,
  "max_context": 20
}
```

Will output something like:
```json
{
  "hits": [
    {
      "pos": 286,
      "doc_id": "/home/mlinhard/dev/projects/exactly/workspace/exactly/server/src/main/java/sk/linhard/search/Search.java",
      "ctx_before": "eHQuCgkgKiAKCSAqIEBwYXJhbSA=",
      "ctx_after": "CgkgKiBAcmV0dXJuCgkgKi8KCVM="
    },
    {
      "pos": 521,
      "doc_id": "/home/mlinhard/dev/projects/exactly/workspace/exactly/server/src/main/java/sk/linhard/search/HitContext.java",
      "ctx_before": "IHN0cmluZyArIGxlbmd0aCBvZiA=",
      "ctx_after": "CgkgKi8KCWludCBoaWdobGlnaHQ="
    },
    {
      "pos": 189,
      "doc_id": "/home/mlinhard/dev/projects/exactly/workspace/exactly/server/src/main/java/sk/linhard/search/HitContext.java",
      "ctx_before": "IHN0cmluZyBiZWZvcmUgKwogKiA=",
      "ctx_after": "ICsgYWZ0ZXIgd2l0aCBoaWdobGk="
    }
  ]
}
```

Request params:
---------------

- **pattern** - Base64 encoded binary string to search for
- **max_candidates** - Maximum number of candidates to return. Since the pattern can be even a single letter, the search query result size can be potentially quite huge, thus we need to limit the number of hits.
- **max_context** - Max number of bytes before and after the pattern that will be included in each hit to give the context to the position of the found pattern.

Response format:
----------------

- **hits** - JSON array of hit JSON objects

Hit format:
-----------

- **pos** - position of the hit in the document
- **doc_id** - string ID of the document, currently this is a file name
- **ctx_before** - Base64 encoded context *before* the pattern occurence
- **ctx_after** - Base64 encoded context *after* the pattern occurence


# TODO

- [ ] Improve console - display document paths and more context on demand
- [ ] Improve console - display server connection / indexing status in status-bar
- [ ] Add JVM stats (mainly memory usage)
- [ ] Enhanced suffix array memory optimization
- [ ] Hexadecimal console mode (allow search for binary strings)
- [ ] Add memory performance test (comparison with Lucene)
- [ ] Change the included Swing GUI code to REST client mode
- [ ] Allow UTF-8 String searches in GUI mode
- [ ] Better test coverage

