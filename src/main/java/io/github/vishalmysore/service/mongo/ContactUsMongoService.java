package io.github.vishalmysore.service.mongo;

import io.github.vishalmysore.data.ContactUs;
import io.github.vishalmysore.service.base.ContactUsDBService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ContactUsMongoService extends MongoService implements ContactUsDBService {

    @Override
    public void insertSupportTicket(ContactUs score) {

    }
}
