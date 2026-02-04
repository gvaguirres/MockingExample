package com.example;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class BookingSystemTest {

    @Mock
    TimeProvider timeProvider;

    @Mock
    RoomRepository roomRepository;

    @Mock
    NotificationService notificationService;

    @InjectMocks
    BookingSystem bookingSystem;

    @Captor
    ArgumentCaptor<Booking> bookingCaptor;

    @ParameterizedTest
    @MethodSource("provideNullScenariosForRoomIdStartAndEndTime")
    @DisplayName("Should throw IllegalArgumentException if room ID, start time, or end time is null")
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
    @DisplayName("Should throw IllegalArgumentException if the room ID provided does not exist in the repository")
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
    @DisplayName("Should throw IllegalArgumentException when attempting to book a room in the past")
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
    @DisplayName("Should throw IllegalArgumentException if the booking end time is before the start time")
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
    @DisplayName("Should successfully book a room, save it to the repository, and send a confirmation when the room is available")
    void bookRoomIfRoomIsAvailable() throws NotificationException {

        Room room = new Room("100", "Room1");
        LocalDateTime currentTime = LocalDateTime.now();
        LocalDateTime startTime = currentTime.plusDays(2);
        LocalDateTime endTime = currentTime.plusDays(3);

        Mockito.when(timeProvider.getCurrentTime()).thenReturn(currentTime);
        Mockito.when(roomRepository.findById(room.getId())).thenReturn(Optional.of(room));

        var result = bookingSystem.bookRoom(room.getId(), startTime, endTime);

        verify(notificationService).sendBookingConfirmation(bookingCaptor.capture());
        Booking bookingCaptorValue = bookingCaptor.getValue();

        assertThat(room.hasBooking(bookingCaptorValue.getId())).isTrue();
        verify(roomRepository).save(room);
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("Should return false and not save to repository if the room is already occupied during the requested time")
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

        verify(roomRepository, Mockito.never()).save(room);
    }

    @ParameterizedTest
    @MethodSource("provideNullScenarios")
    @DisplayName("Should throw IllegalArgumentException when searching for available rooms with null dates")
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
    @DisplayName("Should throw IllegalArgumentException if the end time is before the start time during availability search")
    void getAvailableRoomsThrowsExceptionWhenEndTimeIsBeforeStartTime() {

            LocalDateTime currentTime = LocalDateTime.now();
            LocalDateTime startTime = currentTime.plusDays(4);
            LocalDateTime endTime = currentTime.plusDays(3);

            var exception = assertThrows(IllegalArgumentException.class,
                    () -> bookingSystem.getAvailableRooms(startTime, endTime));
            assertThat(exception).hasMessage("Sluttid måste vara efter starttid");
    }

    @Test
    @DisplayName("Should return a list of all rooms that do not have overlapping bookings for the given period")
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
    @DisplayName("Should throw IllegalArgumentException if the booking ID to cancel is null")
    void cancelBookingThrowsExceptionWhenBookingIdIsNull() {

        var exception = assertThrows(IllegalArgumentException.class,
                () -> bookingSystem.cancelBooking(null));

        assertThat(exception).hasMessage("Boknings-id kan inte vara null");
    }

    @Test
    @DisplayName("Should successfully cancel a booking, update the correct room, and send a cancellation notification")
    void cancelBooking() throws NotificationException {

        LocalDateTime currentTime = LocalDateTime.now();
        LocalDateTime startTime = currentTime.plusDays(3);
        LocalDateTime endTime = currentTime.plusDays(5);
        Booking booking1 = new Booking("1", "100", startTime, endTime);
        Booking booking2 = new Booking("2", "200", startTime, endTime);
        Room room1 = new Room("100", "Room1");
        Room room2 = new Room("102", "Room2");

        room1.addBooking(booking1);
        room2.addBooking(booking2);

        Mockito.when(timeProvider.getCurrentTime()).thenReturn(currentTime);
        Mockito.when(roomRepository.findAll()).thenReturn(List.of(room1, room2));

        boolean result = bookingSystem.cancelBooking("1");

        verify(notificationService).sendCancellationConfirmation(bookingCaptor.capture());

        assertThat(result).isTrue();
        assertThat(room1.hasBooking("1")).isFalse();
        assertThat(room2.hasBooking("2")).isTrue();

        verify(roomRepository).save(room1);
    }

    @Test
    @DisplayName("Should throw IllegalStateException when trying to cancel a booking that has already started or finished")
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

    @Test
    @DisplayName("Should return false when attempting to cancel a booking ID that is not found in any room")
    void cancelBookingShouldReturnFalseIfBookingDoesNotExist() {
        Room room = new Room("100", "Room1");

        Mockito.when(roomRepository.findAll()).thenReturn(List.of(room));
        boolean result = bookingSystem.cancelBooking("999");

        assertThat(result).isFalse();
    }

}
