import 'package:flutter/material.dart';

import '../../core/constants/app_colors.dart';
import '../../core/storage/quick_menu_storage.dart';
import '../../data/models/mypage_model.dart';
import '../../data/services/member_api.dart';
import '../account/account_list_screen.dart';
import '../account_opening/account_opening_screen.dart';
import '../branch/branch_map_screen.dart';
import '../branch/branch_reservation_list_screen.dart';
import '../mypage/mypage_screen.dart';
import '../product_compare/product_list_screen.dart';
import '../quick_menu/quick_menu_edit_screen.dart';

class HomeScreen extends StatefulWidget {
  final String memberName;

  const HomeScreen({super.key, required this.memberName});

  @override
  State<HomeScreen> createState() => _HomeScreenState();
}

class _HomeScreenState extends State<HomeScreen> {
  final MemberApi _memberApi = MemberApi();

  late Future<MyPageModel> _myPageFuture;

  List<String> _quickMenuSlotIds = List<String>.from(
    QuickMenuStorage.defaultSlotIds,
  );

  String _quickMenuViewType = 'GRID';

  @override
  void initState() {
    super.initState();
    _myPageFuture = _memberApi.getMyPage();
    _loadQuickMenuSetting();
  }

  Future<void> _loadQuickMenuSetting() async {
    final slotIds = await QuickMenuStorage.getMenuSlotIds();
    final viewType = await QuickMenuStorage.getViewType();

    if (!mounted) return;

    setState(() {
      _quickMenuSlotIds = slotIds;
      _quickMenuViewType = viewType;
    });
  }

  String _formatMoney(int amount) {
    return amount.toString().replaceAllMapped(
      RegExp(r'\B(?=(\d{3})+(?!\d))'),
      (match) => ',',
    );
  }

