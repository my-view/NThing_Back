package data.controller;

import data.dto.ChatMessageDto;
import data.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class ChatController {
    // Broker로 메시지 전달하는 역할
    // @EnableWebSocketMessageBroker 로 등록되는 빈임
    private final SimpMessagingTemplate simpMessagingTemplate;
    private final ChatService chatService;

    @MessageMapping("/{roomId}") // prifix(send) + roomid 클라이언트가 메시지를 보낼 경로.
    @SendTo("/room/{roomId}")  // (@DestinationVariable : 경로변수값에 대한 맵핑)
    public ChatMessageDto chat(@DestinationVariable int roomId, ChatMessageDto chatMessageDto) {

        // DB에 채팅 생성 후
        // 해당 메시지 리턴 -> sendTo의 앤드포인트로 이동함
        chatMessageDto.setChatRoomId(roomId);
        // sender id도 토큰을 통해 가져와서 메시지에 넣어야함
        chatService.createChatMessage(chatMessageDto); // 생성 후 id 담았다.
        return chatMessageDto;
    }
}