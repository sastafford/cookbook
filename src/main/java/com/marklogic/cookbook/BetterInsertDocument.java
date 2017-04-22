package com.marklogic.cookbook;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.marklogic.client.DatabaseClient;
import com.marklogic.client.document.DocumentManager;
import com.marklogic.client.document.DocumentUriTemplate;
import com.marklogic.client.document.JSONDocumentManager;
import com.marklogic.client.helper.DatabaseClientProvider;
import com.marklogic.client.io.DocumentMetadataHandle;
import com.marklogic.client.io.Format;
import com.marklogic.client.io.JacksonHandle;
import com.marklogic.client.io.StringHandle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.io.IOException;

public class BetterInsertDocument {

    protected final Logger logger = LoggerFactory.getLogger(getClass());
    private DatabaseClient client;

    public BetterInsertDocument(DatabaseClient client) {
        this.client = client;
    }

    public void insertXmlDocument() {

        // Add Content
        String xml = "<hello>world</hello>";

        // Map content into a Handle
        StringHandle handle = new StringHandle(xml);

        // Generate Document Metadata (optional)
        long byteLength = xml.getBytes().length;
        handle.setByteLength(byteLength);
        int hashCode = handle.hashCode();

        DocumentMetadataHandle metadata =
                new DocumentMetadataHandle()
                        .withMetadataValue("hash", Integer.toString(hashCode))
                        .withProperty("bytelength", Long.toString(byteLength))
                        .withCollections("test-xml");

        // Create a DocumentManager and add the Document
        DocumentManager docMgr = client.newDocumentManager();

        // Create URI
        DocumentUriTemplate uriTemplate =
                docMgr.newDocumentUriTemplate("xml")
                        .withDirectory("/test/")
                        .withFormat(Format.XML);

        docMgr.create(uriTemplate, metadata, handle);

    }

    public void insertJsonDocument() throws IOException {

        // Add Content
        ObjectMapper mapper = new ObjectMapper();
        String json = "{ \"hello\": \"world\" }";
        JsonNode node = mapper.readTree(json);

        // Map content into a Handle
        JacksonHandle handle = new JacksonHandle(node);

        // Generate Document Metadata (optional)
        long byteLength = json.getBytes().length;
        handle.setByteLength(byteLength);
        int hashCode = handle.hashCode();

        DocumentMetadataHandle metadata =
                new DocumentMetadataHandle()
                        .withMetadataValue("hash", Integer.toString(hashCode))
                        .withProperty("bytelength", Long.toString(byteLength))
                        .withCollections("test-json");

        // Create a DocumentManager and add the Document
        JSONDocumentManager docMgr = client.newJSONDocumentManager();

        // Create URI
        String uri = "/test/sample-" + hashCode + ".json";

        docMgr.write(uri, metadata, handle);

    }
}
