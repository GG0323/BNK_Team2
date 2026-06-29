class ApiConstants {
  static const String baseUrl = 'https://192.168.0.87:8443';
  static const bool allowSelfSignedDevCertificate = true;

  static const String appPing = '/api/auth/session-remaining';
  static const String appLogin = '/member/login';
  static const String appLogout = '/member/logout';
  static const String appMe = '/api/member/myinfo';

  static const String memberMypage = '/api/member/mypage';
  static const String memberAccounts = '/api/member/myaccounts';

  static const String products = '/api/products';
  static const String productAiRecommend = '/api/products/ai/recommend';
  static const String productJoinEntryStatus =
      '/api/products/join/entry-status';
  static const String productJoinStart = '/api/products/join/start';
  static const String productJoinStatus = '/api/products/join/status';
  static const String productJoinTermsPdf = '/api/products/join/terms-pdf';
  static const String productJoinWithdrawalAccounts =
      '/api/products/join/withdrawal-accounts';
  static const String productJoinTerms = '/api/products/join/terms';
  static const String productJoinContractConfirm =
      '/api/products/join/contract/confirm';
  static const String productJoinSecurityChallenge =
      '/api/products/join/security-card/challenge';
  static const String productJoinComplete = '/api/products/join/complete';

  static const String reservationBranches = '/api/member/reservation/branches';
  static const String reservationSlots = '/api/member/reservation/slots';
  static const String reservationList = '/api/member/reservation/list';
  static const String reservationCreate = '/api/member/reservation/create';
  static const String reservationCancel = '/api/member/reservation/cancel';
}
