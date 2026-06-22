import 'package:shared_preferences/shared_preferences.dart';

class QuickMenuStorage {
  static const String _slotsKey = 'quick_menu_slots';
  static const String _viewTypeKey = 'quick_menu_view_type';

  static const String emptySlotId = '__EMPTY__';

  static const List<String> defaultMenuIds = [
    'account',
    'transfer',
    'deposit',
    'saving',
    'recommend',
    'branch',
    'reservation',
    'customer',
  ];

  static List<String> get defaultSlotIds {
    return List<String>.from(defaultMenuIds);
  }

  static List<String> _normalizeSlots(List<String> slots) {
    final normalized = List<String>.from(slots);

    while (normalized.length < 8) {
      normalized.add(emptySlotId);
    }

    if (normalized.length > 8) {
      return normalized.sublist(0, 8);
    }

    return normalized;
  }

  static Future<List<String>> getMenuSlotIds() async {
    final prefs = await SharedPreferences.getInstance();
    final saved = prefs.getStringList(_slotsKey);

    if (saved == null || saved.isEmpty) {
      return defaultSlotIds;
    }

    return _normalizeSlots(saved);
  }

  static Future<void> saveMenuSlotIds(List<String> slotIds) async {
    final prefs = await SharedPreferences.getInstance();
    await prefs.setStringList(_slotsKey, _normalizeSlots(slotIds));
  }

  static Future<List<String>> getMenuIds() async {
    final slots = await getMenuSlotIds();

    return slots
        .where((id) => id != emptySlotId)
        .toList();
  }

  static Future<void> saveMenuIds(List<String> ids) async {
    final slots = List<String>.from(ids);

    while (slots.length < 8) {
      slots.add(emptySlotId);
    }

    await saveMenuSlotIds(slots);
  }

  static Future<String> getViewType() async {
    final prefs = await SharedPreferences.getInstance();
    return prefs.getString(_viewTypeKey) ?? 'GRID';
  }

  static Future<void> saveViewType(String viewType) async {
    final prefs = await SharedPreferences.getInstance();
    await prefs.setString(_viewTypeKey, viewType);
  }

  static Future<void> reset() async {
    final prefs = await SharedPreferences.getInstance();
    await prefs.remove(_slotsKey);
    await prefs.remove(_viewTypeKey);
  }
}