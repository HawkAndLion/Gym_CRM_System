Feature: Authentication and profile management

  The authentication controller allows users to log in
  and change their passwords with proper validation,
  security checks, and error handling.

  Scenario: Successful login with valid credentials
    Given user "john" is not blocked
    And authentication succeeds for username "john" and password "secret"
    And a JWT token can be generated
    When the user logs in with username "john" and password "secret"
    Then the response status should be 200
    And the response should contain a JWT token
    And the response type should be "Bearer"
    And the response username should be "john"

  Scenario: Login fails with invalid credentials
    Given user "john" is not blocked
    And authentication fails for username "john" and password "wrong"
    When the user logs in with username "john" and password "wrong"
    Then the response status should be 400
    And the response token should be "Invalid credentials"

  Scenario: Login is blocked due to too many failed attempts
    Given user "john" is blocked
    And the remaining block time is 5 minutes
    When the user logs in with username "john" and password "secret"
    Then an AccountLockedException should be thrown
    And the error message should be "Your account is blocked for 5 minute(s). Please wait."


  Scenario: Successfully change password
    Given the current password is "old" and the new password is "new"
    And the profile service allows password change
    When the user requests to change password
    Then the response status should be 200
    And the response message should be "Password was changed successfully."

  Scenario: Password change fails due to service error
    Given the current password is "old" and the new password is "new"
    And the profile service throws a service exception with message "Password error"
    When the user requests to change password
    Then the response status should be 400
    And the response message should be "Password error"

  Scenario: Password change fails when old password is missing
    Given the new password is "new"
    When the user requests to change password
    Then the response status should be 400

  Scenario: Password change fails when new password is missing
    Given the current password is "old"
    When the user requests to change password
    Then the response status should be 400

  Scenario: Password change fails when both passwords are missing
    Given no password values are provided
    When the user requests to change password
    Then the response status should be 400
