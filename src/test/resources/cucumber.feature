Feature:
  Scenario:
    Given There are 10 customers in dynamodb
    When Thanos snaps the infinity lambda
    Then There are only 5 customers remaining in dynamodb
