package data.mapper;

import data.dto.UserDto;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface UserMapper {
    int join(UserDto userDto);
    void updateUser(UserDto userDto);
    void updateRefreshToken(UserDto userDto);
    List<UserDto> findAll();
    UserDto findById(int id);
    void deleteUser(int id);

    int findByEmail(String email);
    boolean isValidEmail(String email);
    String getTokenById(int id);
}
