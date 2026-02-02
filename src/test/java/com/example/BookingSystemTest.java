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
import java.util.List;
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
    @MethodSource("provideNullScenariosForRoomIdStartAndEndTime")
    void throwsExceptionWhenRoomIdStartTimeOrEndTimeIsNull(String roomId, LocalDateTime startTime, LocalDateTime endTime) {

        var exception = assertThrows(IllegalArgumentException.class,
                () -> bookingSystem.bookRoom(roomId, startTime, endTime));
        assertThat(exception).hasMessage("Bokning kräver giltiga start- och sluttider samt rum-id");
    }
    private static Stream<Arguments> provideNullScenariosForRoomIdStartAndEndTime() {
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

    @Test
    void bookRoomIfRoomIsAvailable() {

        Room room = new Room("100", "Room1");
        LocalDateTime currentTime = LocalDateTime.now();
        LocalDateTime startTime = currentTime.plusDays(2);
        LocalDateTime endTime = currentTime.plusDays(3);

        Mockito.when(timeProvider.getCurrentTime()).thenReturn(currentTime);
        Mockito.when(roomRepository.findById(room.getId())).thenReturn(Optional.of(room));

        boolean result = bookingSystem.bookRoom(room.getId(), startTime, endTime);

        assertThat(result).isTrue();

        Mockito.verify(roomRepository).save(room);
    }

    @Test
    void notBookRoomIfRoomIsNotAvailable() {

        Room room = new Room("100", "Room1");
        LocalDateTime currentTime = LocalDateTime.now();
        LocalDateTime startTime = currentTime.plusDays(1);
        LocalDateTime endTime = currentTime.plusDays(2);
        Booking booking = new Booking("1", "100", startTime, endTime);

        Mockito.when(timeProvider.getCurrentTime()).thenReturn(currentTime);
        Mockito.when(roomRepository.findById(room.getId())).thenReturn(Optional.of(room));
        room.addBooking(booking);

        boolean result = bookingSystem.bookRoom(room.getId(), startTime, endTime);

        assertThat(result).isFalse();

        Mockito.verify(roomRepository, Mockito.never()).save(room);

    }

    //Test missing
    //To check if the notification is sent when a room is booked

    @ParameterizedTest
    @MethodSource("provideNullScenarios")
    void getAvailableRoomsThrowsExceptionWhenStartOrEndTimeIsNull(LocalDateTime startTime, LocalDateTime endTime) {

        var exception = assertThrows(IllegalArgumentException.class,
                () -> bookingSystem.getAvailableRooms(startTime, endTime));
        assertThat(exception).hasMessage("Måste ange både start- och sluttid");
    }
    private static Stream<Arguments> provideNullScenarios() {
        LocalDateTime timeNow = LocalDateTime.now();
        return Stream.of(
                Arguments.of(timeNow, null),
                Arguments.of(null, timeNow),
                Arguments.of(null, null)
        );
    }

    @Test
    void getAvailableRoomsThrowsExceptionWhenEndTimeIsBeforeStartTime() {

            LocalDateTime currentTime = LocalDateTime.now();
            LocalDateTime startTime = currentTime.plusDays(4);
            LocalDateTime endTime = currentTime.plusDays(3);

            var exception = assertThrows(IllegalArgumentException.class,
                    () -> bookingSystem.getAvailableRooms(startTime, endTime));
            assertThat(exception).hasMessage("Sluttid måste vara efter starttid");
    }

    @Test
    void getAvailableRoomsReturnsAListOfAvailableRooms() {

        LocalDateTime currentTime = LocalDateTime.now();
        LocalDateTime startTime = currentTime.plusDays(4);
        LocalDateTime endTime = currentTime.plusDays(5);
        Room room1 = new Room("101", "Room1");
        Room room2 = new Room("102", "Room2");

        Mockito.when(roomRepository.findAll()).thenReturn(List.of(room1, room2));
        List<Room> result = bookingSystem.getAvailableRooms(startTime, endTime);

        assertThat(result).hasSize(2);

    }

    @Test
    void cancelBookingThrowsExceptionWhenBookingIdIsNull() {

        var exception = assertThrows(IllegalArgumentException.class,
                () -> bookingSystem.cancelBooking(null));

        assertThat(exception).hasMessage("Boknings-id kan inte vara null");
    }

    @Test
    void cancelBooking(){

        LocalDateTime currentTime = LocalDateTime.now();
        LocalDateTime startTime = currentTime.plusDays(3);
        LocalDateTime endTime = currentTime.plusDays(5);
        Booking booking = new Booking("1", "100", startTime, endTime);
        Room room = new Room("100", "Room1");

        room.addBooking(booking);
        Mockito.when(timeProvider.getCurrentTime()).thenReturn(currentTime);
        Mockito.when(roomRepository.findAll()).thenReturn(List.of(room));
        boolean result = bookingSystem.cancelBooking("1");

        assertThat(result).isTrue();
    }

    @Test
    void cancelBookingThrowsExceptionWhenStartTimeIsBeforeCurrentTime(){

        LocalDateTime currentTime = LocalDateTime.now();
        LocalDateTime startTime = currentTime.minusDays(3);
        LocalDateTime endTime = currentTime.plusDays(5);
        Booking booking = new Booking("1", "100", startTime, endTime);
        Room room = new Room("100", "Room1");

        room.addBooking(booking);
        Mockito.when(timeProvider.getCurrentTime()).thenReturn(currentTime);
        Mockito.when(roomRepository.findAll()).thenReturn(List.of(room));

        var exception = assertThrows(IllegalStateException.class,
                () -> bookingSystem.cancelBooking("1"));

        assertThat(exception).hasMessage("Kan inte avboka påbörjad eller avslutad bokning");
    }

}
