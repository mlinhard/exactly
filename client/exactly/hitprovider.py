'''
Provides pattern hits

'''
from exactly.exactly import Query, SearchResult


class HitProvider(object):
    '''
    classdocs
    '''

    def __init__(self, client, view):
        self._client = client
        self._pattern = ""
        self._view = view
        self._hits = None
        self._complete_size = None
    
    def hits(self, offset):
        length = self._view.max_displayable_hits()
        end = offset + length
        self._ensure_hits(max([self._view.max_displayable_hits(), end]))
        if end <= self._num_hits():
            return self._hits[offset:end]
        else:
            return self._hits[offset:]
    
    def hit(self, index):
        self._ensure_hits(max([self._view.max_displayable_hits(), index + 1]))
        if index < self._num_hits():
            return self._hits[index]
        else:
            raise Exception("Index out of range: " + str(index))

    def pattern_append(self, c):
        self._pattern += chr(c)
        self._hits = None
        
    def pattern_back(self):
        self._pattern = self._pattern[0:-1]
        self._hits = None

    def _ensure_hits(self, max_hits):
        if self._hits == None:
            search_result = self._load_hits(0, max_hits)
            self._hits = search_result.hits()
            self._complete_size = search_result.complete_size_marker()
        else:
            cached = len(self._hits)
            if max_hits > cached and cached < self._num_hits():
                real_max_hits = min([self._num_hits(), max_hits])
                if real_max_hits > 0:
                    search_result = self._load_hits(cached, real_max_hits)
                    self._hits += search_result.hits()
                    if self._complete_size != search_result.complete_size_marker():
                        raise Exception("Different complete size returned for same query. Reindexing not yet supported")
            
    def has_hits(self):
        self._ensure_hits(self._view.max_displayable_hits())
        return len(self._hits) > 0
    
    def has_pattern(self):
        return len(self._pattern) > 0

    def pattern_bytes(self):
        return self._pattern.encode("utf-8")

    def num_hits(self):
        self._ensure_hits(self._view.max_displayable_hits())
        return self._num_hits()

    def _num_hits(self):
        return self._complete_size if self._complete_size != None else len(self._hits)

    def _load_hits(self, offset, max_hits):
        if self.has_pattern():
            pattern_bytes = self.pattern_bytes()
            max_context = self._view.max_displayable_context(len(pattern_bytes))
            return self._client.search(Query(pattern_bytes, max_hits, max_context, offset))
        else:
            return SearchResult([], None)
