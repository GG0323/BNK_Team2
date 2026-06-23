import '../../core/constants/api_constants.dart';
import '../models/mypage_model.dart';
import 'api_client.dart';

class MemberApi {
  final ApiClient _apiClient = ApiClient.instance;

  Future<MyPageModel> getMyPage() async {
    final response = await _apiClient.get(
      ApiConstants.memberMypage,
      useAuthCookie: true,
    );

    final bool success = response['success'] == true;

    if (!success) {
      throw Exception(response['message'] ?? '마이페이지 조회에 실패했습니다.');
    }

    return MyPageModel.fromJson(response['data']);
  }
}
