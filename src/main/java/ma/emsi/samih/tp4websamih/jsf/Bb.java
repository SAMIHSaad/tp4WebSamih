package ma.emsi.samih.tp4websamih.jsf;

import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import ma.emsi.samih.tp4websamih.rag.RagService;

import java.io.Serializable;

@Named
@ViewScoped
public class Bb implements Serializable {

    @Inject
    private RagService ragService;

    private String ragQuestion;

    private String ragAnswer;

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

    public void askRag() {
        if (ragQuestion != null && !ragQuestion.trim().isEmpty()) {
            this.ragAnswer = ragService.answer(ragQuestion);
        }
    }
}

    