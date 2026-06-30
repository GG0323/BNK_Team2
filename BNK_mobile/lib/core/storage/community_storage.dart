import 'package:shared_preferences/shared_preferences.dart';

import '../../data/models/community_profile_model.dart';

class CommunityStorage {
  static const String _isMemberKey = 'community_has_account';
  static const String _nicknameKey = 'community_nickname';
  static const String _memberNoKey = 'community_member_no';
  static const String _communityAccountNoKey = 'community_account_no';

  static Future<CommunityProfileModel> getProfile() async {
    final prefs = await SharedPreferences.getInstance();

    return CommunityProfileModel(
      isMember: prefs.getBool(_isMemberKey) ?? false,
      communityAccountNo: prefs.getInt(_communityAccountNoKey) ?? 0,
      nickname: prefs.getString(_nicknameKey),
    );
  }

  static Future<void> saveProfile({
    required int memberNo,
    required CommunityProfileModel profile,
  }) async {
    final prefs = await SharedPreferences.getInstance();

    await prefs.setInt(_memberNoKey, memberNo);
    await prefs.setInt(_communityAccountNoKey, profile.communityAccountNo);
    await prefs.setBool(_isMemberKey, profile.isMember);

    final nickname = profile.nickname;
    if (nickname == null || nickname.isEmpty) {
      await prefs.remove(_nicknameKey);
    } else {
      await prefs.setString(_nicknameKey, nickname);
    }
  }

  static Future<void> clearProfile() async {
    final prefs = await SharedPreferences.getInstance();
    await prefs.remove(_isMemberKey);
    await prefs.remove(_nicknameKey);
    await prefs.remove(_memberNoKey);
    await prefs.remove(_communityAccountNoKey);
  }
}
