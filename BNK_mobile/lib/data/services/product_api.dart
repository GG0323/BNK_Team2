import '../../core/constants/api_constants.dart';
import '../models/product_model.dart';
import 'api_client.dart';

class ProductApi {
  final ApiClient _apiClient = ApiClient.instance;

  Future<List<ProductModel>> getProducts() async {
    final response = await _apiClient.get(
      ApiConstants.products,
      useAuthCookie: true,
    );

    final data = response['data'];

    if (data is List) {
      return data
          .whereType<Map<String, dynamic>>()
          .map(ProductModel.fromJson)
          .toList();
    }

    if (data is Map<String, dynamic>) {
      final list = data['products'] ??
          data['productList'] ??
          data['items'] ??
          data['list'];

      if (list is List) {
        return list
            .whereType<Map<String, dynamic>>()
            .map(ProductModel.fromJson)
            .toList();
      }
    }

    return [];
  }

  Future<ProductModel> getProductDetailByNo(int productNo) async {
    final response = await _apiClient.get(
      '${ApiConstants.productDetail}?product_no=$productNo',
      useAuthCookie: true,
    );

    final data = response['data'];

    if (data is Map<String, dynamic>) {
      final productData = data['product'];

      if (productData is Map<String, dynamic>) {
        return ProductModel.fromJson(productData);
      }

      return ProductModel.fromJson(data);
    }

    throw Exception('상품 상세 정보를 불러오지 못했습니다.');
  }
}