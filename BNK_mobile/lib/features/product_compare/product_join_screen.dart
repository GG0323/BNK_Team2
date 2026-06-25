import 'package:flutter/material.dart';

import '../../core/constants/app_colors.dart';
import '../../data/models/product_join_status_model.dart';
import '../../data/models/product_model.dart';
import '../../data/services/api_client.dart';
import '../../data/services/product_join_api.dart';
import '../account_opening/account_opening_screen.dart';

class ProductJoinScreen extends StatefulWidget {
  final ProductModel? product;
  final ProductModel? resumeProduct;
  final bool accountOpeningOnly;

  const ProductJoinScreen({
    super.key,
    required this.product,
  })  : resumeProduct = null,
        accountOpeningOnly = false;

  const ProductJoinScreen.accountOpening({
    super.key,
    this.resumeProduct,
  })  : product = null,
        accountOpeningOnly = true;

  @override
  State<ProductJoinScreen> createState() => _ProductJoinScreenState();
}

class _ProductJoinScreenState extends State<ProductJoinScreen> {
  final ProductJoinApi _api = ProductJoinApi();
  final TextEditingController _amountController = TextEditingController();
  final TextEditingController _monthsController = TextEditingController();

  ProductJoinStatusModel? _status;
  bool _requiredTermsAgreed = false;
  bool _optionalTermsAgreed = false;
  bool _loading = true;
  String _accountPurpose = 'ETC';

  static const List<Map<String, String>> _purposes = [
    {'label': '급여 수령', 'code': 'SALARY'},
    {'label': '아르바이트 급여', 'code': 'PART_TIME_SALARY'},
    {'label': '연금 수령', 'code': 'PENSION'},
    {'label': '사업용 계좌', 'code': 'BUSINESS'},
    {'label': '모임 계좌', 'code': 'GROUP'},
    {'label': '공과금 납부', 'code': 'UTILITY_PAYMENT'},
    {'label': '생활비 관리', 'code': 'LIVING_EXPENSE'},
    {'label': '기타', 'code': 'ETC'},
  ];

  @override
  void initState() {
    super.initState();
    if (widget.accountOpeningOnly) {
      _loading = false;
    } else {
      _start();
    }
  }

  @override
  void dispose() {
    _amountController.dispose();
    _monthsController.dispose();
    super.dispose();
  }

  Future<void> _start() async {
    final product = widget.product;
    if (product == null) return;

    await _run(() async {
      _applyStatus(await _api.start(product.productNo));
    });
  }

  Future<void> _saveTerms() async {
    final status = _status;
    final subscriptionNo = status?.subscriptionNo;

    if (status == null || subscriptionNo == null) return;

    final amount = int.tryParse(_amountController.text.trim());
    final months = int.tryParse(_monthsController.text.trim());

    if (amount == null || months == null) {
      _showMessage('가입 금액과 기간을 숫자로 입력해 주세요.');
      return;
    }

    if (!_requiredTermsAgreed) {
      _showMessage('필수 약관에 동의해 주세요.');
      return;
    }

    await _run(() async {
      _applyStatus(await _api.saveTerms(
        subscriptionNo: subscriptionNo,
        subscriptionAmount: amount,
        subscriptionMonths: months,
        requiredTermsAgreed: _requiredTermsAgreed,
        optionalTermsAgreed: _optionalTermsAgreed,
      ));
    });
  }

  Future<void> _complete() async {
    final subscriptionNo = _status?.subscriptionNo;
    if (subscriptionNo == null) return;

    await _run(() async {
      _applyStatus(await _api.complete(
        subscriptionNo: subscriptionNo,
        accountPurpose: _accountPurpose,
      ));
    });
  }

  Future<void> _openDemandDepositAccount() async {
    await _run(() async {
      final response = await ApiClient.instance.post(
        '/api/member/accounts/open',
        body: {'accountPurpose': _accountPurpose},
        useAuthCookie: true,
      );

      if (response['success'] != true) {
        throw Exception(response['message'] ?? '계좌 개설에 실패했습니다.');
      }

      if (!mounted) return;

      final resumeProduct = widget.resumeProduct;
      if (resumeProduct != null) {
        Navigator.of(context).pushReplacement(
          MaterialPageRoute(
            builder: (_) => ProductJoinScreen(product: resumeProduct),
          ),
        );
      } else {
        Navigator.of(context).pop(true);
      }
    });
  }

  Future<void> _cancelAccountOpening() async {
    await _run(() async {
      final response = await ApiClient.instance.delete(
        '/api/member/accounts/open',
        useAuthCookie: true,
      );

      if (response['success'] != true) {
        throw Exception(response['message'] ?? '계좌 개설 취소에 실패했습니다.');
      }

      if (mounted) {
        Navigator.of(context).pop(false);
      }
    });
  }

