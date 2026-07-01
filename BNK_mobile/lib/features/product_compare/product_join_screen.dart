import 'dart:convert';

import 'package:flutter/material.dart';
import 'package:flutter/rendering.dart';
import 'package:flutter/services.dart';

import '../../core/constants/app_colors.dart';
import '../../core/events/account_refresh_notifier.dart';
import '../../data/models/account_model.dart';
import '../../data/models/product_join_status_model.dart';
import '../../data/models/product_model.dart';
import '../../data/models/product_terms_images_model.dart';
import '../../data/services/product_join_api.dart';
import '../account/account_list_screen.dart';
import '../account_opening/account_opening_screen.dart';

class ProductJoinScreen extends StatefulWidget {
  final ProductModel product;

  const ProductJoinScreen({super.key, required this.product});

  @override
  State<ProductJoinScreen> createState() => _ProductJoinScreenState();
}

class _ProductJoinScreenState extends State<ProductJoinScreen> {
  final ProductJoinApi _joinApi = ProductJoinApi();
  final TextEditingController _amountController = TextEditingController();
  final TextEditingController _passwordController = TextEditingController();
  final TextEditingController _frontAnswerController = TextEditingController();
  final TextEditingController _backAnswerController = TextEditingController();

  ProductJoinStatusModel? _status;
  ProductTermsImagesModel? _termsImages;
  List<AccountModel> _accounts = [];
  Map<String, dynamic>? _challenge;
  final Set<int> _agreedTermIndexes = {};

  int? _selectedAccountNo;
  int? _selectedMonths;
  bool _loading = true;
  bool _reviewingContract = false;
  bool _securityStep = false;

  @override
  void initState() {
    super.initState();
    _load();
  }

  @override
  void dispose() {
    _amountController.dispose();
    _passwordController.dispose();
    _frontAnswerController.dispose();
    _backAnswerController.dispose();
    super.dispose();
  }

