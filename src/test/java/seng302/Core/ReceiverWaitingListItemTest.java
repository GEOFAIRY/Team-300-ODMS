package seng302.Core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import seng302.Generic.ReceiverWaitingListItem;
import seng302.User.Attribute.Organ;
import seng302.User.User;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class ReceiverWaitingListItemTest {
    private User testUser;
    private ReceiverWaitingListItem item;
    private Organ heart;

    @BeforeEach
    void setUp() {
        testUser = new User("Joe", LocalDate.parse("01/01/1999", User.dateFormat));
        heart = Organ.HEART;
        item = new ReceiverWaitingListItem(heart, Long.parseLong("-1"));
        testUser.getWaitingListItems().add(item);
    }

    @Test
    void testNullDeregisteredDateOnRegister() {
        String date = "notNull";
        item.deregisterOrgan(3);
        item.registerOrgan();
        for (ReceiverWaitingListItem listItem : testUser.getWaitingListItems()) {
            date = listItem.getOrganDeregisteredDate();
        }
        assertTrue(date == null);
    }

    @Test
    void testIsStillWaitingOnRegister() {
        boolean stillWaitingOn = false;
        item.deregisterOrgan(3);
        item.registerOrgan();
        for (ReceiverWaitingListItem listItem : testUser.getWaitingListItems()) {
            stillWaitingOn = listItem.getStillWaitingOn();
        }
        assertTrue(stillWaitingOn);
    }

    @Test
    void testIsNotStillWaitingOnDeregister() {
        boolean stillWaitingOn = true;
        item.deregisterOrgan(3);
        for (ReceiverWaitingListItem listItem : testUser.getWaitingListItems()) {
            stillWaitingOn = listItem.getStillWaitingOn();
        }
        assertFalse(stillWaitingOn);
    }

    @Test
    void isDonatingOrgan() {
        testUser.setOrgan(Organ.HEART);
        ReceiverWaitingListItem listItem = testUser.getWaitingListItems().get(0);
        System.out.println(listItem.getOrganType());
        assertTrue(listItem.isDonatingOrgan(testUser));
    }
}