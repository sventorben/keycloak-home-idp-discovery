package de.sventorben.keycloak.authentication.hidpd;

import static org.keycloak.services.validation.Validation.FIELD_USERNAME;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import org.jboss.logging.Logger;
import org.jboss.resteasy.specimpl.MultivaluedMapImpl;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.AuthenticationFlowError;
import org.keycloak.authentication.authenticators.browser.AbstractUsernameFormAuthenticator;
import org.keycloak.events.Details;
import org.keycloak.events.Errors;
import org.keycloak.forms.login.LoginFormsProvider;
import org.keycloak.models.FederatedIdentityModel;
import org.keycloak.models.IdentityProviderModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.ModelDuplicateException;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.utils.KeycloakModelUtils;
import org.keycloak.protocol.oidc.OIDCLoginProtocol;
import org.keycloak.services.managers.AuthenticationManager;

final class HomeIdpDiscoveryAuthenticator extends AbstractUsernameFormAuthenticator {

	private static final Logger LOG = Logger.getLogger(HomeIdpDiscoveryAuthenticator.class);

	HomeIdpDiscoveryAuthenticator() {
	}

	@Override
	public void authenticate(AuthenticationFlowContext context) {
		MultivaluedMap<String, String> formData = new MultivaluedMapImpl<>();
		String loginHint = context.getAuthenticationSession().getClientNote(OIDCLoginProtocol.LOGIN_HINT_PARAM);

		String rememberMeUsername = AuthenticationManager.getRememberMeUsername(context.getRealm(), context.getHttpRequest().getHttpHeaders());

		if (loginHint != null || rememberMeUsername != null) {
			if (loginHint != null) {
				formData.add(AuthenticationManager.FORM_USERNAME, loginHint);
			} else {
				formData.add(AuthenticationManager.FORM_USERNAME, rememberMeUsername);
				formData.add("rememberMe", "on");
			}
		}
		Response challengeResponse = challenge(context, formData);
		context.challenge(challengeResponse);
	}

	@Override
	public void action(AuthenticationFlowContext context) {
		MultivaluedMap<String, String> formData = context.getHttpRequest().getDecodedFormParameters();
		if (formData.containsKey("cancel")) {
			context.cancelLogin();
			return;
		}

		String username = setUserInContext(context, formData);
		if (username == null) {
			return;
		}

		UserModel user = context.getUser();

		final Optional<IdentityProviderModel> homeIdp = discoverHomeIdp(context, user, username);

		if (homeIdp.isEmpty()) {
			context.attempted();
		} else {
			new Redirector(context).redirectTo(homeIdp.get());
		}
	}

	private String setUserInContext(AuthenticationFlowContext context, MultivaluedMap<String, String> inputData) {
		context.clearUser();

		String username = inputData.getFirst(AuthenticationManager.FORM_USERNAME);

		if (username != null) {
			username = username.trim();
			if ("".equalsIgnoreCase(username))
				username = null;
		}

		if (username == null) {
			context.getEvent().error(Errors.USER_NOT_FOUND);
			Response challengeResponse = challenge(context, getDefaultChallengeMessage(context), FIELD_USERNAME);
			context.failureChallenge(AuthenticationFlowError.INVALID_USER, challengeResponse);
			return null;
		}

		context.getEvent().detail(Details.USERNAME, username);
		context.getAuthenticationSession().setAuthNote(AbstractUsernameFormAuthenticator.ATTEMPTED_USERNAME, username);

		try {
			UserModel user = KeycloakModelUtils.findUserByNameOrEmail(context.getSession(), context.getRealm(), username);
			if (user != null) {
				context.setUser(user);
			}
		} catch (ModelDuplicateException ex) {
			LOG.debugf(ex, "Could not find user %s", username);
		}

		return username;
	}

	protected Response challenge(AuthenticationFlowContext context, MultivaluedMap<String, String> formData) {
		LoginFormsProvider forms = context.form();
		if (!formData.isEmpty()) {
			forms.setFormData(formData);
		}
		return forms.createLoginUsername();
	}

	@Override
	protected Response createLoginForm(LoginFormsProvider form) {
		return form.createLoginUsername();
	}

	@Override
	protected String getDefaultChallengeMessage(AuthenticationFlowContext context) {
		return context.getRealm().isLoginWithEmailAllowed() ? "invalidUsernameOrEmailMessage" : "invalidUsernameMessage";
	}

