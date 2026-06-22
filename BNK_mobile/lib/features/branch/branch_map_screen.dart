import 'package:flutter/material.dart';

import '../../core/constants/app_colors.dart';
import '../../data/models/branch_model.dart';
import '../../data/services/branch_api.dart';
import 'branch_reservation_screen.dart';

class BranchMapScreen extends StatefulWidget {
  const BranchMapScreen({super.key});

  @override
  State<BranchMapScreen> createState() => _BranchMapScreenState();
}

class _BranchMapScreenState extends State<BranchMapScreen> {
  final BranchApi _branchApi = BranchApi();
  final TextEditingController _searchController = TextEditingController();

  late Future<List<BranchModel>> _branchFuture;

  List<BranchModel> _allBranches = [];
  List<BranchModel> _filteredBranches = [];

  @override
  void initState() {
    super.initState();
    _branchFuture = _loadBranches();
  }

  @override
  void dispose() {
    _searchController.dispose();
    super.dispose();
  }

  Future<List<BranchModel>> _loadBranches() async {
    final branches = await _branchApi.getBranches();

    _allBranches = branches;
    _filteredBranches = branches;

    return branches;
  }

  void _filterBranches(String keyword) {
    final query = keyword.trim().toLowerCase();

    setState(() {
      if (query.isEmpty) {
        _filteredBranches = _allBranches;
        return;
      }

      _filteredBranches = _allBranches.where((branch) {
        return branch.branchName.toLowerCase().contains(query) ||
            branch.address.toLowerCase().contains(query) ||
            branch.branchCode.toLowerCase().contains(query);
      }).toList();
    });
  }

