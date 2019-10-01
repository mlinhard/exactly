package server

import (
	"bytes"
	"encoding/json"
	"fmt"
	"io/ioutil"
	"net/http"
	"testing"

	"github.com/mlinhard/exactly/server/search"
)

type testClient struct {
	t          *testing.T
	baseUrl    string
	maxContext int
	maxHits    int
}

type testClientResponse struct {
	client   *testClient
	response *SearchResponse
}

type testClientHit struct {
	response *testClientResponse
	Index    int
	hit      Hit
}

func (this *testClient) DocumentCount() int {
	return 1
}

func (this *testClient) Document(i int) *search.Document {
	request := new(DocumentRequest)
	response := new(DocumentResponse)
	request.DocumentIndex = &i
	err := this.postJson("/document", request, response)
	if err != nil {
		this.t.Error(err)
		return nil
	}
	doc := new(search.Document)
	doc.Content = response.Content
	doc.Id = response.DocumentId
	doc.Index = response.DocumentIndex
	return doc
}

func (this *testClient) search(pattern string) *testClientResponse {
	return this.searchBounded(pattern, 0, this.maxContext, this.maxHits)
}

func (this *testClient) searchBounded(pattern string, offset, maxContext, maxHits int) *testClientResponse {
	request := new(SearchRequest)
	response := new(SearchResponse)
	request.MaxContext = maxContext
	request.MaxHits = maxHits
	request.Offset = &offset
	request.Pattern = []byte(pattern)
	err := this.postJson("/search", request, response)
	if err != nil {
		this.t.Error(err)
		return nil
	}
	return &testClientResponse{this, response}
}

func (this *testClient) postJson(relUrl string, request, response interface{}) error {
	reqData, err := json.Marshal(request)
	if err != nil {
		this.t.Errorf("marshalling: %v", err)
		return nil
	}
	var httpResp *http.Response
	httpResp, err = http.Post(this.baseUrl+relUrl, "application/json", bytes.NewReader(reqData))
	if err != nil {
		this.t.Errorf("http post: %v", err)
		return nil
	}
	respData, err := ioutil.ReadAll(httpResp.Body)
	fmt.Printf("received resp: %v", string(respData))
	err = json.Unmarshal(respData, response)
	if err != nil {
		this.t.Errorf("unmarshalling: %v", err)
	}
	return nil
}

func (this *testClientResponse) AssertHitCount(hitCount int) {
	if len(this.response.Hits) != hitCount {
		this.client.t.Errorf("Unexpected hit count %v (expected %v)", len(this.response.Hits), hitCount)
	}
}

func (this *testClientResponse) AssertHitDocId(docId string) *testClientHit {
	for hitIdx, hit := range this.response.Hits {
		if hit.DocumentId == docId {
			return &testClientHit{this, hitIdx, hit}
		}
	}
	this.client.t.Errorf("Document ID %v not found", docId)
	return nil
}

func (this *testClientResponse) AssertNoCursor() {
	c := this.response.Cursor
	if c != nil {
		this.client.t.Errorf("Expected no cursor, but cursor offset=%v, total=%v present", c.Offset, c.CompleteSize)
	}
}

func (this *testClientResponse) AssertCursor(offset, completeSize int) {
	c := this.response.Cursor
	if c == nil {
		this.client.t.Errorf("Expected cursor offset=%v, total=%v not found", offset, completeSize)
	}
	if c.Offset != offset {
		this.client.t.Errorf("Expected cursor offset %v but got %v", offset, c.Offset)
	}
	if c.CompleteSize != completeSize {
		this.client.t.Errorf("Expected cursor total %v but got %v", completeSize, c.CompleteSize)
	}
}

func (this *testClientHit) AssertBefore(ctx string) *testClientHit {
	if this == nil {
		return nil
	}
	actualCtx := string(this.hit.ContextBefore)
	if actualCtx != ctx {
		this.response.client.t.Errorf("Expected before context %v and got %v", ctx, actualCtx)
	}
	return this
}

func (this *testClientHit) AssertAfter(ctx string) *testClientHit {
	if this == nil {
		return nil
	}
	actualCtx := string(this.hit.ContextAfter)
	if actualCtx != ctx {
		this.response.client.t.Errorf("Expected after context %v and got %v", ctx, actualCtx)
	}
	return this
}

func (this *testClientHit) AssertPosition(position int) *testClientHit {
	if this == nil {
		return nil
	}
	if this.hit.Position != position {
		this.response.client.t.Errorf("Unexpected position %v in hit %v (expected %v)", this.hit.Position, this.Index, position)
	}
	return this
}

func (this *testClientHit) AssertIndex(hitIdx int) *testClientHit {
	if this == nil {
		return nil
	}
	if this.Index != hitIdx {
		this.response.client.t.Errorf("Unexpected hit index %v for doc %v (expected %v)", this.Index, this.hit.DocumentId, hitIdx)
	}
	return this
}
