package bg.softuni.taskmaster.service.impl;

import bg.softuni.taskmaster.model.dto.UserChangePasswordDTO;
import bg.softuni.taskmaster.model.dto.UserEditDTO;
import bg.softuni.taskmaster.model.dto.UserInfoDTO;
import bg.softuni.taskmaster.model.dto.UserRegisterDTO;
import bg.softuni.taskmaster.model.entity.User;
import bg.softuni.taskmaster.model.enums.UserRoles;
import bg.softuni.taskmaster.repository.RoleRepository;
import bg.softuni.taskmaster.repository.UserRepository;
import bg.softuni.taskmaster.service.UserHelperService;
import bg.softuni.taskmaster.service.UserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@EnableWebSecurity
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final ModelMapper modelMapper;
    private final UserHelperService userHelperService;

    @Override
    public void register(UserRegisterDTO userRegisterDTO) {
        User user = modelMapper.map(userRegisterDTO, User.class);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.getRoles().add(roleRepository.getByName(UserRoles.USER));
        userRepository.save(user);

    }
    @Override
    public UserInfoDTO getInfo(Long id) {
        return modelMapper.map(userHelperService.getUser(id), UserInfoDTO.class);
    }

    private UserInfoDTO toInfo(User e) {
        UserInfoDTO infoDTO = modelMapper.map(e, UserInfoDTO.class);
        infoDTO.setAdmin(userHelperService.isAdmin(e));
        return infoDTO;
    }
    @Override
    public void edit(UserEditDTO userEditDTO) {
        User user = userHelperService.getUser();
        BeanUtils.copyProperties(userEditDTO, user);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        userRepository.deleteById(id);
    }

    @Override
    @Transactional
    public void makeAdmin(Long id) {
        User user = userHelperService.getUser(id);
        user.getRoles().add(roleRepository.getByName(UserRoles.ADMIN));
        userRepository.save(user);
    }

    @Override
    public void removeAdmin(Long id) {
        User user = userHelperService.getUser(id);
        user.getRoles().remove(roleRepository.getByName(UserRoles.ADMIN));
        userRepository.save(user);
    }

    @Override
    public void changePassword(UserChangePasswordDTO changePasswordDTO) {
        User user = userHelperService.getUser();
        user.setPassword(passwordEncoder.encode(changePasswordDTO.getNewPassword()));
        userRepository.save(user);
    }


    @Override
    public Page<UserInfoDTO> getAll(String searchQuery, Pageable pageable) {
        if (searchQuery.isEmpty()) {
            return userRepository.findAll(pageable).map(this::toInfo);
        }
        return userRepository.findAllBySearchQuery(searchQuery, pageable).map(this::toInfo);
    }

    @Override
    public UserEditDTO getCurrentUserEditData() {
        return modelMapper.map(userHelperService.getUser(), UserEditDTO.class);
    }
}
