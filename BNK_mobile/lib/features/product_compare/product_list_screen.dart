import 'package:flutter/material.dart';

import '../../core/constants/app_colors.dart';
import '../../data/models/product_model.dart';
import '../../data/services/product_api.dart';
import 'product_ai_recommend_screen.dart';
import 'product_compare_screen.dart';
import 'product_detail_screen.dart';
import 'widgets/compare_bottom_panel.dart';
import 'widgets/product_card.dart';

class ProductListScreen extends StatefulWidget {
  const ProductListScreen({super.key});

  @override
  State<ProductListScreen> createState() => _ProductListScreenState();
}

class _ProductListScreenState extends State<ProductListScreen> {
  final ProductApi _productApi = ProductApi();

  late Future<List<ProductModel>> _productsFuture;

  final List<ProductModel> _selectedProducts = [];

  String _selectedType = 'ALL';

  @override
  void initState() {
    super.initState();
    _productsFuture = _productApi.getProducts();
  }

  void _goToDetail(ProductModel product) {
    Navigator.push(
      context,
      MaterialPageRoute(
        builder: (_) => ProductDetailScreen(product: product),
      ),
    );
  }

  void _goToAiRecommend(List<ProductModel> products) {
    Navigator.push(
      context,
      MaterialPageRoute(
        builder: (_) => ProductAiRecommendScreen(
          products: products,
        ),
      ),
    );
  }

