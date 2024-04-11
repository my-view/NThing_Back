package data.mapper;

import data.dto.ChatMessageDto;
import data.dto.ChatRoomDto;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ChatMapper {
    List<ChatRoomDto> findAllRoom();
    ChatRoomDto findRoomById(int id);
    void createChatRoom(ChatRoomDto chatRoomDto);

    ChatRoomDto _findChatRoomByPurchaseId(int purchaseId);

    void createChatMessage(ChatMessageDto chatMessage);

    void createOrUpdateChatRoomUser(ChatRoomDto chatRoomDto);

    void deleteChatRoomUser(ChatRoomDto chatRoomDto);

    List<ChatRoomDto> findChatRoomsByUserId(int userId);
}
