package ma.emsi.samih.tp4websamih.llm;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class LlmClient {

    private final ChatModel model;

    public LlmClient() {
        String apiKey = System.getenv("GEMINI_KEY");
        if (apiKey == null || apiKey.trim().isEmpty()) {
            throw new IllegalStateException("GEMINI_KEY environment variable is not set.");
        }

        this.model = GoogleAiGeminiChatModel.builder()
                .apiKey(apiKey)
                .modelName("gemini-1.5-flash")
                .build();
    }

    /**
     * Sends a chat request to the LLM.
     *
     * @param prompt The full conversation history or a single prompt.
     * @return The response from the LLM.
     */
    public String chat(String prompt) {
        if (prompt == null || prompt.trim().isEmpty()) {
            return "Prompt cannot be empty.";
        }
        return model.generate(prompt);
    }

    /**
     * Sends a chat request with a system role and a user question.
     * This can be used to start a new conversation with a specific context.
     *
     * @param systemRole The system role to set the context for the LLM.
     * @param question   The user's question.
     * @return The response from the LLM.
     */
    public String chat(String systemRole, String question) {
        String fullPrompt = systemRole + "\n\n== User:\n" + question;
        return chat(fullPrompt);
    }
}