  void _toggleProduct(ProductModel product) {
    final alreadySelected = _selectedProducts.any(
          (item) => item.productNo == product.productNo,
    );

    if (alreadySelected) {
      setState(() {
        _selectedProducts.removeWhere(
              (item) => item.productNo == product.productNo,
        );
      });
      return;
    }

    if (_selectedProducts.length >= 3) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(
          content: Text('상품 비교는 최대 3개까지 가능합니다.'),
          behavior: SnackBarBehavior.floating,
        ),
      );
      return;
    }

    setState(() {
      _selectedProducts.add(product);
    });
  }

  void _clearSelectedProducts() {
    setState(() {
      _selectedProducts.clear();
    });
  }

  void _goToCompare() {
    if (_selectedProducts.length < 2) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(
          content: Text('비교하려면 상품을 2개 이상 선택해주세요.'),
          behavior: SnackBarBehavior.floating,
        ),
      );
      return;
    }

    Navigator.push(
      context,
      MaterialPageRoute(
        builder: (_) => ProductCompareScreen(
          products: List<ProductModel>.from(_selectedProducts),
        ),
      ),
    );
  }

  List<ProductModel> _filterProducts(List<ProductModel> products) {
    if (_selectedType == 'DEPOSIT') {
      return products.where((p) => p.productType == 'DEPOSIT').toList();
    }

    if (_selectedType == 'SAVINGS') {
      return products.where((p) => p.productType == 'SAVINGS').toList();
    }

    return products;
  }

  Widget _buildHeader() {
    return Container(
      width: double.infinity,
      padding: const EdgeInsets.fromLTRB(22, 22, 22, 24),
      decoration: BoxDecoration(
        color: AppColors.cardBackground,
        borderRadius: BorderRadius.circular(28),
      ),
      child: const Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(
            'BNK',
            style: TextStyle(
              fontSize: 32,
              fontWeight: FontWeight.w900,
              color: AppColors.primaryRed,
            ),
          ),
          SizedBox(height: 16),
          Text(
            '예적금 상품',
            style: TextStyle(
              fontSize: 25,
              fontWeight: FontWeight.w900,
              color: AppColors.textPrimary,
            ),
          ),
          SizedBox(height: 8),
          Text(
            '상품 상세를 확인하고 최대 3개까지 비교할 수 있습니다.',
            style: TextStyle(
              fontSize: 14,
              color: AppColors.textSecondary,
              fontWeight: FontWeight.w600,
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildAiRecommendCard(List<ProductModel> products) {
    return InkWell(
      borderRadius: BorderRadius.circular(26),
      onTap: () => _goToAiRecommend(products),
      child: Container(
        width: double.infinity,
        padding: const EdgeInsets.fromLTRB(20, 20, 20, 20),
        decoration: BoxDecoration(
          color: const Color(0xFF111827),
          borderRadius: BorderRadius.circular(26),
          boxShadow: [
            BoxShadow(
              color: Colors.black.withOpacity(0.10),
              blurRadius: 18,
              offset: const Offset(0, 8),
            ),
          ],
        ),
        child: Row(
          children: [
            Container(
              width: 48,
              height: 48,
              decoration: BoxDecoration(
                color: AppColors.primaryRed,
                borderRadius: BorderRadius.circular(18),
              ),
              child: const Icon(
                Icons.auto_awesome_rounded,
                color: AppColors.white,
                size: 25,
              ),
            ),
            const SizedBox(width: 16),
            const Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    'AI 맞춤 상품 추천',
                    style: TextStyle(
                      fontSize: 18,
                      fontWeight: FontWeight.w900,
                      color: AppColors.white,
                    ),
                  ),
                  SizedBox(height: 6),
                  Text(
                    '가입 목적과 조건을 입력하면 나에게 맞는 예·적금 상품을 추천합니다.',
                    style: TextStyle(
                      fontSize: 13,
                      height: 1.45,
                      fontWeight: FontWeight.w600,
                      color: Color(0xFFD1D5DB),
                    ),
                  ),
                ],
              ),
            ),
            const SizedBox(width: 12),
            const Icon(
              Icons.arrow_forward_ios_rounded,
              size: 18,
              color: AppColors.white,
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildTypeFilters() {
    return Row(
      children: [
        _buildFilterChip(label: '전체', value: 'ALL'),
        const SizedBox(width: 8),
        _buildFilterChip(label: '예금', value: 'DEPOSIT'),
        const SizedBox(width: 8),
        _buildFilterChip(label: '적금', value: 'SAVINGS'),
      ],
    );
  }

  Widget _buildFilterChip({
    required String label,
    required String value,
  }) {
    final selected = _selectedType == value;

    return InkWell(
      borderRadius: BorderRadius.circular(999),
      onTap: () {
        setState(() {
          _selectedType = value;
        });
      },
      child: Container(
        padding: const EdgeInsets.symmetric(horizontal: 18, vertical: 10),
        decoration: BoxDecoration(
          color: selected ? AppColors.primaryRed : AppColors.cardBackground,
          borderRadius: BorderRadius.circular(999),
          border: Border.all(
            color: selected ? AppColors.primaryRed : AppColors.border,
          ),
        ),
        child: Text(
          label,
          style: TextStyle(
            fontSize: 14,
            fontWeight: FontWeight.w800,
            color: selected ? AppColors.white : AppColors.textSecondary,
          ),
        ),
      ),
    );
  }

  Widget _buildList(List<ProductModel> products) {
    final filteredProducts = _filterProducts(products);

    return SingleChildScrollView(
      child: Padding(
        padding: const EdgeInsets.symmetric(horizontal: 22, vertical: 20),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            _buildHeader(),
            const SizedBox(height: 18),
            _buildAiRecommendCard(products),
            const SizedBox(height: 18),
            _buildTypeFilters(),
            const SizedBox(height: 18),
            Text(
              '총 ${filteredProducts.length}개 상품',
              style: const TextStyle(
                fontSize: 16,
                fontWeight: FontWeight.w900,
                color: AppColors.textPrimary,
              ),
            ),
            const SizedBox(height: 14),
            ...filteredProducts.map((product) {
              final selected = _selectedProducts.any(
                    (item) => item.productNo == product.productNo,
              );

              return ProductCard(
                product: product,
                selected: selected,
                onDetailTap: () => _goToDetail(product),
                onCompareTap: () => _toggleProduct(product),
              );
            }),
            const SizedBox(height: 90),
          ],
        ),
      ),
    );
  }

  Widget _buildEmptyView() {
    return const Center(
      child: Text(
        '조회 가능한 상품이 없습니다.',
        style: TextStyle(
          fontSize: 15,
          color: AppColors.textSecondary,
          fontWeight: FontWeight.w700,
        ),
      ),
    );
  }

  Widget _buildErrorView(Object error) {
    return Center(
      child: Padding(
        padding: const EdgeInsets.all(24),
        child: Container(
          width: double.infinity,
          padding: const EdgeInsets.all(22),
          decoration: BoxDecoration(
            color: AppColors.cardBackground,
            borderRadius: BorderRadius.circular(24),
            border: Border.all(color: AppColors.border),
          ),
          child: Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              const Icon(
                Icons.error_outline,
                color: AppColors.primaryRed,
                size: 42,
              ),
              const SizedBox(height: 14),
              const Text(
                '상품 목록을 불러오지 못했습니다.',
                style: TextStyle(
                  fontSize: 17,
                  fontWeight: FontWeight.w900,
                  color: AppColors.textPrimary,
                ),
              ),
              const SizedBox(height: 10),
              Text(
                error.toString().replaceFirst('Exception: ', ''),
                textAlign: TextAlign.center,
                style: const TextStyle(
                  fontSize: 13,
                  height: 1.5,
                  color: AppColors.textSecondary,
                ),
              ),
              const SizedBox(height: 18),
              ElevatedButton(
                onPressed: () {
                  setState(() {
                    _productsFuture = _productApi.getProducts();
                  });
                },
                child: const Text('다시 시도'),
              ),
            ],
          ),
        ),
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: AppColors.background,
      appBar: AppBar(
        backgroundColor: AppColors.background,
        elevation: 0,
        foregroundColor: AppColors.textPrimary,
        title: const Text(
          '상품',
          style: TextStyle(fontWeight: FontWeight.w900),
        ),
      ),
      body: FutureBuilder<List<ProductModel>>(
        future: _productsFuture,
        builder: (context, snapshot) {
          if (snapshot.connectionState == ConnectionState.waiting) {
            return const Center(
              child: CircularProgressIndicator(color: AppColors.primaryRed),
            );
          }

          if (snapshot.hasError) {
            return _buildErrorView(snapshot.error!);
          }

          final products = snapshot.data ?? [];

          if (products.isEmpty) {
            return _buildEmptyView();
          }

          return _buildList(products);
        },
      ),
      bottomNavigationBar: CompareBottomPanel(
        selectedProducts: _selectedProducts,
        onCompareTap: _goToCompare,
        onClearTap: _clearSelectedProducts,
      ),
    );
  }
}