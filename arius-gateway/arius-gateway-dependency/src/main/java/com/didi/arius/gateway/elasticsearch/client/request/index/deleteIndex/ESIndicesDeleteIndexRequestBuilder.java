/*
 * Licensed to Elasticsearch under one or more contributor
 * license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright
 * ownership. Elasticsearch licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.didi.arius.gateway.elasticsearch.client.request.index.deleteIndex;

import com.didi.arius.gateway.elasticsearch.client.response.indices.deleteindex.ESIndicesDeleteIndexResponse;
import org.elasticsearch.action.ActionRequestBuilder;
import org.elasticsearch.client.ElasticsearchClient;

public class ESIndicesDeleteIndexRequestBuilder extends ActionRequestBuilder<ESIndicesDeleteIndexRequest, ESIndicesDeleteIndexResponse, ESIndicesDeleteIndexRequestBuilder> {

    public ESIndicesDeleteIndexRequestBuilder(ElasticsearchClient client, ESIndicesDeleteIndexAction action) {
        super(client, action, new ESIndicesDeleteIndexRequest());
    }

    public ESIndicesDeleteIndexRequestBuilder setIndex(String index) {
        request.setIndex(index);
        return this;
    }
}
