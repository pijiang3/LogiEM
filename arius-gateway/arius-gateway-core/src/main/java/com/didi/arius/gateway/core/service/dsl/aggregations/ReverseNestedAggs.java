package com.didi.arius.gateway.core.service.dsl.aggregations;

import com.didi.arius.gateway.common.metadata.AggsBukcetInfo;
import com.didi.arius.gateway.common.metadata.FieldInfo;
import com.google.gson.JsonObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Map;

@Component("reverseNestedAggs")
public class ReverseNestedAggs extends BucketAggsType {
	private String name = "reverse_nested";
	
	@Autowired
	private AggsTypes aggsTypes;
	
	@PostConstruct
	public void init() {
		aggsTypes.putAggsType(name, this);
	}
	
	@Override
	public AggsBukcetInfo computeAggsType(JsonObject item, Map<String, FieldInfo> mergedMappings) {
		return AggsBukcetInfo.createSingleBucket();
	}

}
