package data.mapper;

import data.dto.UserDto;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface UserMapper {

    int join(UserDto userDto);
    List<UserDto> findAll();
    UserDto findById(int id);
    int findByEmail(String email);
    boolean isValidEmail(String email);
    void deleteUser(int id);

    String getTokenById(int id);
    void updateRefreshToken(UserDto userDto);
}
