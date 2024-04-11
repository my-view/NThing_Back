//package data;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import data.dto.ChatMessageDto;
//import data.dto.ChatRoomDto;
//import data.service.ChatRoomManger;
//import data.service.ChatService;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.stereotype.Component;
//import org.springframework.web.socket.CloseStatus;
//import org.springframework.web.socket.TextMessage;
//import org.springframework.web.socket.WebSocketSession;
//import org.springframework.web.socket.handler.TextWebSocketHandler;
//
//import java.io.IOException;
//import java.util.Set;
////스톰프는 앤드포인트를 거치지 않는다.
//@Slf4j
//@Component
//@RequiredArgsConstructor
//public class WebSocketHandler extends TextWebSocketHandler {
//    private final ObjectMapper objectMapper;
//    private final ChatService chatService;
//
//    // 인메모리 구현한 코드
//    private final ChatRoomManger chatRoomManger;
//
//    @Override
//    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
//        super.afterConnectionEstablished(session);
//        System.out.println("연결됐나??");
//    }
//
//    // 채팅방은 거래 생성시에 함께 생성됨
//    @Override
//    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
//        String payload = message.getPayload();
//        System.out.println("payload : " + payload);
//        // 일단 메시지 객체에 넣어서 정보를 활용함
//        ChatMessageDto chatMessage = objectMapper.readValue(payload, ChatMessageDto.class);
//        // 구매글이 작성되면 방이 생성되어야 하기 때문에 없는 방일 가능성은 없음(구매글 삭제시 함께 삭제? deleted y/n으로 처리할 것)
//        ChatRoomDto chatRoom = chatRoomManger.getChatRoom(chatMessage.getChatRoomId());
//        //Set<WebSocketSession> sessions = chatRoom.getSessions(); // 해당 방에 있는 사용자 세션 WebsocketSession
//
//        // 입장 -> 방 + 유저 의 연결
//
//        // 이후 모든 senderId의 내용을 sender 닉네임을 가져오도록 수정해야함
//        switch (chatMessage.getWebSocketMessageType()) {
//            case ENTER:
//                // 0. 채팅방은 이미 있다는 전제하에 동작 수행
//                // 1. 채팅방과 유저 데이터 입력(create or update 로 구현해야함. 들락날락할수도 있으니까/ 객체와 DB모두)
//
//                chatRoom.setUserId(chatMessage.getSenderId());
//                chatService.createOrUpdateChatRoomUser(chatRoom);
//                sessions.add(session);
//                // 2.메시지 설정 및 저장
//                chatMessage.setMessage(chatMessage.getSender() + "님이 입장했습니다."); // ENTER, QUIT의 경우 내용 없으므로 set
//                chatService.createChatMessage(chatMessage);
//                sendToEachSocket(sessions, new TextMessage(objectMapper.writeValueAsString(chatMessage)));
//                break;
//
//            case OUIT:
//                chatRoom.setUserId(chatMessage.getSenderId());
//                // 1. 채팅방과 유저 중간테이블 데이터 업데이트 - leftAt
//                chatService.deleteChatRoomUser(chatRoom);
//                sessions.remove(session);
//                // 2. 메시지 설정 및 저장
//                chatMessage.setMessage(chatMessage.getSender() + "님이 퇴장했습니다.");
//                chatService.createChatMessage(chatMessage);
//                sendToEachSocket(sessions, new TextMessage(objectMapper.writeValueAsString(chatMessage)));
//                break;
//
//            //case LIST: 는 어떻게 줘야하지 -
//
//            default: // WRITE인 경우 - IMAGE인 경우에 대해 구현하지 않음
//
//                switch (chatMessage.getChatMessageType()) {
//
//                    case KICK_OUT:
//                        // quit과 같음
//                        chatRoom.setUserId(chatMessage.getSenderId());
//                        chatService.deleteChatRoomUser(chatRoom);
//                        sessions.remove(session);
//                        chatMessage.setMessage(chatMessage.getSender() + "님이 강퇴되었습니다."); // 강퇴...
//                        sendToEachSocket(sessions, new TextMessage(objectMapper.writeValueAsString(chatMessage)));
//                        break;
//
//                    case COMPLETE:
//                        chatMessage.setMessage(chatMessage.getSender() + "님이 이 거래를 확정했습니다.");
//                        sendToEachSocket(sessions, new TextMessage(objectMapper.writeValueAsString(chatMessage)));
//                        break;
//
//                    default: // NORMAL의 경우 사용자가 보낸 메시지를 그대로 전송한다. 다만 - **비속어 필터 필요.
//                        sendToEachSocket(sessions, message);
//                }
//        }
//        // WebSocketMessageType, ChatMessageType, sender, message, sentAt, chatRoomId
//        chatService.createChatMessage(chatMessage);
//    }
//    private  void sendToEachSocket(Set<WebSocketSession> sessions, TextMessage message) {
//        sessions.parallelStream().forEach(roomSession -> {
//            try {
//                roomSession.sendMessage(message);
//            } catch (IOException e) {
//                throw new RuntimeException(e);
//            }
//        });
//    }
//
//    // 모든 세션을 없애는 건가?
//    @Override
//    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
//        super.afterConnectionClosed(session, status);
//    }
//
//}
