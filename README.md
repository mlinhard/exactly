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

# Demo

[![asciicast](https://asciinema.org/a/Pj6xP9ZP0DRz7OFtBJoLaiYXj.png)](https://asciinema.org/a/Pj6xP9ZP0DRz7OFtBJoLaiYXj)

# Building and installation
## Requirements

So far I've only created installer for Fedora 64-bit. I thought that would automatically give us CentOS/RHEL 7 but there's no python3
on those so it's Fedora only. I haven't figured out a way to build and securely distribute RPMs yet, 
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

## Non-RPM installation

I haven't yet produced a convenient installer for other linux distros, but it shouldn't be that hard to make **exactly** running without
the installer. 

### Server
The server is a standard mavenized Java 1.8 project. It needs to be built by standard `mvn clean install` command (in server folder).
This will produce `server/target/exactly-server-<version>.jar` file. This is runnable by 
 
 `java -jar exactly-server-<version>.jar --dir=<root>`
 
 where `<root>` is the folder to be indexed

### Client

You need to be root to perform some of this. Create file `/opt/exactly/lib/python/VERSION` with version string 
(it should reflect the current version, e.g. same as in the `exactly-server-<version>.jar` filename). Then the
python client should be installable with `python3 setup.py install` inside of the client folder. After this you could place
client/bin/exactly into your /usr/bin/exactly and you should be fine.


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
  "max_hits": 3,
  "max_context": 20,
  "offset": 0
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

#### Request params:

- **pattern** - Base64 encoded binary string to search for
- **max_hits** - Maximum number of hits to return. Since the pattern can be even a single letter, the search query result size can be potentially quite huge, thus we need to limit the number of hits.
- **max_context** - Max number of bytes before and after the pattern that will be included in each hit to give the context to the position of the found pattern.
- **offset** - Optional parameter, if there are more pattern hits than max_hits, return segment starting at offset in complete hit list

#### Response format:

- **hits** - JSON array of hit JSON objects
- **cursor** - JSON Object representing the hit array cursor. If this object is not present this means
that the returned **hits** array is complete. If present this means that the array is only a portion of bigger array
that wasn't returned complete due to **max_hits** limitation

#### Hit format:

- **pos** - position of the hit in the document
- **doc_id** - string ID of the document, currently this is a file name
- **ctx_before** - Base64 encoded context *before* the pattern occurence
- **ctx_after** - Base64 encoded context *after* the pattern occurence

#### Cursor format:

- **complete_size** - size of the complete search result (number of hits)
- **offset** - offset of this result's segment in the complete array

Usually what you want to do if you receive incomplete response (with cursor element present) is to POST /search
again with offset increased by max_hits.

### Document retrieval

*GET http://localhost:9201/document/{document_idx}*

Will retrieve document by its index (order in which it was indexed)

*POST http://localhost:9201/document*  (Content-Type: application/json)

With request data:
```json
{
  "document_id": "/home/mlinhard/Documents/textfile1.txt",
  "document_index": 3
}
```
Can be used to retrieve the documents both by index and their string ID (usually path).

#### Response format:

Example
```json
{
  "document_id": "/home/mlinhard/Documents/textfile1.txt",
  "document_index": 3,
  "content": "cGF0dGVybg=="
}
```

- **document_id** - Document string ID, usually path
- **document_index** - Document index (order in which it was indexed)
- **content** - Base64 encoded binary document content

# TODO

- [ ] Improve console - display document paths and more context and more hits on demand
- [ ] Improve console - display server connection / indexing status in status-bar
- [ ] Add JVM stats (mainly memory usage)
- [ ] Enhanced suffix array memory optimization
- [ ] Hexadecimal console mode (allow search for binary strings)
- [ ] Add memory performance test (comparison with Lucene)
- [ ] Change the included Swing GUI code to REST client mode
- [ ] Allow UTF-8 String searches in GUI mode
- [ ] Better test coverage

