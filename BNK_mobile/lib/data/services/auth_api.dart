import 'package:flutter/foundation.dart';

import '../../core/constants/api_constants.dart';
import '../../core/storage/secure_storage.dart';
import '../models/member_model.dart';
import 'api_client.dart';

class AuthApi {
  final ApiClient _apiClient = ApiClient.instance;

  Future<MemberModel> login({
    required String loginId,
    required String password,
  }) async {
    debugPrint('[AUTH LOGIN] POST ${ApiConstants.baseUrl}${ApiConstants.appLogin}');

    final response = await _apiClient.postForm(
      ApiConstants.appLogin,
      body: {
        'username': loginId,
        'password': password,
      },
    );

    final bool success = response['result'] == 'success';

    if (!success) {
      throw Exception(response['message'] ?? '로그인에 실패했습니다.');
    }

    return getMe();
  }

  Future<void> ping() async {
    debugPrint('[AUTH PING] GET ${ApiConstants.baseUrl}${ApiConstants.appPing}');
    await _apiClient.get(
      ApiConstants.appPing,
      useAuthCookie: true,
    );
  }

  Future<MemberModel> getMe() async {
    final response = await _apiClient.get(
      ApiConstants.appMe,
      useAuthCookie: true,
    );

    final bool success = response['success'] == true;

    if (!success) {
      throw Exception(response['message'] ?? '회원정보 조회에 실패했습니다.');
    }

    return MemberModel.fromJson(response['data']);
  }

  Future<void> logout() async {
    await SecureStorage.deleteAuthCookie();
  }
}
