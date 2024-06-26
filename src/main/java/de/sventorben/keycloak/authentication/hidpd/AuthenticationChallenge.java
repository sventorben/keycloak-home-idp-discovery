package de.sventorben.keycloak.authentication.hidpd;

import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.models.IdentityProviderModel;
import org.keycloak.services.managers.AuthenticationManager;

import java.util.List;

final class AuthenticationChallenge {

    private final AuthenticationFlowContext context;
    private final RememberMe rememberMe;
    private final LoginHint loginHint;
    private final LoginForm loginForm;
    private final Reauthentication reauthentication;

    AuthenticationChallenge(AuthenticationFlowContext context, RememberMe rememberMe, LoginHint loginHint, LoginForm loginForm, Reauthentication reauthentication) {
        this.context = context;
        this.rememberMe = rememberMe;
        this.loginHint = loginHint;
        this.loginForm = loginForm;
        this.reauthentication = reauthentication;
    }

    void forceChallenge() {
        MultivaluedMap<String, String> formData = new MultivaluedHashMap<>();
        String loginHintUsername = loginHint.getFromSession();

        String rememberMeUsername = rememberMe.getUserName();

        if (reauthentication.required() && context.getUser() != null) {
            String attribute = context.getAuthenticatorConfig().getConfig().getOrDefault("userAttribute", "username");
            formData.add(AuthenticationManager.FORM_USERNAME, context.getUser().getFirstAttribute(attribute));
        } else {
            if (loginHintUsername != null || rememberMeUsername != null) {
                if (loginHintUsername != null) {
                    formData.add(AuthenticationManager.FORM_USERNAME, loginHintUsername);
                } else {
                    formData.add(AuthenticationManager.FORM_USERNAME, rememberMeUsername);
                    formData.add("rememberMe", "on");
                }
            }
        }

        Response challengeResponse;
        if (reauthentication.required()) {
            challengeResponse = loginForm.createWithSignInButtonOnly(formData);
        } else {
            challengeResponse = loginForm.create(formData);
        }

        context.challenge(challengeResponse);
    }

    void forceChallenge(List<IdentityProviderModel> homeIdps) {
        context.forceChallenge(loginForm.create(homeIdps));
    }

}
