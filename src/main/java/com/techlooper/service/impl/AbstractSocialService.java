package com.techlooper.service.impl;

import com.techlooper.entity.UserEntity;
import com.techlooper.entity.UserProfile;
import com.techlooper.model.SocialConfig;
import com.techlooper.model.SocialProvider;
import com.techlooper.repository.JsonConfigRepository;
import com.techlooper.service.SocialService;
import com.techlooper.service.UserService;
import org.dozer.Mapper;
import org.jasypt.util.password.PasswordEncryptor;
import org.springframework.social.connect.support.OAuth1ConnectionFactory;
import org.springframework.social.connect.support.OAuth2ConnectionFactory;
import org.springframework.social.oauth1.AuthorizedRequestToken;
import org.springframework.social.oauth1.OAuth1Operations;
import org.springframework.social.oauth1.OAuth1Parameters;
import org.springframework.social.oauth1.OAuthToken;
import org.springframework.social.oauth2.AccessGrant;

import javax.annotation.Resource;
import java.util.Optional;

import static com.techlooper.entity.AccessGrant.AccessGrantBuilder.accessGrant;
import static com.techlooper.entity.UserEntity.UserEntityBuilder.userEntity;
import static com.techlooper.model.SocialProvider.LINKEDIN;

/**
 * Created by phuonghqh on 12/15/14.
 */
public abstract class AbstractSocialService implements SocialService {

  @Resource
  protected UserService userService;

  @Resource
  protected Mapper dozerBeanMapper;

  @Resource
  protected PasswordEncryptor passwordEncryptor;

  protected SocialConfig socialConfig;

  public AbstractSocialService(JsonConfigRepository jsonConfigRepository, SocialProvider socialProvider) {
    socialConfig = jsonConfigRepository.getSocialConfig().stream()
      .filter(config -> socialProvider == config.getProvider()).findFirst().get();
  }

  public com.techlooper.entity.AccessGrant getAccessGrant(String accessCode) {
    AccessGrant access = getOAuth2ConnectionFactory().getOAuthOperations().exchangeForAccess(accessCode, socialConfig.getRedirectUri(), null);
    return dozerBeanMapper.map(access, com.techlooper.entity.AccessGrant.class);
  }

  public com.techlooper.entity.AccessGrant getAccessGrant(String accessToken, String accessSecret) {
    OAuth1Operations oAuthOperations = getOAuth1ConnectionFactory().getOAuthOperations();
    if (Optional.ofNullable(accessToken).isPresent()) {
      OAuthToken token = oAuthOperations.exchangeForAccessToken(
        new AuthorizedRequestToken(new OAuthToken(accessToken, null), accessSecret), null);
      return dozerBeanMapper.map(token, com.techlooper.entity.AccessGrant.class);
    }
    OAuthToken token = oAuthOperations.fetchRequestToken(socialConfig.getRedirectUri(), null);
    String authorizeUrl = oAuthOperations.buildAuthorizeUrl(token.getValue(), OAuth1Parameters.NONE);
    return accessGrant().withAuthorizeUrl(authorizeUrl).build();
  }

  protected AccessGrant getAccessGrant(com.techlooper.entity.AccessGrant accessGrant) {
    return new AccessGrant(accessGrant.getAccessToken(), accessGrant.getScope(), accessGrant.getRefreshToken(), accessGrant.getExpireTime());
  }

  public OAuth2ConnectionFactory getOAuth2ConnectionFactory() {
    throw new UnsupportedOperationException("Method is not supported");
  }

  public OAuth1ConnectionFactory getOAuth1ConnectionFactory() {
    throw new UnsupportedOperationException("Method is not supported");
  }

  public abstract UserProfile getProfile(com.techlooper.entity.AccessGrant accessGrant);

  public UserEntity saveFootprint(com.techlooper.entity.AccessGrant accessGrant, String key) {
    UserEntity entity = userService.findUserEntityByKey(key);
    if (!Optional.ofNullable(entity).isPresent()) {
      throw new UnsupportedOperationException("Method is not supported");
    }
    userEntity(entity).withProfile(socialConfig.getProvider(), getProfile(accessGrant));
    userService.save(entity);
    return entity;
  }
}
