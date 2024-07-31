package bg.softuni.taskmaster.service.impl;

import bg.softuni.taskmaster.events.AccountDeletionEvent;
import bg.softuni.taskmaster.exceptions.UserNotFoundException;
import bg.softuni.taskmaster.model.dto.UserInfoDTO;
import bg.softuni.taskmaster.model.entity.User;
import bg.softuni.taskmaster.repository.UserRepository;
import bg.softuni.taskmaster.service.UserHelperService;
import bg.softuni.taskmaster.service.UserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@EnableWebSecurity
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final UserHelperService userHelperService;
    private final ApplicationEventPublisher publisher;

    @Override
    public UserInfoDTO getInfo(Long id) {
        return toInfo(userHelperService.getUser(id));
    }

    private UserInfoDTO toInfo(User e) {
        UserInfoDTO userInfoDTO = modelMapper.map(e, UserInfoDTO.class);
        userInfoDTO.setAdmin(userHelperService.isAdmin(e.getId()));
        return userInfoDTO;
    }

    @Override
    @Transactional
    @PreAuthorize("@userHelperServiceImpl.getLoggedUser().id.equals(#id)  ||  hasRole('ADMIN')")
    //todo think how to don't call db
    public void delete(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(UserNotFoundException::new);
        userRepository.delete(user);
        publisher.publishEvent(new AccountDeletionEvent(this, user.getUsername(), user.getEmail()));
    }


    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public Page<UserInfoDTO> getAll(String searchQuery, Pageable pageable) {
        if (searchQuery.isEmpty()) {
            return userRepository.findAll(pageable).map(this::toInfo);
        }
        return userRepository.findAllBySearchQuery(searchQuery, pageable).map(this::toInfo);
    }

    @Override
    public boolean exists(Long id) {
        return userRepository.existsById(id);
    }
}
