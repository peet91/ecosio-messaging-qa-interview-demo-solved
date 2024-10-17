package com.ecosio.qa.sample.controller;

import com.ecosio.qa.sample.model.Contact;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.filter.CommonsRequestLoggingFilter;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@RestController
@RequestMapping("/contact")
public class ContactController {

    private Map<Integer, Contact> contacts = new ConcurrentHashMap<>();

    public ContactController() {
        contacts.put(0, new Contact(0, "Firstname", "Lastname"));
        contacts.put(1, new Contact(1, "Testa", "Testb"));
        contacts.put(2, new Contact(2, "name", "name"));
    }

    @Operation(
            summary = "test method",
            description = "will just return string \"hello\" for testing",
            tags = {"testing"})
    @GetMapping("hello")
    public ResponseEntity<String> hello() {
        log.info("hello");
        return new ResponseEntity<>(
                "hello",
                new HttpHeaders(),
                HttpStatus.OK
        );
    }


    @Operation(
            summary = "returns all available contacts",
            description = "returns all available contacts")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    content = {
                            @Content(
                                    schema = @Schema(implementation = Contact.class),
                                    mediaType = "application/json")
                    })
    })
    @GetMapping("allContacts")
    public ResponseEntity<List<Contact>> getAllContacts() {
        return new ResponseEntity<>(
                contacts.values().stream().toList(),
                new HttpHeaders(),
                HttpStatus.OK
        );
    }


    @Operation(
            summary = "returns all contacts based on firstname filtering",
            description = "get all contacts, as list, where firstname contains (case insensitive) "
                    + "the query parameter, if no matching contacts are found, "
                    + "an empty list is returned")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    content = {
                            @Content(
                                    schema = @Schema(implementation = Contact.class),
                                    mediaType = "application/json")
                    })
    })
    @GetMapping("firstname/{firstname}")
    public ResponseEntity<List<Contact>> getContactByFirstname(
            @PathVariable String firstname
    ) {
        return formatResponse(findByFirstname(firstname));
    }


    @Operation(
            summary = "returns all contacts based on lastname filtering",
            description = "get all contacts, as list, where lastname contains (case insensitive) "
                    + "the query parameter, if no matching contacts are found, "
                    + "an empty list is returned")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    content = {
                            @Content(
                                    schema = @Schema(implementation = Contact.class),
                                    mediaType = "application/json")
                    })
    })
    @GetMapping("lastname/{lastname}")
    public ResponseEntity<List<Contact>> getContactByLastname(
            @PathVariable String lastname
    ) {
        return formatResponse(findByLastname(lastname));
    }

    private ResponseEntity<List<Contact>> formatResponse(List<Contact> contacts) {
        if (!contacts.isEmpty()) {
            log.info("found matching contacts {}", contacts);
        } else {
            log.error("no matching contact has been found");
            contacts = Collections.emptyList();
        }
        return new ResponseEntity<>(
                contacts,
                new HttpHeaders(),
                HttpStatus.OK
        );
    }

    @Operation(
            summary = "creates or updates an already existing contact",
            description = "creates or updates an already existing contact based on the provided id, if "
                    + "a contact with a matching id is found it will be updated to match the one "
                    + "provided as POST body"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    content = {
                            @Content(
                                    schema = @Schema(implementation = Contact.class),
                                    mediaType = "application/json")
                    })
    })
    @PostMapping("createOrUpdateContact/{id}")
    public ResponseEntity createOrUpdateContact(
            @RequestBody Contact contact,
            @PathVariable int id
    ) {

        if (id != contact.getId()) {
            log.error("IDs not matching");
            return ResponseEntity.badRequest().body("IDs not matching");
        }

        log.info("looking for matching contacts before creating new one");

        if (getContactByContactId(id) != null) {
            log.info(
                    "matching contact found, updating this one"
            );

            Integer mapId = getContactByContactId(id).getKey();
            contacts.put(mapId, contact);
            log.info(
                    "updating done"
            );
        } else {
            contacts.put(contacts.size(), contact);
            log.info("contact saved");
        }
        return new ResponseEntity<>(
                contact,
                new HttpHeaders(),
                HttpStatus.OK
        );
    }

    @Operation(
            summary = "deletes the contact matching the ID",
            description = "if a contact with an ID matching the one provided exists it will be deleted, "
                    + "returns the contact which has been deleted, "
                    + "if no matching contact is found an empty one is returned"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    content = {
                            @Content(
                                    schema = @Schema(implementation = Contact.class),
                                    mediaType = "application/json")
                    })
    })
    @DeleteMapping("{id}")
    public ResponseEntity deleteContact(
            @PathVariable int id
    ) {
        log.info("removing id: {}", id);
        Entry<Integer, Contact> contactToDelete = getContactByContactId(id);

        if (contactToDelete != null) {
            contacts.remove(contactToDelete.getKey());
            return new ResponseEntity<>(
                    contactToDelete.getValue(),
                    new HttpHeaders(),
                    HttpStatus.OK
            );
        }

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body("Contact with requested ID doesn't exist");

    }

    private List<Contact> findByFirstname(final String firstname) {
        return contacts
                .values()
                .stream()
                .filter(p -> StringUtils.containsIgnoreCase(p.getFirstname(), firstname))
//        .findFirst()
//        .stream()
                .toList();
    }

    private List<Contact> findByLastname(final String lastname) {
        return contacts
                .values()
                .stream()
//            easy copy paste error, which causes the corresponding test to fail
                .filter(p -> StringUtils.containsIgnoreCase(p.getFirstname(), lastname))
                .toList();
    }

    private Entry<Integer, Contact> getContactByContactId(int id) {
        return contacts.entrySet()
                .stream()
                .filter(c -> c.getValue().getId() == id)
                .findFirst()
                .orElse(null);
    }

    @Bean("httpLogging")
    public CommonsRequestLoggingFilter requestLoggingFilter() {
        CommonsRequestLoggingFilter loggingFilter = new CommonsRequestLoggingFilter();
        loggingFilter.setIncludeClientInfo(true);
        loggingFilter.setIncludeQueryString(true);
        loggingFilter.setIncludePayload(true);
        loggingFilter.setIncludeHeaders(false);
        return loggingFilter;
    }

}