  Future<void> _load() async {
    await _run(() async {
      final status = await _joinApi.start(widget.product.productNo);
      final accounts = await _joinApi.withdrawalAccounts();
      ProductTermsImagesModel? termsImages;

      if (status.currentStep == 'TERMS') {
        termsImages = await _joinApi.termsPdf(widget.product.productNo);
      }

      if (!mounted) return;

      setState(() {
        _accounts = accounts.where(_isWithdrawableAccount).toList();
        _termsImages = termsImages;
        _applyStatus(status);
      });
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

    if (status.subscriptionAmount != null) {
      _amountController.text = status.subscriptionAmount.toString();
    } else {
      _amountController.clear();
    }

    final monthOptions = _monthOptions(status);
    _selectedMonths = monthOptions.contains(status.subscriptionMonths)
        ? status.subscriptionMonths
        : null;

    _selectedAccountNo =
        _accounts.any((account) => account.accountNo == status.linkedAccountId)
        ? status.linkedAccountId
        : null;

    _reviewingContract = status.currentStep == 'CONTRACT_CONFIRM';
    _securityStep = false;
  }

  Future<void> _agreeTerms() async {
    final terms = _termsImages?.terms ?? [];

    if (terms.isEmpty) {
      _showMessage('확인할 약관 PDF가 없습니다.');
      return;
    }

    if (_agreedTermIndexes.length != terms.length) {
      _showMessage('모든 약관을 마지막 페이지까지 확인하고 동의해주세요.');
      return;
    }

    await _run(() async {
      final status = await _joinApi.agreeTerms(
        productNo: widget.product.productNo,
        requiredTermsAgreed: true,
        optionalTermsAgreed: false,
      );

      if (!mounted) return;
      setState(() => _applyStatus(status));
    });
  }

  void _goToContractReview() {
    final validationMessage = _contractInputValidationMessage();

    if (validationMessage != null) {
      _showMessage(validationMessage);
      return;
    }

    setState(() => _reviewingContract = true);
  }

  Future<void> _confirmContract() async {
    final subscriptionNo = _status?.subscriptionNo;
    final amount = _enteredAmount;

    if (subscriptionNo == null ||
        _contractInputValidationMessage() != null ||
        amount == null) {
      _showMessage('계약내용을 확인할 수 없습니다.');
      return;
    }

    await _run(() async {
      final status = await _joinApi.confirmContract(
        subscriptionNo: subscriptionNo,
        linkedAccountNo: _selectedAccountNo!,
        subscriptionAmount: amount,
        subscriptionMonths: _selectedMonths!,
      );
      final challenge = await _joinApi.securityChallenge();

      if (!mounted) return;
      setState(() {
        _applyStatus(status);
        _reviewingContract = false;
        _securityStep = true;
        _challenge = challenge;
      });
    });
  }

  Future<void> _completeJoin() async {
    final subscriptionNo = _status?.subscriptionNo;
    final challenge = _challenge;

    if (subscriptionNo == null || challenge == null) {
      _showMessage('보안카드 인증 정보를 확인할 수 없습니다.');
      return;
    }

    final productAccountPassword = _passwordController.text.trim();

    if (!RegExp(r'^\d{4}$').hasMatch(productAccountPassword)) {
      _showMessage('계좌 비밀번호는 숫자 4자리로 입력해주세요.');
      return;
    }

    if (mounted) setState(() => _loading = true);
    try {
      final status = await _joinApi.complete(
        subscriptionNo: subscriptionNo,
        accountPassword: productAccountPassword,
        frontIndex: _toInt(challenge['frontIndex']),
        backIndex: _toInt(challenge['backIndex']),
        frontAnswer: _frontAnswerController.text.trim(),
        backAnswer: _backAnswerController.text.trim(),
      );

      if (!mounted) return;
      setState(() {
        _applyStatus(status);
        _securityStep = false;
      });

      AccountRefreshNotifier.notifyChanged();
      Navigator.of(context).pop(true);
    } catch (error) {
      _showMessage(error.toString().replaceFirst('Exception: ', ''));
    } finally {
      if (mounted) setState(() => _loading = false);
    }
  }

  void _showMessage(String message) {
    if (!mounted) return;
    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(content: Text(message), behavior: SnackBarBehavior.floating),
    );
  }

  Future<void> _openTermsViewer() async {
    final terms = _termsImages?.terms ?? [];

    if (terms.isEmpty) {
      _showMessage('확인할 약관 PDF가 없습니다.');
      return;
    }

    final result = await Navigator.of(context).push<Set<int>>(
      MaterialPageRoute(
        fullscreenDialog: true,
        builder: (_) => _ProductTermsViewer(
          terms: terms,
          initialAgreedTermIndexes: _agreedTermIndexes,
        ),
      ),
    );

    if (!mounted || result == null) return;

    setState(() {
      _agreedTermIndexes
        ..clear()
        ..addAll(result);
    });
  }

  Widget _buildCurrentStep() {
    final status = _status;

    if (status == null) {
      return const SizedBox.shrink();
    }

    if (status.accountRequired) {
      return _accountRequiredStep();
    }

    if (status.complete) {
      return _completeStep();
    }

    if (status.currentStep == 'EXPIRED') {
      return _messageStep('만기 또는 해지된 가입 이력이 있습니다.');
    }

    if (_securityStep) {
      return _securityStepView();
    }

    if (status.currentStep == 'TERMS') {
      return _termsStep();
    }

    if (_reviewingContract) {
      return _contractReviewStep();
    }

    return _contractInputStep();
  }

  Widget _accountRequiredStep() {
    return _section(
      title: '입출금 계좌 필요',
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          const Text('상품 가입을 위해서는 입출금 계좌 개설이 먼저 필요합니다.'),
          const SizedBox(height: 14),
          _primaryButton(
            label: '계좌 개설하기',
            onPressed: () async {
              await Navigator.of(context).push(
                MaterialPageRoute(builder: (_) => const AccountOpeningScreen()),
              );
              if (mounted) _load();
            },
          ),
        ],
      ),
    );
  }

  Widget _termsStep() {
    final terms = _termsImages?.terms ?? [];

    if (terms.isEmpty) {
      return _section(
        title: '약관 확인',
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            const Text('조회된 약관 PDF가 없습니다. 상품 약관 파일을 확인해주세요.'),
            const SizedBox(height: 14),
            _primaryButton(label: '다시 조회', onPressed: _load),
          ],
        ),
      );
    }

    final allAgreed = _agreedTermIndexes.length == terms.length;

    return Column(
      children: [
        _section(
          title: '약관 목록',
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Text(
                '전체 약관을 순서대로 확인하고 약관별로 동의해주세요.',
                style: const TextStyle(
                  color: AppColors.textSecondary,
                  fontWeight: FontWeight.w700,
                  height: 1.4,
                ),
              ),
              const SizedBox(height: 10),
              ...List.generate(terms.length, (index) {
                final agreed = _agreedTermIndexes.contains(index);
                return _termListTile(
                  title: terms[index].termsTitle,
                  agreed: agreed,
                  onTap: _openTermsViewer,
                );
              }),
              const SizedBox(height: 12),
              Container(
                width: double.infinity,
                padding: const EdgeInsets.all(14),
                decoration: BoxDecoration(
                  color: AppColors.inputBackground,
                  borderRadius: BorderRadius.circular(10),
                  border: Border.all(color: AppColors.border),
                ),
                child: const Text(
                  '고객 확인 사항\n상품설명서와 약관의 주요 내용을 확인하고 이해한 뒤 다음 단계로 진행해주세요.',
                  style: TextStyle(
                    color: AppColors.textSecondary,
                    fontWeight: FontWeight.w700,
                    height: 1.45,
                  ),
                ),
              ),
            ],
          ),
        ),
        _primaryButton(label: '다음', onPressed: allAgreed ? _agreeTerms : null),
      ],
    );
  }

  Widget _termListTile({
    required String title,
    required bool agreed,
    required VoidCallback onTap,
  }) {
    return InkWell(
      onTap: onTap,
      borderRadius: BorderRadius.circular(10),
      child: Padding(
        padding: const EdgeInsets.symmetric(vertical: 7),
        child: Row(
          children: [
            Icon(
              agreed ? Icons.check_circle : Icons.radio_button_unchecked,
              color: agreed ? AppColors.primaryRed : AppColors.textSecondary,
              size: 22,
            ),
            const SizedBox(width: 10),
            Expanded(
              child: Text(
                '$title 동의',
                style: const TextStyle(
                  color: AppColors.textPrimary,
                  fontWeight: FontWeight.w800,
                ),
              ),
            ),
            const Icon(Icons.chevron_right, color: AppColors.textSecondary),
          ],
        ),
      ),
    );
  }

  Widget _contractInputStep() {
    final status = _status!;
    final selectedAccount = _selectedAccount();
    final validationMessage = _contractInputValidationMessage(
      showRequired: false,
    );
    return _section(
      title: '가입조건 입력',
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          DropdownButtonFormField<int>(
            initialValue: _selectedAccountNo,
            hint: const Text('출금계좌를 선택해주세요'),
            items: _accounts
                .map(
                  (account) => DropdownMenuItem(
                    value: account.accountNo,
                    child: Text(
                      '${account.accountNumber} (${_formatMoney(account.balance)}원)',
                    ),
                  ),
                )
                .toList(),
            onChanged: (value) => setState(() => _selectedAccountNo = value),
            decoration: _inputDecoration('출금계좌'),
          ),
          if (selectedAccount != null) ...[
            const SizedBox(height: 8),
            Text(
              '출금가능금액 ${_formatMoney(selectedAccount.balance)}원',
              style: const TextStyle(
                color: AppColors.textSecondary,
                fontWeight: FontWeight.w800,
              ),
            ),
          ],
          const SizedBox(height: 12),
          DropdownButtonFormField<int>(
            initialValue: _selectedMonths,
            hint: const Text('가입기간을 선택해주세요'),
            items: _monthOptions(status)
                .map(
                  (month) =>
                      DropdownMenuItem(value: month, child: Text('$month개월')),
                )
                .toList(),
            onChanged: (value) => setState(() => _selectedMonths = value),
            decoration: _inputDecoration('가입기간'),
          ),
          const SizedBox(height: 12),
          TextField(
            controller: _amountController,
            keyboardType: TextInputType.number,
            onChanged: (_) => setState(() {}),
            decoration: _inputDecoration('가입금액').copyWith(
              hintText: '가입금액을 입력해주세요',
              helperText: _amountGuide(status),
            ),
          ),
          if (validationMessage != null) ...[
            const SizedBox(height: 10),
            Text(
              validationMessage,
              style: const TextStyle(
                color: AppColors.primaryRed,
                fontWeight: FontWeight.w800,
                height: 1.35,
              ),
            ),
          ],
          const SizedBox(height: 16),
          _primaryButton(
            label: '계약내용 확인',
            onPressed: _canReviewContract ? _goToContractReview : null,
          ),
        ],
      ),
    );
  }

  Widget _contractReviewStep() {
    final status = _status!;
    final account = _selectedAccount();
    final amount = _enteredAmount ?? 0;
    final appliedRate =
        status.appliedInterestRate ?? widget.product.minInterestRate;
    final maturityDate = status.maturityDate ?? '계약 확인 후 산출';

    return _section(
      title: '계약내용 확인',
      child: Column(
        children: [
          _infoRow('상품명', status.productName),
          _infoRow('출금계좌번호', account?.accountNumber ?? '-'),
          _infoRow('만기입금계좌번호', account?.accountNumber ?? '-'),
          _infoRow('가입기간', '${_selectedMonths ?? '-'}개월'),
          _infoRow('가입금액', '${_formatMoney(amount)}원'),
          _infoRow('자동이체금액', '${_formatMoney(amount)}원'),
          _infoRow('적용금리', '${appliedRate.toStringAsFixed(2)}%'),
          _infoRow('만기일', maturityDate),
          _infoRow('약관동의', status.requiredTermsDone ? '동의' : '미동의'),
          const SizedBox(height: 16),
          _primaryButton(label: '확인', onPressed: _confirmContract),
          const SizedBox(height: 10),
          SizedBox(
            width: double.infinity,
            height: 46,
            child: OutlinedButton(
              onPressed: () => setState(() => _reviewingContract = false),
              child: const Text('수정하기'),
            ),
          ),
        ],
      ),
    );
  }

  Widget _securityStepView() {
    final challenge = _challenge ?? {};
    final canComplete =
        RegExp(r'^\d{4}$').hasMatch(_passwordController.text) &&
        _frontAnswerController.text.trim().isNotEmpty &&
        _backAnswerController.text.trim().isNotEmpty;

    return _section(
      title: '상품계좌 비밀번호 및 보안카드 인증',
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          TextField(
            controller: _passwordController,
            keyboardType: TextInputType.number,
            obscureText: true,
            maxLength: 4,
            inputFormatters: [
              FilteringTextInputFormatter.digitsOnly,
              LengthLimitingTextInputFormatter(4),
            ],
            onChanged: (_) => setState(() {}),
            decoration: _inputDecoration(
              '상품계좌 비밀번호',
            ).copyWith(helperText: '숫자 4자리를 입력해주세요.', counterText: ''),
          ),
          const SizedBox(height: 12),
          Text(
            (challenge['message'] ?? '').toString(),
            style: const TextStyle(
              color: AppColors.textSecondary,
              fontWeight: FontWeight.w700,
            ),
          ),
          const SizedBox(height: 12),
          TextField(
            controller: _frontAnswerController,
            keyboardType: TextInputType.number,
            inputFormatters: [FilteringTextInputFormatter.digitsOnly],
            onChanged: (_) => setState(() {}),
            decoration: _inputDecoration('${challenge['frontIndex']}번 앞자리'),
          ),
          const SizedBox(height: 12),
          TextField(
            controller: _backAnswerController,
            keyboardType: TextInputType.number,
            inputFormatters: [FilteringTextInputFormatter.digitsOnly],
            onChanged: (_) => setState(() {}),
            decoration: _inputDecoration('${challenge['backIndex']}번 뒷자리'),
          ),
          const SizedBox(height: 16),
          _primaryButton(
            label: '가입 완료',
            onPressed: canComplete ? _completeJoin : null,
          ),
        ],
      ),
    );
  }

  Widget _completeStep() {
    return _section(
      title: '가입 완료',
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          const Text('상품 가입이 완료되었습니다. 약관 PDF는 등록된 이메일로 발송됩니다.'),
          const SizedBox(height: 14),
          _primaryButton(
            label: '보유 계좌 보기',
            onPressed: () {
              Navigator.of(context).pushReplacement(
                MaterialPageRoute(builder: (_) => const AccountListScreen()),
              );
            },
          ),
        ],
      ),
    );
  }

  Widget _messageStep(String message) {
    return _section(title: '안내', child: Text(message));
  }

  Widget _section({required String title, required Widget child}) {
    return Container(
      width: double.infinity,
      margin: const EdgeInsets.only(bottom: 14),
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
              fontSize: 18,
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

  Widget _infoRow(String label, String value) {
    return Padding(
      padding: const EdgeInsets.symmetric(vertical: 7),
      child: Row(
        children: [
          SizedBox(
            width: 112,
            child: Text(
              label,
              style: const TextStyle(
                color: AppColors.textSecondary,
                fontWeight: FontWeight.w800,
              ),
            ),
          ),
          Expanded(
            child: Text(
              value,
              style: const TextStyle(fontWeight: FontWeight.w800),
            ),
          ),
        ],
      ),
    );
  }

  Widget _primaryButton({
    required String label,
    required VoidCallback? onPressed,
  }) {
    return SizedBox(
      width: double.infinity,
      height: 50,
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

  List<int> _monthOptions(ProductJoinStatusModel status) {
    final min = status.minTermMonths <= 0 ? 1 : status.minTermMonths;
    final max = status.maxTermMonths < min ? min : status.maxTermMonths;
    return List<int>.generate(max - min + 1, (index) => min + index);
  }

  AccountModel? _selectedAccount() {
    for (final account in _accounts) {
      if (account.accountNo == _selectedAccountNo) {
        return account;
      }
    }
    return null;
  }

  bool _isWithdrawableAccount(AccountModel account) {
    final purpose = account.accountPurpose?.toUpperCase();
    return account.accountStatus == 'ACTIVE' &&
        purpose != 'DEPOSIT' &&
        purpose != 'SAVINGS';
  }

  bool get _canReviewContract => _contractInputValidationMessage() == null;

  int? get _enteredAmount {
    final digitsOnly = _amountController.text.replaceAll(RegExp(r'[^0-9]'), '');
    return int.tryParse(digitsOnly);
  }

  String? _contractInputValidationMessage({bool showRequired = true}) {
    final status = _status;
    final amount = _enteredAmount;
    final selectedAccount = _selectedAccount();

    if (_selectedAccountNo == null || selectedAccount == null) {
      return showRequired ? '출금계좌를 선택해주세요.' : null;
    }

    if (_selectedMonths == null) {
      return showRequired ? '가입기간을 선택해주세요.' : null;
    }

    if (amount == null || amount <= 0) {
      return showRequired ? '가입금액을 입력해주세요.' : null;
    }

    if (status != null) {
      if (amount < status.minJoinAmount) {
        return '최소 가입금액은 ${_formatMoney(status.minJoinAmount)}원입니다.';
      }

      if (status.maxJoinAmount > 0 && amount > status.maxJoinAmount) {
        return '최대 가입금액은 ${_formatMoney(status.maxJoinAmount)}원입니다.';
      }

      if (status.depositUnit > 0 && amount % status.depositUnit != 0) {
        return '가입금액은 ${_formatMoney(status.depositUnit)}원 단위로 입력해주세요.';
      }
    }

    if (amount > selectedAccount.balance) {
      return '출금 가능 금액이 부족합니다.\n가입금액을 줄이거나 다른 출금계좌를 선택해주세요.';
    }

    return null;
  }

  String _amountGuide(ProductJoinStatusModel status) {
    final min = _formatMoney(status.minJoinAmount);
    if (status.maxJoinAmount > 0) {
      return '$min원 ~ ${_formatMoney(status.maxJoinAmount)}원';
    }
    return '$min원 이상';
  }

  String _formatMoney(int value) {
    return value.toString().replaceAllMapped(
      RegExp(r'\B(?=(\d{3})+(?!\d))'),
      (_) => ',',
    );
  }

  int _toInt(dynamic value) {
    if (value is int) return value;
    if (value is double) return value.toInt();
    return int.tryParse((value ?? '0').toString()) ?? 0;
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
          '상품 가입',
          style: TextStyle(fontWeight: FontWeight.w900),
        ),
      ),
      body: Stack(
        children: [
          ListView(
            padding: const EdgeInsets.fromLTRB(18, 14, 18, 28),
            children: [
              _section(
                title: widget.product.productName,
                child: Text(
                  _status?.message ?? '상품 가입 정보를 확인하고 있습니다.',
                  style: const TextStyle(
                    color: AppColors.textSecondary,
                    fontWeight: FontWeight.w700,
                    height: 1.45,
                  ),
                ),
              ),
              _buildCurrentStep(),
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

class _ProductTermsViewer extends StatefulWidget {
  final List<ProductTermsFileModel> terms;
  final Set<int> initialAgreedTermIndexes;

  const _ProductTermsViewer({
    required this.terms,
    required this.initialAgreedTermIndexes,
  });

  @override
  State<_ProductTermsViewer> createState() => _ProductTermsViewerState();
}

class _ProductTermsViewerState extends State<_ProductTermsViewer> {
  late final ScrollController _scrollController;
  late List<GlobalKey> _currentPageKeys;
  late List<Uint8List> _currentPageBytes;
  late final Set<int> _agreedTermIndexes;

  int _currentTermIndex = 0;
  int _currentVisiblePageIndex = 0;

  bool get _canScrollToNextPage =>
      _currentVisiblePageIndex < _currentPageKeys.length - 1;

  @override
  void initState() {
    super.initState();
    _scrollController = ScrollController()..addListener(_handleScrollChanged);
    _agreedTermIndexes = Set<int>.from(widget.initialAgreedTermIndexes);
    _currentTermIndex = _firstNotAgreedIndex();
    _rebuildCurrentTermPages();
  }

  @override
  void dispose() {
    _scrollController.removeListener(_handleScrollChanged);
    _scrollController.dispose();
    super.dispose();
  }

  int _firstNotAgreedIndex() {
    for (var i = 0; i < widget.terms.length; i++) {
      if (!_agreedTermIndexes.contains(i)) return i;
    }
    return widget.terms.isEmpty ? 0 : widget.terms.length - 1;
  }

  void _rebuildCurrentTermPages() {
    final pages = widget.terms[_currentTermIndex].pages;
    _currentPageKeys = List.generate(pages.length, (_) => GlobalKey());
    _currentPageBytes = pages
        .map((page) => base64Decode(page.imageBase64))
        .toList();
  }

  void _handleScrollChanged() {
    _syncCurrentVisiblePageIndex();
  }

  void _syncCurrentVisiblePageIndex() {
    if (!_scrollController.hasClients) return;

    final position = _scrollController.position;
    if (!position.hasContentDimensions) return;

    final currentOffset = position.pixels + 1;
    var visibleIndex = _currentVisiblePageIndex;

    for (var i = 0; i < _currentPageKeys.length; i++) {
      final context = _currentPageKeys[i].currentContext;
      final renderObject = context?.findRenderObject();
      if (renderObject == null || !renderObject.attached) continue;

      final viewport = RenderAbstractViewport.maybeOf(renderObject);
      if (viewport == null) continue;

      final pageOffset = viewport.getOffsetToReveal(renderObject, 0).offset;
      if (pageOffset <= currentOffset) {
        visibleIndex = i;
      } else {
        break;
      }
    }

    final nextVisibleIndex = visibleIndex
        .clamp(0, _currentPageKeys.isEmpty ? 0 : _currentPageKeys.length - 1)
        .toInt();

    if (nextVisibleIndex != _currentVisiblePageIndex && mounted) {
      setState(() => _currentVisiblePageIndex = nextVisibleIndex);
    }
  }

  Future<void> _scrollToNextPage() async {
    if (!_scrollController.hasClients) {
      WidgetsBinding.instance.addPostFrameCallback((_) {
        if (mounted) _scrollToNextPage();
      });
      return;
    }

    final position = _scrollController.position;
    if (!position.hasContentDimensions) {
      WidgetsBinding.instance.addPostFrameCallback((_) {
        if (mounted) _scrollToNextPage();
      });
      return;
    }

    if (_currentPageKeys.isEmpty) {
      return;
    }

    final maxExtent = position.maxScrollExtent;
    if (maxExtent <= 0) {
      WidgetsBinding.instance.addPostFrameCallback((_) {
        if (!mounted || !_scrollController.hasClients) return;
        _syncCurrentVisiblePageIndex();
      });
      return;
    }

    final current = position.pixels;
    double? nextOffset;
    var nextVisibleIndex = _currentVisiblePageIndex;

    for (var i = 0; i < _currentPageKeys.length; i++) {
      final context = _currentPageKeys[i].currentContext;
      final renderObject = context?.findRenderObject();
      if (renderObject == null || !renderObject.attached) continue;

      final viewport = RenderAbstractViewport.maybeOf(renderObject);
      if (viewport == null) continue;

      final pageOffset = viewport.getOffsetToReveal(renderObject, 0).offset;
      if (pageOffset > current + 8) {
        nextOffset = pageOffset;
        nextVisibleIndex = i;
        break;
      }
    }

    if (nextOffset == null) {
      return;
    }

    final target = nextOffset.clamp(0.0, maxExtent).toDouble();

    if (target <= current + 8) return;

    if (nextVisibleIndex != _currentVisiblePageIndex && mounted) {
      setState(() => _currentVisiblePageIndex = nextVisibleIndex);
    }

    await _scrollController.animateTo(
      target,
      duration: const Duration(milliseconds: 360),
      curve: Curves.easeOutCubic,
    );
  }

  Future<void> _agreeCurrentTerm() async {
    setState(() => _agreedTermIndexes.add(_currentTermIndex));

    if (_currentTermIndex >= widget.terms.length - 1) {
      Navigator.of(context).pop(Set<int>.from(_agreedTermIndexes));
      return;
    }

    setState(() {
      _currentTermIndex += 1;
      _currentVisiblePageIndex = 0;
      _rebuildCurrentTermPages();
    });

    WidgetsBinding.instance.addPostFrameCallback((_) {
      if (!mounted || !_scrollController.hasClients) return;
      _scrollController.jumpTo(0);
    });
  }

  String get _currentProgressText {
    if (widget.terms.isEmpty) return '';
    final title = widget.terms[_currentTermIndex].termsTitle;
    return '$title 확인 중 (${_currentTermIndex + 1}/${widget.terms.length})';
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
          '약관·동의서 보기',
          style: TextStyle(fontWeight: FontWeight.w900),
        ),
        leading: IconButton(
          icon: const Icon(Icons.close),
          onPressed: () =>
              Navigator.of(context).pop(Set<int>.from(_agreedTermIndexes)),
        ),
      ),
      body: Column(
        children: [
          Container(
            width: double.infinity,
            padding: const EdgeInsets.fromLTRB(18, 8, 18, 12),
            child: Text(
              _currentProgressText,
              style: const TextStyle(
                color: AppColors.textSecondary,
                fontWeight: FontWeight.w800,
              ),
            ),
          ),
          Expanded(
            child: Stack(
              children: [
                NotificationListener<ScrollMetricsNotification>(
                  onNotification: (_) {
                    WidgetsBinding.instance.addPostFrameCallback((_) {
                      if (mounted) _syncCurrentVisiblePageIndex();
                    });
                    return false;
                  },
                  child: ListView(
                    controller: _scrollController,
                    padding: const EdgeInsets.fromLTRB(18, 0, 18, 90),
                    children: [_termSection(_currentTermIndex)],
                  ),
                ),
                if (_canScrollToNextPage)
                  Positioned(
                    left: 0,
                    right: 0,
                    bottom: 18,
                    child: Center(
                      child: ElevatedButton(
                        onPressed: _scrollToNextPage,
                        style: ElevatedButton.styleFrom(
                          backgroundColor: const Color(0xFF737B96),
                          foregroundColor: AppColors.white,
                          elevation: 0,
                          padding: const EdgeInsets.symmetric(
                            horizontal: 28,
                            vertical: 13,
                          ),
                          shape: RoundedRectangleBorder(
                            borderRadius: BorderRadius.circular(26),
                          ),
                        ),
                        child: const Text(
                          '아래로 스크롤',
                          style: TextStyle(fontWeight: FontWeight.w900),
                        ),
                      ),
                    ),
                  ),
              ],
            ),
          ),
          SafeArea(
            top: false,
            child: Container(
              width: double.infinity,
              padding: const EdgeInsets.fromLTRB(18, 12, 18, 14),
              decoration: const BoxDecoration(
                color: AppColors.background,
                border: Border(top: BorderSide(color: AppColors.border)),
              ),
              child: SizedBox(
                height: 50,
                child: ElevatedButton(
                  onPressed: _agreeCurrentTerm,
                  style: ElevatedButton.styleFrom(
                    backgroundColor: AppColors.primaryRed,
                    foregroundColor: AppColors.white,
                    elevation: 0,
                    shape: RoundedRectangleBorder(
                      borderRadius: BorderRadius.circular(12),
                    ),
                  ),
                  child: const Text(
                    '동의하기',
                    style: TextStyle(fontWeight: FontWeight.w900),
                  ),
                ),
              ),
            ),
          ),
        ],
      ),
    );
  }

  Widget _termSection(int index) {
    final term = widget.terms[index];
    final agreed = _agreedTermIndexes.contains(index);

    return Container(
      margin: EdgeInsets.zero,
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            children: [
              Expanded(
                child: Text(
                  term.termsTitle,
                  style: const TextStyle(
                    color: AppColors.textPrimary,
                    fontSize: 18,
                    fontWeight: FontWeight.w900,
                  ),
                ),
              ),
              Icon(
                agreed ? Icons.check_circle : Icons.radio_button_unchecked,
                color: agreed ? AppColors.primaryRed : AppColors.textSecondary,
              ),
            ],
          ),
          const SizedBox(height: 12),
          ...List.generate(term.pages.length, (pageIndex) {
            return Padding(
              key: _currentPageKeys[pageIndex],
              padding: const EdgeInsets.only(bottom: 12),
              child: ClipRRect(
                borderRadius: BorderRadius.circular(8),
                child: Image.memory(
                  _currentPageBytes[pageIndex],
                  width: double.infinity,
                  fit: BoxFit.fitWidth,
                ),
              ),
            );
          }),
        ],
      ),
    );
  }
}
