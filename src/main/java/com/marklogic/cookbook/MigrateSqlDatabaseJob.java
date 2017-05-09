package com.marklogic.cookbook;

import com.marklogic.client.DatabaseClient;
import com.marklogic.client.datamovement.*;
import com.marklogic.client.document.DocumentPatchBuilder;
import com.marklogic.client.document.DocumentWriteOperation;
import com.marklogic.client.document.XMLDocumentManager;
import com.marklogic.client.impl.DocumentWriteOperationImpl;
import com.marklogic.client.io.DocumentMetadataHandle;
import com.marklogic.client.io.StringHandle;
import com.marklogic.client.io.marker.DocumentPatchHandle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.*;
import org.springframework.batch.core.annotation.AfterJob;
import org.springframework.batch.core.annotation.BeforeJob;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@EnableBatchProcessing
public class MigrateSqlDatabaseJob implements JobExecutionListener {

    @Autowired
    DatabaseClient client;

    private int BATCH_SIZE = 10;

    protected final Logger logger = LoggerFactory.getLogger(getClass());
    private DataMovementManager manager;
    private WriteBatcher writer;

    @Bean(name = "importCustomersJob")
    public Job job(JobBuilderFactory jobBuilderFactory, Step step) {
        return jobBuilderFactory.get("importCustomersJob")
                .start(step)
                .listener(this)
                .build();
    }

    @Bean
    @JobScope
    public Step step1(StepBuilderFactory stepBuilderFactory) {
        JdbcCursorItemReader<Customer> reader = new JdbcCursorItemReader<Customer>();
        reader.setRowMapper(new RowMapper<Customer>() {
            @Override
            public Customer mapRow(ResultSet rs, int rowNum) throws SQLException {
                return new Customer(rs.getInt(1), rs.getString(2), rs.getString(3));
            }
        });
        DriverManagerDataSource ds = new DriverManagerDataSource("jdbc:h2:file:./data/sample", "sa", "");
        ds.setDriverClassName("org.h2.Driver");
        reader.setDataSource(ds);
        reader.setSql("SELECT * FROM CUSTOMER");

        ItemProcessor<Customer, DocumentWriteOperation> processor = new ItemProcessor<Customer, DocumentWriteOperation>() {
            @Override
            public DocumentWriteOperation process(Customer item) throws Exception {
                String uri = item.id + ".xml";
                DocumentMetadataHandle metadata = new DocumentMetadataHandle().withCollections("customer");
                return new DocumentWriteOperationImpl(
                        DocumentWriteOperation.OperationType.DOCUMENT_WRITE,
                        uri, metadata, new StringHandle(item.toXml()));
            }
        };

        ItemWriter<DocumentWriteOperation> itemWriter  = new ItemWriter<DocumentWriteOperation>() {

            @Override
            public void write(List<? extends DocumentWriteOperation> items) throws Exception {
                for (DocumentWriteOperation item: items) {
                    writer.add(item.getUri(), item.getMetadata(), item.getContent());
                }
                writer.flushAndWait();
            }
        };

        return stepBuilderFactory.get("step")
                .<Customer, DocumentWriteOperation>chunk(10)
                .reader(reader)
                .processor(processor)
                .writer(itemWriter)
                .build();
    }

    @BeforeJob
    public void beforeJob(JobExecution jobExecution) {
        manager = client.newDataMovementManager();
        writer = manager
                .newWriteBatcher()
                .withJobName("writeSampleDocs")
                .withBatchSize(BATCH_SIZE)
                .withThreadCount(4)
                .onBatchFailure((batch, throwable) -> logger.warn("Batch write fail"));
        writer.setBatchSuccessListeners(new WriteStatusToMarkLogic(client));
        writer.onBatchSuccess(batch -> logger.info(Long.toString(batch.getJobWritesSoFar())));
        JobTicket ticket = manager.startJob(writer);
    }

    @AfterJob
    public void afterJob(JobExecution jobExecution) {
        manager.stopJob(writer);
        manager.release();
    }

    public class WriteStatusToMarkLogic implements WriteBatchListener {

        DatabaseClient client;
        XMLDocumentManager xmlDocMgr;
        JobTicket ticket;
        DocumentMetadataHandle metadata = new DocumentMetadataHandle().withCollections("job-ticket");
        String uri;
        boolean ticketDocNotCreated = true;

        public WriteStatusToMarkLogic(DatabaseClient client) {
            xmlDocMgr = client.newXMLDocumentManager();
        }

        private void initJobTicket(JobTicket ticket) {
            uri = "/ticket/" + ticket.getJobId() + ".xml";
            String xml =
                    "<ticket>" +
                            "<id>" + ticket.getJobId() + "</id>" +
                            "<name>" + ticket.getJobType() + "</name>" +
                    "</ticket>";
            xmlDocMgr.write(uri, metadata, new StringHandle(xml));
        }

        @Override
        public void processEvent(WriteBatch batch) {
            JobTicket ticket = batch.getJobTicket();
            if (ticketDocNotCreated) {
                initJobTicket(ticket);
                ticketDocNotCreated = false;
            }
            DocumentPatchBuilder builder = xmlDocMgr.newPatchBuilder();
            builder.insertFragment("/ticket", DocumentPatchBuilder.Position.LAST_CHILD,
                    "<numberOfWrites>" + batch.getJobWritesSoFar()  +  "</numberOfWrites>");
            DocumentPatchHandle handle = builder.build();
            xmlDocMgr.patch(uri, handle);
        }
    }

    public class Customer {
        private String id;
        private String firstName;
        private String lastName;
        public Customer(int id, String firstName, String lastName) {
            this.id = Integer.toString(id);
            this.firstName = firstName;
            this.lastName = lastName;
        }
        public String toXml() {
            return "<customer id=\"" + this.id + "\">" +
                      "<last-name>" + this.lastName + "</last-name>" +
                      "<first-name>" + this.firstName + "</first-name>" +
                    "</customer>";
        }
    }
}
