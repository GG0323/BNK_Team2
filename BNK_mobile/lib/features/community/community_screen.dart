import 'package:flutter/material.dart';

import '../../core/constants/app_colors.dart';
import '../../core/storage/community_storage.dart';
import '../../core/storage/secure_storage.dart';
import '../../data/models/community_profile_model.dart';
import '../../data/services/community_api.dart';
import '../auth/login_screen.dart';
import 'community_board_tab.dart';
import 'community_home_tab.dart';
import 'community_match_tab.dart';
import 'community_more_sheet.dart';

class CommunityScreen extends StatefulWidget {
  const CommunityScreen({super.key});

  @override
  State<CommunityScreen> createState() => _CommunityScreenState();
}

class _CommunityScreenState extends State<CommunityScreen> {
  final CommunityApi _communityApi = CommunityApi();

  int _selectedIndex = 0;
  CommunityProfileModel _profile = const CommunityProfileModel(isMember: false);
  bool _isLoading = true;
  String? _errorMessage;

  @override
  void initState() {
    super.initState();
    _ensureAuthenticatedAndLoad();
  }

  Future<void> _ensureAuthenticatedAndLoad() async {
    setState(() {
      _isLoading = true;
      _errorMessage = null;
    });

    try {
      final hasSession = await _ensureLoggedIn();
      if (!hasSession) return;

      await _refreshProfile();
    } catch (error) {
      final cachedProfile = await CommunityStorage.getProfile();

      if (!mounted) return;

      setState(() {
        _profile = cachedProfile;
        _errorMessage = error.toString().replaceFirst('Exception: ', '');
      });
    } finally {
      if (mounted) {
        setState(() {
          _isLoading = false;
        });
      }
    }
  }

  Future<bool> _ensureLoggedIn() async {
    final authCookie = await SecureStorage.getAuthCookie();

    if (authCookie != null && authCookie.isNotEmpty) {
      return true;
    }

    if (!mounted) return false;

    final loggedIn = await Navigator.push<bool>(
      context,
      MaterialPageRoute(
        builder: (_) => const LoginScreen(returnOnSuccess: true),
      ),
    );

    if (loggedIn != true && mounted) {
      Navigator.pop(context);
    }

    return loggedIn == true;
  }

  Future<void> _refreshProfile() async {
    final profile = await _communityApi.checkCurrentMember();

    if (!mounted) return;

    setState(() {
      _profile = profile;
      _errorMessage = null;
    });
  }

  void _showMoreMenu() {
    showModalBottomSheet(
      context: context,
      isScrollControlled: true,
      backgroundColor: Colors.transparent,
      builder: (_) {
        return CommunityMoreSheet(
          profile: _profile,
          onProfileChanged: _refreshProfile,
        );
      },
    );
  }

  List<Widget> get _pages {
    return [
      CommunityHomeTab(
        onTabChanged: (index) {
          setState(() {
            _selectedIndex = index;
          });
        },
      ),
      const CommunityMatchTab(),
      const CommunityBoardTab(),
    ];
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: AppColors.background,
      appBar: AppBar(
        backgroundColor: AppColors.cardBackground,
        foregroundColor: AppColors.textPrimary,
        elevation: 0,
        title: Row(
          children: [
            Image.asset(
              'assets/community/fearx_logo.png',
              width: 34,
              height: 34,
              fit: BoxFit.contain,
            ),
            const SizedBox(width: 10),
            const Text.rich(
              TextSpan(
                children: [
                  TextSpan(
                    text: 'BNK ',
                    style: TextStyle(
                      color: AppColors.primaryRed,
                      fontWeight: FontWeight.w900,
                    ),
                  ),
                  TextSpan(
                    text: 'FearX',
                    style: TextStyle(
                      color: AppColors.textPrimary,
                      fontWeight: FontWeight.w900,
                    ),
                  ),
                ],
              ),
            ),
          ],
        ),
        actions: [
          IconButton(
            tooltip: '닫기',
            icon: const Icon(Icons.close_rounded),
            onPressed: () => Navigator.pop(context),
          ),
        ],
      ),
      body: SafeArea(
        child: _isLoading
            ? const Center(
                child: CircularProgressIndicator(color: AppColors.primaryRed),
              )
            : Column(
                children: [
                  if (_errorMessage != null)
                    Container(
                      width: double.infinity,
                      margin: const EdgeInsets.fromLTRB(18, 14, 18, 0),
                      padding: const EdgeInsets.all(12),
                      decoration: BoxDecoration(
                        color: const Color(0xFFFFF0F0),
                        borderRadius: BorderRadius.circular(12),
                        border: Border.all(
                          color: AppColors.primaryRed.withValues(alpha: 0.18),
                        ),
                      ),
                      child: Text(
                        _errorMessage!,
                        style: const TextStyle(
                          color: AppColors.primaryRed,
                          fontSize: 12,
                          fontWeight: FontWeight.w700,
                        ),
                      ),
                    ),
                  Expanded(child: _pages[_selectedIndex]),
                ],
              ),
      ),
      bottomNavigationBar: NavigationBar(
        selectedIndex: _selectedIndex,
        onDestinationSelected: (index) {
          if (index == 3) {
            _showMoreMenu();
            return;
          }

          setState(() {
            _selectedIndex = index;
          });
        },
        destinations: const [
          NavigationDestination(
            icon: Icon(Icons.home_outlined),
            selectedIcon: Icon(Icons.home_rounded),
            label: '홈',
          ),
          NavigationDestination(
            icon: Icon(Icons.calendar_today_outlined),
            selectedIcon: Icon(Icons.calendar_today_rounded),
            label: '경기',
          ),
          NavigationDestination(
            icon: Icon(Icons.chat_bubble_outline),
            selectedIcon: Icon(Icons.chat_bubble),
            label: '게시판',
          ),
          NavigationDestination(
            icon: Icon(Icons.more_horiz),
            selectedIcon: Icon(Icons.more_horiz),
            label: '더보기',
          ),
        ],
      ),
    );
  }
}
