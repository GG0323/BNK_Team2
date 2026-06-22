import 'package:flutter/material.dart';

import '../../core/constants/app_colors.dart';
import '../../core/storage/quick_menu_storage.dart';

class QuickMenuEditScreen extends StatefulWidget {
  const QuickMenuEditScreen({super.key});

  @override
  State<QuickMenuEditScreen> createState() => _QuickMenuEditScreenState();
}

class _QuickMenuEditScreenState extends State<QuickMenuEditScreen> {
  final List<_QuickMenuEditItem> _masterMenus = [
    _QuickMenuEditItem(
      id: 'account',
      title: '계좌조회',
      subtitle: '내 계좌와 잔액 확인',
      icon: Icons.account_balance_outlined,
    ),
    _QuickMenuEditItem(
      id: 'transfer',
      title: '이체',
      subtitle: '내 계좌 간 이체',
      icon: Icons.swap_horiz_rounded,
    ),
    _QuickMenuEditItem(
      id: 'deposit',
      title: '예금상품',
      subtitle: '예금 상품 목록 확인',
      icon: Icons.savings_outlined,
    ),
    _QuickMenuEditItem(
      id: 'saving',
      title: '적금상품',
      subtitle: '적금 상품 목록 확인',
      icon: Icons.account_balance_wallet_outlined,
    ),
    _QuickMenuEditItem(
      id: 'recommend',
      title: '상품추천',
      subtitle: 'AI 기반 상품 추천',
      icon: Icons.auto_awesome_outlined,
    ),
    _QuickMenuEditItem(
      id: 'branch',
      title: '영업점',
      subtitle: '가까운 영업점 찾기',
      icon: Icons.location_on_outlined,
    ),
    _QuickMenuEditItem(
      id: 'reservation',
      title: '예약내역',
      subtitle: '상담 예약 내역 확인',
      icon: Icons.calendar_month_outlined,
    ),
    _QuickMenuEditItem(
      id: 'customer',
      title: '고객센터',
      subtitle: 'FAQ 및 공지사항 확인',
      icon: Icons.headset_mic_outlined,
    ),

    // 추가 후보 메뉴
    _QuickMenuEditItem(
      id: 'exchange',
      title: '환율조회',
      subtitle: '주요 통화 환율 확인',
      icon: Icons.currency_exchange_rounded,
    ),
    _QuickMenuEditItem(
      id: 'event',
      title: '이벤트/혜택',
      subtitle: '진행 중인 이벤트 확인',
      icon: Icons.card_giftcard_rounded,
    ),
    _QuickMenuEditItem(
      id: 'dictionary',
      title: '금융용어',
      subtitle: '어려운 금융 용어 확인',
      icon: Icons.menu_book_rounded,
    ),
    _QuickMenuEditItem(
      id: 'calculator',
      title: '금융계산기',
      subtitle: '예금·적금 예상 금액 계산',
      icon: Icons.calculate_rounded,
    ),
  ];

  List<_QuickMenuEditItem?> _slots = List<_QuickMenuEditItem?>.filled(8, null);
  List<_QuickMenuEditItem> _hiddenMenus = [];

  bool _loading = true;
  String _viewType = 'GRID';

  @override
  void initState() {
    super.initState();
    _loadSavedSetting();
  }

  Future<void> _loadSavedSetting() async {
    final savedSlotIds = await QuickMenuStorage.getMenuSlotIds();
    final savedViewType = await QuickMenuStorage.getViewType();

    final usedIds = <String>{};
    final loadedSlots = <_QuickMenuEditItem?>[];

    for (final id in savedSlotIds) {
      if (id == QuickMenuStorage.emptySlotId) {
        loadedSlots.add(null);
        continue;
      }

      final menuIndex = _masterMenus.indexWhere((menu) => menu.id == id);

      if (menuIndex == -1 || usedIds.contains(id)) {
        loadedSlots.add(null);
        continue;
      }

      loadedSlots.add(_masterMenus[menuIndex]);
      usedIds.add(id);
    }

    while (loadedSlots.length < 8) {
      loadedSlots.add(null);
    }

    final hiddenMenus = _masterMenus
        .where((menu) => !usedIds.contains(menu.id))
        .toList();

    if (!mounted) return;

    setState(() {
      _slots = loadedSlots.sublist(0, 8);
      _hiddenMenus = hiddenMenus;
      _viewType = savedViewType;
      _loading = false;
    });
  }

