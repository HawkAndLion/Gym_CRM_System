Feature: Training Workload Event Producer

  Scenario: Successfully send training event with valid JWT
    Given a TrainingEventDto is created
    And JwtTokenProvider generates a service token
    When the training event is sent
    Then the JMS template should send the message to the correct destination
    And the message should have an Authorization header with the JWT token

  Scenario: Fail to generate JWT token
    Given a TrainingEventDto is created
    And JwtTokenProvider fails to generate a token
    When the training event is sent
    Then an exception should be thrown

  Scenario: Fail to send message via JMS
    Given a TrainingEventDto is created
    And JwtTokenProvider generates a service token
    And the JMS template fails to send the message
    When the training event is sent
    Then an exception should be thrown
