package com.sevnis.localstackcucumberjunit5demo.handler;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder.EndpointConfiguration;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.sevnis.localstackcucumberjunit5demo.dao.InfinityLambdaRequest;
import com.sevnis.localstackcucumberjunit5demo.dao.InfinityLambdaResponse;
import java.util.HashMap;
import java.util.Map;

public class InfinityLambdaHandler implements RequestHandler<InfinityLambdaRequest, InfinityLambdaResponse> {


  @Override
  public InfinityLambdaResponse handleRequest(InfinityLambdaRequest input, Context context) {

    ///NOTE: localstack does not care about your credentials but it must exists
    BasicAWSCredentials awsCredentials = new BasicAWSCredentials("test", "test");

    AmazonDynamoDB amazonDynamoDB = AmazonDynamoDBClientBuilder
        .standard()
        ///NOTE: localstack default port for dynamodb is 4569
        ///NOTE: localstack default region is us-east-1
        .withEndpointConfiguration(new EndpointConfiguration("http://localhost:4569", "us-east-1"))
        .withCredentials(new AWSStaticCredentialsProvider(awsCredentials))
        .build();

    for (int i = 0; i < 5; i++) {
      Map<String, AttributeValue> item = new HashMap<>();
      item.put("identifier", new AttributeValue("" + i));
      amazonDynamoDB.deleteItem("customers", item);
    }

    return new InfinityLambdaResponse("OK");
  }
}
