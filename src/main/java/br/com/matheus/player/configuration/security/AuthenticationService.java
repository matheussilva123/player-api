package br.com.matheus.player.configuration.security;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.stereotype.Service;

@Service
public class AuthenticationService {

  @Value("${api.secret:}")
  private String apiSecret;

  public Authentication getAuthentication(final HttpServletRequest request) {
    String apiKey = request.getHeader("x-api-key");
    if (apiKey == null || !apiKey.equals(apiSecret)) {
      throw new BadCredentialsException("Invalid API Key");
    }

    return new ApiKeyAuthentication(apiKey, AuthorityUtils.NO_AUTHORITIES);
  }
}