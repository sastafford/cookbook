package com.marklogic.cookbook;

import com.marklogic.client.DatabaseClient;
import com.marklogic.client.io.SearchHandle;
import com.marklogic.client.query.QueryManager;
import com.marklogic.client.query.StructuredQueryBuilder;
import com.marklogic.client.query.StructuredQueryDefinition;
import com.marklogic.junit.Fragment;
import com.marklogic.junit.MarkLogicNamespaceProvider;
import com.marklogic.junit.NamespaceProvider;
import com.marklogic.junit.spring.AbstractSpringTest;
import org.jdom2.Namespace;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(classes = {com.marklogic.junit.spring.BasicTestConfig.class})
public class BestInsertDocumentTest extends AbstractSpringTest {

    DatabaseClient client;

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    @Before
    public void init() {
        client = getClient();
    }

    @Test
    public void insertDocumentsWithDataMovementSdkTest() throws Exception {

        // Insert XML Document
        new BestInsertDocument(client).insertJsonDocuments();

        // Search for document in test collection
        QueryManager queryMgr = client.newQueryManager();
        StructuredQueryBuilder qb = new StructuredQueryBuilder("default");

        StructuredQueryDefinition querydef =
                qb.and(
                        qb.collection("dmsdk"));

        Fragment frag = new Fragment(querydef.serialize());
        logger.info(frag.getPrettyXml());

        SearchHandle results = queryMgr.search(querydef, new SearchHandle());
        Assert.assertEquals(5000, results.getTotalResults());
    }
}
