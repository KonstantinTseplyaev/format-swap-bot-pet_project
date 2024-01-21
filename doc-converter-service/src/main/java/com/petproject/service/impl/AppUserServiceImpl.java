package com.petproject.service.impl;

import com.petproject.dao.AppUserDao;
import com.petproject.entity.AppUser;
import com.petproject.service.AppUserService;
import com.petproject.service.ProducerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AppUserServiceImpl implements AppUserService {
    private final ProducerService producerService;
    private final AppUserDao appUserDao;

    @Override
    public String registerUser(AppUser user) {
        user.setActive(true);
        appUserDao.save(user);
        return "Ура! Вы успешно зарегистрировались!";
    }
}
