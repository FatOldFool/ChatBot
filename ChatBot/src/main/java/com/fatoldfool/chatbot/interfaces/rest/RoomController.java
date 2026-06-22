package com.fatoldfool.chatbot.interfaces.rest;

import com.fatoldfool.chatbot.application.command.CreateRoomCommand;
import com.fatoldfool.chatbot.application.result.CreateRoomResult;
import com.fatoldfool.chatbot.application.usecase.CreateRoomUseCase;
import com.fatoldfool.chatbot.application.usecase.GetAllRoomsUseCase;
import com.fatoldfool.chatbot.application.usecase.GetRoomMessagesUseCase;
import com.fatoldfool.chatbot.domain.exception.TooManyRequestsException;
import com.fatoldfool.chatbot.domain.model.Message;
import com.fatoldfool.chatbot.infrastructure.ratelimit.RateLimiter;
import com.fatoldfool.chatbot.interfaces.rest.dto.CreateRoomRequest;
import com.fatoldfool.chatbot.interfaces.rest.dto.RoomResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/rooms")
@CrossOrigin(origins = "*")
public class RoomController {

    private final CreateRoomUseCase createRoomUseCase;
    private final GetAllRoomsUseCase getAllRoomsUseCase;
    private final GetRoomMessagesUseCase getRoomMessagesUseCase;
    private final RateLimiter rateLimiter;

    public RoomController(CreateRoomUseCase createRoomUseCase,
                          GetAllRoomsUseCase getAllRoomsUseCase,
                          GetRoomMessagesUseCase getRoomMessagesUseCase,
                          RateLimiter rateLimiter) {
        this.createRoomUseCase = createRoomUseCase;
        this.getAllRoomsUseCase = getAllRoomsUseCase;
        this.getRoomMessagesUseCase = getRoomMessagesUseCase;
        this.rateLimiter = rateLimiter;
    }

    @PostMapping
    public ResponseEntity<?> createRoom(@Valid @RequestBody CreateRoomRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName(); // username из токена

        if (!rateLimiter.tryAcquire("createRoom:" + username, 5, 60)) {
            throw new TooManyRequestsException("Слишком много запросов. Подождите минуту.");
        }

        CreateRoomCommand command = new CreateRoomCommand(request.getRoomName(), username);
        CreateRoomResult result = createRoomUseCase.execute(command);
        if (result.success()) {
            return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(result.room()));
        } else {
            return ResponseEntity.badRequest().body(Map.of("error", result.errorMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<List<RoomResponse>> getAllRooms() {
        List<RoomResponse> responses = getAllRoomsUseCase.execute().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{roomId}/messages")
    public ResponseEntity<?> getRoomMessages(@PathVariable Long roomId,
                                             @RequestParam(defaultValue = "50") int limit,
                                             @RequestParam(defaultValue = "0") int offset) {
        List<Message> messages = getRoomMessagesUseCase.execute(roomId, limit, offset);
        List<Map<String, Object>> response = messages.stream()
                .map(msg -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", msg.getId());
                    map.put("roomId", msg.getRoomId());
                    map.put("userId", msg.getUserId());
                    map.put("userName", msg.getUserName());
                    map.put("content", msg.getContent());
                    map.put("createdAt", msg.getCreatedAt().toString());
                    return map;
                })
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    private RoomResponse toResponse(com.fatoldfool.chatbot.domain.model.ChatRoom room) {
        return new RoomResponse(room.getId(), room.getName(), room.getCreatedAt().toString());
    }
}