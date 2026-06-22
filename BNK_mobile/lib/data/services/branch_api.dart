import '../../core/constants/api_constants.dart';
import '../models/branch_model.dart';
import '../models/branch_reservation_model.dart';
import 'api_client.dart';

class BranchApi {
  final ApiClient _apiClient = ApiClient.instance;

  Future<List<BranchModel>> getBranches() async {
    final response = await _apiClient.get(
      ApiConstants.reservationBranches,
      useToken: true,
    );

    final data = response['data'];

    if (data is List) {
      return data
          .map((item) => BranchModel.fromJson(item as Map<String, dynamic>))
          .toList();
    }

    return [];
  }

  Future<List<String>> getBookedSlots({
    required int branchId,
    required String date,
  }) async {
    final query = Uri(
      queryParameters: {
        'branchId': branchId.toString(),
        'date': date,
      },
    ).query;

    final response = await _apiClient.get(
      '${ApiConstants.reservationSlots}?$query',
      useToken: true,
    );

    final data = response['data'];

    if (data is List) {
      return data.map((item) => item.toString()).toList();
    }

    return [];
  }

  Future<List<BranchReservationModel>> getMyReservations() async {
    final response = await _apiClient.get(
      ApiConstants.reservationList,
      useToken: true,
    );

    final data = response['data'];

    if (data is List) {
      return data
          .map(
            (item) => BranchReservationModel.fromJson(
              item as Map<String, dynamic>,
            ),
          )
          .toList();
    }

    return [];
  }

  Future<void> createReservation({
    required int branchId,
    required String reservedAt,
    required String bizType,
    required String purpose,
  }) async {
    final query = Uri(
      queryParameters: {
        'branchId': branchId.toString(),
        'reservedAt': reservedAt,
        'bizType': bizType,
        'purpose': purpose,
      },
    ).query;

    await _apiClient.post(
      '${ApiConstants.reservationCreate}?$query',
      body: {},
      useToken: true,
    );
  }

  Future<void> cancelReservation({
    required int reservationId,
  }) async {
    final query = Uri(
      queryParameters: {
        'reservationId': reservationId.toString(),
      },
    ).query;

    await _apiClient.post(
      '${ApiConstants.reservationCancel}?$query',
      body: {},
      useToken: true,
    );
  }
}