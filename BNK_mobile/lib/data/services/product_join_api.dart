import '../../core/constants/api_constants.dart';
import '../models/product_join_status_model.dart';
import 'api_client.dart';

class ProductJoinApi {
  final ApiClient _apiClient = ApiClient.instance;

  Future<ProductJoinStatusModel> start(int productNo) async {
    final response = await _apiClient.post(
      ApiConstants.productJoinStart,
      body: {'productNo': productNo},
      useAuthCookie: true,
    );
    return _statusFrom(response);
  }

  Future<ProductJoinStatusModel> saveTerms({
    required int subscriptionNo,
    required int subscriptionAmount,
    required int subscriptionMonths,
    required bool requiredTermsAgreed,
    required bool optionalTermsAgreed,
  }) async {
    final response = await _apiClient.post(
      ApiConstants.productJoinTerms,
      body: {
        'subscriptionNo': subscriptionNo,
        'subscriptionAmount': subscriptionAmount,
        'subscriptionMonths': subscriptionMonths,
        'requiredTermsAgreed': requiredTermsAgreed,
        'optionalTermsAgreed': optionalTermsAgreed,
      },
      useAuthCookie: true,
    );
    return _statusFrom(response);
  }

  Future<ProductJoinStatusModel> complete({
    required int subscriptionNo,
    String? accountPurpose,
  }) async {
    final response = await _apiClient.post(
      ApiConstants.productJoinComplete,
      body: {
        'subscriptionNo': subscriptionNo,
        'accountPurpose': accountPurpose,
      },
      useAuthCookie: true,
    );
    return _statusFrom(response);
  }

  ProductJoinStatusModel _statusFrom(Map<String, dynamic> response) {
    return ProductJoinStatusModel.fromJson(
      Map<String, dynamic>.from(response['data'] ?? {}),
    );
  }
}