  void _showPreparingMessage(String message) {
    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(
        content: Text(message),
        behavior: SnackBarBehavior.floating,
      ),
    );
  }

  void _goToReservation(BranchModel branch) {
    Navigator.push(
      context,
      MaterialPageRoute(
        builder: (_) => BranchReservationScreen(
          branch: branch,
        ),
      ),
    );
  }

  Widget _buildSearchBox() {
    return Container(
      margin: const EdgeInsets.fromLTRB(22, 16, 22, 12),
      padding: const EdgeInsets.symmetric(horizontal: 16),
      decoration: BoxDecoration(
        color: AppColors.cardBackground,
        borderRadius: BorderRadius.circular(18),
        border: Border.all(color: AppColors.border),
      ),
      child: TextField(
        controller: _searchController,
        onChanged: _filterBranches,
        decoration: const InputDecoration(
          border: InputBorder.none,
          icon: Icon(
            Icons.search_rounded,
            color: AppColors.textSecondary,
          ),
          hintText: '영업점명 또는 주소를 검색하세요',
          hintStyle: TextStyle(
            color: AppColors.textSecondary,
            fontSize: 14,
            fontWeight: FontWeight.w600,
          ),
        ),
      ),
    );
  }

  Widget _buildMapMockBox() {
    return Container(
      width: double.infinity,
      margin: const EdgeInsets.fromLTRB(22, 0, 22, 18),
      padding: const EdgeInsets.all(22),
      decoration: BoxDecoration(
        color: const Color(0xFF111827),
        borderRadius: BorderRadius.circular(26),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          const Row(
            children: [
              Icon(
                Icons.location_on_rounded,
                color: AppColors.primaryRed,
                size: 28,
              ),
              SizedBox(width: 8),
              Text(
                '가까운 영업점 지도',
                style: TextStyle(
                  fontSize: 20,
                  fontWeight: FontWeight.w900,
                  color: AppColors.white,
                ),
              ),
            ],
          ),
          const SizedBox(height: 12),
          const Text(
            '지도 API 연동 전 단계입니다.\n현재는 영업점 위치 데이터를 API로 불러와 목록에 표시합니다.',
            style: TextStyle(
              fontSize: 13,
              height: 1.5,
              color: Color(0xFFB8C3D6),
              fontWeight: FontWeight.w600,
            ),
          ),
          const SizedBox(height: 18),
          Container(
            height: 130,
            width: double.infinity,
            decoration: BoxDecoration(
              color: const Color(0xFF1F2937),
              borderRadius: BorderRadius.circular(20),
              border: Border.all(
                color: Colors.white.withOpacity(0.08),
              ),
            ),
            child: Stack(
              children: [
                Positioned(
                  left: 28,
                  top: 32,
                  child: _buildMapPin('본점'),
                ),
                Positioned(
                  right: 48,
                  top: 24,
                  child: _buildMapPin('수영'),
                ),
                Positioned(
                  left: 120,
                  bottom: 26,
                  child: _buildMapPin('서면'),
                ),
                Positioned(
                  right: 96,
                  bottom: 30,
                  child: _buildMapPin('동래'),
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildMapPin(String label) {
    return Column(
      children: [
        Container(
          width: 34,
          height: 34,
          decoration: const BoxDecoration(
            color: AppColors.primaryRed,
            shape: BoxShape.circle,
          ),
          child: const Icon(
            Icons.location_on_rounded,
            color: AppColors.white,
            size: 21,
          ),
        ),
        const SizedBox(height: 4),
        Text(
          label,
          style: const TextStyle(
            color: AppColors.white,
            fontSize: 11,
            fontWeight: FontWeight.w800,
          ),
        ),
      ],
    );
  }

  Widget _buildBranchCard(BranchModel branch) {
    final isActive = branch.status == 'ACTIVE';

    return Container(
      margin: const EdgeInsets.fromLTRB(22, 0, 22, 14),
      padding: const EdgeInsets.all(18),
      decoration: BoxDecoration(
        color: AppColors.cardBackground,
        borderRadius: BorderRadius.circular(24),
        border: Border.all(color: AppColors.border),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            children: [
              Container(
                width: 46,
                height: 46,
                decoration: BoxDecoration(
                  color: const Color(0xFFFFF0F0),
                  borderRadius: BorderRadius.circular(16),
                ),
                child: const Icon(
                  Icons.account_balance_rounded,
                  color: AppColors.primaryRed,
                  size: 25,
                ),
              ),
              const SizedBox(width: 13),
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      branch.branchName,
                      style: const TextStyle(
                        fontSize: 17,
                        fontWeight: FontWeight.w900,
                        color: AppColors.textPrimary,
                      ),
                    ),
                    const SizedBox(height: 5),
                    Text(
                      branch.branchCode,
                      style: const TextStyle(
                        fontSize: 12,
                        fontWeight: FontWeight.w700,
                        color: AppColors.textSecondary,
                      ),
                    ),
                  ],
                ),
              ),
              Container(
                padding: const EdgeInsets.symmetric(
                  horizontal: 10,
                  vertical: 6,
                ),
                decoration: BoxDecoration(
                  color: isActive
                      ? const Color(0xFFFFF0F0)
                      : const Color(0xFFF1F3F5),
                  borderRadius: BorderRadius.circular(999),
                ),
                child: Text(
                  isActive ? '운영중' : '중지',
                  style: TextStyle(
                    fontSize: 11,
                    fontWeight: FontWeight.w900,
                    color: isActive
                        ? AppColors.primaryRed
                        : AppColors.textSecondary,
                  ),
                ),
              ),
            ],
          ),
          const SizedBox(height: 16),
          _buildInfoRow(
            icon: Icons.place_outlined,
            text: branch.address,
          ),
          const SizedBox(height: 8),
          _buildInfoRow(
            icon: Icons.call_outlined,
            text: branch.phoneNumber,
          ),
          const SizedBox(height: 8),
          _buildInfoRow(
            icon: Icons.map_outlined,
            text:
                '위도 ${branch.latitude.toStringAsFixed(4)} · 경도 ${branch.longitude.toStringAsFixed(4)}',
          ),
          const SizedBox(height: 16),
          Row(
            children: [
              Expanded(
                child: OutlinedButton(
                  style: OutlinedButton.styleFrom(
                    foregroundColor: AppColors.textPrimary,
                    side: const BorderSide(color: AppColors.border),
                    padding: const EdgeInsets.symmetric(vertical: 13),
                    shape: RoundedRectangleBorder(
                      borderRadius: BorderRadius.circular(14),
                    ),
                  ),
                  onPressed: () {
                    _showPreparingMessage('지도 상세 화면은 이후 연결 예정입니다.');
                  },
                  child: const Text(
                    '위치 보기',
                    style: TextStyle(
                      fontWeight: FontWeight.w900,
                    ),
                  ),
                ),
              ),
              const SizedBox(width: 10),
              Expanded(
                child: ElevatedButton(
                  style: ElevatedButton.styleFrom(
                    backgroundColor: AppColors.primaryRed,
                    foregroundColor: AppColors.white,
                    elevation: 0,
                    padding: const EdgeInsets.symmetric(vertical: 13),
                    shape: RoundedRectangleBorder(
                      borderRadius: BorderRadius.circular(14),
                    ),
                  ),
                  onPressed: () {
                    _goToReservation(branch);
                  },
                  child: const Text(
                    '상담예약',
                    style: TextStyle(
                      fontWeight: FontWeight.w900,
                    ),
                  ),
                ),
              ),
            ],
          ),
        ],
      ),
    );
  }

  Widget _buildInfoRow({
    required IconData icon,
    required String text,
  }) {
    return Row(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Icon(
          icon,
          size: 18,
          color: AppColors.textSecondary,
        ),
        const SizedBox(width: 8),
        Expanded(
          child: Text(
            text,
            style: const TextStyle(
              fontSize: 13,
              height: 1.35,
              color: AppColors.textSecondary,
              fontWeight: FontWeight.w600,
            ),
          ),
        ),
      ],
    );
  }

  Widget _buildEmptyView() {
    return Container(
      width: double.infinity,
      margin: const EdgeInsets.fromLTRB(22, 20, 22, 0),
      padding: const EdgeInsets.all(24),
      decoration: BoxDecoration(
        color: AppColors.cardBackground,
        borderRadius: BorderRadius.circular(24),
        border: Border.all(color: AppColors.border),
      ),
      child: const Column(
        children: [
          Icon(
            Icons.search_off_rounded,
            size: 42,
            color: AppColors.textSecondary,
          ),
          SizedBox(height: 12),
          Text(
            '검색 결과가 없습니다.',
            style: TextStyle(
              fontSize: 16,
              fontWeight: FontWeight.w900,
              color: AppColors.textPrimary,
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildErrorView(Object error) {
    return Center(
      child: Padding(
        padding: const EdgeInsets.all(22),
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
                Icons.error_outline_rounded,
                color: AppColors.primaryRed,
                size: 42,
              ),
              const SizedBox(height: 14),
              const Text(
                '영업점 정보를 불러오지 못했습니다.',
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
                    _branchFuture = _loadBranches();
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

  Widget _buildContent() {
    return FutureBuilder<List<BranchModel>>(
      future: _branchFuture,
      builder: (context, snapshot) {
        if (snapshot.connectionState == ConnectionState.waiting) {
          return const Center(
            child: CircularProgressIndicator(
              color: AppColors.primaryRed,
            ),
          );
        }

        if (snapshot.hasError) {
          return _buildErrorView(snapshot.error!);
        }

        return SingleChildScrollView(
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              _buildSearchBox(),
              _buildMapMockBox(),
              const Padding(
                padding: EdgeInsets.fromLTRB(22, 2, 22, 14),
                child: Text(
                  '가까운 영업점',
                  style: TextStyle(
                    fontSize: 19,
                    fontWeight: FontWeight.w900,
                    color: AppColors.textPrimary,
                  ),
                ),
              ),
              if (_filteredBranches.isEmpty)
                _buildEmptyView()
              else
                ..._filteredBranches.map(_buildBranchCard),
              const SizedBox(height: 24),
            ],
          ),
        );
      },
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
          '영업점 찾기',
          style: TextStyle(
            fontWeight: FontWeight.w900,
          ),
        ),
      ),
      body: _buildContent(),
    );
  }
}