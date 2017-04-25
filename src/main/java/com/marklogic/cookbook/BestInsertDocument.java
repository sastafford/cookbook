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
import com.marklogic.client.io.StringHandle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class BestInsertDocument {

    final int BATCH_SIZE = 50;

    protected final Logger logger = LoggerFactory.getLogger(getClass());
    private DatabaseClient client;

    public BestInsertDocument(DatabaseClient client) {
        this.client = client;
    }

    interface XmlString {
        String toString(int i);
    }

    public void insertJsonDocuments() throws Exception {

        DataMovementManager manager = client.newDataMovementManager();
        WriteBatcher writer = manager
                .newWriteBatcher()
                .withJobName("writeSampleDocs")
                .withBatchSize(BATCH_SIZE)
                .withThreadCount(4)
                .onBatchSuccess(batch -> logger.info(Long.toString(batch.getJobWritesSoFar())))
                .onBatchFailure((batch, throwable) -> logger.warn("Batch write fail"));
        JobTicket ticket = manager.startJob(writer);

        XmlString xml = (int i) -> { return "<output>" + i + "</output>"; };

        for (int loop = 0; loop < 100; loop++) {
            List<DocumentWriteOperation> docs = new ArrayList<DocumentWriteOperation>();
            for (int i = 0; i < BATCH_SIZE; i++) {
                StringHandle handle = new StringHandle(xml.toString(i));
                DocumentMetadataHandle metadata =
                        new DocumentMetadataHandle().withCollections("dmsdk");
                DocumentWriteOperation doc =
                        new DocumentWriteOperationImpl(
                                DocumentWriteOperation.OperationType.DOCUMENT_WRITE,
                                "/test/sample-" + (loop * 100 + i) + ".xml", metadata, handle);
                docs.add(doc);
            }
            for (DocumentWriteOperation item: docs) {
                writer.add(item.getUri(), item.getMetadata(), item.getContent());
            }
            writer.flushAndWait();
            docs.clear();
        }
        manager.stopJob(writer);
        manager.release();

    }

    public void write(List<? extends DocumentWriteOperation> items) throws Exception {


    }
}
