package ma.emsi.samih.tp4websamih.rag;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentParser;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.document.parser.apache.pdfbox.ApachePdfBoxDocumentParser;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.onnx.allminilml6v2.AllMiniLmL6V2EmbeddingModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingSearchResult;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@ApplicationScoped
public class RagService {

    private static final Logger LOGGER = Logger.getLogger(RagService.class.getName());

    private final EmbeddingStore<TextSegment> embeddingStore;
    private final EmbeddingModel embeddingModel;

    public RagService() {
        embeddingModel = new AllMiniLmL6V2EmbeddingModel();
        embeddingStore = new InMemoryEmbeddingStore<>();

        List<String> pdfFileNames = Arrays.asList(
                "Embeddings et utilisation des LMs.pdf",
                "Introduction agents et MCP.pdf"
        );

        for (String pdfFileName : pdfFileNames) {
            try (InputStream inputStream = getClass().getResourceAsStream("/" + pdfFileName)) {
                if (inputStream == null) {
                    LOGGER.log(Level.SEVERE, "PDF file not found in classpath: " + pdfFileName);
                    continue;
                }

                Path tempFile = Files.createTempFile("rag-doc-", ".pdf");
                Files.copy(inputStream, tempFile, StandardCopyOption.REPLACE_EXISTING);

                DocumentParser documentParser = new ApachePdfBoxDocumentParser();
                try (InputStream fileInputStream = Files.newInputStream(tempFile)) {
                    Document document = documentParser.parse(fileInputStream);

                    // Split the document into smaller segments
                    List<TextSegment> segments = DocumentSplitters.recursive(500, 0).split(document);

                    for (TextSegment segment : segments) {
                        Embedding embedding = embeddingModel.embed(segment.text()).content();
                        embeddingStore.add(embedding, segment);
                    }
                }

                Files.delete(tempFile); // Clean up the temporary file

            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Error loading PDF file: " + pdfFileName, e);
            }
        }
    }

    public String answer(String question) {
        Embedding questionEmbedding = embeddingModel.embed(question).content();
        EmbeddingSearchRequest searchRequest = EmbeddingSearchRequest.builder()
                .queryEmbedding(questionEmbedding)
                .maxResults(1)
                .build();
        EmbeddingSearchResult<TextSegment> searchResult = embeddingStore.search(searchRequest);
        List<EmbeddingMatch<TextSegment>> relevantEmbeddings = searchResult.matches();
        if (relevantEmbeddings.isEmpty()) {
            return "No relevant information found in the documents for your question.";
        }
        EmbeddingMatch<TextSegment> embeddingMatch = relevantEmbeddings.get(0);

        return embeddingMatch.embedded().text();
    }
}
