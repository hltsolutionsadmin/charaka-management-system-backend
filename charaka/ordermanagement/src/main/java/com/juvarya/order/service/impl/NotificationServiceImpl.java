package com.juvarya.order.service.impl;



import com.juvarya.order.dao.NotificationRepository;
import com.juvarya.order.entity.NotificationModel;
import com.juvarya.order.service.NotificationService;
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
