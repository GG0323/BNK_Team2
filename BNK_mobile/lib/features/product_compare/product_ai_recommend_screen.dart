import 'package:flutter/material.dart';

import '../../core/constants/app_colors.dart';
import '../../data/models/product_ai_recommend_model.dart';
import '../../data/models/product_model.dart';
import '../../data/services/product_ai_api.dart';
import 'product_detail_screen.dart';

class ProductAiRecommendScreen extends StatefulWidget {
  final List<ProductModel> products;

  const ProductAiRecommendScreen({
    super.key,
    required this.products,
  });

  @override
  State<ProductAiRecommendScreen> createState() =>
      _ProductAiRecommendScreenState();
}

class _ProductAiRecommendScreenState extends State<ProductAiRecommendScreen> {
  final ProductAiApi _productAiApi = ProductAiApi();

  final TextEditingController _ageController = TextEditingController(text: '24');
  final TextEditingController _balanceController =
  TextEditingController(text: '3000000');
  final TextEditingController _monthlyAmountController =
  TextEditingController(text: '300000');
  final TextEditingController _periodController =
  TextEditingController(text: '12');

  String _purpose = 'MAKE_MONEY';
  String _preferredProductType = 'SAVINGS';
  String _preferredChannel = 'MOBILE';

  final Set<String> _interestConditions = {
    'HIGH_RATE',
    'MOBILE',
    'PREFERENTIAL_RATE',
  };

  bool _loading = false;
  ProductAiRecommendResponse? _response;
  String? _errorMessage;

  @override
  void dispose() {
    _ageController.dispose();
    _balanceController.dispose();
    _monthlyAmountController.dispose();
    _periodController.dispose();
    super.dispose();
  }

  int _toInt(String value) {
    final cleaned = value.replaceAll(',', '').trim();
    return int.tryParse(cleaned) ?? 0;
  }

  String _formatMoney(int amount) {
    return amount.toString().replaceAllMapped(
      RegExp(r'\B(?=(\d{3})+(?!\d))'),
          (match) => ',',
    );
  }

  String _formatRate(double rate) {
    if (rate == 0) return '-';
    return rate.toStringAsFixed(2);
  }

  Future<void> _recommend() async {
    FocusScope.of(context).unfocus();

    final age = _toInt(_ageController.text);
    final balance = _toInt(_balanceController.text);
    final monthlyAmount = _toInt(_monthlyAmountController.text);
    final periodMonths = _toInt(_periodController.text);

    if (age <= 0) {
      _showSnackBar('나이를 입력해주세요.');
      return;
    }

    if (balance <= 0 && monthlyAmount <= 0) {
      _showSnackBar('현재 사용 가능 금액 또는 월 납입 가능 금액을 입력해주세요.');
      return;
    }

    if (periodMonths <= 0) {
      _showSnackBar('희망 가입 기간을 입력해주세요.');
      return;
    }

    setState(() {
      _loading = true;
      _errorMessage = null;
      _response = null;
    });

    try {
      final request = ProductAiRecommendRequest(
        age: age,
        balance: balance,
        monthlyAmount: monthlyAmount,
        periodMonths: periodMonths,
        purpose: _purpose,
        preferredProductType: _preferredProductType,
        preferredChannel: _preferredChannel,
        interestConditions: _interestConditions.toList(),
      );

      final result = await _productAiApi.recommendProducts(request: request);

      if (!mounted) return;

      setState(() {
        _response = result;
      });
    } catch (error) {
      if (!mounted) return;

      setState(() {
        _errorMessage = error.toString().replaceFirst('Exception: ', '');
      });
    } finally {
      if (!mounted) return;

      setState(() {
        _loading = false;
      });
    }
  }

  void _goToDetail(ProductAiRecommendItem item) {
    final matchedProduct = _findProduct(item.productNo);

    final product = matchedProduct ??
        ProductModel(
          productNo: item.productNo,
          productName: item.productName,
          productType: item.productType,
          productStatus: '',
          minInterestRate: item.minInterestRate,
          maxInterestRate: item.maxInterestRate,
          mobileJoinYn: item.mobileJoinYn,
          branchJoinYn: item.branchJoinYn,
          content: item.subtitle.isEmpty ? item.reason : item.subtitle,
        );

    Navigator.push(
      context,
      MaterialPageRoute(
        builder: (_) => ProductDetailScreen(product: product),
      ),
    );
  }

