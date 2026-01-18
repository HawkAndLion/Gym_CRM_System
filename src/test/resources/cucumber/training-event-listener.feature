Feature: Training Event Listener

  Scenario: Successfully publish training created event
    Given a Training with id 1, trainerId 2, and duration 1.5 hours
    And the Trainer with id 2 exists
    When the TrainingCreatedEvent is published
    Then the workload producer should send a training event with trainingId 1
    And the action type should be ADD

  Scenario: Fail to publish training created event when trainer not found
    Given a Training with id 1, trainerId 2, and duration 1.5 hours
    And the Trainer with id 2 does not exist
    When the TrainingCreatedEvent is published
    Then a ServiceException should be thrown

  Scenario: Fail to publish training created event when duration is non-positive
    Given a Training with id 1, trainerId 2, and duration 0.0 hours
    And the Trainer with id 2 exists
    When the TrainingCreatedEvent is published
    Then an IllegalArgumentException should be thrown

  Scenario: Successfully publish training deleted event
    Given a Training with id 1, trainerId 2, and duration 1.5 hours
    And the Trainer with id 2 exists
    When the TrainingDeletedEvent is published
    Then the workload producer should send a training event with trainingId 1
    And the action type should be DELETE

  Scenario: Fail to publish training deleted event when trainer not found
    Given a Training with id 1, trainerId 2, and duration 1.5 hours
    And the Trainer with id 2 does not exist
    When the TrainingDeletedEvent is published
    Then a ServiceException should be thrown