	private static Optional<IdentityProviderModel> discoverHomeIdp(AuthenticationFlowContext context, UserModel user, String username) {

		Optional<IdentityProviderModel> homeIdp = Optional.empty();

		final Optional<String> emailDomain;
		if (user == null) {
			emailDomain = getEmailDomain(username);
		} else {
			emailDomain = getEmailDomain(user);
		}

		if (emailDomain.isPresent()) {
			String domain = emailDomain.get();
			homeIdp = discoverHomeIdp(context, domain, user, username);
			if (homeIdp.isEmpty()) {
				LOG.tracef("Could not find home IdP for domain %s and user %s", domain, username);
				homeIdp = discoverHomeIdp(context, "*", user, username);
				if (homeIdp.isEmpty()) {
					LOG.tracef("Could not find home IdP for domain %s and user %s", "*", username);
				}
			}
		} else {
			LOG.warnf("Could not extract domain from email address %s", username);
		}

		return homeIdp;
	}

	private static Optional<IdentityProviderModel> discoverHomeIdp(AuthenticationFlowContext context, String domain, UserModel user, String username) {
		final Map<String, String> linkedIdps;

		HomeIdpDiscoveryConfig config = new HomeIdpDiscoveryConfig(context.getAuthenticatorConfig());
		if (user == null || !config.forwardToLinkedIdp()) {
			linkedIdps = Collections.emptyMap();
		} else {
			linkedIdps = context.getSession().userLocalStorage()
					.getFederatedIdentitiesStream(context.getRealm(), user)
					.collect(
							Collectors.toMap(FederatedIdentityModel::getIdentityProvider, FederatedIdentityModel::getUserName));
		}

		// enabled IdPs with domain
		List<IdentityProviderModel> idpsWithDomain = context.getRealm().getIdentityProvidersStream()
				.filter(IdentityProviderModel::isEnabled)
				.filter(it -> new IdentityProviderModelConfig(it).hasDomain(domain))
				.collect(Collectors.toList());

		// Linked IdPs with matching domain
		Optional<IdentityProviderModel> homeIdp = idpsWithDomain.stream()
				.filter(it -> linkedIdps.containsKey(it.getAlias()))
				.findFirst();

		// linked and enabled IdPs
		if (homeIdp.isEmpty() && !linkedIdps.isEmpty()) {
			homeIdp = context.getRealm().getIdentityProvidersStream()
					.filter(IdentityProviderModel::isEnabled)
					.filter(it -> linkedIdps.containsKey(it.getAlias()))
					.findFirst();
		}

		// Matching domain
		if (homeIdp.isEmpty()) {
			homeIdp = idpsWithDomain.stream().findFirst();
			if (homeIdp.isEmpty()) {
				List<IdentityProviderModel> idpsWithFallback = context.getRealm().getIdentityProvidersStream()
						.filter(IdentityProviderModel::isEnabled)
						.filter(it -> new IdentityProviderModelConfig(it).isFallback())
						.collect(Collectors.toList());
				homeIdp = idpsWithFallback.stream().findFirst();
			}
		}

		homeIdp.ifPresent(it -> {
			if (linkedIdps.containsKey(it.getAlias()) && config.forwardToLinkedIdp()) {
				String idpUsername = linkedIdps.get(it.getAlias());
				context.getAuthenticationSession().setClientNote(OIDCLoginProtocol.LOGIN_HINT_PARAM, idpUsername);
			} else {
				context.getAuthenticationSession().setClientNote(OIDCLoginProtocol.LOGIN_HINT_PARAM, username);
			}
		});

		return homeIdp;
	}

	private static Optional<String> getEmailDomain(UserModel user) {
		Optional<String> domain = Optional.empty();
		if (user.isEnabled() && user.isEmailVerified()) {
			String email = user.getEmail();
			domain = getEmailDomain(email);
		}
		return domain;
	}

	private static Optional<String> getEmailDomain(String email) {
		String domain = null;
		if (email != null) {
			int atIndex = email.trim().lastIndexOf("@");
			if (atIndex >= 0) {
				domain = email.substring(atIndex + 1).trim();
				if (domain.length() == 0) {
					domain = null;
				}
			}
		}
		return Optional.ofNullable(domain);
	}

	@Override
	public boolean requiresUser() {
		return false;
	}

	@Override
	public boolean configuredFor(KeycloakSession session, RealmModel realm, UserModel user) {
		return true;
	}

	@Override
	public void setRequiredActions(KeycloakSession session, RealmModel realm, UserModel user) {
	}

}
