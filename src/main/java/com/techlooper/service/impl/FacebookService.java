package com.techlooper.service.impl;

import com.techlooper.entity.AccessGrant;
import com.techlooper.entity.LinkedInProfile;
import com.techlooper.entity.UserEntity;
import com.techlooper.entity.UserProfile;
import com.techlooper.model.SocialProvider;
import com.techlooper.repository.JsonConfigRepository;
import org.springframework.social.connect.Connection;
import org.springframework.social.connect.support.OAuth2ConnectionFactory;
import org.springframework.social.facebook.api.Facebook;
import org.springframework.social.facebook.api.FacebookProfile;
import org.springframework.social.facebook.connect.FacebookConnectionFactory;
import org.springframework.social.linkedin.api.LinkedIn;
import org.springframework.social.linkedin.api.LinkedInProfileFull;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.inject.Inject;
import java.util.Optional;

import static com.techlooper.entity.UserEntity.UserEntityBuilder.userEntity;
import static com.techlooper.model.SocialProvider.FACEBOOK;

/**
 * Created by phuonghqh on 12/15/14.
 */
@Service("FACEBOOKService")
public class FacebookService extends AbstractSocialService {


  @Resource
  private FacebookConnectionFactory facebookConnectionFactory;

  @Inject
  public FacebookService(JsonConfigRepository jsonConfigRepository) {
    super(jsonConfigRepository, FACEBOOK);
  }

  public OAuth2ConnectionFactory getOAuth2ConnectionFactory() {
    return facebookConnectionFactory;
  }

  public UserProfile getProfile(AccessGrant accessGrant) {
    Connection<Facebook> connection = facebookConnectionFactory.createConnection(getAccessGrant(accessGrant));
    FacebookProfile profile = connection.getApi().userOperations().getUserProfile();
    com.techlooper.entity.FacebookProfile fbProfile = dozerBeanMapper.map(profile, com.techlooper.entity.FacebookProfile.class);
    fbProfile.setAccessGrant(accessGrant);
    return fbProfile;
  }

  public UserEntity saveFootprint(AccessGrant accessGrant) {
    com.techlooper.entity.FacebookProfile profileEntity = (com.techlooper.entity.FacebookProfile) getProfile(accessGrant);
    UserEntity userEntity = Optional.ofNullable(userService.findById(profileEntity.getEmail())).orElse(new UserEntity());
    UserEntity.UserEntityBuilder builder = userEntity(userEntity)
      .withProfile(SocialProvider.FACEBOOK, profileEntity)
      .withAccessGrant(dozerBeanMapper.map(accessGrant, AccessGrant.class));
    if (!Optional.ofNullable(userEntity.getEmailAddress()).isPresent()) {
      builder.withId(profileEntity.getEmail())
        .withLoginSource(SocialProvider.FACEBOOK)
        .withFirstName(profileEntity.getFirstName())
        .withLastName(profileEntity.getLastName())
        .withEmailAddress(profileEntity.getEmail())
        .withKey(passwordEncryptor.encryptPassword(profileEntity.getEmail()));
    }
    userService.save(userEntity);
    return userEntity;
  }
}
