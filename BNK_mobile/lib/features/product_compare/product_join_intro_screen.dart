import 'package:flutter/material.dart';

import '../../core/constants/app_colors.dart';
import '../../data/models/product_model.dart';
import '../../data/services/product_api.dart';
import 'product_join_terms_screen.dart';
import 'product_list_screen.dart';

class ProductJoinIntroScreen extends StatefulWidget {
  final int productNo;
  final bool enteredFromQr;

  const ProductJoinIntroScreen({
    super.key,
    required this.productNo,
    this.enteredFromQr = true,
  });

  @override
  State<ProductJoinIntroScreen> createState() => _ProductJoinIntroScreenState();
}

class _ProductJoinIntroScreenState extends State<ProductJoinIntroScreen> {
  final ProductApi _productApi = ProductApi();

  bool _isLoading = true;
  ProductModel? _product;
  Object? _error;

  @override
  void initState() {
    super.initState();
    _loadProduct();
  }

  Future<void> _loadProduct() async {
    setState(() {
      _isLoading = true;
      _error = null;
    });

    try {
      final product = await _productApi.getProductDetailByNo(widget.productNo);

      if (!mounted) return;

      setState(() {
        _product = product;
        _isLoading = false;
      });
    } catch (error) {
      if (!mounted) return;

      setState(() {
        _error = error;
        _isLoading = false;
      });
    }
  }

  String _formatRate(double rate) {
    if (rate == 0) return '-';
    return rate.toStringAsFixed(2);
  }

  void _goToProductList() {
    Navigator.pushReplacement(
      context,
      MaterialPageRoute(
        builder: (_) => const ProductListScreen(),
      ),
    );
  }

