package data.mapper;

import data.dto.UserDto;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper {
    int join(UserDto.Create dto);
    UserDto.User findUser(UserDto.Search dto);
    void updateUser(UserDto.Update dto);
    void deleteUser(int id);
}
