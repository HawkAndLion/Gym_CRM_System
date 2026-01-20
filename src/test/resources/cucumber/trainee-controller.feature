Feature: Trainee management controller

  Scenario: Successfully register a trainee
    Given a valid trainee registration request
    When the trainee registration is submitted
    Then the trainee response status should be 200

  Scenario: Trainee registration fails due to error
    Given a valid trainee registration request
    And the profile service throws an error during trainee registration
    When the trainee registration is submitted
    Then the trainee registration should fail with status 400


  Scenario: Successfully get trainee profile
    Given a trainee with username "Alice.Brown" exists
    When the trainee profile is requested for username "Alice.Brown"
    Then the successful response status should be 200
    And the trainee profile should be returned

  Scenario: Get trainee profile fails when trainee does not exist
    Given no trainee exists with username "Unknown"
    When the trainee profile is requested for username "Unknown"
    Then the response status when fail should be 404
    And the error message should contain "Trainee not found"


  Scenario: Successfully update trainee profile
    Given the authenticated user is "Alice.Brown"
    And a valid trainee profile update request
    When the trainee profile update is requested
    Then the successful response status should be 200


  Scenario: Successfully delete trainee profile
    Given a trainee profile exists for username "Alice.Brown"
    When the trainee profile is deleted for username "Alice.Brown"
    Then the successful response status should be 200

  Scenario: Delete trainee profile fails when trainee does not exist
    Given the trainee profile does not exist for username "Ghost.User"
    When the trainee profile is deleted for username "Ghost.User"
    Then the response status when fail should be 404


  Scenario: Activate trainee account
    Given a trainee status request for username "Alice.Brown" with active status true
    When the trainee status is updated
    Then the successful response status should be 200

  Scenario: Deactivate trainee account
    Given a trainee status request for username "Alice.Brown" with active status false
    When the trainee status is updated
    Then the successful response status should be 200

  Scenario: Successfully update trainee trainers
    Given the authenticated user is "Alice.Brown"
    And a trainee trainers update request with valid trainer usernames
    When the trainee trainers are updated
    Then the successful response status should be 200
    And the updated trainer list should be returned

  Scenario: Successfully get trainee trainings with filters
    Given training data exists for trainee "Alice.Brown"
    And training filters are provided
    When trainee trainings are requested
    Then the successful response status should be 200
    And the training list should be returned

  Scenario: Successfully get trainee trainings without optional filters
    Given training data exists for trainee "Alice.Brown" but no trainings match filters
    And no optional training filters are provided
    When trainee trainings are requested
    Then the successful response status should be 200
    And the training list should be empty

