package com.ecosio.messaging.task.testcase;

import com.ecosio.messaging.task.model.Contact;
import io.restassured.response.Response;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

// TODO: have a look at all the existing tests (some may need fixing or can be implemented in a
//  better way, you are not expected to fix them, but think about why and how you would address
//  these issues and we'll talk about them in the next interview)

// TODO: think of a few different tests (including/excluding the ones here)
//  you would implement and why
@Slf4j
class ContactTest extends BaseTest {

    @Test
    void allContacts() throws IOException {

        // get all available contacts
        List<Contact> contacts = getAllContacts();

        log.info("all contacts: {}", contacts);

        // number of contacts is expected to stay the same
        assertThat(contacts.size())
                .as("number of contacts")
                .isEqualTo(3);
    }

    @Test
    void createContact() throws IOException {
        int id = getFirstAvailableId();
        Contact contact = new Contact(id, "John", "Doe");

        Response createResponse = createContact(contact);

        assertThat(createResponse.getStatusCode())
                .as("Contact creation request failed!\n" + createResponse.asPrettyString())
//I think it should return 201
//                .isEqualTo(HttpStatus.SC_CREATED);
                .isEqualTo(HttpStatus.SC_OK);

        List<Contact> contacts = getAllContacts();

        assertThat(contacts)
                .as("Contacts should contain the newly created contact: " + contact + "!")
                .contains(contact);

        deleteContact(id);
    }

    @Test
    void updateContact() throws IOException {
        // get specific contact
        List<Contact> contactsBefore = getContactByFirstname("Testa").as(LIST_OF_CONTACTS);
        Contact originalContact = contactsBefore.getFirst();

        assertThat(contactsBefore.size())
                .as("number of contacts before update")
                .isOne();

        // update previously retrieved contact to this
        Contact updatedContact = new Contact(
                originalContact.getId(),
                "abc",
                "def"
        );

        Response updateResponse = updateContact(originalContact, updatedContact);

        assertThat(updateResponse.getStatusCode())
                .as("Contact update request failed!\n" + updateResponse.asPrettyString())
                .isEqualTo(HttpStatus.SC_OK);

        List<Contact> contacts = getAllContacts();
        softAssert.assertThat(contacts.contains(updatedContact))
                .as("Contacts should contain the updated contact: " + updatedContact + "!")
                .isTrue();
        softAssert.assertThat(contacts.contains(originalContact))
                .as("Contacts should contain the updated contact: " + updatedContact + "!")
                .isFalse();
        softAssert.assertAll();

        //reverting the values to the original ones
        updateContact(updatedContact, originalContact);
    }

    @Test
    void getContactByFirstname() {
        List<Contact> contacts = getContactByFirstname("name").getBody().as(LIST_OF_CONTACTS);

        softAssert.assertThat(contacts.size())
                .as("Contact list size mismatch!")
                .isEqualTo(2);
        softAssert.assertThat(contacts)
                .as("Contact list should only contain properly filtered entities!")
                .allMatch(contact -> StringUtils.containsIgnoreCase(contact.getFirstname(), "name"));
        softAssert.assertAll();
    }

    //due to the faulty implementation in the backend code, this test will fail
    @Test
    void getContactByLastname() {
        List<Contact> contacts = getContactByLastname("Testb").getBody().as(LIST_OF_CONTACTS);

        softAssert.assertThat(contacts.size())
                .as("Contact list size mismatch!")
                .isEqualTo(1);
        softAssert.assertThat(contacts)
                .as("Contact list should only contain properly filtered entities!")
                .allMatch(contact -> StringUtils.containsIgnoreCase(contact.getLastname(), "Testb"));
        softAssert.assertAll();
    }

    @Test
    void deleteContact() throws IOException {
        int id = getFirstAvailableId();
        Contact contact = new Contact(id, "John", "Doe");

        createContact(contact);

        Response deleteResponse = deleteContact(id);

        assertThat(deleteResponse.getStatusCode())
                .as("Delete contact request error:\n" + deleteResponse.prettyPrint())
                .isEqualTo(HttpStatus.SC_OK);

        List<Contact> contacts = getAllContacts();

        assertThat(contacts)
                .as("Contacts should not contain the deleted contact: " + contact + "!")
                .doesNotContain(contact);
    }
}
