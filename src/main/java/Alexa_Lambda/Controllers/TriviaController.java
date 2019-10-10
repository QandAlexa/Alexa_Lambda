package Alexa_Lambda.Controllers;

import Alexa_Lambda.Models.Incorrect_Answers;
import Alexa_Lambda.Models.Input;
import Alexa_Lambda.Models.Trivia;
import com.amazonaws.Request;
import com.amazonaws.Response;
import com.amazonaws.http.HttpResponse;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.datamodeling.*;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.amazonaws.services.s3.model.JSONInput;
import com.amazonaws.services.sqs.model.*;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.sun.org.apache.xalan.internal.xsltc.compiler.Parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;


public class TriviaController {

    private static final String QUEUE_NAMEA = "Questions" ;
    private DynamoDB dynamoDb;
    private String DYNAMODB_TABLE_NAME = "Trivia";
    private Regions REGION = Regions.US_WEST_2;

    public Trivia save(Trivia[] trivia) {
        for(int i = 0; i < trivia.length; i++) {


            final AmazonDynamoDB ddb = AmazonDynamoDBClientBuilder.defaultClient();
            DynamoDBMapper ddbMapper = new DynamoDBMapper(ddb);

            Incorrect_Answers incorrect_answers = new Incorrect_Answers();

            ddbMapper.save(trivia[i]);
            ddbMapper.save(incorrect_answers);
        }
        return trivia[0];
    }

    public String getTrivia(Input input){

        final AmazonSQS sqs = AmazonSQSClientBuilder.defaultClient();
        String queueUrlA = sqs.getQueueUrl(QUEUE_NAMEA).getQueueUrl();
        System.out.println("INPUTS---" + input.toString());

        int questions = input.getNumberOfQuestions();

        HashMap<String, AttributeValue> eav = new HashMap<>();
        //eav.put(":v1", new AttributeValue().withS(task.getAssignee()));
        eav.put(":v1", new AttributeValue().withS(input.getCategory()));
        //eav.put(":v2", new AttributeValue().withS(input.getDifficulty()));

        DynamoDBScanExpression scanExpression = new DynamoDBScanExpression()
                .withFilterExpression("category = :v1")
                //.withFilterExpression("difficulty = :v2")
                .withExpressionAttributeValues((eav));

        final AmazonDynamoDB ddb = AmazonDynamoDBClientBuilder.defaultClient();
        DynamoDBMapper ddbMapper = new DynamoDBMapper(ddb);
        List<Trivia> trivias = ddbMapper.scan(Trivia.class, scanExpression);
        List<Trivia> returnList = new ArrayList<>();
        int i = 0;

        while (returnList.size() < questions && i < trivias.size()){
            if (trivias.get(i).getDifficulty().equals(input.getDifficulty())) {
                returnList.add(trivias.get(i));
                String s = trivias.get(i).getQuestion();
                s = s + "||" + trivias.get(i).getCorrect_answer();
                SendMessageRequest send_msg_request = new SendMessageRequest()
                        .withQueueUrl(queueUrlA)
                        .withMessageBody(s);
                //.withDelaySeconds(5);
                sqs.sendMessage(send_msg_request);
            }
            i++;
        }

        Gson gson = new Gson();
        String json = gson.toJson(returnList);

        return json;

    }

    public String getQuestionsFromQueue() {
        final AmazonSQS sqs = AmazonSQSClientBuilder.defaultClient();
        String queueUrl = sqs.getQueueUrl(QUEUE_NAMEA).getQueueUrl();
        ReceiveMessageRequest receiveMessageRequest = new ReceiveMessageRequest(queueUrl);
        //receiveMessageRequest.setMaxNumberOfMessages(1);
        receiveMessageRequest.withMaxNumberOfMessages(1);
        List<Message> messages = sqs.receiveMessage(receiveMessageRequest).getMessages();
        return messages.get(0).getBody();
//        List<String> questions = new ArrayList<>();
//        for(Message msg : messages){
//            questions.add(msg.getBody());
//            sqs.deleteMessage(queueUrl, msg.getReceiptHandle());
//            //sqs.deleteMessage(new DeleteMessageRequest().withQueueUrl(queuename).withReceiptHandle(messageReceiptHandle));
//        }
//        return questions;
    }

}