  Future<void> _run(Future<void> Function() action) async {
    if (mounted) setState(() => _loading = true);
    try {
      await action();
    } catch (error) {
      _showMessage(error.toString().replaceFirst('Exception: ', ''));
    } finally {
      if (mounted) setState(() => _loading = false);
    }
  }

  void _applyStatus(ProductJoinStatusModel status) {
    _status = status;
    _requiredTermsAgreed = status.requiredTermsDone;
    _optionalTermsAgreed = status.optionalTermsDone;

    _amountController.text = (status.subscriptionAmount ??
            (status.minJoinAmount > 0 ? status.minJoinAmount : 0))
        .toString();
    _monthsController.text = (status.subscriptionMonths ??
            (status.minTermMonths > 0 ? status.minTermMonths : 1))
        .toString();
  }

  void _showMessage(String message) {
    if (!mounted) return;
    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(content: Text(message), behavior: SnackBarBehavior.floating),
    );
  }

  Widget _buildHeader() {
    final product = widget.product;
    final title = widget.accountOpeningOnly
        ? '입출금 계좌 개설하기'
        : product?.productName ?? '상품 가입';
    final message = widget.accountOpeningOnly
        ? '상품 가입 전 입출금 계좌를 먼저 개설합니다.'
        : _status?.message ?? '상품 가입 정보를 준비 중입니다.';

    return Container(
      width: double.infinity,
      padding: const EdgeInsets.all(20),
      decoration: BoxDecoration(
        color: AppColors.cardBackground,
        borderRadius: BorderRadius.circular(16),
        border: Border.all(color: AppColors.border),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(
            title,
            style: const TextStyle(
              fontSize: 21,
              fontWeight: FontWeight.w900,
              color: AppColors.textPrimary,
            ),
          ),
          const SizedBox(height: 10),
          Text(
            message,
            style: const TextStyle(
              fontSize: 14,
              height: 1.45,
              fontWeight: FontWeight.w700,
              color: AppColors.textSecondary,
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildAccountRequired() {
    return _buildSection(
      title: '입출금 계좌 필요',
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          const Text(
            '상품 가입 전 입출금 계좌 개설이 필요합니다.',
            style: TextStyle(
              fontSize: 14,
              height: 1.5,
              fontWeight: FontWeight.w700,
              color: AppColors.textSecondary,
            ),
          ),
          const SizedBox(height: 14),
          _primaryButton(
            label: '입출금 계좌 개설하기',
            onPressed: () async {
              await Navigator.of(context).push(
                MaterialPageRoute(
                  builder: (_) => const AccountOpeningScreen(),
                ),
              );

              if (mounted) {
                _start();
              }
            },
          ),
        ],
      ),
    );
  }

  Widget _buildAccountOpeningForm() {
    return Column(
      children: [
        _buildHeader(),
        _buildSection(
          title: '계좌 사용 목적',
          child: Column(
            children: [
              _purposeDropdown(enabled: true),
              const SizedBox(height: 14),
              _primaryButton(
                label: '입출금 계좌 개설하기',
                onPressed: _openDemandDepositAccount,
              ),
              const SizedBox(height: 10),
              SizedBox(
                width: double.infinity,
                height: 46,
                child: OutlinedButton(
                  onPressed: _loading ? null : _cancelAccountOpening,
                  child: const Text('취소'),
                ),
              ),
            ],
          ),
        ),
      ],
    );
  }

  Widget _buildForm(ProductJoinStatusModel status) {
    final amountGuide = status.maxJoinAmount > 0
        ? '${_formatMoney(status.minJoinAmount)} ~ ${_formatMoney(status.maxJoinAmount)}'
        : '${_formatMoney(status.minJoinAmount)} 이상';

    return Column(
      children: [
        _buildSection(
          title: '가입 조건',
          child: Column(
            children: [
              _numberField(
                controller: _amountController,
                label: '가입 금액',
                helperText: amountGuide,
              ),
              const SizedBox(height: 12),
              _numberField(
                controller: _monthsController,
                label: '가입 기간',
                helperText: '${status.minTermMonths} ~ ${status.maxTermMonths}',
              ),
            ],
          ),
        ),
        _buildSection(
          title: '약관 동의',
          child: Column(
            children: [
              CheckboxListTile(
                value: _requiredTermsAgreed,
                onChanged: status.complete
                    ? null
                    : (value) => setState(
                          () => _requiredTermsAgreed = value ?? false,
                        ),
                title: const Text('필수 약관에 동의합니다'),
                controlAffinity: ListTileControlAffinity.leading,
                contentPadding: EdgeInsets.zero,
              ),
              CheckboxListTile(
                value: _optionalTermsAgreed,
                onChanged: status.complete
                    ? null
                    : (value) => setState(
                          () => _optionalTermsAgreed = value ?? false,
                        ),
                title: const Text('선택 약관에 동의합니다'),
                controlAffinity: ListTileControlAffinity.leading,
                contentPadding: EdgeInsets.zero,
              ),
              _primaryButton(
                label: '가입 조건 저장',
                onPressed: status.complete ? null : _saveTerms,
              ),
            ],
          ),
        ),
        _buildSection(
          title: '상품 계좌 사용 목적',
          child: Column(
            children: [
              _purposeDropdown(enabled: !status.complete),
              const SizedBox(height: 14),
              _primaryButton(
                label: status.complete ? '가입 완료' : '상품 가입 완료',
                onPressed: status.readyToComplete ? _complete : null,
              ),
            ],
          ),
        ),
      ],
    );
  }

  Widget _purposeDropdown({required bool enabled}) {
    return DropdownButtonFormField<String>(
      initialValue: _accountPurpose,
      items: _purposes
          .map(
            (purpose) => DropdownMenuItem(
              value: purpose['code'],
              child: Text(purpose['label']!),
            ),
          )
          .toList(),
      onChanged: enabled
          ? (value) => setState(() => _accountPurpose = value ?? 'ETC')
          : null,
      decoration: _inputDecoration('사용 목적'),
    );
  }

  Widget _buildSection({required String title, required Widget child}) {
    return Container(
      width: double.infinity,
      margin: const EdgeInsets.only(top: 14),
      padding: const EdgeInsets.all(18),
      decoration: BoxDecoration(
        color: AppColors.cardBackground,
        borderRadius: BorderRadius.circular(14),
        border: Border.all(color: AppColors.border),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(
            title,
            style: const TextStyle(
              fontSize: 17,
              fontWeight: FontWeight.w900,
              color: AppColors.textPrimary,
            ),
          ),
          const SizedBox(height: 14),
          child,
        ],
      ),
    );
  }

  Widget _numberField({
    required TextEditingController controller,
    required String label,
    required String helperText,
  }) {
    return TextField(
      controller: controller,
      keyboardType: TextInputType.number,
      decoration: _inputDecoration(label).copyWith(helperText: helperText),
    );
  }

  InputDecoration _inputDecoration(String label) {
    return InputDecoration(
      labelText: label,
      filled: true,
      fillColor: AppColors.inputBackground,
      border: OutlineInputBorder(
        borderRadius: BorderRadius.circular(12),
        borderSide: const BorderSide(color: AppColors.border),
      ),
    );
  }

  Widget _primaryButton({
    required String label,
    required VoidCallback? onPressed,
  }) {
    return SizedBox(
      width: double.infinity,
      height: 48,
      child: ElevatedButton(
        onPressed: _loading ? null : onPressed,
        style: ElevatedButton.styleFrom(
          backgroundColor: AppColors.primaryRed,
          foregroundColor: AppColors.white,
          elevation: 0,
          shape: RoundedRectangleBorder(
            borderRadius: BorderRadius.circular(12),
          ),
        ),
        child: Text(label, style: const TextStyle(fontWeight: FontWeight.w900)),
      ),
    );
  }

  String _formatMoney(int value) {
    return value.toString().replaceAllMapped(
          RegExp(r'\B(?=(\d{3})+(?!\d))'),
          (_) => ',',
        );
  }

  @override
  Widget build(BuildContext context) {
    final status = _status;

    return Scaffold(
      backgroundColor: AppColors.background,
      appBar: AppBar(
        backgroundColor: AppColors.background,
        elevation: 0,
        foregroundColor: AppColors.textPrimary,
        title: Text(
          widget.accountOpeningOnly ? '계좌 개설' : '상품 가입',
          style: const TextStyle(fontWeight: FontWeight.w900),
        ),
      ),
      body: Stack(
        children: [
          ListView(
            padding: const EdgeInsets.fromLTRB(18, 14, 18, 24),
            children: [
              if (widget.accountOpeningOnly)
                _buildAccountOpeningForm()
              else ...[
                _buildHeader(),
                if (status != null && status.accountRequired)
                  _buildAccountRequired()
                else if (status != null)
                  _buildForm(status),
              ],
            ],
          ),
          if (_loading)
            Container(
              color: Colors.black.withValues(alpha: 0.08),
              child: const Center(child: CircularProgressIndicator()),
            ),
        ],
      ),
    );
  }
}
