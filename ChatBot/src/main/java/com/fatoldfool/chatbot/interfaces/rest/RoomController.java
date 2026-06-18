package com.fatoldfool.chatbot.interfaces.rest;

import com.fatoldfool.chatbot.application.command.CreateRoomCommand;
import com.fatoldfool.chatbot.application.result.CreateRoomResult;
import com.fatoldfool.chatbot.application.usecase.CreateRoomUseCase;
import com.fatoldfool.chatbot.application.usecase.GetRoomMessagesUseCase;
import com.fatoldfool.chatbot.domain.model.ChatRoom;
import com.fatoldfool.chatbot.domain.model.Message;
import com.fatoldfool.chatbot.domain.port.ChatRoomRepository;
import com.fatoldfool.chatbot.interfaces.rest.dto.CreateRoomRequest;
import com.fatoldfool.chatbot.interfaces.rest.dto.RoomResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    private final ChatRoomRepository chatRoomRepository;
    private final GetRoomMessagesUseCase getRoomMessagesUseCase;

    public RoomController(CreateRoomUseCase createRoomUseCase,
                          ChatRoomRepository chatRoomRepository,
                          GetRoomMessagesUseCase getRoomMessagesUseCase) {
        this.createRoomUseCase = createRoomUseCase;
        this.chatRoomRepository = chatRoomRepository;
        this.getRoomMessagesUseCase = getRoomMessagesUseCase;
    }

    @PostMapping
    public ResponseEntity<?> createRoom(@Valid @RequestBody CreateRoomRequest request) {
        CreateRoomCommand command = new CreateRoomCommand(request.getRoomName(), request.getCreatorSessionId());
        CreateRoomResult result = createRoomUseCase.execute(command);
        if (result.success()) {
            RoomResponse response = toResponse(result.room());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } else {
            return ResponseEntity.badRequest().body(Map.of("error", result.errorMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<List<RoomResponse>> getAllRooms() {
        List<ChatRoom> rooms = chatRoomRepository.findAll();
        List<RoomResponse> responses = rooms.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{roomId}/messages")
    public ResponseEntity<?> getRoomMessages(@PathVariable Long roomId) {
        List<Message> messages = getRoomMessagesUseCase.execute(roomId);
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

    private RoomResponse toResponse(ChatRoom room) {
        return new RoomResponse(room.getId(), room.getName(), room.getCreatedAt().toString());
    }
}