  int get _selectedCount {
    return _slots.where((menu) => menu != null).length;
  }

  bool get _isFull {
    return _selectedCount >= 8;
  }

  void _showSnack(String message) {
    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(
        content: Text(message),
        behavior: SnackBarBehavior.floating,
      ),
    );
  }

  void _removeMenuFromSlot(int index) {
    final menu = _slots[index];

    if (menu == null) return;

    if (_selectedCount <= 4) {
      _showSnack('빠른 메뉴는 최소 4개 이상 선택해야 합니다.');
      return;
    }

    setState(() {
      _slots[index] = null;
      _hiddenMenus.add(menu);
    });
  }

  void _addHiddenMenu(_QuickMenuEditItem menu) {
    final emptyIndex = _slots.indexWhere((slot) => slot == null);

    if (emptyIndex == -1) {
      _showSnack('빠른 메뉴는 최대 8개까지 등록할 수 있습니다. 기존 메뉴를 해제한 뒤 추가해주세요.');
      return;
    }

    setState(() {
      _slots[emptyIndex] = menu;
      _hiddenMenus.removeWhere((item) => item.id == menu.id);
    });
  }

  void _moveOrSwapMenu(int fromIndex, int toIndex) {
    if (fromIndex == toIndex) return;

    final fromMenu = _slots[fromIndex];

    if (fromMenu == null) return;

    setState(() {
      final targetMenu = _slots[toIndex];

      _slots[toIndex] = fromMenu;
      _slots[fromIndex] = targetMenu;
    });
  }

  Future<void> _saveMenus() async {
    if (_selectedCount < 4) {
      _showSnack('빠른 메뉴는 최소 4개 이상 선택해야 합니다.');
      return;
    }

    final slotIds = _slots
        .map((menu) => menu?.id ?? QuickMenuStorage.emptySlotId)
        .toList();

    await QuickMenuStorage.saveMenuSlotIds(slotIds);
    await QuickMenuStorage.saveViewType(_viewType);

    if (!mounted) return;

    _showSnack('빠른 메뉴 설정을 저장했습니다.');

    Navigator.pop(context, true);
  }

  Future<void> _resetMenus() async {
    await QuickMenuStorage.reset();

    if (!mounted) return;

    setState(() {
      _loading = true;
    });

    await _loadSavedSetting();

    if (!mounted) return;

    _showSnack('빠른 메뉴 설정을 초기화했습니다.');
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
            '빠른 메뉴 편집',
            style: TextStyle(
              fontSize: 25,
              fontWeight: FontWeight.w900,
              color: AppColors.textPrimary,
            ),
          ),
          SizedBox(height: 8),
          Text(
            '홈 화면에 표시할 8개 슬롯을 직접 구성합니다.',
            style: TextStyle(
              fontSize: 14,
              color: AppColors.textSecondary,
              fontWeight: FontWeight.w600,
              height: 1.4,
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildSelectedCountCard() {
    return Container(
      width: double.infinity,
      padding: const EdgeInsets.fromLTRB(18, 15, 18, 15),
      decoration: BoxDecoration(
        color: AppColors.cardBackground,
        borderRadius: BorderRadius.circular(18),
        border: Border.all(color: AppColors.border),
      ),
      child: Row(
        children: [
          const Text(
            '선택된 메뉴',
            style: TextStyle(
              fontSize: 14,
              fontWeight: FontWeight.w800,
              color: AppColors.textPrimary,
            ),
          ),
          const Spacer(),
          Text(
            '$_selectedCount / 8',
            style: const TextStyle(
              fontSize: 14,
              fontWeight: FontWeight.w900,
              color: AppColors.primaryRed,
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildSlotGridSection() {
    return Container(
      width: double.infinity,
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        color: AppColors.cardBackground,
        borderRadius: BorderRadius.circular(24),
        border: Border.all(color: AppColors.border),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          const Text(
            '홈 빠른메뉴 배치',
            style: TextStyle(
              fontSize: 16,
              fontWeight: FontWeight.w900,
              color: AppColors.textPrimary,
            ),
          ),
          const SizedBox(height: 6),
          const Text(
            '빈칸은 실제 홈 화면에서도 그대로 유지됩니다.',
            style: TextStyle(
              fontSize: 12,
              fontWeight: FontWeight.w600,
              color: AppColors.textSecondary,
            ),
          ),
          const SizedBox(height: 16),
          GridView.builder(
            itemCount: 8,
            shrinkWrap: true,
            physics: const NeverScrollableScrollPhysics(),
            gridDelegate: const SliverGridDelegateWithFixedCrossAxisCount(
            crossAxisCount: 4,
            crossAxisSpacing: 12,
            mainAxisSpacing: 14,
            childAspectRatio: 0.70,
          ),
            itemBuilder: (context, index) {
              final menu = _slots[index];

              if (menu == null) {
                return _buildEmptySlot(index);
              }

              return _buildDraggableMenuIcon(index, menu);
            },
          ),
        ],
      ),
    );
  }

  Widget _buildEmptySlot(int index) {
    return DragTarget<int>(
      onWillAccept: (fromIndex) {
        return fromIndex != null && fromIndex != index;
      },
      onAccept: (fromIndex) {
        _moveOrSwapMenu(fromIndex, index);
      },
      builder: (context, candidateData, rejectedData) {
        final isHovering = candidateData.isNotEmpty;

        return AnimatedContainer(
          duration: const Duration(milliseconds: 150),
          curve: Curves.easeOut,
          decoration: BoxDecoration(
            color: isHovering
                ? const Color(0xFFFFF0F0)
                : const Color(0xFFF7F8FA),
            borderRadius: BorderRadius.circular(18),
            border: Border.all(
              color: isHovering ? AppColors.primaryRed : AppColors.border,
              width: isHovering ? 1.5 : 1,
            ),
          ),
          child: Center(
            child: Icon(
              Icons.add_rounded,
              color: isHovering
                  ? AppColors.primaryRed
                  : AppColors.textSecondary.withOpacity(0.35),
              size: 24,
            ),
          ),
        );
      },
    );
  }

  Widget _buildDraggableMenuIcon(int index, _QuickMenuEditItem menu) {
    return DragTarget<int>(
      onWillAccept: (fromIndex) {
        return fromIndex != null && fromIndex != index;
      },
      onAccept: (fromIndex) {
        _moveOrSwapMenu(fromIndex, index);
      },
      builder: (context, candidateData, rejectedData) {
        final isHovering = candidateData.isNotEmpty;

        return LongPressDraggable<int>(
          data: index,
          dragAnchorStrategy: pointerDragAnchorStrategy,
          feedback: Material(
            color: Colors.transparent,
            child: SizedBox(
              width: 70,
              height: 92,
              child: _buildIconTile(
                menu: menu,
                dragging: true,
                hovering: false,
              ),
            ),
          ),
          childWhenDragging: Opacity(
            opacity: 0.28,
            child: _buildIconTile(
              menu: menu,
              dragging: false,
              hovering: false,
            ),
          ),
          child: GestureDetector(
            onTap: () {
              _removeMenuFromSlot(index);
            },
            child: _buildIconTile(
              menu: menu,
              dragging: false,
              hovering: isHovering,
            ),
          ),
        );
      },
    );
  }

  Widget _buildIconTile({
    required _QuickMenuEditItem menu,
    required bool dragging,
    required bool hovering,
  }) {
    final double scale = dragging
        ? 1.03
        : hovering
            ? 1.02
            : 1.0;

    return AnimatedScale(
      scale: scale,
      duration: const Duration(milliseconds: 150),
      curve: Curves.easeOutBack,
      child: AnimatedContainer(
        duration: const Duration(milliseconds: 160),
        curve: Curves.easeOut,
        padding: const EdgeInsets.fromLTRB(6, 8, 6, 8),
        decoration: BoxDecoration(
          color: hovering ? const Color(0xFFFFF0F0) : AppColors.cardBackground,
          borderRadius: BorderRadius.circular(18),
          border: Border.all(
            color: AppColors.primaryRed,
            width: hovering ? 1.5 : 1.3,
          ),
          boxShadow: dragging
              ? [
                  BoxShadow(
                    color: Colors.black.withOpacity(0.16),
                    blurRadius: 18,
                    offset: const Offset(0, 7),
                  ),
                ]
              : [],
        ),
        child: Stack(
          clipBehavior: Clip.none,
          children: [
            const Positioned(
              top: -3,
              right: -3,
              child: Icon(
                Icons.check_circle_rounded,
                color: AppColors.primaryRed,
                size: 18,
              ),
            ),
            Column(
              mainAxisAlignment: MainAxisAlignment.center,
              children: [
                AnimatedContainer(
                  duration: const Duration(milliseconds: 160),
                  width: dragging ? 43 : 42,
                  height: dragging ? 43 : 42,
                  decoration: BoxDecoration(
                    color: const Color(0xFFFFF0F0),
                    borderRadius: BorderRadius.circular(15),
                  ),
                  child: Icon(
                    menu.icon,
                    color: AppColors.primaryRed,
                    size: dragging ? 24 : 23,
                  ),
                ),
                const SizedBox(height: 8),
                Text(
                  menu.title,
                  maxLines: 1,
                  overflow: TextOverflow.ellipsis,
                  textAlign: TextAlign.center,
                  style: const TextStyle(
                    fontSize: 11,
                    fontWeight: FontWeight.w800,
                    color: AppColors.textPrimary,
                  ),
                ),
              ],
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildAvailableMenuSection() {
    return Container(
      width: double.infinity,
      padding: const EdgeInsets.all(18),
      decoration: BoxDecoration(
        color: AppColors.cardBackground,
        borderRadius: BorderRadius.circular(22),
        border: Border.all(color: AppColors.border),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          const Text(
            '추가 가능한 메뉴',
            style: TextStyle(
              fontSize: 16,
              fontWeight: FontWeight.w900,
              color: AppColors.textPrimary,
            ),
          ),
          const SizedBox(height: 6),
          Text(
            _isFull
                ? '8개 슬롯이 모두 사용 중입니다. 기존 메뉴를 해제한 뒤 추가할 수 있습니다.'
                : '추가할 메뉴를 선택하면 첫 번째 빈 슬롯에 들어갑니다.',
            style: const TextStyle(
              fontSize: 12,
              color: AppColors.textSecondary,
              fontWeight: FontWeight.w600,
            ),
          ),
          const SizedBox(height: 14),
          if (_hiddenMenus.isEmpty)
            const Text(
              '추가 가능한 메뉴가 없습니다.',
              style: TextStyle(
                fontSize: 13,
                color: AppColors.textSecondary,
                fontWeight: FontWeight.w600,
              ),
            )
          else
            Wrap(
              spacing: 10,
              runSpacing: 10,
              children: _hiddenMenus.map((menu) {
                return _buildAvailableMenuChip(menu);
              }).toList(),
            ),
        ],
      ),
    );
  }

  Widget _buildAvailableMenuChip(_QuickMenuEditItem menu) {
    final disabled = _isFull;

    return InkWell(
      borderRadius: BorderRadius.circular(999),
      onTap: () {
        _addHiddenMenu(menu);
      },
      child: AnimatedOpacity(
        duration: const Duration(milliseconds: 140),
        opacity: disabled ? 0.48 : 1.0,
        child: Container(
          padding: const EdgeInsets.fromLTRB(12, 9, 14, 9),
          decoration: BoxDecoration(
            color: disabled
                ? const Color(0xFFF7F8FA)
                : const Color(0xFFFFF0F0),
            borderRadius: BorderRadius.circular(999),
            border: Border.all(
              color: disabled ? AppColors.border : AppColors.primaryRed,
            ),
          ),
          child: Row(
            mainAxisSize: MainAxisSize.min,
            children: [
              Icon(
                menu.icon,
                size: 17,
                color: disabled
                    ? AppColors.textSecondary
                    : AppColors.primaryRed,
              ),
              const SizedBox(width: 6),
              Text(
                menu.title,
                style: TextStyle(
                  fontSize: 12,
                  color: disabled
                      ? AppColors.textSecondary
                      : AppColors.primaryRed,
                  fontWeight: FontWeight.w800,
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }

  Widget _buildViewTypeSection() {
    return Container(
      width: double.infinity,
      padding: const EdgeInsets.all(18),
      decoration: BoxDecoration(
        color: AppColors.cardBackground,
        borderRadius: BorderRadius.circular(22),
        border: Border.all(color: AppColors.border),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          const Text(
            '보기 방식',
            style: TextStyle(
              fontSize: 16,
              fontWeight: FontWeight.w900,
              color: AppColors.textPrimary,
            ),
          ),
          const SizedBox(height: 14),
          _buildViewTypeOption(
            title: '아이콘형',
            subtitle: '홈 화면에 아이콘 그리드로 표시',
            value: 'GRID',
          ),
          const SizedBox(height: 10),
          _buildViewTypeOption(
            title: '가로형',
            subtitle: '홈 화면에 가로 스크롤 카드로 표시',
            value: 'HORIZONTAL',
          ),
        ],
      ),
    );
  }

  Widget _buildViewTypeOption({
    required String title,
    required String subtitle,
    required String value,
  }) {
    final selected = _viewType == value;

    return InkWell(
      borderRadius: BorderRadius.circular(18),
      onTap: () {
        setState(() {
          _viewType = value;
        });
      },
      child: Container(
        padding: const EdgeInsets.fromLTRB(14, 14, 14, 14),
        decoration: BoxDecoration(
          color: selected ? const Color(0xFFFFF0F0) : const Color(0xFFF7F8FA),
          borderRadius: BorderRadius.circular(18),
          border: Border.all(
            color: selected ? AppColors.primaryRed : AppColors.border,
          ),
        ),
        child: Row(
          children: [
            Icon(
              selected
                  ? Icons.radio_button_checked_rounded
                  : Icons.radio_button_unchecked_rounded,
              color: selected ? AppColors.primaryRed : AppColors.textSecondary,
              size: 22,
            ),
            const SizedBox(width: 12),
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    title,
                    style: TextStyle(
                      fontSize: 14,
                      fontWeight: FontWeight.w900,
                      color: selected
                          ? AppColors.primaryRed
                          : AppColors.textPrimary,
                    ),
                  ),
                  const SizedBox(height: 4),
                  Text(
                    subtitle,
                    style: const TextStyle(
                      fontSize: 12,
                      color: AppColors.textSecondary,
                      fontWeight: FontWeight.w600,
                    ),
                  ),
                ],
              ),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildBottomButtons() {
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
              height: 54,
              child: OutlinedButton(
                style: OutlinedButton.styleFrom(
                  foregroundColor: AppColors.textSecondary,
                  side: const BorderSide(color: AppColors.border),
                  shape: RoundedRectangleBorder(
                    borderRadius: BorderRadius.circular(16),
                  ),
                ),
                onPressed: _resetMenus,
                child: const Text(
                  '초기화',
                  style: TextStyle(
                    fontWeight: FontWeight.w900,
                  ),
                ),
              ),
            ),
          ),
          const SizedBox(width: 12),
          Expanded(
            child: SizedBox(
              height: 54,
              child: ElevatedButton(
                style: ElevatedButton.styleFrom(
                  backgroundColor: AppColors.primaryRed,
                  foregroundColor: AppColors.white,
                  elevation: 0,
                  shape: RoundedRectangleBorder(
                    borderRadius: BorderRadius.circular(16),
                  ),
                ),
                onPressed: _saveMenus,
                child: const Text(
                  '저장',
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

  Widget _buildContent() {
    return SingleChildScrollView(
      padding: const EdgeInsets.fromLTRB(22, 18, 22, 24),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          _buildHeader(),
          const SizedBox(height: 18),
          _buildSelectedCountCard(),
          const SizedBox(height: 14),
          _buildSlotGridSection(),
          const SizedBox(height: 14),
          _buildAvailableMenuSection(),
          const SizedBox(height: 14),
          _buildViewTypeSection(),
          const SizedBox(height: 90),
        ],
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    if (_loading) {
      return const Scaffold(
        backgroundColor: AppColors.background,
        body: Center(
          child: CircularProgressIndicator(
            color: AppColors.primaryRed,
          ),
        ),
      );
    }

    return Scaffold(
      backgroundColor: AppColors.background,
      appBar: AppBar(
        backgroundColor: AppColors.background,
        elevation: 0,
        foregroundColor: AppColors.textPrimary,
        title: const Text(
          '빠른 메뉴 편집',
          style: TextStyle(
            fontWeight: FontWeight.w900,
          ),
        ),
      ),
      body: _buildContent(),
      bottomNavigationBar: _buildBottomButtons(),
    );
  }
}

class _QuickMenuEditItem {
  final String id;
  final String title;
  final String subtitle;
  final IconData icon;

  _QuickMenuEditItem({
    required this.id,
    required this.title,
    required this.subtitle,
    required this.icon,
  });
}