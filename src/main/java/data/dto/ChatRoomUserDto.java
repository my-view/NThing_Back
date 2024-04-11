package data.dto;

import lombok.Data;
import org.apache.ibatis.type.Alias;

import java.sql.Date;

@Data
@Alias("ChatRoomUserDto")
public class ChatRoomUserDto {
    private int id;
    private int userId;
    private int chatRoomId;
    private Date joinedAt;
    private Date leftAt;
}
