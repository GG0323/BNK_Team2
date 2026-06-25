import 'api_client.dart';

class AccountOpeningApi {
  static const String _base = '/api/member/accounts/open';
  final ApiClient _client = ApiClient.instance;

  Future<Map<String, dynamic>> status() async {
    return _data(await _client.get('$_base/status', useAuthCookie: true));
  }

  Future<Map<String, dynamic>> agreePrivacy() async {
    return _data(await _client.post(
      '$_base/consent/privacy',
      body: {'agreed': true},
      useAuthCookie: true,
    ));
  }

  Future<Map<String, dynamic>> uploadIdCard(String filePath) async {
    return _data(await _client.postMultipart(
      '$_base/id-card',
      filePath: filePath,
      useAuthCookie: true,
    ));
  }

  Future<Map<String, dynamic>> getOcr() async {
    return _data(await _client.get('$_base/ocr', useAuthCookie: true));
  }

  Future<Map<String, dynamic>> updateOcr(Map<String, dynamic> ocr) async {
    return _data(await _client.put(
      '$_base/ocr',
      body: ocr,
      useAuthCookie: true,
    ));
  }

  Future<Map<String, dynamic>> uploadFace(String filePath) async {
    return _data(await _client.postMultipart(
      '$_base/face',
      filePath: filePath,
      useAuthCookie: true,
    ));
  }

  Future<Map<String, dynamic>> securityChallenge() async {
    return _data(await _client.get(
      '$_base/security-card/challenge',
      useAuthCookie: true,
    ));
  }

  Future<Map<String, dynamic>> verifySecurityCard({
    required String frontAnswer,
    required String backAnswer,
  }) async {
    return _data(await _client.post(
      '$_base/security-card/verify',
      body: {
        'frontAnswer': frontAnswer,
        'backAnswer': backAnswer,
      },
      useAuthCookie: true,
    ));
  }

  Future<Map<String, dynamic>> agreeAccountTerms() async {
    return _data(await _client.post(
      '$_base/consent/account',
      body: {'agreed': true},
      useAuthCookie: true,
    ));
  }

  Future<Map<String, dynamic>> savePassword(String password) async {
    return _data(await _client.post(
      '$_base/password',
      body: {'password': password},
      useAuthCookie: true,
    ));
  }

  Future<Map<String, dynamic>> savePurpose(String purpose) async {
    return _data(await _client.post(
      '$_base/purpose',
      body: {'accountPurpose': purpose},
      useAuthCookie: true,
    ));
  }

  Future<Map<String, dynamic>> open() async {
    return _data(await _client.post(_base, useAuthCookie: true));
  }

  Future<void> cancel() async {
    await _client.delete(_base, useAuthCookie: true);
  }

  Map<String, dynamic> _data(Map<String, dynamic> response) {
    if (response['success'] != true) {
      throw Exception(response['message'] ?? '요청 처리에 실패했습니다.');
    }
    final data = response['data'];
    return data is Map<String, dynamic> ? data : <String, dynamic>{};
  }
}
