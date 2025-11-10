package ma.emsi.samih.tp4websamih.jsf;

import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.model.SelectItem;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import ma.emsi.samih.tp4websamih.llm.LlmClient;
import ma.emsi.samih.tp4websamih.rag.RagService;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Named
@ViewScoped
public class Bb implements Serializable {

    private String roleSysteme;
    private boolean roleSystemeChangeable = true;
    private List<SelectItem> listeRolesSysteme;
    private String question;
    private String reponse;
    private StringBuilder conversation = new StringBuilder();

    // For RAG
    private String ragQuestion;
    private String ragAnswer;
    private RagService ragService;

    @Inject
    private FacesContext facesContext;

    @Inject
    private LlmClient llmClient;

    public Bb() {
    }

    @PostConstruct
    public void init() {
        try {
            ragService = new RagService();
        } catch (Exception e) {
            e.printStackTrace();
            FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_FATAL,
                    "Erreur RAG", "Impossible d'initialiser le service RAG. Vérifiez la présence des fichiers PDF.");
            facesContext.addMessage(null, message);
        }
    }

    public String envoyer() {
        if (question == null || question.isBlank()) {
            FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Texte question vide", "Il manque le texte de la question");
            facesContext.addMessage(null, message);
            return null;
        }

        try {
            if (conversation.isEmpty() && roleSysteme != null && !roleSysteme.isEmpty()) {
                this.reponse = llmClient.chat(roleSysteme, question);
            } else {
                this.reponse = llmClient.chat(conversation.toString() + "\n== User:\n" + question);
            }
        } catch (Exception e) {
            this.reponse = "Erreur: " + e.getMessage();
        }

        if (this.conversation.isEmpty()) {
            if(roleSysteme != null && !roleSysteme.isEmpty()){
                 this.conversation.append(roleSysteme.toUpperCase(Locale.FRENCH)).append("\n");
            }
            this.roleSystemeChangeable = false;
        }
        afficherConversation();
        return null;
    }

    public String askRag() {
        if (ragQuestion == null || ragQuestion.isBlank()) {
            FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Question RAG vide", "Il manque le texte de la question pour le RAG.");
            facesContext.addMessage(null, message);
            return null;
        }
        this.ragAnswer = ragService.answer(ragQuestion);
        return null;
    }

    public String nouveauChat() {
        return "index";
    }

    private void afficherConversation() {
        this.conversation.append("== User:\n").append(question).append("\n== Serveur:\n").append(reponse).append("\n");
    }

    // Getters and Setters

    public String getRoleSysteme() {
        return roleSysteme;
    }

    public void setRoleSysteme(String roleSysteme) {
        this.roleSysteme = roleSysteme;
    }

    public boolean isRoleSystemeChangeable() {
        return roleSystemeChangeable;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getReponse() {
        return reponse;
    }

    public void setReponse(String reponse) {
        this.reponse = reponse;
    }

    public String getConversation() {
        return conversation.toString();
    }

    public void setConversation(String conversation) {
        this.conversation = new StringBuilder(conversation);
    }

    public String getRagQuestion() {
        return ragQuestion;
    }

    public void setRagQuestion(String ragQuestion) {
        this.ragQuestion = ragQuestion;
    }

    public String getRagAnswer() {
        return ragAnswer;
    }

    public void setRagAnswer(String ragAnswer) {
        this.ragAnswer = ragAnswer;
    }

    public List<SelectItem> getRolesSysteme() {
        if (this.listeRolesSysteme == null) {
            this.listeRolesSysteme = new ArrayList<>();
            String role = "You are a helpful assistant. You help the user to find the information they need.";
            this.listeRolesSysteme.add(new SelectItem(role, "Assistant"));
            role = "You are an interpreter. You translate from English to French and from French to English.";
            this.listeRolesSysteme.add(new SelectItem(role, "Traducteur Anglais-Français"));
            role = "Your are a travel guide. If the user type the name of a country or of a town, you tell them what are the main places to visit.";
            this.listeRolesSysteme.add(new SelectItem(role, "Guide touristique"));
        }
        return this.listeRolesSysteme;
    }
}