  void _goToTerms() {
    final product = _product;

    if (product == null) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(
          content: Text('상품 정보를 불러온 후 다시 시도해주세요.'),
          behavior: SnackBarBehavior.floating,
        ),
      );
      return;
    }

    Navigator.push(
      context,
      MaterialPageRoute(
        builder: (_) => ProductJoinTermsScreen(
          product: product,
        ),
      ),
    );
  }

  Widget _buildHeader() {
    return Container(
      width: double.infinity,
      padding: const EdgeInsets.fromLTRB(22, 24, 22, 24),
      decoration: BoxDecoration(
        color: const Color(0xFF111827),
        borderRadius: BorderRadius.circular(28),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          const Text(
            'BNK',
            style: TextStyle(
              fontSize: 32,
              fontWeight: FontWeight.w900,
              color: AppColors.primaryRed,
            ),
          ),
          const SizedBox(height: 18),
          Text(
            widget.enteredFromQr ? 'QR 상품 가입 안내' : '상품 가입 안내',
            style: const TextStyle(
              fontSize: 24,
              fontWeight: FontWeight.w900,
              color: AppColors.white,
            ),
          ),
          const SizedBox(height: 10),
          Text(
            widget.enteredFromQr
                ? 'QR로 선택한 상품의 가입 안내를 이어서 진행합니다.'
                : '선택하신 상품의 가입 안내를 확인해주세요.',
            style: const TextStyle(
              fontSize: 14,
              height: 1.5,
              fontWeight: FontWeight.w700,
              color: Color(0xFFD1D5DB),
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildProductCard(ProductModel product) {
    final isMobile = product.mobileJoinYn == 'Y';

    return Container(
      width: double.infinity,
      padding: const EdgeInsets.all(22),
      decoration: BoxDecoration(
        color: AppColors.cardBackground,
        borderRadius: BorderRadius.circular(26),
        border: Border.all(color: AppColors.border),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Container(
            padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 6),
            decoration: BoxDecoration(
              color: const Color(0xFFFFF0F0),
              borderRadius: BorderRadius.circular(999),
            ),
            child: Text(
              product.productTypeText,
              style: const TextStyle(
                fontSize: 12,
                fontWeight: FontWeight.w900,
                color: AppColors.primaryRed,
              ),
            ),
          ),
          const SizedBox(height: 14),
          Text(
            product.productName,
            style: const TextStyle(
              fontSize: 22,
              height: 1.35,
              fontWeight: FontWeight.w900,
              color: AppColors.textPrimary,
            ),
          ),
          const SizedBox(height: 12),
          Text(
            product.content.isEmpty ? '상품 설명을 확인해보세요.' : product.content,
            style: const TextStyle(
              fontSize: 14,
              height: 1.55,
              fontWeight: FontWeight.w600,
              color: AppColors.textSecondary,
            ),
          ),
          const SizedBox(height: 20),
          Row(
            children: [
              Expanded(
                child: _buildSmallInfoBox(
                  title: '최저금리',
                  value: '${_formatRate(product.minInterestRate)}%',
                ),
              ),
              const SizedBox(width: 12),
              Expanded(
                child: _buildSmallInfoBox(
                  title: '최고금리',
                  value: '${_formatRate(product.maxInterestRate)}%',
                  highlight: true,
                ),
              ),
            ],
          ),
          const SizedBox(height: 18),
          Container(
            width: double.infinity,
            padding: const EdgeInsets.all(16),
            decoration: BoxDecoration(
              color: isMobile ? const Color(0xFFFFF8F8) : const Color(0xFFF8FAFC),
              borderRadius: BorderRadius.circular(18),
              border: Border.all(
                color: isMobile ? AppColors.primaryRed : AppColors.border,
              ),
            ),
            child: Row(
              children: [
                Icon(
                  isMobile ? Icons.phone_android_rounded : Icons.info_outline_rounded,
                  color: isMobile ? AppColors.primaryRed : AppColors.textSecondary,
                ),
                const SizedBox(width: 12),
                Expanded(
                  child: Text(
                    isMobile
                        ? '모바일 앱에서 가입 신청을 진행할 수 있는 상품입니다.'
                        : '이 상품은 모바일 가입 가능 여부를 확인한 뒤 진행해주세요.',
                    style: TextStyle(
                      fontSize: 13,
                      height: 1.5,
                      fontWeight: FontWeight.w700,
                      color: isMobile ? AppColors.primaryRed : AppColors.textSecondary,
                    ),
                  ),
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildSmallInfoBox({
    required String title,
    required String value,
    bool highlight = false,
  }) {
    return Container(
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        color: const Color(0xFFF8FAFC),
        borderRadius: BorderRadius.circular(18),
        border: Border.all(color: AppColors.border),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(
            title,
            style: const TextStyle(
              fontSize: 12,
              fontWeight: FontWeight.w800,
              color: AppColors.textSecondary,
            ),
          ),
          const SizedBox(height: 8),
          Text(
            value,
            style: TextStyle(
              fontSize: 20,
              fontWeight: FontWeight.w900,
              color: highlight ? AppColors.primaryRed : AppColors.textPrimary,
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildLoadingCard() {
    return const Padding(
      padding: EdgeInsets.symmetric(vertical: 44),
      child: Center(
        child: CircularProgressIndicator(
          color: AppColors.primaryRed,
        ),
      ),
    );
  }

  Widget _buildErrorCard() {
    final errorMessage = _error?.toString().replaceFirst('Exception: ', '') ??
        '상품 정보를 불러오지 못했습니다.';

    return Container(
      width: double.infinity,
      padding: const EdgeInsets.all(22),
      decoration: BoxDecoration(
        color: AppColors.cardBackground,
        borderRadius: BorderRadius.circular(26),
        border: Border.all(color: AppColors.border),
      ),
      child: Column(
        children: [
          const Icon(
            Icons.warning_amber_rounded,
            color: AppColors.primaryRed,
            size: 42,
          ),
          const SizedBox(height: 14),
          const Text(
            '상품 정보를 불러오지 못했습니다.',
            style: TextStyle(
              fontSize: 17,
              fontWeight: FontWeight.w900,
              color: AppColors.textPrimary,
            ),
          ),
          const SizedBox(height: 8),
          const Text(
            '선택하신 상품 정보를 불러오지 못했습니다.\n잠시 후 다시 시도해주세요.',
            textAlign: TextAlign.center,
            style: TextStyle(
              fontSize: 13,
              height: 1.5,
              fontWeight: FontWeight.w600,
              color: AppColors.textSecondary,
            ),
          ),
          const SizedBox(height: 12),
          Text(
            errorMessage,
            textAlign: TextAlign.center,
            style: const TextStyle(
              fontSize: 12,
              height: 1.4,
              color: AppColors.textSecondary,
            ),
          ),
          const SizedBox(height: 18),
          SizedBox(
            width: double.infinity,
            height: 48,
            child: OutlinedButton(
              onPressed: _loadProduct,
              style: OutlinedButton.styleFrom(
                foregroundColor: AppColors.primaryRed,
                side: const BorderSide(color: AppColors.primaryRed),
                shape: RoundedRectangleBorder(
                  borderRadius: BorderRadius.circular(15),
                ),
              ),
              child: const Text(
                '다시 불러오기',
                style: TextStyle(
                  fontWeight: FontWeight.w900,
                ),
              ),
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildGuideCard() {
    return Container(
      width: double.infinity,
      padding: const EdgeInsets.all(20),
      decoration: BoxDecoration(
        color: AppColors.cardBackground,
        borderRadius: BorderRadius.circular(24),
        border: Border.all(color: AppColors.border),
      ),
      child: const Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(
            '가입 진행 안내',
            style: TextStyle(
              fontSize: 18,
              fontWeight: FontWeight.w900,
              color: AppColors.textPrimary,
            ),
          ),
          SizedBox(height: 12),
          Text(
            '상품 내용과 금리를 확인한 뒤 약관 동의, 출금계좌 선택, 가입정보 입력 순서로 가입 신청을 진행합니다.',
            style: TextStyle(
              fontSize: 14,
              height: 1.6,
              fontWeight: FontWeight.w600,
              color: AppColors.textSecondary,
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildBottomButton() {
    return Container(
      padding: const EdgeInsets.fromLTRB(22, 12, 22, 18),
      decoration: BoxDecoration(
        color: AppColors.background,
        border: Border(
          top: BorderSide(color: AppColors.border.withOpacity(0.7)),
        ),
      ),
      child: Row(
        children: [
          Expanded(
            child: SizedBox(
              height: 52,
              child: OutlinedButton(
                onPressed: _goToProductList,
                style: OutlinedButton.styleFrom(
                  foregroundColor: AppColors.textPrimary,
                  side: const BorderSide(color: AppColors.border),
                  shape: RoundedRectangleBorder(
                    borderRadius: BorderRadius.circular(16),
                  ),
                ),
                child: const Text(
                  '상품목록',
                  style: TextStyle(
                    fontWeight: FontWeight.w900,
                  ),
                ),
              ),
            ),
          ),
          const SizedBox(width: 10),
          Expanded(
            child: SizedBox(
              height: 52,
              child: ElevatedButton(
                onPressed: _product == null ? null : _goToTerms,
                style: ElevatedButton.styleFrom(
                  backgroundColor: AppColors.primaryRed,
                  foregroundColor: AppColors.white,
                  disabledBackgroundColor: const Color(0xFFE5E7EB),
                  disabledForegroundColor: AppColors.textSecondary,
                  elevation: 0,
                  shape: RoundedRectangleBorder(
                    borderRadius: BorderRadius.circular(16),
                  ),
                ),
                child: const Text(
                  '가입 진행하기',
                  style: TextStyle(
                    fontWeight: FontWeight.w900,
                  ),
                ),
              ),
            ),
          ),
        ],
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    Widget content;

    if (_isLoading) {
      content = _buildLoadingCard();
    } else if (_product != null) {
      content = _buildProductCard(_product!);
    } else {
      content = _buildErrorCard();
    }

    return Scaffold(
      backgroundColor: AppColors.background,
      appBar: AppBar(
        backgroundColor: AppColors.background,
        elevation: 0,
        foregroundColor: AppColors.textPrimary,
        title: const Text(
          '상품 가입 안내',
          style: TextStyle(
            fontWeight: FontWeight.w900,
          ),
        ),
      ),
      body: SingleChildScrollView(
        padding: const EdgeInsets.fromLTRB(22, 18, 22, 24),
        child: Column(
          children: [
            _buildHeader(),
            const SizedBox(height: 16),
            content,
            const SizedBox(height: 16),
            _buildGuideCard(),
            const SizedBox(height: 90),
          ],
        ),
      ),
      bottomNavigationBar: _buildBottomButton(),
    );
  }
}