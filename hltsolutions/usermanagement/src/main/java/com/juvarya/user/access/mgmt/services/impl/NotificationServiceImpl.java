package com.juvarya.user.access.mgmt.services.impl;


import com.juvarya.user.access.mgmt.model.NotificationModel;
import com.juvarya.user.access.mgmt.repository.NotificationRepository;
import com.juvarya.user.access.mgmt.services.NotificationService;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Transactional
public class NotificationServiceImpl implements NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    @Transactional
    @Override
    public NotificationModel save(NotificationModel jtNotification) {
        return notificationRepository.save(jtNotification);
    }

    @Override
    public NotificationModel findById(Long id) {
        Optional<NotificationModel> notification = notificationRepository.findById(id);
        return notification.orElse(null);

    }

    @Transactional
    @Override
    public void removeNotification(NotificationModel notificationModel) {
        notificationRepository.delete(notificationModel);

    }

    @Override
    public Page<NotificationModel> findByUser(Long userId, Pageable pageable) {
        return notificationRepository.findByUserIdOrderByCreationTimeDesc(userId, pageable);

    }

    @Override
    public void clearAllNotifications(Long userId) {
        notificationRepository.deleteAllByUserId(userId);
    }

}
