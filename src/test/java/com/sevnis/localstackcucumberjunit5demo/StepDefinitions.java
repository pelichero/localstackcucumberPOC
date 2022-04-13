package com.sevnis.localstackcucumberjunit5demo;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import cloud.localstack.DockerTestUtils;
import cloud.localstack.TestUtils;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.model.AttributeDefinition;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.CreateTableRequest;
import com.amazonaws.services.dynamodbv2.model.DescribeTableResult;
import com.amazonaws.services.dynamodbv2.model.KeySchemaElement;
import com.amazonaws.services.dynamodbv2.model.KeyType;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;
import com.amazonaws.services.dynamodbv2.model.ScalarAttributeType;
import com.amazonaws.services.lambda.AWSLambda;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.lambda.model.CreateFunctionRequest;
import com.amazonaws.services.lambda.model.FunctionCode;
import com.amazonaws.services.lambda.model.InvokeRequest;
import com.amazonaws.services.lambda.model.InvokeResult;
import com.amazonaws.services.lambda.model.Runtime;
import com.amazonaws.services.sqs.model.CreateQueueResult;
import com.amazonaws.services.sqs.model.SendMessageBatchRequest;
import com.amazonaws.services.sqs.model.SendMessageBatchRequestEntry;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sevnis.localstackcucumberjunit5demo.dao.InfinityLambdaRequest;
import com.sevnis.localstackcucumberjunit5demo.handler.InfinityLambdaHandler;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(classes = SpringConfig.class)
public class StepDefinitions {

  @Given("There are (\\d+) customers in dynamodb")
  public void insertCustomerToDynamoDb(Integer customerCount) {

    AmazonDynamoDB clientDynamoDb = DockerTestUtils.getClientDynamoDb();
    CreateTableRequest createTableRequest = new CreateTableRequest()
        .withTableName("customers")
        .withKeySchema(new KeySchemaElement("identifier", KeyType.HASH))
        .withAttributeDefinitions(new AttributeDefinition("identifier", ScalarAttributeType.S))
        .withProvisionedThroughput(new ProvisionedThroughput(10L, 10L));
    clientDynamoDb.createTable(createTableRequest);

    for (int i = 0; i < customerCount; i++) {
      Map<String, AttributeValue> item = new HashMap<>();
      item.put("identifier", new AttributeValue("" + i));
      clientDynamoDb.putItem("customers", item);
    }
  }

  @When("Thanos snaps the infinity lambda")
  public void invokeLambda() {

    // create lambda
    String functionName = "InfinityLambda";
    AWSLambda lambaClient = DockerTestUtils.getClientLambda();


    try {
      CreateFunctionRequest request = new CreateFunctionRequest();
      request.setFunctionName(functionName);
      request.setRuntime(Runtime.Java8);

      FunctionCode code = new FunctionCode();
      ///NOTE: you need the shadowJar version of jar file for localstack
      code.setZipFile(ByteBuffer.wrap(Files
                      .readAllBytes(Paths.get("target/gradleex-mavenplugin-1.0.0-SNAPSHOT-shaded.jar"))));
      request.setCode(code);

      request.setHandler(InfinityLambdaHandler.class.getName());
      lambaClient.createFunction(request);
    } catch (IOException e) {
      e.printStackTrace();
    }

    // invoke lambda

    InvokeResult result = null;
    try {
      result = lambaClient.invoke(new InvokeRequest()
          .withFunctionName(functionName)
          .withPayload(new ObjectMapper().writeValueAsString(new InfinityLambdaRequest("OK"))));
    } catch (JsonProcessingException e) {
      e.printStackTrace();
    }

    assertThat(new String(result.getPayload().array()), is("{\"value\":\"OK\"}"));
  }

  @Then("There are only (\\d+) customers remaining in dynamodb")
  public void checkDynamoDb(Integer customerCount) {

    AmazonDynamoDB clientDynamoDb = DockerTestUtils.getClientDynamoDb();
    DescribeTableResult result = clientDynamoDb.describeTable("customers");
    assertThat(result.getTable().getItemCount(), is((long) customerCount));
  }

  @Then("There sqs online and save qtd costumers remaining")
  public void makeQueue(){
      AmazonSQS amazonSQS = DockerTestUtils.getClientSQS();
      CreateQueueResult url = amazonSQS.createQueue("test-queue");

      AmazonDynamoDB clientDynamoDb = DockerTestUtils.getClientDynamoDb();
      DescribeTableResult result = clientDynamoDb.describeTable("customers");

      SendMessageRequest send = new SendMessageRequest().withQueueUrl(url.getQueueUrl())
            .withMessageBody("costumers : " + result.getTable().getItemCount());

      amazonSQS.sendMessage(send);

      assertThat(amazonSQS.receiveMessage(url.getQueueUrl()), notNullValue());
      assertThat(amazonSQS.receiveMessage(url.getQueueUrl()).getMessages(), notNullValue());

    }

}
