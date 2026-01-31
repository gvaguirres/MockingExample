package com.example;


import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
public class BookingSystemTest {

    @Mock
    TimeProvider timeProvider;

    @Mock
    RoomRepository roomRepository;

    @Mock
    NotificationService notificationService;

    @InjectMocks
    BookingSystem bookingSystem;

    @ParameterizedTest
    @MethodSource("provideNullScenarios")
    void throwsExceptionWhenRoomIdStartTimeOrEndTimeIsNull(String roomId, LocalDateTime startTime, LocalDateTime endTime) {

        var exception = assertThrows(IllegalArgumentException.class,
                () -> bookingSystem.bookRoom(roomId, startTime, endTime));
        assertThat(exception).hasMessage("Bokning kräver giltiga start- och sluttider samt rum-id");
    }
    static Stream<Arguments> provideNullScenarios() {
        LocalDateTime timeNow = LocalDateTime.now();
        return Stream.of(
                Arguments.of(null, timeNow, timeNow),
                Arguments.of("Room1", null, timeNow),
                Arguments.of("Room1", timeNow, null),
                Arguments.of(null, null, null)
                );
    }

    @Test
    void throwsExceptionWhenRoomDoesNotExist() {

        Room room = new Room("100", "Room1");
        LocalDateTime currentTime = LocalDateTime.now();
        LocalDateTime startTime = currentTime.plusDays(2);
        LocalDateTime endTime = currentTime.plusDays(3);

        Mockito.when(timeProvider.getCurrentTime()).thenReturn(currentTime);
        Mockito.when(roomRepository.findById(room.getId())).thenReturn(Optional.empty());

        var exception = assertThrows(IllegalArgumentException.class,
                () -> bookingSystem.bookRoom(room.getId(), startTime, endTime));
        assertThat(exception).hasMessage("Rummet existerar inte");

    }

    @Test
    void throwsExceptionWhenStartTimeIsBeforeCurrentDate(){

        LocalDateTime currentTime = LocalDateTime.now();
        LocalDateTime startTime = currentTime.minusDays(1);
        LocalDateTime endTime = currentTime.plusDays(2);
        String roomId = "100";

        Mockito.when(timeProvider.getCurrentTime()).thenReturn(currentTime);

        var exception = assertThrows(IllegalArgumentException.class,
                () -> bookingSystem.bookRoom(roomId, startTime, endTime));
        assertThat(exception).hasMessage("Kan inte boka tid i dåtid");
    }

    @Test
    void throwsExceptionWhenEndTimeIsBeforeStartTime(){

        LocalDateTime currentTime = LocalDateTime.now();
        LocalDateTime startTime = currentTime.plusDays(1);
        LocalDateTime endTime = currentTime.minusDays(2);
        String roomId = "100";

        Mockito.when(timeProvider.getCurrentTime()).thenReturn(currentTime);

        var exception = assertThrows(IllegalArgumentException.class,
                () -> bookingSystem.bookRoom(roomId, startTime, endTime));
        assertThat(exception).hasMessage("Sluttid måste vara efter starttid");

    }

}
