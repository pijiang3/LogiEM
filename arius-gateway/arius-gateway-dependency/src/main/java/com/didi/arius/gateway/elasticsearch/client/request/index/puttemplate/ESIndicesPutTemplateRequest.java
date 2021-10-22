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

package com.didi.arius.gateway.elasticsearch.client.request.index.puttemplate;

import com.alibaba.fastjson.JSON;
import com.didi.arius.gateway.elasticsearch.client.model.ESActionRequest;
import com.didi.arius.gateway.elasticsearch.client.model.ESActionResponse;
import com.didi.arius.gateway.elasticsearch.client.model.RestRequest;
import com.didi.arius.gateway.elasticsearch.client.model.RestResponse;
import com.didi.arius.gateway.elasticsearch.client.model.type.ESVersion;
import com.didi.arius.gateway.elasticsearch.client.response.indices.puttemplate.ESIndicesPutTemplateResponse;
import com.didi.arius.gateway.elasticsearch.client.response.setting.template.TemplateConfig;
import org.elasticsearch.action.ActionRequestValidationException;

public class ESIndicesPutTemplateRequest extends ESActionRequest<ESIndicesPutTemplateRequest> {
    private String template;
    private String templateConfig;
    private ESVersion esVersion = ESVersion.ES233;


    public ESIndicesPutTemplateRequest setESVersion(ESVersion version) {
        this.esVersion = version;
        return this;
    }

    public ESIndicesPutTemplateRequest setTemplate(String template) {
        this.template = template;
        return this;
    }


    public ESIndicesPutTemplateRequest setTemplateConfig(String templateConfig) {
        this.templateConfig = templateConfig;
        return this;
    }


    public ESIndicesPutTemplateRequest setTemplateConfig(TemplateConfig templateConfig) {
        this.templateConfig = templateConfig.toJson(esVersion).toJSONString();
        return this;
    }


    @Override
    public RestRequest toRequest() throws Exception {
        if(template==null || template.length()==0) {
            throw new Exception("template is null");
        }

        if(templateConfig==null || templateConfig.length()==0) {
            throw new Exception("template config is null");
        }

        String endPoint = "/_template/" + template;

        RestRequest rr = new RestRequest("PUT", endPoint, null);
        rr.setBody(templateConfig);
        return rr;
    }

    @Override
    public ESActionResponse toResponse(RestResponse response) throws Exception {
        String respStr = response.getResponseContent();
        return JSON.parseObject(respStr, ESIndicesPutTemplateResponse.class);
    }

    @Override
    public ActionRequestValidationException validate() {
        return null;
    }
}
