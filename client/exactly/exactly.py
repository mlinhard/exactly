'''
Created on 12 Jul 2018

@author: mlinhard
'''

import requests
import json
import base64
from requests.exceptions import ConnectionError


class Document(object):

    def __init__(self, document_id, document_index, content):
        self.document_id = document_id
        self.document_index = document_index
        self.content = content

    @staticmethod 
    def from_json(json):
        document_id = json["document_id"]
        document_index = json["document_index"]
        content = base64.b64decode(json["content"])
        return Document(document_id, document_index, content)


class Stats(object):

    def __init__(self, indexed_bytes, indexed_files, done_crawling, done_loading, done_indexing):
        self.indexed_bytes = indexed_bytes
        self.indexed_files = indexed_files
        self.done_crawling = done_crawling
        self.done_loading = done_loading
        self.done_indexing = done_indexing
       
    @staticmethod 
    def from_json(json):
        indexed_bytes = json["indexed_bytes"]
        indexed_files = json["indexed_files"]
        done_crawling = json["done_crawling"]
        done_loading = json["done_loading"]
        done_indexing = json["done_indexing"]
        return Stats(indexed_bytes, indexed_files, done_crawling, done_loading, done_indexing)
       
    
class Hit(object):

    def __init__(self, position, document_id, before, after):
        self.position = position
        self.document_id = document_id
        self.before = before
        self.after = after

    @staticmethod
    def from_json(json):
        position = int(json["pos"])
        document_id = json["doc_id"]
        before = base64.b64decode(json["ctx_before"])
        after = base64.b64decode(json["ctx_after"])
        return Hit(position, document_id, before, after)


class Query(object):

    def __init__(self, pattern_bytes, max_hits=100, max_context=40, offset=0):
        self.pattern_bytes = pattern_bytes
        self._max_hits = max_hits
        self._max_context = max_context
        self._offset = offset
        
    def json(self):
        json = {
            "pattern" : base64.b64encode(self.pattern_bytes).decode("utf-8"),
            "max_hits" :  self._max_hits,
            "max_context" : self._max_context }
        if self._offset != 0:
            json["offset"] = self._offset
        return json


class SearchResult(object):
    
    def __init__(self, hits, complete_size):
        self._hits = hits
        self._complete_size = complete_size
        
    def hits(self):
        return self._hits
        
    def complete_size(self):
        if self._complete_size == None:
            return len(self._hits)
        else:
            return self._complete_size
    
    def complete_size_marker(self):
        return self._complete_size
  
    def is_complete(self):
        return self._complete_size == None or self._complete_size <= len(self._hits)
            
    @staticmethod
    def from_json(json):
        json_hits = json.get("hits")
        if json_hits == None:
            raise Exception("Unexpected response format")
        hits = [Hit.from_json(json_hit) for json_hit in json_hits]
        cursor = json.get("cursor")
        complete_size = cursor.get("complete_size") if cursor != None else None
        return SearchResult(hits, complete_size)


class ExactlyClient(object):
    '''
    classdocs
    '''

    def __init__(self, uri="http://localhost:9201"):
        '''
        Constructor
        '''
        self.uri = uri

    def get(self, relpath):
        try:
            return requests.get(self.uri + relpath)
        except ConnectionError:
            raise Exception("Can't connect to index at " + self.uri + ". Make sure that the index is running")
        except Exception as e:
            raise e

    def post(self, relpath, json_req):
        try:
            return requests.post(self.uri + relpath, json=json.dumps(json_req))
        except ConnectionError:
            raise Exception("Can't connect to index at " + self.uri + ". Make sure that the index is running")
        except Exception as e:
            raise e

    def document_by_index(self, document_index):
        r = self.get("/document/" + document_index)
        if r.status_code == 200:
            return Document.from_json(r.json())
        else:
            return None
        
    def document_by_id(self, document_id):
        r = self.post("/document", { "document_id" : document_id })
        if r.status_code == 200:
            return Document.from_json(r.json())
        else:
            return None
        
    def search(self, query):
        r = self.post("/search", query.json())
        if r.status_code == 200:
            return SearchResult.from_json(r.json())
        else:
            return None
        
    def stats(self):
        r = self.get("/stats")
        if r.status_code == 200:
            return Stats.from_json(r.json())
        else:
            return None

