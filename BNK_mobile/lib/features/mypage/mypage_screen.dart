import 'package:flutter/material.dart';

import '../../core/constants/app_colors.dart';
import '../../core/storage/secure_storage.dart';
import '../../data/models/mypage_model.dart';
import '../../data/services/auth_api.dart';
import '../../data/services/member_api.dart';
import '../account/account_list_screen.dart';
import '../auth/login_screen.dart';
import '../pin/pin_setup_screen.dart';
import '../product_compare/product_list_screen.dart';
import '../quick_menu/quick_menu_edit_screen.dart';

class MyPageScreen extends StatefulWidget {
  const MyPageScreen({super.key});

  @override
  State<MyPageScreen> createState() => _MyPageScreenState();
}

class _MyPageScreenState extends State<MyPageScreen> {
  final MemberApi _memberApi = MemberApi();
  final AuthApi _authApi = AuthApi();

  late Future<MyPageModel> _myPageFuture;
  bool _isLoggingOut = false;

  @override
  void initState() {
    super.initState();
    _myPageFuture = _memberApi.getMyPage();
  }

  Future<void> _logout() async {
    if (_isLoggingOut) return;

    final shouldLogout = await showDialog<bool>(
      context: context,
      builder: (dialogContext) {
        return AlertDialog(
          title: const Text('로그아웃'),
          content: const Text('현재 기기에서 로그아웃하시겠습니까?'),
          actions: [
            TextButton(
              onPressed: () => Navigator.of(dialogContext).pop(false),
              child: const Text('취소'),
            ),
            FilledButton(
              onPressed: () => Navigator.of(dialogContext).pop(true),
              child: const Text('로그아웃'),
            ),
          ],
        );
      },
    );

    if (shouldLogout != true || !mounted) return;

    setState(() {
      _isLoggingOut = true;
    });

    try {
      await _authApi.logout();

      if (!mounted) return;

      Navigator.pushAndRemoveUntil(
        context,
        MaterialPageRoute(builder: (_) => const LoginScreen()),
        (route) => false,
      );
    } catch (error) {
      if (!mounted) return;

      setState(() {
        _isLoggingOut = false;
      });

      _showPreparingMessage('로그아웃 중 오류가 발생했습니다. 다시 시도해 주세요.');
    }
  }

  Future<void> _resetPin(String memberName) async {
    await SecureStorage.deletePin();

    if (!mounted) return;

    Navigator.push(
      context,
      MaterialPageRoute(builder: (_) => PinSetupScreen(memberName: memberName)),
    );
  }

  void _goToQuickMenuEdit() {
    Navigator.push(
      context,
      MaterialPageRoute(builder: (_) => const QuickMenuEditScreen()),
    );
  }