  ProductModel? _findProduct(int productNo) {
    for (final product in widget.products) {
      if (product.productNo == productNo) {
        return product;
      }
    }

    return null;
  }

  void _showSnackBar(String message) {
    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(
        content: Text(message),
        behavior: SnackBarBehavior.floating,
      ),
    );
  }

  Widget _buildHero() {
    return Container(
      width: double.infinity,
      padding: const EdgeInsets.fromLTRB(22, 22, 22, 24),
      decoration: BoxDecoration(
        color: AppColors.textPrimary,
        borderRadius: BorderRadius.circular(28),
        boxShadow: [
          BoxShadow(
            color: Colors.black.withOpacity(0.08),
            blurRadius: 20,
            offset: const Offset(0, 10),
          ),
        ],
      ),
      child: const Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(
            'AI PERSONAL RECOMMEND',
            style: TextStyle(
              fontSize: 11,
              fontWeight: FontWeight.w900,
              color: AppColors.primaryRed,
              letterSpacing: 0.5,
            ),
          ),
          SizedBox(height: 12),
          Text(
            'AI 맞춤 상품 추천',
            style: TextStyle(
              fontSize: 25,
              fontWeight: FontWeight.w900,
              color: AppColors.white,
            ),
          ),
          SizedBox(height: 8),
          Text(
            '가입 목적, 금액, 선호 채널을 기준으로\n예·적금 상품을 개인화 추천합니다.',
            style: TextStyle(
              fontSize: 13,
              height: 1.5,
              fontWeight: FontWeight.w600,
              color: Color(0xFFDDE3EC),
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildFormCard() {
    return Container(
      width: double.infinity,
      padding: const EdgeInsets.all(20),
      decoration: BoxDecoration(
        color: AppColors.cardBackground,
        borderRadius: BorderRadius.circular(26),
        border: Border.all(color: AppColors.border),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          const Text(
            '추천 조건 입력',
            style: TextStyle(
              fontSize: 18,
              fontWeight: FontWeight.w900,
              color: AppColors.textPrimary,
            ),
          ),
          const SizedBox(height: 16),
          _buildInputField(
            label: '나이',
            controller: _ageController,
            suffix: '세',
          ),
          const SizedBox(height: 12),
          _buildInputField(
            label: '현재 사용 가능 금액',
            controller: _balanceController,
            suffix: '원',
          ),
          const SizedBox(height: 12),
          _buildInputField(
            label: '월 납입 가능 금액',
            controller: _monthlyAmountController,
            suffix: '원',
          ),
          const SizedBox(height: 12),
          _buildInputField(
            label: '희망 가입 기간',
            controller: _periodController,
            suffix: '개월',
          ),
          const SizedBox(height: 18),
          _buildDropdown(
            label: '가입 목적',
            value: _purpose,
            items: const {
              'MAKE_MONEY': '목돈 만들기',
              'ROLL_MONEY': '목돈 굴리기',
              'HIGH_RATE': '고금리 우선',
              'EMERGENCY': '비상금 마련',
            },
            onChanged: (value) {
              if (value == null) return;
              setState(() {
                _purpose = value;
              });
            },
          ),
          const SizedBox(height: 12),
          _buildDropdown(
            label: '선호 상품 유형',
            value: _preferredProductType,
            items: const {
              'ALL': '전체',
              'DEPOSIT': '예금',
              'SAVINGS': '적금',
            },
            onChanged: (value) {
              if (value == null) return;
              setState(() {
                _preferredProductType = value;
              });
            },
          ),
          const SizedBox(height: 12),
          _buildDropdown(
            label: '선호 가입 방식',
            value: _preferredChannel,
            items: const {
              'ANY': '상관없음',
              'MOBILE': '모바일',
              'INTERNET': '인터넷',
              'BRANCH': '영업점',
            },
            onChanged: (value) {
              if (value == null) return;
              setState(() {
                _preferredChannel = value;
              });
            },
          ),
          const SizedBox(height: 18),
          const Text(
            '관심 조건',
            style: TextStyle(
              fontSize: 14,
              fontWeight: FontWeight.w900,
              color: AppColors.textPrimary,
            ),
          ),
          const SizedBox(height: 10),
          _buildConditionWrap(),
          const SizedBox(height: 22),
          SizedBox(
            width: double.infinity,
            height: 52,
            child: ElevatedButton(
              onPressed: _loading ? null : _recommend,
              style: ElevatedButton.styleFrom(
                backgroundColor: AppColors.primaryRed,
                foregroundColor: AppColors.white,
                disabledBackgroundColor: AppColors.border,
                shape: RoundedRectangleBorder(
                  borderRadius: BorderRadius.circular(16),
                ),
              ),
              child: _loading
                  ? const SizedBox(
                width: 22,
                height: 22,
                child: CircularProgressIndicator(
                  strokeWidth: 2.4,
                  color: AppColors.white,
                ),
              )
                  : const Text(
                'AI 추천받기',
                style: TextStyle(
                  fontSize: 16,
                  fontWeight: FontWeight.w900,
                ),
              ),
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildInputField({
    required String label,
    required TextEditingController controller,
    required String suffix,
  }) {
    return TextField(
      controller: controller,
      keyboardType: TextInputType.number,
      style: const TextStyle(
        fontSize: 15,
        fontWeight: FontWeight.w800,
        color: AppColors.textPrimary,
      ),
      decoration: InputDecoration(
        labelText: label,
        suffixText: suffix,
        filled: true,
        fillColor: const Color(0xFFF7F8FA),
        labelStyle: const TextStyle(
          color: AppColors.textSecondary,
          fontWeight: FontWeight.w700,
        ),
        suffixStyle: const TextStyle(
          color: AppColors.textSecondary,
          fontWeight: FontWeight.w800,
        ),
        contentPadding:
        const EdgeInsets.symmetric(horizontal: 16, vertical: 15),
        enabledBorder: OutlineInputBorder(
          borderRadius: BorderRadius.circular(16),
          borderSide: const BorderSide(color: AppColors.border),
        ),
        focusedBorder: OutlineInputBorder(
          borderRadius: BorderRadius.circular(16),
          borderSide: const BorderSide(
            color: AppColors.primaryRed,
            width: 1.4,
          ),
        ),
      ),
    );
  }

  Widget _buildDropdown({
    required String label,
    required String value,
    required Map<String, String> items,
    required ValueChanged<String?> onChanged,
  }) {
    return DropdownButtonFormField<String>(
      value: value,
      decoration: InputDecoration(
        labelText: label,
        filled: true,
        fillColor: const Color(0xFFF7F8FA),
        labelStyle: const TextStyle(
          color: AppColors.textSecondary,
          fontWeight: FontWeight.w700,
        ),
        contentPadding:
        const EdgeInsets.symmetric(horizontal: 16, vertical: 15),
        enabledBorder: OutlineInputBorder(
          borderRadius: BorderRadius.circular(16),
          borderSide: const BorderSide(color: AppColors.border),
        ),
        focusedBorder: OutlineInputBorder(
          borderRadius: BorderRadius.circular(16),
          borderSide: const BorderSide(
            color: AppColors.primaryRed,
            width: 1.4,
          ),
        ),
      ),
      items: items.entries
          .map(
            (entry) => DropdownMenuItem<String>(
          value: entry.key,
          child: Text(
            entry.value,
            style: const TextStyle(
              fontWeight: FontWeight.w800,
              color: AppColors.textPrimary,
            ),
          ),
        ),
      )
          .toList(),
      onChanged: onChanged,
    );
  }

  Widget _buildConditionWrap() {
    return Wrap(
      spacing: 8,
      runSpacing: 8,
      children: [
        _buildConditionChip('HIGH_RATE', '고금리'),
        _buildConditionChip('MOBILE', '모바일'),
        _buildConditionChip('PREFERENTIAL_RATE', '우대금리'),
        _buildConditionChip('LOW_AMOUNT', '소액가입'),
        _buildConditionChip('BRANCH', '영업점상담'),
      ],
    );
  }

  Widget _buildConditionChip(String value, String label) {
    final selected = _interestConditions.contains(value);

    return InkWell(
      borderRadius: BorderRadius.circular(999),
      onTap: () {
        setState(() {
          if (selected) {
            _interestConditions.remove(value);
          } else {
            _interestConditions.add(value);
          }
        });
      },
      child: Container(
        padding: const EdgeInsets.symmetric(horizontal: 13, vertical: 9),
        decoration: BoxDecoration(
          color: selected ? const Color(0xFFFFF0F0) : const Color(0xFFF7F8FA),
          borderRadius: BorderRadius.circular(999),
          border: Border.all(
            color: selected ? AppColors.primaryRed : AppColors.border,
          ),
        ),
        child: Text(
          label,
          style: TextStyle(
            fontSize: 13,
            fontWeight: FontWeight.w900,
            color: selected ? AppColors.primaryRed : AppColors.textSecondary,
          ),
        ),
      ),
    );
  }

  Widget _buildErrorCard() {
    if (_errorMessage == null) {
      return const SizedBox.shrink();
    }

    return Container(
      width: double.infinity,
      margin: const EdgeInsets.only(top: 18),
      padding: const EdgeInsets.all(18),
      decoration: BoxDecoration(
        color: const Color(0xFFFFF8F8),
        borderRadius: BorderRadius.circular(22),
        border: Border.all(color: const Color(0xFFFFC9C9)),
      ),
      child: Row(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          const Icon(
            Icons.error_outline_rounded,
            color: AppColors.primaryRed,
            size: 24,
          ),
          const SizedBox(width: 12),
          Expanded(
            child: Text(
              _errorMessage!,
              style: const TextStyle(
                fontSize: 13,
                height: 1.5,
                fontWeight: FontWeight.w700,
                color: AppColors.textPrimary,
              ),
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildResultSection() {
    final response = _response;

    if (response == null) {
      return const SizedBox.shrink();
    }

    return Container(
      width: double.infinity,
      margin: const EdgeInsets.only(top: 18),
      padding: const EdgeInsets.all(18),
      decoration: BoxDecoration(
        color: AppColors.cardBackground,
        borderRadius: BorderRadius.circular(26),
        border: Border.all(color: const Color(0xFFFFD6D6)),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          const Text(
            'AI 추천 결과',
            style: TextStyle(
              fontSize: 18,
              fontWeight: FontWeight.w900,
              color: AppColors.textPrimary,
            ),
          ),
          if (response.summary.isNotEmpty) ...[
            const SizedBox(height: 10),
            Text(
              response.summary,
              style: const TextStyle(
                fontSize: 14,
                height: 1.55,
                fontWeight: FontWeight.w700,
                color: AppColors.textPrimary,
              ),
            ),
          ],
          const SizedBox(height: 16),
          if (response.recommendedProducts.isEmpty)
            const Text(
              '추천 결과가 없습니다. 조건을 변경해서 다시 시도해주세요.',
              style: TextStyle(
                fontSize: 13,
                color: AppColors.textSecondary,
                fontWeight: FontWeight.w700,
              ),
            )
          else
            ...response.recommendedProducts.asMap().entries.map(
                  (entry) => _buildRecommendCard(
                rank: entry.key + 1,
                item: entry.value,
              ),
            ),
        ],
      ),
    );
  }

  Widget _buildRecommendCard({
    required int rank,
    required ProductAiRecommendItem item,
  }) {
    return Container(
      width: double.infinity,
      margin: const EdgeInsets.only(bottom: 14),
      padding: const EdgeInsets.all(18),
      decoration: BoxDecoration(
        color: const Color(0xFFFCFCFD),
        borderRadius: BorderRadius.circular(22),
        border: Border.all(color: AppColors.border),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            children: [
              Container(
                padding:
                const EdgeInsets.symmetric(horizontal: 10, vertical: 6),
                decoration: BoxDecoration(
                  color: AppColors.textPrimary,
                  borderRadius: BorderRadius.circular(999),
                ),
                child: Text(
                  '$rank위',
                  style: const TextStyle(
                    fontSize: 12,
                    fontWeight: FontWeight.w900,
                    color: AppColors.white,
                  ),
                ),
              ),
              const Spacer(),
              Column(
                crossAxisAlignment: CrossAxisAlignment.end,
                children: [
                  Text(
                    '적합도 ${item.fitPercent}%',
                    style: const TextStyle(
                      fontSize: 12,
                      fontWeight: FontWeight.w900,
                      color: AppColors.primaryRed,
                    ),
                  ),
                  const SizedBox(height: 2),
                  Text(
                    '우대 가능성 ${item.benefitChancePercent}%',
                    style: const TextStyle(
                      fontSize: 11,
                      fontWeight: FontWeight.w800,
                      color: AppColors.textSecondary,
                    ),
                  ),
                ],
              ),
            ],
          ),
          const SizedBox(height: 14),
          Text(
            item.productName,
            style: const TextStyle(
              fontSize: 18,
              height: 1.3,
              fontWeight: FontWeight.w900,
              color: AppColors.textPrimary,
            ),
          ),
          const SizedBox(height: 8),
          Text(
            '${item.productTypeText} · ${item.joinMethodText}',
            style: const TextStyle(
              fontSize: 13,
              fontWeight: FontWeight.w800,
              color: AppColors.textSecondary,
            ),
          ),
          const SizedBox(height: 14),
          Row(
            children: [
              Expanded(
                child: _buildSmallInfoBox(
                  title: '최저금리',
                  value: '${_formatRate(item.minInterestRate)}%',
                ),
              ),
              const SizedBox(width: 10),
              Expanded(
                child: _buildSmallInfoBox(
                  title: '최고금리',
                  value: '${_formatRate(item.maxInterestRate)}%',
                  highlight: true,
                ),
              ),
            ],
          ),
          const SizedBox(height: 14),
          Text(
            item.reason.isEmpty ? '추천 이유를 불러오지 못했습니다.' : item.reason,
            style: const TextStyle(
              fontSize: 13,
              height: 1.55,
              fontWeight: FontWeight.w600,
              color: AppColors.textPrimary,
            ),
          ),
          if (item.evidence.isNotEmpty) ...[
            const SizedBox(height: 12),
            Wrap(
              spacing: 6,
              runSpacing: 6,
              children: item.evidence
                  .map(
                    (tag) => Container(
                  padding: const EdgeInsets.symmetric(
                    horizontal: 9,
                    vertical: 6,
                  ),
                  decoration: BoxDecoration(
                    color: const Color(0xFFF3F4F6),
                    borderRadius: BorderRadius.circular(999),
                  ),
                  child: Text(
                    tag,
                    style: const TextStyle(
                      fontSize: 11,
                      fontWeight: FontWeight.w800,
                      color: AppColors.textSecondary,
                    ),
                  ),
                ),
              )
                  .toList(),
            ),
          ],
          const SizedBox(height: 16),
          SizedBox(
            width: double.infinity,
            height: 46,
            child: ElevatedButton(
              onPressed: () => _goToDetail(item),
              style: ElevatedButton.styleFrom(
                backgroundColor: AppColors.primaryRed,
                foregroundColor: AppColors.white,
                shape: RoundedRectangleBorder(
                  borderRadius: BorderRadius.circular(14),
                ),
              ),
              child: const Text(
                '상세보기',
                style: TextStyle(
                  fontSize: 14,
                  fontWeight: FontWeight.w900,
                ),
              ),
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
      padding: const EdgeInsets.fromLTRB(14, 12, 14, 12),
      decoration: BoxDecoration(
        color: const Color(0xFFF7F8FA),
        borderRadius: BorderRadius.circular(16),
        border: Border.all(color: AppColors.border),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(
            title,
            style: const TextStyle(
              fontSize: 12,
              fontWeight: FontWeight.w700,
              color: AppColors.textSecondary,
            ),
          ),
          const SizedBox(height: 6),
          Text(
            value,
            style: TextStyle(
              fontSize: 18,
              fontWeight: FontWeight.w900,
              color: highlight ? AppColors.primaryRed : AppColors.textPrimary,
            ),
          ),
        ],
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
          'AI 상품 추천',
          style: TextStyle(fontWeight: FontWeight.w900),
        ),
      ),
      body: SafeArea(
        child: SingleChildScrollView(
          padding: const EdgeInsets.fromLTRB(22, 18, 22, 28),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              _buildHero(),
              const SizedBox(height: 18),
              _buildFormCard(),
              _buildErrorCard(),
              _buildResultSection(),
            ],
          ),
        ),
      ),
    );
  }
}