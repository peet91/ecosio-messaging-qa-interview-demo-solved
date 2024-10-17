## Outline

All task-relevant material can be found inside this repository. The repository includes
- a very basic Spring Boot application exposing just two endpoints, one to test and the corresponding openAPI specification (directory: sample-app)
- the actual QA task (directory: task)

## Spring Boot Application:

**Requirements:**
- Java 21
- Maven

This is a very basic Spring Boot application that you will be testing. You can either run the app from the IDE of you choice, the main class is "QATaskApplication.java" or you build a jar file by doing mvn package and then run the application via CLI. 
After the app has started successfully you can access the OpenAPI definition using the URL "http://localhost:18080/swagger-ui/index.html".
Here's a short outline what the contact endpoint is doing:

The endpoint is used to store and retrieve contacts. The Contact object consists of
- an ID (int)
- the firstname (String) and
- the lastname (String).

The following three contacts are added on automatically on startup of the sample application:

- 0, "Firstname", "Lastname"
- 1, "Testa", "Testb"
- 2, "name", "name"

**GET**:
- `/contact/allContacts`: retrieve all contacts, stored in the app
- `/contact/firstname/{firstname}`: retrieve all contacts where the firstname includes the string provided as path parameter (contains case-insensitive)
- `/contact/lastname/{lastname}`: retrieve all contacts where the lastname includes the string provided as path parameter (contains case-insensitive)

**POST**:
- `/contact/createOrUpdateContact/{id}`: updates an existing contact where the id of that contact is matching the one provided as path parameter, if no matching contact is found creates a new one, requires JSON of a serialized contact in the request body

**DELETE**:
- `/contact/{id}`: deletes the contact matching the path parameter id, does nothing if none exists


## Actual QA task:

**Requirements:**
- Java 21
- Maven

Inside the "task" project you'll find the "ContactTest" class, you might have a look at the
other helper, util, etc. classes as well. All testcases are making use of JUnit5 and AssertJ. With
the sample app running, those test cases are expected to pass. There are some TODOs inside
ContactTest you should have a look at and implement/fix what's missing. Please commit your changes 
to the VCS of your choice and provide us access or submit it per mail.

Here's a short list summarizing the bullet points of the task:
- familiarize yourself with the code, implement what's missing (TODOs), so we can have a chat about it
- what would be your testing strategy toward the Contact endpoint overall, from the beginning of the feature request to the definition of done (DoD)
- can you spot mistakes in the already existing testcases? what would you be doing differently and why?
