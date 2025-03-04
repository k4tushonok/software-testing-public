package org.itmo.testing.lab2.service;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

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

}
