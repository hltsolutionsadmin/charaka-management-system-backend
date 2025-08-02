package com.juvarya.user.access.mgmt.scheduler;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import com.juvarya.user.access.mgmt.dto.enums.EnabledStatusSource;
import com.juvarya.user.access.mgmt.model.B2BUnitModel;
import com.juvarya.user.access.mgmt.repository.B2BUnitRepository;
import com.juvarya.user.access.mgmt.utils.BusinessTimingUtil;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class BusinessStatusScheduler {

	private static final ZoneId ZONE_IST = ZoneId.of("Asia/Kolkata");

	private final B2BUnitRepository b2BUnitRepository;

	public BusinessStatusScheduler(B2BUnitRepository b2BUnitRepository) {
		this.b2BUnitRepository = b2BUnitRepository;
	}

	@Scheduled(fixedRate = 60000) // Runs every minute
	@Transactional
	public void updateBusinessStatuses() {
		ZonedDateTime now = ZonedDateTime.now(ZONE_IST);
		log.info("BusinessStatusScheduler started at {}", now.toLocalTime());

		// Only fetch businesses that are allowed to be auto-managed
		List<B2BUnitModel> businesses = b2BUnitRepository.findByEnabledStatusSource(EnabledStatusSource.MANUAL);

		List<B2BUnitModel> toUpdate = new ArrayList<>();

		for (B2BUnitModel business : businesses) {
			boolean shouldBeEnabled = BusinessTimingUtil.isBusinessEnabled(business.getAttributes());

			if (business.isEnabled() != shouldBeEnabled) {
				business.setEnabled(shouldBeEnabled);
				business.setEnabledStatusSource(EnabledStatusSource.MANUAL); // re-confirming source
				toUpdate.add(business);

				log.debug("Business {} status changed to {} by scheduler", business.getId(), shouldBeEnabled);
			}
		}

		if (!toUpdate.isEmpty()) {
			b2BUnitRepository.saveAll(toUpdate);
			log.info("Scheduler updated {} businesses at {}", toUpdate.size(), now.toLocalTime());
		} else {
			log.info("No business status changes needed at {}", now.toLocalTime());
		}
	}
}
