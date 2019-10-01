# Exactly indexing server

This is the indexing server for [Exactly](https://github.com/mlinhard/exactly) written in *Go* language. It uses [sais-go](https://github.com/mlinhard/sais-go) which is Go language wrapper for Yuta Mori's [SAIS implementation](https://sites.google.com/site/yuta256/sais) for fast suffix array construction.

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

