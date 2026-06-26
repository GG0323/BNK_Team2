import '../../core/constants/api_constants.dart';
import '../models/account_model.dart';
import '../models/product_join_entry_status_model.dart';
import '../models/product_join_status_model.dart';
import '../models/product_terms_images_model.dart';
import 'api_client.dart';

class ProductJoinApi {
  final ApiClient _apiClient = ApiClient.instance;

  Future<ProductJoinEntryStatusModel> entryStatus(int productNo) async {
    final response = await _apiClient.get(
      '${ApiConstants.productJoinEntryStatus}?product_no=$productNo',
      useAuthCookie: true,
    );

    return ProductJoinEntryStatusModel.fromJson(
      Map<String, dynamic>.from(response['data'] ?? {}),
    );
  }

  Future<ProductJoinStatusModel> start(int productNo) async {
    final response = await _apiClient.post(
      ApiConstants.productJoinStart,
      body: {'productNo': productNo},
      useAuthCookie: true,
    );
    return _statusFrom(response);
  }

  Future<ProductTermsImagesModel> termsPdf(int productNo) async {
    final response = await _apiClient.get(
      '${ApiConstants.productJoinTermsPdf}?product_no=$productNo',
      useAuthCookie: true,
    );

    return ProductTermsImagesModel.fromJson(
      Map<String, dynamic>.from(response['data'] ?? {}),
    );
  }

  Future<List<AccountModel>> withdrawalAccounts() async {
    final response = await _apiClient.get(
      ApiConstants.productJoinWithdrawalAccounts,
      useAuthCookie: true,
    );

    final data = response['data'];

    if (data is List) {
      return data
          .map((item) => AccountModel.fromJson(Map<String, dynamic>.from(item)))
          .toList();
    }

    return [];
  }

  Future<ProductJoinStatusModel> agreeTerms({
    required int productNo,
    required bool requiredTermsAgreed,
    required bool optionalTermsAgreed,
  }) async {
    final response = await _apiClient.post(
      ApiConstants.productJoinTerms,
      body: {
        'productNo': productNo,
        'requiredTermsAgreed': requiredTermsAgreed,
        'optionalTermsAgreed': optionalTermsAgreed,
      },
      useAuthCookie: true,
    );
    return _statusFrom(response);
  }

  Future<ProductJoinStatusModel> confirmContract({
    required int subscriptionNo,
    required int linkedAccountNo,
    required int subscriptionAmount,
    required int subscriptionMonths,
  }) async {
    final response = await _apiClient.post(
      ApiConstants.productJoinContractConfirm,
      body: {
        'subscriptionNo': subscriptionNo,
        'linkedAccountNo': linkedAccountNo,
        'subscriptionAmount': subscriptionAmount,
        'subscriptionMonths': subscriptionMonths,
      },
      useAuthCookie: true,
    );
    return _statusFrom(response);
  }

  Future<Map<String, dynamic>> securityChallenge() async {
    final response = await _apiClient.get(
      ApiConstants.productJoinSecurityChallenge,
      useAuthCookie: true,
    );
    return Map<String, dynamic>.from(response['data'] ?? {});
  }

  Future<ProductJoinStatusModel> complete({
    required int subscriptionNo,
    required String accountPassword,
    required int frontIndex,
    required int backIndex,
    required String frontAnswer,
    required String backAnswer,
  }) async {
    final response = await _apiClient.post(
      ApiConstants.productJoinComplete,
      body: {
        'subscriptionNo': subscriptionNo,
        'accountPassword': accountPassword,
        'frontIndex': frontIndex,
        'backIndex': backIndex,
        'frontAnswer': frontAnswer,
        'backAnswer': backAnswer,
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
