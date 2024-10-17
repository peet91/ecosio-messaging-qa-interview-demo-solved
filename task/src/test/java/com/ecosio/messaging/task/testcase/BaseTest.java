package com.ecosio.messaging.task.testcase;

import com.ecosio.messaging.task.model.Contact;
import com.ecosio.messaging.task.util.HttpClientHelper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.common.mapper.TypeRef;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.CloseableHttpClient;
import org.assertj.core.api.SoftAssertions;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.List;

import static io.restassured.RestAssured.given;

@Slf4j
public class BaseTest {

    protected static final String BASE_URL = "http://localhost:18080/";
    protected static final String CONTACT = BASE_URL + "contact/";
    protected static final String ALL_CONTACTS = CONTACT + "allContacts";
    protected static final String CONTACT_FIRST_NAME = CONTACT + "firstname/{firstname}";
    protected static final String CONTACT_LAST_NAME = CONTACT + "lastname/{lastname}";
    protected static final String CONTACT_ID = CONTACT + "{id}";
    protected static final String CREATE_OR_UPDATE_CONTACT_ID = CONTACT + "createOrUpdateContact/{id}";

    protected static final TypeRef<List<Contact>> LIST_OF_CONTACTS = new TypeRef<>() {
    };

    SoftAssertions softAssert = new SoftAssertions();

    /**
     * gets all contacts where parameter <code>firstname</code> is a substring of the contacts firstname
     *
     * @param firstname
     * @return list of all matching contacts
     * @throws IOException
     */

    protected Response getContactByFirstname(String firstname) {
        return given()
                .pathParam("firstname", firstname)
                .get(CONTACT_FIRST_NAME)
                .then().extract().response();
    }

    /**
     * gets all contacts where parameter <code>lastname</code> is a substring of the contacts lastname
     *
     * @param lastname
     * @return list of all matching contacts
     * @throws IOException
     */

    protected Response getContactByLastname(String lastname) {
        return given()
                .pathParam("lastname", lastname)
                .get(CONTACT_LAST_NAME)
                .then().extract().response();
    }


    /**
     * gets a list of all contacts stored of the app
     *
     * @return list of all contacts
     * @throws IOException
     */
    protected List<Contact> getAllContacts() throws IOException {
        //I didn't want to touch this function to refactor it to the RestAssured equivalent,
        //that's why it doesn't follow the signature pattern with Responses as return value
        HttpGet httpGet = new HttpGet(ALL_CONTACTS);
        return connectionHelper(httpGet);
    }

    /**
     * creates a new contact given the contact DTO as parameter
     *
     * @param contact
     * @return
     */
    protected Response createContact(Contact contact) {
        return given()
                .contentType(ContentType.JSON)
                .body(contact)
                .pathParam("id", contact.getId())
                .post(CREATE_OR_UPDATE_CONTACT_ID)
                .then()
                .extract().response();
    }

    /**
     * updates an existing contact
     *
     * @param currentContact contact to be updated
     * @param updatedContact contact to update the existing one to
     * @return
     */
    protected Response updateContact(Contact currentContact, Contact updatedContact) {
        //I handle the mismatching id here for the two entities
        assert currentContact.getId() == updatedContact.getId();
        return updateContact(currentContact, updatedContact, currentContact.getId());
    }

    /**
     * updates an
     *
     * @param currentContact contact to be updated
     * @param updatedContact contact to update the existing one to
     * @param id             the id which is passed as a path variable and should be equal to both entities' ids
     * @return
     */
    protected Response updateContact(Contact currentContact, Contact updatedContact, int id) {
        //the update should be a PUT method to meet the HTTP standard
        //currently the implementation violates it, because it uses the same endpoint
        //for creating and updating entities

        //I had to disable this assertion here, because otherwise I couldn't test that branch in the implementation
//        assert currentContact.getId() == updatedContact.getId() && currentContact.getId() == id;
        return given()
                .contentType(ContentType.JSON)
                .pathParam("id", id)
                .body(updatedContact)
                .post(CREATE_OR_UPDATE_CONTACT_ID)
                .then().extract().response();
    }

    /**
     * deletes a contact with the given id passed as a path variable
     *
     * @param id an existing contact id
     * @return
     */
    protected Response deleteContact(int id) {
        return given()
                .pathParam("id", id)
                .delete(CONTACT_ID)
                .then().extract().response();
    }

    protected int getFirstAvailableId() {
        try {
            List<Contact> contacts = getAllContacts();
            int firstAvailableId = contacts.stream().map(Contact::getId)
                    .sorted(Comparator.reverseOrder()).findFirst().orElse(0) + 1;
            log.info("First available id: {}", firstAvailableId);
            return firstAvailableId;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * connection helper to abstract the "connection layer" from the "application layer"
     *
     * @param httpRequestBase
     * @return list of contacts based on the request
     * @throws IOException
     */
    private List<Contact> connectionHelper(HttpRequestBase httpRequestBase) throws IOException {

        try (CloseableHttpClient httpClient = HttpClientHelper.getHttpClientAcceptsAllSslCerts()) {
            try {

                ObjectMapper mapper = new ObjectMapper();
                String response = IOUtils.toString(
                        httpClient.execute(httpRequestBase)
                                .getEntity()
                                .getContent(),
                        StandardCharsets.UTF_8
                );
                return mapper.readValue(
                        response,
                        new TypeReference<List<Contact>>() {
                        }
                );

            } finally {
                httpRequestBase.releaseConnection();
            }
        }
    }
}
