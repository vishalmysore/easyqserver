package io.github.vishalmysore.service.mongo;

import io.github.vishalmysore.data.GoogleUser;
import io.github.vishalmysore.service.base.GoogleDBService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class GoogleMongoService extends MongoService implements GoogleDBService {


    @Override
    public void insertGoogleUser(GoogleUser googleUser) {
        log.info("Inserting google user: " + googleUser);
    }

    @Override
    public void updateLoginAndLogoutTime(String email, String status) {
       log.info("Updating login and logout time for email: " + email + " status: " + status);
    }

    @Override
    public GoogleUser getGoogleUserByEmail(String email) {
        log.info("Getting google user by email: " + email);
        return null;
    }
}
