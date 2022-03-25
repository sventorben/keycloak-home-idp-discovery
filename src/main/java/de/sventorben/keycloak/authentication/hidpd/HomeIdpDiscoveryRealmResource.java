package de.sventorben.keycloak.authentication.hidpd;

import static java.util.Collections.emptyList;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.jboss.resteasy.spi.ResteasyProviderFactory;
import org.keycloak.models.IdentityProviderModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.services.resources.admin.AdminAuth;
import org.keycloak.services.resources.admin.AdminRoot;
import org.keycloak.services.resources.admin.permissions.AdminPermissionEvaluator;
import org.keycloak.services.resources.admin.permissions.AdminPermissions;

public final class HomeIdpDiscoveryRealmResource {

	@Context
	private HttpHeaders httpHeaders;

	private final RealmModel managedRealm;
	private final KeycloakSession session;
	private AdminPermissionEvaluator adminAuth;

	public HomeIdpDiscoveryRealmResource(RealmModel managedRealm, KeycloakSession session) {
		this.managedRealm = managedRealm;
		this.session = session;
	}

	@GET
	@Path("/{idpAlias}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response domains(@PathParam("idpAlias") String idpAlias) {

		adminAuth.realm().requireViewIdentityProviders();

		IdentityProviderModel idp = managedRealm.getIdentityProviderByAlias(idpAlias);

		Response.ResponseBuilder responseBuilder;
		if (idp == null) {
			responseBuilder = Response.status(Response.Status.NOT_FOUND);
		} else {
			IdpConfig idpConfig = new IdpConfig();
			IdentityProviderModelConfig config = new IdentityProviderModelConfig(idp);
			idpConfig.setDomains(config.getDomains().collect(Collectors.toList()));
			idpConfig.setFallback(config.isFallback());
			responseBuilder = Response.ok(idpConfig);
		}

		return responseBuilder.build();
	}

	@PUT
	@Path("/{idpAlias}")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response domains(@PathParam("idpAlias") String idpAlias, IdpConfig idpConfig) {
		adminAuth.realm().requireManageIdentityProviders();

		IdentityProviderModel idp = managedRealm.getIdentityProviderByAlias(idpAlias);

		Response.ResponseBuilder responseBuilder;
		if (idp == null) {
			responseBuilder = Response.status(Response.Status.NOT_FOUND);
		} else {
			IdentityProviderModelConfig config = new IdentityProviderModelConfig(idp);
			config.setDomains(config.getDomains().collect(Collectors.toList()));
			config.setIsFallback(config.isFallback());
			responseBuilder = Response.status(Response.Status.NO_CONTENT);
		}

		return responseBuilder.build();
	}

	final class IdpConfig {
		private List<String> domains;
		private boolean fallback;

		void setDomains(List<String> domains) {
			this.domains = Objects.requireNonNullElse(domains, emptyList());
		}

		public List<String> getDomains() {
			return domains;
		}

		public boolean isFallback() {
			return fallback;
		}

		public void setFallback(boolean isFallback) {
			this.fallback = Objects.requireNonNullElse(isFallback, false);
		}

	}

	void init() {
		MyAdminRoot adminRoot = new MyAdminRoot();
		ResteasyProviderFactory.getInstance().injectProperties(adminRoot);
		this.adminAuth = AdminPermissions.evaluator(session, session.getContext().getRealm(),
				adminRoot.authenticateRealmAdminRequest());
	}

	class MyAdminRoot extends AdminRoot {
		public AdminAuth authenticateRealmAdminRequest() {
			return this.authenticateRealmAdminRequest(httpHeaders);
		}
	}
}
