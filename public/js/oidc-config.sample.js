const OIDC_CONFIG = {
  authority: AUTHORITY,
  client_id: CLIENT_ID,
  scope: SCOPE,
  response_type: "id_token token",
  filterProtocolClaims: true,
  redirect_uri: window.location.origin,
  loadUserInfo: true,
  automaticSilentRenew: false,
  silent_redirect_uri: 'http://localhost:4200/silent-refresh.html'
}