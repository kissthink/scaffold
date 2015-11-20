package com.rick.scaffold.common.search.services.worker;

import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.rick.scaffold.common.search.services.delegate.SearchDelegate;
import com.rick.scaffold.common.search.utils.FileUtil;
import com.rick.scaffold.common.search.utils.IndexConfiguration;
import com.rick.scaffold.common.search.utils.SearchClient;
import com.rick.scaffold.common.utils.StringUtils;

public class ObjectIndexerImpl implements IndexWorker {

	private static boolean init = false;

	@Autowired
	private SearchDelegate searchDelegate;

	private List<IndexConfiguration> indexConfigurations;

	public List<IndexConfiguration> getIndexConfigurations() {
		return indexConfigurations;
	}

	public void setIndexConfigurations(
			List<IndexConfiguration> indexConfigurations) {
		this.indexConfigurations = indexConfigurations;
	}

	private static Logger log = Logger.getLogger(ObjectIndexerImpl.class);

	public synchronized void init(SearchClient client) {
		if (init) {
			return;
		}
		init = true;
		if (getIndexConfigurations() != null
				&& getIndexConfigurations().size() > 0) {

			for (IndexConfiguration config : indexConfigurations) {

				String mappingFile = null;
				String settingsFile = null;
				
				if (!StringUtils.isBlank(config.getMappingFileName())) {
					mappingFile = config.getMappingFileName();
				}
				if (!StringUtils.isBlank(config.getSettingsFileName())) {
					settingsFile = config.getSettingsFileName();
				}

				if (mappingFile != null || settingsFile != null) {
					String metadata = null;
					String settingsdata = null;
					try {
						if (mappingFile != null) {
							metadata = FileUtil.readFileAsString(mappingFile);
						}

						if (settingsFile != null) {
							settingsdata = FileUtil.readFileAsString(settingsFile);
						}

						if (!StringUtils.isBlank(config.getTypeName())) {
							if (!searchDelegate.indexExist(config.getIndiceName())) {
								searchDelegate.createIndice(metadata, settingsdata, config.getIndiceName(), config.getTypeName());
							}
						}
					} catch (Exception e) {
						log.error("******************Create index fail*******************", e);
						init = false;
					}
				}
			}
		}
	}

	//insert object into es
	public void execute(SearchClient client, String json, String index, String type, String id)
			throws Exception {
		try {
			if (!init) {
				init(client);
			}
			searchDelegate.index(json, index, type, id);
		} catch (Exception e) {
			log.error("Exception while indexing a product, maybe a timing ussue for no shards available", e);
		}

	}

}