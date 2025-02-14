package io.github.vishalmysore.service.base;

import io.github.vishalmysore.data.GoogleUser;

public interface GoogleDBService{
    public void insertGoogleUser(GoogleUser googleUser);
    public void updateLoginAndLogoutTime(String email, String status);

    public GoogleUser getGoogleUserByEmail(String email);
}
