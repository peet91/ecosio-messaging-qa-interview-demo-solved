package com.ecosio.messaging.task.testcase;

import com.ecosio.messaging.task.model.Contact;
import io.restassured.response.Response;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class ContactNegativeTest extends BaseTest {


    @Test
    void nonExistentContactByFirstName() {
        List<Contact> contacts = getContactByFirstname("NON_EXISTENT").getBody().as(LIST_OF_CONTACTS);

        assertThat(contacts)
                .as("Querying for non-existent first name should result in empty list!")
                .isEmpty();
    }

    @Test
    void nonExistentContactByLastName() {
        List<Contact> contacts = getContactByLastname("NON_EXISTENT").getBody().as(LIST_OF_CONTACTS);

        assertThat(contacts)
                .as("Querying for non-existent last name should result in empty list!")
                .isEmpty();
    }

    /**
     * This is rather a unit test for the update endpoint action, because I added a safety net there
     * to prevent faulty update requests with mismatching ids on the object level
     * @throws IOException
     */
    @Test
    void oldObjectIdAndUpdatedObjectIdMismatch() throws IOException {
        List<Contact> contacts = getAllContacts();

        Contact contactToBeUpdated = contacts.get(0);

        Contact updatedContact = new Contact(
                contacts.get(1).getId(),
                "abc",
                "def"
        );

        assertThatExceptionOfType(AssertionError.class)
                .as("Update request with mismatching object ids should throw error!")
                .isThrownBy(() -> updateContact(contactToBeUpdated, updatedContact));
    }

    @Test
    void requestIdAndObjectIdMismatchDuringUpdate() throws IOException {
        List<Contact> contacts = getAllContacts();

        Contact contactToBeUpdated = contacts.get(0);

        Contact updatedContact = new Contact(
                contactToBeUpdated.getId(),
                "abc",
                "def"
        );

        Response updateResponse = updateContact(contactToBeUpdated, updatedContact, contacts.get(1).getId());

        softAssert.assertThat(updateResponse.getStatusCode())
                .as("Mismatching ID should result in BAD_REQUEST")
                .isEqualTo(HttpStatus.SC_BAD_REQUEST);
        softAssert.assertThat(getAllContacts())
                .as("Updated contact should not be present in all contacts")
                .doesNotContain(updatedContact);
        softAssert.assertThat(getAllContacts())
                .as("Original contact should be present in all contacts," +
                        " since the update wasn't successful")
                .contains(contactToBeUpdated);
        softAssert.assertAll();
    }

    @Test
    void deleteNonExistentContact() throws IOException {
        List<Contact> contacts = getAllContacts();

        Contact contactToBeDeleted = contacts.getFirst();

        Response updateResponse = deleteContact(getFirstAvailableId());
        softAssert.assertThat(updateResponse.getStatusCode())
                .as("Mismatching ID should result in NOT_FOUND")
                .isEqualTo(HttpStatus.SC_NOT_FOUND);
        softAssert.assertThat(getAllContacts())
                .as("Original contact should be intact in all contacts," +
                        " since the deletion wasn't successful")
                .contains(contactToBeDeleted);
        softAssert.assertAll();
    }
}
