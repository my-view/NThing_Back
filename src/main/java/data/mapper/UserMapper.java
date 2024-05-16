package data.mapper;

import data.dto.UserDto;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

@Mapper
public interface UserMapper {
    int join(UserDto userDto);
    void updateUser(Map<String, Object> map);
    void updateRefreshToken(UserDto userDto);
    List<UserDto> findAll();
    UserDto findById(int id);
    void deleteUser(int id);

    int findByEmail(String email);
    boolean isValidEmail(String email);
    String getTokenById(int id);
}
