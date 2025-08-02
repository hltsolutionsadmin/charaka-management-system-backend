package com.hlt.healthcare.controllers;


import com.hlt.commonservice.dto.MessageResponse;
import com.hlt.commonservice.dto.NotificationDTO;
import com.hlt.commonservice.user.UserDetailsImpl;
import com.hlt.healthcare.model.NotificationModel;
import com.hlt.healthcare.populator.NotificationPopulator;
import com.hlt.healthcare.service.NotificationService;
import com.hlt.utils.AbstractConverter;
import com.hlt.utils.JTBaseEndpoint;
import com.hlt.utils.JuavaryaConstants;
import com.hlt.utils.SecurityUtils;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/usernotification")
@Slf4j
public class NotificationController extends JTBaseEndpoint {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private NotificationPopulator notificationPopulator;

    @SuppressWarnings("unchecked")
    public AbstractConverter getConverterInstance() {
        return getConverter(notificationPopulator, NotificationDTO.class.getName());
    }

    @DeleteMapping("/delete")
    public ResponseEntity delete(@Valid @RequestParam Long notificationId) {
        log.info("Entering delete API with notificationId: {}", notificationId);
        NotificationModel notificationModel = notificationService.findById(notificationId);
        if (null != notificationModel) {
            notificationService.removeNotification(notificationModel);
            log.info("Notification deleted successfully with id: {}", notificationId);
            return ResponseEntity.ok().body(new MessageResponse("Notification Deleted"));
        }
        log.warn("Notification not found with id: {}", notificationId);
        return ResponseEntity.badRequest().body(new MessageResponse("Notification Not Found With Given Id"));
    }

    @SuppressWarnings("unchecked")
    @GetMapping("/user/list")
    public ResponseEntity getUserNotifications(@RequestParam int pageNo, @RequestParam int pageSize)
            throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        UserDetailsImpl userDetails = SecurityUtils.getCurrentUserDetails();
        Pageable pageable = PageRequest.of(pageNo, pageSize);

        Page<NotificationModel> notifications = notificationService.findByUser(userDetails.getId(), pageable);

        if (null == notifications || CollectionUtils.isEmpty(notifications.getContent())) {
            return null;
        }
        Map<String, Object> response = new HashMap<>();
        response.put(JuavaryaConstants.CURRENT_PAGE, notifications.getNumber());
        response.put(JuavaryaConstants.TOTAL_ITEMS, notifications.getTotalElements());
        response.put(JuavaryaConstants.TOTAL_PAGES, notifications.getTotalPages());
        response.put(JuavaryaConstants.PAGE_NUM, pageNo);
        response.put(JuavaryaConstants.PAGE_SIZE, pageSize);
        if (!CollectionUtils.isEmpty(notifications.getContent())) {
            List<NotificationDTO> list = getConverterInstance().convertAll(notifications.getContent());
            response.put(JuavaryaConstants.PROFILES, list);
        }
        return new ResponseEntity<>(response, HttpStatus.OK);

    }

    @DeleteMapping("/clear-all")
    public ResponseEntity<MessageResponse> clearAllNotifications() {
        UserDetailsImpl userDetails = SecurityUtils.getCurrentUserDetails();
        notificationService.clearAllNotifications(userDetails.getId());
        return ResponseEntity.ok().body(new MessageResponse("Notifications Deleted Successfully!"));
    }
}
