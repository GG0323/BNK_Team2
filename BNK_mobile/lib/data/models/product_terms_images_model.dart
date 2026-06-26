class ProductTermsImagesModel {
  final int productNo;
  final List<ProductTermsFileModel> terms;

  ProductTermsImagesModel({
    required this.productNo,
    required this.terms,
  });

  factory ProductTermsImagesModel.fromJson(Map<String, dynamic> json) {
    final rawTerms = json['terms'];
    return ProductTermsImagesModel(
      productNo: _toInt(json['productNo']),
      terms: rawTerms is List
          ? rawTerms
              .map((item) =>
                  ProductTermsFileModel.fromJson(Map<String, dynamic>.from(item)))
              .toList()
          : <ProductTermsFileModel>[],
    );
  }

  static int _toInt(dynamic value) {
    if (value is int) return value;
    if (value is double) return value.toInt();
    return int.tryParse((value ?? '0').toString()) ?? 0;
  }
}

class ProductTermsFileModel {
  final String termsTitle;
  final String fileName;
  final int pageCount;
  final List<ProductTermsPageModel> pages;

  ProductTermsFileModel({
    required this.termsTitle,
    required this.fileName,
    required this.pageCount,
    required this.pages,
  });

  factory ProductTermsFileModel.fromJson(Map<String, dynamic> json) {
    final rawPages = json['pages'];
    return ProductTermsFileModel(
      termsTitle: (json['termsTitle'] ?? '').toString(),
      fileName: (json['fileName'] ?? '').toString(),
      pageCount: ProductTermsImagesModel._toInt(json['pageCount']),
      pages: rawPages is List
          ? rawPages
              .map((item) =>
                  ProductTermsPageModel.fromJson(Map<String, dynamic>.from(item)))
              .toList()
          : <ProductTermsPageModel>[],
    );
  }
}

class ProductTermsPageModel {
  final int pageNo;
  final String imageBase64;

  ProductTermsPageModel({
    required this.pageNo,
    required this.imageBase64,
  });

  factory ProductTermsPageModel.fromJson(Map<String, dynamic> json) {
    return ProductTermsPageModel(
      pageNo: ProductTermsImagesModel._toInt(json['pageNo']),
      imageBase64: (json['imageBase64'] ?? '').toString(),
    );
  }
}
