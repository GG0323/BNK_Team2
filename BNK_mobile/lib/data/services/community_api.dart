import '../../core/constants/api_constants.dart';
import '../../core/storage/community_storage.dart';
import '../models/community_board_model.dart';
import '../models/community_profile_model.dart';
import '../models/community_reply_model.dart';
import '../models/member_model.dart';
import 'api_client.dart';
import 'auth_api.dart';

class CommunityApi {
  final ApiClient _apiClient = ApiClient.instance;
  final AuthApi _authApi = AuthApi();

  Future<MemberModel> getCurrentMember() {
    return _authApi.getMe();
  }

  Future<CommunityProfileModel> checkCurrentMember() async {
    final member = await getCurrentMember();
    final response = await _apiClient.get(
      ApiConstants.communityMe,
      useAuthCookie: true,
    );

    final profile = CommunityProfileModel.fromJson(response);
    await CommunityStorage.saveProfile(
      memberNo: member.memberNo,
      profile: profile,
    );

    return profile;
  }

  Future<CommunityProfileModel> registerCurrentMember({
    required String nickname,
  }) async {
    final member = await getCurrentMember();
    final response = await _apiClient.post(
      ApiConstants.communityRegister,
      useAuthCookie: true,
      body: {'nickname': nickname},
    );

    final profile = CommunityProfileModel.fromJson(response);
    await CommunityStorage.saveProfile(
      memberNo: member.memberNo,
      profile: profile,
    );

    return profile;
  }

  Future<CommunityProfileModel> updateNickname({
    required String nickname,
  }) async {
    final member = await getCurrentMember();
    final response = await _apiClient.put(
      ApiConstants.communityNickname,
      useAuthCookie: true,
      body: {'nickname': nickname},
    );

    final profile = CommunityProfileModel.fromJson(response);
    await CommunityStorage.saveProfile(
      memberNo: member.memberNo,
      profile: profile,
    );

    return profile;
  }

  Future<List<CommunityBoardModel>> getBoards({
    String sort = 'latest',
    String? keyword,
  }) async {
    final query = <String>['sort=$sort'];
    final trimmedKeyword = keyword?.trim();

    if (trimmedKeyword != null && trimmedKeyword.isNotEmpty) {
      query.add('keyword=${Uri.encodeQueryComponent(trimmedKeyword)}');
    }

    final response = await _apiClient.get(
      '${ApiConstants.communityBoards}?${query.join('&')}',
      useAuthCookie: true,
    );

    return _listFromResponse(response)
        .whereType<Map<String, dynamic>>()
        .map(CommunityBoardModel.fromJson)
        .toList();
  }

  Future<CommunityBoardModel> getBoard(int boardNo) async {
    final response = await _apiClient.get(
      ApiConstants.communityBoard(boardNo),
      useAuthCookie: true,
    );

    return CommunityBoardModel.fromJson(_dataMap(response));
  }

  Future<CommunityBoardModel> createBoard({
    required String title,
    required String content,
  }) async {
    final response = await _apiClient.post(
      ApiConstants.communityBoards,
      useAuthCookie: true,
      body: {'title': title, 'content': content},
    );

    return CommunityBoardModel.fromJson(_dataMap(response));
  }

  Future<CommunityBoardModel> likeBoard(int boardNo) async {
    final response = await _apiClient.post(
      ApiConstants.communityBoardLike(boardNo),
      useAuthCookie: true,
    );

    return CommunityBoardModel.fromJson(_dataMap(response));
  }

  Future<void> deleteBoard(int boardNo) async {
    await _apiClient.delete(
      ApiConstants.communityBoard(boardNo),
      useAuthCookie: true,
    );
  }

  Future<List<CommunityReplyModel>> getReplies(int boardNo) async {
    final response = await _apiClient.get(
      ApiConstants.communityReplies(boardNo),
      useAuthCookie: true,
    );

    return _listFromResponse(response)
        .whereType<Map<String, dynamic>>()
        .map(CommunityReplyModel.fromJson)
        .toList();
  }

  Future<CommunityReplyModel> createReply({
    required int boardNo,
    required String content,
  }) async {
    final response = await _apiClient.post(
      ApiConstants.communityReplies(boardNo),
      useAuthCookie: true,
      body: {'content': content},
    );

    return CommunityReplyModel.fromJson(_dataMap(response));
  }

  Future<void> deleteReply(int replyNo) async {
    await _apiClient.delete(
      ApiConstants.communityReply(replyNo),
      useAuthCookie: true,
    );
  }

  Map<String, dynamic> _dataMap(Map<String, dynamic> response) {
    final data = response['data'];
    if (data is Map<String, dynamic>) return data;
    return response;
  }

  List<dynamic> _listFromResponse(Map<String, dynamic> response) {
    final data = response['data'];
    if (data is List) return data;
    if (response['items'] is List) return response['items'] as List;
    return const [];
  }
}
