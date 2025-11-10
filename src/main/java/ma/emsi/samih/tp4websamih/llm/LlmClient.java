package ma.emsi.samih.tp2websamih.llm;

import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;

import dev.langchain4j.service.AiServices;
import jakarta.enterprise.context.SessionScoped;

import java.io.Serializable;

@SessionScoped
public class LlmClient implements Serializable {

    public interface Assistant {
        String chat(String prompt);
    }

    private String systemRole;
    private final Assistant assistant;
    private final ChatMemory chatMemory;

    public LlmClient() {
        String apiKey = System.getenv("GEMINI_API_KEY");

        // Le modèle Gemini à utiliser. "gemini-1.5-flash" est une option plus courante que "gemini-2.5-flash".
        ChatModel model = GoogleAiGeminiChatModel.builder()
                .apiKey(apiKey)
                .modelName("gemini-2.0-flash")
                .build();

        this.chatMemory = MessageWindowChatMemory.withMaxMessages(10);

        // La méthode .chatModel() est dépréciée, .chatLanguageModel() est la méthode actuelle.
        this.assistant = AiServices.builder(Assistant.class)
                .chatModel(model)
                .chatMemory(chatMemory)
                .build();
    }

    /**
     * Définit le rôle système pour l'assistant.
     * Cela efface l'historique de la conversation précédente.
     * @param systemRole Le rôle à définir.
     */
    public void setSystemRole(String systemRole) {
        this.systemRole = systemRole;
        chatMemory.clear();
        if (systemRole != null && !systemRole.trim().isEmpty()) {
            chatMemory.add(SystemMessage.from(systemRole));
        }
    }

    /**
     * Envoie un prompt au LLM et obtient une réponse.
     * @param prompt Le prompt de l'utilisateur.
     * @return La réponse du LLM.
     */
    public String chat(String prompt) {
        return assistant.chat(prompt);
    }
}
