import '../../core/constants/api_constants.dart';
import '../models/product_ai_recommend_model.dart';
import 'api_client.dart';

class ProductAiApi {
  final ApiClient _apiClient = ApiClient.instance;

  Future<ProductAiRecommendResponse> recommendProducts({
    required ProductAiRecommendRequest request,
  }) async {
    final response = await _apiClient.post(
      ApiConstants.productAiRecommend,
      body: request.toJson(),

      /*
       * 현재 AI 추천 API는 사용자가 직접 조건을 입력해서 추천받는 구조라
       * 1차 앱 이식에서는 토큰 없이 호출하는 방식으로 둔다.
       *
       * 나중에 로그인 회원의 나이/잔고/계좌정보를 자동 반영하려면
       * useToken: true 로 바꾸고 Spring에서 인증 사용자 정보를 활용하면 됨.
       */
      useToken: false,
    );

    return ProductAiRecommendResponse.fromJson(response);
  }
}