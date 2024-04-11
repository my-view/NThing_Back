package data.controller;

import data.dto.ApiResult;
import data.dto.ChatMessageDto;
import data.dto.ChatRoomDto;
import data.dto.CommentDto;
import data.service.ChatService;
import data.util.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class ChatController {
    // Broker로 메시지 전달하는 역할
    // @EnableWebSocketMessageBroker 로 등록되는 빈임
    //private final SimpMessagingTemplate simpMessagingTemplate;
    private final ChatService chatService;
    private final JwtProvider jwtProvider;

    // 채팅 목록, 채팅방 삭제 혹은, 거래 완료 여부 구별하여 리턴하도록, 10개씩?
    @GetMapping("/chat/rooms")
    public ResponseEntity<ApiResult<List<ChatRoomDto>>> findChatRoomsByUserId(
            @RequestHeader(value = "Authorization", required = false) String token) {
        int userId = tokenValidation(token);
        return ResponseEntity.ok(ApiResult.ok(chatService.findChatRoomsByUserId(userId)));
    }





    // 스톰프 COMMAND : SEND 메시지 처리
    @MessageMapping("/{roomId}") // prifix(send) + roomid 클라이언트가 메시지를 보낼 경로.
    @SendTo("/room/{roomId}")  // (@DestinationVariable : 경로변수값에 대한 맵핑)
    public ChatMessageDto chat(@DestinationVariable int roomId, ChatMessageDto chatMessageDto, SimpMessageHeaderAccessor accessor) {

        Map<String, ?> headers = accessor.getMessageHeaders();
        List<?> authorization = accessor.getNativeHeader("Authorization");
        String token = authorization.get(0).toString();
        int userId = jwtProvider.parseJwt(token);

        // 출력 결과 : controller : {simpMessageType=MESSAGE, stompCommand=SEND,
        // nativeHeaders ={Authorization=[Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJsb2dpbklkIjoxMDMsImlzcyI6InRlc3QiLCJpYXQiOjE3MDk2MzQ2ODUsImV4cCI6MTcwOTcyMTA4NX0.HBgr5AKgOrVa5iEwvrXsGjTwbverY5UroSslZZiq4bI], destination=[/send/1], content-length=[24]},
        // DestinationVariableMethodArgumentResolver.templateVariables={roomId=1},
        // simpSessionAttributes={},
        // simpHeartbeat=[J@330e7aab, lookupDestination=/1, simpSessionId=b7a7034b-5fb4-840a-3024-8c3153437fe6, simpDestination=/send/1}

        // DB에 채팅 생성 후
        // 해당 메시지 리턴 -> sendTo의 앤드포인트로 이동함
        chatMessageDto.setChatRoomId(roomId);
        chatMessageDto.setSenderId(userId);

        // sender id도 토큰을 통해 가져와서 메시지에 넣어야함
        chatService.createChatMessage(chatMessageDto); // 생성 후 id 담았다.
        return chatMessageDto;
    }

    private int tokenValidation(String token) {
        if (token != null) {
            return jwtProvider.parseJwt(token);
        }
        return 0;
    }
}
