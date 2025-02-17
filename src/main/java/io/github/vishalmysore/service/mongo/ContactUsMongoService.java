package io.github.vishalmysore.service.mongo;

import io.github.vishalmysore.data.ContactUs;
import io.github.vishalmysore.service.base.ContactUsDBService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service("contactUsDBService")
@ConditionalOnProperty(name = "easyQZ_DBTYPE", havingValue = "mongo", matchIfMissing = true)
public class ContactUsMongoService extends MongoService implements ContactUsDBService {

    @Async
    public void insertSupportTicket(ContactUs supportTicket) {
        try {
            if (supportTicket == null) {
                log.error("Received null score object.");
                return;
            }



            // Insert the new support ticket into MongoDB
            mongoTemplate.insert(supportTicket, CONTACTUS_TABLE_NAME);

            // Log the essential details
            log.info("Support ticket created for email: " + supportTicket.getEmail() + ", type: " + supportTicket.getType());

        } catch (Exception e) {
            log.error("Error occurred while inserting support ticket: " + e.getMessage());
        }
    }

}
