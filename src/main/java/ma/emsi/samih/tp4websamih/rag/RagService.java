package ma.emsi.samih.tp4websamih.rag;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentParser;
import dev.langchain4j.data.document.loader.FileSystemDocumentLoader;
import dev.langchain4j.data.document.parser.apache.pdfbox.ApachePdfBoxDocumentParser;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.AllMiniLmL6V2EmbeddingModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.util.List;

public class RagService {

    private final EmbeddingStore<TextSegment> embeddingStore;
    private final EmbeddingModel embeddingModel;

    public RagService() {
        embeddingModel = new AllMiniLmL6V2EmbeddingModel();
        embeddingStore = new InMemoryEmbeddingStore<>();

        try {
            URL resourceUrl = getClass().getResource("/");
            if (resourceUrl != null) {
                File resourceDir = new File(resourceUrl.toURI());
                File[] pdfFiles = resourceDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".pdf"));
                if (pdfFiles != null) {
                    for (File pdfFile : pdfFiles) {
                        try {
                            Path pdfPath = pdfFile.toPath();
                            DocumentParser documentParser = new ApachePdfBoxDocumentParser();
                            Document document = FileSystemDocumentLoader.loadDocument(pdfPath, documentParser);
                            TextSegment segment = TextSegment.from(document.text());
                            Embedding embedding = embeddingModel.embed(segment).content();
                            embeddingStore.add(embedding, segment);
                        } catch (Exception e) {
                            // Log the error for the specific file, but continue with others
                            System.err.println("Error loading PDF file: " + pdfFile.getName() + " - " + e.getMessage());
                        }
                    }
                }
            }
        } catch (URISyntaxException e) {
            throw new RuntimeException("Error accessing resources directory.", e);
        }
    }

    public String answer(String question) {
        Embedding questionEmbedding = embeddingModel.embed(question).content();
        List<EmbeddingMatch<TextSegment>> relevantEmbeddings = embeddingStore.findRelevant(questionEmbedding, 1);
        if (relevantEmbeddings.isEmpty()) {
            return "No relevant information found in the documents for your question.";
        }
        EmbeddingMatch<TextSegment> embeddingMatch = relevantEmbeddings.get(0);

        return embeddingMatch.embedded().text();
    }
}