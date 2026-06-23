import '../../core/constants/api_constants.dart';
import '../models/account_model.dart';
import 'api_client.dart';

class AccountApi {
  final ApiClient _apiClient = ApiClient.instance;

  Future<List<AccountModel>> getMyAccounts() async {
    final response = await _apiClient.get(
      ApiConstants.memberAccounts,
      useAuthCookie: true,
    );

    final bool success = response['success'] == true;

    if (!success) {
      throw Exception(response['message'] ?? '계좌 목록 조회에 실패했습니다.');
    }

    final data = response['data'];

    if (data is List) {
      return data
          .map((item) => AccountModel.fromJson(item as Map<String, dynamic>))
          .toList();
    }

    if (data is Map<String, dynamic>) {
      final list = data['accounts'] ?? data['accountList'] ?? data['myAccounts'];

      if (list is List) {
        return list
            .map((item) => AccountModel.fromJson(item as Map<String, dynamic>))
            .toList();
      }
    }

    return [];
  }
}
