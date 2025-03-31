package org.itmo.testing.lab2.service;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class UserStatusServiceTest {

    private UserAnalyticsService userAnalyticsService;
    private UserStatusService userStatusService;

    @BeforeAll
    void setUp() {
        userAnalyticsService = mock(UserAnalyticsService.class);
        userStatusService = new UserStatusService(userAnalyticsService);
    }

    @Test
    public void testGetUserStatus_Active() {
        // Настроим поведение mock-объекта
        when(userAnalyticsService.getTotalActivityTime("user123")).thenReturn(90L);

        String status = userStatusService.getUserStatus("user123");

        assertEquals("Active", status);
    }

    @Test
    public void testGetUserStatus() {
        when(userAnalyticsService.getTotalActivityTime("user1")).thenReturn(30L);
        when(userAnalyticsService.getTotalActivityTime("user2")).thenReturn(90L);
        when(userAnalyticsService.getTotalActivityTime("user3")).thenReturn(150L);

        assertEquals("Inactive", userStatusService.getUserStatus("user1"));
        assertEquals("Active", userStatusService.getUserStatus("user2"));
        assertEquals("Highly active", userStatusService.getUserStatus("user3"));

        verify(userAnalyticsService, times(4)).getTotalActivityTime(anyString());
    }

    @Test
    void testGetUserLastSessionDate() {
        LinkedList<UserAnalyticsService.Session> sessions = new LinkedList<>();
        sessions.add(new UserAnalyticsService.Session(
                LocalDateTime.of(2024, 3, 1, 10, 0),
                LocalDateTime.of(2024, 3, 1, 12, 0)
        ));
        sessions.add(new UserAnalyticsService.Session(
                LocalDateTime.of(2024, 3, 5, 14, 0),
                LocalDateTime.of(2024, 3, 5, 16, 0)
        ));

        when(userAnalyticsService.getUserSessions("user1")).thenReturn(sessions);

        Optional<String> lastSessionDate = userStatusService.getUserLastSessionDate("user1");
        assertTrue(lastSessionDate.isPresent());
        assertEquals("2024-03-05", lastSessionDate.get());

        verify(userAnalyticsService, times(1)).getUserSessions("user1");
    }
}
