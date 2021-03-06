package com.rick.scaffold.search.services.worker;

import java.util.Collection;
import java.util.List;

import com.rick.scaffold.search.SearchConstants;
import org.springframework.beans.factory.annotation.Autowired;

import com.rick.scaffold.search.services.RZSearchRequest;
import com.rick.scaffold.search.services.RZSearchResponse;
import com.rick.scaffold.search.services.delegate.SearchDelegate;
import com.rick.scaffold.search.utils.CustomIndexConfiguration;
import com.rick.scaffold.search.utils.SearchClient;

public class DeleteKeywordsImpl implements DeleteObjectWorker {

	private List<CustomIndexConfiguration> indexConfigurations = null;
	
	public List<CustomIndexConfiguration> getIndexConfigurations() {
		return indexConfigurations;
	}

	public void setIndexConfigurations(
			List<CustomIndexConfiguration> indexConfigurations) {
		this.indexConfigurations = indexConfigurations;
	}

	@Autowired
	private SearchDelegate searchDelegate;

	@Override
	public void deleteObject(SearchClient client, String index, String type, Long id) throws Exception {
		String keywordType = SearchConstants.KEYWORD + "_" + type;
		if (searchDelegate.indexExist(index)) {
			String query = new StringBuilder()
					.append("{\"query\":{\"term\" : {\"dbid\" : \"").append(id)
					.append("\" }}}").toString();
			RZSearchRequest sr = new RZSearchRequest();
			sr.setIndex(index);
			sr.setType(keywordType);
			sr.setJson(query);
			RZSearchResponse r = searchDelegate.search(sr);
			if (r != null) {
				Collection<String> ids = r.getIds();
				searchDelegate.bulkDeleteIndex(index, keywordType, ids);
			}
		}
	}

}
