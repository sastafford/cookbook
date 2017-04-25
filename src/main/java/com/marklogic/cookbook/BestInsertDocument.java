package com.marklogic.cookbook;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.marklogic.client.DatabaseClient;
import com.marklogic.client.datamovement.DataMovementManager;
import com.marklogic.client.datamovement.JobTicket;
import com.marklogic.client.datamovement.WriteBatcher;
import com.marklogic.client.document.DocumentWriteOperation;
import com.marklogic.client.impl.DocumentWriteOperationImpl;
import com.marklogic.client.io.DocumentMetadataHandle;
import com.marklogic.client.io.JacksonHandle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class BestInsertDocument {

    final int BATCH_SIZE = 50;

    protected final Logger logger = LoggerFactory.getLogger(getClass());
    private DatabaseClient client;
    DataMovementManager manager;
    WriteBatcher writer;
    JobTicket ticket;

    public BestInsertDocument(DatabaseClient client) {
        this.client = client;
        // Create a DocumentManager and add the Documents
        manager = client.newDataMovementManager();

        writer = manager
            .newWriteBatcher()
            .withJobName("writeSampleDocs")
            .withBatchSize(BATCH_SIZE)
            .withThreadCount(4)
            // Configure listeners for asynchronous lifecycle events
            // Success:
            .onBatchSuccess(batch ->
                logger.info(Long.toString(batch.getJobWritesSoFar())))
            // Failure:
            .onBatchFailure((batch, throwable) -> logger.warn("Batch write fail"));

        ticket = manager.startJob(writer);
    }

    public void insertJsonDocuments() throws Exception {
        for (int loop = 0; loop < 100; loop++) {
            List<DocumentWriteOperation> docs = new ArrayList<DocumentWriteOperation>();
            for (int i = 0; i < BATCH_SIZE; i++) {
                // Add Content
                ObjectMapper mapper = new ObjectMapper();
                String json = "{ \"value\":" + i + "}";
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

                String uri = "/test/sample-" + hashCode + ".json";

                DocumentWriteOperation doc =
                        new DocumentWriteOperationImpl(
                                DocumentWriteOperation.OperationType.DOCUMENT_WRITE,
                                uri, metadata, handle);
                docs.add(doc);
            }
            write(docs);
            docs.clear();
        }

        manager.stopJob(writer);
        manager.release();

    }

    public void write(List<? extends DocumentWriteOperation> items) throws Exception {
        for (DocumentWriteOperation item: items) {
            writer.add(item.getUri(), item.getMetadata(), item.getContent());
        }
        writer.flushAndWait();

    }
}