  void _showPreparingMessage(String message) {
    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(content: Text(message), behavior: SnackBarBehavior.floating),
    );
  }

  void _goToProductList() {
    Navigator.push(
      context,
      MaterialPageRoute(builder: (_) => const ProductListScreen()),
    );
  }

  Future<void> _goToAccountOpening() async {
    final result = await Navigator.push<bool>(
      context,
      MaterialPageRoute(builder: (_) => const AccountOpeningScreen()),
    );

    if (result == true && mounted) {
      setState(() {
        _myPageFuture = _memberApi.getMyPage();
      });
    }
  }

  void _goToBranchMap() {
    Navigator.push(
      context,
      MaterialPageRoute(builder: (_) => const BranchMapScreen()),
    );
  }

  void _goToReservationList() {
    Navigator.push(
      context,
      MaterialPageRoute(builder: (_) => const BranchReservationListScreen()),
    );
  }

  Future<void> _goToMyPage() async {
    await Navigator.push(
      context,
      MaterialPageRoute(builder: (_) => const MyPageScreen()),
    );

    await _loadQuickMenuSetting();
  }

  Future<void> _goToQuickMenuEdit() async {
    final result = await Navigator.push<bool>(
      context,
      MaterialPageRoute(builder: (_) => const QuickMenuEditScreen()),
    );

    if (result == true) {
      await _loadQuickMenuSetting();
    }
  }

  List<_QuickMenuItem> _getAllQuickMenus() {
    return [
      _QuickMenuItem(
        id: 'account',
        title: '계좌조회',
        subtitle: '내 계좌와 잔액 확인',
        icon: Icons.account_balance_outlined,
        onTap: () {
          Navigator.push(
            context,
            MaterialPageRoute(builder: (_) => const AccountListScreen()),
          );
        },
      ),
      _QuickMenuItem(
        id: 'transfer',
        title: '이체',
        subtitle: '내 계좌 간 이체',
        icon: Icons.swap_horiz_rounded,
        onTap: () {
          _showPreparingMessage('이체 기능은 이후 연결 예정입니다.');
        },
      ),
      _QuickMenuItem(
        id: 'deposit',
        title: '예금상품',
        subtitle: '예금 상품 목록 확인',
        icon: Icons.savings_outlined,
        onTap: _goToProductList,
      ),
      _QuickMenuItem(
        id: 'saving',
        title: '적금상품',
        subtitle: '적금 상품 목록 확인',
        icon: Icons.account_balance_wallet_outlined,
        onTap: _goToProductList,
      ),
      _QuickMenuItem(
        id: 'recommend',
        title: '상품추천',
        subtitle: 'AI 기반 상품 추천',
        icon: Icons.auto_awesome_outlined,
        onTap: () {
          _showPreparingMessage('AI 상품추천 기능은 이후 연결 예정입니다.');
        },
      ),
      _QuickMenuItem(
        id: 'branch',
        title: '영업점',
        subtitle: '가까운 영업점 찾기',
        icon: Icons.location_on_outlined,
        onTap: _goToBranchMap,
      ),
      _QuickMenuItem(
        id: 'reservation',
        title: '예약내역',
        subtitle: '상담 예약 내역 확인',
        icon: Icons.calendar_month_outlined,
        onTap: _goToReservationList,
      ),
      _QuickMenuItem(
        id: 'customer',
        title: '고객센터',
        subtitle: 'FAQ 및 공지사항 확인',
        icon: Icons.headset_mic_outlined,
        onTap: () {
          _showPreparingMessage('고객센터 화면은 이후 연결 예정입니다.');
        },
      ),
      _QuickMenuItem(
        id: 'exchange',
        title: '환율조회',
        subtitle: '주요 통화 환율 확인',
        icon: Icons.currency_exchange_rounded,
        onTap: () {
          _showPreparingMessage('환율조회 기능은 이후 연결 예정입니다.');
        },
      ),
      _QuickMenuItem(
        id: 'event',
        title: '이벤트/혜택',
        subtitle: '진행 중인 이벤트 확인',
        icon: Icons.card_giftcard_rounded,
        onTap: () {
          _showPreparingMessage('이벤트/혜택 화면은 이후 연결 예정입니다.');
        },
      ),
      _QuickMenuItem(
        id: 'dictionary',
        title: '금융용어',
        subtitle: '어려운 금융 용어 확인',
        icon: Icons.menu_book_rounded,
        onTap: () {
          _showPreparingMessage('금융용어 사전은 이후 연결 예정입니다.');
        },
      ),
      _QuickMenuItem(
        id: 'calculator',
        title: '금융계산기',
        subtitle: '예금·적금 예상 금액 계산',
        icon: Icons.calculate_rounded,
        onTap: () {
          _showPreparingMessage('금융계산기는 이후 연결 예정입니다.');
        },
      ),
    ];
  }

  List<_QuickMenuItem?> _getQuickMenuSlots() {
    final allMenus = _getAllQuickMenus();
    final slots = <_QuickMenuItem?>[];

    for (final id in _quickMenuSlotIds) {
      if (id == QuickMenuStorage.emptySlotId) {
        slots.add(null);
        continue;
      }

      final index = allMenus.indexWhere((menu) => menu.id == id);

      if (index == -1) {
        slots.add(null);
      } else {
        slots.add(allMenus[index]);
      }
    }

    while (slots.length < 8) {
      slots.add(null);
    }

    if (slots.length > 8) {
      return slots.sublist(0, 8);
    }

    return slots;
  }

  List<_QuickMenuItem> _getSelectedQuickMenus() {
    final selectedMenus = _getQuickMenuSlots()
        .whereType<_QuickMenuItem>()
        .toList();

    if (selectedMenus.isEmpty) {
      return _getAllQuickMenus().take(8).toList();
    }

    return selectedMenus;
  }

  Widget _buildTopHeader(String displayName) {
    return Padding(
      padding: const EdgeInsets.fromLTRB(22, 18, 22, 10),
      child: Row(
        children: [
          Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Text(
                '$displayName님',
                style: const TextStyle(
                  fontSize: 25,
                  fontWeight: FontWeight.w900,
                  color: AppColors.textPrimary,
                ),
              ),
              const SizedBox(height: 4),
              Container(
                width: 70,
                height: 2,
                decoration: BoxDecoration(
                  color: AppColors.textPrimary,
                  borderRadius: BorderRadius.circular(99),
                ),
              ),
            ],
          ),
          const Spacer(),
          _buildHeaderIcon(
            icon: Icons.history_rounded,
            onTap: () {
              _showPreparingMessage('최근 이용 내역은 이후 연결 예정입니다.');
            },
          ),
          const SizedBox(width: 10),
          _buildHeaderIcon(
            icon: Icons.notifications_none_rounded,
            onTap: () {
              _showPreparingMessage('알림 기능은 이후 연결 예정입니다.');
            },
          ),
          const SizedBox(width: 10),
          _buildHeaderIcon(
            icon: Icons.menu_rounded,
            onTap: () {
              _showPreparingMessage('전체 메뉴는 이후 연결 예정입니다.');
            },
          ),
        ],
      ),
    );
  }

  Widget _buildHeaderIcon({
    required IconData icon,
    required VoidCallback onTap,
  }) {
    return InkWell(
      borderRadius: BorderRadius.circular(999),
      onTap: onTap,
      child: Container(
        width: 38,
        height: 38,
        decoration: BoxDecoration(
          color: AppColors.cardBackground,
          shape: BoxShape.circle,
          border: Border.all(color: AppColors.border),
        ),
        child: Icon(icon, size: 22, color: AppColors.textPrimary),
      ),
    );
  }

  Widget _buildBalanceCard(MyPageModel mypage, String displayName) {
    if (mypage.accountCount == 0) {
      return Container(
        width: double.infinity,
        margin: const EdgeInsets.symmetric(horizontal: 22),
        padding: const EdgeInsets.fromLTRB(24, 24, 24, 22),
        decoration: BoxDecoration(
          color: const Color(0xFF111827),
          borderRadius: BorderRadius.circular(26),
          boxShadow: [
            BoxShadow(
              color: Colors.black.withValues(alpha: 0.08),
              blurRadius: 18,
              offset: const Offset(0, 8),
            ),
          ],
        ),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text(
              displayName,
              style: const TextStyle(
                fontSize: 17,
                fontWeight: FontWeight.w800,
                color: AppColors.white,
              ),
            ),
            const SizedBox(height: 12),
            const Text(
              '아직 입출금 계좌가 없습니다',
              style: TextStyle(
                fontSize: 13,
                color: Color(0xFFB8C3D6),
                fontWeight: FontWeight.w600,
              ),
            ),
            const SizedBox(height: 16),
            SizedBox(
              width: double.infinity,
              height: 44,
              child: ElevatedButton(
                style: ElevatedButton.styleFrom(
                  backgroundColor: AppColors.primaryRed,
                  foregroundColor: AppColors.white,
                  elevation: 0,
                  shape: RoundedRectangleBorder(
                    borderRadius: BorderRadius.circular(999),
                  ),
                ),
                onPressed: _goToAccountOpening,
                child: const Text(
                  '입출금 계좌 개설하기',
                  style: TextStyle(fontSize: 15, fontWeight: FontWeight.w900),
                ),
              ),
            ),
          ],
        ),
      );
    }

    return Container(
      width: double.infinity,
      margin: const EdgeInsets.symmetric(horizontal: 22),
      padding: const EdgeInsets.fromLTRB(24, 24, 24, 22),
      decoration: BoxDecoration(
        color: const Color(0xFF111827),
        borderRadius: BorderRadius.circular(26),
        boxShadow: [
          BoxShadow(
            color: Colors.black.withOpacity(0.08),
            blurRadius: 18,
            offset: const Offset(0, 8),
          ),
        ],
      ),
      child: Row(
        children: [
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  '$displayName님, 안녕하세요',
                  style: const TextStyle(
                    fontSize: 17,
                    fontWeight: FontWeight.w800,
                    color: AppColors.white,
                  ),
                ),
                const SizedBox(height: 12),
                const Text(
                  '대표계좌 잔액',
                  style: TextStyle(
                    fontSize: 13,
                    color: Color(0xFFB8C3D6),
                    fontWeight: FontWeight.w600,
                  ),
                ),
                const SizedBox(height: 6),
                Text(
                  '${_formatMoney(mypage.totalBalance)}원',
                  style: const TextStyle(
                    fontSize: 30,
                    fontWeight: FontWeight.w900,
                    color: AppColors.white,
                    letterSpacing: -0.8,
                  ),
                ),
              ],
            ),
          ),
          const SizedBox(width: 14),
          SizedBox(
            height: 42,
            child: ElevatedButton(
              style: ElevatedButton.styleFrom(
                backgroundColor: AppColors.primaryRed,
                foregroundColor: AppColors.white,
                elevation: 0,
                padding: const EdgeInsets.symmetric(horizontal: 20),
                shape: RoundedRectangleBorder(
                  borderRadius: BorderRadius.circular(999),
                ),
              ),
              onPressed: () {
                _showPreparingMessage('이체 기능은 이후 연결 예정입니다.');
              },
              child: const Text(
                '이체',
                style: TextStyle(fontSize: 15, fontWeight: FontWeight.w900),
              ),
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildSectionTitle({
    required String title,
    String? actionText,
    VoidCallback? onActionTap,
  }) {
    return Padding(
      padding: const EdgeInsets.fromLTRB(22, 26, 22, 14),
      child: Row(
        children: [
          Text(
            title,
            style: const TextStyle(
              fontSize: 19,
              fontWeight: FontWeight.w900,
              color: AppColors.textPrimary,
            ),
          ),
          const Spacer(),
          if (actionText != null)
            InkWell(
              borderRadius: BorderRadius.circular(999),
              onTap: onActionTap,
              child: Container(
                padding: const EdgeInsets.symmetric(
                  horizontal: 18,
                  vertical: 8,
                ),
                decoration: BoxDecoration(
                  color: AppColors.cardBackground,
                  borderRadius: BorderRadius.circular(999),
                  border: Border.all(color: AppColors.primaryRed),
                ),
                child: Text(
                  actionText,
                  style: const TextStyle(
                    fontSize: 13,
                    fontWeight: FontWeight.w800,
                    color: AppColors.primaryRed,
                  ),
                ),
              ),
            ),
        ],
      ),
    );
  }

  Widget _buildQuickMenuGrid() {
    if (_quickMenuViewType == 'HORIZONTAL') {
      return _buildQuickMenuHorizontal(_getSelectedQuickMenus());
    }

    final slots = _getQuickMenuSlots();

    return Padding(
      padding: const EdgeInsets.symmetric(horizontal: 22),
      child: GridView.builder(
        itemCount: 8,
        shrinkWrap: true,
        physics: const NeverScrollableScrollPhysics(),
        gridDelegate: const SliverGridDelegateWithFixedCrossAxisCount(
          crossAxisCount: 4,
          crossAxisSpacing: 14,
          mainAxisSpacing: 18,
          childAspectRatio: 0.82,
        ),
        itemBuilder: (context, index) {
          final menu = slots[index];

          if (menu == null) {
            return const SizedBox.shrink();
          }

          return _buildQuickMenuTile(menu);
        },
      ),
    );
  }

  Widget _buildQuickMenuTile(_QuickMenuItem menu) {
    return InkWell(
      borderRadius: BorderRadius.circular(18),
      onTap: menu.onTap,
      child: Column(
        children: [
          Container(
            width: 58,
            height: 58,
            decoration: BoxDecoration(
              color: AppColors.cardBackground,
              borderRadius: BorderRadius.circular(18),
              border: Border.all(color: AppColors.border),
            ),
            child: Icon(menu.icon, color: AppColors.primaryRed, size: 27),
          ),
          const SizedBox(height: 8),
          Text(
            menu.title,
            textAlign: TextAlign.center,
            maxLines: 1,
            overflow: TextOverflow.ellipsis,
            style: const TextStyle(
              fontSize: 12,
              fontWeight: FontWeight.w700,
              color: AppColors.textSecondary,
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildQuickMenuHorizontal(List<_QuickMenuItem> menus) {
    return SizedBox(
      height: 100,
      child: ListView.separated(
        padding: const EdgeInsets.symmetric(horizontal: 22),
        scrollDirection: Axis.horizontal,
        itemCount: menus.length,
        separatorBuilder: (context, index) => const SizedBox(width: 12),
        itemBuilder: (context, index) {
          final menu = menus[index];

          return InkWell(
            borderRadius: BorderRadius.circular(22),
            onTap: menu.onTap,
            child: Container(
              width: 160,
              padding: const EdgeInsets.all(14),
              decoration: BoxDecoration(
                color: AppColors.cardBackground,
                borderRadius: BorderRadius.circular(22),
                border: Border.all(color: AppColors.border),
              ),
              child: Row(
                children: [
                  Container(
                    width: 46,
                    height: 46,
                    decoration: BoxDecoration(
                      color: const Color(0xFFFFF0F0),
                      borderRadius: BorderRadius.circular(15),
                    ),
                    child: Icon(
                      menu.icon,
                      color: AppColors.primaryRed,
                      size: 25,
                    ),
                  ),
                  const SizedBox(width: 12),
                  Expanded(
                    child: Column(
                      mainAxisAlignment: MainAxisAlignment.center,
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Text(
                          menu.title,
                          maxLines: 1,
                          overflow: TextOverflow.ellipsis,
                          style: const TextStyle(
                            fontSize: 14,
                            fontWeight: FontWeight.w900,
                            color: AppColors.textPrimary,
                          ),
                        ),
                        const SizedBox(height: 5),
                        Text(
                          menu.subtitle,
                          maxLines: 2,
                          overflow: TextOverflow.ellipsis,
                          style: const TextStyle(
                            fontSize: 11,
                            height: 1.3,
                            fontWeight: FontWeight.w600,
                            color: AppColors.textSecondary,
                          ),
                        ),
                      ],
                    ),
                  ),
                ],
              ),
            ),
          );
        },
      ),
    );
  }

  Widget _buildRecommendCard() {
    return InkWell(
      borderRadius: BorderRadius.circular(24),
      onTap: _goToProductList,
      child: Container(
        width: double.infinity,
        margin: const EdgeInsets.symmetric(horizontal: 22),
        padding: const EdgeInsets.fromLTRB(22, 22, 22, 22),
        decoration: BoxDecoration(
          color: AppColors.cardBackground,
          borderRadius: BorderRadius.circular(24),
          border: Border.all(color: AppColors.border),
        ),
        child: Row(
          children: [
            Container(
              width: 48,
              height: 48,
              decoration: BoxDecoration(
                color: const Color(0xFFFFF0F0),
                borderRadius: BorderRadius.circular(16),
              ),
              child: const Icon(
                Icons.recommend_outlined,
                color: AppColors.primaryRed,
                size: 26,
              ),
            ),
            const SizedBox(width: 16),
            const Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    '추천 상품',
                    style: TextStyle(
                      fontSize: 17,
                      fontWeight: FontWeight.w900,
                      color: AppColors.textPrimary,
                    ),
                  ),
                  SizedBox(height: 8),
                  Text(
                    'AI 추천 예금 상품',
                    style: TextStyle(
                      fontSize: 14,
                      fontWeight: FontWeight.w800,
                      color: AppColors.textPrimary,
                    ),
                  ),
                  SizedBox(height: 5),
                  Text(
                    '최고 연 3.8% · 모바일 가입 가능',
                    style: TextStyle(
                      fontSize: 13,
                      color: AppColors.textSecondary,
                      fontWeight: FontWeight.w600,
                    ),
                  ),
                ],
              ),
            ),
            const Icon(
              Icons.chevron_right_rounded,
              color: AppColors.textSecondary,
              size: 28,
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildFinanceSummary(MyPageModel mypage) {
    return Padding(
      padding: const EdgeInsets.symmetric(horizontal: 22),
      child: Row(
        children: [
          _buildSmallSummaryBox(title: '계좌', value: '${mypage.accountCount}개'),
          const SizedBox(width: 12),
          _buildSmallSummaryBox(
            title: '가입상품',
            value: '${mypage.productCount}개',
          ),
        ],
      ),
    );
  }

  Widget _buildSmallSummaryBox({required String title, required String value}) {
    return Expanded(
      child: Container(
        padding: const EdgeInsets.fromLTRB(18, 16, 18, 16),
        decoration: BoxDecoration(
          color: AppColors.cardBackground,
          borderRadius: BorderRadius.circular(18),
          border: Border.all(color: AppColors.border),
        ),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
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
                fontSize: 22,
                color: AppColors.textPrimary,
                fontWeight: FontWeight.w900,
              ),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildBottomNavigation() {
    return Container(
      margin: const EdgeInsets.fromLTRB(18, 8, 18, 16),
      padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 10),
      decoration: BoxDecoration(
        color: AppColors.cardBackground,
        borderRadius: BorderRadius.circular(28),
        border: Border.all(color: AppColors.border),
        boxShadow: [
          BoxShadow(
            color: Colors.black.withOpacity(0.04),
            blurRadius: 18,
            offset: const Offset(0, 8),
          ),
        ],
      ),
      child: Row(
        mainAxisAlignment: MainAxisAlignment.spaceAround,
        children: [
          _buildBottomNavItem(
            title: '홈',
            icon: Icons.home_rounded,
            selected: true,
            onTap: () {},
          ),
          _buildBottomNavItem(
            title: '상품',
            icon: Icons.savings_outlined,
            selected: false,
            onTap: _goToProductList,
          ),
          _buildBottomNavItem(
            title: '영업점',
            icon: Icons.location_on_outlined,
            selected: false,
            onTap: _goToBranchMap,
          ),
          _buildBottomNavItem(
            title: '마이',
            icon: Icons.person_outline_rounded,
            selected: false,
            onTap: _goToMyPage,
          ),
          _buildBottomNavItem(
            title: '전체',
            icon: Icons.menu_rounded,
            selected: false,
            onTap: () {
              _showPreparingMessage('전체 메뉴는 이후 연결 예정입니다.');
            },
          ),
        ],
      ),
    );
  }

  Widget _buildBottomNavItem({
    required String title,
    required IconData icon,
    required bool selected,
    required VoidCallback onTap,
  }) {
    return InkWell(
      borderRadius: BorderRadius.circular(16),
      onTap: onTap,
      child: SizedBox(
        width: 54,
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            Icon(
              icon,
              size: 23,
              color: selected ? AppColors.primaryRed : AppColors.textSecondary,
            ),
            const SizedBox(height: 5),
            Text(
              title,
              style: TextStyle(
                fontSize: 11,
                fontWeight: FontWeight.w800,
                color: selected
                    ? AppColors.primaryRed
                    : AppColors.textSecondary,
              ),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildHomeContent(MyPageModel mypage) {
    final displayName = mypage.memberName.isNotEmpty
        ? mypage.memberName
        : widget.memberName;

    return Column(
      children: [
        Expanded(
          child: SingleChildScrollView(
            child: Column(
              children: [
                _buildTopHeader(displayName),
                const SizedBox(height: 10),
                _buildBalanceCard(mypage, displayName),
                _buildSectionTitle(
                  title: '빠른 메뉴',
                  actionText: '편집',
                  onActionTap: _goToQuickMenuEdit,
                ),
                _buildQuickMenuGrid(),
                _buildSectionTitle(title: '내 금융 요약'),
                _buildFinanceSummary(mypage),
                _buildSectionTitle(title: '추천 상품'),
                _buildRecommendCard(),
                const SizedBox(height: 22),
              ],
            ),
          ),
        ),
        _buildBottomNavigation(),
      ],
    );
  }

  Widget _buildErrorView(Object error) {
    return Center(
      child: Padding(
        padding: const EdgeInsets.all(24),
        child: Container(
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
                size: 40,
              ),
              const SizedBox(height: 14),
              const Text(
                '마이페이지 정보를 불러오지 못했습니다.',
                style: TextStyle(
                  fontSize: 17,
                  fontWeight: FontWeight.w800,
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
      body: SafeArea(
        child: FutureBuilder<MyPageModel>(
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

            return _buildHomeContent(snapshot.data!);
          },
        ),
      ),
    );
  }
}

class _QuickMenuItem {
  final String id;
  final String title;
  final String subtitle;
  final IconData icon;
  final VoidCallback onTap;

  _QuickMenuItem({
    required this.id,
    required this.title,
    required this.subtitle,
    required this.icon,
    required this.onTap,
  });
}
