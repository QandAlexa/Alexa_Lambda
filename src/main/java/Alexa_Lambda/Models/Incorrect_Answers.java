package Alexa_Lambda.Models;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBDocument;

@DynamoDBDocument
public class Incorrect_Answers {

    private String incorrect_answers;

    public Incorrect_Answers () {
    }

    public Incorrect_Answers (String incorrect_answers) {
        this.incorrect_answers = incorrect_answers;
    }

    public String getIncorrect_answer () {
        return incorrect_answers;
    }

    public void setIncorrect_answer (String incorrect_answers) {
        this.incorrect_answers = incorrect_answers;
    }
}
