package com.marklogic.cookbook;

import com.marklogic.client.DatabaseClient;
import com.marklogic.client.DatabaseClientFactory;
import com.marklogic.client.document.GenericDocumentManager;
import com.marklogic.client.io.SearchHandle;
import com.marklogic.client.query.QueryManager;
import com.marklogic.client.query.StructuredQueryBuilder;
import com.marklogic.client.query.StructuredQueryDefinition;
import org.jdom2.Document;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.StringReader;

public class InsertDocumentTest {

    DatabaseClient client;
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    @Before
    public void init() {
        // Create a connection to the MarkLogic database
        DatabaseClientFactory.DigestAuthContext authContext =
                new DatabaseClientFactory.DigestAuthContext("admin", "admin");
        client = DatabaseClientFactory.newClient("oscar", 8200, authContext);
    }

    @Test
    public void insertXmlDocumentTest() {

        // Insert XML Document
        new InsertDocument().insertXmlDocument();

        // Search for document in test collection
        QueryManager queryMgr = client.newQueryManager();
        StructuredQueryBuilder qb = new StructuredQueryBuilder("default");

        StructuredQueryDefinition querydef =
                qb.and(
                        qb.collection("test-xml"));

        logger.info(getPrettyXml(querydef.serialize()));

        SearchHandle results = queryMgr.search(querydef, new SearchHandle());
        Assert.assertEquals(1, results.getTotalResults());
    }

    @Test
    public void insertJsonDocumentTest() throws IOException {

        // Insert JSON Document
        new InsertDocument().insertJsonDocument();

        // Search for document in test collection
        QueryManager queryMgr = client.newQueryManager();
        StructuredQueryBuilder qb = new StructuredQueryBuilder("default");

        StructuredQueryDefinition querydef =
                qb.and(
                        qb.collection("test-json"));

        logger.info(getPrettyXml(querydef.serialize()));

        SearchHandle results = queryMgr.search(querydef, new SearchHandle());
        Assert.assertEquals(1, results.getTotalResults());
    }

    public String getPrettyXml(String xml) {
        Document internalDoc = null;
        try {
            internalDoc = new SAXBuilder().build(new StringReader(xml));
        } catch (JDOMException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new XMLOutputter(Format.getPrettyFormat()).outputString(internalDoc);
    }
}