  void _showPreparingMessage(String message) {
    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(content: Text(message), behavior: SnackBarBehavior.floating),
    );
  }

  Widget _buildHeader() {
    return Container(
      width: double.infinity,
      padding: const EdgeInsets.fromLTRB(22, 22, 22, 24),
      decoration: BoxDecoration(
        color: AppColors.cardBackground,
        borderRadius: BorderRadius.circular(28),
        border: Border.all(color: AppColors.border),
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
            '마이페이지',
            style: TextStyle(
              fontSize: 25,
              fontWeight: FontWeight.w900,
              color: AppColors.textPrimary,
            ),
          ),
          SizedBox(height: 8),
          Text(
            '내 정보, 금융 현황, 보안 설정을 확인합니다.',
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

  Widget _buildProfileCard(MyPageModel mypage) {
    return Container(
      width: double.infinity,
      padding: const EdgeInsets.all(22),
      decoration: BoxDecoration(
        color: const Color(0xFF111827),
        borderRadius: BorderRadius.circular(26),
      ),
      child: Row(
        children: [
          Container(
            width: 64,
            height: 64,
            decoration: BoxDecoration(
              color: AppColors.primaryRed,
              borderRadius: BorderRadius.circular(22),
            ),
            child: const Icon(
              Icons.person_rounded,
              color: AppColors.white,
              size: 36,
            ),
          ),
          const SizedBox(width: 16),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  '${mypage.memberName}님',
                  style: const TextStyle(
                    fontSize: 23,
                    fontWeight: FontWeight.w900,
                    color: AppColors.white,
                  ),
                ),
                const SizedBox(height: 8),
                Text(
                  '${mypage.memberType} · ${mypage.memberStatus}',
                  style: const TextStyle(
                    fontSize: 14,
                    color: Color(0xFFB8C3D6),
                    fontWeight: FontWeight.w700,
                  ),
                ),
                const SizedBox(height: 8),
                Text(
                  mypage.loginId.isEmpty ? '로그인 ID 확인' : 'ID ${mypage.loginId}',
                  style: const TextStyle(
                    fontSize: 13,
                    color: Color(0xFFB8C3D6),
                    fontWeight: FontWeight.w600,
                  ),
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildSummarySection(MyPageModel mypage) {
    return Row(
      children: [
        _buildSummaryBox(
          title: '계좌',
          value: '${mypage.accountCount}개',
          icon: Icons.account_balance_outlined,
        ),
        const SizedBox(width: 12),
        _buildSummaryBox(
          title: '가입상품',
          value: '${mypage.productCount}개',
          icon: Icons.savings_outlined,
        ),
      ],
    );
  }

  Widget _buildSummaryBox({
    required String title,
    required String value,
    required IconData icon,
  }) {
    return Expanded(
      child: Container(
        padding: const EdgeInsets.all(18),
        decoration: BoxDecoration(
          color: AppColors.cardBackground,
          borderRadius: BorderRadius.circular(22),
          border: Border.all(color: AppColors.border),
        ),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Icon(icon, color: AppColors.primaryRed, size: 26),
            const SizedBox(height: 14),
            Text(
              title,
              style: const TextStyle(
                fontSize: 13,
                color: AppColors.textSecondary,
                fontWeight: FontWeight.w700,
              ),
            ),
            const SizedBox(height: 8),
            Text(
              value,
              style: const TextStyle(
                fontSize: 24,
                color: AppColors.textPrimary,
                fontWeight: FontWeight.w900,
              ),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildSectionTitle(String title) {
    return Padding(
      padding: const EdgeInsets.fromLTRB(2, 26, 2, 12),
      child: Align(
        alignment: Alignment.centerLeft,
        child: Text(
          title,
          style: const TextStyle(
            fontSize: 19,
            fontWeight: FontWeight.w900,
            color: AppColors.textPrimary,
          ),
        ),
      ),
    );
  }

  Widget _buildInfoManageMenu() {
    return _buildMenuGroup(
      children: [
        _buildMenuItem(
          icon: Icons.manage_accounts_outlined,
          title: '내 정보 관리',
          subtitle: '회원 정보 확인 및 수정',
          onTap: () {
            _showPreparingMessage('내 정보 관리는 이후 연결 예정입니다.');
          },
        ),
      ],
    );
  }

  Widget _buildFinanceMenu() {
    return _buildMenuGroup(
      children: [
        _buildMenuItem(
          icon: Icons.account_balance_outlined,
          title: '계좌 조회',
          subtitle: '내 계좌와 잔액 확인',
          onTap: () {
            Navigator.push(
              context,
              MaterialPageRoute(builder: (_) => const AccountListScreen()),
            );
          },
        ),
        _buildDivider(),
        _buildMenuItem(
          icon: Icons.account_balance_wallet_outlined,
          title: '계좌 관리',
          subtitle: '대표계좌, 계좌 별명 설정',
          onTap: () {
            _showPreparingMessage('계좌 관리는 이후 연결 예정입니다.');
          },
        ),
        _buildDivider(),
        _buildMenuItem(
          icon: Icons.savings_outlined,
          title: '가입상품 조회',
          subtitle: '가입한 예금/적금 상품 확인',
          onTap: () {
            _showPreparingMessage('가입상품 조회는 가입신청 API 완성 후 연결합니다.');
          },
        ),
        _buildDivider(),
        _buildMenuItem(
          icon: Icons.favorite_border_rounded,
          title: '관심상품',
          subtitle: '관심 등록한 상품 보기',
          onTap: () {
            _showPreparingMessage('관심상품 기능은 이후 연결 예정입니다.');
          },
        ),
        _buildDivider(),
        _buildMenuItem(
          icon: Icons.history_rounded,
          title: '최근 본 상품',
          subtitle: '최근 확인한 예적금 상품',
          onTap: () {
            Navigator.push(
              context,
              MaterialPageRoute(builder: (_) => const ProductListScreen()),
            );
          },
        ),
        _buildDivider(),
        _buildMenuItem(
          icon: Icons.compare_arrows_rounded,
          title: '상품 비교 내역',
          subtitle: '비교했던 상품 조건 확인',
          onTap: () {
            _showPreparingMessage('상품 비교 내역은 이후 연결 예정입니다.');
          },
        ),
      ],
    );
  }

  Widget _buildSecurityMenu(MyPageModel mypage) {
    return _buildMenuGroup(
      children: [
        _buildMenuItem(
          icon: Icons.password_rounded,
          title: 'PIN 재설정',
          subtitle: '간편비밀번호를 다시 등록합니다.',
          onTap: () {
            _resetPin(mypage.memberName);
          },
        ),
        _buildDivider(),
        _buildMenuItem(
          icon: Icons.fingerprint_rounded,
          title: '생체인증 설정',
          subtitle: '얼굴인식 또는 지문인증 설정',
          onTap: () {
            _showPreparingMessage('생체인증 설정은 이후 연결 예정입니다.');
          },
        ),
        _buildDivider(),
        _buildMenuItem(
          icon: Icons.login_rounded,
          title: '로그인 방식 설정',
          subtitle: 'PIN, 생체인증 로그인 방식 관리',
          onTap: () {
            _showPreparingMessage('로그인 방식 설정은 이후 연결 예정입니다.');
          },
        ),
        _buildDivider(),
        _buildMenuItem(
          icon: Icons.qr_code_rounded,
          title: 'QR 로그인 관리',
          subtitle: '웹 QR 로그인 인증 관리',
          onTap: () {
            _showPreparingMessage('QR 로그인 관리는 이후 연결 예정입니다.');
          },
        ),
        _buildDivider(),
        _buildMenuItem(
          icon: Icons.security_rounded,
          title: '보안매체 관리',
          subtitle: 'OTP, 보안매체 설정 관리',
          onTap: () {
            _showPreparingMessage('보안매체 관리는 이후 연결 예정입니다.');
          },
        ),
        _buildDivider(),
        _buildMenuItem(
          icon: _isLoggingOut
              ? Icons.hourglass_top_rounded
              : Icons.logout_rounded,
          title: '로그아웃',
          subtitle: '현재 기기의 로그인 정보를 삭제합니다.',
          onTap: _logout,
        ),
      ],
    );
  }

  Widget _buildCustomerCenterMenu() {
    return _buildMenuGroup(
      children: [
        _buildMenuItem(
          icon: Icons.help_outline_rounded,
          title: 'FAQ',
          subtitle: '자주 묻는 질문 확인',
          onTap: () {
            _showPreparingMessage('FAQ 화면은 이후 연결 예정입니다.');
          },
        ),
        _buildDivider(),
        _buildMenuItem(
          icon: Icons.campaign_outlined,
          title: '공지사항',
          subtitle: '은행 공지와 이벤트 확인',
          onTap: () {
            _showPreparingMessage('공지사항 화면은 이후 연결 예정입니다.');
          },
        ),
        _buildDivider(),
        _buildMenuItem(
          icon: Icons.location_on_outlined,
          title: '영업점 찾기',
          subtitle: '가까운 영업점 위치 확인',
          onTap: () {
            _showPreparingMessage('영업점 지도 화면은 이후 연결 예정입니다.');
          },
        ),
        _buildDivider(),
        _buildMenuItem(
          icon: Icons.calendar_month_outlined,
          title: '상담 예약',
          subtitle: '영업점 방문 상담 예약',
          onTap: () {
            _showPreparingMessage('상담 예약 화면은 이후 연결 예정입니다.');
          },
        ),
      ],
    );
  }

  Widget _buildSettingMenu() {
    return _buildMenuGroup(
      children: [
        _buildMenuItem(
          icon: Icons.notifications_none_rounded,
          title: '알림 설정',
          subtitle: '앱 푸시 알림 관리',
          onTap: () {
            _showPreparingMessage('알림 설정은 이후 연결 예정입니다.');
          },
        ),
        _buildDivider(),
        _buildMenuItem(
          icon: Icons.tune_rounded,
          title: '빠른 메뉴 편집',
          subtitle: '홈 화면 메뉴 순서 변경',
          onTap: _goToQuickMenuEdit,
        ),
        _buildDivider(),
        _buildMenuItem(
          icon: Icons.lock_outline_rounded,
          title: '앱 잠금 설정',
          subtitle: '앱 실행 시 인증 방식 관리',
          onTap: () {
            _showPreparingMessage('앱 잠금 설정은 이후 연결 예정입니다.');
          },
        ),
        _buildDivider(),
        _buildMenuItem(
          icon: Icons.info_outline_rounded,
          title: '앱 버전 정보',
          subtitle: 'BNK Mobile Test v1.0.0',
          onTap: () {
            _showPreparingMessage('현재 앱 버전은 v1.0.0입니다.');
          },
        ),
      ],
    );
  }

  Widget _buildMenuGroup({required List<Widget> children}) {
    return Container(
      width: double.infinity,
      decoration: BoxDecoration(
        color: AppColors.cardBackground,
        borderRadius: BorderRadius.circular(24),
        border: Border.all(color: AppColors.border),
      ),
      child: Column(children: children),
    );
  }

  Widget _buildMenuItem({
    required IconData icon,
    required String title,
    required String subtitle,
    required VoidCallback onTap,
  }) {
    return InkWell(
      borderRadius: BorderRadius.circular(24),
      onTap: onTap,
      child: Padding(
        padding: const EdgeInsets.fromLTRB(18, 16, 18, 16),
        child: Row(
          children: [
            Container(
              width: 44,
              height: 44,
              decoration: BoxDecoration(
                color: const Color(0xFFFFF0F0),
                borderRadius: BorderRadius.circular(16),
              ),
              child: Icon(icon, color: AppColors.primaryRed, size: 24),
            ),
            const SizedBox(width: 14),
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    title,
                    style: const TextStyle(
                      fontSize: 15,
                      fontWeight: FontWeight.w900,
                      color: AppColors.textPrimary,
                    ),
                  ),
                  const SizedBox(height: 5),
                  Text(
                    subtitle,
                    style: const TextStyle(
                      fontSize: 12,
                      fontWeight: FontWeight.w600,
                      color: AppColors.textSecondary,
                    ),
                  ),
                ],
              ),
            ),
            const Icon(
              Icons.chevron_right_rounded,
              color: AppColors.textSecondary,
              size: 26,
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildDivider() {
    return const Padding(
      padding: EdgeInsets.symmetric(horizontal: 18),
      child: Divider(height: 1, color: AppColors.border),
    );
  }

  Widget _buildContent(MyPageModel mypage) {
    return SingleChildScrollView(
      padding: const EdgeInsets.fromLTRB(22, 18, 22, 24),
      child: Column(
        children: [
          _buildHeader(),
          const SizedBox(height: 16),
          _buildProfileCard(mypage),
          _buildSectionTitle('프로필 / 내 정보'),
          _buildInfoManageMenu(),
          _buildSectionTitle('내 금융'),
          _buildSummarySection(mypage),
          const SizedBox(height: 12),
          _buildFinanceMenu(),
          _buildSectionTitle('인증 / 보안'),
          _buildSecurityMenu(mypage),
          _buildSectionTitle('고객센터'),
          _buildCustomerCenterMenu(),
          _buildSectionTitle('환경설정'),
          _buildSettingMenu(),
          const SizedBox(height: 24),
        ],
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
                '마이페이지 정보를 불러오지 못했습니다.',
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
                  color: AppColors.textSecondary,
                ),
              ),
              const SizedBox(height: 18),
              ElevatedButton(
                onPressed: () {
                  setState(() {
                    _myPageFuture = _memberApi.getMyPage();
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
          '마이페이지',
          style: TextStyle(fontWeight: FontWeight.w900),
        ),
      ),
      body: FutureBuilder<MyPageModel>(
        future: _myPageFuture,
        builder: (context, snapshot) {
          if (snapshot.connectionState == ConnectionState.waiting) {
            return const Center(
              child: CircularProgressIndicator(color: AppColors.primaryRed),
            );
          }

          if (snapshot.hasError) {
            return _buildErrorView(snapshot.error!);
          }

          if (!snapshot.hasData) {
            return const Center(child: Text('마이페이지 정보가 없습니다.'));
          }

          return _buildContent(snapshot.data!);
        },
      ),
    );
  }
}
