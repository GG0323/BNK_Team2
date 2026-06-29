import '../../core/constants/api_constants.dart';
import '../models/product_model.dart';
import 'api_client.dart';

class ProductApi {
  final ApiClient _apiClient = ApiClient.instance;

  Future<List<ProductModel>> getProducts() async {
    final response = await _apiClient.get(
      ApiConstants.products,
      useAuthCookie: false,
    );

    final data = response['data'];

    if (data is List) {
      return data
          .map((item) => ProductModel.fromJson(item as Map<String, dynamic>))
          .toList();
    }

    if (data is Map<String, dynamic>) {
      final list = data['products'] ??
          data['productList'] ??
          data['items'] ??
          data['list'];

      if (list is List) {
        return list
            .map((item) => ProductModel.fromJson(item as Map<String, dynamic>))
            .toList();
      }
    }

    return [];
  }

  Future<ProductModel?> getProductByNo(int productNo) async {
    final products = await getProducts();

    for (final product in products) {
      if (product.productNo == productNo) {
        return product;
      }
    }

    return null;
  }
}