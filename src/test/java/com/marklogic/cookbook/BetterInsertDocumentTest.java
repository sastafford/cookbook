package com.marklogic.cookbook;

import com.marklogic.client.DatabaseClient;
import com.marklogic.client.io.SearchHandle;
import com.marklogic.client.query.QueryManager;
import com.marklogic.client.query.StructuredQueryBuilder;
import com.marklogic.client.query.StructuredQueryDefinition;
import com.marklogic.junit.Fragment;
import com.marklogic.junit.spring.AbstractSpringTest;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ContextConfiguration;

import java.io.IOException;

@ContextConfiguration(classes = {com.marklogic.junit.spring.BasicTestConfig.class})
public class BetterInsertDocumentTest extends AbstractSpringTest {

    DatabaseClient client;

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    @Before
    public void init() {
        client = getClient();
    }

    @Test
    public void insertXmlDocumentTest() {

        // Insert XML Document
        new BetterInsertDocument(client).insertXmlDocument();

        // Search for document in test collection
        QueryManager queryMgr = client.newQueryManager();
        StructuredQueryBuilder qb = new StructuredQueryBuilder("default");

        StructuredQueryDefinition querydef =
                qb.and(
                        qb.collection("test-xml"));

        Fragment frag = new Fragment(querydef.serialize(), null);
        logger.info(frag.getPrettyXml());

        SearchHandle results = queryMgr.search(querydef, new SearchHandle());
        Assert.assertEquals(1, results.getTotalResults());
    }

    @Test
    public void insertJsonDocumentTest() throws IOException {

        // Insert JSON Document
        new BetterInsertDocument(client).insertJsonDocument();

        // Search for document in test collection
        QueryManager queryMgr = client.newQueryManager();
        StructuredQueryBuilder qb = new StructuredQueryBuilder("default");

        StructuredQueryDefinition querydef =
                qb.and(
                        qb.collection("test-json"));

        Fragment frag = new Fragment(querydef.serialize(), null);
        logger.info(frag.getPrettyXml());

        SearchHandle results = queryMgr.search(querydef, new SearchHandle());
        Assert.assertEquals(1, results.getTotalResults());
    }
}
